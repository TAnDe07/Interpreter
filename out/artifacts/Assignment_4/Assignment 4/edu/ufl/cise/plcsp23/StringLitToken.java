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
        kind = Kind.STRING_LIT;
        this.pos = pos;
        this.length = length;
        this.source = source;
        this.line = line;
        this.column = column;
    }

    public String getValue() {

        String value = getTokenString();
        value = value.substring(1, length);

        String newValue = "";

        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\') {
                if ((i + 1) < value.length()) {
                    if (value.charAt(i + 1) == 'b') {
                        newValue += "\b";
                    }
                    else if (value.charAt(i + 1) == 't') {
                        newValue += "\t";
                    }
                    else if (value.charAt(i + 1) == 'n') {
                        newValue += "\n";
                    }
                    else if (value.charAt(i + 1) == 'r') {
                        newValue += "\r";
                    }
                    else if (value.charAt(i + 1) == '\"') {
                        newValue += "\"";
                    }
                    else if (value.charAt(i + 1) == '\\') {
                        newValue += "\\";
                    }
                }
                i++;
            }
            else {
                newValue += value.charAt(i);
            }
        }

        return newValue;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return new IToken.SourceLocation(line, column);
    }

    @Override
    public Kind getKind() {
        return Kind.STRING_LIT;
    }

    @Override
    public String getTokenString() {
        String value = new String(source);
        value = value.substring(pos, pos + 1 + length );

        return value;
    }
}
