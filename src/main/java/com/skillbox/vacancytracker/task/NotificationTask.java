package com.skillbox.vacancytracker.task;

import com.skillbox.vacancytracker.constant.BotMessages;
import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.UserVacancy;
import com.skillbox.vacancytracker.model.Vacancy;
import com.skillbox.vacancytracker.repository.UserVacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(NotificationTask.class);
    private static final int VACANCIES_PER_PAGE = 10;
    
    private final BotUser user;
    private final UserVacancyRepository userVacancyRepository;
    private final TelegramClient telegramClient;
    
    public NotificationTask(BotUser user,
                           UserVacancyRepository userVacancyRepository,
                           TelegramClient telegramClient) {
        this.user = user;
        this.userVacancyRepository = userVacancyRepository;
        this.telegramClient = telegramClient;
    }
    
    @Override
    public void run() {
        if (!user.isActive()) {
            logger.debug("User {} is not active, skipping notification", user.getUserId());
            return;
        }
        
        try {
            List<UserVacancy> newVacancies = userVacancyRepository.findNewByUserId(user.getUserId());
            
            if (newVacancies.isEmpty()) {
                logger.debug("No new vacancies to notify user {}", user.getUserId());
                return;
            }
            
            logger.info("Sending notification to user {} about {} new vacancies", 
                       user.getUserId(), newVacancies.size());
            
            sendNotificationMessage(newVacancies);
            
            for (UserVacancy vacancy : newVacancies) {
                userVacancyRepository.markAsNotified(vacancy.getId());
            }
            
        } catch (Exception e) {
            logger.error("Error sending notification to user {}", user.getUserId(), e);
        }
    }
    
    private void sendNotificationMessage(List<UserVacancy> vacancies) throws TelegramApiException {
        StringBuilder messageText = new StringBuilder();
        messageText.append("🔔 Найдены новые вакансии: ").append(vacancies.size()).append("\n\n");
        
        int endIndex = Math.min(VACANCIES_PER_PAGE, vacancies.size());
        for (int i = 0; i < endIndex; i++) {
            Vacancy vacancy = vacancies.get(i).getVacancy();
            messageText.append(formatVacancy(i + 1, vacancy));
        }
        
        SendMessage message = SendMessage.builder()
                .chatId(user.getChatId())
                .text(messageText.toString())
                .parseMode("HTML")
                .build();
        
        if (vacancies.size() > VACANCIES_PER_PAGE) {
            message.setReplyMarkup(createPaginationKeyboard(vacancies.size()));
        }
        
        telegramClient.execute(message);
    }
    
    private String formatVacancy(int number, Vacancy vacancy) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(number).append(". ").append(vacancy.getTitle()).append("</b>\n");
        
        if (vacancy.getCompanyName() != null) {
            sb.append("Компания: ").append(vacancy.getCompanyName()).append("\n");
        }
        
        if (vacancy.getSalaryFrom() != null || vacancy.getSalaryTo() != null) {
            sb.append("Зарплата: ");
            if (vacancy.getSalaryFrom() != null) {
                sb.append("от ").append(vacancy.getSalaryFrom());
            }
            if (vacancy.getSalaryTo() != null) {
                if (vacancy.getSalaryFrom() != null) sb.append(" ");
                sb.append("до ").append(vacancy.getSalaryTo());
            }
            sb.append(" ").append(vacancy.getCurrency() != null ? vacancy.getCurrency() : "руб.");
            sb.append("\n");
        }
        
        if (vacancy.getExperienceRequired() != null) {
            sb.append("Опыт: ").append(formatExperience(vacancy.getExperienceRequired())).append("\n");
        }
        
        if (vacancy.getUrl() != null) {
            sb.append("<a href=\"").append(vacancy.getUrl()).append("\">Подробнее</a>\n");
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    private String formatExperience(Integer years) {
        if (years == null || years == 0) return "без опыта";
        if (years == 1) return "1 год";
        if (years >= 2 && years <= 4) return years + " года";
        return years + " лет";
    }
    
    private InlineKeyboardMarkup createPaginationKeyboard(int totalVacancies) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Следующие " + VACANCIES_PER_PAGE + " →")
                .callbackData("next_vacancies_1")
                .build());
        
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(row))
                .build();
    }
}