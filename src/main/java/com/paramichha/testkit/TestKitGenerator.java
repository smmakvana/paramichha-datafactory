package com.paramichha.testkit;

import java.util.List;

public interface TestKitGenerator {
    List<GeneratedTestClass> generateAll();

    GeneratedTestClass generate();
}
