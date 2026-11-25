package net.kdt.pojavlaunch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

/**
 * Lowers the native frame-rate cap when the battery is low (and not charging) or the device is
 * heavily thermal-throttled, on top of whatever cap the user picked in Settings. It only ever
 * makes the cap STRICTER, never looser (see perf_tuning.c) — it can never push the frame rate
 * above what the user themselves chose. Purely a safety net: if a check is unsupported on this
 * API level, or fails for any reason, it just does nothing and leaves the user's cap untouched.
 */
public class AdaptiveFpsController {
    private static final String TAG = "AdaptiveFpsController";
    private static final long CHECK_INTERVAL_MS = 8000;
    private static final int LOW_BATTERY_PERCENT = 20;
    private static final int THROTTLED_CAP_FPS = 30;

    private final Activity mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mRunning = false;
    private int mLastAppliedCap = -1; // -1 = "unknown yet", so the very first check always applies something

    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            check();
            mHandler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    public AdaptiveFpsController(Activity activity) {
        mActivity = activity;
    }

    public void start() {
        if (mRunning) return;
        mRunning = true;
        mLastAppliedCap = -1;
        mHandler.post(mTick);
    }

    /** Stops polling. Deliberately leaves the last applied native override in place: this is also
     *  called on a plain pause, and clearing/reapplying on every pause-resume would be wasted work
     *  (the override is harmless if the process is about to go away, and gets reset on next launch). */
    public void stop() {
        mRunning = false;
        mHandler.removeCallbacks(mTick);
    }

    private void check() {
        boolean shouldThrottle = isBatteryLow() || isThermallyThrottled();
        int cap = shouldThrottle ? THROTTLED_CAP_FPS : 0;
        if (cap == mLastAppliedCap) return;
        mLastAppliedCap = cap;
        try {
            JREUtils.setAdaptiveFpsCap(cap);
            Log.i(TAG, shouldThrottle ? "Adaptive cap engaged: " + THROTTLED_CAP_FPS + " FPS" : "Adaptive cap cleared");
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Native adaptive cap unavailable, stopping", e);
            stop();
        }
    }

    private boolean isBatteryLow() {
        try {
            Intent batteryStatus = mActivity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryStatus == null) return false;
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            if (level < 0 || scale <= 0 || charging) return false;
            int percent = Math.round(level * 100f / scale);
            return percent <= LOW_BATTERY_PERCENT;
        } catch (Exception e) {
            Log.w(TAG, "Could not read battery status", e);
            return false;
        }
    }

    private boolean isThermallyThrottled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false; // getCurrentThermalStatus() needs API 29+
        try {
            PowerManager powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
            if (powerManager == null) return false;
            // THERMAL_STATUS_SEVERE and above: the system is already cutting performance elsewhere too
            return powerManager.getCurrentThermalStatus() >= PowerManager.THERMAL_STATUS_SEVERE;
        } catch (Exception e) {
            Log.w(TAG, "Could not read thermal status", e);
            return false;
        }
    }
}
