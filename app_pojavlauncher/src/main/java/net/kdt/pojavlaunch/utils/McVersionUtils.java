package net.kdt.pojavlaunch.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decides whether a Minecraft version is 1.17 or newer from its name. The caller passes the base
 * version of a local install (see MainActivity), so mod loader folders are judged by the game they run.
 */
public final class McVersionUtils {
    private static final Pattern RELEASE = Pattern.compile("^(\\d+)\\.(\\d+)");
    private static final Pattern SNAPSHOT = Pattern.compile("^(\\d{2})w(\\d{2})[a-z]$");

    private McVersionUtils() {}

    /**
     * @return true for 1.17 and newer ("1.17", "1.17.1", "1.17-pre1", "1.21.4", year-based "26.1"),
     * and for snapshots from 2022 on. 2021 snapshots and anything unrecognised return false, so the
     * user's own renderer choice stays in effect when we cannot tell.
     */
    public static boolean isAtLeast117(String version) {
        if (version == null) return false;
        String v = version.trim();
        Matcher release = RELEASE.matcher(v);
        if (release.find()) {
            int major = parseOrMinusOne(release.group(1));
            int minor = parseOrMinusOne(release.group(2));
            if (major > 1) return true; // year-based numbering starts at 26.x
            return minor >= 17;
        }
        Matcher snapshot = SNAPSHOT.matcher(v);
        if (snapshot.matches()) return parseOrMinusOne(snapshot.group(1)) > 21;
        return false;
    }

    private static int parseOrMinusOne(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
