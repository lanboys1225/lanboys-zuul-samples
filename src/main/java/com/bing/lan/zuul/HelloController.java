package com.bing.lan.zuul;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lb on 2020/5/9.
 */
@RestController
public class HelloController {

    @RequestMapping("/a/foo")
    public String a() {
        return "a";
    }

    @RequestMapping("/b/foo")
    public String b() {
        return "b";
    }
}
