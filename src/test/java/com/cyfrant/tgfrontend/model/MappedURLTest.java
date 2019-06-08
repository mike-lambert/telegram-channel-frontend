package com.cyfrant.tgfrontend.model;

import org.junit.Test;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URL;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class MappedURLTest {
    @Test
    public void basePathsIgnoredRespectfully() throws Exception {
        String base1 = "https://t.me/s/IERussia";
        String base2 = "https://t.me/";
        String frontend1 = "https://ierussia.cyfrant.com/";
        String frontend2 = "https://ierussia.cyfrant.com/channel/";
        String relative = "/s/channel?skip=3001";

        String expectedFrontend = "https://ierussia.cyfrant.com/s/channel?skip=3001";
        String expectedTarget = "https://t.me/s/channel?skip=3001";

        MappedURL m1 = MappedURL.relative(new URI(frontend1).toURL(), new URI(base1).toURL(), relative);
        assertEquals(expectedFrontend, m1.getFrontendAddress());
        assertEquals(expectedTarget, m1.getTargetAddress());

        MappedURL m2 = MappedURL.relative(new URI(frontend1).toURL(), new URI(base2).toURL(), relative);
        assertEquals(expectedFrontend, m2.getFrontendAddress());
        assertEquals(expectedTarget, m2.getTargetAddress());

        MappedURL m3 = MappedURL.relative(new URI(frontend2).toURL(), new URI(base1).toURL(), relative);
        assertEquals(expectedFrontend, m3.getFrontendAddress());
        assertEquals(expectedTarget, m3.getTargetAddress());
    }

    @Test
    public void hashNotEmpty() throws Exception {
        String base1 = "https://t.me/s/IERussia";
        String frontend1 = "https://ierussia.cyfrant.com/";
        String relative = "/s/channel?skip=3001";

        MappedURL m1 = MappedURL.relative(new URI(frontend1).toURL(), new URI(base1).toURL(), relative);
        assertFalse(StringUtils.isEmpty(m1.sha256()));
    }

    @Test
    public void directMapShouldBeProper() throws Exception {
        String frontend = "https://ierussia.cyfrant.com/pictures/123.jpg";
        String target = "https://t.me/s/IERussia";
        String expected = "https://t.me/pictures/123.jpg";
        MappedURL m = MappedURL.map(frontend, new URL(target));
        assertEquals(expected, m.getTargetAddress());
    }
}