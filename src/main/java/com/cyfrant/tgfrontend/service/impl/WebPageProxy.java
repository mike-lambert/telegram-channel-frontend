package com.cyfrant.tgfrontend.service.impl;

import com.cyfrant.tgfrontend.service.DataUriService;
import com.cyfrant.tgfrontend.service.PageProxyService;
import com.cyfrant.tgfrontend.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public InputStream get(String relativeURL) throws IOException {
        return refreshDocument(relativeURL);
    }

    @Override
    public InputStream post(HttpServletRequest original) throws IOException {
        String path = original.getRequestURI() + (StringUtils.isEmpty(original.getQueryString()) ? "" : "?" + original.getQueryString());
        Map<String, String> headers = new ConcurrentHashMap<>();
        URL url = new URL(index.toString() + path);
        /*
         * curl -sS 'https://t.me/s/zatelecom?before=10119' -X POST -H 'Origin: https://t.me' -H 'X-Requested-With: XMLHttpRequest' -H 'Connection: keep-alive' -H 'Content-Length: 0' -H 'DNT: 1' --compressed*/
        String origin = index.getProtocol() + "://" + index.getHost();
        headers.put("Origin", origin);
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Length", "0");
        headers.put("DNT", "1");
        String normalized = url.toString().replace("/?", "?");
        return WebUtils.post(normalized, original.getInputStream(), headers, proxyAddress);
    }

    private InputStream refreshDocument(String relativeUrl) throws IOException {
        URL url = new URL(index.toString() + relativeUrl);
        PageService parser = new PageService(url, dataUriService, proxyAddress);
        Document document = parser.parse();
        parser.bundleResources(new URL(frontendURL));
        return new ByteArrayInputStream(document.toString().getBytes("UTF-8"));
    }
}