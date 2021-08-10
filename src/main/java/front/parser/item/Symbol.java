package front.parser.item;

import java.util.Objects;

/**
 * 文法中的符号类
 */
public class Symbol {
    private final String value;
    private final boolean isTerminator;

    public Symbol(String value, boolean isTerminator) {
        this.value = value;
        this.isTerminator = isTerminator;
    }

    public String getValue() {
        return value;
    }

    public boolean isTerminator() {
        return isTerminator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return isTerminator == symbol.isTerminator &&
                value.equals(symbol.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isTerminator);
    }

//    @Override
//    public String toString() {
//        return "Symbol{" +
//                "value='" + value + '\'' +
//                ", isTerminator=" + isTerminator +
//                '}';
//    }


    @Override
    public String toString() {
        return value;
    }
}
