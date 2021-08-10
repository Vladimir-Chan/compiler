package front.lexer;

import utils.exceptions.CompileException;

import java.io.IOException;
import java.io.Reader;

public class LexerImpl extends ToyLexer{

    public LexerImpl(Reader reader) {
        super(reader);
    }

    @Override
    public Token nextToken() throws CompileException, IOException {
        Token token = super.nextToken();
        System.out.println(token);
        return token;
    }
}
