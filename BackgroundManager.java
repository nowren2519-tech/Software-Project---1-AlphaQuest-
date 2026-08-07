import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

public class BackgroundManager {

    private static final String[] BACKGROUNDS = {
            "/assets/vormir.png",
            "/assets/darkBlue.jpg",
            "/assets/yallowplanet.jpg",
            "/assets/desert.png",
            "/assets/lighthouse.png",
            "/assets/mountains.png",
    };
    private static final List<Integer> bag = new ArrayList<>();
    private static int bagPos = 0;

    private static synchronized int nextIndex() {
        if (bag.isEmpty() || bagPos >= bag.size()) {
            int lastIdx = (!bag.isEmpty()) ? bag.get(bag.size() - 1) : -1;
            bag.clear();
            for (int i = 0; i < BACKGROUNDS.length; i++) bag.add(i);
            Collections.shuffle(bag);
        }
        return bag.get(bagPos++);
    }
    public static Image getBackgroundForWord(String word) {
        int idx = nextIndex();
        Image img = loadSafe(BACKGROUNDS[idx]);
        if (img == null) {
            for (int i = 0; i < BACKGROUNDS.length; i++) {
                if (i == idx) continue;
                img = loadSafe(BACKGROUNDS[i]);
                if (img != null) break;
            }
        }
        return img;
    }

    private static Image loadSafe(String path) {
        try {
            URL url = BackgroundManager.class.getResource(path);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            return icon.getIconWidth() > 0 ? icon.getImage() : null;
        } catch (Exception e) {
            return null;
        }
    }
}