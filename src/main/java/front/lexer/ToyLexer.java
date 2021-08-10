package front.lexer;

import utils.exceptions.CompileException;

import java.io.*;

public class ToyLexer implements Lexer {
    private final LineNumberReader inputStream;
    private final StringBuilder valueBuilder;

    public ToyLexer(Reader reader) {
        this.inputStream = new LineNumberReader(reader);
        inputStream.setLineNumber(1);
        this.valueBuilder = new StringBuilder();
    }

    @Override
    public Token nextToken() throws CompileException, IOException {
        valueBuilder.delete(0, valueBuilder.length());
        int a;
        do {
            inputStream.mark(3);
            a = inputStream.read();
        }
        while (Character.isWhitespace(a));
        if (a == -1) {
            inputStream.close();
            return null;
        }
        else if (Character.isDigit(a)) {
            inputStream.reset();
            int flag = getNumConstant();
            if (flag == 1 || flag == 7)
                return new Token(2, valueBuilder.toString());
            else if (flag == 3 || flag == 5)
                return new Token(3, valueBuilder.toString());
            else
                throw new CompileException();
        }
        else if (Character.isLetter(a) || (char)a == '_') {
            inputStream.reset();
            getWords();
            return new Token(1, valueBuilder.toString());
        }
        else if ((char)a == '\'')
            return getCharacterConstant(a);
        else if ((char)a == '"')
            return getStringConstant(a);
        else
            return getSymbol(a);
    }

    private int getNumConstant() throws IOException, CompileException {
        int a, flag;
        for (a = inputStream.read(), flag = 1;; inputStream.mark(3), a = inputStream.read()) {
            if (Character.isDigit(a)) {
                valueBuilder.append((char)a);
                if (flag == 2 || flag == 4 || flag == 6)
                    flag ++;
            }
            else if ((char)a == '.')
                if (flag == 1) {
                    valueBuilder.append((char)a);
                    flag = 2;
                }
                else
                    throw new CompileException();
            else if ((char)a == 'E' || (char)a == 'e')
                if (flag == 1) {
                    valueBuilder.append((char)a);
                    flag = 6;
                }
                else if (flag == 3) {
                    valueBuilder.append((char)a);
                    flag = 4;
                }
                else
                    break;
            else
                break;
        }
        inputStream.reset();
        return flag;
    }

    private void getWords() throws IOException {
        for (int a = inputStream.read(); Character.isLetterOrDigit(a)|| (char)a == '_'; inputStream.mark(3), a = inputStream.read())
            valueBuilder.append((char)a);
        inputStream.reset();
    }

    private Token getCharacterConstant(int a) throws IOException, CompileException {
        valueBuilder.append((char)a);
        a = inputStream.read();
        if (a == -1)
            throw new CompileException();
        valueBuilder.append((char)a);
        if ((char)a == '\\') {
            a = inputStream.read();
            if (a == -1)
                throw new CompileException();
            else
                valueBuilder.append((char)a);
        }
        a = inputStream.read();
        if ((char)a == '\'') {
            valueBuilder.append((char)a);
            return new Token(4, valueBuilder.toString());
        }
        else
            throw new CompileException();
    }

    private Token getStringConstant(int a) throws IOException, CompileException {
        do {
            valueBuilder.append((char)a);
            inputStream.mark(3);
            a = inputStream.read();
        }while (a != -1 && (char)a != '"');
        if (a == -1)
            throw new CompileException();
        else {
            valueBuilder.append((char)a);
            return new Token(5, valueBuilder.toString());
        }
    }

    private Token getSymbol(int a) throws IOException, CompileException {
        valueBuilder.append((char)a);
        int aa;
        switch ((char)a) {
            case '>':
            case '<':
                inputStream.mark(3);
                aa = inputStream.read();
                if (aa == a || (char)aa == '=')
                    valueBuilder.append((char) aa);
                else
                    inputStream.reset();;
                return new Token(6, valueBuilder.toString());
            case '=':
            case '!':
                inputStream.mark(3);
                a = inputStream.read();
                if ((char)a == '=')
                    valueBuilder.append((char)a);
                else
                    inputStream.reset();
                return new Token(6, valueBuilder.toString());
            case '&':
            case '|':
            case '+':
                inputStream.mark(3);
                aa = inputStream.read();
                if (a == aa)
                    valueBuilder.append((char)aa);
                else
                    inputStream.reset();
                return new Token(6, valueBuilder.toString());
            case '-':
                inputStream.mark(3);
                aa = inputStream.read();
                if (a == aa || (char)aa == '>')
                    valueBuilder.append((char)aa);
                else
                    inputStream.reset();
                return new Token(6, valueBuilder.toString());
            case '*':
            case '/':
            case '%':
            case '~':
            case '.':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
            case ',':
            case ';':
            case '#':
            case ':':
                return new Token(6, valueBuilder.toString());
            default:
                throw new CompileException();
        }
    }
}
