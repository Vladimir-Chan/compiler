package front.parser.processor;

import front.parser.item.LRZItem;
import front.parser.item.Production;
import front.parser.item.Symbol;

import java.util.*;

public class LRZProcessor {
    private final List<Production> productions; // 产生式集合，其中第一条一定要是开始符号的产生式
//    private final Symbol startSymbol; // 文法开始符号
    private final Set<Symbol> symbols; // 所有文法符号
//    private final List<LinkedHashSet<LRZItem>> itemsSetCore; // LR(0)项集核心
//    private final List<Map<Symbol, Integer>> gotoTable; // goto表

    public LRZProcessor(List<Production> productions, Set<Symbol> symbols) {
        this.productions = productions;
//        this.startSymbol = startSymbol;
        this.symbols = symbols;
//        this.gotoTable = new ArrayList<>();
    }

//    public List<Map<Symbol, Integer>> getGotoTable() {
//        return gotoTable;
//    }

    public List<LRZItem> closure(List<LRZItem> items) {
        List<LRZItem> closureItems = new ArrayList<>(items);
//        Set<LRZItem> preItems = new HashSet<>(items);
//        Set<LRZItem> newItems = new HashSet<>();
        Queue<LRZItem> itemsQueue = new LinkedList<>(items);
        Symbol epsilon = new Symbol("\\epsilon", true);
        while (!itemsQueue.isEmpty()) {
            LRZItem item = itemsQueue.poll();
            // 找到B
            Symbol b;
            if (item.getPoint() < item.getRight().size())
                b = item.getRight().get(item.getPoint());
            else
                continue;
            for (Production pro : productions)
                // 找到 B -> \gamma
                if (pro.getLeft().equals(b)) {
                    // 加入 B -> \cdot \gamma
                    LRZItem newItem = new LRZItem(pro.getLeft(), pro.getRight(), 0);
                    if (!closureItems.contains(newItem)) {
                        itemsQueue.offer(newItem);
                        closureItems.add(newItem);
                    }
                    if (pro.getRight().get(0).equals(epsilon))
                        // 如果是空产生式，则将 A->\epsilon\cdot也加入
                        closureItems.add(new LRZItem(pro.getLeft(), pro.getRight(), 1));
                }
//            items.addAll(newItems);
//            preItems.clear();
//            preItems.addAll(newItems);
//            newItems.clear();
        }
        return closureItems;
    }

    public List<LRZItem> lrzGoto(List<LRZItem> items, Symbol x) {
        List<LRZItem> r = new ArrayList<>();
        for (LRZItem item : items)
            // 如果有 A -> \alpha \cdot X \beta
            if (item.getPoint() < item.getRight().size() && item.getRight().get(item.getPoint()).equals(x))
                // 加入 A -> \alpha X \cdot \beta
                r.add(new LRZItem(item.getLeft(), item.getRight(), item.getPoint() + 1));
        return closure(r);
    }

    public List<LRZItem> getCore(List<LRZItem> items) {
        List<LRZItem> itemsCore = new ArrayList<>();
        // Symbol s = new Symbol(productions.get(0).getLeft().getValue() + "'", false);
        for (LRZItem item : items)
            if (item.getPoint() != 0 || item.getLeft().equals(productions.get(0).getLeft()))
                itemsCore.add(item);
        return itemsCore;
    }

    public List<List<LRZItem>> getLRZItemsCore() {
        List<List<LRZItem>> itemSets = new ArrayList<>(); // 项集
//        List<Set<LRZItem>> newSets = new ArrayList<>(); // 新加入的项集
//        List<Set<LRZItem>> preSets = new ArrayList<>(); // 前一轮的项集
        Queue<List<LRZItem>> setsQueue = new LinkedList<>();
        List<LRZItem> firstItems = new ArrayList<>();
        firstItems.add(new LRZItem(productions.get(0).getLeft(), productions.get(0).getRight(), 0));
        setsQueue.offer(firstItems); // 加入最初的内核项集 S' -> S
        itemSets.add(firstItems); // 状态0的内核项集 S' -> S
        while (!setsQueue.isEmpty()) {
            List<LRZItem> items = closure(setsQueue.poll());
            for (Symbol symbol : symbols) {
                List<LRZItem> gotoX = getCore(lrzGoto(items, symbol));
                if (!gotoX.isEmpty() && !itemSets.contains(gotoX)) {
                    setsQueue.offer(gotoX);
                    itemSets.add(gotoX);
                }
            }
//            items.addAll(newItems);
//            preItems.clear();
//            preItems.addAll(newItems);
//            newItems.clear();
        }
        return itemSets;
    }
}
