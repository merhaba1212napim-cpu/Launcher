package net.kdt.pojavlaunch.prefs.screens;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.GLInfoUtils;
import net.kdt.pojavlaunch.utils.VersionCompatibilityPredictor;

import java.util.Locale;

/**
 * Fragment for any settings video related
 */
public class LauncherPreferenceVideoFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video);
        //Disable notch checking behavior on android 8.1 and below.
        requirePreference("ignoreNotch").setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0);

        // Sustained performance is only available since Nougat
        SwitchPreference sustainedPerfSwitch = requirePreference("sustainedPerformance",
                SwitchPreference.class);
        sustainedPerfSwitch.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
        sustainedPerfSwitch.setChecked(LauncherPreferences.PREF_SUSTAINED_PERFORMANCE);

        requirePreference("alternate_surface", SwitchPreferenceCompat.class).setChecked(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
        requirePreference("force_vsync", SwitchPreferenceCompat.class).setChecked(LauncherPreferences.PREF_FORCE_VSYNC);

        computeVisibility();
        setupDeviceCompatInfo();
    }

    /** Read-only device card: name, chip, memory, GPU and a rough version estimate (see VersionCompatibilityPredictor). */
    private void setupDeviceCompatInfo() {
        Preference infoPref = requirePreference("deviceCompatInfo");
        int cores = java.lang.Runtime.getRuntime().availableProcessors();
        long ramMb = -1;
        Context context = getContext();
        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(memoryInfo);
                ramMb = memoryInfo.totalMem / (1024 * 1024);
            }
        }
        boolean is64Bit = Build.SUPPORTED_64_BIT_ABIS.length > 0;

        int maxJava = 0;
        for (net.kdt.pojavlaunch.multirt.Runtime rt : MultiRTUtils.getRuntimes()) {
            if (rt.javaVersion > maxJava) maxJava = rt.javaVersion;
        }
        VersionCompatibilityPredictor.Breakpoint best =
                VersionCompatibilityPredictor.highestLikelyPlayable(maxJava, ramMb);
        String bestText = best != null ? best.label : "tespit edilemedi";
        String ramText = ramMb > 0 ? (ramMb + " MB") : "bilinmiyor";
        String device = Build.MANUFACTURER + " " + Build.MODEL;
        String chip = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !Build.UNKNOWN.equals(Build.SOC_MODEL))
                ? Build.SOC_MODEL : Build.HARDWARE;

        infoPref.setSummary(buildDeviceSummary(device, chip, ramText, cores, is64Bit, "ölçülüyor...", bestText));
        infoPref.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(),
                    "Bu kaba bir tahmindir; sürücü ve mod uyumluluğu hesaba katılmaz.",
                    Toast.LENGTH_LONG).show();
            return true;
        });

        // The GPU name needs a GL context, which is created off the UI thread, then shown on it.
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String gpu;
            try {
                gpu = GLInfoUtils.getGlInfo().renderer;
            } catch (Throwable t) {
                Log.w("DeviceInfo", "Could not read the GPU name", t);
                gpu = "bilinmiyor";
            }
            String finalGpu = (gpu == null || gpu.isEmpty()) ? "bilinmiyor" : gpu;
            mainHandler.post(() -> infoPref.setSummary(buildDeviceSummary(device, chip, ramText, cores,
                    is64Bit, finalGpu, bestText)));
        }, "device-info-gpu").start();
    }

    private static String buildDeviceSummary(String device, String chip, String ramText, int cores,
                                             boolean is64Bit, String gpu, String bestText) {
        return String.format(Locale.getDefault(),
                "Cihaz: %s • %s • Android %s\nRAM: %s • %d çekirdek • %s\nGPU: %s\nTahmini üst sınır: %s (kesin değildir)",
                device, chip, Build.VERSION.RELEASE, ramText, cores,
                is64Bit ? "64-bit" : "32-bit", gpu, bestText);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);
        computeVisibility();
    }

    private void computeVisibility(){
        requirePreference("force_vsync", SwitchPreferenceCompat.class)
                .setVisible(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
    }
}
