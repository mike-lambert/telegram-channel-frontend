package com.cyfrant.tgfrontend.model.css;

import com.cyfrant.tgfrontend.model.BundledContent;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpressionMemberTermURI;
import com.helger.css.decl.CSSURI;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.decl.visit.DefaultCSSUrlVisitor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class URLCSSVisitor extends DefaultCSSUrlVisitor {
    private final InetSocketAddress proxyAddress;

    public URLCSSVisitor(InetSocketAddress proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    @Override
    public void onUrlDeclaration(@Nullable final ICSSTopLevelRule aTopLevelRule,
                                 @Nonnull final CSSDeclaration aDeclaration,
                                 @Nonnull final CSSExpressionMemberTermURI aURITerm) {
        final CSSURI aURI = aURITerm.getURI();

        if (aURI.isDataURL()) {

        } else {
            String url = aURI.getURI();
            if (url.startsWith("/img/tgme/") || url.startsWith("/img/oauth/")) {
                url = "https://telegram.org" + url;
            }
            if (url.startsWith("//")) {
                url = "https:" + url;
            }
            try {
                String data = BundledContent.dataUri(url, proxyAddress, "image/png");
                log.debug("{} -> \n {}", url, data);
                aURI.setURI(data);
            } catch (IOException e) {
                log.warn("Unable to get " + url, e);
            }
        }
    }
}
