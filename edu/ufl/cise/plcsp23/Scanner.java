package edu.ufl.cise.plcsp23;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner implements IScanner {

    final String input;
    final char[] inputChars;
    int pos;    // position within input
    char ch;    // current character
    int line;   // line number of character
    int column; // column number of character
    private static HashMap<String, Token.Kind> reserved;

    public Scanner (String input) {
        this.input = input;
        inputChars = new char[input.length() + 1];

        for (int i = 0; i < input.length(); i++) {  // input + 0 for EOF
            inputChars[i] = input.charAt(i);
        }

        pos = 0;
        line = 0;
        column = 0;
        ch = inputChars[pos];
    }

    @Override
    public Token next() throws LexicalException {
        Token token = scanToken();
        return token;
    }

    private enum State {
        START,
        HAVE_EQ,
        HAVE_AST,
        IN_IDENT,
        IN_NUM_LIT,
        IN_STRING
    }

    static { // add reserved words
        reserved = new HashMap<String, Token.Kind>();
        reserved.put("if", Token.Kind.RES_if);
    }

    private Token scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1;
        while(true) {
            switch (state) {
                case START -> {
                    tokenStart = pos;
                    switch (ch) {
                        case 0 -> { // EOF
                            return new Token(Token.Kind.EOF, tokenStart, 0, inputChars, line, column);
                        }
                        case ' ', '\r', '\t', '\f' -> next();
                        case '\n' -> {
                            line++;
                            column = 0;
                        }
                        case '+' -> {
                            return new Token(Token.Kind.PLUS, tokenStart, 1, inputChars, line, column);
                        }
                        case '0' -> {
                            return new Token(Token.Kind.NUM_LIT, tokenStart, 1, inputChars, line, column);
                        }
                        case '*' -> {
                            state = state.HAVE_AST;
                            next();
                            //return new Token(Token.Kind.TIMES, tokenStart, 1, inputChars, line, column);
                        }
                        case '=' -> {
                            state = state.HAVE_EQ;
                            next();
                        }
                        case '1','2','3','4','5','6','7','8','9' -> {//char is nonzero digit
                            state = State.IN_NUM_LIT;
                            next();
                        }
                        default -> {
                            if (isLetter(ch)) {
                                state = State.IN_IDENT;
                                next();
                            }
                            else error("illegal char with ascii value: " + (int)ch);}
                    }
                }
                case HAVE_EQ -> {
                    if (ch == '=') {
                        state = state.START;
                        next();
                        return new Token(Token.Kind.EQ, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        error("expected =");
                    }
                }
                case HAVE_AST -> {
                    if (ch == '*') {
                        state = state.START;
                        next();
                        return new Token(Token.Kind.EXP, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        error("expected =");
                    }
                }
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) {//char is digit, continue in IN_NUM_LIT state
                        next();
                    }
                    else {
                        //current char belongs to next token, so don't get next char
                        int length = pos - tokenStart;
                        return new Token(Token.Kind.NUM_LIT, tokenStart, length, inputChars, line, column);
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        next();
                    }
                    else {
                        //current char belongs to next token, so don't get next char
                        int length = pos-tokenStart;
                        //determine if this is a reserved word. If not, it is an ident.
                        String text = input.substring(tokenStart, tokenStart + length);
                        Token.Kind kind = reserved.get(text);
                        if (kind == null){ kind = Token.Kind.IDENT; }
                        return new Token(kind, tokenStart, length, inputChars, line, column);
                    }
                }
                default -> {
                    throw new UnsupportedOperationException("Bug in Scanner");
                }
            }
        }

    }

    private boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }
    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }
    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '_');
    }
    private void error(String message) throws LexicalException {
        throw new LexicalException("Error at pos " + pos + ": " + message);
    }
    
}

