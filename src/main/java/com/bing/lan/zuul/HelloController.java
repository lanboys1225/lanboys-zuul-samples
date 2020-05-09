package com.bing.lan.zuul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lb on 2020/5/9.
 */
@RestController
public class HelloController {

    private static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @RequestMapping("/a/foo")
    public String a() {
        logger.info("/a/foo");
        return "a";
    }

    @RequestMapping("/b/foo")
    public String b() {
        logger.info("/b/foo");
        return "b";
    }

    @RequestMapping("/c/foo")
    public String c(@RequestHeader("uid") Long userId) {
        logger.info("/c/foo: {}", userId);
        return "c";
    }
}
