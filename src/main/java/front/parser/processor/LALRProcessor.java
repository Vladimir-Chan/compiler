package front.parser.processor;

import front.parser.item.LROItem;
import front.parser.item.LRZItem;
import front.parser.item.Production;
import front.parser.item.Symbol;

import java.util.*;

public class LALRProcessor {
    private final List<Production> productions; // 产生式集合，其中第一条一定要是开始符号的产生式
    private final Set<Symbol> symbols; // 所有文法符号

    public LALRProcessor(List<Production> productions, Set<Symbol> symbols) {
        this.productions = productions;
        this.symbols = symbols;
    }

    /**
     * @return LALR(1)项集族
     */
    public List<Set<LROItem>> getItems() {
        // 表示坐标的局部内部类
        class Coordinate {
            public final int x; // 第x个内核
            public final int y; // 第y个项

            public Coordinate(int x, int y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Coordinate that = (Coordinate) o;
                return x == that.x &&
                        y == that.y;
            }

            @Override
            public int hashCode() {
                return Objects.hash(x, y);
            }

            @Override
            public String toString() {
                return "Coordinate{" +
                        "x=" + x +
                        ", y=" + y +
                        '}';
            }
        }
        // 1. 构造LR(0)项集族的内核
        LRZProcessor lrzProcessor = new LRZProcessor(productions, symbols);
        List<List<LRZItem>> lrzCores = lrzProcessor.getLRZItemsCore();
//        List<LinkedHashSet<LROItem>> lroCores = new ArrayList<>();
        Map<Coordinate, List<Coordinate>> transferTable = new HashMap<>(); // 传播关系表，表示第i个内核中的第j项传播到了哪一项
        Map<Coordinate, Set<Symbol>> forwardTable = new HashMap<>(); // 向前看符号表

//        for (int i = 0; i < lrzCores.size(); i++)
//            lroCores.add(new LinkedHashSet<>());
        // 加入初始的项 S'->S, $
//        LinkedHashSet<LRZItem> firstLRZItemSet = lrzCores.get(0);
//        for (LRZItem lrzItem : firstLRZItemSet)
//            lroCores.get(0).add(new LROItem(lrzItem.getLeft(), lrzItem.getRight(), lrzItem.getPoint(), new Symbol("$", true)));

        Symbol atSymbol = new Symbol("@", true);
        // 2. 确定自发生成的向前看符号，并且存储传播关系
        // [S'->S,$]，这是初始生成的
        HashSet<Symbol> firstItemSymbol = new HashSet<>();
        firstItemSymbol.add(new Symbol("$", true));
        forwardTable.put(new Coordinate(0, 0), firstItemSymbol);
        for (int i = 0; i < lrzCores.size(); i++) {
            List<LRZItem> lrzItemsCore = lrzCores.get(i); // 内核K
            for (int j = 0; j < lrzItemsCore.size(); j++) {
                List<LROItem> itemList = new ArrayList<>();
                LRZItem lrzItem = lrzItemsCore.get(j); // K中的每个项A->\alpha\cdot\beta
                Coordinate originCoordinate = new Coordinate(i, j);
                itemList.add(new LROItem(lrzItem.getLeft(), lrzItem.getRight(), lrzItem.getPoint(), atSymbol)); // {[A->\alpha\cdot\beta, #]}
                Set<LROItem> itemClosure = closure(itemList); // J = CLOSURE({[A->\alpha\cdot\beta, #]})
                for (LROItem lroItem : itemClosure) {
                    // J中的每个项[B->\gamma\cdot X\delta,a]
                    // 找到X
                    Symbol x;
                    if (lroItem.getPoint() < lroItem.getRight().size())
                        x = lroItem.getRight().get(lroItem.getPoint());
                    else
                        continue;
                    // 找到GOTO(I, X)
                    List<LRZItem> gotoLRZItemsCore = lrzProcessor.getCore(lrzProcessor.lrzGoto(lrzProcessor.closure(lrzItemsCore), x));
                    // 找到GOTO的位置
                    int gotoIndex = lrzCores.indexOf(gotoLRZItemsCore);
                    if (gotoIndex == -1)
                        continue;
                    // 找到具体项的位置
                    int itemIndex = lrzCores.get(gotoIndex).indexOf(new LRZItem(lroItem.getLeft(), lroItem.getRight(), lroItem.getPoint() + 1));
                    if (itemIndex == -1)
                        continue;
                    Coordinate gotoCoordinate = new Coordinate(gotoIndex, itemIndex);
                    if (!lroItem.getNextSymbol().equals(atSymbol))
                        // a != #，说明是自发生成的
                        // 将自发生成的符号存储到表中
                        if (forwardTable.containsKey(gotoCoordinate))
                            forwardTable.get(gotoCoordinate).add(lroItem.getNextSymbol());
                        else {
                            HashSet<Symbol> symbols = new HashSet<>();
                            symbols.add(lroItem.getNextSymbol());
                            forwardTable.put(gotoCoordinate, symbols);
                        }
                    else
                        // a == #，说明向前看符号从I中的B->\gamma\cdot X\delta传播到了GOTO(I,X)中的B->\gamma X\cdot\delta
                        // 将传播关系存储到表中
                        if (transferTable.containsKey(originCoordinate)) {
                            List<Coordinate> transferCoordinateList = transferTable.get(originCoordinate);
                            if (!transferCoordinateList.contains(gotoCoordinate))
                                transferCoordinateList.add(gotoCoordinate);
                        }
                        else {
                            ArrayList<Coordinate> coordinates = new ArrayList<>();
                            coordinates.add(gotoCoordinate);
                            transferTable.put(originCoordinate, coordinates);
                        }
                }
            }
        }

//        transferTable.forEach((k,v) -> System.out.println(k + " " + v));
//        forwardTable.forEach((k,v) -> System.out.println(k + " " + v));

        // 3. 传播向前看符号
        Queue<Coordinate> lrzItemsQueue = new LinkedList<>(forwardTable.keySet());
        while (!lrzItemsQueue.isEmpty()) {
            Coordinate originCoordinate = lrzItemsQueue.poll();
            List<Coordinate> transferCoordinates = transferTable.get(originCoordinate);
            if (!(transferCoordinates == null))
                for (Coordinate transferCoordinate : transferCoordinates) {
                    // 传播到的每个项坐标
                    if (forwardTable.containsKey(transferCoordinate)) {
                        if (!forwardTable.get(transferCoordinate).containsAll(forwardTable.get(originCoordinate))) {
                            // 如果传播目标的符号表中已经包含所有的传播起点的符号，则不继续传播
                            forwardTable.get(transferCoordinate).addAll(forwardTable.get(originCoordinate));
                            lrzItemsQueue.offer(transferCoordinate);
                        }
                    }
                    else {
                        HashSet<Symbol> transferCoordinateSymbols = new HashSet<>(forwardTable.get(originCoordinate));
                        forwardTable.put(transferCoordinate, transferCoordinateSymbols);
                        lrzItemsQueue.offer(transferCoordinate);
                    }
                }
        }

//        forwardTable.forEach((k,v) -> System.out.println(k + " " + v));

        // 4. 从符号表中得到项集核心
        List<List<LROItem>> lroItemsCore = new ArrayList<>();
        for (int i = 0; i < lrzCores.size(); i++)
            lroItemsCore.add(new ArrayList<>());

        for (Map.Entry<Coordinate, Set<Symbol>> entry : forwardTable.entrySet()) {
            int x = entry.getKey().x;
            int y = entry.getKey().y;
            List<LROItem> lroItems = lroItemsCore.get(x);
            LRZItem lrzItem = lrzCores.get(x).get(y);
            for (Symbol forwardSymbol : entry.getValue())
                lroItems.add(new LROItem(lrzItem.getLeft(), lrzItem.getRight(), lrzItem.getPoint(), forwardSymbol));
        }

//         5. 从项集核心得到项集族
        List<Set<LROItem>> lroItems = new ArrayList<>();
        for (List<LROItem> lroCore : lroItemsCore)
            lroItems.add(closure(lroCore));
        return lroItems;
    }

