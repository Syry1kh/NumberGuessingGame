package org.example;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Оконная версия игры «Угадай число».
 * Запуск: метод main (или jar после mvn package).
 */
public class NumberGuessGUI {

    // ---------- палитра ----------
    static final Color BG = new Color(0x1E2327);
    static final Color CARD = new Color(0x2A3238);
    static final Color TEXT = new Color(0xE8EDF0);
    static final Color MUTED = new Color(0x93A1AB);
    static final Color ACCENT = new Color(0x4EC9B0);
    static final Color UP = new Color(0xF0B45A);     // загаданное число больше
    static final Color DOWN = new Color(0x6FB3F2);   // загаданное число меньше
    static final Color WIN = new Color(0x6FD08C);
    static final Color LOSE = new Color(0xEF6B6B);

    private static final String SCREEN_MENU = "menu";
    private static final String SCREEN_GAME = "game";
    private static final String ACTIONS_PLAY = "play";
    private static final String ACTIONS_OVER = "over";

    // ---------- окно ----------
    private final JFrame frame = new JFrame("Угадай число");
    private final CardLayout screens = new CardLayout();
    private final JPanel root = new JPanel(screens);

    // ---------- экран игры ----------
    private final JLabel levelLabel = label("", 15, Font.BOLD, MUTED);
    private final JLabel timerLabel = label("00:00", 15, Font.BOLD, MUTED);
    private final AttemptsPanel attemptsPanel = new AttemptsPanel();
    private final JLabel attemptsLabel = label("", 13, Font.PLAIN, MUTED);
    private final JLabel hintLabel = label("", 24, Font.BOLD, TEXT);
    private final JLabel subLabel = label("", 14, Font.PLAIN, MUTED);
    private final RangeBar rangeBar = new RangeBar();
    private final DefaultListModel<GameEngine.Guess> historyModel = new DefaultListModel<>();
    private final GuessField input = new GuessField();
    private final RoundButton submitBtn = new RoundButton("Проверить", null, ACCENT, BG);
    private final RoundButton againBtn = new RoundButton("Сыграть ещё", null, ACCENT, BG);
    private final RoundButton menuBtn = new RoundButton("В меню", null, CARD, MUTED);
    private final CardLayout actionsLayout = new CardLayout();
    private final JPanel actions = new JPanel(actionsLayout);

    private final javax.swing.Timer clock = new javax.swing.Timer(200, e -> updateTimer());
    private GameEngine engine;
    private long startNanos;
    private long finalMillis;

    NumberGuessGUI() {
        root.setBackground(BG);
        root.add(buildMenu(), SCREEN_MENU);
        root.add(buildGame(), SCREEN_GAME);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.setMinimumSize(new Dimension(420, 660));
        frame.setSize(460, 720);
        frame.setLocationRelativeTo(null);
        screens.show(root, SCREEN_MENU);
    }

    void show() {
        frame.setVisible(true);
    }

    JFrame frame() {
        return frame;
    }

    // =====================================================================
    //  Меню
    // =====================================================================

