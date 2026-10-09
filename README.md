https://roadmap.sh/projects/number-guessing-game
# Number Guessing Game

Игра на Java, в которой нужно угадать загаданное компьютером число от 1 до 100.
Есть оконная версия (Swing) и исходная консольная.

## Возможности

Три уровня сложности:
  - Лёгкий (Easy) — 10 попыток
  - Средний (Medium) — 5 попыток
  - Сложный (Hard) — 3 попытки

Подсказки после каждого хода («больше» / «меньше»).
Шкала 1…100, на которой видно, где ещё может быть число.
История ходов.
Подсчёт времени, потраченного на угадывание.
Обработка некорректного ввода: в поле можно ввести только цифры, а число вне диапазона не тратит попытку.

## Запуск

Нужна Java (JDK 25 по `pom.xml`).

### Оконная версия

```
mvn package
java -jar target/NumberGuessing.jar
```

Или запустить метод `main` в `org.example.NumberGuessGUI` из IDE.
Готовый jar можно открыть двойным кликом, если в системе установлена Java.

### Windows .exe (Java на компьютере не нужна)

Двойной клик по `build-exe.bat` (нужен JDK 21 или новее в `PATH`) — получится
`dist\NumberGuess\NumberGuess.exe`. Внутри папки лежит встроенная Java, поэтому
игра запускается и на компьютерах, где Java не установлена. Папку `NumberGuess`
нужно хранить целиком: одному `.exe` нужны файлы рядом с ним.

Без установки JDK `.exe` собирает и GitHub: вкладка **Actions** → **Build Windows exe** →
нужный запуск → **Artifacts** → `NumberGuess-windows`
(workflow `.github/workflows/build-exe.yml`, можно запустить вручную кнопкой *Run workflow*).

### Консольная версия

Запустить метод `main` в классе `NumberGuess` (`src/main/java/NumberGuess.java`).

## Структура

- `org.example.GameEngine` — правила одной партии, ничего не знает про консоль и окно.
- `org.example.Difficulty` — уровни сложности и число попыток.
- `org.example.NumberGuessGUI` — окно игры.
- `NumberGuess`, `org.example.Levels` — консольная версия.
- `build-exe.bat` — сборка Windows `.exe` (jpackage); `.github/workflows/build-exe.yml` — то же на сервере GitHub.
