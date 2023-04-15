package edu.ufl.cise.plcsp23.ast;

public class Pair {
    NameDef nameDef;
    int scope;
    boolean initialized;

    Pair(NameDef nameDef, int scope, boolean initialized) {
        this.nameDef = nameDef;
        this.scope = scope;
        this.initialized = initialized;
    }

    public NameDef getFirst() {
        return nameDef;
    }

    public int getSecond() {
        return scope;
    }

    public boolean getInitialized() {
        return initialized;
    }
}
