package com.cyfrant.tgfrontend.model;

import com.cyfrant.tgfrontend.utils.CryptoUtils;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

@Getter
public class MappedURL {
    private final String targetAddress;
    private final String frontendAddress;

    public MappedURL(String frontend, String target) {
        this.frontendAddress = frontend;
        this.targetAddress = target;
    }

    public String sha256() {
        return CryptoUtils.sha256hex(targetAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MappedURL)) return false;

        MappedURL mappedURL = (MappedURL) o;

        if (!targetAddress.equals(mappedURL.targetAddress)) return false;
        return frontendAddress.equals(mappedURL.frontendAddress);
    }

    @Override
    public int hashCode() {
        int result = targetAddress.hashCode();
        result = 31 * result + frontendAddress.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return frontendAddress + " -> " + targetAddress;
    }

    public static MappedURL relative(URL frontendBase, URL targetBase, String relative) throws MalformedURLException {
        URL frontend = new URL(frontendBase, relative);
        URL target = new URL(targetBase, relative);
        return new MappedURL(frontend.toString(), target.toString());
    }

    public static MappedURL map(String frontend, URL targetBase) throws MalformedURLException {
        URL parsed = new URL(frontend);
        String path = parsed.getPath() + (StringUtils.isEmpty(parsed.getQuery()) ? "" : parsed.getQuery());
        URL target = new URL(targetBase, path);
        return new MappedURL(frontend, target.toString());
    }

    public static MappedURL hashUrl(URL frontendBase, String target) throws MalformedURLException {
        URL targetUrl = new URL(target);
        String hash = "/" + CryptoUtils.sha256hex(target);
        return new MappedURL(new URL(frontendBase, hash).toString(), target);
    }
}