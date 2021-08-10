package front.parser.item;


import java.util.ArrayList;
import java.util.Objects;

public class LRZItem extends Production {
    protected final int point; // 表示点后一个符号的位置，比如 .bc中point的值为0， bc.的值为2

    public LRZItem(Symbol left, ArrayList<Symbol> right, int point) {
        super(left, right);
        this.point = point;
    }

    public LRZItem(Symbol left, Symbol right, int point) {
        super(left, right);
        this.point = point;
    }

    public int getPoint() {
        return point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LRZItem lrzItem = (LRZItem) o;
        return point == lrzItem.point;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), point);
    }

    @Override
    public String toString() {
        return "LRZItem{" +
                "point=" + point +
                ", left=" + left +
                ", right=" + right +
                '}';
    }
}
