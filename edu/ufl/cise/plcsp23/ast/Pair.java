package edu.ufl.cise.plcsp23.ast;

public class Pair {
    NameDef nameDef;
    int scope;

    Pair(NameDef nameDef, int scope) {
        this.nameDef = nameDef;
        this.scope = scope;
    }

    public NameDef getFirst() {
        return nameDef;
    }

    public int getSecond() {
        return scope;
    }
}
