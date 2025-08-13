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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegionCommandTest {

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
    void shouldReturnCorrectCommandName() {
        assertThat(regionCommand.getCommandName()).isEqualTo("/region");
    }

    @Test
    void shouldReturnCorrectDescription() {
        assertThat(regionCommand.getDescription()).isEqualTo("Выбрать регион поиска");
    }

    @Test
    void shouldHandleRegionCommand() {
        UserMessage message1 = new UserMessage(1L, 100L, "/region", "John", "Doe", "user1", true, "region", null);
        UserMessage message2 = new UserMessage(1L, 100L, "/region 77", "John", "Doe", "user1", true, "region", "77");
        UserMessage message3 = new UserMessage(1L, 100L, "/start", "John", "Doe", "user1", true, "start", null);
        
        assertThat(regionCommand.canHandle(message1)).isTrue();
        assertThat(regionCommand.canHandle(message2)).isTrue();
        assertThat(regionCommand.canHandle(message3)).isFalse();
    }

    @Test
    void shouldReturnRegistrationMessageForUnknownUser() {
        when(userService.findById(1L)).thenReturn(Optional.empty());
        
        UserMessage message = new UserMessage(1L, 100L, "/region", "John", "Doe", "user1", true, "region", null);
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Пожалуйста, сначала используйте /start для регистрации");
    }

    @Test
    void shouldSetValidRegionCode() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 77", "John", "Doe", "user1", true, "region", "77");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).contains("Регион установлен: Москва [77]");
        assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(77);
        verify(userService).save(testUser);
    }

    @Test
    void shouldSetValidRegionCodeForStPetersburg() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 78", "John", "Doe", "user1", true, "region", "78");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).contains("Регион установлен: Санкт-Петербург [78]");
        assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(78);
        verify(userService).save(testUser);
    }

    @Test
    void shouldCreateSearchCriteriaIfNull() {
        testUser.setSearchCriteria(null);
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 77", "John", "Doe", "user1", true, "region", "77");
        regionCommand.handle(message);
        
        assertThat(testUser.getSearchCriteria()).isNotNull();
        assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(77);
        verify(userService).save(testUser);
    }

    @Test
    void shouldRejectInvalidRegionCode() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 999", "John", "Doe", "user1", true, "region", "999");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Неверный код региона: 999");
        verify(userService, never()).save(any());
    }

    @Test
    void shouldHandleInvalidNumberFormat() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region abc", "John", "Doe", "user1", true, "region", "abc");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Неверный формат. Используйте: /region КОД_РЕГИОНА");
        verify(userService, never()).save(any());
    }

    @Test
    void shouldShowRegionListWhenNoRegionSpecified() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region", "John", "Doe", "user1", true, "region", null);
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getChatId()).isEqualTo("100");
        assertThat(result.getText()).isEqualTo("Выберите регион:");
        assertThat(result.getReplyMarkup()).isInstanceOf(InlineKeyboardMarkup.class);
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) result.getReplyMarkup();
        assertThat(keyboard.getKeyboard()).hasSize(11); // 10 regions + navigation
        
        // Check first region button (sorted alphabetically)
        InlineKeyboardRow firstRow = keyboard.getKeyboard().get(0);
        assertThat(firstRow.get(0).getText()).contains("Алтайский край");
        assertThat(firstRow.get(0).getCallbackData()).contains("region_22");
    }

    @Test
    void shouldShowRegionListWithNavigationButtons() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region", "John", "Doe", "user1", true, "region", null);
        SendMessage result = regionCommand.handle(message);
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) result.getReplyMarkup();
        
        // Should have navigation row at the bottom (next page)
        InlineKeyboardRow lastRow = keyboard.getKeyboard().get(keyboard.getKeyboard().size() - 1);
        assertThat(lastRow.get(0).getText()).contains("Следующие 10");
        assertThat(lastRow.get(0).getCallbackData()).contains("region_page_1");
    }

    @Test
    void shouldHandleAllValidRegionCodes() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Test some specific region codes
        int[] validCodes = {1, 22, 77, 78, 91, 92};
        String[] expectedNames = {"Республика Адыгея", "Алтайский край", "Москва", "Санкт-Петербург", "Республика Крым", "Севастополь"};
        
        for (int i = 0; i < validCodes.length; i++) {
            testUser.setSearchCriteria(new SearchCriteria()); // Reset for each test
            UserMessage message = new UserMessage(1L, 100L, "/region " + validCodes[i], "John", "Doe", "user1", true, "region", String.valueOf(validCodes[i]));
            SendMessage result = regionCommand.handle(message);
            
            assertThat(result.getText()).contains("Регион установлен: " + expectedNames[i] + " [" + validCodes[i] + "]");
            assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(validCodes[i]);
        }
    }

    @Test
    void shouldHandleEdgeCaseRegionCodes() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Test edge cases
        int[] edgeCodes = {83, 86, 87, 89}; // Special autonomous regions
        String[] expectedNames = {"Ненецкий автономный округ", "Ханты-Мансийский автономный округ", "Чукотский автономный округ", "Ямало-Ненецкий автономный округ"};
        
        for (int i = 0; i < edgeCodes.length; i++) {
            testUser.setSearchCriteria(new SearchCriteria()); // Reset
            UserMessage message = new UserMessage(1L, 100L, "/region " + edgeCodes[i], "John", "Doe", "user1", true, "region", String.valueOf(edgeCodes[i]));
            SendMessage result = regionCommand.handle(message);
            
            assertThat(result.getText()).contains("Регион установлен: " + expectedNames[i] + " [" + edgeCodes[i] + "]");
        }
    }

    @Test
    void shouldHandleMultipleSpacesInCommand() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region   77", "John", "Doe", "user1", true, "region", "  77");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getText()).contains("Регион установлен: Москва [77]");
        assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(77);
    }

    @Test
    void shouldIgnoreExtraArguments() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 77 extra arguments", "John", "Doe", "user1", true, "region", "77 extra arguments");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getText()).contains("Регион установлен: Москва [77]");
        assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(77);
    }

    @Test
    void shouldHandleNegativeRegionCode() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region -1", "John", "Doe", "user1", true, "region", "-1");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getText()).isEqualTo("Неверный код региона: -1");
        verify(userService, never()).save(any());
    }

    @Test
    void shouldHandleZeroRegionCode() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 0", "John", "Doe", "user1", true, "region", "0");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getText()).isEqualTo("Неверный код региона: 0");
        verify(userService, never()).save(any());
    }

    @Test
    void shouldOverrideExistingRegion() {
        testUser.getSearchCriteria().setRegionCode(78); // Set initial region
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        
        UserMessage message = new UserMessage(1L, 100L, "/region 77", "John", "Doe", "user1", true, "region", "77");
        SendMessage result = regionCommand.handle(message);
        
        assertThat(result.getText()).contains("Регион установлен: Москва [77]");
        assertThat(testUser.getSearchCriteria().getRegionCode()).isEqualTo(77);
        verify(userService).save(testUser);
    }
}