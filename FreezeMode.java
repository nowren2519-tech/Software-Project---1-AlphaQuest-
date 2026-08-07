import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * Manages the Freeze Mode power-up.
 * Triggers every 4 collected letters — freezes all asteroids
 * and letter objects for 10 seconds.
 */
public class FreezeMode {

    // ── Config ────────────────────────────────────────────────────────────────
    private static final int DURATION       = 10 * 60;  // 10 sec at 60fps
    private static final int TRIGGER_EVERY  = 5;        // every N letters
    private static final int BANNER_TICKS   = 120;      // 2 sec banner

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean active         = false;
    private int     ticksLeft      = 0;
    private int     lastTriggerAt  = 0;   // last collected-count that triggered
    private int     bannerTicks    = 0;
    private float   overlayAlpha   = 0f;

    // ── Public API ────────────────────────────────────────────────────────────

    /** Call every game tick while playing. */
    public void update() {
        if (bannerTicks > 0) bannerTicks--;

        if (!active) return;

        ticksLeft--;
        overlayAlpha = 0.08f + 0.05f * (float) Math.sin(ticksLeft * 0.15);

        if (ticksLeft <= 0) {
            active       = false;
            overlayAlpha = 0f;
        }
    }

    /**
     * Call after each correct letter collection.
     * @param collectedCount total letters collected so far
     * @return true if freeze mode just activated
     */
    public boolean onLetterCollected(int collectedCount) {
        if (collectedCount > 0
                && collectedCount % TRIGGER_EVERY == 0
                && collectedCount != lastTriggerAt) {
            lastTriggerAt = collectedCount;
            active        = true;
            ticksLeft     = DURATION;
            bannerTicks   = BANNER_TICKS;
            overlayAlpha  = 0.08f;
            return true;
        }
        return false;
    }

    /** Reset everything (on game restart). */
    public void reset() {
        active        = false;
        ticksLeft     = 0;
        lastTriggerAt = 0;
        bannerTicks   = 0;
        overlayAlpha  = 0f;
    }

    public boolean isActive()      { return active; }
    public boolean isBannerShowing(){ return bannerTicks > 0; }
    public int     getTicksLeft()  { return ticksLeft; }

    // ── Drawing ───────────────────────────────────────────────────────────────

    /** Full freeze overlay — blue tint + progress bar + frozen asteroid crystals. */
    public void drawOverlay(Graphics2D g2, int screenW, int screenH,
                            ArrayList<Asteroid> asteroids) {
        // Blue-cyan screen tint
        g2.setColor(new Color(0, 180, 255, (int)(overlayAlpha * 255)));
        g2.fillRect(0, 0, screenW, screenH);

        // Progress bar (top center)
        drawProgressBar(g2, screenW);

        // Ice crystals over each frozen asteroid
        for (Asteroid a : asteroids)
            drawIceCrystal(g2, (int) a.x, (int) a.y, a.radius);
    }

    private void drawProgressBar(Graphics2D g2, int screenW) {
        int barW = 300, barH = 10;
        int bx = screenW / 2 - barW / 2, by = 58;
        float progress = (float) ticksLeft / DURATION;

        // Background
        g2.setColor(new Color(0, 0, 40, 180));
        g2.fillRoundRect(bx - 2, by - 2, barW + 4, barH + 4, 6, 6);

        // Fill
        GradientPaint gp = new GradientPaint(
                bx, by, new Color(80, 220, 255),
                bx + (int)(barW * progress), by, new Color(0, 120, 220));
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, (int)(barW * progress), barH, 5, 5);

        // Border
        g2.setColor(new Color(100, 200, 255, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx - 2, by - 2, barW + 4, barH + 4, 6, 6);

        // Labels
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(100, 220, 255));
        g2.drawString("FREEZE", bx - 60, by + 9);

        FontMetrics fm = g2.getFontMetrics();
        String secStr = (ticksLeft / 60 + 1) + "s";
        g2.setColor(new Color(180, 240, 255));
        g2.drawString(secStr, bx + barW + 8, by + 9);
    }

    private void drawIceCrystal(Graphics2D g2, int cx, int cy, int r) {
        g2.setColor(new Color(150, 230, 255, 160));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 3; i++) {
            double a = Math.toRadians(i * 60);
            g2.drawLine((int)(cx + Math.cos(a) * r), (int)(cy + Math.sin(a) * r),
                    (int)(cx - Math.cos(a) * r), (int)(cy - Math.sin(a) * r));
        }
        g2.setColor(new Color(200, 245, 255, 200));
        g2.fillOval(cx - 5, cy - 5, 10, 10);
    }

    /** Animated "FREEZE MODE!" banner — fades in, holds, fades out. */
    public void drawBanner(Graphics2D g2, int screenW, int screenH) {
        if (bannerTicks <= 0) return;

        float alpha = Math.min(1f,
                bannerTicks < 20  ? bannerTicks / 20f :
                        bannerTicks > 100 ? (BANNER_TICKS - bannerTicks) / 20f : 1f);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int cw = 520, ch = 90;
        int cx = screenW / 2 - cw / 2;
        int cy = screenH / 2 - ch / 2 - 60;

        // Card
        g2.setColor(new Color(0, 20, 60, 210));
        g2.fillRoundRect(cx, cy, cw, ch, 18, 18);
        g2.setColor(new Color(80, 200, 255, 220));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(cx, cy, cw, ch, 18, 18);

        // Snowflake icon
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        g2.setColor(new Color(150, 230, 255));
        g2.drawString("*", cx + 22, cy + 58);

        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        GradientPaint gp = new GradientPaint(
                0, cy + 20, new Color(150, 230, 255),
                0, cy + 55, new Color(0, 150, 255));
        g2.setPaint(gp);
        g2.drawString("FREEZE MODE!", cx + 65, cy + 46);

        // Subtitle
        g2.setFont(new Font("kalpurush", Font.PLAIN, 18));
        g2.setColor(new Color(180, 230, 255, 220));
        g2.drawString("--Everything frozen for "+ (DURATION / 60) + " seconds!", cx + 65, cy + 68);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    /** Returns the color/style the timer box should use when freeze is active. */
    public Color getTimerBgColor()     { return new Color(0, 60, 120, 210); }
    public Color getTimerBorderColor() { return new Color(80, 200, 255, 220); }
    public Color getTimerTextColor()   { return new Color(100, 220, 255); }
}