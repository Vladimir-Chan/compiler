package front.lexer;

import utils.exceptions.CompileException;

import java.io.IOException;

public interface Lexer{
    Token nextToken() throws CompileException, IOException;
}
