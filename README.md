

# 🥊 Boxing God Bot

🤖 **Telegram Bot:** [@BoxingGod_Bot](https://t.me/BoxingGod_Bot)

---

## 🇬🇧 English

### Overview
An AI-powered tactical boxing fight simulator and Telegram mini-game built with **Java 21**, **Spring Boot 3**, and **Spring AI**. Simulate dream matchups between boxing legends in their prime, build and level up your custom boxer, and place virtual bets on generated fights.

### Key Features
* **Tactical AI Fight Simulation:**
  * Detailed round-by-round breakdown (Rounds 1–4, 5–8, 9–12) factoring in fighter reach, punching power, hand speed, and signature combinations.
  * Stance and combat geometry awareness: strict analysis of Orthodox vs. Southpaw matchups and lead-hand positioning.
  * Free-text input: simulate any fight directly (e.g., *Mike Tyson vs. Muhammad Ali* or *Oleksandr Usyk vs. Anthony Joshua*).
* **"My Boxer" RPG Mode:**
  * Create and customize your fighter across 5 attributes: **Strength**, **Speed**, **Stamina**, **Chin**, and **Ring-IQ**.
  * Challenge over 100+ real-world boxing legends across all eras (from the 1920s to modern undisputed champions).
  * Persistent win-loss record with rewards for major underdog upsets.
* **Virtual Betting Arena:**
  * Start with a bankroll of 1,000 coins.
  * Place 100-coin bets on randomly generated legend matchups with a 2x payout for guessing the winner.
* **Interactive UI & Controls:**
  * Intuitive Reply and Inline keyboards.
  * Instant "Rematch", "Random Fight", and "🧹 Clear Chat" buttons.
* **Built-in REST API:**
  * Dedicated HTTP endpoint to test fight generation directly without opening Telegram.

### Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot 3.3.5
* **AI Integration:** Spring AI (compatible with OpenAI, Groq, Ollama)
* **Telegram API:** TelegramBots Spring Boot Starter
* **Persistence:** Spring Data JPA + In-Memory H2 Database
* **Build Tool:** Maven

### Environment Variables
| Variable | Description | Default |
| :--- | :--- | :--- |
| `BOT_TOKEN` | Telegram Bot token from [@BotFather](https://t.me/botfather) | — |
| `AI_API_KEY` | API Key for LLM provider (OpenAI / Groq) | — |
| `AI_BASE_URL` | Base URL for LLM provider | `https://api.openai.com` |

### Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/boxing_god_bot.git
   cd boxing_god_bot

2.  Set environment variables and run:
    # Linux / macOS
    export BOT_TOKEN="your_telegram_bot_token"
    export AI_API_KEY="your_api_key"
    ./mvnw clean spring-boot:run

    # Windows
    set BOT_TOKEN=your_telegram_bot_token
    set AI_API_KEY=your_api_key
    mvnw.cmd clean spring-boot:run

REST API Usage

You can test the fight simulator directly via HTTP GET:

GET http://localhost:8080/api/fight?boxer1=Mike Tyson&boxer2=Muhammad Ali

🇷🇺 Русский

Описание

Интеллектуальный симулятор боксерских поединков и мини-игра в Telegram на базе
Java 21, Spring Boot 3 и Spring AI. Моделируйте бои мечты между легендами бокса
в их пиковой форме, создавайте и прокачивайте собственного бойца, а также
делайте виртуальные ставки на исходы поединков.

Основные возможности

  - Тактическая ИИ-симуляция боев:
      - Пошаговый разбор поединка по раундам (1–4, 5–8, 9–12) с учетом
        габаритов, дистанции атаки (reach), скорости, нокаутирующей мощи и
        коронных комбинаций.
      - Точная боевая геометрия стоек: строгий учет противостояний «правша
        против левши» (Orthodox vs. Southpaw), передней руки и силовых ударов.
      - Свободный текстовый ввод: отправляйте любую пару бойцов напрямую
        (например: Майк Тайсон vs Мухаммед Али или Усик vs Джошуа).
  - RPG-режим «Мой боксер»:
      - Создание и кастомизация персонажа по 5 характеристикам: Сила, Скорость,
        Выносливость, Челюсть и Ринг-IQ.
      - Вызов на бой более 100+ реальных мировых легенд всех эпох (от 1920-х
        годов до современных чемпионов).
      - Ведение послужного списка побед и поражений (Record) с наградами за
        апсеты и победы над фаворитами.
  - Арена виртуальных ставок:
      - Стартовый капитал в 1 000 монет для каждого игрока.
      - Ставки по 100 монет на исход случайных боев легенд с выплатой 2x за
        угаданного победителя.
  - Интерактивный интерфейс:
      - Удобные Reply и Inline-клавиатуры.
      - Кнопки мгновенного запуска «Реванша», «Случайного боя» и быстрой очистки
        чата («🧹 Очистить чат»).
  - Встроенный REST API:
      - Быстрый HTTP-эндпоинт для проверки генерации симуляции без Telegram.

Стек технологий

  - Язык: Java 21
  - Фреймворк: Spring Boot 3.3.5
  - Интеграция с ИИ: Spring AI (совместимо с OpenAI, Groq или Ollama)
  - Telegram API: TelegramBots Spring Boot Starter
  - Хранение данных: Spring Data JPA + In-Memory база данных H2
  - Сборка проекта: Maven

Переменные окружения

| Переменная    | Описание                                           | По умолчанию             |
| :------------ | :------------------------------------------------- | :----------------------- |
| `BOT_TOKEN`   | Токен бота от [@BotFather](https://t.me/botfather) | —                        |
| `AI_API_KEY`  | API-ключ для нейросети (OpenAI / Groq)             | —                        |
| `AI_BASE_URL` | Базовый URL API нейросети                          | `https://api.openai.com` |

Запуск проекта

1.  Клонируйте репозиторий:

    git clone https://github.com/YOUR_USERNAME/boxing_god_bot.git
    cd boxing_god_bot

2.  Установите переменные окружения и запустите:

    # Linux / macOS
    export BOT_TOKEN="ваш_токен_бота"
    export AI_API_KEY="ваш_api_ключ"
    ./mvnw clean spring-boot:run

    # Windows
    set BOT_TOKEN=ваш_токен_бота
    set AI_API_KEY=ваш_api_ключ
    mvnw.cmd clean spring-boot:run

Использование REST API

Протестировать симулятор можно напрямую через HTTP GET запрос:

GET http://localhost:8080/api/fight?boxer1=Майк Тайсон&boxer2=Мухаммед Али

