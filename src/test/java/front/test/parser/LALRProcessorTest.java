package front.test.parser;


import front.parser.item.LROItem;
import front.parser.processor.GrammarPreprocessor;
import front.parser.processor.LALRProcessor;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class LALRProcessorTest {

    public static void main(String[] args) throws IOException {
        GrammarPreprocessor grammarPreprocessor = new GrammarPreprocessor(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\intermediate\\simple.txt"));
        LALRProcessor lalrProcessor = new LALRProcessor(grammarPreprocessor.getProductions(), grammarPreprocessor.getSymbols());
        List<Set<LROItem>> items = lalrProcessor.getItems();
        for (Set<LROItem> item : items) {
            for (LROItem it : item)
                System.out.println(it);
            System.out.println();
        }
//        List<Symbol> terminator = lalrProcessor.getTerminator();
//        List<Symbol> nonTerminator = lalrProcessor.getNonTerminator();
//
//
//        int[][] gotoTable = lalrProcessor.getGOTOTable(items, nonTerminator);
//        System.out.println(nonTerminator);
//        for (int[] ints : gotoTable) {
//            System.out.println(Arrays.toString(ints));
//        }
//
//        System.out.println();
//
//        String[][] actionTable = lalrProcessor.getACTIONTable(items, terminator);
//        System.out.println(terminator);
//        for (String[] strings : actionTable) {
//            System.out.println(Arrays.toString(strings));
//        }
    }
}
