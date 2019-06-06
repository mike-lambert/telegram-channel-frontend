package com.cyfrant.tgfrontend.service;

import java.io.IOException;

public interface DataUriService {
    String dataURI(String url, String defaultContentType) throws IOException;

    String content(String url) throws IOException;
}
