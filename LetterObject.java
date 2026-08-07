import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class LetterObject {
    int x, y;
    int radius;
    char letter;
    int sequenceIndex;
    boolean collected = false;
    int collectAlpha  = 255;

    Color planetColor;
    Color glowColor;
    Color craterColor;
    boolean hasRing;
    Color ringColor;
    PlanetShape shape = PlanetShape.CIRCLE;


    double floatX, floatY;
    double velX, velY;
    int minX, maxX, minY, maxY;
    boolean frozen = true;

    int shakeTimer = 0;

    static class Particle {
        float x, y, vx, vy, alpha, size;
        Color color;
        Particle(float x, float y, float vx, float vy, Color c, float size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = c; this.alpha = 1f; this.size = size;
        }
        boolean update() {
            x += vx; y += vy;
            vy += 0.18f;   // gravity
            vx *= 0.96f;
            alpha -= 0.045f;
            size  *= 0.97f;
            return alpha > 0;
        }
    }
    java.util.List<Particle> burst = new java.util.ArrayList<>();
    boolean burstSpawned = false;

    private static final Color[] PLANET_COLORS = {
            new Color(220, 80,  80),  new Color(80,  160, 220),
            new Color(100, 200, 100), new Color(220, 180, 60),
            new Color(180, 80,  220), new Color(220, 130, 60),
            new Color(60,  200, 200), new Color(220, 100, 160),
    };
    private static final Color[] RING_COLORS = {
            new Color(200, 160, 80, 180),
            new Color(150, 200, 220, 180),
            new Color(180, 150, 220, 180),
    };

    Random rand = new Random();

    public LetterObject(int x, int y, char letter, int sequenceIndex,
                        int screenW, int screenH, PlanetShape shape) {
        this.x = x; this.y = y;
        this.floatX = x; this.floatY = y;
        this.letter = letter;
        this.sequenceIndex = sequenceIndex;
        this.radius = 26 + rand.nextInt(10);
        this.shape  = shape;

        planetColor = PLANET_COLORS[rand.nextInt(PLANET_COLORS.length)];
        glowColor   = new Color(planetColor.getRed(), planetColor.getGreen(), planetColor.getBlue(), 80);
        craterColor = planetColor.darker().darker();

        hasRing     = (shape == PlanetShape.CIRCLE )
                && rand.nextInt(10) < 4;
        ringColor   = RING_COLORS[rand.nextInt(RING_COLORS.length)];

        minX = radius + 10;
        maxX = screenW - radius - 10;
        minY = 60;
        maxY = (int)(screenH * 0.70) - radius;

        double spd = 1.2 + rand.nextDouble() * 2.0;
        velX = rand.nextBoolean() ? spd : -spd;
        double spdy = 0.6 + rand.nextDouble() * 1.2;
        velY = rand.nextBoolean() ? spdy : -spdy;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void update() {
        burst.removeIf(p -> !p.update());

        if (collected) {
            if (collectAlpha > 0) collectAlpha -= 15;
            return;
        }
        if (frozen) return;


        floatX += velX;
        floatY += velY;

        if (floatX - radius <= minX) { floatX = minX + radius; velX =  Math.abs(velX); }
        if (floatX + radius >= maxX) { floatX = maxX - radius; velX = -Math.abs(velX); }
        if (floatY - radius <= minY) { floatY = minY + radius; velY =  Math.abs(velY); }
        if (floatY + radius >= maxY) { floatY = maxY - radius; velY = -Math.abs(velY); }

        x = (int) floatX;
        y = (int) floatY;

        if (shakeTimer > 0) shakeTimer--;
    }
    public void reset() {
        collected = false;
        collectAlpha = 255;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Particle p : burst) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, p.alpha)));
            g2.setColor(p.color);
            g2.fillOval((int)(p.x - p.size/2),
                    (int)(p.y - p.size/2),
                    (int)p.size, (int)p.size);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        if (collected && collectAlpha <= 0)
            return;

        float alpha = collected ? collectAlpha / 255f : 1.0f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int shake = (shakeTimer > 0) ? (int)(Math.sin(shakeTimer * 1.8) * 4) : 0;
        int cx = x + shake;
        int cy = y;
        for (int i = 3; i >= 1; i--) {
            int ga = 40 / i;
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(),
                    glowColor.getBlue(), ga));
            int gr = radius + i * 5;
            g2.fillOval(cx - gr, cy - gr, gr * 2, gr * 2);
        }

        if (hasRing) drawRingBack(g2, cx - radius, cy - radius, radius * 2);

        if (shape == PlanetShape.BALLOON) {
            Color bodyColor = (shakeTimer > 0) ? new Color(255, 80, 80) : planetColor;
            PlanetShape.drawBalloon(g2, cx, cy, radius, bodyColor, shakeTimer > 0);
        } else {
            Color bodyColor = (shakeTimer > 0) ? new Color(255, 80, 80) : planetColor;
            shape.fill(g2, cx, cy, radius, bodyColor);

            g2.setColor(new Color(255, 255, 255, 70));
            shape.drawHighlight(g2, cx - radius / 4, cy - radius / 4, radius / 2);

            shape.drawCraters(g2, cx, cy, radius, craterColor);

            shape.outline(g2, cx, cy, radius, planetColor.darker(), 2f);
        }
        if (hasRing) drawRingFront(g2, cx - radius, cy - radius, radius * 2);

        g2.setFont(new Font("Arial", Font.BOLD, radius));
        FontMetrics fm = g2.getFontMetrics();
        String ls = String.valueOf(Character.toUpperCase(letter));
        int tx = cx - fm.stringWidth(ls) / 2;

        int letterOffsetY = (shape == PlanetShape.BALLOON) ? -radius / 4 : 0;
        int ty = cy + fm.getAscent() / 2 - 2 + letterOffsetY;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(ls, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(ls, tx, ty);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawRingBack(Graphics2D g2, int dx, int dy, int size) {
        g2.setColor(ringColor); g2.setStroke(new BasicStroke(4));
        g2.drawArc(dx - 12, dy + size/4, size + 24, size/2, 180, 180);
    }
    private void drawRingFront(Graphics2D g2, int dx, int dy, int size) {
        g2.setColor(ringColor); g2.setStroke(new BasicStroke(4));
        g2.drawArc(dx - 12, dy + size/4, size + 24, size/2, 0, 180);
    }

    public boolean collidesWith(int px, int py, int pWidth, int pHeight) {
        if (collected) return false;
        int pcx = px + pWidth/2, pcy = py + pHeight/2;
        int dx = pcx - x, dy = pcy - y;
        return Math.sqrt(dx*dx + dy*dy) < radius + Math.min(pWidth, pHeight)/2;
    }

    public void collect() {
        collected = true;
        if (shape == PlanetShape.BALLOON && !burstSpawned) {
            burstSpawned = true;
            spawnBurst();
        }
    }

    private void spawnBurst() {
        Random r = new Random();
        Color[] popColors = {
                planetColor, planetColor.brighter(), Color.WHITE,
                new Color(255, 240, 80), new Color(255, 100, 100),
                new Color(100, 220, 255), new Color(200, 100, 255)
        };
        int N = 28;
        for (int i = 0; i < N; i++) {
            double angle = (2 * Math.PI / N) * i + r.nextDouble() * 0.4;
            float spd    = 2.5f + r.nextFloat() * 4.5f;
            float vx     = (float)(Math.cos(angle) * spd);
            float vy     = (float)(Math.sin(angle) * spd) - 1.5f; // bias upward
            float sz     = 4f + r.nextFloat() * 6f;
            Color c      = popColors[r.nextInt(popColors.length)];
            burst.add(new Particle(x, y, vx, vy, c, sz));
        }
        for (int i = 0; i < 6; i++) {
            double angle = r.nextDouble() * 2 * Math.PI;
            float spd    = 1.5f + r.nextFloat() * 3f;
            burst.add(new Particle(x, y,
                    (float)(Math.cos(angle) * spd),
                    (float)(Math.sin(angle) * spd) - 1f,
                    new Color(planetColor.getRed(), planetColor.getGreen(),
                            planetColor.getBlue(), 200),
                    9f + r.nextFloat() * 7f));
        }
    }

    public boolean isFullyVanished() {
        if (shape == PlanetShape.BALLOON)
            return collected && collectAlpha <= 0 && burst.isEmpty();
        return collected && collectAlpha <= 0;
    }
    public char    getLetter()       {
        return letter;
    }
    public int     getSequenceIndex(){
        return sequenceIndex;
    }
    public boolean isCollected()     {
        return collected;
    }
}