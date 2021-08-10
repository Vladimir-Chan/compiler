package front.parser.item;


import java.util.ArrayList;

public class LROItem extends LRZItem {
    protected final Symbol nextSymbol;

    public LROItem(Symbol left, ArrayList<Symbol> right, int point, Symbol nextSymbol) {
        super(left, right, point);
        this.nextSymbol = nextSymbol;
    }

    public LROItem(Symbol left, Symbol right, int point, Symbol nextSymbol) {
        super(left, right, point);
        this.nextSymbol = nextSymbol;
    }

    public Symbol getNextSymbol() {
        return nextSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LROItem LROItem = (LROItem) o;
        return nextSymbol.equals(LROItem.nextSymbol);
    }

//    @Override
//    public String toString() {
//        return "LROItem{" +
//                "nextSymbol=" + nextSymbol +
//                ", point=" + point +
//                ", left=" + left +
//                ", right=" + right +
//                '}';
//    }


    @Override
    public String toString() {
        return left + " -> " + right + ',' + nextSymbol + ',' + point;
    }
}
