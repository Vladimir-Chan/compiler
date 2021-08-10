package front.parser.processor;

import front.parser.item.Production;
import front.parser.item.Symbol;

import java.util.Set;

/**
 * 文法处理器，根据给定的文法生成文法分析器Parser类
 */
public class GrammarProcessor {
    private final Set<Production> productions; // 产生式集合
    private final Symbol startSymbol; // 文法开始符号
    private final Set<Symbol> symbols; // 所有文法符号

    public GrammarProcessor(Set<Production> productions, Symbol startSymbol, Set<Symbol> symbols) {
        this.productions = productions;
        this.startSymbol = startSymbol;
        this.symbols = symbols;
    }

//    private void closure(Set<LROItem> LROItems) {
//        Set<LROItem> newLROItems = new HashSet<>();
//        Set<LROItem> preLROItems = new HashSet<>(LROItems);
//        while (!preLROItems.isEmpty()) {
//            for (LROItem LROItem : preLROItems) {
//                // 找到B和\beta
//                char b;
//                try {
//                    b = LROItem.getRight().charAt(LROItem.getPoint());
//                } catch (IndexOutOfBoundsException e) {
//                    continue;
//                }
//                String beta = LROItem.getRight().substring(LROItem.getPoint() + 1);
//                for (Production production : productions)
//                    // 依次搜索B的产生式
//                    if (production.getLeft() == b) {
//                        String betaA;
//                        if (LROItem.getNextSymbol() == '$' && beta.length() != 0)
//                            // a为$ 时，如果\beta不为\epsilon，则\beta a为\beta
//                            betaA = beta;
//                        else
//                            betaA = beta + LROItem.getNextSymbol();
//                        for (Character a : first(betaA))
//                            if (terminator.contains(a))
//                                // a 是终结符号
//                                newLROItems.add(new LROItem(b, production.getRight(), 0, a));
//                    }
//            }
//            LROItems.addAll(newLROItems);
//            preLROItems.clear();
//            preLROItems.addAll(newLROItems);
//            newLROItems.clear();
//        }
//    }
//
//    private Set<LROItem> grammarGoto(Set<LROItem> LROItemI, Character x) {
//        HashSet<LROItem> r = new HashSet<>();
//        for (LROItem LROItem : LROItemI)
//            // I 中的每个项 {A->\alpha . X\beta,a}
//            if (LROItem.getRight().charAt(LROItem.getPoint()) == x)
//                r.add(LROItem);
//        closure(r);
//        return r;
//    }
//
//    private Set<Character> first(String str) {
//        Set<Character> r = new HashSet<>();
//        for (int i = 0; i < str.length(); i++) {
//            // 依次搜索串 X1X2... 的FIRST集合
//            HashSet<Character> xiFirst = new HashSet<>();
//            char symbol = str.charAt(i);
//            if (terminator.contains(symbol)) {
//                // X是终结符号
//                xiFirst.add(symbol);
//                break;
//            }
//            for (Production production : productions)
//                // 搜索所有X的产生式，注意 X->\epsilon也包含在内
//                if (production.getLeft() == symbol)
//                    // 产生式 X->Y1Y2...
//                    for (int j = 0; j < production.getRight().length(); j++) {
//                        String yi = String.valueOf(production.getRight().length());
//                        Set<Character> yiFirst = first(yi);
//                        xiFirst.addAll(yiFirst);
//                        if (!yiFirst.contains('e')) // Yi的FIRST集合中不包含\epsilon，则不会再加入任何符号
//                            break;
//                    }
//            r.addAll(xiFirst);
//            if (!xiFirst.contains('e')) // Xi的FIRST集合中不包含\epsilon，则不会再加入任何符号
//                break;
//        }
//        return r;
//    }
}
