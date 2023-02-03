package edu.ufl.cise.plcsp23;

import java.util.HashMap;

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
        line = 1;
        column = 0;
        ch = inputChars[0];
    }

    @Override
    public IToken next() throws LexicalException {
        IToken token = scanToken();
        return token;
    }

    private enum State {
        START,
        HAVE_EQ,
        HAVE_AST,
        HAVE_LESS,
        HAVE_GREAT,
        HAVE_AND,
        HAVE_OR,
        EXCHANGE,
        IN_IDENT,
        IN_NUM_LIT,
        IN_STRING,
        COMMENT
    }

    static { // add reserved words
        reserved = new HashMap<>();
        reserved.put("image", Token.Kind.RES_image);
        reserved.put("pixel", Token.Kind.RES_pixel);
        reserved.put("int", Token.Kind.RES_int);
        reserved.put("string", Token.Kind.RES_string);
        reserved.put("void", Token.Kind.RES_void);
        reserved.put("nil", Token.Kind.RES_nil);
        reserved.put("load", Token.Kind.RES_load);
        reserved.put("display", Token.Kind.RES_display);
        reserved.put("write", Token.Kind.RES_write);
        reserved.put("x", Token.Kind.RES_x);
        reserved.put("y", Token.Kind.RES_y);
        reserved.put("a", Token.Kind.RES_a);
        reserved.put("r", Token.Kind.RES_r);
        reserved.put("X", Token.Kind.RES_X);
        reserved.put("Y", Token.Kind.RES_Y);
        reserved.put("Z", Token.Kind.RES_Z);
        reserved.put("x_cart", Token.Kind.RES_x_cart);
        reserved.put("y_cart", Token.Kind.RES_y_cart);
        reserved.put("a_polar", Token.Kind.RES_a_polar);
        reserved.put("r_polar", Token.Kind.RES_r_polar);
        reserved.put("rand", Token.Kind.RES_rand);
        reserved.put("sin", Token.Kind.RES_sin);
        reserved.put("cos", Token.Kind.RES_cos);
        reserved.put("atan", Token.Kind.RES_atan);
        reserved.put("if", Token.Kind.RES_if);
        reserved.put("while", Token.Kind.RES_while);
    }

    private IToken scanToken() throws LexicalException {
        State state = State.START;
        column++;
        int tokenStart = -1;
        while(true) {
            ch = inputChars[pos];
            switch (state) {
                case START -> {
                    tokenStart = pos;
                    switch (ch) {
                        case 0 -> { // EOF
                            return new Token(Token.Kind.EOF, tokenStart, 0, inputChars, line, column);
                        }
                        // whitespace
                        case ' ', '\r', '\t', '\f' -> {
                            pos++; // whitespace, ignore
                            if (column == 0) {
                                column += 2;
                            }
                            else {
                                column++;
                            }
                        }
                        case '\n' -> {  // newline, increment line but otherwise ignore
                            line++;
                            column = 0;
                            pos++;
                        }
                        //one offs
                        case '+' -> {
                            pos++;
                            return new Token(Token.Kind.PLUS, tokenStart, 1, inputChars, line, column);
                        }
                        case '.' -> {
                            pos++;
                            return new Token(Token.Kind.DOT, tokenStart, 1, inputChars, line, column);
                        }
                        case ',' -> {
                            pos++;
                            return new Token(Token.Kind.COMMA, tokenStart, 1, inputChars, line, column);
                        }
                        case '?' -> {
                            pos++;
                            return new Token(Token.Kind.QUESTION, tokenStart, 1, inputChars, line, column);
                        }
                        case ':' -> {
                            pos++;
                            return new Token(Token.Kind.COLON, tokenStart, 1, inputChars, line, column);
                        }
                        case '(' -> {
                            pos++;
                            return new Token(Token.Kind.LPAREN, tokenStart, 1, inputChars, line, column);
                        }
                        case ')' -> {
                            pos++;
                            return new Token(Token.Kind.RPAREN, tokenStart, 1, inputChars, line, column);
                        }
                        case '[' -> {
                            pos++;
                            return new Token(Token.Kind.LSQUARE, tokenStart, 1, inputChars, line, column);
                        }
                        case ']' -> {
                            pos++;
                            return new Token(Token.Kind.RSQUARE, tokenStart, 1, inputChars, line, column);
                        }
                        case '{' -> {
                            pos++;
                            return new Token(Token.Kind.LCURLY, tokenStart, 1, inputChars, line, column);
                        }
                        case '}' -> {
                            pos++;
                            return new Token(Token.Kind.RCURLY, tokenStart, 1, inputChars, line, column);
                        }
                        case '!' -> {
                            pos++;
                            return new Token(Token.Kind.BANG, tokenStart, 1, inputChars, line, column);
                        }
                        case '-' -> {
                            pos++;
                            return new Token(Token.Kind.MINUS, tokenStart, 1, inputChars, line, column);
                        }
                        case '/' -> {
                            pos++;
                            return new Token(Token.Kind.DIV, tokenStart, 1, inputChars, line, column);
                        }
                        case '%' -> {
                            pos++;
                            return new Token(Token.Kind.MOD, tokenStart, 1, inputChars, line, column);
                        }
                        case '0' -> {
                            pos++;
                            long i = 0;
                            return new NumLitToken(tokenStart, 1, "0", line, column, i);
                        }
                        // 2-3 chars
                        case '*' -> {
                            state = state.HAVE_AST;
                            pos++;
                        }
                        case '=' -> {
                            state = state.HAVE_EQ;
                            pos++;
                        }
                        case '<' -> {
                            state = state.HAVE_LESS;
                            pos++;
                        }
                        case '>' -> {
                            state = state.HAVE_GREAT;
                            pos++;
                        }
                        case '&' -> {
                            state = state.HAVE_AND;
                            pos++;
                        }
                        case '|' -> {
                            state = state.HAVE_OR;
                            pos++;
                        }
                        // extended cases
                        case '1','2','3','4','5','6','7','8','9' -> { //char is nonzero digit
                            state = State.IN_NUM_LIT;
                            pos++;
                        }
                        case '~' -> {
                            state = state.COMMENT;
                            pos++;
                        }
                        case '\"' -> {
                            state = state.IN_STRING;
                            pos++;
                        }
                        default -> {
                            if (isLetter(ch)) {
                                state = State.IN_IDENT;
                                pos++;
                            }
                            else error("illegal char with ascii value: " + (int)ch);}
                    }
                }
                case HAVE_EQ -> {
                    if (ch == '=') {
                        pos++;
                        return new Token(Token.Kind.EQ, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        return new Token(Token.Kind.ASSIGN, tokenStart, 2, inputChars, line, column);
                    }
                }
                case HAVE_AST -> {
                    if (ch == '*') {
                        pos++;
                        return new Token(Token.Kind.EXP, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        return new Token(Token.Kind.TIMES, tokenStart, 2, inputChars, line, column);
                    }
                }
                case HAVE_LESS -> {
                    if (ch == '=') {
                        pos++;
                        return new Token(Token.Kind.LE, tokenStart, 2, inputChars, line, column);
                    }
                    else if (ch == '-') {
                        state = state.EXCHANGE;
                        pos++;
                    }
                    else {
                        return new Token(Token.Kind.LT, tokenStart, 2, inputChars, line, column);
                    }
                }
                case EXCHANGE -> {
                    if (ch == '>') {
                        pos++;
                        return new Token(Token.Kind.EXCHANGE, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        error("expected >");
                    }
                }
                case HAVE_GREAT -> {
                    if (ch == '=') {
                        pos++;
                        return new Token(Token.Kind.GE, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        return new Token(Token.Kind.GT, tokenStart, 2, inputChars, line, column);
                    }
                }
                case HAVE_AND -> {
                    if (ch == '&') {
                        pos++;
                        return new Token(Token.Kind.AND, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        return new Token(Token.Kind.BITAND, tokenStart, 2, inputChars, line, column);
                    }
                }
                case HAVE_OR -> {
                    if (ch == '|') {
                        pos++;
                        return new Token(Token.Kind.OR, tokenStart, 2, inputChars, line, column);
                    }
                    else {
                        return new Token(Token.Kind.BITOR, tokenStart, 2, inputChars, line, column);
                    }
                }
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) { //char is digit, continue in IN_NUM_LIT state
                        pos++;
                    }
                    else {
                        //current char belongs to next token, so don't get next char
                        int length = pos - tokenStart;

                        String value = "";
                        int length2 = length;
                        int pos2 = tokenStart;
                        while (length2 > 0){
                            value += inputChars[pos2];
                            pos2++;
                            length2--;
                        }

                        try {
                            long i = Long.parseLong(value);
                        }
                        catch (NumberFormatException nfe) {
                            error("Number too large");
                        }

                        long i = Long.parseLong(value);

                        return new NumLitToken(tokenStart, length, value, line, column, i);
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch) || isDigit(ch)) {
                        pos++;
                    }
                    else {
                        //current char belongs to next token, so don't get next char
                        int length = pos - tokenStart;
                        //determine if this is a reserved word. If not, it is an ident.
                        String text = input.substring(tokenStart, pos);
                        Token.Kind kind = reserved.get(text);
                        if (kind == null){ kind = Token.Kind.IDENT; }
                        return new Token(kind, tokenStart, length, inputChars, line, column);
                    }
                }
                case COMMENT -> {
                    if (ch == '\n') {
                        state = State.START;
                        line++;
                        column = 1;
                    }
                    pos++;
                }
                case IN_STRING -> {
                    if (ch == '\"') {
                        int length = pos - tokenStart;
                        pos++;
                        return new StringLitToken(tokenStart, length, inputChars, line, column);
                    }
                    else if (ch == '\\') {
                        pos++;
                        ch = inputChars[pos];
                        if (ch == '\\') {
                            error("lone \\ located in string");
                        }
                        else if (ch == 't') {
                            pos++;
                        }
                        else if (ch == 'b') {
                            pos++;
                        }
                        else if (ch == 'n') {
                            line++;
                            pos++;
                        }
                        else if (ch == 'r') {
                            pos++;
                        }
                        else {
                            error("\\ followed by illegal character" );
                        }
                    }
                    else {
                        if (ch == '\n') {
                            line++;
                        }
                        pos++;
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

