package net.kdt.pojavlaunch.prefs.screens;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
import net.kdt.pojavlaunch.utils.VersionCompatibilityPredictor;

import java.util.Locale;

/**
 * Fragment for any settings video related
 */
public class LauncherPreferenceVideoFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video);
        int resolution = (int) (LauncherPreferences.PREF_SCALE_FACTOR * 100);

        //Disable notch checking behavior on android 8.1 and below.
        requirePreference("ignoreNotch").setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0);

        CustomSeekBarPreference resolutionSeekbar = requirePreference("resolutionRatio",
                CustomSeekBarPreference.class);
        resolutionSeekbar.setSuffix(" %");

        // #724 bug fix
        if (resolution < 25) {
            resolutionSeekbar.setValue(100);
        } else {
            resolutionSeekbar.setValue(resolution);
        }

        // Sustained performance is only available since Nougat
        SwitchPreference sustainedPerfSwitch = requirePreference("sustainedPerformance",
                SwitchPreference.class);
        sustainedPerfSwitch.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
        sustainedPerfSwitch.setChecked(LauncherPreferences.PREF_SUSTAINED_PERFORMANCE);

        requirePreference("alternate_surface", SwitchPreferenceCompat.class).setChecked(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
        requirePreference("force_vsync", SwitchPreferenceCompat.class).setChecked(LauncherPreferences.PREF_FORCE_VSYNC);

        ListPreference rendererListPreference = requirePreference("renderer",
                ListPreference.class);
        Tools.RenderersList renderersList = Tools.getCompatibleRenderers(getContext());
        rendererListPreference.setEntries(renderersList.rendererDisplayNames);
        rendererListPreference.setEntryValues(renderersList.rendererIds.toArray(new String[0]));

        computeVisibility();
        setupDeviceCompatInfo();
    }

    /** Fills in the read-only "device & compatibility" preference; see VersionCompatibilityPredictor. */
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

        String ramText = ramMb > 0 ? (ramMb + " MB") : "bilinmiyor";
        String bestText = best != null ? best.label : "tespit edilemedi";
        infoPref.setSummary(String.format(Locale.getDefault(),
                "RAM: %s • %d çekirdek • %s\nTahmini üst sınır: %s (kesin değildir)",
                ramText, cores, is64Bit ? "64-bit" : "32-bit", bestText));
        infoPref.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(),
                    "Bu kaba bir tahmindir; sürücü ve mod uyumluluğu hesaba katılmaz.",
                    Toast.LENGTH_LONG).show();
            return true;
        });
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
