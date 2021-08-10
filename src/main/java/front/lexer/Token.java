package front.lexer;

public class Token {
    /**
     * tag = 1: Identifier
     * tag = 2: Integer
     * tag = 3: Double
     * tag = 4: Character
     * tag = 5: String
     * tag = 6: Punctuation
     */
    private int tag;
    private final String value;

    public Token(int tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public int getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "[" + tag + ", " + value + ']';
    }
}
