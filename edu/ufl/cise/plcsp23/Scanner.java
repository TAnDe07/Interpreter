package edu.ufl.cise.plcsp23;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner implements IScanner {
    final String input;
    final List<Token> tokens = new ArrayList<>();
    private static final Map<String, Token.Kind> reserved;

    static { // add reserved words
        reserved = new HashMap<>();
        reserved.put("if", Token.Kind.RES_if);
    }


    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String input) {
        this.input = input;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(Token.Kind.EOF, line, current, 0, "")); // change parameters
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '('-> addToken(Token.Kind.LPAREN); break;
            case ')'-> addToken(Token.Kind.RPAREN); break;
            case '{'-> addToken(Token.Kind.LCURLY); break;
            case '}'-> addToken(Token.Kind.RCURLY); break;
            case ','-> addToken(Token.Kind.COMMA); break;
            case '.'-> addToken(Token.Kind.DOT); break;
            case '-'-> addToken(Token.Kind.MINUS); break;
            case '+'-> addToken(Token.Kind.PLUS); break;
            case '!'-> addToken(Token.Kind.BANG); break;
            case '?'-> addToken(Token.Kind.QUESTION); break;
            case ':'-> addToken(Token.Kind.COLON); break;
            case ']'-> addToken(Token.Kind.RSQUARE); break;
            case '['-> addToken(Token.Kind.LSQUARE); break;
            case '='->
                    addToken(match('=') ? Token.Kind.EQ : Token.Kind.ASSIGN);
                break;
            case '<' -> {
                if (match('=')) {
                    addToken(Token.Kind.LE);
                }
                else if (match('-')) {
                    if (match('>')) {
                        addToken(Token.Kind.EXCHANGE);
                    }
                    else {
                        addToken(Token.Kind.LT);
                        addToken(Token.Kind.MINUS);
                    }
                }
                else {
                    addToken(Token.Kind.LT);
                }

                break;
            }
            case '>'->
                addToken(match('=') ? Token.Kind.GE : Token.Kind.GT);
                break;
            case '&'->
                addToken(match('&') ? Token.Kind.AND : Token.Kind.BITAND);
                break;
            case '|'->
                addToken(match('|') ? Token.Kind.OR : Token.Kind.BITOR);
                break;
            case '*'->
                addToken(match('*') ? Token.Kind.EXP : Token.Kind.TIMES);
                break;
            case '/'-> addToken(Token.Kind.DIV); break;
            case '%'-> addToken(Token.Kind.MOD); break;
            case '~'-> {
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd()) advance();
                break;
            }
            case ' ' -> {
            }
            case '\r'-> {}
            case '\t'-> {}
            case '\b' -> {
                // Ignore whitespace.
            }
            case '\n' -> line++;
            break;
            case '\"' -> string();
            default -> {
                if (isDigit(c)) {
                    if (c == '0') { // 0 at beginning can only be by itself
                        addToken(Token.Kind.NUM_LIT);
                    }
                    else {
                        number();
                    }
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {  // not a recognized character
                    new LexicalException("illegal char with ascii value: " + (int) c);
                }
                break;
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance(); // check until no more letter, digits, or _

        String text = input.substring(start, current);
        Token.Kind type = reserved.get(text); // check reserved list for text
        if (type == null) type = Token.Kind.IDENT; // if not in list, it's an identifier
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance(); // check until no more digits

        addToken(Token.Kind.NUM_LIT);
    }

    private void string() { // edit to exclude " and \ and includes escape sequences
        while (peek() != '"' && !isAtEnd()) {   // while second parenthesis not reached and not end of file

            advance();
        }

        if (isAtEnd()) { // string starts but doesn't end
            new PLCException("Unterminated string at line " + line);
            return;
        }

        // The closing ".
        advance();

        addToken(Token.Kind.STRING_LIT);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (input.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= input.length()) return '\0';
        return input.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= input.length();
    }

    private char advance() {
        return input.charAt(current++);
    }

    private void addToken(Token.Kind type) {
        String text;
        if (type == Token.Kind.STRING_LIT) {
            text = input.substring(start + 1, current + 1); // remove quotes
        }
        else {
            text = input.substring(start, current);
        }
        tokens.add(new Token(type, line, start, current - start, text));
    }


    @Override
    public Token next() throws LexicalException {
        return null;
    }

    /*private enum State {
        START,
        HAVE_EQ,
        IN_IDENT,
        IN_NUM_LIT
    }*/

    //private Token scanToken() throws LexicalException {




        /*State state = State.START;
        int tokenStart = -1;
        while(true) {
            switch (state) {
                case START -> {
                    tokenStart = pos;
                    switch (ch) {
                        case 0-> {
                            return new Token(Token.Kind.EOF, tokenStart, 0, inputCharacters);
                        }
                        case ' ', '\n', '\r', '\t', '\f' -> next();
                        case '+' -> {
                            return new Token(Token.Kind.PLUS, tokenStart, 1, inputCharacters);
                        }
                        case '*' -> {
                            return new Token(Token.Kind.TIMES, tokenStart, 1, inputCharacters);
                        }
                        case '0' -> {
                        return new Token(Token.Kind.NUM_LIT, tokenStart, 1, inputCharacters);
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
                        return new Token(Token.Kind.EQ, tokenStart, 2, inputCharacters);
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
                        int length = pos-tokenStart;
                        return new Token(Token.Kind.NUM_LIT, tokenStart, length, inputCharacters);
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        next();
                    }
                    else {//
                        //current char belongs to next token, so don't get next char
                        int length = pos-tokenStart;
                        //determine if this is a reserved word. If not, it is an ident.
                        String text = input.substring(tokenStart, tokenStart + length);
                        Token.Kind kind = reservedWords.get(text);
                        if (kind == null){ kind = Token.Kind.IDENT; }
                        return new Token(kind, tokenStart, length, inputCharacters);
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
        return isLetter(ch) || (ch == '$') || (ch == '_');
    }
    private void error(String message) throws LexicalException{
        throw new LexicalException("Error at pos " + pos + ": " + message);
    }
}*/

       // return null;
    //}
}