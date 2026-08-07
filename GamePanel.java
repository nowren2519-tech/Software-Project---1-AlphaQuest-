import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements java.awt.event.KeyListener, MouseListener, Runnable {

    Image background;
    Player player;
    Thread gameThread;
    int WIDTH, HEIGHT;

    ArrayList<LetterObject> letterObjects = new ArrayList<>();

    WordManager wordManager;
    LetterBox letterBox;

    String targetWord;
    char[] targetLetters;
    int currentLevel = 1;

    String gameState = "memorize";
    int memorizeTimer        = 0;
    final int MEMORIZE_TICKS = 300;
    float memorizeAlpha      = 1.0f;

    int countdownTicks = 60* 60;
    boolean timerStarted = false;
    int Score = 0;
    final int POINTS_PER_LETTER = 10;

    ArrayList<Asteroid> asteroids = new ArrayList<>();
    int asteroidCount = 4;
    int penaltyFlashTicks = 0;
    int penaltySeconds    = 5;
    int invincibleTicks   = 0;// bujhi nai


    FreezeMode freezeMode = new FreezeMode();
    Confettieffect confetti = new Confettieffect();

    String  wordInfoText     = "Loading...";
    String  wordSynonyms     = " ";
    float   winPanelAlpha    = 0f;

    Image gameOverBg;

    Runnable onBackToMenu;

    Rectangle btnRestart = new Rectangle();
    Rectangle btnMenu    = new Rectangle();
    Rectangle btnNewWord = new Rectangle();


    Rectangle btnWinPlayAgain = new Rectangle();
    Rectangle btnWinLevelMenu = new Rectangle();

    Rectangle btnTimesupTryAgain  = new Rectangle();
    Rectangle btnTimesupLevelMenu = new Rectangle();

    Font bigFont   = new Font("Kalpurush", Font.BOLD, 72);
    Font medFont   = new Font("Kalpurush", Font.BOLD, 36);
    Font smallFont = new Font("Kalpurush", Font.PLAIN, 20);

    public GamePanel(String word, int level) {
        this.targetWord    = word.toLowerCase();
        this.currentLevel  = level;
        this.targetLetters = new char[targetWord.length()];
        for (int i = 0; i < targetWord.length(); i++)
            targetLetters[i] = targetWord.charAt(i);

        WIDTH  = Toolkit.getDefaultToolkit().getScreenSize().width;
        HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        background  = BackgroundManager.getBackgroundForWord(targetWord);
        this.setBackground(Color.BLACK);

        player      = new Player(this);
        wordManager = new WordManager(targetWord);
        letterBox   = new LetterBox(targetWord, WIDTH, HEIGHT);

        addKeyListener(this);
        addMouseListener(this);
        spawnLetterObjects();
        spawnAsteroids();
        freezeAll(true);
        startGameLoop();
    }

    void freezeAll(boolean frozen) {

        for (LetterObject lo : letterObjects) lo.setFrozen(frozen);
    }

    void spawnLetterObjects() {
        letterObjects.clear();
        Random rand  = new Random();
        int count    = targetLetters.length;
        int sectionW = WIDTH / count;
        PlanetShape shape = PlanetShape.forLevel(currentLevel);
        for (int i = 0; i < count; i++) {
            int CenterX = sectionW*i + sectionW/2;
            int randomOffset = rand.nextInt(sectionW/2)-sectionW/4;
            int startX = CenterX + randomOffset;
            int startY = 80 + rand.nextInt((int)(HEIGHT * 0.55));
            letterObjects.add(new LetterObject(
                    startX, startY, targetLetters[i], i, WIDTH, HEIGHT, shape));
        }
    }

    void spawnAsteroids() {
        asteroids.clear();
        for (int i = 0; i < asteroidCount; i++)
            asteroids.add(new Asteroid(WIDTH, HEIGHT));
    }

    void applySequenceState() {
        int idx = wordManager.getCurrentIndex();
        for (int i = 0; i < letterObjects.size(); i++)
            letterObjects.get(i).setFrozen(i < idx);
    }

    public void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (true) {
            update();
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    void update() {
        if (gameState.equals("memorize")) {
            memorizeTimer++;
            if (memorizeTimer >= MEMORIZE_TICKS - 60)
                memorizeAlpha = Math.max(0f, 1f - (memorizeTimer - (MEMORIZE_TICKS - 60)) / 60f);
            if (memorizeTimer >= MEMORIZE_TICKS) {
                gameState     = "playing";
                memorizeAlpha = 0f;
                timerStarted  = true;
                freezeAll(false);
            }
            return;
        }

        if (gameState.equals("playing")) {
            if (timerStarted && countdownTicks > 0 && !freezeMode.isActive()) {
                countdownTicks--;
                if (countdownTicks <= 0) {
                    gameState = "timesup";
                    return;
                }
            }

            freezeMode.update();
            if (!freezeMode.isActive())
                applySequenceState();

            player.update();
            for (LetterObject lo : letterObjects) {
                if (lo.isCollected())
                    lo.update();
                else if (!freezeMode.isActive())
                    lo.update();
            }
            if (invincibleTicks > 0)
                invincibleTicks--;
            if (penaltyFlashTicks > 0)
                penaltyFlashTicks--;
            for (Asteroid a : asteroids) {
                if (!freezeMode.isActive()) {
                    a.update(WIDTH, HEIGHT);
                    if (invincibleTicks == 0 &&
                            a.collidesWith(player.x, player.y, player.width, player.height)) {
                        SoundManager.playHit();
                        a.triggerHit();
                        countdownTicks    = Math.max(0, countdownTicks - penaltySeconds * 60);
                        Score             = Math.max(0, Score - 5);
                        penaltyFlashTicks = 45;
                        invincibleTicks   = 90;
                        if (countdownTicks <= 0) {
                            SoundManager.playTimesUp();
                            gameState = "timesup";
                            return;
                        }
                    }
                }
            }
            int cidx = wordManager.getCurrentIndex();
            if (cidx < letterObjects.size()) {
                LetterObject obj = letterObjects.get(cidx);
                if (!obj.isCollected() &&
                        obj.collidesWith(player.x, player.y, player.width, player.height))
                    handleLetterCollect(obj, cidx);
            }
            letterBox.update();
            if (wordManager.isWordComplete()) triggerWin();
            return;
        }


        if (gameState.equals("win")) {
            if (winPanelAlpha < 1f) winPanelAlpha = Math.min(1f, winPanelAlpha + 0.03f);
            confetti.update();
        }
    }

    void triggerWin() {
        gameState     = "win";
        SoundManager.playWin();
        SoundManager.playCelebration();
        winPanelAlpha = 0f;
        wordInfoText  = "Loading...";

        PlayerProgress.recordWordCollected(currentLevel, targetWord, Score);

        int cardW = 900, cardH = 600;
        int cardX = WIDTH / 2 - cardW / 2;
        int cardY = HEIGHT / 2 - cardH / 2;
        confetti.spawn(WIDTH, HEIGHT, cardX, cardW, cardY, cardH);

        WordInfoFetcher.fetch(targetWord, (desc, syns) -> {
            wordInfoText = desc;
            repaint();
        });
    }

    void handleLetterCollect(LetterObject obj, int idx) {
        String result = wordManager.checkAndCollect(obj.getLetter());
        if (result.equals("correct")) {
            SoundManager.playCollect();
            Score += POINTS_PER_LETTER;
            obj.collect();
            letterBox.addLetter(obj.getLetter());
            applySequenceState();
            freezeMode.onLetterCollected(wordManager.getCurrentIndex());
            SoundManager.playFreeze();
        }
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        g.drawImage(background, 0, 0, WIDTH, HEIGHT, null);

        if (gameState.equals("memorize")) {
            drawMemorizeOverlay(g2);
            return;
        }

        for (LetterObject obj : letterObjects)
            obj.draw(g);
        for (Asteroid a : asteroids)
            a.draw(g);
        player.draw(g);
        letterBox.draw(g);
        drawTimer(g2);
        drawScore(g2);
        if (freezeMode.isActive())
            freezeMode.drawOverlay(g2, WIDTH, HEIGHT, asteroids);
        if (freezeMode.isBannerShowing())
            freezeMode.drawBanner(g2, WIDTH, HEIGHT);

        if (penaltyFlashTicks > 0) {//
            float alpha = (penaltyFlashTicks / 45f) * 0.45f;
            g2.setColor(new Color(1f, 0f, 0f, alpha));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setFont(new Font("Kalpurush", Font.BOLD, 70));
            g2.setColor(new Color(0, 51, 0,
                    (int)(Math.min(1f, penaltyFlashTicks / 20f) * 255)));
            String pen = "-" + penaltySeconds + "sec!";
            FontMetrics pfm = g2.getFontMetrics();
            g2.drawString(pen, WIDTH/2 - pfm.stringWidth(pen)/2, HEIGHT/2);
        }

        if (gameState.equals("win"))     drawWin(g2);
        if (gameState.equals("timesup")) drawTimesUp(g2);
    }


    void drawMemorizeOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);


        g2.setFont(new Font("Kalpurush", Font.BOLD, 90));
        String upper = targetWord.toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        int wordPx = fm.stringWidth(upper);
        int cardW  = Math.max(800, wordPx + 80);
        int cardH  = 260;
        int cardX  = WIDTH / 2 - cardW / 2;
        int cardY  = HEIGHT / 2 - cardH / 2;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, memorizeAlpha));

        g2.setColor(new Color(10, 15, 40, 230));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 90, 90);
        g2.setColor(new Color(153, 51, 102, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 90, 90);

        g2.setFont(new Font("Kalpurush", Font.BOLD, 38));
        g2.setColor(new Color(204, 204, 255));
        fm = g2.getFontMetrics();
        String label = "MEMORIZE THIS WORD";
        g2.drawString(label, WIDTH / 2 - fm.stringWidth(label) / 2, cardY + 50);

        g2.setFont(new Font("Kalpurush", Font.BOLD, 80));
        fm = g2.getFontMetrics();
        g2.setColor(new Color(255, 200, 0, 60));
        g2.drawString(upper, WIDTH / 2 - fm.stringWidth(upper) / 2 + 3, cardY + 160 + 3);
        GradientPaint gp = new GradientPaint(0, cardY + 90,  new Color(255, 240, 100),
                0, cardY + 170, new Color(220, 140, 20));
        g2.setPaint(gp);
        g2.drawString(upper, WIDTH / 2 - fm.stringWidth(upper) / 2, cardY + 160);

        int secsLeft = (int) Math.ceil((MEMORIZE_TICKS - memorizeTimer) / 60.0);
        secsLeft = Math.max(0, secsLeft);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(200, 200, 200));
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < secsLeft; i++) dots.append("● ");
        String dotsStr = dots.toString().trim();
        fm = g2.getFontMetrics();
        g2.drawString(dotsStr, WIDTH / 2 - fm.stringWidth(dotsStr) / 2, cardY + cardH - 22);
    }

    void drawTimer(Graphics2D g2) {
        int secsLeft = countdownTicks / 60;
        boolean frozen = freezeMode.isActive();
        boolean urgent = secsLeft < 10 && !frozen;
        String timeStr = String.format("%d", secsLeft);
        String displayStr = frozen ? timeStr + "s  |  FROZEN" : timeStr + "s";

        g2.setFont(new Font("Times New Roman", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth("  " + displayStr + "  ") + 28;
        int bx = 15, by = 10;
        g2.setColor(frozen ? freezeMode.getTimerBgColor()
                : (urgent ? new Color(180, 30, 30, 200) : new Color(51, 153, 102, 190)));
        g2.fillRoundRect(bx, by, bw, 38, 15, 15);

        g2.setColor(frozen ? freezeMode.getTimerBorderColor()
                : (urgent ? new Color(255, 80, 80, 200) : new Color(255, 255, 205, 200)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx, by, bw, 38, 15, 15);

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(frozen ? new Color(150, 230, 255) : new Color(0, 51, 0));
        g2.drawString(frozen ? "*" : "T", bx + 10, by + 26);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(frozen ? freezeMode.getTimerTextColor()
                : (urgent ? new Color(255, 100, 100) : new Color(255, 230, 80)));
        g2.drawString(timeStr + "s", bx + 30, by + 27);

        if (frozen) {
            int tx = bx + 30 + fm.stringWidth(timeStr + "s") + 12;
            g2.setColor(new Color(100, 200, 255, 80));
            g2.fillRoundRect(tx - 4, by + 8, fm.stringWidth("FROZEN") + 8, 22, 6, 6);
            g2.setFont(new Font("Kalpurush", Font.BOLD, 13));
            g2.setColor(new Color(150, 230, 255));
            g2.drawString("FROZEN", tx, by + 24);
        }


        int gap   = 10;
        int btnH  = 38;
        int btnY  = by;
        int startX = bx + bw + gap;

        g2.setFont(new Font("Kalpurush", Font.BOLD, 12));
        fm = g2.getFontMetrics();

        String[] labels = { "\u27F3 Restart", "\u2302 Menu", "\u2606 New Word" };
        Color[]  colors = {
                new Color(220, 100,  80),
                new Color( 80, 150, 220),
                new Color( 80, 200, 120),
        };
        Rectangle[] bounds = { btnRestart, btnMenu, btnNewWord };


        int curX = startX;
        for (int i = 0; i < 3; i++) {
            int btnW = fm.stringWidth(labels[i]) + 22;
            Color c  = colors[i];

            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 55));
            g2.fillRoundRect(curX, btnY, btnW, btnH, 8, 8);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(curX, btnY, btnW, btnH, 8, 8);
            g2.setColor(new Color(255, 255, 255, 210));
            g2.drawString(labels[i], curX + 11, btnY + btnH / 2 + fm.getAscent() / 2 - 2);

            bounds[i].setBounds(curX, btnY, btnW, btnH);//
            curX += btnW + gap;
        }
    }
    void drawScore(Graphics2D g2) {

        String text = "SCORE   " + Score;

        g2.setFont(new Font("Kalpurush", Font.BOLD, 18));

        FontMetrics fm = g2.getFontMetrics();

        int w = fm.stringWidth(text) + 35;
        int h = 40;

        int x = WIDTH - w - 1420;
        int y = 12;

        g2.setColor(new Color(150,150,150,100));
        g2.fillRoundRect(x,y,w,h,18,18);

        g2.setColor(new Color(65,180,255));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x,y,w,h,18,18);

        g2.setColor(new Color(255,255,255));
        g2.drawString(text,x+10,y+25);

    }
    void drawWin(Graphics2D g2) {

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        winPanelAlpha));


        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        confetti.draw(g2);



        int cardW = 900;
        int cardH = 600;

        int cardX = WIDTH / 2 - cardW / 2;
        int cardY = HEIGHT / 2 - cardH / 2;

        RoundRectangle2D.Float card =
                new RoundRectangle2D.Float(
                        cardX,
                        cardY,
                        cardW,
                        cardH,
                        35,
                        35);



        if (gameOverBg != null) {

            Shape old = g2.getClip();

            g2.setClip(card);

            g2.drawImage(
                    gameOverBg,
                    cardX,
                    cardY,
                    cardW,
                    cardH,
                    null);

            g2.setClip(old);
        }



        GradientPaint bgGradient =
                new GradientPaint(
                        cardX,
                        cardY,
                        new Color(12, 22, 42, 235),

                        cardX,
                        cardY + cardH,
                        new Color(18, 32, 58, 235));

        g2.setPaint(bgGradient);
        g2.fill(card);



        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(70, 150, 255));
        g2.draw(card);

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(255,255,255,40));
        g2.draw(new RoundRectangle2D.Float(
                cardX+4,
                cardY+4,
                cardW-8,
                cardH-8,
                30,
                30));

        //====================================================
        // Trophy
        //====================================================

        g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,44));

        String trophy="🏆";

        FontMetrics fm=g2.getFontMetrics();

        int tx=cardX+(cardW-fm.stringWidth(trophy))/2;

        g2.drawString(trophy,tx,cardY+60);

        //====================================================
        // TITLE
        //====================================================

        Font titleFont=new Font("Segoe UI",Font.BOLD,54);

        g2.setFont(titleFont);

        String title="YOU WIN!";

        fm=g2.getFontMetrics();

        int titleX=cardX+(cardW-fm.stringWidth(title))/2;

        int titleY=cardY+120;

        g2.setColor(new Color(0,0,0,150));
        g2.drawString(title,titleX+3,titleY+3);

        GradientPaint gp=
                new GradientPaint(
                        0,
                        titleY-50,
                        new Color(255,255,255),

                        0,
                        titleY,
                        new Color(255,215,80));

        g2.setPaint(gp);
        g2.drawString(title,titleX,titleY);

        //====================================================
        // Divider
        //====================================================

        g2.setColor(new Color(0,220,170));

        g2.setStroke(new BasicStroke(2));

        g2.drawLine(
                cardX+120,
                cardY+145,
                cardX+cardW-120,
                cardY+145);

        //====================================================
        // SCORE BOX
        //====================================================

        int scoreX=cardX+40;
        int scoreY=cardY+175;
        int boxW=250;
        int boxH=90;

        g2.setColor(new Color(18,45,42,220));
        g2.fillRoundRect(scoreX,scoreY,boxW,boxH,20,20);

        g2.setColor(new Color(55,180,120));
        g2.drawRoundRect(scoreX,scoreY,boxW,boxH,20,20);

        g2.setFont(new Font("Segoe UI",Font.BOLD,18));
        g2.setColor(Color.WHITE);
        g2.drawString("SCORE",scoreX+20,scoreY+28);

        g2.setFont(new Font("Segoe UI",Font.BOLD,38));
        g2.setColor(new Color(90,255,140));
        g2.drawString(String.valueOf(Score),
                scoreX+20,
                scoreY+70);

        //====================================================
        // WORD BOX
        //====================================================

        int wordX=cardX+cardW-290;
        int wordY=scoreY;

        g2.setColor(new Color(20,36,70,220));
        g2.fillRoundRect(wordX,wordY,250,90,20,20);

        g2.setColor(new Color(70,170,255));
        g2.drawRoundRect(wordX,wordY,250,90,20,20);

        g2.setFont(new Font("Segoe UI",Font.BOLD,17));
        g2.setColor(Color.WHITE);
        g2.drawString("WORD COLLECTED",wordX+18,wordY+28);

        g2.setFont(new Font("Segoe UI",Font.BOLD,26));
        g2.setColor(new Color(255,220,90));
        g2.drawString(targetWord.toUpperCase(),
                wordX+18,
                wordY+68);

        //====================================================
        // PART-2 এখান থেকে শুরু হবে
        // WORD INFO PANEL
        //====================================================
        int infoX = cardX + 40;
        int infoY = cardY + 290;
        int infoW = cardW - 80;
        int infoH = 200;

        // Panel Background
        g2.setColor(new Color(15, 28, 45, 220));
        g2.fillRoundRect(infoX, infoY, infoW, infoH, 20, 20);

        // Border
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(60, 180, 255));
        g2.drawRoundRect(infoX, infoY, infoW, infoH, 20, 20);

        //----------------------------------------------------
        // Header
        //----------------------------------------------------

        GradientPaint infoHeader =
                new GradientPaint(
                        infoX,
                        infoY,
                        new Color(32, 70, 115),

                        infoX,
                        infoY + 40,
                        new Color(20, 48, 80));

        g2.setPaint(infoHeader);
        g2.fillRoundRect(infoX, infoY, infoW, 42, 20, 20);

        // Bottom line
        g2.setColor(new Color(0,220,170));
        g2.drawLine(infoX + 15,
                infoY + 42,
                infoX + infoW - 15,
                infoY + 42);

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        g2.drawString("WORD INFO", infoX + 20, infoY + 28);

        //----------------------------------------------------
        // Description
        //----------------------------------------------------

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g2.setColor(new Color(235,235,235));

        fm = g2.getFontMetrics();

        drawWrappedText(
                g2,
                wordInfoText,
                infoX + 20,
                infoY + 75,
                infoW - 40,
                fm.getHeight() + 6);

        //====================================================
        // Bottom Divider
        //====================================================

        int dividerY = cardY + cardH - 90;

        g2.setColor(new Color(255,255,255,40));
        g2.drawLine(
                cardX + 30,
                dividerY,
                cardX + cardW - 30,
                dividerY);

        //====================================================
        // BUTTONS
        //====================================================

        int buttonY = dividerY + 22;

        btnWinPlayAgain =
                drawModernButton(
                        g2,
                        cardX + 150,
                        buttonY,
                        220,
                        52,
                        "PLAY AGAIN",
                        new Color(46,139,87),
                        new Color(65,180,120));

        btnWinLevelMenu =
                drawModernButton(
                        g2,
                        cardX + cardW - 370,
                        buttonY,
                        220,
                        52,
                        "HOME",
                        new Color(52,102,204),
                        new Color(82,150,255));

        //====================================================
        // Reset Alpha
        //====================================================

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        1f));
    }

    private Rectangle drawModernButton(Graphics2D g2,
                                       int x,
                                       int y,
                                       int w,
                                       int h,
                                       String text,
                                       Color bg,
                                       Color border) {

        GradientPaint gp = new GradientPaint(
                x,
                y,
                bg.brighter(),
                x,
                y + h,
                bg.darker());

        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 18, 18);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(border);
        g2.drawRoundRect(x, y, w, h, 18, 18);

        // Inner Highlight
        g2.setColor(new Color(255, 255, 255, 35));
        g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 16, 16);

        // Top Shine
        g2.setColor(new Color(255,255,255,25));
        g2.fillRoundRect(x + 2, y + 2, w - 4, h / 2, 16, 16);

        Font font = new Font("Segoe UI", Font.BOLD, 20);
        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();

        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + ((h - fm.getHeight()) / 2) + fm.getAscent();

        g2.setColor(new Color(0,0,0,120));
        g2.drawString(text, tx + 2, ty + 2);

        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);

        return new Rectangle(x, y, w, h);
    }


    void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words  = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        int maxLines = 8;
        int drawn = 0;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), x, curY);
                curY += lineH;
                drawn++;
                if (drawn >= maxLines)
                    return;
                line = new StringBuilder(w);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0 && drawn < maxLines)
            g2.drawString(line.toString(), x, curY);
    }

    void drawTimesUp(Graphics2D g2) {

        //-----------------------------
        // Background
        //-----------------------------

        g2.setColor(new Color(0,0,0,220));
        g2.fillRect(0,0,WIDTH,HEIGHT);

        int cardW = 900;
        int cardH = 560;

        int cardX = WIDTH/2-cardW/2;
        int cardY = HEIGHT/2-cardH/2;

        RoundRectangle2D.Float card =
                new RoundRectangle2D.Float(
                        cardX,
                        cardY,
                        cardW,
                        cardH,
                        30,
                        30);

        //-----------------------------
        // Background Image
        //-----------------------------

        if(gameOverBg!=null){

            Shape old=g2.getClip();

            g2.setClip(card);

            g2.drawImage(
                    gameOverBg,
                    cardX,
                    cardY,
                    cardW,
                    cardH,
                    null);

            g2.setClip(old);
        }

        //-----------------------------
        // Glass Overlay
        //-----------------------------

        GradientPaint bg =
                new GradientPaint(

                        cardX,
                        cardY,
                        new Color(10,15,28,235),

                        cardX,
                        cardY+cardH,
                        new Color(18,24,40,235));

        g2.setPaint(bg);
        g2.fill(card);

        //-----------------------------
        // Border
        //-----------------------------

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(180,60,60));
        g2.draw(card);

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(255,255,255,35));

        g2.draw(new RoundRectangle2D.Float(
                cardX+4,
                cardY+4,
                cardW-8,
                cardH-8,
                26,
                26));

        //-----------------------------
        // Skull
        //-----------------------------

        g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,40));

        String skull="☠";

        FontMetrics fm=g2.getFontMetrics();

        int sx=cardX+(cardW-fm.stringWidth(skull))/2;

        g2.drawString(skull,sx,cardY+55);

        //-----------------------------
        // GAME OVER
        //-----------------------------

        Font titleFont =
                new Font("Segoe UI",Font.BOLD,58);

        g2.setFont(titleFont);

        String title="GAME OVER";

        fm=g2.getFontMetrics();

        int tx=
                cardX+
                        (cardW-fm.stringWidth(title))/2;

        int ty=cardY+120;

        g2.setColor(new Color(0,0,0,140));
        g2.drawString(title,tx+3,ty+3);

        GradientPaint titlePaint=
                new GradientPaint(

                        0,
                        ty-50,
                        new Color(255,255,255),

                        0,
                        ty,
                        new Color(255,70,70));

        g2.setPaint(titlePaint);

        g2.drawString(title,tx,ty);

        //-----------------------------
        // Divider
        //-----------------------------

        g2.setStroke(new BasicStroke(2));

        g2.setColor(new Color(255,70,70));

        g2.drawLine(
                cardX+130,
                cardY+145,
                cardX+cardW-130,
                cardY+145);

        //-----------------------------
        // REASON PANEL
        //-----------------------------

        int reasonX=cardX+45;
        int panelY=cardY+180;

        int panelW=320;
        int panelH=90;

        g2.setColor(new Color(45,18,22));
        g2.fillRoundRect(
                reasonX,
                panelY,
                panelW,
                panelH,
                18,
                18);

        g2.setColor(new Color(200,70,70));
        g2.drawRoundRect(
                reasonX,
                panelY,
                panelW,
                panelH,
                18,
                18);

        g2.setFont(new Font("Segoe UI",Font.BOLD,18));
        g2.setColor(new Color(255,120,120));

        g2.drawString(
                "REASON",
                reasonX+20,
                panelY+28);

        g2.setFont(new Font("Segoe UI",Font.BOLD,34));
        g2.setColor(Color.WHITE);

        g2.drawString(
                "TIME'S UP!",
                reasonX+20,
                panelY+68);

        //-----------------------------
        // SCORE PANEL
        //-----------------------------

        int scoreX=cardX+cardW-365;

        g2.setColor(new Color(18,45,42));
        g2.fillRoundRect(
                scoreX,
                panelY,
                320,
                panelH,
                18,
                18);

        g2.setColor(new Color(60,200,140));
        g2.drawRoundRect(
                scoreX,
                panelY,
                320,
                panelH,
                18,
                18);

        g2.setFont(new Font("Segoe UI",Font.BOLD,18));
        g2.setColor(new Color(60,220,170));

        g2.drawString(
                "SCORE",
                scoreX+20,
                panelY+28);

        g2.setFont(new Font("Segoe UI",Font.BOLD,38));

        g2.setColor(Color.WHITE);

        g2.drawString(
                String.valueOf(Score),
                scoreX+20,
                panelY+70);

        int ansX = cardX + 45;
        int ansY = cardY + 300;
        int ansW = cardW - 90;
        int ansH = 130;

        // Panel Background
        GradientPaint ansBg = new GradientPaint(
                ansX,
                ansY,
                new Color(12, 28, 55),
                ansX,
                ansY + ansH,
                new Color(8, 20, 40));

        g2.setPaint(ansBg);
        g2.fillRoundRect(ansX, ansY, ansW, ansH, 20, 20);

        // Border
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(45,120,255));
        g2.drawRoundRect(ansX, ansY, ansW, ansH, 20, 20);

        //----------------------------------------
        // Header
        //----------------------------------------

        GradientPaint header = new GradientPaint(
                ansX,
                ansY,
                new Color(28,70,130),

                ansX,
                ansY+38,
                new Color(18,45,90));

        g2.setPaint(header);

        g2.fillRoundRect(
                ansX,
                ansY,
                ansW,
                38,
                20,
                20);

        g2.setColor(new Color(255,255,255));

        g2.setFont(new Font("Segoe UI",Font.BOLD,18));

        g2.drawString(
                "ANSWER",
                ansX+20,
                ansY+25);

        //----------------------------------------
        // Word
        //----------------------------------------

        String answer = targetWord.toUpperCase();

        g2.setFont(new Font("Segoe UI",Font.BOLD,42));

        fm = g2.getFontMetrics();

        int textX =
                ansX +
                        (ansW-fm.stringWidth(answer))/2;

        int textY =
                ansY + 85;

        g2.setColor(new Color(0,0,0,120));
        g2.drawString(answer,textX+2,textY+2);

        GradientPaint wordPaint =
                new GradientPaint(
                        0,
                        textY-30,
                        new Color(255,255,180),

                        0,
                        textY,
                        new Color(255,190,40));

        g2.setPaint(wordPaint);

        g2.drawString(answer,textX,textY);

        //====================================================
        // Divider
        //====================================================

        int dividerY = cardY + cardH - 95;

        g2.setStroke(new BasicStroke(1f));

        g2.setColor(new Color(255,255,255,40));

        g2.drawLine(
                cardX+35,
                dividerY,
                cardX+cardW-35,
                dividerY);

        //====================================================
        // BUTTONS
        //====================================================

        int btnY = dividerY + 22;

        btnTimesupTryAgain =
                drawModernButton(
                        g2,
                        cardX + 140,
                        btnY,
                        240,
                        56,
                        "TRY AGAIN",
                        new Color(180,55,70),
                        new Color(255,90,90));

        btnTimesupLevelMenu =
                drawModernButton(
                        g2,
                        cardX + cardW - 380,
                        btnY,
                        240,
                        56,
                        "HOME",
                        new Color(45,90,200),
                        new Color(70,170,255));

        //====================================================
        // Reset Alpha
        //====================================================

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        1f));
    }


    void restart() {
        gameState      = "memorize";
        memorizeTimer  = 0;
        Score = 0;
        memorizeAlpha  = 1.0f;
        countdownTicks = 60 * 60;
        timerStarted   = false;
        winPanelAlpha  = 0f;
        wordInfoText   = "Loading...";
        wordSynonyms   = "";
        wordManager    = new WordManager(targetWord);
        letterBox      = new LetterBox(targetWord, WIDTH, HEIGHT);
        penaltyFlashTicks = 0;
        invincibleTicks   = 0;
        freezeMode.reset();
        confetti.reset();
        spawnLetterObjects();
        spawnAsteroids();
        freezeAll(true);
        player.resetPosition(HEIGHT);
    }

    @Override public void keyPressed(KeyEvent e) {
        if (gameState.equals("playing")) player.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_R) restart();
        if (e.getKeyCode() == KeyEvent.VK_M && onBackToMenu != null) onBackToMenu.run();
    }
    @Override public void keyReleased(KeyEvent e) { player.keyReleased(e); }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {
        if (gameState.equals("win")) {
            if (btnWinPlayAgain.contains(e.getPoint())) { restart(); return; }
            if (btnWinLevelMenu.contains(e.getPoint()) && onBackToMenu != null) { onBackToMenu.run(); return; }
            return;
        }

        if (gameState.equals("timesup")) {
            if (btnTimesupTryAgain.contains(e.getPoint())) { restart(); return; }
            if (btnTimesupLevelMenu.contains(e.getPoint()) && onBackToMenu != null) { onBackToMenu.run(); return; }
            return;
        }

        if (btnRestart.contains(e.getPoint())) { restart(); return; }
        if (btnMenu.contains(e.getPoint()) && onBackToMenu != null) { onBackToMenu.run(); return; }
        if (btnNewWord.contains(e.getPoint())) {
            targetWord    = WordDictionary.pickWord(currentLevel);
            targetLetters = targetWord.toCharArray();
            restart();
            return;
        }
        if (gameState.equals("playing")) player.jump();
    }
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}