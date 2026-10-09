package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Логика одной партии. Ничего не знает ни про консоль, ни про окно —
 * поэтому её можно использовать и в GUI, и в тестах.
 */
public class GameEngine {

    public static final int MIN = 1;
    public static final int MAX = 100;

    public enum State { PLAYING, WON, LOST }

    /** Подсказка после хода: загаданное число больше / меньше / угадано. */
    public enum Hint { HIGHER, LOWER, CORRECT }

    public record Guess(int value, Hint hint) { }

    private final Difficulty difficulty;
    private final int secret;
    private final List<Guess> guesses = new ArrayList<>();
    private State state = State.PLAYING;
    // Границы, в которых ещё может находиться число (для подсказки «между X и Y»)
    private int low = MIN;
    private int high = MAX;

    public GameEngine(Difficulty difficulty) {
        this(difficulty, new Random().nextInt(MIN, MAX + 1));
    }

    /** Конструктор с заданным числом — удобен для проверок. */
    GameEngine(Difficulty difficulty, int secret) {
        if (secret < MIN || secret > MAX) {
            throw new IllegalArgumentException("Число должно быть от " + MIN + " до " + MAX);
        }
        this.difficulty = difficulty;
        this.secret = secret;
    }

    /**
     * Делает ход. Некорректное число (вне диапазона) попытку не тратит —
     * вместо этого бросается исключение.
     */
    public Guess guess(int value) {
        if (state != State.PLAYING) {
            throw new IllegalStateException("Игра уже закончена");
        }
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException("Введите число от " + MIN + " до " + MAX);
        }

        Hint hint;
        if (value == secret) {
            hint = Hint.CORRECT;
        } else if (value < secret) {
            hint = Hint.HIGHER;
            low = Math.max(low, value + 1);
        } else {
            hint = Hint.LOWER;
            high = Math.min(high, value - 1);
        }

        Guess guess = new Guess(value, hint);
        guesses.add(guess);

        if (hint == Hint.CORRECT) {
            state = State.WON;
        } else if (guesses.size() >= difficulty.maxAttempts()) {
            state = State.LOST;
        }
        return guess;
    }

    public State state() {
        return state;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    /** Загаданное число. GUI показывает его только после окончания игры. */
    public int secret() {
        return secret;
    }

    public int attemptsUsed() {
        return guesses.size();
    }

    public int attemptsLeft() {
        return difficulty.maxAttempts() - guesses.size();
    }

    public int low() {
        return low;
    }

    public int high() {
        return high;
    }

    public List<Guess> guesses() {
        return Collections.unmodifiableList(guesses);
    }
}
