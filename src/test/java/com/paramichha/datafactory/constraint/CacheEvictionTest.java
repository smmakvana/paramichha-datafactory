package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.instantiation.LombokObjectCreation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The hot-reload helpers clear the caches, and generation keeps working afterwards. */
class CacheEvictionTest {

    record Item(@NotBlank String name) {}

    @Test
    void evictingTheCachesKeepsGenerationWorking() {
        assertThat(FieldDescriptor.extract(Item.class)).hasSize(1);
        FieldDescriptor.evict(Item.class);
        assertThat(FieldDescriptor.extract(Item.class)).hasSize(1);
        FieldDescriptor.evictAll();
        LombokObjectCreation.evictAll();
        assertThat(DataFactory.valid(Item.class).get().name()).isNotBlank();
    }
}
