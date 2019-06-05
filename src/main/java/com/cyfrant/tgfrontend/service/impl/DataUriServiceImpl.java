package com.cyfrant.tgfrontend.service.impl;

import com.cyfrant.tgfrontend.model.BundledContent;
import com.cyfrant.tgfrontend.service.DataUriService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class DataUriServiceImpl implements DataUriService {
    private final InetSocketAddress proxyAddress;

    public DataUriServiceImpl(InetSocketAddress proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    @Cacheable(value = "data-uri", key = "#url")
    @Override
    public String dataURI(String url, String defaultContentType) throws IOException {
        log.info("Caching {}", url);
        return BundledContent.dataUri(url, proxyAddress, defaultContentType);
    }
}
