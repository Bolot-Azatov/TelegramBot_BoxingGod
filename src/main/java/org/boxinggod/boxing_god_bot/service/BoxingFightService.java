package org.boxinggod.boxing_god_bot.service;

import org.boxinggod.boxing_god_bot.entity.CustomBoxer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BoxingFightService {

    private final ChatClient chatClient;
    private static final Pattern RESULT_PATTERN = Pattern.compile("RESULT_WINNER:\\s*([12])");

    public record SimulationResult(String fullText, int winnerIndex) {}

    public BoxingFightService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public SimulationResult simulateFight(String boxer1, String boxer2) {
        String systemPrompt = """
            Ты — ведущий русскоязычный эксперт и историк профессионального бокса.
            Твоя задача — провести тактически и исторически точный разбор поединка между бойцами в их АБСОЛЮТНОМ ПИКЕ.

            ЖЕЛЕЗНЫЕ ПРАВИЛА:
            1. СТОЙКИ БОЙЦОВ:
               - ПРАВШИ (Orthodox): Роберто Дюран, Шугар Рэй Леонард, Майк Тайсон, Мухаммед Али, Виталий Кличко, Флойд Мейвезер, Сауль Альварес, Геннадий Головкин, Джордж Форман, Артур Бетербиев, Дмитрий Бивол, Энтони Джошуа.
               - ЛЕВШИ (Southpaw): Мэнни Пакьяо, Александр Усик, Василий Ломаченко, Пернелл Уитакер, Джервонта Дэвис, Марвин Хаглер.
               - Не путать стойки! У правши джеб ЛЕВОЙ, кросс ПРАВОЙ. У левши джеб ПРАВОЙ, кросс ЛЕВОЙ.
            2. ОФОРМЛЕНИЕ: Без таблиц! Запрещены англицизмы в скобках. Разделяй текст абзацами.

            СТРУКТУРА:
            🥊 СОПОСТАВЛЕНИЕ СТИЛЕЙ И ГАБАРИТОВ
            • [Боксер 1]: Стойка, рост/рич, стиль, коронные удары.
            • [Боксер 2]: Стойка, рост/рич, стиль, коронные удары.

            ⚡ ТАКТИЧЕСКАЯ ИНТРИГА
            (2-3 емких предложения).

            🔥 СЦЕНАРИЙ БОЯ
            ▪ РАУНДЫ 1–4 (РАЗВЕДКА И ТЕМП)
            ▪ РАУНДЫ 5–8 (ЭКВАТОР И ТАКТИЧЕСКАЯ БОРЬБА)
            ▪ РАУНДЫ 9–12 (ЧЕМПИОНСКИЙ ОТРЕЗОК И РАЗВЯЗКА)

            🏆 ИТОГОВЫЙ ВЕРДИКТ
            • ПОБЕДИТЕЛЬ: [Точное имя победителя]
            • СПОСОБ ПОБЕДЫ: [KO/TKO (раунд) или Решение судей]
            • ОБОСНОВАНИЕ: [2-3 предложения].

            ВАЖНО: В самой последней строке своего ответа напиши строго технический тег:
            RESULT_WINNER:1 (если победил Боксер 1) или RESULT_WINNER:2 (если победил Боксер 2).
            """;

        String userPrompt = String.format("Боксер 1: %s. Боксер 2: %s. Проведи симуляцию боя.", boxer1, boxer2);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return parseResponse(response);
    }

    public SimulationResult simulateCustomFight(CustomBoxer custom, String legendName) {
        String systemPrompt = """
        Ты — ведущий русскоязычный эксперт и комментатор профессионального бокса.
        Твоя задача — смоделировать поединок между СОЗДАННЫМ БОЙЦОМ (Боксер 1) и РЕАЛЬНОЙ ЛЕГЕНДОЙ (Боксер 2).

        ПРАВИЛА ОЦЕНКИ:
        1. Характеристики созданного бойца даны по шкале от 1 до 100:
           - 60-70: крепкий региональный боксер.
           - 71-84: претендент на титул / чемпион мира.
           - 85-94: элитный боец P4P.
           - 95-100: уровень величия (All-time Great).
        2. Если суммарные статы кастомного бойца близки или превосходят оппонента — он ДОЛЖЕН иметь реальный шанс и побеждать, если выбран верный тактический ключ.
        3. Не засуживай кастомного бойца только потому, что напротив него историческое имя!

        СТРУКТУРА:
        🥊 СОПОСТАВЛЕНИЕ СТИЛЕЙ
        ⚡ ТАКТИЧЕСКИЙ РАЗБОР
        🔥 СЦЕНАРИЙ БОЯ
        🏆 ИТОГОВЫЙ ВЕРДИКТ
        • ПОБЕДИТЕЛЬ: [Имя победителя]
        • СПОСОБ ПОБЕДЫ: [KO/TKO или Решение]

        В конце отдельной строкой строго укажи:
        RESULT_WINNER:1 (если победил кастомный боец) или RESULT_WINNER:2 (если победила легенда).
        """;

        String userPrompt = String.format("""
        Боксер 1 (Кастомный): '%s'
        Характеристики:
        - Сила удара: %d/100
        - Скорость: %d/100
        - Выносливость: %d/100
        - Крепость подбородка: %d/100
        - Ринг-IQ: %d/100
        - Стойка: %s, Стиль: %s

        Боксер 2 (Легенда): '%s'
        """,
                custom.getName(),
                custom.getPower(), custom.getSpeed(), custom.getStamina(), custom.getChin(), custom.getRingIq(),
                custom.getStance(), custom.getStyle(),
                legendName
        );

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return parseResponse(response);
    }

    private SimulationResult parseResponse(String rawResponse) {
        if (rawResponse == null) {
            return new SimulationResult("Ошибка симуляции", 1);
        }

        Matcher matcher = RESULT_PATTERN.matcher(rawResponse);
        int winner = 1;
        if (matcher.find()) {
            winner = Integer.parseInt(matcher.group(1));
        }

        // Очищаем технический тег из сообщения, чтобы пользователь его не видел
        String cleanText = rawResponse.replaceAll("RESULT_WINNER:\\s*[12]", "").trim();
        return new SimulationResult(cleanText, winner);
    }
}