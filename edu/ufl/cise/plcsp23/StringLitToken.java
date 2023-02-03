package edu.ufl.cise.plcsp23;

public class StringLitToken implements IStringLitToken {

    final Token.Kind kind;
    final int pos;
    final int length;
    final char[] source;
    final int line;
    final int column;

    public StringLitToken(int pos, int length, char[] source, int line, int column) {
        super();
        kind = Token.Kind.NUM_LIT;
        this.pos = pos;
        this.length = length;
        this.source = source;
        this.line = line;
        this.column = column;
    }

    public String getValue() {
        String value = getTokenString();
        String temp = "\\\\";
        value.replaceAll(temp + 'b', "\b");
        value.replaceAll(temp + 't', "\t");
        value.replaceAll(temp + 'n', "\n");
        value.replaceAll(temp + 'r', "\r");
        value.replaceAll(temp + '\"', "\"");
        value.replaceAll(temp + "\\\\", "\\");

        return value;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    @Override
    public Kind getKind() {
        return Kind.STRING_LIT;
    }

    @Override
    public String getTokenString() {
        String value = "";

        if (length == 2) {
            return value;
        }

        String string = new String(source);

        value = string.substring(pos, pos + (length - 2));

        return value;
    }
}
