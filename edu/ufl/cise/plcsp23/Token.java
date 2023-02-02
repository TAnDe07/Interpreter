package edu.ufl.cise.plcsp23;

public class Token implements IToken {

    //Added by Tana from ppt
    public record SourceLocation(int line, int column) {}
    //Added by Tana from ppt
    public static enum Kind {
        IDENT,
        NUM_LIT,
        PLUS,
        TIMES,
        EQ,
        KW_IF,
        KW_ELSE,
        EOF,
        ERROR //may be useful
    }

    final Kind kind;
    final int line;
    final int column;
    final int length;
    final String source;


    public Token(Kind kind, int line, int column, int length, String source) {
        super();
        this.kind = kind;
        this.line = line;
        this.column = column;
        this.length = length;
        this.source = source;
    }

    public SourceLocation getSourceLocation() {
        return null;
    }

    public Kind getKind() {
        return kind;
    }

    public String getTokenString() {
        return source;
    }

    //@Override public String toString() {}
}
