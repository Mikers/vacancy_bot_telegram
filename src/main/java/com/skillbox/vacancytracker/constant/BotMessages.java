package com.skillbox.vacancytracker.constant;

public final class BotMessages {
    public static final String WELCOME_NEW_USER = "Добро пожаловать в Vacancy Tracker Bot! 🎯\n" +
            "Я помогу вам найти идеальную работу.\n\n" +
            "Используйте /menu для начала настройки поиска вакансий.";
    
    public static final String WELCOME_BACK = "С возвращением! 👋\n" +
            "Ваши настройки поиска сохранены.\n\n" +
            "Используйте /menu для изменения параметров поиска.";
    
    public static final String UNKNOWN_COMMAND = "Неизвестная команда. Используйте /help для списка доступных команд.";
    
    public static final String ERROR_GENERIC = "Произошла ошибка. Пожалуйста, попробуйте позже.";
    
    private BotMessages() {
    }
}