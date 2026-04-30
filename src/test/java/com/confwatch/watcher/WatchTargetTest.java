package com.confwatch.watcher;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class WatchTargetTest {

    @Test
    void shouldStoreServiceNameAndAbsolutePath() {
        WatchTarget target = new WatchTarget("my-service", "/etc/myapp/config.yaml");
        assertEquals("my-service", target.getServiceName());
        assertTrue(target.getFilePath().isAbsolute());
    }

    @Test
    void shouldNormaliseRelativePaths() {
        WatchTarget target = new WatchTarget("svc", "relative/path/config.conf");
        assertTrue(target.getFilePath().isAbsolute(),
                "WatchTarget should convert relative paths to absolute");
    }

    @Test
    void equalityBasedOnFilePath() {
        WatchTarget a = new WatchTarget("svc-a", "/etc/app.conf");
        WatchTarget b = new WatchTarget("svc-b", "/etc/app.conf");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldRejectNullServiceName() {
        assertThrows(NullPointerException.class,
                () -> new WatchTarget(null, "/etc/app.conf"));
    }

    @Test
    void shouldRejectNullFilePath() {
        assertThrows(NullPointerException.class,
                () -> new WatchTarget("svc", null));
    }

    @Test
    void toStringShouldContainServiceAndPath() {
        WatchTarget target = new WatchTarget("nginx", "/etc/nginx/nginx.conf");
        String str = target.toString();
        assertTrue(str.contains("nginx"));
        assertTrue(str.contains("nginx.conf"));
    }
}
