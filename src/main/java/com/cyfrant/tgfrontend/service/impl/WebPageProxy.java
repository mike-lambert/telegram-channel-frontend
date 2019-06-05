package com.cyfrant.tgfrontend.service.impl;

import com.cyfrant.tgfrontend.service.DataUriService;
import com.cyfrant.tgfrontend.service.PageProxyService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;

@Slf4j
public class WebPageProxy implements PageProxyService {
    private final URL index;
    private final String frontendURL;
    private final InetSocketAddress proxyAddress;
    private final DataUriService dataUriService;

    public WebPageProxy(String startPage,
                        String frontendURL,
                        DataUriService uriService,
                        InetSocketAddress proxyAddress) throws Exception {
        this.frontendURL = frontendURL;
        this.proxyAddress = proxyAddress;
        this.dataUriService = uriService;
        index = new URL(startPage);
    }

    @Override
    public InputStream getContextURL(String relativeURL) throws IOException {
        return refreshDocument(relativeURL);
    }

    private InputStream refreshDocument(String relativeUrl) throws IOException {
        URL url = new URL(index.toString() + relativeUrl);
        PageService parser = new PageService(url, dataUriService, proxyAddress);
        Document document = parser.parse();
        parser.bundleResources(new URL(frontendURL));
        return new ByteArrayInputStream(document.toString().getBytes("UTF-8"));
    }
}