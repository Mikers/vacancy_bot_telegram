# Vacancy Tracker Bot

A Telegram bot for tracking and searching job vacancies through REST API aggregator.

## Description

This Telegram bot serves as a personal assistant for job searching. It automatically searches and filters vacancies according to specified criteria, periodically checks for updates, and sends notifications to users.

### Key Features

- 🔍 Job search through trudvsem.ru open data API
- 📝 Customizable search criteria (region, experience, salary, keywords)
- 🔔 Automatic notifications about new matching vacancies
- 💾 File-based data persistence (no database required)
- 🕐 User timezone support
- 📱 Intuitive interface with buttons and commands

## Technical Implementation

The project is implemented using **standard Java tools without frameworks**:

- **Java 21** with modern language features
- **Gradle** for build management and dependencies
- **Three-tier architecture**: Presentation → Business Logic → Data Access
- **File-based storage** with JSON serialization
- **Native HTTP client** for REST API integration
- **Task scheduler** for automated checks
- **Logging** with SLF4J + Logback

## Architecture Overview

### Design Decisions

1. **No Framework Approach**: The bot is built without Spring or other heavyweight frameworks to reduce complexity and dependencies.

2. **File-Based Storage**: Uses JSON files instead of a database for simplicity and portability. Data is stored in:
   - `data/users/` - User profiles and search criteria
   - `data/vacancies/` - Cached vacancy data
   - `data/notifications/` - Notification history

3. **Layered Architecture**: Clear separation of concerns:
   - **Presentation Layer**: Handles Telegram Bot API interactions
   - **Business Logic Layer**: Processes vacancies, manages users
   - **Data Access Layer**: Abstracts file operations with Repository pattern

4. **Asynchronous Processing**: Uses `ScheduledExecutorService` for:
   - Periodic vacancy updates (every 4 hours)
   - Daily notifications at user-specified times
   - Non-blocking message processing

## Quick Start

### Requirements

