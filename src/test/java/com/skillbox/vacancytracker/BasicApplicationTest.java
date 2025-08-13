package com.skillbox.vacancytracker;

import com.skillbox.vacancytracker.model.BotUser;
import com.skillbox.vacancytracker.model.SearchCriteria;
import com.skillbox.vacancytracker.model.Vacancy;
import com.skillbox.vacancytracker.model.UserVacancy;
import com.skillbox.vacancytracker.repository.*;
import com.skillbox.vacancytracker.service.*;
import com.skillbox.vacancytracker.util.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BasicApplicationTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void shouldCreateAndFindUser() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        UserService service = new UserServiceImpl(repository);
        
        BotUser user = new BotUser();
        user.setUserId(123L);
        user.setUsername("testuser");
        user.setChatId(456L);
        user.setActive(true);
        
        service.save(user);
        
        Optional<BotUser> found = service.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void shouldCreateAndFindVacancy() {
        VacancyRepository repository = new JsonVacancyRepository(tempDir.toString());
        
        Vacancy vacancy = new Vacancy();
        vacancy.setId("VAC-123");
        vacancy.setTitle("Java Developer");
        vacancy.setCompanyName("Tech Corp");
        vacancy.setSalaryFrom(150000);
        
        repository.save(vacancy);
        
        Optional<Vacancy> found = repository.findById("VAC-123");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Java Developer");
    }
    
    @Test
    void shouldCreateAndFindUserVacancy() {
        UserVacancyRepository repository = new JsonUserVacancyRepository(tempDir.toString());
        
        UserVacancy uv = new UserVacancy();
        uv.setId(UUID.randomUUID().toString());
        uv.setUserId(123L);
        
        Vacancy vacancy = new Vacancy();
        vacancy.setId("VAC-456");
        vacancy.setTitle("Python Developer");
        uv.setVacancy(vacancy);
        uv.setFoundAt(LocalDateTime.now());
        uv.setNew(true);
        
        repository.save(uv);
        
        assertThat(repository.findByUserId(123L)).hasSize(1);
        assertThat(repository.findNewByUserId(123L)).hasSize(1);
    }
    
    @Test
    void shouldUpdateUserSearchCriteria() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        UserService service = new UserServiceImpl(repository);
        
        BotUser user = new BotUser();
        user.setUserId(789L);
        service.save(user);
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("developer");
        criteria.setMinimumSalary(100000);
        
        service.updateSearchCriteria(789L, criteria);
        
        Optional<BotUser> updated = service.findById(789L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getSearchCriteria()).isNotNull();
        assertThat(updated.get().getSearchCriteria().getKeyword()).isEqualTo("developer");
    }
    
    @Test
    void shouldProvideJsonMapper() {
        assertThat(JsonMapper.getInstance()).isNotNull();
    }
    
    @Test
    void shouldCreateScheduledTaskManager() {
        ScheduledTaskManager manager = new ScheduledTaskManager();
        
        manager.scheduleVacancyCheck(123L, () -> {});
        
        manager.cancelUserTasks(123L);
        manager.shutdown();
    }
    
    @Test
    void shouldHandleSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("java");
        criteria.setMinimumSalary(120000);
        criteria.setMinimumExperience(3);
        criteria.setRegionCode(77);
        
        assertThat(criteria.getKeyword()).isEqualTo("java");
        assertThat(criteria.getMinimumSalary()).isEqualTo(120000);
        assertThat(criteria.getMinimumExperience()).isEqualTo(3);
        assertThat(criteria.getRegionCode()).isEqualTo(77);
    }
    
    @Test
    void shouldDeactivateUser() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        UserService service = new UserServiceImpl(repository);
        
        BotUser user = new BotUser();
        user.setUserId(999L);
        user.setActive(true);
        service.save(user);
        
        service.deactivateUser(999L);
        
        Optional<BotUser> deactivated = service.findById(999L);
        assertThat(deactivated).isPresent();
        assertThat(deactivated.get().isActive()).isFalse();
    }
    
    @Test
    void shouldFindAllActiveUsers() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        UserService service = new UserServiceImpl(repository);
        
        BotUser activeUser = new BotUser();
        activeUser.setUserId(1L);
        activeUser.setActive(true);
        service.save(activeUser);
        
        BotUser inactiveUser = new BotUser();
        inactiveUser.setUserId(2L);
        inactiveUser.setActive(false);
        service.save(inactiveUser);
        
        assertThat(service.findAllActive()).hasSize(1);
    }
    
    @Test
    void shouldUpdateTimezone() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        UserService service = new UserServiceImpl(repository);
        
        BotUser user = new BotUser();
        user.setUserId(100L);
        service.save(user);
        
        service.updateTimezone(100L, ZoneOffset.ofHours(3));
        
        Optional<BotUser> updated = service.findById(100L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
    }
    
    @Test
    void shouldUpdateNotificationTime() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        UserService service = new UserServiceImpl(repository);
        
        BotUser user = new BotUser();
        user.setUserId(200L);
        service.save(user);
        
        service.updateNotificationTime(200L, "10:30");
        
        Optional<BotUser> updated = service.findById(200L);
        assertThat(updated).isPresent();
        assertThat(updated.get().getNotificationTime()).isEqualTo("10:30");
    }
    
    @Test
    void shouldHandleRepositoryExists() {
        UserRepository repository = new JsonUserRepository(tempDir.toString());
        
        BotUser user = new BotUser();
        user.setUserId(300L);
        repository.save(user);
        
        assertThat(repository.exists(300L)).isTrue();
        assertThat(repository.exists(999L)).isFalse();
    }
    
    @Test
    void shouldDeleteFromRepository() {
        VacancyRepository repository = new JsonVacancyRepository(tempDir.toString());
        
        Vacancy vacancy = new Vacancy();
        vacancy.setId("DELETE-ME");
        vacancy.setTitle("Test Vacancy");
        repository.save(vacancy);
        
        assertThat(repository.exists("DELETE-ME")).isTrue();
        repository.delete("DELETE-ME");
        assertThat(repository.exists("DELETE-ME")).isFalse();
    }
    
    @Test
    void shouldMarkUserVacancyAsNotified() {
        UserVacancyRepository repository = new JsonUserVacancyRepository(tempDir.toString());
        
        UserVacancy uv = new UserVacancy();
        String id = UUID.randomUUID().toString();
        uv.setId(id);
        uv.setUserId(500L);
        uv.setNew(true);
        
        Vacancy vacancy = new Vacancy();
        vacancy.setId("TEST");
        vacancy.setTitle("Test Job");
        uv.setVacancy(vacancy);
        uv.setFoundAt(LocalDateTime.now());
        
        repository.save(uv);
        
        assertThat(repository.findNewByUserId(500L)).hasSize(1);
        
        repository.markAsNotified(id);
        
        assertThat(repository.findNewByUserId(500L)).hasSize(0);
    }
    
    @Test
    void shouldDeleteUserVacanciesByUserId() {
        UserVacancyRepository repository = new JsonUserVacancyRepository(tempDir.toString());
        
        UserVacancy uv1 = createTestUserVacancy(600L, "VAC1");
        UserVacancy uv2 = createTestUserVacancy(600L, "VAC2");
        UserVacancy uv3 = createTestUserVacancy(700L, "VAC3");
        
        repository.save(uv1);
        repository.save(uv2);
        repository.save(uv3);
        
        assertThat(repository.findByUserId(600L)).hasSize(2);
        
        repository.deleteByUserId(600L);
        
        assertThat(repository.findByUserId(600L)).hasSize(0);
        assertThat(repository.findByUserId(700L)).hasSize(1);
    }
    
    @Test
    void shouldHandleVacancyRepository() {
        VacancyRepository repository = new JsonVacancyRepository(tempDir.toString());
        
        Vacancy v1 = new Vacancy();
        v1.setId("V1");
        v1.setTitle("Job 1");
        v1.setSalaryFrom(100000);
        v1.setSalaryTo(150000);
        v1.setCurrency("RUB");
        v1.setExperienceRequired(2);
        v1.setDescription("Test job description");
        
        repository.save(v1);
        
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findById("V1")).isPresent();
        
        v1.setSalaryFrom(120000);
        repository.save(v1);
        
        Optional<Vacancy> updated = repository.findById("V1");
        assertThat(updated).isPresent();
        assertThat(updated.get().getSalaryFrom()).isEqualTo(120000);
    }
    
    @Test
    void shouldTestVacancyModel() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId("TEST-ID");
        vacancy.setTitle("Test Title");
        vacancy.setCompanyName("Test Company");
        vacancy.setSalaryFrom(50000);
        vacancy.setSalaryTo(100000);
        vacancy.setCurrency("USD");
        vacancy.setExperienceRequired(5);
        vacancy.setRegion("Test Region");
        vacancy.setRegionCode(99);
        vacancy.setDescription("Test Description");
        vacancy.setUrl("https://test.com");
        
        assertThat(vacancy.getId()).isEqualTo("TEST-ID");
        assertThat(vacancy.getTitle()).isEqualTo("Test Title");
        assertThat(vacancy.getCompanyName()).isEqualTo("Test Company");
        assertThat(vacancy.getSalaryFrom()).isEqualTo(50000);
        assertThat(vacancy.getSalaryTo()).isEqualTo(100000);
        assertThat(vacancy.getCurrency()).isEqualTo("USD");
        assertThat(vacancy.getExperienceRequired()).isEqualTo(5);
        assertThat(vacancy.getRegion()).isEqualTo("Test Region");
        assertThat(vacancy.getRegionCode()).isEqualTo(99);
        assertThat(vacancy.getDescription()).isEqualTo("Test Description");
        assertThat(vacancy.getUrl()).isEqualTo("https://test.com");
    }
    
    @Test
    void shouldTestBotUserModel() {
        BotUser user = new BotUser();
        user.setUserId(123L);
        user.setChatId(456L);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        user.setNotificationTime("09:00");
        user.setTimezoneOffset(ZoneOffset.ofHours(3));
        
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword("java");
        user.setSearchCriteria(criteria);
        
        assertThat(user.getUserId()).isEqualTo(123L);
        assertThat(user.getChatId()).isEqualTo(456L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getNotificationTime()).isEqualTo("09:00");
        assertThat(user.getTimezoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(user.getSearchCriteria()).isNotNull();
    }
    
    @Test
    void shouldTestUserVacancyModel() {
        UserVacancy uv = createTestUserVacancy(777L, "TEST-VAC");
        
        assertThat(uv.getUserId()).isEqualTo(777L);
        assertThat(uv.getVacancy().getId()).isEqualTo("TEST-VAC");
        assertThat(uv.isNew()).isTrue();
        assertThat(uv.getFoundAt()).isNotNull();
    }
    
    private UserVacancy createTestUserVacancy(Long userId, String vacancyId) {
        UserVacancy uv = new UserVacancy();
        uv.setId(UUID.randomUUID().toString());
        uv.setUserId(userId);
        uv.setNew(true);
        uv.setFoundAt(LocalDateTime.now());
        
        Vacancy vacancy = new Vacancy();
        vacancy.setId(vacancyId);
        vacancy.setTitle("Test Vacancy " + vacancyId);
        vacancy.setCompanyName("Test Company");
        
        uv.setVacancy(vacancy);
        return uv;
    }
}