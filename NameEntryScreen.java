import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
public class NameEntryScreen extends JPanel implements MouseListener, MouseMotionListener {

    public interface NameEntryListener {
        void onNameConfirmed(String name);
    }

    int WIDTH, HEIGHT;
    NameEntryListener listener;

    JTextField nameField;
    Rectangle  btnStart = new Rectangle();
    boolean    hoverStart = false;

    java.util.List<String> knownPlayers;
    java.util.List<Rectangle> chipRects = new java.util.ArrayList<>();

    String errorMessage = "";
    int errorTicks = 0;

    float[] starX, starY, starSpeed, starSize;
    int STAR_COUNT = 90;
    float glowPhase = 0f;

    Timer animTimer;

    public NameEntryScreen(NameEntryListener listener) {
        this.listener = listener;

        WIDTH  = Toolkit.getDefaultToolkit().getScreenSize().width;
        HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setLayout(null);

        knownPlayers = PlayerProgress.getKnownPlayers();

        addMouseListener(this);
        addMouseMotionListener(this);

        buildNameField();
        initStars();

        animTimer = new Timer(16, e -> {
            updateStars();
            glowPhase += 0.03f;
            if (glowPhase > Math.PI * 2) glowPhase -= (float) (Math.PI * 2);
            if (errorTicks > 0) errorTicks--;
            repaint();
        });
        animTimer.start();
    }

    void buildNameField() {
        int fieldW = 420, fieldH = 52;
        int fieldX = WIDTH / 2 - fieldW / 2;
        int fieldY = HEIGHT / 2 - 30;

        nameField = new JTextField();
        nameField.setBounds(fieldX, fieldY, fieldW, fieldH);
        nameField.setFont(new Font("Arial", Font.BOLD, 22));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBackground(new Color(15, 18, 45));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(90, 130, 210), 2));
        nameField.addActionListener(e -> confirm(nameField.getText()));
        add(nameField);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
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

    void confirm(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            errorMessage = "Please type a name first!";
            errorTicks   = 120;
            repaint();
            return;
        }
        stop();
        PlayerProgress.setPlayer(name);
        listener.onNameConfirmed(name);
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

        g2.setFont(new Font("kalpurush", Font.PLAIN, 22));
        g2.setColor(new Color(160, 200, 255, 200));
        String sub = "Enter your name to save your progress";
        FontMetrics sfm = g2.getFontMetrics();
        g2.drawString(sub, (WIDTH - sfm.stringWidth(sub)) / 2, HEIGHT / 2 - 70);

        drawStartButton(g2);
        drawKnownPlayers(g2);

        if (errorTicks > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(new Color(255, 100, 100, Math.min(255, errorTicks * 4)));
            FontMetrics efm = g2.getFontMetrics();
            g2.drawString(errorMessage, (WIDTH - efm.stringWidth(errorMessage)) / 2, HEIGHT / 2 + 60);
        }
    }

    void drawTitle(Graphics2D g2) {
        String title = "WHO'S  PLAYING?";
        float glow = (float) (0.5 + 0.5 * Math.sin(glowPhase));

        g2.setFont(new Font("kalpurush", Font.BOLD, 56));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (WIDTH - fm.stringWidth(title)) / 2;
        int ty = HEIGHT / 2 - 140;

        for (int i = 5; i >= 1; i--) {
            float alpha = (0.04f + 0.04f * glow) * i;
            g2.setColor(new Color(100, 180, 255, (int) (alpha * 255)));
            g2.drawString(title, tx - i * 2, ty + i * 2);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 56));
        fm = g2.getFontMetrics();
        tx = (WIDTH - fm.stringWidth(title)) / 2;

        GradientPaint gp = new GradientPaint(tx, ty - 46, new Color(255, 240, 120),
                tx, ty, new Color(220, 160, 40));
        g2.setPaint(gp);
        g2.drawString(title, tx, ty);
    }

    void drawStartButton(Graphics2D g2) {
        int bw = 180, bh = 50;
        int bx = WIDTH / 2 - bw / 2;
        int by = HEIGHT / 2 + 40;
        btnStart.setBounds(bx, by, bw, bh);

        Color base = hoverStart ? new Color(90, 190, 120) : new Color(55, 150, 90);
        GradientPaint gp = new GradientPaint(bx, by, base.brighter(), bx, by + bh, base.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, bw, bh, 14, 14);

        g2.setColor(new Color(255, 255, 255, hoverStart ? 130 : 70));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 14, 14);

        g2.setFont(new Font("Arial", Font.BOLD, 17));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "START";
        g2.setColor(Color.WHITE);
        g2.drawString(txt, bx + (bw - fm.stringWidth(txt)) / 2, by + bh / 2 + fm.getAscent() / 2 - 3);
    }

    void drawKnownPlayers(Graphics2D g2) {
        chipRects.clear();
        if (knownPlayers.isEmpty()) return;

        int labelY = HEIGHT / 2 + 130;
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(150, 170, 210, 190));
        String label = "OR CONTINUE AS";
        FontMetrics lfm = g2.getFontMetrics();
        g2.drawString(label, (WIDTH - lfm.stringWidth(label)) / 2, labelY);

        int maxShown = Math.min(6, knownPlayers.size());
        Font chipFont = new Font("Arial", Font.BOLD, 15);
        g2.setFont(chipFont);
        FontMetrics cfm = g2.getFontMetrics();

        int gap = 14;
        int[] widths = new int[maxShown];
        int totalW = 0;
        for (int i = 0; i < maxShown; i++) {
            widths[i] = cfm.stringWidth(knownPlayers.get(i)) + 36;
            totalW += widths[i] + (i > 0 ? gap : 0);
        }

        int startX = WIDTH / 2 - totalW / 2;
        int chipY  = labelY + 18;
        int chipH  = 38;
        int x = startX;

        for (int i = 0; i < maxShown; i++) {
            int w = widths[i];
            Rectangle r = new Rectangle(x, chipY, w, chipH);
            chipRects.add(r);

            boolean hovered = r.contains(getMousePosition() == null ? new Point(-1, -1) : getMousePosition());
            Color base = hovered ? new Color(70, 90, 150) : new Color(35, 42, 80);
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(x + 2, chipY + 3, w, chipH, 18, 18);
            g2.setColor(base);
            g2.fillRoundRect(x, chipY, w, chipH, 18, 18);
            g2.setColor(new Color(140, 170, 230, hovered ? 200 : 110));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, chipY, w, chipH, 18, 18);

            g2.setColor(Color.WHITE);
            String name = knownPlayers.get(i);
            g2.drawString(name, x + (w - cfm.stringWidth(name)) / 2, chipY + chipH / 2 + cfm.getAscent() / 2 - 3);

            x += w + gap;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean prev = hoverStart;
        hoverStart = btnStart.contains(e.getPoint());

        boolean overChip = false;
        for (Rectangle r : chipRects) if (r.contains(e.getPoint())) { overChip = true; break; }

        if (hoverStart != prev || overChip) setCursor(
                (hoverStart || overChip)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (btnStart.contains(e.getPoint())) {
            confirm(nameField.getText());
            return;
        }
        for (int i = 0; i < chipRects.size(); i++) {
            if (chipRects.get(i).contains(e.getPoint())) {
                confirm(knownPlayers.get(i));
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