- Java 21 or higher
- Telegram bot token from [@BotFather](https://t.me/botfather)

### Installation and Running

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd vacancy_bot_telegram
   ```

2. Set the bot token environment variable:
   ```bash
   export BOT_TOKEN="your_telegram_bot_token_here"
   ```

3. Run the application:
   ```bash
   ./gradlew runBot
   ```

   Or build JAR and run:
   ```bash
   ./gradlew build
   java -jar build/libs/vacancy-tracker-bot-1.0.0.jar
   ```

### Configuration

The application looks for configuration in the following order:
1. File path from `CONFIG_PATH` environment variable
2. `src/main/resources/application.json`

Example configuration file:
```json
{
  "bot_token": "YOUR_BOT_TOKEN",
  "bot_name": "VacancyTrackerBot",
  "vacancy_api_url": "https://opendata.trudvsem.ru/api/v1/vacancies",
  "data_directory": "data",
  "update_interval_hours": 4,
  "max_vacancies_per_request": 100,
  "connection_timeout_seconds": 30
}
```

## Usage

### Bot Commands

- `/start` - Register and begin using the bot
- `/menu` - Main menu for configuring search criteria
- `/region <code>` - Set search region (e.g., 78 for St. Petersburg)
- `/experience <years>` - Minimum required experience
- `/salary <amount>` - Minimum salary
- `/keyword <text>` - Keyword for search
- `/notify <time>` - Daily notification time (format: HH:mm)
- `/stop` - Disconnect from bot

### Usage Example

1. Start the bot with `/start`
2. Configure timezone (e.g., `UTC+3`)
3. Open menu with **Menu** button
4. Configure search criteria:
   - Select region (e.g., St. Petersburg - code 78)
   - Specify minimum experience
   - Set desired salary
   - Add keywords
5. Set notification time
6. Click **Done**

The bot will search for matching vacancies daily and send notifications at the specified time.

## Project Structure

```
src/main/java/com/vacancytracker/
├── VacancyTrackerBotApplication.java  # Entry point
├── config/                            # Configuration
│   ├── BotConfig.java                # Bot configuration model
│   └── ConfigurationManager.java     # Config loading and validation
├── model/                             # Data models
│   ├── BotUser.java                  # User entity with preferences
│   ├── Vacancy.java                  # Vacancy data structure
│   ├── SearchCriteria.java           # Search parameters
│   └── Region.java                   # Region codes and names
├── repository/                        # Data access layer
│   ├── Repository.java               # Generic repository interface
│   ├── UserRepository.java           # User data persistence
│   ├── VacancyRepository.java        # Vacancy caching
│   └── AbstractJsonRepository.java   # JSON serialization base
├── service/                           # Business logic
│   ├── UserService.java              # User management
│   ├── VacancyApiClient.java         # API client interface
│   ├── TrudvsemApiClient.java        # API implementation
│   └── NotificationScheduler.java    # Notification management
├── presentation/                      # Presentation layer
│   ├── VacancyTrackerBot.java        # Main bot class
│   ├── BotCommand.java               # Command interface
│   ├── CommandDispatcher.java        # Command routing
│   └── command/                      # Command implementations
│       ├── StartCommand.java
│       ├── MenuCommand.java
│       ├── RegionCommand.java
│       └── ...
└── exception/                         # Custom exceptions
    ├── BotException.java
    ├── ApiException.java
    └── DataAccessException.java
```

## Implementation Details

### Key Design Patterns

1. **Repository Pattern**: Abstracts data access logic, making it easy to switch storage mechanisms
2. **Command Pattern**: Each bot command is a separate class implementing `BotCommand` interface
3. **Factory Pattern**: `CommandDispatcher` creates appropriate command handlers
4. **Singleton Pattern**: Configuration manager ensures single configuration instance
5. **Template Method**: `AbstractJsonRepository` provides common JSON operations

### Data Flow

1. **User Registration**:
   - User sends `/start` → Bot creates `BotUser` → Saved to `UserRepository`
   - Default search criteria are initialized
   - User is prompted to set timezone

2. **Search Configuration**:
   - User updates criteria → `SearchCriteria` object updated → Persisted to file
   - Changes trigger immediate vacancy search
   - Results are cached for efficiency

3. **Vacancy Processing**:
   - API client fetches vacancies → Filtered by criteria → Deduplicated against cache
   - New vacancies are stored → Users are notified based on preferences
   - Cache expires after 24 hours

### Error Handling

- **Graceful degradation**: API failures don't crash the bot
- **Retry mechanism**: Failed API calls retry with exponential backoff
- **User feedback**: Clear error messages sent to users
- **Logging**: All errors logged with context for debugging

## Development

### Building and Testing

```bash
# Compilation
./gradlew compileJava

# Run tests
./gradlew test

# Full build with tests and checks
./gradlew build

# Test coverage report (minimum 80%)
./gradlew jacocoTestReport
```

### Adding New Commands

1. Create a class implementing `BotCommand` interface:
```java
public class CustomCommand implements BotCommand {
    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/custom");
    }
    
    @Override
    public void handle(Update update, VacancyTrackerBot bot) {
        // Implementation
    }
}
```

2. Register the command in `VacancyTrackerBotApplication`:
```java
dispatcher.registerCommand(new CustomCommand());
```

### Development Principles

- **SOLID**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **KISS**: Keep it simple and straightforward
- **DRY**: Don't repeat yourself - use abstractions
- **YAGNI**: You aren't gonna need it - implement only required features
- **Clean Code**: Meaningful names, small functions, proper abstractions

## API Integration

The bot uses trudvsem.ru open API for vacancy search:

- **Base URL**: https://opendata.trudvsem.ru/api/v1/vacancies
- **Format**: JSON
- **Filters**: 
  - `regionCode` - Region identifier
  - `experience` - Minimum years of experience
  - `salary` - Minimum salary
  - `text` - Keywords search
  - `modifiedFrom` - Get only recent vacancies

### API Response Structure
```json
{
  "results": {
    "vacancies": [
      {
        "vacancy": {
          "id": "123",
          "job-name": "Software Developer",
          "salary": "150000",
          "requirement": {
            "experience": "3"
          },
          "company": {
            "name": "Tech Corp"
          }
        }
      }
    ]
  }
}
```

## Monitoring and Logs

Logs are saved in `logs/` directory:
- `vacancy-tracker-bot.log` - Main log file
- Daily rotation, maximum 30 days retention
- Different log levels for components:
  - `DEBUG` - Detailed execution flow
  - `INFO` - General information
  - `WARN` - Potential issues
  - `ERROR` - Failures requiring attention

### Log Configuration
```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/vacancy-tracker-bot.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/vacancy-tracker-bot.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
</configuration>
```

## Performance Considerations

- **Caching**: Vacancies cached for 24 hours to reduce API calls
- **Pagination**: Large result sets processed in batches
- **Connection pooling**: Reuses HTTP connections for efficiency
- **Async processing**: Non-blocking message handling
- **Rate limiting**: Respects API rate limits (100 requests/minute)

## Security

- **Token protection**: Bot token stored in environment variables
- **Input validation**: All user inputs sanitized
- **File permissions**: Restrictive permissions on data files
- **No SQL injection**: File-based storage eliminates SQL risks
- **HTTPS only**: All API communications encrypted

## License

MIT License

## Support

For questions or issues, please create an issue in the project repository.