package net.kdt.pojavlaunch.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Rough estimate of which Minecraft version range this device/install can likely start, based on
 * (a) which Java runtimes are actually available and (b) approximate RAM needs.
 *
 * This is a HEURISTIC, not a guarantee: it cannot predict GPU driver crashes, out-of-memory during
 * world generation, or mod compatibility. The breakpoints below are approximate; exact patch-level
 * boundaries can differ by a point release. Treat the result as "worth trying", not as a promise.
 */
public final class VersionCompatibilityPredictor {

    public static final class Breakpoint {
        public final String label;
        public final int requiredJavaMajor;
        public final int recommendedRamMb;

        public Breakpoint(String label, int requiredJavaMajor, int recommendedRamMb) {
            this.label = label;
            this.requiredJavaMajor = requiredJavaMajor;
            this.recommendedRamMb = recommendedRamMb;
        }
    }

    public static final class Verdict {
        public final Breakpoint breakpoint;
        public final boolean javaAvailable;
        public final boolean ramLikelyEnough;

        public Verdict(Breakpoint breakpoint, boolean javaAvailable, boolean ramLikelyEnough) {
            this.breakpoint = breakpoint;
            this.javaAvailable = javaAvailable;
            this.ramLikelyEnough = ramLikelyEnough;
        }

        public boolean isLikelyPlayable() {
            return javaAvailable && ramLikelyEnough;
        }
    }

    // Newest first. See the class doc: approximate, not exact.
    public static final List<Breakpoint> BREAKPOINTS = Collections.unmodifiableList(Arrays.asList(
            new Breakpoint("26.1 ve üzeri", 25, 3072),
            new Breakpoint("1.20.5 – 26.0", 21, 2560),
            new Breakpoint("1.18 – 1.20.4", 17, 2048),
            new Breakpoint("1.17 ve altı", 8, 1024)
    ));

    private VersionCompatibilityPredictor() {}

    /**
     * @param maxAvailableJavaMajor the highest Java major version this install can actually run
     *                              right now (bundled or user-installed)
     * @param deviceRamMb           total device RAM in MB; 0 or negative skips the RAM check (treated as unknown)
     * @return one verdict per breakpoint, newest first
     */
    public static List<Verdict> evaluate(int maxAvailableJavaMajor, long deviceRamMb) {
        List<Verdict> out = new ArrayList<>(BREAKPOINTS.size());
        for (Breakpoint bp : BREAKPOINTS) {
            boolean javaOk = maxAvailableJavaMajor >= bp.requiredJavaMajor;
            boolean ramOk = deviceRamMb <= 0 || deviceRamMb >= bp.recommendedRamMb;
            out.add(new Verdict(bp, javaOk, ramOk));
        }
        return out;
    }

    /** @return the newest breakpoint considered likely playable, or null if none are. */
    public static Breakpoint highestLikelyPlayable(int maxAvailableJavaMajor, long deviceRamMb) {
        for (Verdict v : evaluate(maxAvailableJavaMajor, deviceRamMb)) {
            if (v.isLikelyPlayable()) return v.breakpoint;
        }
        return null;
    }
}
