public class WordManager {

    String targetWord;
    int currentIndex     = 0;
    int consecutiveCount = 0;

    public WordManager(String targetWord) {

        this.targetWord = targetWord.toLowerCase();
    }

    public String checkAndCollect(char letter) {
        if (currentIndex >= targetWord.length())
            return "already_done";
        char expected = targetWord.charAt(currentIndex);

        if (Character.toLowerCase(letter) == expected) {
            currentIndex++;
            consecutiveCount++;
            return "correct";
        } else {
            consecutiveCount = 0;
            return "wrong";
        }
    }

    public boolean isWordComplete()  {
        return currentIndex >= targetWord.length();
    }
    public char getNextExpected()    {
        return currentIndex < targetWord.length() ? targetWord.charAt(currentIndex) : ' ';
    }
    public int getCurrentIndex()     {
        return currentIndex;
    }
    public int getConsecutiveCount() {
        return consecutiveCount;
    }
    public String getTargetWord()
    { return targetWord;
    }
}