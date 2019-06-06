package com.cyfrant.tgfrontend.service.impl;

import com.cyfrant.tgfrontend.model.BundledContent;
import com.cyfrant.tgfrontend.service.DataUriService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class PageService {
    private Document document;
    private final URL url;
    private final InetSocketAddress proxyAddress;
    private final DataUriService dataUriService;

    public PageService(URL url, DataUriService dataUriService, InetSocketAddress proxy) {
        this.url = url;
        this.proxyAddress = proxy;
        this.dataUriService = dataUriService;
    }

    public Document parse() throws IOException {
        document = Jsoup.connect(url.toString())
                .proxy(proxyAddress == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.SOCKS, proxyAddress))
                .get();
        log.debug("{} ->\n{}", url, document);
        return document;
    }


    public List<Element> remoteResources() {
        List<Element> result = new CopyOnWriteArrayList<>();
        result.addAll(document.select("link[href]"));
        result.addAll(document.select("a[href]"));
        result.addAll(document.select("img[src]"));
        result.addAll(document.select("script[src]"));
        result.addAll(document.select("i[style]"));
        return result;
    }

    private String normalizeUrl(String address) {
        if (address.startsWith("//")) {
            return "https:" + address;
        }
        if (address.startsWith("?")) {
            return getMainURL(url) + address;
        }
        if (address.startsWith("/")) {
            return getBaseURL(url) + address;
        }
        return address;
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

    private String getMainURL(URL address) {
        return getBaseURL(address) + address.getPath();
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

    private String replaceToProxy(String link, URL frontendBase) {
        String base = getMainURL(url);
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (link.startsWith(base)) {
            link = link.replace(base, frontendBase.toString());
        }
        return link;
    }

    private void bundleBackground(Element node, String styleAttribute) {
        /*
         * style="background-image:url('https://cdn4.telesco.pe/file/W8LSPa3TeoinlbruKz1gKS96FZNOEnZGfYqNcuXoPqh3MmTOjvXD_i6cQkjStK1T9cKs5d59Ev-HxN5Yc6rrqfYiAUVj8o5cznVFAOuALvlBZCflQpGA8lxILoOKAiJiiB-MK0LzAH2IGBNiFICfXLp6ldqvm9YKBcB-_147ZgqmZ_f5KwV4z7VLGpbXO0mcxp-_A_xRHMZo5NlYaWQjKiQ_nOgw6x039wTr4AIrOT1kTnX4e-LChb-qENnIhmSybIXBcnLtqhyxB8ltEmS9FdOj_hnN7-wroCQu4Ao1fliPoXxt68tKZIw58vDSKv-wQHDx0PTT-N5wdIt4FGxiQw.jpg')"*/
        final String startExpression = "background-image:url(";
        final String endExpression = ")";
        String style = node.attr(styleAttribute);
        final int backgroundStart = style.indexOf(startExpression);
        if (backgroundStart >= 0) {
            final int delta1 = backgroundStart + startExpression.length();
            final int backgroundEnd = style.indexOf(endExpression, delta1);
            if (backgroundEnd > delta1) {
                String url = style.substring(delta1 + 1, backgroundEnd - 1);
                if (url.startsWith("//")) {
                    url = "https:" + url;
                }
                String start = style.substring(0, delta1);
                String tail = style.substring(backgroundEnd);
                try {
                    String data = BundledContent.dataUri(url, proxyAddress, "image/png");
                    style = start + "'" + data + "'" + tail;
                    node.attr(styleAttribute, style);
                } catch (IOException e) {
                    log.warn("Unable to get " + url, e);
                }
            }
        }
    }

    private String bundleCssBackground(String style) {
        final String startExpression = "background-image:url(";
        final String endExpression = ")";
        final int backgroundStart = style.indexOf(startExpression);
        if (backgroundStart >= 0) {
            final int delta1 = backgroundStart + startExpression.length();
            final int backgroundEnd = style.indexOf(endExpression, delta1);
            if (backgroundEnd > delta1) {
                String url = style.substring(delta1 + 1, backgroundEnd - 1);
                if (url.startsWith("/img/tgme/")) {
                    url = "https://telegram.org" + url;
                }
                if (url.startsWith("//")) {
                    url = "https:" + url;
                }
                String start = style.substring(0, delta1);
                String tail = style.substring(backgroundEnd);
                try {
                    String data = BundledContent.dataUri(url, proxyAddress, "image/png");
                    return start + "'" + data + "'" + tail;
                } catch (IOException e) {
                    log.warn("Unable to get " + url, e);
                }
            }
        }
        return style;
    }

    public void bundleResources(URL frontendBase) {
        remoteResources().parallelStream().forEach(r -> {
            if ("script".equalsIgnoreCase(r.tagName())) {
                String defaultContentType = "application/javascript";
                embedDataURI(r, "src", defaultContentType);
            }

            if ("img".equalsIgnoreCase(r.tagName())) {
                String defaultContentType = "image/png";
                embedDataURI(r, "src", defaultContentType);
            }

            if ("link".equalsIgnoreCase(r.tagName()) && "stylesheet".equalsIgnoreCase(r.attr("rel"))) {
                String defaultContentType = "text/css";
                String attribute = "href";
                try {
                    String link = r.attr(attribute);
                    if (link.startsWith("//")) {
                        link = "https:" + link;
                    }
                    String css = dataUriService.content(link);
                    css = bundleCssBackground(css);
                    String embed = "data:text/css;base64," + Base64Utils.encodeToString(
                            css.getBytes("UTF-8")
                    );
                    r.attr(attribute, embed);
                    log.debug("{}.{} : {} -> {}", r.tagName(), attribute, link, embed);
                } catch (Exception e) {
                    log.warn("{}.{}: ", r.tagName(), attribute, e);
                }
            }

            if ("a".equalsIgnoreCase(r.tagName())) {
                String link = r.attr("href");
                link = normalizeUrl(link);
                link = replaceToProxy(link, frontendBase);
                r.attr("href", link);

                String style = r.attr("style");
                if (!StringUtils.isEmpty(style)) {
                    bundleBackground(r, "style");
                }
            }

            if ("i".equalsIgnoreCase(r.tagName())) {
                String style = r.attr("style");
                if (!StringUtils.isEmpty(style)) {
                    bundleBackground(r, "style");
                }
            }
        });
    }


    private void embedDataURI(Element node, String attribute, String defaultContentType) {
        try {
            String link = node.attr(attribute);
            if (link.startsWith("/img/tgme/")) {
                link = "https://telegram.org" + link;
            } else {
                link = normalizeUrl(link);
            }
            String embed = dataUriService.dataURI(link, defaultContentType);
            node.attr(attribute, embed);
            log.debug("{}.{} : {} -> {}", node.tagName(), attribute, link, embed);
        } catch (Exception e) {
            log.warn("{}.{}: ", node.tagName(), attribute, e);
        }
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