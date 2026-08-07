import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;

public class Player {

    int x = 350, y = 0;
    int width = 54, height = 80;
    GamePanel panel;
    int speed = 8;
    int groundY;

    boolean left, right;
    boolean facingRight = true;

    double velY      = 20;
    double gravity   = 1;
    double jumpForce = -43;
    boolean onGround = false;

    int walkFrame   = 0;
    int walkTick    = 0;
    int walkSpeed   = 6;

    float scaleY = 1.0f;

    public Player(GamePanel panel) {
        this.panel = panel;
    }

    public void update() {
        if (panel.getHeight() == 0)
            return;

        groundY = panel.getHeight() - height - 20;
        if (y == 0) y = groundY;

        velY += gravity;
        y   += (int) velY;

        onGround = false;
        if (y >= groundY) {
            if (velY > 6) scaleY = 0.75f;
            y        = groundY;
            velY     = 0;
            onGround = true;
        }

        if (scaleY < 1.0f)      scaleY += 0.06f;
        else if (scaleY > 1.0f) scaleY -= 0.06f;
        if (Math.abs(scaleY - 1.0f) < 0.02f) scaleY = 1.0f;

        if (left)  { x -= speed; facingRight = false; }
        if (right) { x += speed; facingRight = true;  }

        if (x < 0) x = 0;
        if (x > panel.getWidth() - width) x = panel.getWidth() - width;

        if ((left || right) && onGround) {
            walkTick++;
            if (walkTick >= walkSpeed) { walkTick = 0; walkFrame = (walkFrame + 1) % 4; }
        } else if (onGround) {
            walkFrame = 0; walkTick = 0;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = x + width / 2;
        int baseY = y + height;
        drawGroundPlatform(g2, cx, baseY);

        AffineTransform old = g2.getTransform();
        g2.translate(cx, baseY);
        g2.scale(1.0 / scaleY, scaleY);
        g2.translate(-cx, -baseY);

        drawAstronaut(g2, cx, baseY);

        g2.setTransform(old);
    }

    private void drawGroundPlatform(Graphics2D g2, int cx, int baseY) {
        int pw = width;
        int ph = 20;
        int px = cx - pw / 2;
        int py = baseY;

        g2.setColor(new Color(5, 5, 5, 70));
        g2.fillRoundRect(px + 4, py + 5, pw, ph, 8, 8);


        GradientPaint gp = new GradientPaint(
                px, py,      new Color(40, 140, 230),
                px, py + ph, new Color(20, 60, 140));
        g2.setPaint(gp);
        g2.fillRoundRect(px, py, pw, ph, 8, 8);


        g2.setColor(new Color(255, 220, 80, 220));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(px + 6, py + 2, px + pw - 6, py + 2);

        g2.setColor(new Color(100, 190, 255, 200));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(px, py, pw, ph, 8, 8);
        int[] dotX = { px + pw/4, cx, px + pw*3/4 };
        for (int i = 0; i < 3; i++) {
            g2.setColor(new Color(80, 180, 255, 50));
            g2.fillOval(dotX[i] - 5, py + ph - 2, 10, 8);

            g2.setColor(new Color(180, 230, 255, 180));
            g2.fillOval(dotX[i] - 2, py + ph, 4, 4);
        }
    }

    private void drawAstronaut(Graphics2D g2, int cx, int baseY) {

        int[] legSwing = {0, 6, 0, -6};
        int lLeg = onGround ? legSwing[walkFrame] : 8;
        int rLeg = onGround ? -legSwing[walkFrame] : -8;

        Color bootColor  = new Color(50, 50, 60);
        Color bootShine  = new Color(80, 80, 95);

        drawRoundedRect(g2, bootColor, bootShine,
                cx - 18 + lLeg - 2, baseY - 14, 16, 14, 5);

        drawRoundedRect(g2, bootColor, bootShine,
                cx + 2 + rLeg + 2, baseY - 14, 16, 14, 5);


        Color legColor  = new Color(200, 210, 230);
        Color legDark   = new Color(160, 170, 190);

        drawRoundedRect(g2, legColor, legDark,
                cx - 16 + lLeg, baseY - 30, 14, 18, 5);

        drawRoundedRect(g2, legColor, legDark,
                cx + 2 + rLeg, baseY - 30, 14, 18, 5);

        Color torsoColor = new Color(210, 220, 240);
        Color torsoDark  = new Color(170, 180, 200);
        drawRoundedRect(g2, torsoColor, torsoDark,
                cx - 18, baseY - 58, 36, 30, 8);


        g2.setColor(new Color(80, 160, 220));
        g2.fillRoundRect(cx - 8, baseY - 54, 16, 12, 4, 4);
        g2.setColor(new Color(255, 80, 80));
        g2.fillOval(cx - 4, baseY - 51, 6, 6);
        g2.setColor(new Color(80, 255, 120));
        g2.fillOval(cx + 2, baseY - 51, 4, 4);


        Color armColor = new Color(200, 210, 230);
        Color armDark  = new Color(160, 170, 190);
        int armSwing = onGround ? legSwing[walkFrame] : 0;

        drawRoundedRect(g2, armColor, armDark,
                cx - 28, baseY - 56 + armSwing, 12, 22, 5);

        drawRoundedRect(g2, armColor, armDark,
                cx + 16, baseY - 56 - armSwing, 12, 22, 5);

        g2.setColor(new Color(60, 60, 75));
        g2.fillOval(cx - 30, baseY - 36 + armSwing, 14, 12);
        g2.fillOval(cx + 16, baseY - 36 - armSwing, 14, 12);


        Color helmetColor = new Color(220, 230, 250);
        g2.setColor(helmetColor);
        g2.fillOval(cx - 20, baseY - 88, 40, 38);

        Color visorColor = new Color(30, 120, 200, 220);
        g2.setColor(visorColor);
        g2.fillOval(cx - 13, baseY - 82, 26, 24);

        g2.setColor(new Color(255, 255, 255, 100));
        g2.fillOval(cx - 10, baseY - 80, 10, 8);

        g2.setColor(new Color(160, 170, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - 20, baseY - 88, 40, 38);


        drawRoundedRect(g2, new Color(180, 190, 210), new Color(140, 150, 170),
                cx + (facingRight ? 16 : -26), baseY - 56, 10, 26, 4);


        if (!onGround) {
            drawFlame(g2, cx - 2, baseY - 14);
            drawFlame(g2, cx + 2, baseY - 14);
        }
    }

    private void drawFlame(Graphics2D g2, int fx, int fy) {

        g2.setColor(new Color(255, 140, 0, 180));
        int[] xp = { fx, fx + 6, fx + 3 };
        int[] yp = { fy, fy, fy + 20 };
        g2.fillPolygon(xp, yp, 3);

        g2.setColor(new Color(255, 240, 80, 200));
        int[] xi = { fx + 1, fx + 5, fx + 3 };
        int[] yi = { fy, fy, fy + 8 };
        g2.fillPolygon(xi, yi, 3);
    }

    private void drawRoundedRect(Graphics2D g2, Color fill, Color dark,
                                 int rx, int ry, int rw, int rh, int arc) {

        GradientPaint gp = new GradientPaint(rx, ry, fill, rx + rw, ry + rh, dark);
        g2.setPaint(gp);
        g2.fillRoundRect(rx, ry, rw, rh, arc, arc);

        g2.setColor(dark.darker());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(rx, ry, rw, rh, arc, arc);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left  = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_SPACE) jump();
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left  = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
    }

    public void jump() {
        if (onGround) {
            velY     = jumpForce;
            onGround = false;
            scaleY   = 1.3f;
        }
    }

    public void resetPosition(int screenH) {
        x = 350;
        y = screenH - height - 20;
        velY = 0; onGround = false;
        left = false; right = false;
        walkFrame = 0; walkTick = 0; scaleY = 1.0f;
    }
}