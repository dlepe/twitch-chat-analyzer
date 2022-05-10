package com.dlepe.twitchchatanalyzer.controller;

import com.dlepe.twitchchatanalyzer.service.LogService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class HelloWorldController {
    private LogService logService;

    @GetMapping("/")
    public String greeting() {
        logService.getLogData();
        return "Hello world!";
    }

}