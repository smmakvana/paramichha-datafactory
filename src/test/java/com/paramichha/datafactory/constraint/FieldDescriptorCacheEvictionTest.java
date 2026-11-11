package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.datafactory.instantiation.LombokObjectCreation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Evicting the descriptor and creation caches (used by hot reload) leaves generation working. */
class FieldDescriptorCacheEvictionTest {

    record Item(@NotBlank String name) {}

    @Test
    @DisplayName("generation still works after the caches are evicted")
    void generationStillWorksAfterEviction() {
        assertThat(FieldDescriptor.extract(Item.class)).hasSize(1);
        FieldDescriptor.evict(Item.class);
        assertThat(FieldDescriptor.extract(Item.class)).hasSize(1);
        FieldDescriptor.evictAll();
        LombokObjectCreation.evictAll();
        assertThat(DataFactory.valid(Item.class).get().name()).isNotBlank();
    }
}
