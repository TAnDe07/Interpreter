package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.*;

import java.util.HashMap;
import java.util.List;

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

    private boolean assignmentCompatible(Type targetType, Type rhsType) {
        return (targetType == rhsType || targetType == Type.STRING && rhsType == Type.INT || targetType
                == Type.STRING && rhsType == Type.BOOLEAN);
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        return null;
    }


    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {
            throw new TypeCheckException(message, node.getSourceLoc());
        }
    }


    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Token.Kind op = binaryExpr.getOp();
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
        String name = declaration.nameDef.toString();
        boolean inserted = symbolTable.insert(name,declaration);
        check(inserted, declaration, "variable " + name + "already declared");
        Expr initializer = declaration.getInitializer();
        if (initializer != null) {
            //infer type of initializer
            Type initializerType = (Type) initializer.visit(this,arg);
            check(assignmentCompatible(declaration.initializer.getType(), initializerType),declaration,
                    "type of expression and declared type do not match");
            // declaration.setAssigned(true); ??
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
        Declaration dec = symbolTable.lookup(name);

        if (dec == null) {
            error("undefined identifier " + name);
        }

        if (dec.getInitializer() == null) {
            error("using uninitialized variable");
        }

        // identExpr.setDec(dec); // save declaration--will be useful later.
        Type type = dec.getNameDef().getType();
        identExpr.setType(type);
        return type;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        if (nameDef.dimension != null) {
            //nameDef.type = ?
        }
        nameDef.ident.getName();
        // symbolTable.insert(nameDef, ?);
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
        List<AST> decsAndStatements = program.getDecsAndStatements();
        for (AST node : decsAndStatements) {
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
        Token.Kind op = unaryExpr.getOp();
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
        return resultType;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        Type prim = unaryExprPostfix.getPrimary().type;
        Type pixSel = (Type) unaryExprPostfix.getPixel().visit(this, arg);
        Type chanSel = null; //????

        if (prim == Type.PIXEL) {
            if (pixSel != null) {
                error("pixel selector exists");
            }
            /*else if (chanSel ==  ){

            }
            */
        }
       return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        if (statementWrite.e == null) {
            return null; //Dk, error maybe?
        }
        return statementWrite.e;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return Type.INT;
    }

    private void error(String message) throws TypeCheckException {
        throw new TypeCheckException(message);
    }
}