package net.kdt.pojavlaunch.multirt;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Installs Java runtimes that are shipped inside the APK as assets/components/java/&lt;name&gt;.tar.xz.
 * Put a JRE tarball there, build, and it is unpacked on the next start, then shows up in the Java
 * runtime list like any other runtime. No version file or extra metadata is needed: the version comes
 * from the runtime's own "release" file, the same as for runtimes installed from the Java settings.
 *
 * The runtime name is the file name without ".tar.xz". Runtimes that already exist are left alone.
 */
public final class JavaDropIn {
    private static final String TAG = "JavaDropIn";
    static final String ASSET_DIR = "components/java";
    static final String SUFFIX = ".tar.xz";

    private JavaDropIn() {}

    /** @return the runtime name for an asset file name, or null if the file is not a runtime tarball. */
    public static String runtimeNameFor(String assetFileName) {
        if (assetFileName == null || !assetFileName.endsWith(SUFFIX)) return null;
        String name = assetFileName.substring(0, assetFileName.length() - SUFFIX.length());
        return name.isEmpty() ? null : name;
    }

    /** A runtime counts as installed when its unpacked folder has a "release" file. */
    public static boolean isInstalled(File runtimeHome) {
        return new File(runtimeHome, "release").isFile();
    }

    /**
     * Installs every drop-in runtime that is not installed yet. Call from a background thread.
     * A failing runtime is logged and skipped, it never stops the others or the launcher.
     */
    public static void installMissing(Context context, String nativeLibDir) {
        AssetManager assets = context.getAssets();
        String[] files;
        try {
            files = assets.list(ASSET_DIR);
        } catch (IOException e) {
            Log.w(TAG, "Could not list " + ASSET_DIR, e);
            return;
        }
        if (files == null) return;

        for (String file : files) {
            String name = runtimeNameFor(file);
            if (name == null) continue;
            if (isInstalled(new File(Tools.MULTIRT_HOME, name))) continue;
            try (InputStream in = assets.open(ASSET_DIR + "/" + file)) {
                MultiRTUtils.installRuntimeNamed(nativeLibDir, in, name);
                Log.i(TAG, "Installed Java runtime '" + name + "'");
            } catch (Exception e) {
                Log.e(TAG, "Could not install Java runtime '" + name + "'", e);
            }
        }
    }
}
