package com.jstart.keyunautocodebackend.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class textController {


    @GetMapping("/text")
    public String text() {
        log.info("text endpoint was called");
        return "Hello, this is a test response from the text endpoint!";
    }



}
