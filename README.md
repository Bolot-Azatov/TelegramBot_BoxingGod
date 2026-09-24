🥊 Boxing God Bot (@BoxingGod_Bot)

Java Spring Boot Spring AI Telegram

An intelligent, AI-powered boxing match simulation game in Telegram. Simulate
dream matchups between prime boxing legends, create and manage your own custom
fighter, and place virtual bets on fight outcomes.

✨ Features

  - 🤖 Tactical AI Match Simulation

      - Realistic round-by-round breakdown (Rounds 1–4, 5–8, 9–12) factoring in
        physical reach, speed, punching power, and signature punches.
      - Accurate boxing stance dynamics: strict handling of Orthodox vs.
        Southpaw angles, lead jabs, and power crosses.
      - Free-form text input: enter any matchup directly (e.g., Mike Tyson vs
        Muhammad Ali or Usyk vs Joshua).

  - 🥊 "My Boxer" RPG Mode

      - Create and customize your fighter with 5 key attributes: Power, Speed,
        Stamina, Chin, and Ring-IQ.
      - Challenge over 100+ real-world boxing legends spanning all eras (from
        the 1920s to modern champions).
      - Track your fighter's career record (Wins / Losses) and earn coin rewards
        for upset victories.

  - 🎰 Virtual Betting Arena

      - Every player starts with 1,000 coins.
      - Place 100-coin bets on randomized legend fights with a 2x payout for
        picking the winner.

  - ⚡ Interactive UI & Extras

      - Clean Telegram reply & inline keyboard menus.
      - Instant Rematch and Random Fight actions.
      - "🧹 Clean Chat" tool to purge recent messages and keep the interface
        tidy.

  - 🔌 Built-in REST API

      - Fast test endpoint to simulate fights via HTTP without opening Telegram.

🛠 Tech Stack

  - Language: Java 21
  - Framework: Spring Boot 3.3.5
  - AI Integration: Spring AI (compatible with OpenAI, Groq, or Ollama)
  - Telegram API: TelegramBots Spring Boot Starter
  - Persistence: Spring Data JPA + H2 In-Memory Database
  - Build Tool: Maven

🚀 Getting Started

1. Clone the Repository

git clone https://github.com/YOUR_USERNAME/boxing_god_bot.git
cd boxing_god_bot

2. Configure Environment Variables

Set your credentials in src/main/resources/application.yaml or export them as
environment variables:

spring:
  application:
    name: boxing_god_bot
  ai:
    openai:
      api-key: ${AI_API_KEY:your_openai_or_groq_api_key}
      base-url: ${AI_BASE_URL:https://api.openai.com}
      chat:
        options:
          model: gpt-4o-mini

bot:
  name: BoxingGod_Bot
  token: ${BOT_TOKEN:your_telegram_bot_token}

spring.datasource.url: jdbc:h2:mem:boxingdb
spring.datasource.driverClassName: org.h2.Driver
spring.jpa.hibernate.ddl-auto: update

3. Run the Application

Linux / macOS:

./mvnw clean spring-boot:run

Windows:

mvnw.cmd clean spring-boot:run

🌐 REST API Usage

You can test the fight engine directly via HTTP:

GET http://localhost:8080/api/fight?boxer1=Mike Tyson&boxer2=Muhammad Ali

📂 Project Structure

src/main/java/org/boxinggod/boxing_god_bot/
├── config/         # Telegram Bot configuration
├── controller/     # REST testing endpoints
├── entity/         # JPA entities (AppUser, CustomBoxer, FightHistory)
├── repository/     # Spring Data JPA repositories
└── service/        # Spring AI simulation engine & user management

📄 License

This project is open-source and available for educational and personal use.
