package net.kdt.pojavlaunch.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional JVM tuning presets, picked in Settings > Java.
 *
 * These are heuristics, not guarantees: whether they help depends on the device, the game
 * version and the heap size. Use the in-game FPS counter to compare them on your own phone.
 * The default preset adds nothing, so behaviour only changes when the user opts in.
 */
public final class JvmPresets {
    public static final String PRESET_DEFAULT = "default";
    /** Shorter garbage collection pauses (G1), serial collector where G1 is unavailable. */
    public static final String PRESET_SMOOTH = "smooth";
    /** Lowest GC overhead and thread count, meant for devices with little RAM / few cores. */
    public static final String PRESET_LOW_MEMORY = "lowmem";

    private JvmPresets() {}

    /**
     * @return true if one of the arguments selects or deselects a garbage collector,
     * for example -XX:+UseG1GC, -XX:+UseSerialGC or -XX:-UseParallelGC.
     * Two different collector flags make the JVM refuse to start ("Conflicting collector
     * combinations"), so a preset must stay out of the way when the user already chose one.
     */
    public static boolean hasCollectorChoice(List<String> args) {
        if (args == null) return false;
        for (String arg : args) {
            if (arg == null) continue;
            boolean usesFlag = arg.startsWith("-XX:+Use") || arg.startsWith("-XX:-Use");
            if (usesFlag && arg.endsWith("GC")) return true;
        }
        return false;
    }

    /**
     * @param preset      one of the PRESET_* constants (unknown values behave like the default)
     * @param serverVm    true if the runtime ships the "server" VM (where G1 is available)
     * @param userArgs    the user's custom Java arguments (already split)
     * @param profileArgs the JVM arguments of the selected profile / game version
     * @return arguments to append, empty if the preset is the default or the user already
     * picked a garbage collector themselves
     */
    public static List<String> argsFor(String preset, boolean serverVm,
                                       List<String> userArgs, List<String> profileArgs) {
        List<String> out = new ArrayList<>();
        if (preset == null || PRESET_DEFAULT.equals(preset)) return out;
        if (hasCollectorChoice(userArgs) || hasCollectorChoice(profileArgs)) return out;

        if (PRESET_LOW_MEMORY.equals(preset)) {
            out.add("-XX:+UseSerialGC");
        } else if (PRESET_SMOOTH.equals(preset)) {
            if (serverVm) {
                out.add("-XX:+UseG1GC");
                out.add("-XX:MaxGCPauseMillis=50");
                out.add("-XX:+ParallelRefProcEnabled");
                // NOT -XX:+DisableExplicitGC: direct buffers (LWJGL) rely on System.gc() to be freed
                out.add("-XX:+ExplicitGCInvokesConcurrent");
            } else {
                out.add("-XX:+UseSerialGC");
            }
        } else {
            return out; // unknown preset id
        }
        // A flag that one particular VM build does not know is skipped instead of aborting the launch
        out.add(0, "-XX:+IgnoreUnrecognizedVMOptions");
        return out;
    }
}
