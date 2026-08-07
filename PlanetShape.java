import java.awt.*;
import java.awt.geom.*;
public enum PlanetShape {
    CIRCLE {
        @Override
        public Shape buildShape(int cx, int cy, int r) {
            return new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2);
        }

        @Override
        public void drawHighlight(Graphics2D g2, int cx, int cy, int r) {
            g2.fillOval(cx - r/2, cy - r/2, r, r/2 + r/4);
        }

        @Override
        public void drawCraters(Graphics2D g2, int cx, int cy, int r, Color craterColor) {
            g2.setColor(craterColor);
            g2.fillOval(cx + r/6,  cy + r/6,  r/5, r/5);
            g2.fillOval(cx - r/2,  cy + r/4,  r/6, r/6);
            g2.fillOval(cx + r/3,  cy - r/4,  r/7, r/7);
        }
    },
    STAR {
        @Override
        public Shape buildShape(int cx, int cy, int r) {
            return buildStarPath(cx, cy, r, r / 2, 5);
        }

        @Override
        public void drawHighlight(Graphics2D g2, int cx, int cy, int r) {
            g2.fillOval(cx - r/4, cy - r/2, r/2, r/3);
        }

        @Override
        public void drawCraters(Graphics2D g2, int cx, int cy, int r, Color craterColor) {
            g2.setColor(craterColor);
            g2.fillOval(cx - r/8, cy - r/8, r/5, r/5);
            g2.fillOval(cx + r/5, cy + r/6, r/7, r/7);
        }
    },
    BALLOON {
        @Override
        public Shape buildShape(int cx, int cy, int r) {
            int bw = (int)(r * 1.55);
            int bh = (int)(r * 1.85);
            return new Ellipse2D.Float(cx - bw/2, cy - bh/2 - r/6, bw, bh);
        }

        @Override
        public void drawHighlight(Graphics2D g2, int cx, int cy, int r) {
            int bw = (int)(r * 1.55);
            int bh = (int)(r * 1.85);
            int bTop = cy - bh/2 - r/6;
            g2.fillOval(cx - bw/3, bTop + bh/8, bw/3, bh/5);
            g2.fillOval(cx + bw/8, bTop + bh/5, bw/8, bh/10);
        }

        @Override
        public void drawCraters(Graphics2D g2, int cx, int cy, int r, Color craterColor) {
        }
    },
    HEXAGON {
        @Override
        public Shape buildShape(int cx, int cy, int r) {
            return buildRegularPolygon(cx, cy, r, 6, -Math.PI / 6);
        }

        @Override
        public void drawHighlight(Graphics2D g2, int cx, int cy, int r) {
            g2.fillOval(cx - r/3, cy - r/2, r * 2/3, r/3);
        }

        @Override
        public void drawCraters(Graphics2D g2, int cx, int cy, int r, Color craterColor) {
            g2.setColor(craterColor);
            g2.fillOval(cx + r/5,  cy + r/5,  r/5, r/5);
            g2.fillOval(cx - r/3,  cy + r/3,  r/7, r/7);
            g2.fillOval(cx + r/3,  cy - r/5,  r/8, r/8);
        }
    };

    public abstract Shape buildShape(int cx, int cy, int r);
    public abstract void drawHighlight(Graphics2D g2, int cx, int cy, int r);
    public abstract void drawCraters(Graphics2D g2, int cx, int cy, int r, Color craterColor);

    public void fill(Graphics2D g2, int cx, int cy, int r, Color color) {
        g2.setColor(color);
        g2.fill(buildShape(cx, cy, r));
    }

    public void outline(Graphics2D g2, int cx, int cy, int r, Color color, float strokeWidth) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.draw(buildShape(cx, cy, r));
    }
    public static PlanetShape forLevel(int level) {
        switch (level) {
            case 1:  return CIRCLE;
            case 2:  return STAR;
            case 3:  return BALLOON;
            default: return CIRCLE;
        }
    }

    public static PlanetShape forWord(String word) {
        switch (word.toLowerCase()) {
            case "constellation": return forLevel(2);
            case "perpendicular": return forLevel(3);
            default:              return forLevel(1);
        }
    }

    static Path2D buildStarPath(int cx, int cy, int outerR, int innerR, int n) {
        Path2D path = new Path2D.Float();
        double step = Math.PI / n;
        for (int i = 0; i < 2 * n; i++) {
            double angle = i * step - Math.PI / 2;
            int r = (i % 2 == 0) ? outerR : innerR;
            double px = cx + Math.cos(angle) * r;
            double py = cy + Math.sin(angle) * r;
            if (i == 0) path.moveTo(px, py);
            else        path.lineTo(px, py);
        }
        path.closePath();
        return path;
    }
 static Polygon buildRegularPolygon(int cx, int cy, int r, int n, double angleOffset) {
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            double a = angleOffset + (2 * Math.PI / n) * i;
            px[i] = cx + (int)(Math.cos(a) * r);
            py[i] = cy + (int)(Math.sin(a) * r);
        }
        return new Polygon(px, py, n);
    }

    public static void drawBalloon(Graphics2D g2, int cx, int cy, int r,
                                   Color base, boolean shaking) {
        int bw   = (int)(r * 1.55);
        int bh   = (int)(r * 1.85);
        int bx   = cx - bw / 2;
        int bTop = cy - bh / 2 - r / 6;


        Color highlight = base.brighter().brighter();
        Color shadow    = base.darker();
        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                bx,        bTop,        highlight,
                bx + bw,   bTop + bh,   shadow);
        g2.setPaint(gp);
        g2.fillOval(bx, bTop, bw, bh);
        g2.setColor(new Color(255, 255, 255, 130));
        g2.fillOval(cx - bw / 3, bTop + bh / 8, bw / 3, bh / 5);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.fillOval(cx + bw / 8, bTop + bh / 5, bw / 8, bh / 10);


        g2.setColor(shaking ? new Color(255, 60, 60) : shadow);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(bx, bTop, bw, bh);

        int knotX = cx;
        int knotY = bTop + bh;
        g2.setColor(shadow);
        g2.fillOval(knotX - 4, knotY - 4, 8, 8);
        g2.setColor(base);
        g2.fillOval(knotX - 3, knotY - 3, 6, 6);

        g2.setColor(new Color(180, 180, 200, 180));
        g2.setStroke(new BasicStroke(1.2f));
        java.awt.geom.QuadCurve2D string = new java.awt.geom.QuadCurve2D.Float(
                knotX, knotY,
                knotX + 8, knotY + r / 2,
                knotX - 4, knotY + r);
        g2.draw(string);
    }
}