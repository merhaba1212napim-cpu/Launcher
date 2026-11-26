package net.kdt.pojavlaunch.prefs.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

/**
 * "Video processor" sub-screen: which renderer to use, and how much the image is scaled.
 * The scaling presets only write the existing resolutionRatio value, so the game's render
 * scale behaves exactly as it does when the slider is moved by hand.
 */
public class LauncherPreferenceVideoProcessorFragment extends LauncherPreferenceFragment {
    private static final String[] PRESET_PERCENTS = {"50", "67", "80", "100"};

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video_processor);

        int resolution = (int) (LauncherPreferences.PREF_SCALE_FACTOR * 100);
        if (resolution < 25) resolution = 100; // #724 bug fix, kept from the original screen

        CustomSeekBarPreference resolutionSeekbar = requirePreference("resolutionRatio",
                CustomSeekBarPreference.class);
        resolutionSeekbar.setSuffix(" %");
        resolutionSeekbar.setValue(resolution);

        ListPreference presetPreference = requirePreference("upscalePreset", ListPreference.class);
        presetPreference.setValue(closestPreset(resolution));
        presetPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            Context context = getContext();
            if (context == null) return false;
            int percent = Integer.parseInt((String) newValue);
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt("resolutionRatio", percent).apply();
            resolutionSeekbar.setValue(percent);
            return true;
        });

        ListPreference rendererListPreference = requirePreference("renderer", ListPreference.class);
        Tools.RenderersList renderersList = Tools.getCompatibleRenderers(getContext());
        rendererListPreference.setEntries(renderersList.rendererDisplayNames);
        rendererListPreference.setEntryValues(renderersList.rendererIds.toArray(new String[0]));
    }

    /** The preset whose percentage is nearest to the given render resolution. On a tie the lower, faster one wins. */
    static String closestPreset(int percent) {
        String best = PRESET_PERCENTS[PRESET_PERCENTS.length - 1];
        int bestDistance = Integer.MAX_VALUE;
        for (String candidate : PRESET_PERCENTS) {
            int distance = Math.abs(Integer.parseInt(candidate) - percent);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }
}
