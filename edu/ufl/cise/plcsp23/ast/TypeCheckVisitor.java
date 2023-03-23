package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.PLCException;
import java.util.HashMap;

public class TypeCheckVisitor implements ASTVisitor {

    public static class SymbolTable {
        HashMap<String, Declaration> entries = new HashMap<>();

        // returns true if name successfully inserted in symbol table, false if already present
        public boolean insert(String name, Declaration declaration) {
            return (entries.putIfAbsent(name, declaration) == null);
        }

        // returns Declaration if present, or null if name not declared.
        public Declaration lookup(String name) {
            return entries.get(name);
        }
    }

    SymbolTable symbolTable = new SymbolTable();

    private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
        if (!condition) {
            throw new TypeCheckException(message, node.getSourceLoc());
        }
    }

    private boolean assignmentCompatible(Type targetType, Type rhsType) {
        return (targetType == rhsType
                || targetType == Type.STRING && rhsType == Type.INT
                || targetType == Type.STRING && rhsType == Type.BOOLEAN
        );
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Kind op = binaryExpr.getOp().getKind();
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        Type resultType = null;
        switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE
            case EQUALS,NOT_EQUALS -> {
                check(leftType == rightType, binaryExpr, "incompatible types for comparison");
                resultType = Type.BOOLEAN;
            }
            case PLUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
                else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case MINUS -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case TIMES -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case DIV -> {
                if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            case LT, LE, GT, GE -> {
                if (leftType == rightType) resultType = Type.BOOLEAN;
                else check(false, binaryExpr, "incompatible types for operator");
            }
            default -> {
                throw new Exception("compiler error");
            }
        }
        binaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        String name = declaration.getName();
        boolean inserted = symbolTable.insert(name,declaration);
        check(inserted, declaration, "variable " + name + "already declared");
        Expr initializer = declaration.getInitializer();
        if (initializer != null) {
        //infer type of initializer
            Type initializerType = (Type) initializer.visit(this,arg);
            check(assignmentCompatible(declaration.getType(), initializerType),declaration,
                    "type of expression and declared type do not match");
            declaration.setAssigned(true);
        }
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        String name = identExpr.getName();
        Declaration dec = symbolTable.lookup(name);
        check(dec != null, identExpr, "undefined identifier " + name);
        check(dec.isAssigned(), identExpr, "using uninitialized variable");
        identExpr.setDec(dec); // save declaration--will be useful later.
        Type type = dec.getType();
        identExpr.setType(type);
        return type;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        List<ASTNode> decsAndStatements = program.getDecsAndStatements();
        for (ASTNode node : decsAndStatements) {
            node.visit(this, arg);
        }
        return program;

    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return null;
    }
}
