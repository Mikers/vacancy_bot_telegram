package com.skillbox.vacancytracker.task;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.UserVacancy;
import com.skillbox.vacancytracker.model.Vacancy;
import com.skillbox.vacancytracker.repository.UserVacancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationTaskTest {

    @Mock
    private UserVacancyRepository userVacancyRepository;
    
    @Mock
    private TelegramClient telegramClient;
    
    private BotUser testUser;
    private NotificationTask notificationTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new BotUser();
        testUser.setUserId(123L);
        testUser.setChatId(456L);
        testUser.setUsername("testuser");
        testUser.setActive(true);
        
        notificationTask = new NotificationTask(testUser, userVacancyRepository, telegramClient);
    }

    @Test
    void shouldSkipNotificationForInactiveUser() throws TelegramApiException {
        testUser.setActive(false);
        
        notificationTask.run();
        
        verify(userVacancyRepository, never()).findNewByUserId(anyLong());
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldSkipNotificationWhenNoNewVacancies() throws TelegramApiException {
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(Collections.emptyList());
        
        notificationTask.run();
        
        verify(telegramClient, never()).execute(any(SendMessage.class));
        verify(userVacancyRepository, never()).markAsNotified(anyString());
    }

    @Test
    void shouldSendNotificationForSingleVacancy() throws TelegramApiException {
        Vacancy vacancy = createTestVacancy("1", "Java Developer", "TechCorp", 100000, 150000, "RUB", 3, "http://example.com/job/1");
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        verify(userVacancyRepository).markAsNotified(userVacancy.getId());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo("456");
        assertThat(sentMessage.getText()).contains("🔔 Найдены новые вакансии: 1");
        assertThat(sentMessage.getText()).contains("<b>1. Java Developer</b>");
        assertThat(sentMessage.getText()).contains("Компания: TechCorp");
        assertThat(sentMessage.getText()).contains("Зарплата: от 100000 до 150000 RUB");
        assertThat(sentMessage.getText()).contains("Опыт: 3 года");
        assertThat(sentMessage.getText()).contains("<a href=\"http://example.com/job/1\">Подробнее</a>");
        assertThat(sentMessage.getParseMode()).isEqualTo("HTML");
    }

    @Test
    void shouldSendNotificationForMultipleVacancies() throws TelegramApiException {
        Vacancy vacancy1 = createTestVacancy("1", "Java Developer", "TechCorp", 100000, 150000, "RUB", 3, null);
        Vacancy vacancy2 = createTestVacancy("2", "Python Developer", "DataCorp", 80000, null, "USD", 2, "http://example.com/job/2");
        
        UserVacancy userVacancy1 = new UserVacancy(123L, vacancy1);
        UserVacancy userVacancy2 = new UserVacancy(123L, vacancy2);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(Arrays.asList(userVacancy1, userVacancy2));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        verify(userVacancyRepository).markAsNotified(userVacancy1.getId());
        verify(userVacancyRepository).markAsNotified(userVacancy2.getId());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).contains("🔔 Найдены новые вакансии: 2");
        assertThat(sentMessage.getText()).contains("1. Java Developer");
        assertThat(sentMessage.getText()).contains("2. Python Developer");
    }

    @Test
    void shouldCreatePaginationKeyboardForManyVacancies() throws TelegramApiException {
        List<UserVacancy> vacancies = createManyUserVacancies(15);
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(vacancies);
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getReplyMarkup()).isInstanceOf(InlineKeyboardMarkup.class);
        
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) sentMessage.getReplyMarkup();
        assertThat(keyboard.getKeyboard().get(0).get(0).getText()).contains("Следующие 10 →");
        assertThat(keyboard.getKeyboard().get(0).get(0).getCallbackData()).isEqualTo("next_vacancies_1");
    }

    @Test
    void shouldNotCreatePaginationKeyboardForFewVacancies() throws TelegramApiException {
        List<UserVacancy> vacancies = createManyUserVacancies(5);
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(vacancies);
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getReplyMarkup()).isNull();
    }

    @Test
    void shouldFormatVacancyWithMinimalData() throws TelegramApiException {
        Vacancy vacancy = createTestVacancy("1", "Basic Job", null, null, null, null, null, null);
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).contains("<b>1. Basic Job</b>");
        assertThat(sentMessage.getText()).doesNotContain("Компания:");
        assertThat(sentMessage.getText()).doesNotContain("Зарплата:");
        assertThat(sentMessage.getText()).doesNotContain("Опыт:");
        assertThat(sentMessage.getText()).doesNotContain("<a href");
    }

    @Test
    void shouldFormatSalaryFromOnly() throws TelegramApiException {
        Vacancy vacancy = createTestVacancy("1", "Job", "Company", 50000, null, "RUB", null, null);
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).contains("Зарплата: от 50000 RUB");
    }

    @Test
    void shouldFormatSalaryToOnly() throws TelegramApiException {
        Vacancy vacancy = createTestVacancy("1", "Job", "Company", null, 80000, "USD", null, null);
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).contains("Зарплата: до 80000 USD");
    }

    @Test
    void shouldFormatExperienceCorrectly() throws TelegramApiException {
        // Test different experience levels
        testExperienceFormatting(null, null); // null means no experience field shown
        testExperienceFormatting(0, "без опыта");
        testExperienceFormatting(1, "1 год");
        testExperienceFormatting(2, "2 года");
        testExperienceFormatting(3, "3 года");
        testExperienceFormatting(4, "4 года");
        testExperienceFormatting(5, "5 лет");
        testExperienceFormatting(10, "10 лет");
    }

    @Test
    void shouldUseDefaultCurrencyWhenNull() throws TelegramApiException {
        Vacancy vacancy = createTestVacancy("1", "Job", "Company", 50000, 80000, null, null, null);
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).contains("Зарплата: от 50000 до 80000 руб.");
    }

    @Test
    void shouldHandleExceptionDuringNotification() throws TelegramApiException {
        Vacancy vacancy = createTestVacancy("1", "Job", "Company", null, null, null, null, null);
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        doThrow(new TelegramApiException("Network error")).when(telegramClient).execute(any(SendMessage.class));
        
        // Should not throw exception
        notificationTask.run();
        
        // Should still try to send but not mark as notified due to exception
        verify(telegramClient).execute(any(SendMessage.class));
        verify(userVacancyRepository, never()).markAsNotified(anyString());
    }

    @Test
    void shouldHandleRepositoryException() throws TelegramApiException {
        when(userVacancyRepository.findNewByUserId(123L)).thenThrow(new RuntimeException("Database error"));
        
        // Should not throw exception
        notificationTask.run();
        
        verify(telegramClient, never()).execute(any(SendMessage.class));
        verify(userVacancyRepository, never()).markAsNotified(anyString());
    }

    @Test
    void shouldLimitVacanciesToTenInMessage() throws TelegramApiException {
        List<UserVacancy> vacancies = createManyUserVacancies(15);
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(vacancies);
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        // Should show total count as 15
        assertThat(sentMessage.getText()).contains("🔔 Найдены новые вакансии: 15");
        
        // But only display first 10
        for (int i = 1; i <= 10; i++) {
            assertThat(sentMessage.getText()).contains(i + ". Test Job " + i);
        }
        assertThat(sentMessage.getText()).doesNotContain("11. Test Job 11");
    }

    private void testExperienceFormatting(Integer experience, String expected) throws TelegramApiException {
        reset(telegramClient, userVacancyRepository);
        
        Vacancy vacancy = createTestVacancy("1", "Job", "Company", null, null, null, experience, null);
        UserVacancy userVacancy = new UserVacancy(123L, vacancy);
        
        when(userVacancyRepository.findNewByUserId(123L)).thenReturn(List.of(userVacancy));
        
        notificationTask.run();
        
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());
        
        SendMessage sentMessage = messageCaptor.getValue();
        if (expected == null) {
            // When experience is null, no experience section should be shown
            assertThat(sentMessage.getText()).doesNotContain("Опыт:");
        } else {
            assertThat(sentMessage.getText()).contains("Опыт: " + expected);
        }
    }

    private Vacancy createTestVacancy(String id, String title, String company, Integer salaryFrom, Integer salaryTo, String currency, Integer experience, String url) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(id);
        vacancy.setTitle(title);
        vacancy.setCompanyName(company);
        vacancy.setSalaryFrom(salaryFrom);
        vacancy.setSalaryTo(salaryTo);
        vacancy.setCurrency(currency);
        vacancy.setExperienceRequired(experience);
        vacancy.setUrl(url);
        vacancy.setCreatedDate(LocalDateTime.now());
        return vacancy;
    }

    private List<UserVacancy> createManyUserVacancies(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
            .mapToObj(i -> {
                Vacancy vacancy = createTestVacancy(String.valueOf(i), "Test Job " + i, "Company " + i, 
                    50000 + i * 1000, 80000 + i * 1000, "RUB", i % 6, "http://example.com/job/" + i);
                return new UserVacancy(123L, vacancy);
            })
            .toList();
    }
}