package edu.ufl.cise.plcsp23;

public class Token implements IToken {

    public record SourceLocation(int line, int column) {}


    final Kind kind;
    final int pos;
    final int length;
    final char[] source;
    final int line;
    final int column;


    public Token(Kind kind, int pos, int length, char[] source, int line, int column) {
        super();
        this.kind = kind;
        this.pos = pos;
        this.length = length;
        this.source = source;
        this.line = line;
        this.column = column;
    }

    public IToken.SourceLocation getSourceLocation() {
        return null;
    } // send help idk

    public Kind getKind() {
        return kind;
    }

    public String getTokenString() {
        String value = new String(source);
        value = value.substring(pos, pos + length);
        return value;
    }

    @Override public String toString() {return "";} // idk
}
