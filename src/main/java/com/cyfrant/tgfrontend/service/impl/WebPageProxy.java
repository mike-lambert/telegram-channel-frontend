package com.cyfrant.tgfrontend.service.impl;

import com.cyfrant.tgfrontend.HTMLParser;
import com.cyfrant.tgfrontend.service.PageProxyService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

@Slf4j
public class WebPageProxy implements PageProxyService {
    private final URL index;
    private final String frontendURL;
    private final List<String> domains;
    private final InetSocketAddress proxyAddress;

    public WebPageProxy(String startPage,
                        String frontendURL,
                        List<String> domains,
                        InetSocketAddress proxyAddress) throws Exception {
        this.frontendURL = frontendURL;
        this.domains = domains;
        this.proxyAddress = proxyAddress;
        index = new URL(startPage);
    }

    @Override
    public InputStream getContextURL(String relativeURL) throws IOException {
        return refreshDocument(relativeURL);
    }

    private InputStream refreshDocument(String relativeUrl) throws IOException {
        HTMLParser parser = new HTMLParser(new URL(index, relativeUrl), proxyAddress);
        Document document = parser.parse();
        parser.bundleResources();
        return new ByteArrayInputStream(document.toString().getBytes("UTF-8"));
    }
}