import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Confettieffect {

    private static class Piece {
        float x, y, vx, vy;
        float angle, spin;
        float w, h;
        Color color;
        int side; // -1 = left column, 1 = right column
    }

    private static final Color[] COLORS = {
            new Color(255, 90, 90), new Color(255, 200, 60),
            new Color(90, 200, 255), new Color(120, 255, 140),
            new Color(230, 130, 255), new Color(255, 255, 255),
            new Color(255, 160, 60),
    };

    private final List<Piece> pieces = new ArrayList<>();
    private final Random rand = new Random();
    private boolean active = false;

    private int screenW, screenH;
    private int zoneLeftW, zoneRightX, zoneRightW;
    public void spawn(int screenW, int screenH, int cardX, int cardW, int cardY, int cardH) {
        this.screenW    = screenW;
        this.screenH    = screenH;
        this.zoneLeftW  = Math.max(0, cardX);
        this.zoneRightX = cardX + cardW;
        this.zoneRightW = Math.max(0, screenW - zoneRightX);

        pieces.clear();
        int perSide = 45;
        for (int i = 0; i < perSide; i++) spawnPiece(-1);
        for (int i = 0; i < perSide; i++) spawnPiece(1);
        active = true;
    }

    private void spawnPiece(int side) {
        Piece p = new Piece();
        p.side  = side;
        int zoneW = (side < 0) ? zoneLeftW : zoneRightW;
        int zoneX = (side < 0) ? 0 : zoneRightX;
        if (zoneW <= 0) return;

        p.x     = zoneX + rand.nextFloat() * zoneW;
        p.y     = -rand.nextFloat() * screenH;          // staggered start above screen
        p.vx    = (rand.nextFloat() - 0.5f) * 1.2f;
        p.vy    = 2.5f + rand.nextFloat() * 3.5f;
        p.angle = rand.nextFloat() * 360f;
        p.spin  = (rand.nextFloat() - 0.5f) * 10f;
        p.w     = 6f + rand.nextFloat() * 6f;
        p.h     = 10f + rand.nextFloat() * 6f;
        p.color = COLORS[rand.nextInt(COLORS.length)];
        pieces.add(p);
    }

    /** Call every tick while the win screen is showing. */
    public void update() {
        if (!active) return;
        for (Piece p : pieces) {
            p.x += p.vx + Math.sin(p.y * 0.02) * 0.6;   // gentle sway
            p.y += p.vy;
            p.angle += p.spin;

            if (p.y > screenH + 20) {
                // recycle back to the top of its own side, endless fall
                p.y = -20;
                int zoneW = (p.side < 0) ? zoneLeftW : zoneRightW;
                int zoneX = (p.side < 0) ? 0 : zoneRightX;
                if (zoneW > 0) p.x = zoneX + rand.nextFloat() * zoneW;
            }
        }
    }

    public void reset() {
        active = false;
        pieces.clear();
    }

    public void draw(Graphics2D g2) {
        if (!active) return;
        for (Piece p : pieces) {
            AffineTransform old = g2.getTransform();
            g2.translate(p.x, p.y);
            g2.rotate(Math.toRadians(p.angle));
            g2.setColor(p.color);
            g2.fillRect((int)(-p.w / 2), (int)(-p.h / 2), (int)p.w, (int)p.h);
            g2.setTransform(old);
        }
    }
}