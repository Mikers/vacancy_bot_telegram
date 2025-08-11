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

import java.util.*;

public class RegionCommand implements BotCommand {
    private static final Map<Integer, String> REGIONS = createRegionsMap();
    private static final int REGIONS_PER_PAGE = 10;
    
    private final UserService userService;
    
    public RegionCommand(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public String getCommandName() {
        return "/region";
    }
    
    @Override
    public String getDescription() {
        return "Выбрать регион поиска";
    }
    
    @Override
    public boolean canHandle(UserMessage message) {
        return message.text().startsWith("/region");
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
        
        String[] parts = message.text().split("\\s+");
        if (parts.length > 1) {
            try {
                int regionCode = Integer.parseInt(parts[1]);
                return setRegion(userOpt.get(), message.chatId(), regionCode);
            } catch (NumberFormatException e) {
                return SendMessage.builder()
                        .chatId(message.chatId())
                        .text("Неверный формат. Используйте: /region КОД_РЕГИОНА")
                        .build();
            }
        }
        
        return showRegionList(message.chatId(), 0);
    }
    
    private SendMessage setRegion(BotUser user, Long chatId, int regionCode) {
        if (!REGIONS.containsKey(regionCode)) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Неверный код региона: " + regionCode)
                    .build();
        }
        
        SearchCriteria criteria = user.getSearchCriteria();
        if (criteria == null) {
            criteria = new SearchCriteria();
            user.setSearchCriteria(criteria);
        }
        criteria.setRegionCode(regionCode);
        userService.save(user);
        
        return SendMessage.builder()
                .chatId(chatId)
                .text("Регион установлен: " + REGIONS.get(regionCode) + " [" + regionCode + "]\n\nИспользуйте /menu для продолжения настройки.")
                .build();
    }
    
    private SendMessage showRegionList(Long chatId, int page) {
        List<Map.Entry<Integer, String>> regionList = new ArrayList<>(REGIONS.entrySet());
        regionList.sort(Map.Entry.comparingByValue());
        
        int startIndex = page * REGIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + REGIONS_PER_PAGE, regionList.size());
        
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder markupBuilder = InlineKeyboardMarkup.builder();
        
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<Integer, String> region = regionList.get(i);
            markupBuilder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text(region.getValue() + " (" + region.getKey() + ")")
                    .callbackData("region_" + region.getKey())
                    .build()));
        }
        
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        if (page > 0) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("← Предыдущие 10")
                    .callbackData("region_page_" + (page - 1))
                    .build());
        }
        if (endIndex < regionList.size()) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("Следующие 10 →")
                    .callbackData("region_page_" + (page + 1))
                    .build());
        }
        if (!navigationRow.isEmpty()) {
            markupBuilder.keyboardRow(new InlineKeyboardRow(navigationRow));
        }
        
        InlineKeyboardMarkup markup = markupBuilder.build();
        
        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите регион:")
                .replyMarkup(markup)
                .build();
    }
    
    private static Map<Integer, String> createRegionsMap() {
        Map<Integer, String> regions = new HashMap<>();
        regions.put(1, "Республика Адыгея");
        regions.put(2, "Республика Башкортостан");
        regions.put(3, "Республика Бурятия");
        regions.put(4, "Республика Алтай");
        regions.put(5, "Республика Дагестан");
        regions.put(6, "Республика Ингушетия");
        regions.put(7, "Кабардино-Балкарская Республика");
        regions.put(8, "Республика Калмыкия");
        regions.put(9, "Карачаево-Черкесская Республика");
        regions.put(10, "Республика Карелия");
        regions.put(11, "Республика Коми");
        regions.put(12, "Республика Марий Эл");
        regions.put(13, "Республика Мордовия");
        regions.put(14, "Республика Саха (Якутия)");
        regions.put(15, "Республика Северная Осетия - Алания");
        regions.put(16, "Республика Татарстан");
        regions.put(17, "Республика Тыва");
        regions.put(18, "Удмуртская Республика");
        regions.put(19, "Республика Хакасия");
        regions.put(20, "Чеченская Республика");
        regions.put(21, "Чувашская Республика");
        regions.put(22, "Алтайский край");
        regions.put(23, "Краснодарский край");
        regions.put(24, "Красноярский край");
        regions.put(25, "Приморский край");
        regions.put(26, "Ставропольский край");
        regions.put(27, "Хабаровский край");
        regions.put(28, "Амурская область");
        regions.put(29, "Архангельская область");
        regions.put(30, "Астраханская область");
        regions.put(31, "Белгородская область");
        regions.put(32, "Брянская область");
        regions.put(33, "Владимирская область");
        regions.put(34, "Волгоградская область");
        regions.put(35, "Вологодская область");
        regions.put(36, "Воронежская область");
        regions.put(37, "Ивановская область");
        regions.put(38, "Иркутская область");
        regions.put(39, "Калининградская область");
        regions.put(40, "Калужская область");
        regions.put(41, "Камчатский край");
        regions.put(42, "Кемеровская область");
        regions.put(43, "Кировская область");
        regions.put(44, "Костромская область");
        regions.put(45, "Курганская область");
        regions.put(46, "Курская область");
        regions.put(47, "Ленинградская область");
        regions.put(48, "Липецкая область");
        regions.put(49, "Магаданская область");
        regions.put(50, "Московская область");
        regions.put(51, "Мурманская область");
        regions.put(52, "Нижегородская область");
        regions.put(53, "Новгородская область");
        regions.put(54, "Новосибирская область");
        regions.put(55, "Омская область");
        regions.put(56, "Оренбургская область");
        regions.put(57, "Орловская область");
        regions.put(58, "Пензенская область");
        regions.put(59, "Пермский край");
        regions.put(60, "Псковская область");
        regions.put(61, "Ростовская область");
        regions.put(62, "Рязанская область");
        regions.put(63, "Самарская область");
        regions.put(64, "Саратовская область");
        regions.put(65, "Сахалинская область");
        regions.put(66, "Свердловская область");
        regions.put(67, "Смоленская область");
        regions.put(68, "Тамбовская область");
        regions.put(69, "Тверская область");
        regions.put(70, "Томская область");
        regions.put(71, "Тульская область");
        regions.put(72, "Тюменская область");
        regions.put(73, "Ульяновская область");
        regions.put(74, "Челябинская область");
        regions.put(75, "Забайкальский край");
        regions.put(76, "Ярославская область");
        regions.put(77, "Москва");
        regions.put(78, "Санкт-Петербург");
        regions.put(79, "Еврейская автономная область");
        regions.put(83, "Ненецкий автономный округ");
        regions.put(86, "Ханты-Мансийский автономный округ");
        regions.put(87, "Чукотский автономный округ");
        regions.put(89, "Ямало-Ненецкий автономный округ");
        regions.put(91, "Республика Крым");
        regions.put(92, "Севастополь");
        return regions;
    }
}