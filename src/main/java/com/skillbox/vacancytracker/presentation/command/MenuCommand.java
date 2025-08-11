package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.presentation.BotCommand;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import com.skillbox.vacancytracker.service.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuCommand implements BotCommand {
    private final UserService userService;
    
    public MenuCommand(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public String getCommandName() {
        return "/menu";
    }
    
    @Override
    public String getDescription() {
        return "Открыть главное меню";
    }
    
    @Override
    public boolean canHandle(UserMessage message) {
        return message.text().equals("/menu") || message.text().equalsIgnoreCase("menu");
    }
    
    @Override
    public SendMessage handle(UserMessage message) {
        Optional<BotUser> userOpt = userService.findById(message.userId());
        
        if (userOpt.isEmpty()) {
            return SendMessage.builder()
                    .chatId(message.chatId())
                    .text("Пожалуйста, сначала используйте /start для регистрации")
                    .build();
        }
        
        BotUser user = userOpt.get();
        SearchCriteria criteria = user.getSearchCriteria();
        
        StringBuilder text = new StringBuilder();
        text.append("Задайте критерии поиска и время нотификации.\n");
        text.append("Затем нажмите \"Готово\", либо введите команду /ready.\n\n");
        
        InlineKeyboardMarkup markup = createMenuKeyboard(criteria, user);
        
        return SendMessage.builder()
                .chatId(message.chatId())
                .text(text.toString())
                .replyMarkup(markup)
                .build();
    }
    
    private InlineKeyboardMarkup createMenuKeyboard(SearchCriteria criteria, BotUser user) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(createButton("Регион" + formatRegion(criteria), "menu_region")))
                .keyboardRow(new InlineKeyboardRow(createButton("Минимальный опыт" + formatExperience(criteria), "menu_experience")))
                .keyboardRow(new InlineKeyboardRow(createButton("Минимальная зарплата" + formatSalary(criteria), "menu_salary")))
                .keyboardRow(new InlineKeyboardRow(createButton("Слово для поиска" + formatKeyword(criteria), "menu_keyword")))
                .keyboardRow(new InlineKeyboardRow(createButton("Настройки нотификации" + formatNotification(user), "menu_notification")))
                .keyboardRow(new InlineKeyboardRow(createButton("Готово", "menu_ready")))
                .build();
    }
    
    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
    
    private String formatRegion(SearchCriteria criteria) {
        if (criteria == null || criteria.getRegionCode() == null) {
            return "";
        }
        return " [" + criteria.getRegionCode() + "]";
    }
    
    private String formatExperience(SearchCriteria criteria) {
        if (criteria == null || criteria.getMinimumExperience() == null) {
            return "";
        }
        return " [" + criteria.getMinimumExperience() + "]";
    }
    
    private String formatSalary(SearchCriteria criteria) {
        if (criteria == null || criteria.getMinimumSalary() == null) {
            return "";
        }
        return " [" + criteria.getMinimumSalary() + "]";
    }
    
    private String formatKeyword(SearchCriteria criteria) {
        if (criteria == null || criteria.getKeyword() == null || criteria.getKeyword().isEmpty()) {
            return "";
        }
        return " [" + criteria.getKeyword() + "]";
    }
    
    private String formatNotification(BotUser user) {
        if (user.getNotificationTime() == null) {
            return "";
        }
        return " [" + user.getNotificationTime() + "]";
    }
}