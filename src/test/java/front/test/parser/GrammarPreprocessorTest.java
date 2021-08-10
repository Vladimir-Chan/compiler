package front.test.parser;


import front.parser.processor.GrammarPreprocessor;

import java.io.FileReader;
import java.io.IOException;

public class GrammarPreprocessorTest {
    public static void main(String[] args) throws IOException {
        GrammarPreprocessor grammarPreprocessor = new GrammarPreprocessor(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\4-49.txt"));
        System.out.println(grammarPreprocessor.getProductions());
        System.out.println(grammarPreprocessor.getSymbols());
    }
}
