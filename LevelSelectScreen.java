import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LevelSelectScreen extends JPanel implements MouseListener, MouseMotionListener {

    int WIDTH, HEIGHT;
    String[] levelLabels = { "LEVEL  1", "LEVEL  2", "LEVEL  3" };
    String[] difficulty  = { "Easy", "Medium", "Hard" };
    Color[]  levelColors = {
            new Color(60,  180, 100),
            new Color(60,  140, 220),
            new Color(200, 160,  40),
    };
    int cardW = 260, cardH = 300;
    int[] cardX = new int[3];
    int[] cardY = new int[3];

    int hoveredCard = -1;

    Rectangle btnProgress = new Rectangle();
    boolean   hoverProgress = false;

    Rectangle btnSwitchPlayer = new Rectangle();
    boolean   hoverSwitchPlayer = false;

    /** Set by Main.java; called when the user clicks the PROGRESS button. */
    Runnable onViewProgress;
    /** Set by Main.java; called when the user clicks the switch-player badge. */
    Runnable onSwitchPlayer;

    LevelSelectListener listener;

    float[] starX, starY, starSpeed, starSize;
    int STAR_COUNT = 100;

    float glowPhase = 0f;

    Timer animTimer;

    public interface LevelSelectListener {
        void onLevelSelected(int level, String word);
    }

    public LevelSelectScreen(LevelSelectListener listener) {
        this.listener = listener;

        WIDTH  = Toolkit.getDefaultToolkit().getScreenSize().width;
        HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        addMouseListener(this);
        addMouseMotionListener(this);

        SoundManager.playMusic("menu_music.wav", 1.0f);

        initStars();
        layoutCards();

        animTimer = new Timer(16, e -> {
            updateStars();
            glowPhase += 0.03f;
            if (glowPhase > Math.PI * 2) glowPhase -= (float)(Math.PI * 2);
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
            starX[i]     = (float)(Math.random() * 1920);
            starY[i]     = (float)(Math.random() * 1080);
            starSpeed[i] = 0.2f + (float)(Math.random() * 0.5f);
            starSize[i]  = 1f + (float)(Math.random() * 2.5f);
        }
    }

    void updateStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > HEIGHT) {
                starY[i] = 0;
                starX[i] = (float)(Math.random() * WIDTH);
            }
        }
    }

    void layoutCards() {
        int gap    = 40;
        int totalW = 3 * cardW + 2 * gap;
        int startX = (WIDTH - totalW) / 2;
        int centerY = HEIGHT / 2 - cardH / 2 + 40;
        for (int i = 0; i < 3; i++) {
            cardX[i] = startX + i * (cardW + gap);
            cardY[i] = centerY;
        }
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
            g2.fillOval((int)starX[i], (int)starY[i], (int)starSize[i], (int)starSize[i]);
        }

        drawTitle(g2);
        drawPlayerBadge(g2);

        g2.setFont(new Font("kalpurush", Font.PLAIN, 25));
        g2.setColor(new Color(160, 200, 255, 200));
        String sub = "Collect letters in order to spell the word — choose your level";
        FontMetrics sfm = g2.getFontMetrics();
        g2.drawString(sub, (WIDTH - sfm.stringWidth(sub)) / 2, HEIGHT / 2 - cardH / 2 - 10);

        for (int i = 0; i < 3; i++) {
            drawCard(g2, i);
        }

        drawProgressButton(g2);

        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setColor(new Color(120, 140, 180, 160));
        String hint = "Click a level to begin   •   Arrow keys / Space to move & jump";
        FontMetrics hfm = g2.getFontMetrics();
        g2.drawString(hint, (WIDTH - hfm.stringWidth(hint)) / 2, HEIGHT - 30);
    }

    void drawPlayerBadge(Graphics2D g2) {
        String name = PlayerProgress.getCurrentPlayerName();
        String label = "PLAYER-  " + name.toUpperCase();

        g2.setFont(new Font("Kalpurush", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int padX = 14, padY = 10;
        int labelW = fm.stringWidth(label);

        int bx = 30, by = 26;
        int bh = fm.getHeight() + padY;
        int bw = labelW + padX * 2 + 96;

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(bx, by, bw, bh, 14, 14);
        g2.setColor(new Color(120, 150, 220, 100));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(bx, by, bw, bh, 14, 14);

        g2.setColor(new Color(220, 235, 255));
        g2.drawString(label, bx + padX, by + bh / 2 + fm.getAscent() / 2 - 3);

        int swW = 84, swH = bh - 8;
        int swX = bx + bw - swW - 6;
        int swY = by + 4;
        btnSwitchPlayer.setBounds(swX, swY, swW, swH);

        g2.setColor(hoverSwitchPlayer ? new Color(90, 110, 170) : new Color(55, 65, 105));
        g2.fillRoundRect(swX, swY, swW, swH, 10, 10);
        g2.setColor(new Color(255, 255, 255, hoverSwitchPlayer ? 130 : 70));
        g2.drawRoundRect(swX, swY, swW, swH, 10, 10);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics swfm = g2.getFontMetrics();
        String swTxt = "SWITCH";
        g2.setColor(Color.WHITE);
        g2.drawString(swTxt, swX + (swW - swfm.stringWidth(swTxt)) / 2, swY + swH / 2 + swfm.getAscent() / 2 - 3);
    }

    void drawTitle(Graphics2D g2) {
        String title = "ALPHA  QUEST";
        float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase));

        g2.setFont(new Font("kalpurush", Font.BOLD, 72));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (WIDTH - fm.stringWidth(title)) / 2;
        int ty = HEIGHT / 2 - cardH / 2 - 80;

        for (int i = 5; i >= 1; i--) {
            float alpha = (0.04f + 0.04f * glow) * i;
            g2.setColor(new Color(100, 180, 255, (int)(alpha * 255)));
            g2.drawString(title, tx - i*2, ty + i*2);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 72));
        fm = g2.getFontMetrics();
        tx = (WIDTH - fm.stringWidth(title)) / 2;

        GradientPaint gp = new GradientPaint(tx, ty - 60, new Color(255, 240, 120),
                tx, ty, new Color(220, 160, 40));
        g2.setPaint(gp);
        g2.drawString(title, tx, ty);
    }

    void drawCard(Graphics2D g2, int i) {
        boolean hovered = (i == hoveredCard);
        Color base = levelColors[i];
        int   cx   = cardX[i];
        int   cy   = cardY[i] - (hovered ? 12 : 0);
        int   cw   = cardW;
        int   ch   = cardH;
        int   arc  = 20;

        g2.setColor(new Color(0, 0, 0, hovered ? 120 : 70));
        g2.fillRoundRect(cx + 6, cy + 10, cw, ch, arc, arc);


        Color dark = base.darker().darker();
        GradientPaint cardBg = new GradientPaint(cx, cy, new Color(20, 25, 50, 230),
                cx, cy + ch, new Color(10, 12, 30, 240));
        g2.setPaint(cardBg);
        g2.fillRoundRect(cx, cy, cw, ch, arc, arc);


        GradientPaint topBand = new GradientPaint(cx, cy, base, cx, cy + 80, dark);
        g2.setPaint(topBand);
        g2.fillRoundRect(cx, cy, cw, 80, arc, arc);
        g2.fillRect(cx, cy + 60, cw, 22);


        if (hovered) {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 200));
            g2.setStroke(new BasicStroke(2.5f));
        } else {
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 80));
            g2.setStroke(new BasicStroke(1.5f));
        }
        g2.drawRoundRect(cx, cy, cw, ch, arc, arc);


        g2.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics nfm = g2.getFontMetrics();
        String lvlTxt = String.valueOf(i + 1);
        g2.setColor(new Color(255, 255, 255, 220));
        g2.drawString(lvlTxt, cx + (cw - nfm.stringWidth(lvlTxt)) / 2, cy + 52);

        // Level label
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics lfm = g2.getFontMetrics();
        g2.setColor(new Color(220, 235, 255, 210));
        g2.drawString(levelLabels[i], cx + (cw - lfm.stringWidth(levelLabels[i])) / 2, cy + 105);


        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(cx + 20, cy + 118, cx + cw - 20, cy + 118);


        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics wfm = g2.getFontMetrics();
        String badge = "  RANDOM  WORD  ";
        int badgeW = wfm.stringWidth(badge) + 16;
        int badgeBx = cx + (cw - badgeW) / 2;
        int badgeBy = cy + 134;
        g2.setColor(new Color(120, 60, 0, 120));
        g2.fillRoundRect(badgeBx, badgeBy, badgeW, 22, 8, 8);
        g2.setColor(new Color(255, 200, 60, 200));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(badgeBx, badgeBy, badgeW, 22, 8, 8);
        g2.setColor(new Color(255, 220, 80));
        g2.drawString(badge, badgeBx + 8, badgeBy + 15);


        String[] ranges = { "8–9 letters", "10–12 letters", "13–16 letters" };
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics hfm2 = g2.getFontMetrics();
        g2.setColor(new Color(180, 210, 255, 180));
        g2.drawString(ranges[i], cx + (cw - hfm2.stringWidth(ranges[i])) / 2, cy + 172);


        drawDiffBadge(g2, i, cx, cy, cw);


        drawPlayButton(g2, base, cx, cy, cw, ch, hovered);
    }

    void drawDiffBadge(Graphics2D g2, int i, int cx, int cy, int cw) {
        String diff = difficulty[i];
        Color  dc   = diff.equals("Easy")   ? new Color(60, 200, 100) :
                diff.equals("Medium") ? new Color(220, 180, 40) :
                        new Color(220, 80, 80);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics dfm = g2.getFontMetrics();
        int bw = dfm.stringWidth(diff) + 16;
        int bh = 20;
        int bx = cx + (cw - bw) / 2;
        int by = cy + 188;

        g2.setColor(new Color(dc.getRed(), dc.getGreen(), dc.getBlue(), 50));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(new Color(dc.getRed(), dc.getGreen(), dc.getBlue(), 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(dc);
        g2.drawString(diff, bx + 8, by + 14);
    }

    void drawPlayButton(Graphics2D g2, Color base, int cx, int cy, int cw, int ch, boolean hovered) {
        int bw = cw - 40;
        int bh = 42;
        int bx = cx + 20;
        int by = cy + ch - bh - 18;

        Color btnCol = hovered ? base.brighter() : base;
        GradientPaint btnGp = new GradientPaint(bx, by, btnCol.brighter(),
                bx, by + bh, btnCol.darker());
        g2.setPaint(btnGp);
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);

        g2.setColor(new Color(255, 255, 255, hovered ? 120 : 60));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics pfm = g2.getFontMetrics();
        String playTxt = hovered ? " PLAY NOW" : " PLAY";
        g2.setColor(Color.WHITE);
        g2.drawString(playTxt, bx + (bw - pfm.stringWidth(playTxt)) / 2, by + bh / 2 + pfm.getAscent() / 2 - 2);
    }

    void drawProgressButton(Graphics2D g2) {
        int bw = 220, bh = 46;
        int bx = WIDTH / 2 - bw / 2;
        int by = cardY[0] + cardH + 30;
        btnProgress.setBounds(bx, by, bw, bh);

        Color base = hoverProgress ? new Color(90, 110, 170) : new Color(55, 65, 105);
        GradientPaint gp = new GradientPaint(bx, by, base.brighter(), bx, by + bh, base.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, bw, bh, 14, 14);

        g2.setColor(new Color(255, 255, 255, hoverProgress ? 130 : 70));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 14, 14);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "MY PROGRESS";
        g2.setColor(Color.WHITE);
        g2.drawString(txt, bx + (bw - fm.stringWidth(txt)) / 2, by + bh / 2 + fm.getAscent() / 2 - 3);
    }

    boolean isInsideCard(int mx, int my, int i) {
        int cy = cardY[i] - (i == hoveredCard ? 12 : 0);
        return mx >= cardX[i] && mx <= cardX[i] + cardW &&
                my >= cy       && my <= cy + cardH;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int prev = hoveredCard;
        hoveredCard = -1;
        for (int i = 0; i < 3; i++) {
            if (isInsideCard(e.getX(), e.getY(), i)) {
                hoveredCard = i;
                break;
            }
        }

        boolean prevProgress = hoverProgress;
        hoverProgress = btnProgress.contains(e.getPoint());

        boolean prevSwitch = hoverSwitchPlayer;
        hoverSwitchPlayer = btnSwitchPlayer.contains(e.getPoint());

        if (hoveredCard != prev || hoverProgress != prevProgress || hoverSwitchPlayer != prevSwitch) setCursor(
                (hoveredCard >= 0 || hoverProgress || hoverSwitchPlayer)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor()
        );
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (btnSwitchPlayer.contains(e.getPoint())) {
            if (onSwitchPlayer != null) {
                animTimer.stop();
                onSwitchPlayer.run();
            }
            return;
        }

        if (btnProgress.contains(e.getPoint())) {
            if (onViewProgress != null) {
                animTimer.stop();
                onViewProgress.run();
            }
            return;
        }

        for (int i = 0; i < 3; i++) {
            if (isInsideCard(e.getX(), e.getY(), i)) {
                animTimer.stop();
                String word = WordDictionary.pickWord(i + 1);
                listener.onLevelSelected(i + 1, word);
                return;
            }
        }
    }

    @Override public void mouseDragged(MouseEvent e)  {}
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}