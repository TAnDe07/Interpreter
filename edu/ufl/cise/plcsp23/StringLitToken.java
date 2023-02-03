package edu.ufl.cise.plcsp23;

public class StringLitToken implements IStringLitToken {

    public String getValue() {
        return null;
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
        return null;
    }
}
