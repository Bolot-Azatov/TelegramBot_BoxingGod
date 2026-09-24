package org.boxinggod.boxing_god_bot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/")
    public String ping() {
        return "Boxing God Bot is alive!";
    }
}