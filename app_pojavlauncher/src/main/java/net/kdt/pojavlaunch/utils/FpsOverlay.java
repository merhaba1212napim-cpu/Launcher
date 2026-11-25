package net.kdt.pojavlaunch.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Small on-screen counter that shows the frame rate of the GAME (frames swapped by the render
 * thread, counted natively in perf_tuning.c), as opposed to the AWT canvas counter that
 * AWTCanvasView draws for Java AWT windows.
 *
 * It ignores touches, so it never gets in the way of the controls.
 */
public class FpsOverlay {
    private static final String TAG = "FpsOverlay";
    private static final long UPDATE_INTERVAL_MS = 500;

    private final TextView mView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mRunning = false;
    private long mLastCount = -1;
    private long mLastTimeMs;

    // Rolling window of recent per-tick FPS samples, to show the worst recent moment next to the
    // live number. This is an approximation of a frame-time "1% low": it is the slowest of the
    // last ~10s of half-second samples, not a true per-frame percentile (that would need the
    // native side to record every single frame's timestamp, which this overlay does not do).
    private static final int RECENT_WINDOW = 20;
    private final float[] mRecentFps = new float[RECENT_WINDOW];
    private int mRecentFpsCount = 0;
    private int mRecentFpsNext = 0;

    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            update();
            if (mRunning) mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    public FpsOverlay(Activity activity) {
        mView = new TextView(activity);
        mView.setTextColor(Color.WHITE);
        mView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mView.setShadowLayer(2f, 1f, 1f, Color.BLACK);
        mView.setBackgroundColor(0x66000000);
        mView.setClickable(false);
        mView.setFocusable(false);
        int horizontal = dp(activity, 6);
        int vertical = dp(activity, 2);
        mView.setPadding(horizontal, vertical, horizontal, vertical);
        mView.setText("-- FPS");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        ViewGroup content = activity.findViewById(android.R.id.content);
        content.addView(mView, params);
    }

    private static int dp(Activity activity, int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                activity.getResources().getDisplayMetrics()));
    }

    public void start() {
        if (mRunning) return;
        mRunning = true;
        mLastCount = -1; // measure from scratch so time spent in the background is not averaged in
        mRecentFpsCount = 0;
        mRecentFpsNext = 0;
        mHandler.post(mTick);
    }

    public void stop() {
        mRunning = false;
        mHandler.removeCallbacks(mTick);
    }

    /** Stops updating and takes the view off the screen. */
    public void remove() {
        stop();
        View parent = (View) mView.getParent();
        if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(mView);
    }

    private void update() {
        long count;
        try {
            count = JREUtils.getFrameCount();
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Native frame counter unavailable", e);
            mView.setText("FPS n/a");
            stop();
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (mLastCount >= 0 && now > mLastTimeMs && count > 0) {
            long frames = Math.max(0, count - mLastCount);
            float fps = (float) (frames * 1000.0 / (now - mLastTimeMs));
            mRecentFps[mRecentFpsNext] = fps;
            mRecentFpsNext = (mRecentFpsNext + 1) % RECENT_WINDOW;
            if (mRecentFpsCount < RECENT_WINDOW) mRecentFpsCount++;

            if (mRecentFpsCount >= 4) {
                float worst = mRecentFps[0];
                for (int i = 1; i < mRecentFpsCount; i++) worst = Math.min(worst, mRecentFps[i]);
                mView.setText(String.format(Locale.US, "%.0f FPS (en kötü %.0f)", fps, worst));
            } else {
                mView.setText(String.format(Locale.US, "%.0f FPS", fps));
            }
        } else if (count == 0) {
            mView.setText("-- FPS"); // the game has not drawn anything yet
        }
        mLastCount = count;
        mLastTimeMs = now;
    }
}