    public List<Symbol> getTerminator() {
        List<Symbol> terminators = new ArrayList<>();
        for (Symbol symbol : symbols)
            if (symbol.isTerminator())
                terminators.add(symbol);
        terminators.add(new Symbol("$", true));
        terminators.remove(new Symbol("\\epsilon", true)); // 空产生式去掉
        return terminators;
    }

    public List<Symbol> getNonTerminator() {
        List<Symbol> nonTerminators = new ArrayList<>();
        for (Symbol symbol : symbols)
            if (!symbol.isTerminator())
                nonTerminators.add(symbol);
        return nonTerminators;
    }

    public String[][] getACTIONTable(List<Set<LROItem>> lroItemsList, List<Symbol> terminatorList) {
        List<Set<LRZItem>> concentricItemSets = new ArrayList<>(); // LALR项集心
        Symbol epsilon = new Symbol("\\epsilon", true); // 空产生式符号
        for (Set<LROItem> lroItemSet : lroItemsList)
            concentricItemSets.add(getConcentricItemSet(lroItemSet));
        String[][] actionTable = new String[lroItemsList.size()][terminatorList.size()];
        Symbol elseSymbol = new Symbol("else", true); // 解决if-else的二义性
        for (int i = 0; i < lroItemsList.size(); i++)
            for (LROItem lroItem : lroItemsList.get(i))
                if (lroItem.getPoint() < lroItem.getRight().size()) {
                    // \cdot不在产生式的末尾
                    if (lroItem.getRight().get(lroItem.getPoint()).isTerminator()) {
                        // [A->\alpha\cdot a\beta, b]
                        int sIndex = concentricItemSets.indexOf(getConcentricItemSet(lalrGOTO(lroItemsList.get(i), lroItem.getRight().get(lroItem.getPoint()))));
                        if (sIndex != -1 && !lroItem.getRight().get(0).equals(epsilon))
//                            if (actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))] == null)
//                            actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))] = "s" + sIndex;
//                            else {
//                                if (!(actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))].charAt(0) == 's' && Integer.parseInt(actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))].substring(1)) == sIndex)) {
//                                    System.out.println(actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))]);
//                                    System.out.println(lroItem);
//                                    System.out.println(sIndex);
//                                }
//                            }
                            if (actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))] == null || lroItem.getNextSymbol().equals(elseSymbol))
                                actionTable[i][terminatorList.indexOf(lroItem.getRight().get(lroItem.getPoint()))] = "s" + sIndex;
                    }
                }
                else if (lroItem.getLeft().equals(productions.get(0).getLeft()))
                        // [S'->S\cdot, $]
                    actionTable[i][terminatorList.indexOf(new Symbol("$", true))] = "acc";
                else
