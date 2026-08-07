import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Asteroid {

    double x, y;
    double velX, velY;
    int radius;
    int[] polyX, polyY;
    int nPoints = 10;

    Color blackcolor, darkColor, craterColor;


    int hitFlash = 0;

    double angle    = 0;
    double rotSpeed = 0;

    boolean active = true;

    static Random rand = new Random();

    public Asteroid(int screenW, int screenH) {

        spawn(screenW, screenH);
    }

    public void spawn(int screenW, int screenH) {
        radius = 18 + rand.nextInt(22);   // 18–40 px

        x = radius + rand.nextInt(screenW - radius * 2);
        y = -radius - rand.nextInt(120);

        velX = (rand.nextDouble() - 0.5) * 2.5;
        velY = 1.8 + rand.nextDouble() * 3.0;

        rotSpeed = (rand.nextDouble() - 0.5) * 0.06;
        angle    = rand.nextDouble() * Math.PI * 2;

        int base = 90 + rand.nextInt(60);
       blackcolor  = new Color(base, base - 20, base - 30);
        darkColor   = blackcolor.darker();
        craterColor = new Color(base - 30, base - 40, base - 45);

        buildShape();
        active = true;
    }

    private void buildShape() {
        polyX = new int[nPoints];
        polyY = new int[nPoints];
        for (int i = 0; i < nPoints; i++) {
            double a = (2 * Math.PI / nPoints) * i;
            int r = (int)(radius * (0.70 + rand.nextDouble() * 0.30));
            polyX[i] = (int)(Math.cos(a) * r);
            polyY[i] = (int)(Math.sin(a) * r);
        }
    }

    public void update(int screenW, int screenH) {
        if (!active) return;
        x += velX;
        y += velY;
        angle += rotSpeed;

        if (hitFlash > 0) hitFlash--;


        if (y - radius > screenH || x + radius < 0 || x - radius > screenW) {
            spawn(screenW, screenH);
        }
    }

    public void draw(Graphics g) {
        if (!active) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform old = g2.getTransform();
        g2.translate((int) x, (int) y);
        g2.rotate(angle);


        g2.setColor(new Color(255, 120, 30, 40));
        g2.fillOval(-radius - 4, -radius - 4, (radius + 4) * 2, (radius + 4) * 2);

        if (hitFlash > 0) {
            float t = hitFlash / 12f;
            g2.setColor(new Color(
                    (int)(blackcolor.getRed()   * (1-t) + 255 * t),
                    (int)(blackcolor.getGreen() * (1-t) +  60 * t),
                    (int)(blackcolor.getBlue()  * (1-t) +  60 * t)));
        } else {
            g2.setColor(blackcolor);
        }
        g2.fillPolygon(polyX, polyY, nPoints);


        GradientPaint shade = new GradientPaint(
                -radius/2, -radius/2, new Color(255,255,255,25),
                radius/2,  radius/2, new Color(0,0,0,80));
        g2.setPaint(shade);
        g2.fillPolygon(polyX, polyY, nPoints);

        g2.setColor(craterColor);
        g2.fillOval(-radius/3,     -radius/4,     radius/4, radius/4);
        g2.fillOval( radius/4,      radius/5,     radius/5, radius/5);
        g2.fillOval(-radius/6,      radius*2/5,   radius/6, radius/6);


        g2.setColor(darkColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(polyX, polyY, nPoints);

        for (int i = 1; i <= 3; i++) {
            int alpha = 80 / i;
            g2.setColor(new Color(255, 160, 60, alpha));
            int ts = radius / 3 / i;
            g2.fillOval(-ts/2, -(radius + i * 8), ts, ts);
        }

        g2.setTransform(old);
    }

    public boolean collidesWith(int px, int py, int pw, int ph) {
        if (!active)
            return false;
        double cx = Math.max(px, Math.min(x, px + pw));
        double cy = Math.max(py, Math.min(y, py + ph));
        double dx = cx - x, dy = cy - y;
        return dx*dx + dy*dy < (radius * radius);
    }

    public void triggerHit() { hitFlash = 12; }
}