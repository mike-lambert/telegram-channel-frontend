package com.cyfrant.tgfrontend.service;

import java.io.IOException;
import java.io.InputStream;

public interface PageProxyService {
    InputStream getContextURL(String relativeURL) throws IOException;
}