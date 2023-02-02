package edu.ufl.cise.plcsp23;

public class NumLitToken {

    public NumLitToken(IToken.Kind kind, int line, int column, int length, String source) {
        super();
        this.kind = kind;
        this.line = line;
        this.column = column;
        this.length = length;
        this.source = source;
    }

    public INumLitToken.SourceLocation getSourceLocation() {
        return null;
    }

    public INumLitToken.Kind getKind() {
        return kind;
    }

    public String getNumLitString() {
        return source;
    }
}


