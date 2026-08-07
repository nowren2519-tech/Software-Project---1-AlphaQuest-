import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setTitle("AlphaQuest");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(window);
        } else {
            window.setVisible(true);
        }

        showNameEntry(window);

        window.revalidate();
        window.repaint();
    }

    static void showNameEntry(JFrame window) {
        NameEntryScreen nameScreen = new NameEntryScreen(name -> {
            window.getContentPane().removeAll();
            showLevelSelect(window);
            window.revalidate();
            window.repaint();
        });

        window.getContentPane().removeAll();
        window.add(nameScreen);
        window.revalidate();
        window.repaint();
        nameScreen.requestFocusInWindow();
    }

    static void showLevelSelect(JFrame window) {
        LevelSelectScreen selectScreen = new LevelSelectScreen((level, word) -> {
            SoundManager.stopMusic();
            window.getContentPane().removeAll();
            GamePanel gamePanel = new GamePanel(word, level);
            gamePanel.onBackToMenu = () -> {
                SoundManager.stopMusic();
                gamePanel.gameState = "paused";
                window.getContentPane().removeAll();
                showLevelSelect(window);
                window.revalidate();
                window.repaint();
            };
            window.add(gamePanel);
            window.revalidate();
            window.repaint();
            gamePanel.requestFocusInWindow();
        });

        selectScreen.onViewProgress  = () -> showProgressScreen(window);
        selectScreen.onSwitchPlayer  = () -> {
            SoundManager.stopMusic();
            showNameEntry(window);
        };

        window.getContentPane().removeAll();
        window.add(selectScreen);
        window.revalidate();
        window.repaint();
        selectScreen.requestFocusInWindow();
    }

    static void showProgressScreen(JFrame window) {
        ProgressScreen progressScreen = new ProgressScreen(() -> {
            window.getContentPane().removeAll();
            showLevelSelect(window);
            window.revalidate();
            window.repaint();
        });

        window.getContentPane().removeAll();
        window.add(progressScreen);
        window.revalidate();
        window.repaint();
        progressScreen.requestFocusInWindow();
    }
}