package net.kdt.pojavlaunch.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Renderers that are dropped into the APK as native libraries named libr_&lt;name&gt;.so (for example
 * app_pojavlauncher/src/main/jniLibs/arm64-v8a/libr_myrenderer.so). After a build they show up in the
 * renderer list as "&lt;name&gt; (özel renderer)" with the id "custom_&lt;name&gt;". No other file is needed.
 *
 * The library must provide both EGL and GL, the same way libltw_custom.so does.
 */
public final class DropInRenderers {
    public static final String ID_PREFIX = "custom_";
    static final String LIB_PREFIX = "libr_";
    static final String LIB_SUFFIX = ".so";
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private DropInRenderers() {}

    /** @return the renderer name for a library file name ("libr_foo.so" -> "foo"), or null if it is not one. */
    public static String nameFor(String libFileName) {
        if (libFileName == null || !libFileName.startsWith(LIB_PREFIX) || !libFileName.endsWith(LIB_SUFFIX)) return null;
        String name = libFileName.substring(LIB_PREFIX.length(), libFileName.length() - LIB_SUFFIX.length());
        return VALID_NAME.matcher(name).matches() ? name : null;
    }

    /** @return the library file name for a renderer name ("foo" -> "libr_foo.so"). */
    public static String libraryFileFor(String name) {
        return LIB_PREFIX + name + LIB_SUFFIX;
    }

    /** @return the renderer id for a renderer name ("foo" -> "custom_foo"). */
    public static String idFor(String name) {
        return ID_PREFIX + name;
    }

    /** @return the library file name for a renderer id ("custom_foo" -> "libr_foo.so"), or null if not a drop-in id. */
    public static String libraryFileForId(String rendererId) {
        if (rendererId == null || !rendererId.startsWith(ID_PREFIX)) return null;
        return libraryFileFor(rendererId.substring(ID_PREFIX.length()));
    }

    /** @return the renderer names found among the given file names, sorted and without duplicates. */
    public static List<String> namesFrom(String[] fileNames) {
        List<String> names = new ArrayList<>();
        if (fileNames == null) return names;
        for (String file : fileNames) {
            String name = nameFor(file);
            if (name != null && !names.contains(name)) names.add(name);
        }
        Collections.sort(names);
        return names;
    }
}
