import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProgressScreen extends JPanel implements MouseListener, MouseMotionListener {

    int WIDTH, HEIGHT;

    String[] levelLabels = { "LEVEL  1", "LEVEL  2", "LEVEL  3" };
    Color[]  levelColors = {
            new Color(60,  180, 100),
            new Color(60,  140, 220),
            new Color(200, 160,  40),
    };

    int cardW = 260, cardH = 220;
    int[] cardX = new int[3];
    int[] cardY = new int[3];

    Rectangle btnBack   = new Rectangle();
    Rectangle btnReset  = new Rectangle();
    boolean hoverBack, hoverReset;

    Runnable onBack;

    float[] starX, starY, starSpeed, starSize;
    int STAR_COUNT = 100;
    float glowPhase = 0f;

    Timer animTimer;

    public ProgressScreen(Runnable onBack) {
        this.onBack = onBack;

        WIDTH  = Toolkit.getDefaultToolkit().getScreenSize().width;
        HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        addMouseListener(this);
        addMouseMotionListener(this);

        initStars();
        layoutCards();

        animTimer = new Timer(16, e -> {
            updateStars();
            glowPhase += 0.03f;
            if (glowPhase > Math.PI * 2) glowPhase -= (float) (Math.PI * 2);
            repaint();
        });
        animTimer.start();
    }

    void initStars() {
        starX     = new float[STAR_COUNT];
        starY     = new float[STAR_COUNT];
        starSpeed = new float[STAR_COUNT];
        starSize  = new float[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = (float) (Math.random() * WIDTH);
            starY[i]     = (float) (Math.random() * HEIGHT);
            starSpeed[i] = 0.2f + (float) (Math.random() * 0.5f);
            starSize[i]  = 1f + (float) (Math.random() * 2.5f);
        }
    }

    void updateStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > HEIGHT) {
                starY[i] = 0;
                starX[i] = (float) (Math.random() * WIDTH);
            }
        }
    }

    void layoutCards() {
        int gap    = 40;
        int totalW = 3 * cardW + 2 * gap;
        int startX = (WIDTH - totalW) / 2;
        int centerY = HEIGHT / 2 - cardH / 2;
        for (int i = 0; i < 3; i++) {
            cardX[i] = startX + i * (cardW + gap);
            cardY[i] = centerY;
        }
    }

    void stop() {
        animTimer.stop();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(0, 0, new Color(5, 5, 25),
                0, HEIGHT, new Color(10, 15, 45));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < STAR_COUNT; i++) {
            float alpha = 0.4f + 0.6f * (starSize[i] / 3.5f);
            g2.setColor(new Color(1f, 1f, 1f, alpha));
            g2.fillOval((int) starX[i], (int) starY[i], (int) starSize[i], (int) starSize[i]);
        }

        drawTitle(g2);

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(200, 220, 255, 220));
        String who = "Player: " + PlayerProgress.getCurrentPlayerName();
        FontMetrics wfm = g2.getFontMetrics();
        g2.drawString(who, (WIDTH - wfm.stringWidth(who)) / 2, HEIGHT / 2 - cardH / 2 - 110);

        drawSummaryBar(g2);

        for (int i = 0; i < 3; i++) drawLevelCard(g2, i);

        drawBackButton(g2);

        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(120, 140, 180, 160));
        String hint = "Progress is saved automatically to a file after every completed word";
        FontMetrics hfm = g2.getFontMetrics();
        g2.drawString(hint, (WIDTH - hfm.stringWidth(hint)) / 2, HEIGHT - 30);
    }

    void drawTitle(Graphics2D g2) {
        String title = "YOUR  PROGRESS";
        float glow = (float) (0.5 + 0.5 * Math.sin(glowPhase));

        g2.setFont(new Font("kalpurush", Font.BOLD, 60));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (WIDTH - fm.stringWidth(title)) / 2;
        int ty = HEIGHT / 2 - cardH / 2 - 140;

        for (int i = 5; i >= 1; i--) {
            float alpha = (0.04f + 0.04f * glow) * i;
            g2.setColor(new Color(100, 180, 255, (int) (alpha * 255)));
            g2.drawString(title, tx - i * 2, ty + i * 2);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 60));
        fm = g2.getFontMetrics();
        tx = (WIDTH - fm.stringWidth(title)) / 2;

        GradientPaint gp = new GradientPaint(tx, ty - 50, new Color(255, 240, 120),
                tx, ty, new Color(220, 160, 40));
        g2.setPaint(gp);
        g2.drawString(title, tx, ty);
    }

    void drawSummaryBar(Graphics2D g2) {
        int barW = 620, barH = 64;
        int bx = WIDTH / 2 - barW / 2;
        int by = HEIGHT / 2 - cardH / 2 - 70;

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(bx + 4, by + 6, barW, barH, 16, 16);

        GradientPaint gp = new GradientPaint(bx, by, new Color(20, 25, 50, 230),
                bx, by + barH, new Color(10, 12, 30, 240));
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, barW, barH, 16, 16);
        g2.setColor(new Color(120, 150, 220, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, barW, barH, 16, 16);

        int totalWords = PlayerProgress.getTotalWordsCollected();
        int gamesPlayed = PlayerProgress.getGamesPlayed();
        int totalScore  = PlayerProgress.getTotalScore();

        String[] labels = { "WORDS COLLECTED", "WORDS WON", "TOTAL SCORE" };
        String[] values = { String.valueOf(totalWords), String.valueOf(gamesPlayed), String.valueOf(totalScore) };
        int col = barW / 3;
        for (int i = 0; i < 3; i++) {
            int colCx = bx + col * i + col / 2;

            g2.setFont(new Font("Arial", Font.BOLD, 26));
            FontMetrics vfm = g2.getFontMetrics();
            g2.setColor(new Color(255, 220, 100));
            g2.drawString(values[i], colCx - vfm.stringWidth(values[i]) / 2, by + 30);

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            FontMetrics lfm = g2.getFontMetrics();
            g2.setColor(new Color(180, 200, 240, 200));
            g2.drawString(labels[i], colCx - lfm.stringWidth(labels[i]) / 2, by + 48);

            if (i < 2) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawLine(bx + col * (i + 1), by + 10, bx + col * (i + 1), by + barH - 10);
            }
        }
    }

    void drawLevelCard(Graphics2D g2, int i) {
        Color base = levelColors[i];
        int cx = cardX[i], cy = cardY[i], cw = cardW, ch = cardH, arc = 20;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(cx + 6, cy + 10, cw, ch, arc, arc);

        Color dark = base.darker().darker();
        GradientPaint cardBg = new GradientPaint(cx, cy, new Color(20, 25, 50, 230),
                cx, cy + ch, new Color(10, 12, 30, 240));
        g2.setPaint(cardBg);
        g2.fillRoundRect(cx, cy, cw, ch, arc, arc);

        GradientPaint topBand = new GradientPaint(cx, cy, base, cx, cy + 56, dark);
        g2.setPaint(topBand);
        g2.fillRoundRect(cx, cy, cw, 56, arc, arc);
        g2.fillRect(cx, cy + 40, cw, 16);

        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(cx, cy, cw, ch, arc, arc);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics lfm = g2.getFontMetrics();
        g2.setColor(new Color(255, 255, 255, 230));
        g2.drawString(levelLabels[i], cx + (cw - lfm.stringWidth(levelLabels[i])) / 2, cy + 35);

        int wordsWon   = PlayerProgress.getWordsCollected(i + 1);
        int distinct   = PlayerProgress.getDistinctWordsCollected(i + 1);

        g2.setFont(new Font("Arial", Font.BOLD, 54));
        FontMetrics bfm = g2.getFontMetrics();
        String bigNum = String.valueOf(wordsWon);
        GradientPaint numGp = new GradientPaint(0, cy + 90, new Color(255, 245, 200),
                0, cy + 140, base.brighter());
        g2.setPaint(numGp);
        g2.drawString(bigNum, cx + (cw - bfm.stringWidth(bigNum)) / 2, cy + 140);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics sfm = g2.getFontMetrics();
        String sub = "words collected";
        g2.setColor(new Color(200, 210, 235, 200));
        g2.drawString(sub, cx + (cw - sfm.stringWidth(sub)) / 2, cy + 160);

        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 90));
        g2.drawLine(cx + 24, cy + 172, cx + cw - 24, cy + 172);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics dfm = g2.getFontMetrics();
        String distinctTxt = distinct + " unique word" + (distinct == 1 ? "" : "s") + " mastered";
        g2.setColor(new Color(180, 220, 255, 210));
        g2.drawString(distinctTxt, cx + (cw - dfm.stringWidth(distinctTxt)) / 2, cy + 195);
    }

    void drawBackButton(Graphics2D g2) {
        int bw = 160, bh = 46;
        int bx = 40, by = 40;
        btnBack.setBounds(bx, by, bw, bh);

        Color base = hoverBack ? new Color(80, 100, 160) : new Color(50, 60, 100);
        GradientPaint gp = new GradientPaint(bx, by, base.brighter(), bx, by + bh, base.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(255, 255, 255, hoverBack ? 120 : 60));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "<  MENU";
        g2.setColor(Color.WHITE);
        g2.drawString(txt, bx + (bw - fm.stringWidth(txt)) / 2, by + bh / 2 + fm.getAscent() / 2 - 3);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean prevBack = hoverBack;
        hoverBack = btnBack.contains(e.getPoint());
        if (prevBack != hoverBack) setCursor(
                hoverBack ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (btnBack.contains(e.getPoint()) && onBack != null) {
            stop();
            onBack.run();
        }
    }

    @Override public void mouseDragged(MouseEvent e)  {}
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}