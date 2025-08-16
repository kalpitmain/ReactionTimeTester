

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.Random;

public class ReactionTimeTester extends JFrame {
    private final JLabel statusLabel = new JLabel("Click START to begin", SwingConstants.CENTER);
    private final JButton startBtn = new JButton("START");
    private final JButton reactBtn = new JButton("CLICK when GO!");
    private final JLabel bestScoreLbl = new JLabel("Best: - ms", SwingConstants.CENTER);

    private final Random rng = new Random();
    private long goTime;
    private boolean waitingForGo = false;

    private final ScoreStore scoreStore = new ScoreStore();

    public ReactionTimeTester() {
        super("Reaction Time Tester");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(statusLabel, BorderLayout.NORTH);

        reactBtn.setFont(reactBtn.getFont().deriveFont(Font.BOLD, 16f));
        reactBtn.setEnabled(false);

        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        center.add(startBtn);
        center.add(reactBtn);
        add(center, BorderLayout.CENTER);

        bestScoreLbl.setFont(bestScoreLbl.getFont().deriveFont(14f));
        bestScoreLbl.setText("Best: " + scoreStore.getBest() + " ms");
        add(bestScoreLbl, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> onStart());
        reactBtn.addActionListener(e -> onReact());
    }

    private void onStart() {
        startBtn.setEnabled(false);
        reactBtn.setEnabled(false);
        statusLabel.setText("Wait for GO!");

        int delay = 2000 + rng.nextInt(3000); // 2–5 seconds
        Timer t = new Timer(delay, e -> {
            goTime = System.currentTimeMillis();
            statusLabel.setText("GO!");
            reactBtn.setEnabled(true);
            waitingForGo = true;
        });
        t.setRepeats(false);
        t.start();
    }

    private void onReact() {
        if (!waitingForGo) return;
        long now = System.currentTimeMillis();
        long reaction = now - goTime;
        statusLabel.setText("Reaction Time: " + reaction + " ms");
        waitingForGo = false;
        reactBtn.setEnabled(false);
        startBtn.setEnabled(true);

        if (scoreStore.getBest() == -1 || reaction < scoreStore.getBest()) {
            scoreStore.setBest((int) reaction);
            bestScoreLbl.setText("Best: " + reaction + " ms");
            JOptionPane.showMessageDialog(this, "New Best Time! " + reaction + " ms");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ReactionTimeTester().setVisible(true));
    }

    // --- Score storage ---
    static class ScoreStore {
        private final Path file;
        private int best = -1;
        ScoreStore() {
            String home = System.getProperty("user.home");
            file = Paths.get(home, ".reaction_best");
            load();
        }
        int getBest() { return best; }
        void setBest(int v) { this.best = v; save(); }
        private void load() {
            try {
                if (Files.exists(file)) {
                    best = Integer.parseInt(Files.readString(file).trim());
                }
            } catch (Exception ignored) { best = -1; }
        }
        private void save() {
            try { Files.writeString(file, String.valueOf(best), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); }
            catch (Exception ignored) {}
        }
    }
}
