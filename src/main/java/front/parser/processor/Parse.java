package front.parser.processor;


import front.lexer.Token;
import front.parser.item.Production;
import front.parser.item.Symbol;
import utils.exceptions.CompileException;

public interface Parse {
    int grammaticalAnalysis(Symbol symbol) throws CompileException;
//    int grammaticalAnalysis(Token token);
    Production getProduction(int index);
    Symbol transfer(Token token) throws CompileException;
}
