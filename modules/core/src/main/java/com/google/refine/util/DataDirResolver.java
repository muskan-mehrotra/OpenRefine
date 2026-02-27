package com.google.refine.util;

import java.io.File;

/**
 * More testable version of the data directory selection logic.
 * This class has NO side effects (no mkdirs, no file renames),
 * and does NOT read global state (System.getenv / System.getProperty).
 *
 * It can be unit tested by providing a stub EnvironmentProvider.
 */
public final class DataDirResolver {

    private DataDirResolver() {}

    /** Dependency abstraction for environment state (testable via stubs). */
    public interface EnvironmentProvider {
        String getenv(String name);
        String getProperty(String name);
    }

    /**
     * Resolves the default OpenRefine data directory based on OS + environment variables.
     * Returns an absolute path string without creating any directories.
     */
    public static String resolve(EnvironmentProvider env) {
        String osName = safe(env.getProperty("os.name")).toLowerCase();
        String userHome = safe(env.getProperty("user.home"));

        // Windows: prefer APPDATA
        if (osName.contains("win")) {
            String appData = env.getenv("APPDATA");
            if (!isBlank(appData)) {
                return new File(appData, "OpenRefine").getAbsolutePath();
            }
            // fallback: user home
            return new File(userHome, "OpenRefine").getAbsolutePath();
        }

        // macOS: ~/Library/Application Support/OpenRefine
        if (osName.contains("mac")) {
            return new File(new File(new File(userHome, "Library"), "Application Support"),
                    "OpenRefine").getAbsolutePath();
        }

        // Linux/Unix: prefer XDG_DATA_HOME, else ~/.local/share
        String xdg = env.getenv("XDG_DATA_HOME");
        if (!isBlank(xdg)) {
            return new File(xdg, "OpenRefine").getAbsolutePath();
        }
        return new File(new File(new File(userHome, ".local"), "share"), "OpenRefine").getAbsolutePath();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}