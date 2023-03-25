package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.Token;
import edu.ufl.cise.plcsp23.TypeCheckException;

import java.util.HashMap;
import java.util.Stack;

public class TypeCheckVisitor implements ASTVisitor {

    int scopeCount = 0;
    Type progType;

    public static class SymbolTable {
        HashMap<String, Pair> entries = new HashMap<>();
        Stack<Integer> currScope = new Stack<Integer>();

        // returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, NameDef nameDef, int scope) {
            Pair pair = new Pair(nameDef, scope, false);
            return (entries.putIfAbsent(name, pair) == null);
        }

        // returns pair if present, or null if name not declared.
        public Pair lookup(String name) {
            return entries.get(name);
        }

        public void initialized(String name) {
            Pair pair = new Pair(entries.get(name).getFirst(), entries.get(name).getSecond(), true);
            entries.replace(name, pair);
        }

        public void enterScope(int scope) {
            this.currScope.push(scope);
        }

        public void leaveScope() {
            currScope.pop();
        }
    }

    SymbolTable symbolTable = new SymbolTable();

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        // LValue is properly typed
        statementAssign.getLv().visit(this, arg);
        // Expr is properly typed
        statementAssign.getE().visit(this, arg);
        Expr e = statementAssign.getE();
        Type eType = e.getType();
        String name = statementAssign.getLv().getIdent().getName();
        // LValue.type is assignment compatible with Expr.type
        switch (symbolTable.lookup(name).getFirst().getType()) {
            case IMAGE -> {
                if (eType == Type.INT || eType == Type.VOID) {
                    error("invalid expr type for image LValue");
                }
            }
            case PIXEL -> {
                if (eType != Type.INT && eType != Type.PIXEL) {
                    error("invalid expr type for pixel LValue");
                }
            }
            case INT -> {
                if (eType != Type.INT && eType != Type.PIXEL) {
                    error("invalid expr type for int LValue");
                }
            }
            case STRING -> {
                if (eType == Type.VOID) {
                    error("invalid expr type for string LValue");
                }
            }
            case VOID -> {
                error("LValue cannot be void");
            }
        }

        return statementAssign;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Token.Kind op = binaryExpr.getOp();
        // Expr0 and Expr1 are properly typed
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        Type resultType = null;
        switch(op) {
            case EQ -> { // ==
                if (leftType != rightType) {
                    error("incompatible types for comparison");
                }
                if (leftType == Type.VOID) {
                    error("void is not a binary expression type");
                }
                resultType = Type.INT;
            }
            case PLUS -> { // +
                if (leftType != rightType) {
                    error("incompatible types for addition");
                }
                if (leftType == Type.VOID) {
                    error("void is not a binary expression type");
                }
                resultType = leftType;
            }
            case MINUS -> { // -
                if (leftType != rightType) {
                    error("incompatible types for addition");
                }
                if (leftType == Type.VOID) {
                    error("void is not a binary expression type");
                }
                if (leftType == Type.STRING) {
                    error("incompatible type for subtraction (string)");
                }
                resultType = leftType;
            }
            case TIMES, DIV, MOD -> { // *, /, %
                if (leftType == Type.INT) {
                    if (rightType != Type.INT) {
                        error("right type incompatible with int");
                    }
                    else {
                        resultType = Type.INT;
                    }
                }
                else if (leftType == Type.PIXEL) {
                    if (rightType == Type.INT) {
                        resultType = Type.PIXEL;
                    }
                    else if (rightType == Type.PIXEL) {
                        resultType = Type.PIXEL;
                    }
                    else {
                        error("right type incompatible with pixel");
                    }
                }
                else if (leftType == Type.IMAGE) {
                    if (rightType == Type.INT) {
                        resultType = Type.IMAGE;
                    }
                    else if (rightType == Type.IMAGE) {
                        resultType = Type.IMAGE;
                    }
                    else {
                        error("right type incompatible with image");
                    }
                }
                else {
                    error("incompatible left type");
                }

            }
            case LT, LE, GT, GE, OR, AND -> { // <, <=, >, >=, ||, &&
                if (leftType != rightType) {
                    error("incompatible types for comparison");
                }
                if (leftType != Type.INT) {
                    error("int must be used");
                }
                resultType = Type.INT;
            }
            case BITAND, BITOR -> {
                if (leftType != rightType) {
                    error("incompatible types for comparison");
                }
                if (leftType != Type.PIXEL) {
                    error("pixel must be used");
                }
                resultType = Type.PIXEL;
            }
            case EXP -> {
                if (rightType != Type.INT) {
                    error("right must be int");
                }
                if (leftType == Type.INT) {
                    resultType = Type.INT;
                }
                else if (leftType == Type.PIXEL) {
                    resultType = Type.PIXEL;
                }
                else {
                    error("left type incompatible");
                }
            }
            default -> {
                error("compiler error");
            }
        }
        // BinaryExpr.type ← result type
        binaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        // DecList is properly typed
        for (int i = 0; i < block.getDecList().size(); i++) {
            block.getDecList().get(i).visit(this, arg);
        }
        // StatementList is properly typed
        for (int i = 0; i < block.getStatementList().size(); i++) {
            block.getStatementList().get(i).visit(this, arg);
        }
        return block;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        // Expr0, Expr1, and Expr2 are properly typed
        conditionalExpr.getGuard().visit(this, arg);
        conditionalExpr.getTrueCase().visit(this, arg);
        conditionalExpr.getFalseCase().visit(this, arg);
        // Expr0.type == int
        if (conditionalExpr.getGuard().getType() != Type.INT) {
            error("guard is not of type int");
        }
        // Expr1.type == Expr2.type
        if (conditionalExpr.getTrueCase().getType() != conditionalExpr.getFalseCase().getType()) {
            error("true and false cases do not have same type");
        }
        // ConditionalExpr.type ← Expr1.type
        conditionalExpr.setType(conditionalExpr.getGuard().getType());

        return conditionalExpr;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef nameDef = declaration.getNameDef();

        // If present, Expr.type must be properly typed and assignment compatible with NameDef.type.
        Expr initializer = declaration.getInitializer();
        if (initializer != null) {
            // infer type of initializer
            initializer.visit(this, arg);
            Type initializerType = declaration.getInitializer().getType();

            // not sure if this checking is correct
            switch (nameDef.getType()) {
                case IMAGE -> {
                    if (initializerType == Type.INT || initializerType == Type.VOID) {
                        error("invalid expr type for image nameDef");
                    }
                }
                case PIXEL -> {
                    if (initializerType != Type.INT && initializerType != Type.PIXEL) {
                        error("invalid expr type for pixel nameDef");
                    }
                }
                case INT -> {
                    if (initializerType != Type.INT && initializerType != Type.PIXEL) {
                        error("invalid expr type for int nameDef");
                    }
                }
                case STRING -> {
                    if (initializerType == Type.VOID) {
                        error("invalid expr type for string nameDef");
                    }
                }
                case VOID -> {
                    error("nameDef cannot be void");
                }
            }

        }
        // It is not allowed to refer to the name being defined.
        // NameDef is properly Typed
        nameDef.visit(this, arg);
        if (initializer != null) {
            String name = nameDef.getIdent().getName();
            symbolTable.initialized(name);
        }
        // If NameDef.Type == image then either it has an initializer (Expr != null)
        // or NameDef.dimension != null, or both
        if (nameDef.getType() == Type.IMAGE) {
            if (initializer == null && nameDef.getDimension() == null) {
                error("nameDef has both null expr and dimension");
            }
        }

        return declaration;
    }


    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        // Expr0 and Expr1 are properly typed
        dimension.getWidth().visit(this, arg);
        dimension.getHeight().visit(this, arg);

        Type expr1 = dimension.getHeight().type;
        Type expr2 = dimension.getHeight().type;
        // Expr0.type == int && Expr1.type == int
        if (expr1 != Type.INT || expr2 != Type.INT) {
           error("Dimension not properly typed");
        }
        return Type.INT; //?????
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        // Expr0, Expr1, and Expr2 are properly typed
        expandedPixelExpr.getBluExpr().visit(this, arg);
        expandedPixelExpr.getGrnExpr().visit(this, arg);
        expandedPixelExpr.getRedExpr().visit(this, arg);

        Type expr1 = expandedPixelExpr.getBluExpr().type;
        Type expr2 = expandedPixelExpr.getGrnExpr().type;
        Type expr3 = expandedPixelExpr.getRedExpr().type;
        Type result = null;
        // Expr0.type == int && Expr1.type == int && Expr2.type == int
        if (expr1 == Type.INT && expr2 == Type.INT && expr3 == Type.INT) {
            result = Type.PIXEL;
        }
        else {
            error("Wrong type for expandedPixelExpr");
        }
        // ExpandedPixelExpr.type ← pixel
        expandedPixelExpr.setType(result);
        return result;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        Type name = ident.def.type;
        Type result = null;
        if (name == Type.IMAGE) {
            result = Type.IMAGE;
        }
        else if (name == Type.PIXEL) {
            result = Type.PIXEL;
        }
        else if (name == Type.STRING) {
            result = Type.STRING;
        }
        else if (name == Type.INT) {
            result = Type.INT;
        }
        else {
            error("incorrect ident");
        }
        return result;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        String name = identExpr.getName();
        Pair dec = symbolTable.lookup(name);

        // IdentExpr.name has been defined
        if (dec == null) {
            error("undefined identifier " + name);
        }

        /*if (dec.getInitializer() == null) {
            error("using uninitialized variable");
        }*/

        // is visible in this scope
        if (symbolTable.currScope.search(dec.getSecond()) == -1) {
            error("ident expression is out of scope");
        }

        // identExpr.setDec(dec); // save declaration--will be useful later.
        Type type = dec.getFirst().getType();
        identExpr.setType(type);
        return type;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        // Ident has been declared
        String name = lValue.getIdent().getName();
        Pair pair = symbolTable.lookup(name);
        if (pair == null) { // null if name not declared
            error("ident not declared");
        }
        // Ident is visible in this scope
        if (symbolTable.currScope.search(pair.getSecond()) == -1) {
            error("ident expression is out of scope");
        }

        return lValue;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Dimension dim = nameDef.getDimension();
        String name = nameDef.ident.getName();

        if (dim != null) {
            // If (Dimension != ε) Type == image
            if (nameDef.getType() != Type.IMAGE) {
                error("type must be image");
            }
            // If (Dimension != ε) Dimension is properly typed
            dim.visit(this, arg);
        }
        // Ident.name has not been previously declared in this scope.
        // need to edit to include scope??
        Pair inserted = symbolTable.lookup(name);
        if (inserted != null) { // null if name not declared
            if (inserted.getSecond() == symbolTable.currScope.peek()) {
                // already declared in scope
                error("ident already declared");
            }
        }
        // Type != void
        if (nameDef.getType() == Type.VOID) {
            error("type cannot be void");
        }
        // Insert (name, NameDef) into symbol table.
        symbolTable.insert(name, nameDef, symbolTable.currScope.peek());

        return nameDef;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        // PixelSelector is properly typed
        pixelFuncExpr.getSelector().visit(this, arg);
        // PixelFunctionExpr.type ← int
        pixelFuncExpr.setType(Type.INT);
        return pixelFuncExpr;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        // Expr0,and Expr1 are properly typed
        pixelSelector.getX().visit(this, arg);
        pixelSelector.getY().visit(this, arg);

        Type expr1 = pixelSelector.getX().type;
        Type expr2 = pixelSelector.getY().type;
        // Expr0.type == int && Expr1.type == int
        if (expr1 != Type.INT || expr2 != Type.INT) {
            error("Dimension not properly typed");
        }
        return Type.INT; //?????
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        // PredeclaredVarExpr.type ← int
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        progType = program.getType();
        // call enter scope on symbol table
        symbolTable.enterScope(scopeCount);
        scopeCount++;
        // check all NameDefs are properly typed -> call visit function on all NameDefs
        for (int i = 0; i < program.getParamList().size(); i++) {
            visitNameDef(program.getParamList().get(i), arg);
            String name = program.getParamList().get(i).getIdent().getName();
            symbolTable.initialized(name);
        }
        // check if block is properly typed -> call visit function on block
        visitBlock(program.getBlock(), arg);
        // call leave scope on symbol table
        symbolTable.leaveScope();
        return program;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        // RandExpr.type ← int
        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        // Expr is properly typed
        returnStatement.getE().visit(this, arg);

        if (returnStatement.getE() instanceof IdentExpr) {
            String name = ((IdentExpr) returnStatement.getE()).getName();
            Pair pair = symbolTable.lookup(name);
            if (!pair.getInitialized()) {
                error("return value not initialized");
            }
        }

        Type type = returnStatement.getE().getType();

        // Expr.type is assignment compatible with Program.type (where Program is root of ast)
        // not sure if this checking is correct
        switch (progType) {
            case IMAGE -> {
                if (type == Type.INT || type == Type.VOID) {
                    error("invalid return type for image");
                }
            }
            case PIXEL -> {
                if (type != Type.INT && type != Type.PIXEL) {
                    error("invalid return type for pixel");
                }
            }
            case INT -> {
                if (type != Type.INT && type != Type.PIXEL) {
                    error("invalid return type for int");
                }
            }
            case STRING -> {
                if (type == Type.VOID) {
                    error("invalid return type for string");
                }
            }
            case VOID -> {
                error("there should not be a return statement");
            }
        }
        return returnStatement;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        // StringLitExpr.type ← string
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Token.Kind op = unaryExpr.getOp();
        // Expr properly typed
        Type rightType = (Type) unaryExpr.getE().visit(this, arg);
        Type resultType = null;
        if (op == IToken.Kind.BANG) {
            if (rightType == Type.INT) {
                resultType = Type.INT;
            }
            else if (rightType != Type.PIXEL) {
                resultType = Type.PIXEL;
            }
            else {
                error("incompatible types for unary");
            }
        }
        else if (op == IToken.Kind.MINUS ||
                op == IToken.Kind.RES_sin ||
                op == IToken.Kind.RES_cos ||
                op == IToken.Kind.RES_atan) {
            if (rightType != Type.INT) {
                error("incompatible types for unary");
            }
            resultType = Type.INT;;
        }
        else {
            error("incompatible op for unary");
        }
        // UnaryExpr.type ← result type
        unaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {

        Type result = null;
        PixelSelector pixel = unaryExprPostfix.getPixel();
        ColorChannel chan = unaryExprPostfix.getColor();
        // If present, PixelSelector is properly typed
        if (pixel != null) {
            pixel.visit(this, arg);
        }

        // PrimaryExpr is properly typed
        unaryExprPostfix.getPrimary().visit(this, arg);
        Type prim = unaryExprPostfix.getPrimary().getType();

        // at least one of PixelSelector or ChannelSelector should be present
        if (pixel == null && chan == null) {
            error("pixel selector and channel selector do not exist in UnaryExprPostFix");
        }


        if (prim == Type.PIXEL) {
            if (pixel != null) {
                error("pixel selector exists in UnaryExprPostFix");
            }

            result = Type.INT;
        }
        else if (prim == Type.IMAGE) {
            if (pixel != null) {
                if (chan != null) {
                    // both pixel and channel selectors
                    result = Type.INT;
                }
                else {
                    // just pixel selector
                    result = Type.PIXEL;
                }
            }
            else {
                // just channel selector
                result = Type.IMAGE;
            }
        }
        unaryExprPostfix.setType(result);
       return result;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        // Expr is properly typed
        whileStatement.getGuard().visit(this, arg);

        // Expr.type == int
        if (whileStatement.getGuard().getType() != Type.INT) {
            error("incorrect type for while statement");
        }

        // enterScope
        symbolTable.enterScope(scopeCount);
        scopeCount++;

        // Block is properly typed
        whileStatement.getBlock().visit(this, arg);

        // leaveScope
        symbolTable.leaveScope();

        return whileStatement;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        // Expr is properly typed
        statementWrite.getE().visit(this, arg);

        return statementWrite;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        // ZExpr.type ← int
        zExpr.setType(Type.INT);
        return Type.INT;
    }

    private void error(String message) throws TypeCheckException {
        throw new TypeCheckException(message);
    }
}