package com.cyfrant.tgfrontend.service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

public interface PageProxyService {
    InputStream get(String relativeURL) throws IOException;
    InputStream post(HttpServletRequest original) throws IOException;
}