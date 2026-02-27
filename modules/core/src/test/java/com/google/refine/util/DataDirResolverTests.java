package com.google.refine.util;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

public class DataDirResolverTests {

    static class StubEnv implements DataDirResolver.EnvironmentProvider {
        private final Map<String, String> env = new HashMap<>();
        private final Map<String, String> props = new HashMap<>();

        StubEnv withEnv(String k, String v) { env.put(k, v); return this; }
        StubEnv withProp(String k, String v) { props.put(k, v); return this; }

        @Override
        public String getenv(String name) { return env.get(name); }

        @Override
        public String getProperty(String name) { return props.get(name); }
    }

    @Test
    public void windowsUsesAppDataWhenPresent() {
        StubEnv e = new StubEnv()
                .withProp("os.name", "Windows 11")
                .withProp("user.home", "/Users/test")
                .withEnv("APPDATA", "C:\\Users\\test\\AppData\\Roaming");

        String path = DataDirResolver.resolve(e);
        assertTrue(path.replace("\\", "/").endsWith("/OpenRefine"),
                "Expected Windows APPDATA-based OpenRefine folder, got: " + path);
        assertTrue(path.contains("AppData") || path.contains("Roaming"),
                "Expected path to include APPDATA location, got: " + path);
    }

    @Test
    public void macUsesLibraryApplicationSupport() {
        StubEnv e = new StubEnv()
                .withProp("os.name", "Mac OS X")
                .withProp("user.home", "/Users/test");

        String path = DataDirResolver.resolve(e).replace("\\", "/");
        assertTrue(path.endsWith("/Library/Application Support/OpenRefine"),
                "Expected macOS Application Support path, got: " + path);
    }

    @Test
    public void linuxUsesXdgDataHomeWhenPresent() {
        StubEnv e = new StubEnv()
                .withProp("os.name", "Linux")
                .withProp("user.home", "/home/test")
                .withEnv("XDG_DATA_HOME", "/tmp/xdgdata");

        String path = DataDirResolver.resolve(e).replace("\\", "/");
        assertTrue(path.equals("/tmp/xdgdata/OpenRefine"),
                "Expected XDG_DATA_HOME path, got: " + path);
    }

    @Test
    public void linuxFallsBackToLocalShareWhenXdgMissing() {
        StubEnv e = new StubEnv()
                .withProp("os.name", "Linux")
                .withProp("user.home", "/home/test");

        String path = DataDirResolver.resolve(e).replace("\\", "/");
        assertTrue(path.equals("/home/test/.local/share/OpenRefine"),
                "Expected fallback ~/.local/share path, got: " + path);
    }
}
