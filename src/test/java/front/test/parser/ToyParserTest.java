package front.test.parser;


import front.parser.item.LROItem;
import front.parser.item.Symbol;
import front.parser.processor.GrammarPreprocessor;
import front.parser.processor.LALRProcessor;
import front.parser.processor.ToyParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ToyParserTest {

    public static void main(String[] args) throws IOException {
        GrammarPreprocessor grammarPreprocessor = new GrammarPreprocessor(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\intermediate\\s2.txt"));
        LALRProcessor lalrProcessor = new LALRProcessor(grammarPreprocessor.getProductions(), grammarPreprocessor.getSymbols());
        List<Set<LROItem>> items = lalrProcessor.getItems();
        List<Symbol> terminator = lalrProcessor.getTerminator();
        List<Symbol> nonTerminator = lalrProcessor.getNonTerminator();
        int[][] gotoTable = lalrProcessor.getGOTOTable(items, nonTerminator);
        String[][] actionTable = lalrProcessor.getACTIONTable(items, terminator);
        ToyParser toyParser = new ToyParser(grammarPreprocessor.getProductions(), terminator, nonTerminator, actionTable, gotoTable);
        toyParser.grammaticalAnalysis("n*i(di){ci;}");
    }
}
