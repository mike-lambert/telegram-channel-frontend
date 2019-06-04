package com.cyfrant.tgfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.cyfrant.tgfrontend.utils.WebUtils.get;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundledContent {
    private String url;
    private String base64;
    private String contentType;

    public static String dataUri(String url, InetSocketAddress proxyAddress, String defaultContentType) throws IOException {
        BundledContent metadata = new BundledContent();
        metadata.setContentType(defaultContentType);
        get(url, proxyAddress, metadata);
        return "data:" + metadata.getContentType() + ";base64," + metadata.getBase64();
    }
}