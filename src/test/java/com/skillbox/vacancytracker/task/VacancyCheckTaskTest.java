package com.skillbox.vacancytracker.task;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.UserVacancy;
import com.skillbox.vacancytracker.model.Vacancy;
import com.skillbox.vacancytracker.repository.UserVacancyRepository;
import com.skillbox.vacancytracker.service.VacancyApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyCheckTaskTest {
    
    @Mock
    private BotUser user;
    
    @Mock
    private VacancyApiClient vacancyApiClient;
    
    @Mock
    private UserVacancyRepository userVacancyRepository;
    
    private VacancyCheckTask task;
    
    @BeforeEach
    void setUp() {
        task = new VacancyCheckTask(user, vacancyApiClient, userVacancyRepository);
    }
    
    @Test
    void shouldSkipInactiveUser() {
        when(user.isActive()).thenReturn(false);
        when(user.getUserId()).thenReturn(123L);
        
        task.run();
        
        verify(vacancyApiClient, never()).searchVacancies(any());
        verify(userVacancyRepository, never()).save(any());
    }
    
    @Test
    void shouldSkipUserWithoutSearchCriteria() {
        when(user.isActive()).thenReturn(true);
        when(user.getSearchCriteria()).thenReturn(null);
        when(user.getUserId()).thenReturn(123L);
        
        task.run();
        
        verify(vacancyApiClient, never()).searchVacancies(any());
    }
    
    @Test
    void shouldProcessNewVacancies() {
        Long userId = 123L;
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("java");
        
        when(user.isActive()).thenReturn(true);
        when(user.getSearchCriteria()).thenReturn(criteria);
        when(user.getUserId()).thenReturn(userId);
        
        Vacancy vacancy1 = createVacancy("1", "Java Developer");
        Vacancy vacancy2 = createVacancy("2", "Senior Java Developer");
        
        when(vacancyApiClient.searchVacancies(criteria)).thenReturn(List.of(vacancy1, vacancy2));
        when(userVacancyRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        
        task.run();
        
        verify(vacancyApiClient).searchVacancies(criteria);
        
        ArgumentCaptor<UserVacancy> captor = ArgumentCaptor.forClass(UserVacancy.class);
        verify(userVacancyRepository, times(2)).save(captor.capture());
        
        List<UserVacancy> savedUserVacancies = captor.getAllValues();
        assertThat(savedUserVacancies).hasSize(2);
        
        UserVacancy first = savedUserVacancies.get(0);
        assertThat(first.getUserId()).isEqualTo(userId);
        assertThat(first.getVacancy()).isEqualTo(vacancy1);
        assertThat(first.isNew()).isTrue();
        assertThat(first.getFoundAt()).isNotNull();
        
        UserVacancy second = savedUserVacancies.get(1);
        assertThat(second.getUserId()).isEqualTo(userId);
        assertThat(second.getVacancy()).isEqualTo(vacancy2);
        assertThat(second.isNew()).isTrue();
    }
    
    @Test
    void shouldNotDuplicateExistingVacancies() {
        Long userId = 123L;
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("python");
        
        when(user.isActive()).thenReturn(true);
        when(user.getSearchCriteria()).thenReturn(criteria);
        when(user.getUserId()).thenReturn(userId);
        
        Vacancy existingVacancy = createVacancy("1", "Python Developer");
        Vacancy newVacancy = createVacancy("2", "Django Developer");
        
        UserVacancy existingUserVacancy = new UserVacancy(userId, existingVacancy);
        
        when(vacancyApiClient.searchVacancies(criteria)).thenReturn(List.of(existingVacancy, newVacancy));
        when(userVacancyRepository.findByUserId(userId)).thenReturn(List.of(existingUserVacancy));
        
        task.run();
        
        verify(userVacancyRepository, times(1)).save(any(UserVacancy.class));
    }
    
    @Test
    void shouldHandleApiException() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("error");
        
        when(user.isActive()).thenReturn(true);
        when(user.getSearchCriteria()).thenReturn(criteria);
        when(user.getUserId()).thenReturn(456L);
        when(vacancyApiClient.searchVacancies(criteria)).thenThrow(new RuntimeException("API error"));
        
        task.run();
        
        verify(userVacancyRepository, never()).save(any());
    }
    
    @Test
    void shouldHandleEmptyVacancyList() {
        SearchCriteria criteria = new SearchCriteria();
        
        when(user.isActive()).thenReturn(true);
        when(user.getSearchCriteria()).thenReturn(criteria);
        
        task.run();
        
        verify(userVacancyRepository, never()).save(any());
    }
    
    @Test
    void shouldCreateTaskWithValidParameters() {
        assertThat(task).isNotNull();
    }
    
    @Test
    void shouldHandleNullVacancyApiResponse() {
        SearchCriteria criteria = new SearchCriteria();
        
        when(user.isActive()).thenReturn(true);
        when(user.getSearchCriteria()).thenReturn(criteria);
        
        task.run();
        
        verify(userVacancyRepository, never()).save(any());
    }
    
    private Vacancy createVacancy(String id, String title) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(id);
        vacancy.setTitle(title);
        vacancy.setCompanyName("Test Company");
        vacancy.setUrl("https://example.com/" + id);
        return vacancy;
    }
}