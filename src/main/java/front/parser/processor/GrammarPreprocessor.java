package front.parser.processor;

import front.parser.item.Production;
import front.parser.item.Symbol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * 对文法进行预处理，得到文法中的产生式
 * 产生式格式： A -> B | C D
 * 由于产生式中会包含 | 符号，并且产生式左边只会有一个文法符号，因此 | 用$代替，并且去掉->
 * 上面的产生式应写为  A B $ C D
 * 文法的开始符号一定要为 S，方便后续处理
 */
public class GrammarPreprocessor {
    private final List<Production> productions;
    private final Set<Symbol> symbols;

    public GrammarPreprocessor(Reader reader) throws IOException {
        productions = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        Set<String> terminators = new HashSet<>(Arrays.asList(line.split(" ")));
        symbols = new HashSet<>(); // 所有文法符号
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            int splitPoint = line.indexOf(" ");
            if (splitPoint == -1) {
                System.out.println(line);
                continue;
            }
            String[] strings1 = line.substring(splitPoint + 1).split("\\$");
            Symbol left = new Symbol(line.substring(0, splitPoint), terminators.contains(line.substring(0, splitPoint)));
            symbols.add(left);
            for (String str1 : strings1) {
                ArrayList<Symbol> right = new ArrayList<>();
                String[] strings2 = str1.trim().split(" ");
                for (String str2 : strings2)
                    right.add(new Symbol(str2, terminators.contains(str2)));
                symbols.addAll(right);
                productions.add(new Production(left, right));
            }
        }
        productions.add(0, new Production(new Symbol(productions.get(0).getLeft().getValue() + "'", false), productions.get(0).getLeft()));
//        return new GrammarProcessor(productions, startSymbol, symbols);
    }

    public List<Production> getProductions() {
        return productions;
    }

    public Set<Symbol> getSymbols() {
        return symbols;
    }
}
