import java.awt.Image;
public class WordInfoFetcher {

    public interface Callback {
        void onResult(String description, String synonyms);
    }

    public static void fetch(String word, Callback callback) {
        String description = WordDictionary.getDescription(word);
        String synonyms    = WordDictionary.getSynonyms(word);
        callback.onResult(description, synonyms);
    }
}