package com.example.api.pmd;

final class PmdTestDummy {
    void swallowNumberFormat() {
        try {
            Integer.parseInt("not-a-number");
        } catch (Exception ignored) {
        }
    }

    int duplicateBlockA(int seed) {
        int total = 0;
        int limit = seed + 5;
        for (int i = 0; i < limit; i++) {
            int value = i * 3 + seed;
            if (value % 2 == 0) {
                total += value / 2;
            } else {
                total += value * 2;
            }
            String text = "value:" + value;
            total += text.length();
        }
        int[] values = new int[] {seed, seed + 1, seed + 2, seed + 3, seed + 4};
        for (int value : values) {
            String label = "seed:" + value;
            total += label.length();
            if (value % 3 == 0) {
                total += value;
            } else if (value % 3 == 1) {
                total -= value;
            } else {
                total += value * 2;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("sum=").append(total).append(":").append(seed);
        String summary = builder.toString();
        total += summary.length();
        return total;
    }

    int duplicateBlockB(int seed) {
        int total = 0;
        int limit = seed + 5;
        for (int i = 0; i < limit; i++) {
            int value = i * 3 + seed;
            if (value % 2 == 0) {
                total += value / 2;
            } else {
                total += value * 2;
            }
            String text = "value:" + value;
            total += text.length();
        }
        int[] values = new int[] {seed, seed + 1, seed + 2, seed + 3, seed + 4};
        for (int value : values) {
            String label = "seed:" + value;
            total += label.length();
            if (value % 3 == 0) {
                total += value;
            } else if (value % 3 == 1) {
                total -= value;
            } else {
                total += value * 2;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("sum=").append(total).append(":").append(seed);
        String summary = builder.toString();
        total += summary.length();
        return total;
    }
}
