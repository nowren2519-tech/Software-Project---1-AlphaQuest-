import java.awt.*;
import java.util.ArrayList;

public class LetterBox {

    ArrayList<Character> collected = new ArrayList<>();
    String targetWord;

    int screenW, screenH;

    int cellSize = 32;
    int padding  = 4;

    int boxX, boxY;

    boolean wordComplete = false;
    boolean flashRed     = false;
    int     flashTimer   = 0;
    boolean winAnim      = false;
    float   winGlow      = 0f;
    boolean winGlowUp    = true;

    public LetterBox(String targetWord, int screenW, int screenH) {
        this.targetWord = targetWord.toLowerCase();
        this.screenW    = screenW;
        this.screenH    = screenH;
        repositionBox();
    }

    private void repositionBox() {
        int totalW = targetWord.length() * (cellSize + padding) + padding;
        boxX = screenW - totalW - 15;
        boxY = 10;
    }

    public void addLetter(char c) {
        collected.add(c);
        if (collected.size() == targetWord.length()) {
            wordComplete = checkWord();
            if (wordComplete) winAnim = true;
        }
    }
    private boolean checkWord() {
        if (collected.size() != targetWord.length())
            return false;
        for (int i = 0; i < targetWord.length(); i++) {
            if (collected.get(i) != targetWord.charAt(i))
                return false;
        }
        return true;
    }

    public void update() {
        if (winAnim) {
            winGlow += winGlowUp ? 0.05f : -0.05f;
            if (winGlow >= 1f) winGlowUp = false;
            if (winGlow <= 0f) winGlowUp = true;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int len    = targetWord.length();
        int totalW = len * (cellSize + padding) + padding;
        int totalH = cellSize + padding * 2 + 20; // label space

        g2.setColor(new Color(0, 0, 20, 190));
        g2.fillRoundRect(boxX - 8, boxY, totalW + 16, totalH + 8, 12, 12);

        g2.setColor(new Color(80, 120, 200, 140));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX - 8, boxY, totalW + 16, totalH + 8, 12, 12);

        g2.setFont(new Font("Kalpurush", Font.BOLD, 10));
        g2.setColor(new Color(150, 200, 255));
        String label = "COLLECT THE WORD";
        FontMetrics lm = g2.getFontMetrics();
        g2.drawString(label, boxX + (totalW - lm.stringWidth(label)) / 2, boxY + 13);

        int cellsY = boxY + 18;
        for (int i = 0; i < len; i++) {
            int cx = boxX + padding + i * (cellSize + padding);

            if (i < collected.size()) {
                if (winAnim) {
                    float glow = 0.5f + winGlow * 0.5f;
                    g2.setColor(new Color(1f, 0.85f * glow, 0f, 0.9f));
                } else {
                    g2.setColor(new Color(30, 150, 70, 210));
                }
            } else if (flashRed && i == collected.size()) {
                g2.setColor(new Color(200, 40, 40, 210));
            } else {
                g2.setColor(new Color(20, 20, 50, 200));
            }
            g2.fillRoundRect(cx, cellsY, cellSize, cellSize, 7, 7);

            if (i == collected.size() && !flashRed) {
                g2.setColor(new Color(80, 200, 255, 200));
                g2.setStroke(new BasicStroke(2f));
            } else {
                g2.setColor(new Color(80, 80, 130, 180));
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawRoundRect(cx, cellsY, cellSize, cellSize, 7, 7);

            g2.setFont(new Font("Kalpurush", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            if (i < collected.size()) {
                g2.setColor(Color.WHITE);
                String ch = String.valueOf(Character.toUpperCase(collected.get(i)));
                g2.drawString(ch,
                        cx + (cellSize - fm.stringWidth(ch)) / 2,
                        cellsY + (cellSize + fm.getAscent()) / 2 - 3);
            }
        }
    }

    public boolean isWordComplete()  {
        return wordComplete;
    }
    public int getCollectedCount()   {
        return collected.size();
    }
}