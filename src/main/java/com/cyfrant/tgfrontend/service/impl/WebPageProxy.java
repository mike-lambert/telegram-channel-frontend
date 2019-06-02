package com.cyfrant.tgfrontend.service.impl;

import com.cyfrant.tgfrontend.HTMLParser;
import com.cyfrant.tgfrontend.model.MappedURL;
import com.cyfrant.tgfrontend.service.PageProxyService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebPageProxy implements PageProxyService {
    private final URL index;
    private final Map<String, MappedURL> resources;
    private HTMLParser parser;
    private final String frontendURL;
    private final List<String> domains;
    private final InetSocketAddress proxyAddress;

    public WebPageProxy(String startPage,
                        String frontendURL,
                        List<String> domains,
                        InetSocketAddress proxyAddress) throws Exception {
        resources = new ConcurrentHashMap<>();
        this.frontendURL = frontendURL;
        this.domains = domains;
        this.proxyAddress = proxyAddress;
        index = new URL(startPage);
        parser = new HTMLParser(index, this.proxyAddress);
    }

    @Override
    public InputStream getContextURL(String relativeURL) throws IOException {
        if (relativeURL.equals("/") /*|| relativeURL.startsWith("/?") || relativeURL.startsWith("?")*/) {
            log.info("MAIN: " + relativeURL);
            return refreshDocument(relativeURL);
        }
        if (resources.get(relativeURL) != null) {
            log.info("RES: " + relativeURL);
            return loadMappedUrl(resources.get(relativeURL));
        }
        throw new RuntimeException("Cannot process " + relativeURL);
    }

    private InputStream loadMappedUrl(MappedURL mappedURL) throws IOException {
        URL address = new URL(mappedURL.getTargetAddress());
        log.info(" <- {}", mappedURL);
        HttpURLConnection connection = (HttpURLConnection) address.openConnection(
                proxyAddress == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.SOCKS, proxyAddress)
        );
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Accept", "*/*");
        connection.connect();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyStream(connection.getInputStream(), out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[65536];
        int read = -1;
        do {
            read = in.read(buffer);
            if (read > 0) {
                out.write(buffer, 0, read);
            }
        } while (read > 0);
        out.flush();
    }

    private synchronized InputStream refreshDocument(String relativeUrl) throws IOException {
        //parser = new HTMLParser(new URL(index, relativeUrl), proxyAddress);
        Document document = parser.parse();
        List<MappedURL> mapped = parser.mapResources(new URL(frontendURL), domains);
        resources.clear();
        mapped.forEach(r -> {
            String relative = null;
            try {
                URL parsed = new URL(r.getFrontendAddress());
                relative = "/" + parsed.getPath() + (StringUtils.isEmpty(parsed.getQuery()) ? "" : "?" + parsed.getQuery());
                if (relative.startsWith("//")) {
                    relative = relative.substring(1);
                }
                resources.put(relative, r);
            } catch (MalformedURLException e) {
                log.warn("Cannot map " + r.getFrontendAddress());
            }
        });
        return new ByteArrayInputStream(document.toString().getBytes("UTF-8"));
    }
}