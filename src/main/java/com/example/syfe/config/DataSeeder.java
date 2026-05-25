package com.example.syfe.config;

import com.example.syfe.enums.TransactionType;
import com.example.syfe.models.Category;
import com.example.syfe.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    // Immutable Map - Map.of()
    private static final Map<TransactionType, List<String>> DEFAULT_CATEGORIES=Map.of(
            TransactionType.INCOME, List.of("Salary"),
            TransactionType.EXPENSE, List.of("Food","Rent","Transportation","Entertainment","Healthcare","Utilities")
    );

    @Override
    @Transactional
    public void run(String... args) {
        int seededCount = 0;

        for(Map.Entry<TransactionType,List<String>>entry : DEFAULT_CATEGORIES.entrySet()){
            TransactionType type=entry.getKey();

            for(String name:entry.getValue()){
                if(categoryRepository.existsByNameAndTypeAndIsDefaultTrue(name,type)){
                    continue;
                }

                Category category=Category.builder()
                                    .name(name)
                                    .type(type)
                                    .isDefault(true)
                                    .user(null)
                                    .build();

                categoryRepository.save(category);
                seededCount++;
            }
        }

        if(seededCount>0) log.info("Seeded {} default categories", seededCount);
        else log.info("Default categories already exist-skipping seed");
    }
}
