package front.test.parser;


import front.parser.item.LRZItem;
import front.parser.processor.GrammarPreprocessor;
import front.parser.processor.LRZProcessor;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LRZProcessorTest {

    public static void main(String[] args) throws IOException {
        GrammarPreprocessor grammarPreprocessor = new GrammarPreprocessor(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\intermediate\\grammar.txt"));
        LRZProcessor lrzProcessor = new LRZProcessor(grammarPreprocessor.getProductions(), grammarPreprocessor.getSymbols());
        for (List<LRZItem> lrzItems : lrzProcessor.getLRZItemsCore()) {
            System.out.println(lrzItems);
            System.out.println();
        }
    }

}
