package front.test.lexer;


import front.lexer.Lexer;
import front.lexer.Token;
import front.lexer.ToyLexer;
import utils.exceptions.CompileException;

import java.io.FileReader;
import java.io.IOException;

public class ToyLexerTest {

    public static void nextTokenTest() throws IOException, CompileException {
        Lexer lexer = new ToyLexer(new FileReader("D:\\JetBrainsProjects\\IDEA\\compiler\\front\\resource\\lexical\\c.txt"));
        Token token;
        do {
            token = lexer.nextToken();
            System.out.println(token);
        } while (token != null);
    }

    public static void main(String[] args) {
        try {
            nextTokenTest();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (CompileException compileException) {
            System.out.println("编译错误");
        }
    }
}
