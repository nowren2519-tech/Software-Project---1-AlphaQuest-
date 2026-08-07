import java.io.*;
import java.net.URL;
import java.util.*;
public class WordDictionary {

    private static final Map<String, String>   descMap     = new LinkedHashMap<>();
    private static final Map<String, String>   synonymMap  = new LinkedHashMap<>();

    private static final List<List<String>> levelWords = new ArrayList<>();
    private static final List<List<Integer>> bags      = new ArrayList<>();
    private static final int[]               bagPos    = new int[4];

    private static boolean loaded = false;

    private static final String[] FILES = {
            null,
            "/assets/word/Level1 words.txt",
            "/assets/word/Level2 words.txt",
            "/assets/word/Level3 words.txt",
    };

    private static synchronized void ensureLoaded() {
        if (loaded)
            return;
        for (int i = 0; i <= 3; i++) {
            levelWords.add(new ArrayList<>());
            bags.add(new ArrayList<>());
        }
        for (int level = 1; level <= 3; level++)
            loadFile(level, FILES[level]);
        loaded = true;
    }

    private static void loadFile(int level, String path) {
        InputStream is = null;
        try {
            URL url = WordDictionary.class.getResource(path);
            if (url != null) {
                is = url.openStream();
            } else {
                String relativePath = "src" + path.replace('/', File.separatorChar);
                File f = new File(relativePath);
                if (!f.exists()) f = new File(path.substring(1));
                if (f.exists()) is = new FileInputStream(f);
            }
            if (is == null) {
                System.err.println("[WordDictionary] File not found: " + path);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;
// extract data
                String word = parts[0].trim().toLowerCase();
                String desc = parts[1].trim();
                String syns = parts.length >= 3 ? parts[2].trim() : "";

                if (word.isEmpty()) continue;

                if (!descMap.containsKey(word)) {
                    descMap.put(word, desc);
                    synonymMap.put(word, syns);
                    levelWords.get(level).add(word);
                    count++;
                }
            }
            br.close();
            System.out.println("[WordDictionary] Level " + level + ": loaded " + count + " words from " + path);
        } catch (Exception e) {
            System.err.println("[WordDictionary] Error loading " + path + ": " + e.getMessage());
        } finally {
            if (is != null) try { is.close(); } catch (Exception ignored) {}
        }
    }
    private static synchronized void ensureBag(int level) {
        List<Integer> bag = bags.get(level);
        List<String>  wl  = levelWords.get(level);
        if (!bag.isEmpty())
            return;
        for (int i = 0; i < wl.size(); i++) bag.add(i);
        Collections.shuffle(bag);
    }

    public static synchronized String pickWord(int level) {
        ensureLoaded();
        if (level < 1 || level > 3) level = 1;
        List<String>  wl  = levelWords.get(level);
        List<Integer> bag = bags.get(level);
        ensureBag(level);
        if (bagPos[level] >= bag.size()) {
            int last = bag.get(bag.size() - 1);
            Collections.shuffle(bag);
            if (bag.get(0).equals(last) && bag.size() > 1) {
                int tmp = bag.get(0); bag.set(0, bag.get(1)); bag.set(1, tmp);
            }
            bagPos[level] = 0;
        }
        return wl.get(bag.get(bagPos[level]++));
    }
    public static String getDescription(String word) {
        ensureLoaded();
        if (word == null)
            return "";
        String desc = descMap.get(word.trim().toLowerCase());
        return desc != null ? desc : "A fascinating word! Look it up in a dictionary to learn more.";
    }
    public static String getSynonyms(String word) {
        ensureLoaded();
        if (word == null)
            return "";
        String syns = synonymMap.get(word.trim().toLowerCase());
        return syns != null ? syns : "";
    }

    public static boolean contains(String word) {
        ensureLoaded();
        return word != null && descMap.containsKey(word.trim().toLowerCase());
    }

    public static int size(int level) {
        ensureLoaded();
        if (level < 1 || level > 3) return 0;
        return levelWords.get(level).size();
    }
}