    private JPanel buildMenu() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(56, 36, 36, 36));

        JLabel title = label("Угадай число", 36, Font.BOLD, TEXT);
        JLabel sub = label("Я загадал число от " + GameEngine.MIN + " до " + GameEngine.MAX + ".", 16, Font.PLAIN, MUTED);
        JLabel choose = label("Выбери сложность", 14, Font.BOLD, ACCENT);

        center(title);
        center(sub);
        center(choose);
        p.add(title);
        p.add(Box.createVerticalStrut(8));
        p.add(sub);
        p.add(Box.createVerticalStrut(48));
        p.add(choose);
        p.add(Box.createVerticalStrut(14));

        Color[] colors = {new Color(0x3C8C74), new Color(0xB98A3B), new Color(0xB4505A)};
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            Difficulty d = levels[i];
            int n = d.maxAttempts();
            RoundButton b = new RoundButton(d.title(), n + " " + plural(n, "попытка", "попытки", "попыток"),
                    colors[i], Color.WHITE);
            b.setPreferredSize(new Dimension(300, 62));
            b.addActionListener(e -> startGame(d));
            center(b);
            stretch(b);
            p.add(b);
            p.add(Box.createVerticalStrut(12));
        }

        p.add(Box.createVerticalGlue());
        JLabel foot = label("После каждого хода — подсказка «больше» или «меньше»", 12, Font.PLAIN, MUTED);
        center(foot);
        p.add(foot);
        return p;
    }

    // =====================================================================
    //  Экран игры
    // =====================================================================

    private JPanel buildGame() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(22, 24, 18, 24));

        // --- верх: уровень, таймер, попытки, подсказка ---
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(levelLabel, BorderLayout.WEST);
        header.add(timerLabel, BorderLayout.EAST);
        stretch(header);
        top.add(header);
        top.add(Box.createVerticalStrut(12));

        attemptsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stretch(attemptsPanel);
        top.add(attemptsPanel);
        top.add(Box.createVerticalStrut(4));
        center(attemptsLabel);
        top.add(attemptsLabel);
        top.add(Box.createVerticalStrut(14));

        RoundedPanel status = new RoundedPanel(new BoxLayout(new JPanel(), BoxLayout.Y_AXIS), CARD, 22);
        status.setLayout(new BoxLayout(status, BoxLayout.Y_AXIS));
        status.setBorder(new EmptyBorder(10, 16, 10, 16));
        status.setPreferredSize(new Dimension(300, 92));
        center(hintLabel);
        center(subLabel);
        status.add(Box.createVerticalGlue());
        status.add(hintLabel);
        status.add(Box.createVerticalStrut(6));
        status.add(subLabel);
        status.add(Box.createVerticalGlue());
        center(status);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        top.add(status);
        p.add(top, BorderLayout.NORTH);

        // --- центр: история ходов ---
        RoundedPanel historyCard = new RoundedPanel(new BorderLayout(), CARD, 22);
        historyCard.setBorder(new EmptyBorder(12, 4, 8, 4));
        JLabel historyTitle = label("Ходы", 13, Font.BOLD, MUTED);
        historyTitle.setBorder(new EmptyBorder(0, 14, 6, 14));
        historyCard.add(historyTitle, BorderLayout.NORTH);
        historyCard.add(buildHistory(), BorderLayout.CENTER);
        p.add(historyCard, BorderLayout.CENTER);

        // --- низ: шкала, ввод, кнопки ---
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        rangeBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        stretch(rangeBar);
        bottom.add(rangeBar);
        bottom.add(Box.createVerticalStrut(10));

        JPanel playRow = new JPanel(new BorderLayout(10, 0));
        playRow.setOpaque(false);
        input.setPreferredSize(new Dimension(100, 54));
        submitBtn.setPreferredSize(new Dimension(130, 54));
        playRow.add(input, BorderLayout.CENTER);
        playRow.add(submitBtn, BorderLayout.EAST);

        JPanel overRow = new JPanel(new BorderLayout());
        overRow.setOpaque(false);
        againBtn.setPreferredSize(new Dimension(100, 54));
        overRow.add(againBtn, BorderLayout.CENTER);

        actions.setOpaque(false);
        actions.add(playRow, ACTIONS_PLAY);
        actions.add(overRow, ACTIONS_OVER);
        actions.setPreferredSize(new Dimension(300, 54));
        stretch(actions);
        bottom.add(actions);
        bottom.add(Box.createVerticalStrut(10));

        menuBtn.setPreferredSize(new Dimension(100, 40));
        center(menuBtn);
        stretch(menuBtn);
        bottom.add(menuBtn);
        p.add(bottom, BorderLayout.SOUTH);

        // --- поведение ---
        submitBtn.addActionListener(e -> submitGuess());
        input.addActionListener(e -> submitGuess());
        againBtn.addActionListener(e -> startGame(engine.difficulty()));
        menuBtn.addActionListener(e -> backToMenu());
        input.onRejected = () -> warn("Можно вводить только цифры");
        return p;
    }

    private JComponent buildHistory() {
        JList<GameEngine.Guess> list = new JList<>(historyModel) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getModel().getSize() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(font(13, Font.PLAIN));
                    g2.setColor(MUTED);
                    String s = "Здесь появятся твои ходы";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, getHeight() / 2);
                    g2.dispose();
                }
            }
        };
        list.setOpaque(false);
        list.setFixedCellHeight(32);
        list.setFocusable(false);
        list.setCellRenderer(new GuessRow());
        list.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) { /* выбор не нужен */ }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        scroll.getVerticalScrollBar().setOpaque(false);
        return scroll;
    }

    // =====================================================================
    //  Игровой процесс
    // =====================================================================

    void startGame(Difficulty difficulty) {
        engine = new GameEngine(difficulty);
        historyModel.clear();
        input.setText("");
        int n = difficulty.maxAttempts();
        levelLabel.setText(difficulty.title() + " · " + n + " " + plural(n, "попытка", "попытки", "попыток"));
        timerLabel.setText("00:00");
        startNanos = System.nanoTime();
        finalMillis = 0;
        clock.restart();
        actionsLayout.show(actions, ACTIONS_PLAY);
        frame.getRootPane().setDefaultButton(submitBtn);
        refresh();
        screens.show(root, SCREEN_GAME);
        SwingUtilities.invokeLater(input::requestFocusInWindow);
    }

    void submitGuess() {
        if (engine == null || engine.state() != GameEngine.State.PLAYING) {
            return;
        }
        String text = input.getText().trim();
        if (text.isEmpty()) {
            warn("Введи число от " + GameEngine.MIN + " до " + GameEngine.MAX);
            return;
        }
        int value = Integer.parseInt(text); // не больше 3 цифр — переполнения нет
        if (value < GameEngine.MIN || value > GameEngine.MAX) {
            warn("Число должно быть от " + GameEngine.MIN + " до " + GameEngine.MAX);
            input.selectAll();
            return;
        }

        GameEngine.Guess guess = engine.guess(value);
        historyModel.add(0, guess);
        input.setText("");
        if (engine.state() != GameEngine.State.PLAYING) {
            finalMillis = (System.nanoTime() - startNanos) / 1_000_000;
            clock.stop();
            updateTimer();
            actionsLayout.show(actions, ACTIONS_OVER);
            frame.getRootPane().setDefaultButton(againBtn);
            SwingUtilities.invokeLater(againBtn::requestFocusInWindow);
        } else {
            SwingUtilities.invokeLater(input::requestFocusInWindow);
        }
        refresh();
    }

    private void backToMenu() {
        clock.stop();
        engine = null;
        frame.getRootPane().setDefaultButton(null);
        screens.show(root, SCREEN_MENU);
    }

    /** Показывает сообщение об ошибке ввода (попытка при этом не тратится). */
    private void warn(String message) {
        subLabel.setForeground(LOSE);
        subLabel.setText(message);
    }

    private void refresh() {
        GameEngine e = engine;
        int max = e.difficulty().maxAttempts();
        attemptsPanel.set(max, e.attemptsUsed());
        rangeBar.set(e.low(), e.high(), e.guesses());
        subLabel.setForeground(MUTED);

        switch (e.state()) {
            case WON -> {
                int used = e.attemptsUsed();
                attemptsLabel.setText("Победа!");
                hintLabel.setForeground(WIN);
                hintLabel.setText("Угадал!");
                subLabel.setText("Число " + e.secret() + " · " + used + " "
                        + plural(used, "попытка", "попытки", "попыток") + " · " + formatTime(finalMillis));
            }
            case LOST -> {
                attemptsLabel.setText("Попытки закончились");
                hintLabel.setForeground(LOSE);
                hintLabel.setText("Не угадал");
                subLabel.setText("Я загадал число " + e.secret());
            }
            default -> {
                int left = e.attemptsLeft();
                attemptsLabel.setText("Осталось " + left + " из " + max);
                if (e.guesses().isEmpty()) {
                    hintLabel.setForeground(TEXT);
                    hintLabel.setText("Твой ход");
                    subLabel.setText("Введи число от " + GameEngine.MIN + " до " + GameEngine.MAX);
                } else {
                    GameEngine.Guess last = e.guesses().get(e.guesses().size() - 1);
                    boolean higher = last.hint() == GameEngine.Hint.HIGHER;
                    hintLabel.setForeground(higher ? UP : DOWN);
                    hintLabel.setText("Число " + (higher ? "больше " : "меньше ") + last.value());
                    subLabel.setText("Теперь оно между " + e.low() + " и " + e.high());
                }
            }
        }
    }

    private void updateTimer() {
        long millis = engine != null && engine.state() != GameEngine.State.PLAYING
                ? finalMillis
                : (System.nanoTime() - startNanos) / 1_000_000;
        timerLabel.setText(formatTime(millis));
    }

    // =====================================================================
    //  Вспомогательное
    // =====================================================================

    static String formatTime(long millis) {
        long s = millis / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    static String plural(int n, String one, String few, String many) {
        int m10 = n % 10;
        int m100 = n % 100;
        if (m10 == 1 && m100 != 11) {
            return one;
        }
        if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) {
            return few;
        }
        return many;
    }

    static Font font(int size, int style) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font(size, style));
        l.setForeground(color);
        return l;
    }

    private static void center(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /** Растягивает компонент на всю ширину в BoxLayout, высота — как есть. */
    private static void stretch(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // =====================================================================
    //  Самописные компоненты
    // =====================================================================

    /** Панель со скруглёнными углами. */
    static class RoundedPanel extends JPanel {
        private final Color fill;
        private final int arc;

        RoundedPanel(LayoutManager layout, Color fill, int arc) {
            super(layout);
            this.fill = fill;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Скруглённая кнопка; необязательный второй текст рисуется справа. */
    static class RoundButton extends JButton {
        private final String detail;
        private final Color base;
        private final Color fg;
        private boolean hover;

        RoundButton(String text, String detail, Color base, Color fg) {
            super(text);
            this.detail = detail;
            this.base = base;
            this.fg = fg;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(font(16, Font.BOLD));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            Color c = base;
            if (getModel().isPressed()) {
                c = base.darker();
            } else if (hover) {
                c = base.brighter();
            }
            g.setColor(c);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

            g.setFont(getFont());
            g.setColor(fg);
            FontMetrics fm = g.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            if (detail == null) {
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, y);
            } else {
                g.drawString(getText(), 22, y);
                g.setFont(font(14, Font.PLAIN));
                g.setColor(withAlpha(fg, 210));
                FontMetrics dm = g.getFontMetrics();
                g.drawString(detail, getWidth() - 22 - dm.stringWidth(detail),
                        (getHeight() - dm.getHeight()) / 2 + dm.getAscent());
            }
            g.dispose();
        }
    }

    /** Поле ввода: только цифры, максимум три символа. */
    static class GuessField extends JTextField {
        Runnable onRejected = () -> { };

        GuessField() {
            super();
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(font(26, Font.BOLD));
            setForeground(TEXT);
            setCaretColor(ACCENT);
            setSelectionColor(withAlpha(ACCENT, 90));
            setBorder(new EmptyBorder(6, 14, 6, 14));
            ((AbstractDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String s, AttributeSet a)
                        throws BadLocationException {
                    replace(fb, offset, 0, s, a);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a)
                        throws BadLocationException {
                    String text = s == null ? "" : s;
                    String digits = text.replaceAll("[^0-9]", "");
                    if (!text.isEmpty() && digits.isEmpty()) {
                        onRejected.run();
                        return;
                    }
                    if (fb.getDocument().getLength() - length + digits.length() > 3) {
                        return;
                    }
                    super.replace(fb, offset, length, digits, a);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(CARD);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            if (hasFocus()) {
                g.setColor(ACCENT);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Кружки-попытки: закрашенные — остались, контурные — потрачены. */
    static class AttemptsPanel extends JComponent {
        private int max = 10;
        private int used = 0;

        AttemptsPanel() {
            setPreferredSize(new Dimension(300, 18));
        }

        void set(int max, int used) {
            this.max = max;
            this.used = used;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int d = 14;
            int gap = 9;
            int total = max * d + (max - 1) * gap;
            int x = (getWidth() - total) / 2;
            int y = (getHeight() - d) / 2;
            for (int i = 0; i < max; i++) {
                Ellipse2D dot = new Ellipse2D.Float(x + i * (d + gap) + 1, y + 1, d - 2, d - 2);
                if (i < max - used) {
                    g.setColor(ACCENT);
                    g.fill(dot);
                } else {
                    g.setColor(withAlpha(MUTED, 130));
                    g.setStroke(new BasicStroke(1.6f));
                    g.draw(dot);
                }
            }
            g.dispose();
        }
    }

    /** Шкала 1…100: подсвечено то, где ещё может быть число; точки — прошлые ходы. */
    static class RangeBar extends JComponent {
        private int low = GameEngine.MIN;
        private int high = GameEngine.MAX;
        private List<GameEngine.Guess> guesses = List.of();

        RangeBar() {
            setPreferredSize(new Dimension(300, 34));
        }

        void set(int low, int high, List<GameEngine.Guess> guesses) {
            this.low = low;
            this.high = high;
            this.guesses = List.copyOf(guesses);
            repaint();
        }

        private float xOf(int value, int pad, int width) {
            return pad + (value - GameEngine.MIN) / (float) (GameEngine.MAX - GameEngine.MIN) * (width - 2 * pad);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int pad = 8;
            int w = getWidth();
            int trackH = 8;
            int trackY = 6;
            float step = (w - 2 * pad) / (float) (GameEngine.MAX - GameEngine.MIN);

            g.setColor(CARD);
            g.fill(new RoundRectangle2D.Float(pad - 4, trackY, w - 2 * pad + 8, trackH, trackH, trackH));

            float x1 = xOf(low, pad, w) - step / 2;
            float x2 = xOf(high, pad, w) + step / 2;
            g.setColor(withAlpha(ACCENT, 200));
            g.fill(new RoundRectangle2D.Float(x1, trackY, Math.max(x2 - x1, trackH), trackH, trackH, trackH));

            for (GameEngine.Guess guess : guesses) {
                Color c = guess.hint() == GameEngine.Hint.HIGHER ? UP
                        : guess.hint() == GameEngine.Hint.LOWER ? DOWN : WIN;
                float cx = xOf(guess.value(), pad, w);
                g.setColor(BG);
                g.fill(new Ellipse2D.Float(cx - 5, trackY + trackH / 2f - 5, 10, 10));
                g.setColor(c);
                g.fill(new Ellipse2D.Float(cx - 3.5f, trackY + trackH / 2f - 3.5f, 7, 7));
            }

            g.setFont(font(11, Font.PLAIN));
            g.setColor(MUTED);
            FontMetrics fm = g.getFontMetrics();
            int ty = trackY + trackH + 4 + fm.getAscent();
            g.drawString(String.valueOf(GameEngine.MIN), pad - 4, ty);
            String max = String.valueOf(GameEngine.MAX);
            g.drawString(max, w - pad + 4 - fm.stringWidth(max), ty);
            g.dispose();
        }
    }

    /** Строка истории: номер хода, число, подсказка. */
    static class GuessRow extends JPanel implements ListCellRenderer<GameEngine.Guess> {
        private final JLabel idx = label("", 13, Font.PLAIN, MUTED);
        private final JLabel value = label("", 16, Font.BOLD, TEXT);
        private final JLabel hint = label("", 14, Font.BOLD, TEXT);

        GuessRow() {
            super(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(3, 14, 3, 14));
            idx.setHorizontalAlignment(SwingConstants.LEFT);
            idx.setPreferredSize(new Dimension(30, 20));
            value.setHorizontalAlignment(SwingConstants.LEFT);
            hint.setHorizontalAlignment(SwingConstants.RIGHT);
            add(idx, BorderLayout.WEST);
            add(value, BorderLayout.CENTER);
            add(hint, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GameEngine.Guess> list, GameEngine.Guess g,
                                                      int index, boolean selected, boolean focused) {
            idx.setText((list.getModel().getSize() - index) + ".");
            value.setText(String.valueOf(g.value()));
            switch (g.hint()) {
                case HIGHER -> {
                    hint.setText("↑ больше");
                    hint.setForeground(UP);
                }
                case LOWER -> {
                    hint.setText("↓ меньше");
                    hint.setForeground(DOWN);
                }
                case CORRECT -> {
                    hint.setText("угадал");
                    hint.setForeground(WIN);
                }
            }
            return this;
        }
    }

    /** Тонкий скроллбар без стрелок под тёмную тему. */
    static class ThinScrollBarUI extends BasicScrollBarUI {
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private static JButton zeroButton() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // дорожку не рисуем
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(new Color(0x56636D));
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // =====================================================================

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new NumberGuessGUI().show());
    }
}
