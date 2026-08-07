import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
public class SoundManager {

    private static final Map<String, Clip> cache = new HashMap<>();
    private static Clip musicClip;
    private static boolean muted = false;

    private static final String BASE = "/assets/sound/";
    public static void playCollect()   { play("collect.wav"); }
    public static void playWrong()     { play("wrong.wav"); }
    public static void playCelebration() { play("celebration.wav"); }

    public static void playHit()       { play("hit.wav"); }
    public static void playFreeze()    { play("freeze.wav"); }
    public static void playWin()       { play("win.wav"); }
    public static void playTimesUp()   { play("timesup.wav"); }

    public static void play(String fileName) {
        if (muted) return;
        try {
            URL url = SoundManager.class.getResource(BASE + fileName);
            if (url == null) {
                System.err.println("[SoundManager] Missing: " + fileName);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            // free resources once playback finishes
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception e) {
            System.err.println("[SoundManager] Failed to play " + fileName + ": " + e.getMessage());
        }
    }


    public static void playMusic(String fileName, float volume) {
        stopMusic();
        try {
            URL url = SoundManager.class.getResource(BASE + fileName);
            if (url == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            musicClip = AudioSystem.getClip();
            musicClip.open(ais);
            setVolume(musicClip, volume);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("[SoundManager] Failed to play music " + fileName + ": " + e.getMessage());
        }
    }

    public static void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }
    public static void setMuted(boolean m) {
        muted = m;
        if (musicClip != null) musicClip.isActive(); // note: doesn't pause, see toggleMute()
    }
    public static void toggleMute() {
        muted = !muted;
        if (musicClip != null) {
            if (muted) musicClip.stop();
            else    musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static boolean isMuted() {
        return muted;
    }
    private static void setVolume(Clip clip, float volume) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gain.getMinimum(), max = gain.getMaximum();
            float dB = min + (max - min) * Math.max(0f, Math.min(1f, volume));
            gain.setValue(dB);
        } catch (Exception e) {

        }
    }
}