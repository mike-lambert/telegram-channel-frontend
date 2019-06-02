package com.cyfrant.tgfrontend.controller;

import com.cyfrant.tgfrontend.service.PageProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/**")
public class EntrypointController {
    @Autowired
    private PageProxyService proxyService;

    @GetMapping
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();
        StreamUtils.copy(proxyService.getContextURL(path), response.getOutputStream());
    }
}