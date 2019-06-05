package com.cyfrant.tgfrontend.config;

import com.cyfrant.tgfrontend.service.DataUriService;
import com.cyfrant.tgfrontend.service.PageProxyService;
import com.cyfrant.tgfrontend.service.impl.DataUriServiceImpl;
import com.cyfrant.tgfrontend.service.impl.WebPageProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

@Configuration
@PropertySource("application.properties")
@Slf4j
@EnableCaching
public class Config {
    @Value("${target.url:https://t.me/s/IERussia}")
    private String startPage;

    @Value("${frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${proxy.address:}")
    private String proxyAddress;
    private PageProxyService pageProxyService;
    private DataUriService dataUriService;

    @Bean
    public PageProxyService pageProxyService() throws Exception {
        if (pageProxyService == null) {
            pageProxyService = new WebPageProxy(startPage, frontendUrl, dataUriService(), proxyAddress());
        }
        return pageProxyService;
    }

    @Bean
    public DataUriService dataUriService() {
        if (dataUriService == null) {
            dataUriService = new DataUriServiceImpl(proxyAddress());
        }
        return dataUriService;
    }

    private InetSocketAddress proxyAddress() {
        if (StringUtils.isEmpty(proxyAddress)) {
            return null;
        }
        String[] pac = proxyAddress.split(":");
        int port = Integer.parseInt(pac[1].trim());
        return new InetSocketAddress(pac[0].trim(), port);
    }
}