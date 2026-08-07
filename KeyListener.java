import java.awt.event.KeyEvent;
import java.util.EventListener;

public interface KeyListener extends EventListener {
    void keyTyped(KeyEvent var1);

    void keyPressed(KeyEvent var1);

    void keyReleased(KeyEvent var1);
}