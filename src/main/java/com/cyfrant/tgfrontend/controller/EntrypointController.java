package com.cyfrant.tgfrontend.controller;

import com.cyfrant.tgfrontend.service.PageProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/**")
@Slf4j
public class EntrypointController {
    @Autowired
    private PageProxyService proxyService;

    @GetMapping
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI() + (StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
        log.info("GET -> {}", path);
        StreamUtils.copy(proxyService.get(path), response.getOutputStream());
    }

    @PostMapping
    public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI() + (StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
        log.info("POST -> {}", path);
        if (path.equals("/v/")) {
            StreamUtils.copy(
                    new ByteArrayInputStream("true".getBytes("UTF-8")),
                    response.getOutputStream()
            );
            return;
        }
        StreamUtils.copy(proxyService.post(request), response.getOutputStream());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView error(Exception e) {
        e.printStackTrace(System.out);
        return new ModelAndView("error");
    }
}