package com.cyfrant.tgfrontend.utils;

import com.cyfrant.tgfrontend.model.BundledContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

@Slf4j
public class WebUtils {
    public static InputStream get(String url, InetSocketAddress proxyAddress) throws IOException {
        URL address = new URL(url);
        log.info(" <- {}", url);
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

    public static InputStream get(String url, InetSocketAddress proxyAddress, BundledContent metadata) throws IOException {
        URL address = new URL(url);
        log.debug(" <- {}", url);
        HttpURLConnection connection = (HttpURLConnection) address.openConnection(
                proxyAddress == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.SOCKS, proxyAddress)
        );
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Accept", "*/*");
        connection.connect();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyStream(connection.getInputStream(), out);
        String contentType = connection.getHeaderField("Content-Type");
        if (!StringUtils.isEmpty(contentType)) {
            metadata.setContentType(contentType);
        }
        metadata.setUrl(url);
        metadata.setBase64(base64(new ByteArrayInputStream(out.toByteArray())));
        return new ByteArrayInputStream(out.toByteArray());
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
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

    public static String base64(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(in, out);
        return Base64Utils.encodeToString(out.toByteArray());
    }

    public static String readStreamToString(InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyStream(in, out);
        final byte[] data = out.toByteArray();
        return new String(data, "UTF-8");
    }

    public static InputStream post(String url, InputStream body, Map<String, String> headers, InetSocketAddress proxy) throws IOException {
        HttpURLConnection uc = null;
        try {
            uc = (HttpURLConnection) new URL(url).openConnection(
                    proxy == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.SOCKS, proxy)
            );
            uc.setAllowUserInteraction(false);
            URLConnection.setDefaultAllowUserInteraction(false);
            uc.setRequestMethod("POST");
            for(Map.Entry<String, String> e : headers.entrySet()) {
                uc.setRequestProperty(e.getKey(), e.getValue());
            }
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.connect();
            copyStream(body, uc.getOutputStream());
            InputStream is = uc.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            copyStream(is, buffer);
            return new ByteArrayInputStream(buffer.toByteArray());
        } catch (Exception e) {
            log.warn("POST failed", e);
            throw e;
        } finally {
            if (uc != null) {
                uc.disconnect();
            }
        }
    }
}