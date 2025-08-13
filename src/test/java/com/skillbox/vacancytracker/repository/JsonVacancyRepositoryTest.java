package com.skillbox.vacancytracker.repository;

import com.skillbox.vacancytracker.model.Vacancy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JsonVacancyRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonVacancyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonVacancyRepository(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir.resolve("vacancies.json"))) {
            Files.delete(tempDir.resolve("vacancies.json"));
        }
    }

    @Test
    void shouldCreateRepositoryWithVacanciesJsonFile() {
        assertThat(tempDir.resolve("vacancies.json")).exists();
    }

    @Test
    void shouldSaveAndRetrieveVacancy() {
        Vacancy vacancy = createTestVacancy("test-vacancy-1", "Java Developer");
        
        repository.save(vacancy);
        
        Optional<Vacancy> found = repository.findById("test-vacancy-1");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("test-vacancy-1");
        assertThat(found.get().getTitle()).isEqualTo("Java Developer");
    }

    @Test
    void shouldFindVacanciesByUserId() {
        Long userId = 123L;
        
        // Create vacancies with user-specific composite IDs
        Vacancy vacancy1 = createTestVacancy(userId + "_vacancy1", "Java Developer");
        Vacancy vacancy2 = createTestVacancy(userId + "_vacancy2", "Python Developer");
        Vacancy vacancy3 = createTestVacancy("456_vacancy3", "C++ Developer"); // Different user
        
        repository.save(vacancy1);
        repository.save(vacancy2);
        repository.save(vacancy3);
        
        List<Vacancy> userVacancies = repository.findByUserId(userId);
        
        assertThat(userVacancies).hasSize(2);
        assertThat(userVacancies).extracting(Vacancy::getTitle)
            .containsExactlyInAnyOrder("Java Developer", "Python Developer");
    }

    @Test
    void shouldReturnEmptyListWhenNoVacanciesForUser() {
        List<Vacancy> userVacancies = repository.findByUserId(999L);
        assertThat(userVacancies).isEmpty();
    }

    @Test
    void shouldDeleteVacanciesByUserId() {
        Long userId = 123L;
        
        Vacancy vacancy1 = createTestVacancy(userId + "_vacancy1", "Java Developer");
        Vacancy vacancy2 = createTestVacancy(userId + "_vacancy2", "Python Developer");
        Vacancy vacancy3 = createTestVacancy("456_vacancy3", "C++ Developer");
        
        repository.save(vacancy1);
        repository.save(vacancy2);
        repository.save(vacancy3);
        
        repository.deleteByUserId(userId);
        
        assertThat(repository.findByUserId(userId)).isEmpty();
        assertThat(repository.findByUserId(456L)).hasSize(1);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void shouldFindVacanciesByUserIdAndCreatedAfter() {
        Long userId = 123L;
        LocalDateTime cutoffDate = LocalDateTime.of(2023, 6, 15, 12, 0);
        
        Vacancy oldVacancy = createTestVacancy(userId + "_old", "Old Vacancy");
        oldVacancy.setCreatedDate(LocalDateTime.of(2023, 6, 10, 10, 0));
        
        Vacancy newVacancy1 = createTestVacancy(userId + "_new1", "New Vacancy 1");
        newVacancy1.setCreatedDate(LocalDateTime.of(2023, 6, 20, 14, 0));
        
        Vacancy newVacancy2 = createTestVacancy(userId + "_new2", "New Vacancy 2");
        newVacancy2.setCreatedDate(LocalDateTime.of(2023, 6, 25, 16, 0));
        
        repository.save(oldVacancy);
        repository.save(newVacancy1);
        repository.save(newVacancy2);
        
        List<Vacancy> recentVacancies = repository.findByUserIdAndCreatedAfter(userId, cutoffDate);
        
        assertThat(recentVacancies).hasSize(2);
        assertThat(recentVacancies).extracting(Vacancy::getTitle)
            .containsExactlyInAnyOrder("New Vacancy 1", "New Vacancy 2");
    }

    @Test
    void shouldHandleVacanciesWithNullCreatedDate() {
        Long userId = 123L;
        LocalDateTime cutoffDate = LocalDateTime.of(2023, 6, 15, 12, 0);
        
        Vacancy vacancyWithoutDate = createTestVacancy(userId + "_no_date", "No Date Vacancy");
        vacancyWithoutDate.setCreatedDate(null);
        
        Vacancy vacancyWithDate = createTestVacancy(userId + "_with_date", "With Date Vacancy");
        vacancyWithDate.setCreatedDate(LocalDateTime.of(2023, 6, 20, 14, 0));
        
        repository.save(vacancyWithoutDate);
        repository.save(vacancyWithDate);
        
        List<Vacancy> recentVacancies = repository.findByUserIdAndCreatedAfter(userId, cutoffDate);
        
        assertThat(recentVacancies).hasSize(1);
        assertThat(recentVacancies.get(0).getTitle()).isEqualTo("With Date Vacancy");
    }

    @Test
    void shouldSaveUserVacancies() {
        Long userId = 123L;
        
        Vacancy vacancy1 = createTestVacancy("original1", "Java Developer");
        Vacancy vacancy2 = createTestVacancy("original2", "Python Developer");
        Vacancy vacancy3 = createTestVacancy(null, "No ID Vacancy");
        
        List<Vacancy> vacancies = Arrays.asList(vacancy1, vacancy2, vacancy3);
        
        repository.saveUserVacancies(userId, vacancies);
        
        List<Vacancy> savedVacancies = repository.findByUserId(userId);
        assertThat(savedVacancies).hasSize(3);
        
        // Check that composite IDs were created (null ID vacancy gets index 2)
        assertThat(savedVacancies).extracting(Vacancy::getId)
            .containsExactlyInAnyOrder("123_original1", "123_original2", "123_2");
    }

    @Test
    void shouldReplaceExistingVacanciesWhenSavingUserVacancies() {
        Long userId = 123L;
        
        // Save initial vacancies
        Vacancy initialVacancy = createTestVacancy(userId + "_initial", "Initial Vacancy");
        repository.save(initialVacancy);
        
        // Save new set of vacancies
        Vacancy newVacancy1 = createTestVacancy("new1", "New Vacancy 1");
        Vacancy newVacancy2 = createTestVacancy("new2", "New Vacancy 2");
        
        repository.saveUserVacancies(userId, Arrays.asList(newVacancy1, newVacancy2));
        
        List<Vacancy> userVacancies = repository.findByUserId(userId);
        assertThat(userVacancies).hasSize(2);
        assertThat(userVacancies).extracting(Vacancy::getTitle)
            .containsExactlyInAnyOrder("New Vacancy 1", "New Vacancy 2");
        
        // Initial vacancy should be deleted
        assertThat(repository.findById(userId + "_initial")).isEmpty();
    }

    @Test
    void shouldHandleEmptyVacanciesList() {
        Long userId = 123L;
        
        // Save initial vacancy
        Vacancy initialVacancy = createTestVacancy(userId + "_initial", "Initial Vacancy");
        repository.save(initialVacancy);
        
        // Save empty list
        repository.saveUserVacancies(userId, List.of());
        
        List<Vacancy> userVacancies = repository.findByUserId(userId);
        assertThat(userVacancies).isEmpty();
    }

    @Test
    void shouldHandleComplexVacancyData() {
        Vacancy vacancy = new Vacancy();
        vacancy.setId("complex-vacancy");
        vacancy.setTitle("Senior Java Backend Developer");
        vacancy.setCompanyName("TechCorp Ltd.");
        vacancy.setDescription("Exciting opportunity for a Senior Java Developer...");
        vacancy.setSalaryFrom(150000);
        vacancy.setSalaryTo(250000);
        vacancy.setCurrency("RUB");
        vacancy.setRegion("Москва");
        vacancy.setUrl("https://example.com/vacancy/123");
        vacancy.setCreatedDate(LocalDateTime.of(2023, 6, 15, 10, 30));
        
        repository.save(vacancy);
        
        Optional<Vacancy> found = repository.findById("complex-vacancy");
        assertThat(found).isPresent();
        
        Vacancy savedVacancy = found.get();
        assertThat(savedVacancy.getTitle()).isEqualTo("Senior Java Backend Developer");
        assertThat(savedVacancy.getCompanyName()).isEqualTo("TechCorp Ltd.");
        assertThat(savedVacancy.getDescription()).isEqualTo("Exciting opportunity for a Senior Java Developer...");
        assertThat(savedVacancy.getSalaryFrom()).isEqualTo(150000);
        assertThat(savedVacancy.getSalaryTo()).isEqualTo(250000);
        assertThat(savedVacancy.getCurrency()).isEqualTo("RUB");
        assertThat(savedVacancy.getRegion()).isEqualTo("Москва");
        assertThat(savedVacancy.getUrl()).isEqualTo("https://example.com/vacancy/123");
        assertThat(savedVacancy.getCreatedDate()).isEqualTo(LocalDateTime.of(2023, 6, 15, 10, 30));
    }

    @Test
    void shouldHandleVacanciesFromMultipleUsers() {
        Long user1 = 111L;
        Long user2 = 222L;
        Long user3 = 333L;
        
        List<Vacancy> user1Vacancies = Arrays.asList(
            createTestVacancy("java1", "Java Developer 1"),
            createTestVacancy("java2", "Java Developer 2")
        );
        
        List<Vacancy> user2Vacancies = Arrays.asList(
            createTestVacancy("python1", "Python Developer 1"),
            createTestVacancy("python2", "Python Developer 2"),
            createTestVacancy("python3", "Python Developer 3")
        );
        
        List<Vacancy> user3Vacancies = Arrays.asList(
            createTestVacancy("cpp1", "C++ Developer")
        );
        
        repository.saveUserVacancies(user1, user1Vacancies);
        repository.saveUserVacancies(user2, user2Vacancies);
        repository.saveUserVacancies(user3, user3Vacancies);
        
        assertThat(repository.findByUserId(user1)).hasSize(2);
        assertThat(repository.findByUserId(user2)).hasSize(3);
        assertThat(repository.findByUserId(user3)).hasSize(1);
        assertThat(repository.findAll()).hasSize(6);
    }

    @Test
    void shouldMaintainUserIsolationWhenDeletingVacancies() {
        Long user1 = 111L;
        Long user2 = 222L;
        
        repository.saveUserVacancies(user1, Arrays.asList(
            createTestVacancy("java1", "Java 1"),
            createTestVacancy("java2", "Java 2")
        ));
        
        repository.saveUserVacancies(user2, Arrays.asList(
            createTestVacancy("python1", "Python 1"),
            createTestVacancy("python2", "Python 2")
        ));
        
        // Delete vacancies for user1 only
        repository.deleteByUserId(user1);
        
        assertThat(repository.findByUserId(user1)).isEmpty();
        assertThat(repository.findByUserId(user2)).hasSize(2);
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void shouldHandleSpecialCharactersInVacancyData() {
        Vacancy vacancy = createTestVacancy("special-vacancy", "C++/C# .NET Developer");
        vacancy.setCompanyName("ООО \"TechnoSoft\"");
        vacancy.setDescription("Разработка на C++/C# с использованием .NET Framework");
        vacancy.setRegion("Санкт-Петербург, м. Невский проспект");
        vacancy.setCurrency("₽");
        
        repository.save(vacancy);
        
        Optional<Vacancy> found = repository.findById("special-vacancy");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("C++/C# .NET Developer");
        assertThat(found.get().getCompanyName()).isEqualTo("ООО \"TechnoSoft\"");
        assertThat(found.get().getDescription()).isEqualTo("Разработка на C++/C# с использованием .NET Framework");
        assertThat(found.get().getRegion()).isEqualTo("Санкт-Петербург, м. Невский проспект");
        assertThat(found.get().getCurrency()).isEqualTo("₽");
    }

    @Test
    void shouldPersistDataBetweenRepositoryInstances() {
        Long userId = 123L;
        
        repository.saveUserVacancies(userId, Arrays.asList(
            createTestVacancy("persistent1", "Persistent Vacancy 1"),
            createTestVacancy("persistent2", "Persistent Vacancy 2")
        ));
        
        JsonVacancyRepository newRepository = new JsonVacancyRepository(tempDir.toString());
        
        List<Vacancy> vacancies = newRepository.findByUserId(userId);
        assertThat(vacancies).hasSize(2);
        assertThat(vacancies).extracting(Vacancy::getTitle)
            .containsExactlyInAnyOrder("Persistent Vacancy 1", "Persistent Vacancy 2");
    }

    @Test
    void shouldHandleVacanciesWithSameTitleButDifferentUsers() {
        Long user1 = 111L;
        Long user2 = 222L;
        String sameTitle = "Java Developer";
        
        repository.saveUserVacancies(user1, Arrays.asList(
            createTestVacancy("java-user1", sameTitle)
        ));
        
        repository.saveUserVacancies(user2, Arrays.asList(
            createTestVacancy("java-user2", sameTitle)
        ));
        
        assertThat(repository.findByUserId(user1)).hasSize(1);
        assertThat(repository.findByUserId(user2)).hasSize(1);
        
        Vacancy user1Vacancy = repository.findByUserId(user1).get(0);
        Vacancy user2Vacancy = repository.findByUserId(user2).get(0);
        
        assertThat(user1Vacancy.getId()).isEqualTo("111_java-user1");
        assertThat(user2Vacancy.getId()).isEqualTo("222_java-user2");
        assertThat(user1Vacancy.getTitle()).isEqualTo(sameTitle);
        assertThat(user2Vacancy.getTitle()).isEqualTo(sameTitle);
    }

    @Test
    void shouldHandleNullSalaryValues() {
        Vacancy vacancy = createTestVacancy("null-salary", "Developer");
        vacancy.setSalaryFrom(null);
        vacancy.setSalaryTo(null);
        vacancy.setCurrency(null);
        
        repository.save(vacancy);
        
        Optional<Vacancy> found = repository.findById("null-salary");
        assertThat(found).isPresent();
        assertThat(found.get().getSalaryFrom()).isNull();
        assertThat(found.get().getSalaryTo()).isNull();
        assertThat(found.get().getCurrency()).isNull();
    }

    @Test
    void shouldHandleEdgeCasesInCreatedDateFiltering() {
        Long userId = 123L;
        LocalDateTime exactCutoff = LocalDateTime.of(2023, 6, 15, 12, 0, 0);
        
        Vacancy exactMatch = createTestVacancy(userId + "_exact", "Exact Match");
        exactMatch.setCreatedDate(exactCutoff);
        
        Vacancy oneMsAfter = createTestVacancy(userId + "_after", "One MS After");
        oneMsAfter.setCreatedDate(exactCutoff.plusNanos(1000000));
        
        Vacancy oneMsBefore = createTestVacancy(userId + "_before", "One MS Before");
        oneMsBefore.setCreatedDate(exactCutoff.minusNanos(1000000));
        
        repository.save(exactMatch);
        repository.save(oneMsAfter);
        repository.save(oneMsBefore);
        
        List<Vacancy> afterCutoff = repository.findByUserIdAndCreatedAfter(userId, exactCutoff);
        
        // Should include only items strictly after cutoff (LocalDateTime.isAfter is exclusive)
        assertThat(afterCutoff).hasSize(1);
        assertThat(afterCutoff).extracting(Vacancy::getTitle)
            .containsExactly("One MS After");
    }

    @Test
    void shouldHandleLargeNumberOfVacancies() {
        Long userId = 123L;
        
        // Create 100 vacancies
        for (int i = 0; i < 100; i++) {
            Vacancy vacancy = createTestVacancy("vacancy" + i, "Developer " + i);
            vacancy.setCreatedDate(LocalDateTime.of(2023, 6, 15 + (i / 10), 10, i % 60));
            vacancy.setSalaryFrom(50000 + i * 1000);
            repository.save(vacancy);
        }
        
        // Save them all as user vacancies (should replace with composite IDs)
        List<Vacancy> allVacancies = repository.findAll();
        repository.saveUserVacancies(userId, allVacancies);
        
        List<Vacancy> userVacancies = repository.findByUserId(userId);
        assertThat(userVacancies).hasSize(100);
        
        // Test filtering by date
        LocalDateTime cutoff = LocalDateTime.of(2023, 6, 20, 0, 0);
        List<Vacancy> recentVacancies = repository.findByUserIdAndCreatedAfter(userId, cutoff);
        assertThat(recentVacancies.size()).isGreaterThan(0);
        assertThat(recentVacancies.size()).isLessThan(100);
    }

    private Vacancy createTestVacancy(String id, String title) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(id);
        vacancy.setTitle(title);
        return vacancy;
    }
}