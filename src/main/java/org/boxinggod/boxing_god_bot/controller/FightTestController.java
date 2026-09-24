package org.boxinggod.boxing_god_bot.controller;

import lombok.RequiredArgsConstructor;
import org.boxinggod.boxing_god_bot.service.BoxingFightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fight")
@RequiredArgsConstructor
public class FightTestController {

    private final BoxingFightService fightService;

    @GetMapping
    public String simulate(
            @RequestParam(defaultValue = "Майк Тайсон") String boxer1,
            @RequestParam(defaultValue = "Мухаммед Али") String boxer2
    ) {
        return fightService.simulateFight(boxer1, boxer2);
    }
}