import java.io.*;
import java.util.*;
public class PlayerProgress {

    private static final String DIR_NAME      = ".alphaquest";
    private static final String PROFILES_DIR  = "profiles";
    private static final String REGISTRY_FILE = "players.dat";
    private static final int    MAX_KNOWN     = 12;

    public static final int LEVEL_COUNT = 3;

    private static String currentPlayerName = null; // as typed/displayed
    private static boolean loaded = false;

    private static final int[] wordsCollected = new int[LEVEL_COUNT + 1]; // 1-indexed
    @SuppressWarnings("unchecked")
    private static final Set<String>[] completedWords = new HashSet[LEVEL_COUNT + 1];
    private static int gamesPlayed = 0;
    private static int totalScore  = 0;

    static {
        for (int i = 1; i <= LEVEL_COUNT; i++) completedWords[i] = new HashSet<>();
    }

    private static File baseDir() {
        File dir = new File(System.getProperty("user.home"), DIR_NAME);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static File profilesDir() {
        File dir = new File(baseDir(), PROFILES_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static File registryFile() {
        return new File(baseDir(), REGISTRY_FILE);
    }

    private static String slugify(String name) {
        String s = name.trim().toLowerCase();
        s = s.replaceAll("[^a-z0-9]+", "_");
        s = s.replaceAll("^_+|_+$", "");
        if (s.isEmpty()) s = "player";
        return s;
    }

    private static File profileFile(String name) {
        return new File(profilesDir(), slugify(name) + ".dat");
    }

    public static synchronized void setPlayer(String rawName) {
        String name = (rawName == null) ? "" : rawName.trim();
        if (name.isEmpty()) name = "Player";
        if (name.length() > 24) name = name.substring(0, 24);

        currentPlayerName = name;
        gamesPlayed = 0;
        totalScore  = 0;
        for (int i = 1; i <= LEVEL_COUNT; i++) {
            wordsCollected[i] = 0;
            completedWords[i].clear();
        }

        loaded = false;
        ensureLoaded();

        rememberPlayer(name);
    }

    public static synchronized String getCurrentPlayerName() {
        return currentPlayerName == null ? "Player" : currentPlayerName;
    }

    public static synchronized boolean hasPlayer() {
        return currentPlayerName != null;
    }

    public static synchronized List<String> getKnownPlayers() {
        List<String> names = new ArrayList<>();
        File f = registryFile();
        if (!f.exists()) return names;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) names.add(line);
            }
        } catch (Exception e) {
            System.err.println("[PlayerProgress] Failed to read player list: " + e.getMessage());
        }
        return names;
    }

    private static synchronized void rememberPlayer(String name) {
        List<String> names = getKnownPlayers();
        names.removeIf(n -> n.equalsIgnoreCase(name));
        names.add(0, name);
        if (names.size() > MAX_KNOWN) names = new ArrayList<>(names.subList(0, MAX_KNOWN));

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(registryFile()), "UTF-8"))) {
            for (String n : names) {
                bw.write(n);
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("[PlayerProgress] Failed to save player list: " + e.getMessage());
        }
    }
    public static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (currentPlayerName == null) return;

        File f = profileFile(currentPlayerName);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String val = parts[1].trim();

                if (key.equals("gamesPlayed")) {
                    gamesPlayed = parseInt(val, 0);
                } else if (key.equals("totalScore")) {
                    totalScore = parseInt(val, 0);
                } else if (key.startsWith("level") && key.length() > 5) {
                    int level = parseInt(key.substring(5, 6), -1);
                    if (level < 1 || level > LEVEL_COUNT) continue;

                    if (key.endsWith(".words")) {
                        wordsCollected[level] = parseInt(val, 0);
                    } else if (key.endsWith(".list") && !val.isEmpty()) {
                        for (String w : val.split(",")) {
                            w = w.trim();
                            if (!w.isEmpty()) completedWords[level].add(w);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PlayerProgress] Failed to load: " + e.getMessage());
        }
    }

    public static synchronized void save() {
        if (currentPlayerName == null) return;
        ensureLoaded();
        File f = profileFile(currentPlayerName);
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))) {
            bw.write("# AlphaQuest progress for " + currentPlayerName);
            bw.newLine();
            bw.write("gamesPlayed=" + gamesPlayed);
            bw.newLine();
            bw.write("totalScore=" + totalScore);
            bw.newLine();
            for (int level = 1; level <= LEVEL_COUNT; level++) {
                bw.write("level" + level + ".words=" + wordsCollected[level]);
                bw.newLine();
                bw.write("level" + level + ".list=" + String.join(",", completedWords[level]));
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("[PlayerProgress] Failed to save: " + e.getMessage());
        }
    }

    private static int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    /** Call once when a word is fully collected (a level win). Saves immediately. */
    public static synchronized void recordWordCollected(int level, String word, int scoreEarned) {
        ensureLoaded();
        if (level < 1 || level > LEVEL_COUNT || word == null) return;
        word = word.trim().toLowerCase();

        wordsCollected[level]++;
        completedWords[level].add(word);
        gamesPlayed++;
        totalScore += Math.max(0, scoreEarned);

        save();
    }

    public static synchronized int getWordsCollected(int level) {
        ensureLoaded();
        if (level < 1 || level > LEVEL_COUNT) return 0;
        return wordsCollected[level];
    }

    public static synchronized int getDistinctWordsCollected(int level) {
        ensureLoaded();
        if (level < 1 || level > LEVEL_COUNT) return 0;
        return completedWords[level].size();
    }

    public static synchronized int getGamesPlayed() {
        ensureLoaded();
        return gamesPlayed;
    }

    public static synchronized int getTotalScore() {
        ensureLoaded();
        return totalScore;
    }

    public static synchronized int getTotalWordsCollected() {
        ensureLoaded();
        int sum = 0;
        for (int i = 1; i <= LEVEL_COUNT; i++) sum += wordsCollected[i];
        return sum;
    }

    public static synchronized void resetCurrent() {
        if (currentPlayerName == null) return;
        gamesPlayed = 0;
        totalScore  = 0;
        for (int i = 1; i <= LEVEL_COUNT; i++) {
            wordsCollected[i] = 0;
            completedWords[i].clear();
        }
        File f = profileFile(currentPlayerName);
        if (f.exists()) f.delete();
    }
}