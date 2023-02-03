package edu.ufl.cise.plcsp23;

public class NumLitToken implements INumLitToken {

    final Token.Kind kind;
    final int pos;
    final int length;
    final String source;
    final int line;
    final int column;
    final int value;

    public NumLitToken(int pos, int length, String source, int line, int column, long i) {
        super();
        kind = Token.Kind.NUM_LIT;
        this.pos = pos;
        this.length = length;
        this.source = source;
        this.line = line;
        this.column = column;
        value = (int) i;
    }

    public int getValue() {
        return value;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    @Override
    public Kind getKind() {
        return Kind.NUM_LIT;
    }

    @Override
    public String getTokenString() {
        return source;
    }
}


