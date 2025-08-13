package com.skillbox.vacancytracker.presentation.command;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.presentation.dto.UserMessage;
import com.skillbox.vacancytracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RegionCommandCoverageTest {

    @Mock
    private UserService userService;
    
    private RegionCommand regionCommand;
    private BotUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        regionCommand = new RegionCommand(userService);
        
        testUser = new BotUser();
        testUser.setUserId(1L);
        testUser.setChatId(100L);
        testUser.setUsername("testuser");
        testUser.setSearchCriteria(new SearchCriteria());
    }

    @Test
    void shouldShowRegionListWithPreviousNavigation() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        Method showRegionListMethod = RegionCommand.class.getDeclaredMethod("showRegionList", Long.class, int.class);
        showRegionListMethod.setAccessible(true);
        
        SendMessage result = (SendMessage) showRegionListMethod.invoke(regionCommand, 100L, 1);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Выберите регион:");
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) result.getReplyMarkup();
        assertThat(keyboard.getKeyboard()).isNotEmpty();
        
        InlineKeyboardRow lastRow = keyboard.getKeyboard().get(keyboard.getKeyboard().size() - 1);
        boolean hasPreviousButton = lastRow.stream()
            .anyMatch(button -> button.getText().contains("Предыдущие 10"));
        assertThat(hasPreviousButton).isTrue();
        
        boolean hasNextButton = lastRow.stream()
            .anyMatch(button -> button.getText().contains("Следующие 10"));
        assertThat(hasNextButton).isTrue();
    }

    @Test
    void shouldShowRegionListWithOnlyPreviousNavigation() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        Method showRegionListMethod = RegionCommand.class.getDeclaredMethod("showRegionList", Long.class, int.class);
        showRegionListMethod.setAccessible(true);
        
        int totalRegions = 85;
        int regionsPerPage = 10;
        int lastPage = (totalRegions - 1) / regionsPerPage;
        
        SendMessage result = (SendMessage) showRegionListMethod.invoke(regionCommand, 100L, lastPage);
        
        assertThat(result.getChatId()).isEqualTo("100");
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) result.getReplyMarkup();
        assertThat(keyboard.getKeyboard()).isNotEmpty();
        
        InlineKeyboardRow lastRow = keyboard.getKeyboard().get(keyboard.getKeyboard().size() - 1);
        boolean hasPreviousButton = lastRow.stream()
            .anyMatch(button -> button.getText().contains("Предыдущие 10"));
        assertThat(hasPreviousButton).isTrue();
        
        boolean hasNextButton = lastRow.stream()
            .anyMatch(button -> button.getText().contains("Следующие 10"));
        assertThat(hasNextButton).isFalse();
    }

    @Test
    void shouldHandleNavigationButtonGeneration() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        Method showRegionListMethod = RegionCommand.class.getDeclaredMethod("showRegionList", Long.class, int.class);
        showRegionListMethod.setAccessible(true);
        
        SendMessage resultPage0 = (SendMessage) showRegionListMethod.invoke(regionCommand, 100L, 0);
        InlineKeyboardMarkup keyboardPage0 = (InlineKeyboardMarkup) resultPage0.getReplyMarkup();
        
        InlineKeyboardRow lastRowPage0 = keyboardPage0.getKeyboard().get(keyboardPage0.getKeyboard().size() - 1);
        boolean hasOnlyNextButton = lastRowPage0.size() == 1 && 
            lastRowPage0.get(0).getText().contains("Следующие 10");
        assertThat(hasOnlyNextButton).isTrue();
        
        SendMessage resultPage1 = (SendMessage) showRegionListMethod.invoke(regionCommand, 100L, 1);
        InlineKeyboardMarkup keyboardPage1 = (InlineKeyboardMarkup) resultPage1.getReplyMarkup();
        
        InlineKeyboardRow lastRowPage1 = keyboardPage1.getKeyboard().get(keyboardPage1.getKeyboard().size() - 1);
        boolean hasBothButtons = lastRowPage1.size() == 2;
        assertThat(hasBothButtons).isTrue();
    }
    
    @Test 
    void shouldVerifyPreviousButtonCallbackData() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        Method showRegionListMethod = RegionCommand.class.getDeclaredMethod("showRegionList", Long.class, int.class);
        showRegionListMethod.setAccessible(true);
        
        SendMessage result = (SendMessage) showRegionListMethod.invoke(regionCommand, 100L, 2);
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) result.getReplyMarkup();
        InlineKeyboardRow lastRow = keyboard.getKeyboard().get(keyboard.getKeyboard().size() - 1);
        
        String previousButtonCallbackData = lastRow.stream()
            .filter(button -> button.getText().contains("Предыдущие 10"))
            .findFirst()
            .map(button -> button.getCallbackData())
            .orElse("");
            
        assertThat(previousButtonCallbackData).isEqualTo("region_page_1");
    }

    @Test
    void shouldVerifyNextButtonCallbackData() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        Method showRegionListMethod = RegionCommand.class.getDeclaredMethod("showRegionList", Long.class, int.class);
        showRegionListMethod.setAccessible(true);
        
        SendMessage result = (SendMessage) showRegionListMethod.invoke(regionCommand, 100L, 0);
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) result.getReplyMarkup();
        InlineKeyboardRow lastRow = keyboard.getKeyboard().get(keyboard.getKeyboard().size() - 1);
        
        String nextButtonCallbackData = lastRow.stream()
            .filter(button -> button.getText().contains("Следующие 10"))
            .findFirst()
            .map(button -> button.getCallbackData())
            .orElse("");
            
        assertThat(nextButtonCallbackData).isEqualTo("region_page_1");
    }
}