package org.boxinggod.boxing_god_bot.entity;

import lombok.extern.slf4j.Slf4j;
import org.boxinggod.boxing_god_bot.BoxingFightService;
import org.boxinggod.boxing_god_bot.entity.AppUser;
import org.boxinggod.boxing_god_bot.entity.CustomBoxer;
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
    private final Random random = new Random();

    // Хранилище ID сообщений для очистки чата
    private final Map<Long, List<Integer>> messageHistory = new ConcurrentHashMap<>();

    private final Map<Long, String> lastFights = new ConcurrentHashMap<>();
    private final Map<Long, String[]> pendingBets = new ConcurrentHashMap<>();

    // Огромный пул мировых легенд разных эпох и весовых категорий
    private final List<String> allLegends = List.of(
            // === СОВРЕМЕННАЯ ЭПОХА И 2010-е ===
            "Александр Усик", "Тайсон Фьюри", "Энтони Джошуа", "Деонтей Уайлдер",
            "Дмитрий Бивол", "Артур Бетербиев", "Сауль Альварес", "Геннадий Головкин",
            "Теренс Кроуфорд", "Эррол Спенс", "Наоя Иноуэ", "Василий Ломаченко",
            "Джервонта Дэвис", "Шакур Стивенсон", "Девин Хейни", "Теофимо Лопес",
            "Джарон Эннис", "Сергей Ковалев", "Давид Бенавидес", "Джермелл Чарло",
            "Джермалл Чарло", "Роман Гонсалес", "Хуан Франсиско Эстрада", "Срисакет Сор Рунгвисаи",
            "Кит Турман", "Дэнни Гарсия", "Шон Портер", "Крис Юбенк-младший",
            "Каллум Смит", "Джозеф Паркер", "Чжан Чжилей", "Даниэль Дюбуа",

            // === ЭПОХА 2000-х (2000–2010) ===
            "Флойд Мейвезер", "Мэнни Пакьяо", "Виталий Кличко", "Владимир Кличко",
            "Джо Кальзаге", "Бернард Хопкинс", "Шейн Мосли", "Хуан Мануэль Маркес",
            "Марко Антонио Баррера", "Эрик Моралес", "Мигель Котто", "Рикки Хаттон",
            "Костя Цзю", "Заб Джуда", "Вернон Форрест", "Крис Бёрд",
            "Хасим Рахман", "Олег Маскаев", "Николай Валуев", "Султан Ибрагимов",
            "Руслан Чагаев", "Антонио Маргарито", "Келли Павлик", "Джермейн Тейлор",
            "Артур Абрахам", "Миккель Кесслер", "Карл Фроч", "Андре Уорд",
            "Тимоти Брэдли", "Амир Хан", "Нонито Донэйр", "Серхио Мартинес",

            // === ЭПОХА 1990-х ===
            "Майк Тайсон", "Эвандер Холифилд", "Леннокс Льюис", "Рой Джонс",
            "Риддик Боу", "Джеймс Тони", "Оскар Де Ла Хойя", "Феликс Тринидад",
            "Насим Хамед", "Артуро Гатти", "Микки Уорд", "Пернелл Уитакер",
            "Майкл Мурер", "Рэй Мерсер", "Томми Моррисон", "Дэвид Туа",
            "Айк Ибеабучи", "Хайме Гарса", "Терри Норрис", "Джулиан Джексон",
            "Джеральд Макклеллан", "Найджел Бенн", "Крис Юбенк-старший", "Стив Коллинз",
            "Майкл Карбахаль", "Умберто Гонсалес", "Рикардо Лопес", "Марк Джонсон",

            // === ЭПОХА 1970–1980-х («Четыре Короля» и Золотая Эра) ===
            "Мухаммед Али", "Джордж Форман", "Джо Фрейзер", "Ларри Холмс",
            "Кен Нортон", "Шугар Рэй Леонард", "Роберто Дюран", "Томас Хирнс",
            "Марвин Хаглер", "Хулио Сезар Чавес", "Алексис Аргуэльо", "Аарон Прайор",
            "Сальвадор Санчес", "Уилфред Бенитес", "Уилфредо Гомес", "Эктор Камачо",
            "Эрни Шейверс", "Рон Лайл", "Джерри Кури", "Майкл Спинкс",
            "Леон Спинкс", "Джерри Куни", "Тим Уизерспун", "Грег Пейдж",
            "Пинки Пинлон", "Карлос Монсон", "Хосе Наполес", "Эдер Жофре",
            "Рубен Оливарес", "Бобби Чакон", "Дэнни Лопес", "Эйсебио Педроса",

            // === ЭПОХА 1950–1960-х ===
            "Сонни Листон", "Флойд Паттерсон", "Ингемар Юханссон", "Шугар Рэй Робинсон",
            "Арчи Мур", "Джин Фуллмер", "Кармен Базилио", "Эмиль Гриффит",
            "Нино Бенвенути", "Дик Тайгер", "Джо Браун", "Карлос Ортис",
            "Файтинг Харада", "Боб Фостер", "Хосе Торрес", "Кливленд Уильямс",

            // === ЭПОХА 1920–1940-х (Исторические Иконы) ===
            "Джо Луис", "Рокки Марчиано", "Генри Армстронг", "Джек Демпси",
            "Джин Танни", "Макс Шмелинг", "Макс Бэр", "Джеймс Брэддок",
            "Билли Конн", "Джерси Джо Уолкотт", "Эззард Чарльз", "Сэнди Саддлер",
            "Тони Зейл", "Рокки Грациано", "Джейк Ламотта", "Барни Росс",
            "Кид Гавилан", "Бенни Леонард", "Микки Уокер", "Гарри Греб"
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
            // Запоминаем сообщение пользователя для возможности очистки
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
                • **«🥊 Мой боксер»** — управляй бойцом и вызывай на бой любых легенд!
                • **«🎰 Сделать ставку»** — делай прогноз на бой и удваивай монеты.
                • **«🎲 Случайный бой»** — мгновенная симуляция боя легенд.
                • **«🧹 Очистить чат»** — удалить историю сообщений и навести порядок.
                """);
            return;
        }

        if (userText.equals("🧹 Очистить чат")) {
            clearChat(chatId, update.getMessage().getMessageId());
            return;
        }

        if (userText.equals("🥊 Мой боксер")) {
            var boxerOpt = userService.getCustomBoxer(chatId);
            if (boxerOpt.isEmpty()) {
                var newBoxer = userService.createDefaultBoxer(chatId, firstName + " 'Громобой'");
                sendBoxerProfile(chatId, newBoxer);
            } else {
                sendBoxerProfile(chatId, boxerOpt.get());
            }
            return;
        }

        if (userText.equals("👤 Мой профиль")) {
            String profileText = String.format("""
                👤 **Профиль игрока:** %s
                
                💰 **Баланс монет:** `%d` 🪙
                🥊 **Проведено боев:** `%d`
                """,
                    user.getFirstName(), user.getBalance(), user.getTotalFights());
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

        // Парсинг произвольной пары
        String lower = userText.toLowerCase();
        String delimiter = lower.contains(" vs ") ? " vs " :
                lower.contains(" против ") ? " против " : null;

        if (delimiter != null) {
            String[] boxers = userText.split("(?i)" + delimiter);
            if (boxers.length == 2) {
                runSimulation(chatId, boxers[0].trim(), boxers[1].trim(), null);
                return;
            }
        }

        sendSimpleText(chatId, "⚠️ Введите пару в формате `Боксер 1 vs Боксер 2` или выберите действие в меню.");
    }

    private void sendBoxerProfile(long chatId, CustomBoxer boxer) {
        String text = String.format("""
            🥊 **ВАШ БОКСЕР:** %s
            
            📊 **Характеристики:**
            • 💥 Сила: `%d/100` | ⚡ Скорость: `%d/100`
            • 🫁 Кардио: `%d/100` | 🗿 Челюсть: `%d/100`
            • 🧠 Ринг-IQ: `%d/100`
            
            🏆 **Рекорд:** `%d-%d` (Побед/Поражений)
            
            🔥 **Выберите легендарного соперника для поединка:**
            """,
                boxer.getName(),
                boxer.getPower(), boxer.getSpeed(), boxer.getStamina(), boxer.getChin(), boxer.getRingIq(),
                boxer.getWins(), boxer.getLosses()
        );

        // Выбираем 3 случайных уникальных соперника
        List<String> opponents = getRandomLegends(3);

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

        // Кнопка обновления списка соперников
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

    private List<String> getRandomLegends(int count) {
        List<String> shuffled = new ArrayList<>(allLegends);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private void prepareBetFight(long chatId) {
        // Случайная пара легенд
        List<String> pair = getRandomLegends(2);
        String b1 = pair.get(0);
        String b2 = pair.get(1);

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
        message.setText(String.format("🎰 **Арена Ставок**\n\nПара: **%s** 🆚 **%s**\nСтавка: **100 монет**\n\nВыбери своего победителя:", b1, b2));
        message.enableMarkdown(true);
        message.setReplyMarkup(markup);

        executeAndTrack(message, chatId);
    }

    private void handleCallbackQuery(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data.equals("REFRESH_OPPONENTS")) {
            userService.getCustomBoxer(chatId).ifPresent(boxer -> sendBoxerProfile(chatId, boxer));
            return;
        }

        if (data.startsWith("CUSTOM_FIGHT:")) {
            String legendName = data.replace("CUSTOM_FIGHT:", "");
            var boxerOpt = userService.getCustomBoxer(chatId);
            if (boxerOpt.isPresent()) {
                var myBoxer = boxerOpt.get();
                sendSimpleText(chatId, "⏳ *Твой боец " + myBoxer.getName() + " выходит в ринг против " + legendName + "...*");
                try {
                    String analysis = fightService.simulateCustomFight(myBoxer, legendName);
                    boolean won = isUserWinner(analysis, myBoxer.getName());
                    userService.recordCustomFightResult(chatId, won);

                    sendFightResultWithButtons(chatId, analysis);

                    if (won) {
                        long balance = userService.awardWin(chatId, 500L);
                        sendSimpleText(chatId, "🏆 **СЕНСАЦИЯ!** Твой боксер победил легенду!\n💰 Награда: **+500 монет** | Баланс: `" + balance + "` 🪙");
                    } else {
                        sendSimpleText(chatId, "🥊 Легенда оказалась сильнее. Твой боец получил ценный опыт!");
                    }
                } catch (Exception e) {
                    log.error("Ошибка боя с кастомным боксером", e);
                }
            }
            return;
        }

        if (data.startsWith("BET_")) {
            String[] pair = pendingBets.get(chatId);
            if (pair == null) {
                sendSimpleText(chatId, "⚠️ Время ставки истекло. Нажмите «🎰 Сделать ставку» заново.");
                return;
            }

            String chosenBoxer = data.equals("BET_1") ? pair[0] : pair[1];
            long betAmount = 100L;

            if (!userService.placeBet(chatId, betAmount)) {
                sendSimpleText(chatId, "❌ У вас недостаточно монет для ставки! Ваш баланс меньше 100 🪙.");
                return;
            }

            sendSimpleText(chatId, String.format("✅ Ставка принята: **100 монет** на победу **%s**!\n\n⏳ *Бойцы выходят на ринг, симулируем поединок...*", chosenBoxer));
            runSimulation(chatId, pair[0], pair[1], chosenBoxer);
            return;
        }

        if (data.equals("REMATCH")) {
            String lastPair = lastFights.get(chatId);
            if (lastPair != null) {
                String[] boxers = lastPair.split(" vs ");
                sendSimpleText(chatId, "🔄 *Запускаем реванш...*");
                runSimulation(chatId, boxers[0], boxers[1], null);
            }
        } else if (data.equals("RANDOM_FIGHT")) {
            triggerRandomFight(chatId);
        }
    }

    private void triggerRandomFight(long chatId) {
        List<String> pair = getRandomLegends(2);
        runSimulation(chatId, pair.get(0), pair.get(1), null);
    }

    private void runSimulation(long chatId, String boxer1, String boxer2, String chosenBoxer) {
        lastFights.put(chatId, boxer1 + " vs " + boxer2);

        try {
            String analysis = fightService.simulateFight(boxer1, boxer2);
            userService.recordFight(chatId, boxer1, boxer2, analysis);

            sendFightResultWithButtons(chatId, analysis);

            if (chosenBoxer != null) {
                boolean won = isUserWinner(analysis, chosenBoxer);
                if (won) {
                    long newBalance = userService.awardWin(chatId, 200L);
                    sendSimpleText(chatId, String.format("🎉 **ПОБЕДА!** Твой боксер **%s** выиграл бой!\n💰 Выигрыш: **+200 монет** | Баланс: `%d` 🪙", chosenBoxer, newBalance));
                } else {
                    long currentBalance = userService.getBalance(chatId);
                    sendSimpleText(chatId, String.format("😢 **ПРОИГРЫШ!** Твой боксер уступил победу.\n💰 Потеряно: 100 монет | Баланс: `%d` 🪙", currentBalance));
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при симуляции", e);
            sendSimpleText(chatId, "❌ Ошибка при генерации боя: " + e.getMessage());
        }
    }

    private boolean isUserWinner(String analysis, String chosenBoxer) {
        if (analysis == null || chosenBoxer == null) return false;

        for (String line : analysis.split("\n")) {
            String upper = line.toUpperCase();
            if (upper.contains("ПОБЕДИТЕЛЬ:") || upper.contains("ПОБЕДИТЕЛЬ :")) {
                String[] parts = chosenBoxer.trim().split(" ");
                String lastName = parts[parts.length - 1].toLowerCase();
                return line.toLowerCase().contains(lastName);
            }
        }
        return false;
    }

    private void clearChat(long chatId, int currentMessageId) {
        // Проходимся по последним 60 сообщениям назад и удаляем их
        for (int i = 0; i < 60; i++) {
            int msgIdToDelete = currentMessageId - i;
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(msgIdToDelete);
                execute(deleteMessage);
            } catch (Exception ignored) {
                // Игнорируем ошибки (если сообщение уже удалено или старше 48 часов)
            }
        }
        // Отправляем свежее чистое меню
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
            log.error("Ошибка отправки сообщения", e);
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
        StringBuilder sb = new StringBuilder("🔥 **Популярные культовые пары для ввода:**\n\n");
        sb.append("• `Майк Тайсон vs Мухаммед Али`\n");
        sb.append("• `Флойд Мейвезер vs Мэнни Пакьяо`\n");
        sb.append("• `Александр Усик vs Энтони Джошуа`\n");
        sb.append("• `Артур Бетербиев vs Дмитрий Бивол`\n");
        sb.append("• `Геннадий Головкин vs Сауль Альварес`\n");
        sb.append("• `Рой Джонс vs Бернард Хопкинс`\n");
        sb.append("\n_Скопируйте любую или отправьте собственную через **vs**!_");
        sendSimpleText(chatId, sb.toString());
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

                try {
                    Message sent = execute(message);
                    if (sent != null) {
                        trackMessage(chatId, sent.getMessageId());
                    }
                } catch (TelegramApiException e) {
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
            start = end;
        }
    }
}