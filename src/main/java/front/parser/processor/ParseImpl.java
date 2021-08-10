package front.parser.processor;

import front.lexer.Token;
import front.parser.item.Production;
import front.parser.item.Symbol;
import utils.exceptions.CompileException;

import java.util.List;
import java.util.Stack;

public class ParseImpl implements Parse {
    private final List<Production> productions; // 产生式列表
    private final List<Symbol> terminators; // 终结符号
    private final List<Symbol> nonTerminators; // 非终结符号
    private final String[][] actionTable; // ACTION表
    private final int[][] gotoTable; // GOTO表
    private final Stack<Integer> statusStack; // 状态栈
    private final Symbol epsilon; // \epsilon

    public ParseImpl(List<Production> productions, List<Symbol> terminators, List<Symbol> nonTerminators, String[][] actionTable, int[][] gotoTable) {
        this.productions = productions;
        this.terminators = terminators;
        this.nonTerminators = nonTerminators;
        this.actionTable = actionTable;
        this.gotoTable = gotoTable;
        statusStack = new Stack<>();
        statusStack.push(0);
        epsilon = new Symbol("\\epsilon", true);
    }

    @Override
    public int grammaticalAnalysis(Symbol symbol) throws CompileException {
        String action = actionTable[statusStack.peek()][terminators.indexOf(symbol)];
//        System.out.println("栈顶状态" + statusStack.peek());
        if (action == null) {
            System.out.println("error");
            System.out.println(statusStack);
            System.out.println(symbol);
            throw new CompileException();
        } else if (action.charAt(0) == 's') {
            // 移入动作
            statusStack.push(Integer.parseInt(action.substring(1)));
//            ++i;
            return -1;
        } else if (action.charAt(0) == 'r') {
            // 规约动作
            // 产生式 A->\beta
            Production production = productions.get(Integer.parseInt(action.substring(1)));
            // 弹出|\beta|个符号
            if (!production.getRight().get(0).equals(epsilon))
                for (int j = 0; j < production.getRight().size(); j++)
                    statusStack.pop();
            // 压入 GOTO(t,A)，t为栈顶符号
            statusStack.push(gotoTable[statusStack.peek()][nonTerminators.indexOf(production.getLeft())]);
            // 输出产生式
            System.out.println(production);
            // 返回产生式序号
//            return production;
            return Integer.parseInt(action.substring(1));
        } else {
            System.out.println("accept");
            return 0;
//            return productions.get(0);
        }
//        return null;
    }

    @Override
    public Production getProduction(int index) {
        return productions.get(index);
    }

    @Override
    public Symbol transfer(Token token) throws CompileException {
        Symbol symbol = new Symbol(token.getValue(), true);
        if (terminators.contains(symbol))
            return symbol;
        else
            switch (token.getTag()) {
                case 1:
                    return new Symbol("id", true);
                case 2:
                    return new Symbol("integer", true);
                case 3:
                    return new Symbol("float", true);
                case 4:
                    return new Symbol("character", true);
                default:
                    System.out.println(token);
                    throw new CompileException();
            }
    }

//    private int getTerminatorIndex(Token token) {
//        int index = terminators.indexOf(new Symbol(token.getValue(), true));
//        if (index == -1)
//            if (token.getTag() == 1)
//                return terminators.indexOf(new Symbol("id", true));
//            else if (token.getTag() != 6)
//                return terminators.indexOf(new Symbol("constant", true));
//            else {
//                System.out.println(token);
//                System.exit(0);
//                return -1;
//            }
//        else
//            return index;
//    }
}
