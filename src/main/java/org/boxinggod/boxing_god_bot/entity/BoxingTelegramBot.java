package org.boxinggod.boxing_god_bot.entity;

import lombok.extern.slf4j.Slf4j;
import org.boxinggod.boxing_god_bot.service.BoxingFightService;
import org.boxinggod.boxing_god_bot.service.BoxingFightService.SimulationResult;
import org.boxinggod.boxing_god_bot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class BoxingTelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final BoxingFightService fightService;
    private final UserService userService;

    private static final int TRAINING_COST = 200;

    // Хранилище ID сообщений для очистки чата
    private final Map<Long, List<Integer>> messageHistory = new ConcurrentHashMap<>();
    private final Map<Long, String> lastFights = new ConcurrentHashMap<>();
    private final Map<Long, String[]> pendingBets = new ConcurrentHashMap<>();

    // === ТИР 1: Величайшие легенды в истории (P4P и абсолюты) ===
    private final List<String> tier1Legends = List.of(
            "Мухаммед Али", "Майк Тайсон", "Флойд Мейвезер", "Мэнни Пакьяо",
            "Шугар Рэй Робинсон", "Шугар Рэй Леонард", "Рой Джонс", "Александр Усик",
            "Эвандер Холифилд", "Леннокс Льюис", "Джордж Форман", "Джо Фрейзер",
            "Марвин Хаглер", "Роберто Дюран", "Томас Хирнс", "Хулио Сезар Чавес",
            "Бернард Хопкинс", "Сауль Альварес", "Теренс Кроуфорд", "Наоя Иноуэ"
    );

    // === ТИР 2: Элита бокса и чемпионы мира ===
    private final List<String> tier2Legends = List.of(
            "Дмитрий Бивол", "Артур Бетербиев", "Геннадий Головкин", "Энтони Джошуа",
            "Тайсон Фьюри", "Виталий Кличко", "Владимир Кличко", "Костя Цзю",
            "Оскар Де Ла Хойя", "Феликс Тринидад", "Шейн Мосли", "Хуан Мануэль Маркес",
            "Марко Антонио Баррера", "Эрик Моралес", "Мигель Котто", "Джеймс Тони",
            "Андре Уорд", "Сергей Ковалев", "Василий Ломаченко", "Джервонта Дэвис"
    );

    // === ТИР 3: Претенденты, крепкие чемпионы и рубаки (для старта карьеры) ===
    private final List<String> tier3Legends = List.of(
            "Артуро Гатти", "Микки Уорд", "Руслан Проводников", "Пол Малиньяджи",
            "Брэндон Риос", "Крис Юбенк-младший", "Келли Павлик", "Дэнни Гарсия",
            "Шон Портер", "Амир Хан", "Рикки Хаттон", "Заб Джуда",
            "Виктор Ортис", "Маркос Майдана", "Габриэль Росадо", "Дерек Чисора"
    );

    public BoxingTelegramBot(
            @Value("${bot.token}") String botToken,
            @Value("${bot.name}") String botName,
            BoxingFightService fightService,
            UserService userService) {
        super(botToken);
        this.botName = botName;
        this.fightService = fightService;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            trackMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
            handleTextMessage(update);
        }
    }

    private void handleTextMessage(Update update) {
        String userText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();
        String firstName = update.getMessage().getFrom().getFirstName();

        AppUser user = userService.getOrCreateUser(chatId, username, firstName);

        if (userText.equalsIgnoreCase("/start") || userText.equals("ℹ️ Инструкция")) {
            sendMainMenu(chatId, """
                🥊 **Добро пожаловать в Боксерский AI-Симулятор!**
                
                💰 Твой баланс: **1000 стартовых монет**!
                
                📌 **Возможности:**
                • **«🥊 Мой боксер»** — развивай бойца, тренируй статы и побеждай легенд!
                • **«🎰 Сделать ставку»** — ставь монеты на бои и удваивай банк.
                • **«🎲 Случайный бой»** — фантазийный поединок двух случайных легенд.
                • **«🧹 Очистить чат»** — удалить сообщения и навести порядок.
                """);
            return;
        }

        if (userText.equals("🧹 Очистить чат")) {
            clearChat(chatId, update.getMessage().getMessageId());
            return;
        }

        if (userText.equals("🥊 Мой боксер")) {
            CustomBoxer boxer = userService.getCustomBoxer(chatId)
                    .orElseGet(() -> userService.createDefaultBoxer(chatId, firstName + " 'Громобой'"));
            sendBoxerProfile(chatId, boxer);
            return;
        }

        if (userText.equals("👤 Мой профиль")) {
            String profileText = String.format("""
                👤 **Профиль игрока:** %s
                
                💰 **Баланс монет:** `%d` 🪙
                🥊 **Проведено боев:** `%d`
                """,
                    escapeMarkdown(user.getFirstName()), user.getBalance(), user.getTotalFights());
            sendSimpleText(chatId, profileText);
            return;
        }

        if (userText.equals("🎰 Сделать ставку")) {
            prepareBetFight(chatId);
            return;
        }

        if (userText.equals("🎲 Случайный бой")) {
            triggerRandomFight(chatId);
            return;
        }

        if (userText.equals("🔥 Топ дуэлей")) {
            sendTopFightsList(chatId);
            return;
        }

        // Парсинг произвольной пары: "Боксер 1 vs Боксер 2"
        String delimiter = userText.toLowerCase().contains(" vs ") ? " vs " :
                userText.toLowerCase().contains(" против ") ? " против " : null;

        if (delimiter != null) {
            String[] boxers = userText.split("(?i)" + delimiter);
            if (boxers.length == 2 && !boxers[0].isBlank() && !boxers[1].isBlank()) {
                runSimulation(chatId, boxers[0].trim(), boxers[1].trim(), 0);
                return;
            }
        }

        sendSimpleText(chatId, "⚠️ Введите пару в формате `Боксер 1 vs Боксер 2` или выберите действие в меню.");
    }

    private void sendBoxerProfile(long chatId, CustomBoxer boxer) {
        int ovr = (boxer.getPower() + boxer.getSpeed() + boxer.getStamina() + boxer.getChin() + boxer.getRingIq()) / 5;
        long balance = userService.getBalance(chatId);

        String text = String.format("""
            🥊 **ВАШ БОКСЕР:** %s
            ⭐️ **Общий рейтинг (OVR):** `%d/100`
            
            📊 **Характеристики:**
            • 💥 Сила удара: `%d/100`
            • ⚡ Скорость: `%d/100`
            • 🫁 Кардио: `%d/100`
            • 🗿 Крепость челюсти: `%d/100`
            • 🧠 Ринг-IQ: `%d/100`
            
            🏆 **Рекорд:** `%d-%d` (Побед/Поражений)
            💰 **Твой баланс:** `%d` 🪙
            
            _Выберите соперника по силам или отправьтесь в тренировочный лагерь!_
            """,
                escapeMarkdown(boxer.getName()), ovr,
                boxer.getPower(), boxer.getSpeed(), boxer.getStamina(), boxer.getChin(), boxer.getRingIq(),
                boxer.getWins(), boxer.getLosses(), balance
        );

        List<String> opponents = getOpponentsForBoxer(ovr, 3);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String opponent : opponents) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText("⚔️ Вызвать: " + opponent);
            btn.setCallbackData("CUSTOM_FIGHT:" + opponent);
            row.add(btn);
            rows.add(row);
        }

        // Кнопка перехода в зал тренировок
        List<InlineKeyboardButton> trainRow = new ArrayList<>();
        InlineKeyboardButton trainBtn = new InlineKeyboardButton();
        trainBtn.setText("🏋️ Тренировочный лагерь (+Статы)");
        trainBtn.setCallbackData("OPEN_CAMP");
        trainRow.add(trainBtn);
        rows.add(trainRow);

        // Кнопка обновления списка
        List<InlineKeyboardButton> refreshRow = new ArrayList<>();
        InlineKeyboardButton refreshBtn = new InlineKeyboardButton();
        refreshBtn.setText("🔄 Другие соперники");
        refreshBtn.setCallbackData("REFRESH_OPPONENTS");
        refreshRow.add(refreshBtn);
        rows.add(refreshRow);

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        message.setReplyMarkup(markup);

        executeAndTrack(message, chatId);
    }

    private void sendTrainingCamp(long chatId, CustomBoxer boxer) {
        long balance = userService.getBalance(chatId);

        String text = String.format("""
            🏋️ **ТРЕНИРОВОЧНЫЙ ЛАГЕРЬ**
            
            Каждая тренировка повышает характеристику на **+2 пункта**.
            Стоимость тренировки: **%d монет** 🪙.
            Твой текущий баланс: `%d` 🪙.
            
            Выберите навык для улучшения:
            """, TRAINING_COST, balance);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(createStatButton("💥 Сила", boxer.getPower(), "UP_POWER")));
        rows.add(List.of(createStatButton("⚡ Скорость", boxer.getSpeed(), "UP_SPEED")));
        rows.add(List.of(createStatButton("🫁 Кардио", boxer.getStamina(), "UP_STAMINA")));
        rows.add(List.of(createStatButton("🗿 Челюсть", boxer.getChin(), "UP_CHIN")));
        rows.add(List.of(createStatButton("🧠 Ринг-IQ", boxer.getRingIq(), "UP_IQ")));

        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("⬅️ Назад в профиль");
        backBtn.setCallbackData("BACK_TO_PROFILE");
        rows.add(List.of(backBtn));

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        message.setReplyMarkup(markup);

        executeAndTrack(message, chatId);
    }

    private InlineKeyboardButton createStatButton(String title, int value, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        if (value >= 100) {
            btn.setText(title + " [МАКС: 100]");
            btn.setCallbackData("MAX_STAT");
        } else {
            btn.setText(title + " (" + value + ") [+2] — " + TRAINING_COST + "🪙");
            btn.setCallbackData(callbackData);
        }
        return btn;
    }

    private List<String> getOpponentsForBoxer(int ovr, int count) {
        List<String> pool;
        if (ovr < 75) {
            pool = new ArrayList<>(tier3Legends);
        } else if (ovr <= 85) {
            pool = new ArrayList<>(tier2Legends);
        } else {
            pool = new ArrayList<>(tier1Legends);
        }
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    private void prepareBetFight(long chatId) {
        List<String> combined = new ArrayList<>(tier1Legends);
        combined.addAll(tier2Legends);
        Collections.shuffle(combined);

        String b1 = combined.get(0);
        String b2 = combined.get(1);

        pendingBets.put(chatId, new String[]{b1, b2});

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("🥊 " + b1 + " (100 🪙)");
        btn1.setCallbackData("BET_1");
        row1.add(btn1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton btn2 = new InlineKeyboardButton();
        btn2.setText("🥊 " + b2 + " (100 🪙)");
        btn2.setCallbackData("BET_2");
        row2.add(btn2);

        rows.add(row1);
        rows.add(row2);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("🎰 **Арена Ставок**\n\nПара: **%s** 🆚 **%s**\nСтавка: **100 монет**\n\nВыбери бойца, на победу которого ставишь:", b1, b2));
        message.enableMarkdown(true);
        message.setReplyMarkup(markup);

        executeAndTrack(message, chatId);
    }

    private void handleCallbackQuery(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data.equals("MAX_STAT")) {
            sendSimpleText(chatId, "ℹ️ Эта характеристика уже развита до абсолютного максимума (100)!");
            return;
        }

        if (data.equals("REFRESH_OPPONENTS") || data.equals("BACK_TO_PROFILE")) {
            userService.getCustomBoxer(chatId).ifPresent(boxer -> sendBoxerProfile(chatId, boxer));
            return;
        }

        if (data.equals("OPEN_CAMP")) {
            userService.getCustomBoxer(chatId).ifPresent(boxer -> sendTrainingCamp(chatId, boxer));
            return;
        }

        // Прокачка характеристик
        if (data.startsWith("UP_")) {
            String stat = switch (data) {
                case "UP_POWER" -> "power";
                case "UP_SPEED" -> "speed";
                case "UP_STAMINA" -> "stamina";
                case "UP_CHIN" -> "chin";
                case "UP_IQ" -> "iq";
                default -> "";
            };

            boolean success = userService.upgradeStat(chatId, stat, TRAINING_COST);
            if (success) {
                sendSimpleText(chatId, "💪 **Тренировка завершена успешно!** Характеристика увеличена на **+2**.");
                userService.getCustomBoxer(chatId).ifPresent(boxer -> sendTrainingCamp(chatId, boxer));
            } else {
                sendSimpleText(chatId, "❌ Недостаточно монет для тренировки (нужно " + TRAINING_COST + " 🪙) или навык уже достиг 100.");
            }
            return;
        }

        // Бой созданного боксера против легенды
        if (data.startsWith("CUSTOM_FIGHT:")) {
            String legendName = data.replace("CUSTOM_FIGHT:", "");
            var boxerOpt = userService.getCustomBoxer(chatId);
            if (boxerOpt.isPresent()) {
                var myBoxer = boxerOpt.get();
                sendSimpleText(chatId, "⏳ *Твой боец " + myBoxer.getName() + " выходит в ринг против " + legendName + "...*");
                try {
                    SimulationResult sim = fightService.simulateCustomFight(myBoxer, legendName);
                    boolean won = (sim.winnerIndex() == 1);
                    userService.recordCustomFightResult(chatId, won);

                    sendFightResultWithButtons(chatId, sim.fullText());

                    if (won) {
                        long balance = userService.awardWin(chatId, 500L);
                        sendSimpleText(chatId, String.format("""
                            🏆 **СЕНСАЦИЯ!** Твой боксер победил легенду!
                            💰 Награда за победу: **+500 монет**
                            🪙 Текущий баланс: `%d` монет
                            """, balance));
                    } else {
                        long balance = userService.awardWin(chatId, 50L);
                        sendSimpleText(chatId, String.format("""
                            🥊 Твой боец уступил, но показал характер и заработал опыт!
                            💰 Гонорар за бой: **+50 монет** | Баланс: `%d` 🪙
                            """, balance));
                    }
                } catch (Exception e) {
                    log.error("Ошибка боя с кастомным боксером", e);
                    sendSimpleText(chatId, "❌ Ошибка симуляции поединка: " + e.getMessage());
                }
            }
            return;
        }

        // Обработка ставки
        if (data.equals("BET_1") || data.equals("BET_2")) {
            String[] pair = pendingBets.get(chatId);
            if (pair == null) {
                sendSimpleText(chatId, "⚠️ Время ставки истекло. Нажмите «🎰 Сделать ставку» заново.");
                return;
            }

            int chosenIndex = data.equals("BET_1") ? 1 : 2;
            String chosenBoxer = (chosenIndex == 1) ? pair[0] : pair[1];
            long betAmount = 100L;

            if (!userService.placeBet(chatId, betAmount)) {
                sendSimpleText(chatId, "❌ У вас недостаточно монет для ставки! Ваш баланс меньше 100 🪙.");
                return;
            }

            sendSimpleText(chatId, String.format("✅ Ставка принята: **100 монет** на победу **%s**!\n\n⏳ *Бой начался, ожидаем вердикт судей...*", chosenBoxer));
            runSimulation(chatId, pair[0], pair[1], chosenIndex);
            return;
        }

        if (data.equals("REMATCH")) {
            String lastPair = lastFights.get(chatId);
            if (lastPair != null && lastPair.contains(" vs ")) {
                String[] boxers = lastPair.split(" vs ");
                if (boxers.length == 2) {
                    sendSimpleText(chatId, "🔄 *Запускаем реванш...*");
                    runSimulation(chatId, boxers[0], boxers[1], 0);
                    return;
                }
            }
            sendSimpleText(chatId, "⚠️ Данные о прошлом поединке устарели. Запустите новый бой из главного меню.");
        } else if (data.equals("RANDOM_FIGHT")) {
            triggerRandomFight(chatId);
        }
    }

    private void triggerRandomFight(long chatId) {
        List<String> all = new ArrayList<>(tier1Legends);
        all.addAll(tier2Legends);
        Collections.shuffle(all);
        runSimulation(chatId, all.get(0), all.get(1), 0);
    }

    private void runSimulation(long chatId, String boxer1, String boxer2, int chosenIndex) {
        lastFights.put(chatId, boxer1 + " vs " + boxer2);

        try {
            SimulationResult result = fightService.simulateFight(boxer1, boxer2);
            userService.recordFight(chatId, boxer1, boxer2, result.fullText());

            sendFightResultWithButtons(chatId, result.fullText());

            if (chosenIndex != 0) {
                String chosenBoxerName = (chosenIndex == 1) ? boxer1 : boxer2;
                boolean won = (result.winnerIndex() == chosenIndex);

                if (won) {
                    long newBalance = userService.awardWin(chatId, 200L);
                    sendSimpleText(chatId, String.format("""
                        🎉 **ПОБЕДА СТАВКИ!**
                        Твой боец **%s** триумфально выиграл поединок!
                        💰 Выигрыш: **+200 монет** | Баланс: `%d` 🪙
                        """, chosenBoxerName, newBalance));
                } else {
                    long currentBalance = userService.getBalance(chatId);
                    sendSimpleText(chatId, String.format("""
                        😢 **СТАВКА НЕ СЫГРАЛА!**
                        Боец **%s** потерпел поражение.
                        📉 Списано: 100 монет | Баланс: `%d` 🪙
                        """, chosenBoxerName, currentBalance));
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при симуляции", e);
            sendSimpleText(chatId, "❌ Ошибка при генерации боя: " + e.getMessage());
        }
    }

    private void clearChat(long chatId, int currentMessageId) {
        for (int i = 0; i < 60; i++) {
            int msgIdToDelete = currentMessageId - i;
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(msgIdToDelete);
                execute(deleteMessage);
            } catch (Exception ignored) {
            }
        }
        sendMainMenu(chatId, "✨ **Чат полностью очищен!** Выберите действие:");
    }

    private void trackMessage(long chatId, int messageId) {
        messageHistory.computeIfAbsent(chatId, k -> Collections.synchronizedList(new ArrayList<>())).add(messageId);
    }

    private void executeAndTrack(SendMessage message, long chatId) {
        try {
            Message sent = execute(message);
            if (sent != null) {
                trackMessage(chatId, sent.getMessageId());
            }
        } catch (TelegramApiException e) {
            // Защита от ошибок форматирования Markdown
            message.enableMarkdown(false);
            try {
                Message sent = execute(message);
                if (sent != null) {
                    trackMessage(chatId, sent.getMessageId());
                }
            } catch (TelegramApiException ex) {
                log.error("Ошибка отправки сообщения", ex);
            }
        }
    }

    private void sendFightResultWithButtons(long chatId, String text) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton rematchBtn = new InlineKeyboardButton();
        rematchBtn.setText("🔄 Реванш");
        rematchBtn.setCallbackData("REMATCH");
        row1.add(rematchBtn);

        InlineKeyboardButton randomBtn = new InlineKeyboardButton();
        randomBtn.setText("🎲 Другой бой");
        randomBtn.setCallbackData("RANDOM_FIGHT");
        row1.add(randomBtn);

        rows.add(row1);
        markup.setKeyboard(rows);

        sendChunkedMessage(chatId, text, markup);
    }

    private void sendMainMenu(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🎰 Сделать ставку"));
        row1.add(new KeyboardButton("🥊 Мой боксер"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🎲 Случайный бой"));
        row2.add(new KeyboardButton("👤 Мой профиль"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("🔥 Топ дуэлей"));
        row3.add(new KeyboardButton("🧹 Очистить чат"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        executeAndTrack(message, chatId);
    }

    private void sendTopFightsList(long chatId) {
        String sb = """
            🔥 **Популярные культовые пары для ввода:**
            
            • `Майк Тайсон vs Мухаммед Али`
            • `Флойд Мейвезер vs Мэнни Пакьяо`
            • `Александр Усик vs Энтони Джошуа`
            • `Артур Бетербиев vs Дмитрий Бивол`
            • `Геннадий Головкин vs Сауль Альварес`
            • `Рой Джонс vs Бернард Хопкинс`
            
            _Скопируйте любую или отправьте собственную пару через **vs**!_""";
        sendSimpleText(chatId, sb);
    }

    private void sendSimpleText(long chatId, String text) {
        sendChunkedMessage(chatId, text, null);
    }

    private void sendChunkedMessage(long chatId, String text, InlineKeyboardMarkup markup) {
        if (text == null || text.isBlank()) return;

        final int MAX_LENGTH = 4000;
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + MAX_LENGTH, text.length());

            if (end < text.length()) {
                int lastNewLine = text.lastIndexOf("\n", end);
                if (lastNewLine > start) {
                    end = lastNewLine;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(chunk);
                message.enableMarkdown(true);

                if (end >= text.length() && markup != null) {
                    message.setReplyMarkup(markup);
                }

                executeAndTrack(message, chatId);
            }
            start = end;
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}