package edu.ufl.cise.plcsp23;

public class Scanner implements IScanner{
    final String input;
    final char[] inputCharacters;

    int pos;
    char ch;

    public Scanner(String input){
        this.input = input;
        inputCharacters = input.toCharArray();
        pos = 0;
        ch = inputCharacters[pos];
    }
    @Override
    public Token next() throws LexicalException {
        return null;
    }

    private enum State {
        START,
        HAVE_EQ,
        IN_IDENT,
        IN_NUM_LIT
    }

    private Token scanToken() throws LexicalException {
        State state = State.START;
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
}
