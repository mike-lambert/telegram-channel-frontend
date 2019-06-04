package com.cyfrant.tgfrontend;

import com.cyfrant.tgfrontend.model.MappedURL;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class HTMLParser {
    private Document document;
    private final URL url;
    private final InetSocketAddress proxyAddress;

    public HTMLParser(URL url, InetSocketAddress proxy) {
        this.url = url;
        this.proxyAddress = proxy;
    }

    public Document parse() throws IOException {
        document = Jsoup.connect(url.toString())
                .proxy(proxyAddress == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.SOCKS, proxyAddress))
                .get();
        //System.out.println(document.outerHtml());
        return document;
    }


    public List<Element> remoteResources() {
        List<Element> result = new CopyOnWriteArrayList<>();
        result.addAll(document.select("link[href]"));
        result.addAll(document.select("a[href]"));
        result.addAll(document.select("img[src]"));
        result.addAll(document.select("script[src]"));
        result.addAll(document.select("video[src]"));
        return result;
    }

    private String normalizeUrl(String address) {
        if (address.startsWith("//")) {
            return "https:" + address;
        }
        if (address.startsWith("?")) {
            return this.url.toString() + address;
        }
        return address;
    }

    private boolean isRootResource(String address) {
        String prefix = url.toString() + "?";
        return address.startsWith(prefix);
    }

    private boolean isSubResource(String address) {
        String prefix = url.toString() + "/";
        return address.startsWith(prefix) && address.length() > prefix.length();
    }

    private String getBaseURL(URL address) {
        StringBuffer result = new StringBuffer();
        result.append(address.getProtocol())
                .append("://")
                .append(address.getHost());
        if (address.getPort() > 0) {
            result.append(':').append(address.getPort());
        }
        return result.toString();
    }

    private boolean isSameDomain(String url1, String url2) throws MalformedURLException {
        return getBaseURL(new URL(url1)).equals(getBaseURL(new URL(url2)));
    }

    private boolean isRelative(String address) {
        if (address.startsWith("https://") || address.startsWith("http://")) {
            return false;
        }
        return true;
    }

    private boolean needProxying(String address, List<String> proxied) throws MalformedURLException {
        String base = getBaseURL(new URL(address));
        for (String domain : proxied) {
            if (domain.equals(base)) {
                return true;
            }
        }
        return false;
    }

    public void bundleResources() {
        // TODO: bundle;
        remoteResources().forEach(r -> {

        });
    }

    public List<MappedURL> mapResources(URL frontendBase, List<String> proxiedDomains) {
        final List<MappedURL> result = new CopyOnWriteArrayList<>();
        remoteResources().forEach(r -> {
            String currentUrl = null;
            MappedURL mapped = null;
            if (r.tagName().equals("script")) {
                if (StringUtils.isEmpty(r.attr("language"))) {
                    r.attr("language", "JavaScript");
                }
                if (StringUtils.isEmpty(r.attr("type"))) {
                    r.attr("type", "text/javascript");
                }
            }
            try {
                if (!StringUtils.isEmpty(r.attr("href"))) {
                    String target = normalizeUrl(r.attr("href"));
                    currentUrl = target;
                    if (isRelative(target)) {
                        target = new URL(url, target).toString();
                    } else {
                        if (!needProxying(target, proxiedDomains)) {
                            target = null;
                        }
                    }
                    if (target != null) {
                        if (isRootResource(target)) {
                            String query = new URL(target).getQuery();
                            mapped = new MappedURL(frontendBase + "?" + query, url.toString() + "?" + query);
                        } else if (isSubResource(target)) {
                            String path = new URL(target).getPath();
                            mapped = new MappedURL(frontendBase + "/" + path, url.toString() + "/" + path);
                        } else {
                            mapped = MappedURL.hashUrl(frontendBase, target);
                        }
                        r.attr("href", mapped.getFrontendAddress());
                    }
                } else if (!StringUtils.isEmpty(r.attr("src"))) {
                    String target = normalizeUrl(r.attr("src"));
                    currentUrl = target;
                    if (isRelative(target)) {
                        target = new URL(url, target).toString();
                    } else {
                        if (!needProxying(target, proxiedDomains)) {
                            target = null;
                        }
                    }
                    if (target != null) {
                        if (isRootResource(target)) {
                            String query = new URL(target).getQuery();
                            mapped = new MappedURL(frontendBase + "?" + query, url.toString() + "?" + query);
                        } else if (isSubResource(target)) {
                            String path = new URL(target).getPath();
                            mapped = new MappedURL(frontendBase + "/" + path, url.toString() + "/" + path);
                        } else {
                            mapped = MappedURL.hashUrl(frontendBase, target);
                        }
                        r.attr("src", mapped.getFrontendAddress());
                    }
                }

                if (mapped != null) {
                    result.add(mapped);
                }
            } catch (Exception e) {
                log.warn("Cannot map {}", currentUrl);
            }
        });
        return result.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /*
    public static void main(String[] args) {
        try {
            URL url = URI.create("https://t.me/s/IERussia").toURL();
            URL frontend = new URL("http://localhost:8080");
            List<String> domains = new CopyOnWriteArrayList<>();
            domains.add("https://t.me");
            domains.add("https://telegram.org");
            domains.add("https://core.telegram.org");
            HTMLParser parser = new HTMLParser(url, new InetSocketAddress("localhost", 3128));
            Document d = parser.parse();
            parser.mapResources(frontend, domains)
                    .forEach(m -> {
                        System.out.println(m);
                    });
            System.out.println(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}