package org.example;

/**
 * Уровни сложности: название и количество попыток (как в консольной версии).
 */
public enum Difficulty {
    EASY("Лёгкий", 10),
    MEDIUM("Средний", 5),
    HARD("Сложный", 3);

    private final String title;
    private final int maxAttempts;

    Difficulty(String title, int maxAttempts) {
        this.title = title;
        this.maxAttempts = maxAttempts;
    }

    public String title() {
        return title;
    }

    public int maxAttempts() {
        return maxAttempts;
    }
}
