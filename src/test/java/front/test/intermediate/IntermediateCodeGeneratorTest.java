package front.test.intermediate;


import front.intermediate.processor.IntermediateCodeGenerator;
import front.lexer.LexerImpl;
import front.parser.item.LROItem;
import front.parser.item.Symbol;
import front.parser.processor.GrammarPreprocessor;
import front.parser.processor.LALRProcessor;
import front.parser.processor.ParseImpl;
import utils.exceptions.CompileException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class IntermediateCodeGeneratorTest {

    public static void main(String[] args) throws IOException, CompileException {
        GrammarPreprocessor grammarPreprocessor = new GrammarPreprocessor(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\intermediate\\s3.txt"));
        LALRProcessor lalrProcessor = new LALRProcessor(grammarPreprocessor.getProductions(), grammarPreprocessor.getSymbols());
        List<Set<LROItem>> items = lalrProcessor.getItems();
        List<Symbol> terminator = lalrProcessor.getTerminator();
        List<Symbol> nonTerminator = lalrProcessor.getNonTerminator();
        int[][] gotoTable = lalrProcessor.getGOTOTable(items, nonTerminator);
        String[][] actionTable = lalrProcessor.getACTIONTable(items, terminator);
        IntermediateCodeGenerator intermediateCodeGenerator = new IntermediateCodeGenerator(new LexerImpl(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\lexical\\qsort.txt")), new ParseImpl(grammarPreprocessor.getProductions(), terminator, nonTerminator, actionTable, gotoTable));
        intermediateCodeGenerator.run();
        intermediateCodeGenerator.outputCodes("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\lexical\\rq.txt");
    }
}
