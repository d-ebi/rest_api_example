package com.example.api.pmd;

final class PmdMainDummy {
    String buildValue(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            StringBuilder loopBuilder = new StringBuilder();
            loopBuilder.append(i);
            result.append(loopBuilder);
        }
        return result.toString();
    }
}