//                    if (actionTable[i][terminatorList.indexOf(lroItem.getNextSymbol())] == null)
                    actionTable[i][terminatorList.indexOf(lroItem.getNextSymbol())] = "r" + productions.indexOf(new Production(lroItem.getLeft(), lroItem.getRight()));
//                    else {
//                        System.out.println(actionTable[i][terminatorList.indexOf(lroItem.getNextSymbol())]);
//                        System.out.println(new Production(lroItem.getLeft(), lroItem.getRight()));
//                        System.out.println(productions.get(Integer.parseInt(actionTable[i][terminatorList.indexOf(lroItem.getNextSymbol())].substring(1))));
//                    }
//        System.exit(0);
        return actionTable;
    }

    /**
     * @param lroItemsList LALR项集族
     * @param nonTerminatorList 非终结符号数组
     * @return GOTO表
     */
    public int[][] getGOTOTable(List<Set<LROItem>> lroItemsList, List<Symbol> nonTerminatorList) {
        int[][] gotoTable = new int[lroItemsList.size()][nonTerminatorList.size()];
        List<Set<LRZItem>> concentricItemSets = new ArrayList<>(); // LALR项集心
        for (Set<LROItem> lroItemSet : lroItemsList)
            concentricItemSets.add(getConcentricItemSet(lroItemSet));
        for (int i = 0; i < lroItemsList.size(); i++)
            for (int j = 0; j < nonTerminatorList.size(); j++) {
                Set<LROItem> gotoItems = lalrGOTO(lroItemsList.get(i), nonTerminatorList.get(j));
                gotoTable[i][j] = concentricItemSets.indexOf(getConcentricItemSet(gotoItems));
            }
        return gotoTable;
    }

    private Set<LROItem> lalrGOTO(Set<LROItem> lroItems, Symbol x) {
        List<LROItem> gotoLROItems = new ArrayList<>();
        for (LROItem lroItem : lroItems)
            // 如果有 [A -> \alpha \cdot X \beta, a]
            if (lroItem.getPoint() < lroItem.getRight().size() && lroItem.getRight().get(lroItem.getPoint()).equals(x))
                // 加入 [A -> \alpha X \cdot \beta, a]
                gotoLROItems.add(new LROItem(lroItem.getLeft(), lroItem.getRight(), lroItem.getPoint() + 1, lroItem.getNextSymbol()));
        return closure(gotoLROItems);
    }

    private Set<LRZItem> getConcentricItemSet(Set<LROItem> lroItems) {
        Set<LRZItem> concentricItemSet = new HashSet<>();
        for (LROItem lroitem : lroItems)
            concentricItemSet.add(new LRZItem(lroitem.getLeft(), lroitem.getRight(), lroitem.getPoint()));
        return concentricItemSet;
    }

    /**
     * @param items LALR(1)项集
     * @return 项集闭包
     */
    private Set<LROItem> closure(Collection<LROItem> items) {
        Set<LROItem> closureItems = new LinkedHashSet<>(items);
//        Set<LROItem> preItems = new HashSet<>(items);
//        Set<LROItem> newItems = new HashSet<>();
        Queue<LROItem> lroItemsQueue = new LinkedList<>(items);
        Symbol epsilon = new Symbol("\\epsilon", true);
        while (!lroItemsQueue.isEmpty()) {
            LROItem item = lroItemsQueue.poll();
            // 找到B
            Symbol b;
            if (item.getPoint() < item.getRight().size())
                b = item.getRight().get(item.getPoint());
            else
                continue;
            // 找到\beta
            List<Symbol> beta = new ArrayList<>(item.getRight().subList(item.getPoint() + 1, item.getRight().size()));
            // 加入a
            beta.add(item.getNextSymbol());
            // 找到FIRST(\beta a)
            Set<Symbol> beFirst = first(beta);
            // 去掉其中的非终结符号
            beFirst.removeIf(symbol -> !symbol.isTerminator());
            for (Production pro : productions)
                // 找到 B -> \gamma
                if (pro.getLeft().equals(b))
                    // 加入 B -> \cdot \gamma, b
                    for (Symbol symbol : beFirst) {
                        LROItem lroItem = new LROItem(pro.getLeft(), pro.getRight(), 0, symbol);
                        if (!closureItems.contains(lroItem)) {
                            lroItemsQueue.offer(lroItem);
                            closureItems.add(lroItem);
                        }
                        if (pro.getRight().get(0).equals(epsilon))
                            // 如果是 B -> \cdot \epsilon，则将 B -> \epsilon \codt也加入
                            closureItems.add(new LROItem(pro.getLeft(), pro.getRight(), 1, symbol));
                    }
        }
        return closureItems;
    }

    private Set<Symbol> first(List<Symbol> symbolList) {
        Set<Symbol> r = new HashSet<>();
        Symbol epsilon = new Symbol("\\epsilon", true);
        HashSet<Symbol> visitedSymbols = new HashSet<>();
        for (Symbol symbol : symbolList) {
            // 一次搜索串X1X2...的FIRST集合
            Set<Symbol> xiFirst = first(symbol);
            r.addAll(xiFirst);
            if (!xiFirst.contains(epsilon)) {
                // Xi 不包含\epsilon，则去掉\epsilon并不会再加入后面的FIRST
                r.remove(epsilon);
                break;
            }
        }
        return r;
    }

    private Set<Symbol> first(Symbol symbol) {
        Set<Symbol> r = new HashSet<>();
        Set<Production> remainProductions = new HashSet<>(); // 收集直接左递归的产生式，如 E -> E + T
        if (symbol.isTerminator())
            // X是终结符号
            r.add(symbol);
        else
            for (Production production : productions)
                // 搜索所有X的产生式，注意 X->\epsilon也包含在内
                if (production.getLeft().equals(symbol)) {
                    // 产生式 X->Y1Y2...
                    if (production.getRight().get(0).equals(symbol))
                        remainProductions.add(production);
                    else {
                        Set<Symbol> firstSet = first(production.getRight());
                        r.addAll(firstSet);
                    }
                }
        Symbol epsilon = new Symbol("\\epsilon", true);
        for (Production production : remainProductions)
            if (r.contains(epsilon)) {
                r.remove(epsilon);
                r.addAll(first(production.getRight().subList(1, production.getRight().size())));
            }
            else
                break;
        return r;
    }

    //    private boolean isEmptyAble(Symbol symbol) {
//        for (Production production : productions) {
//            if (production.getLeft().equals(symbol))
//                // symbol的产生式
//                if ()
//        }
//    }
}
