package front.parser.processor;


import front.parser.item.Production;
import front.parser.item.Symbol;

import java.util.List;
import java.util.Stack;

public class ToyParser {
    private final List<Production> productions; // 产生式列表
    private final List<Symbol> terminators; // 终结符号
    private final List<Symbol> nonTerminators; // 非终结符号
    private final String[][] actionTable; // ACTION表
    private final int[][] gotoTable; // GOTO表

    public ToyParser(List<Production> productions, List<Symbol> terminators, List<Symbol> nonTerminators, String[][] actionTable, int[][] gotoTable) {
        this.productions = productions;
        this.terminators = terminators;
        this.nonTerminators = nonTerminators;
        this.actionTable = actionTable;
        this.gotoTable = gotoTable;
    }

    /**
     * 简单语法分析
     */
    public void grammaticalAnalysis(String inputString) {
        inputString += "$";
        Stack<Integer> statusStack = new Stack<>();
        statusStack.push(0);
        Symbol epsilon = new Symbol("\\epsilon", true);
        int i = 0;
        while (true) {
            System.out.println(inputString.charAt(i));
            String action = actionTable[statusStack.peek()][terminators.indexOf(new Symbol(String.valueOf(inputString.charAt(i)), true))];
            if (action == null) {
                System.out.println("error");
                System.out.println(statusStack);
                System.out.println(inputString.charAt(i));
                System.exit(0);
            } else if (action.charAt(0) == 's') {
                // 移入动作
                statusStack.push(Integer.parseInt(action.substring(1)));
                ++ i;
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
            } else {
                System.out.println("accept");
                return;
            }
        }
    }
}
