package front.parser.item;


import java.util.ArrayList;
import java.util.Objects;

/**
 * 产生式类
 */
public class Production {
    protected final Symbol left;
    protected final ArrayList<Symbol> right;

    public Production(Symbol left, ArrayList<Symbol> right) {
        this.left = left;
        this.right = right;
    }

    public Production(Symbol left, Symbol rightSymbol) {
        this.left = left;
        ArrayList<Symbol> right = new ArrayList<>();
        right.add(rightSymbol);
        this.right = right;
    }

    public Symbol getLeft() {
        return left;
    }

    public ArrayList<Symbol> getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;
        return left.equals(that.left) &&
                right.equals(that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return left + " -> " + right;
    }
}
