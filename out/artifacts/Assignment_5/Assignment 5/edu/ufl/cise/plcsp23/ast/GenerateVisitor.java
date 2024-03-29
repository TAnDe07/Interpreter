package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.TypeCheckException;


import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GenerateVisitor implements ASTVisitor {

    boolean write = false;
    boolean math = false;
    Type program;
    boolean progName;

    HashMap<String, Vector<Integer>> scopes = new HashMap<>();

    int scope = 0;

    @Override
    //NOT DONE
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {

        String assignString = visitLValue(statementAssign.lv, arg) + " = ";

        String assign = statementAssign.getE().visit(this, arg) + "";

        if (statementAssign.getLv().getType() == Type.STRING) {
            if (statementAssign.getE() instanceof NumLitExpr) {
                assign = "\"" + assign + "\"";
            }
        }

        assignString += assign;

        return assignString;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        String binary = "(";
        String kind = "";
        boolean bool = false;
        boolean logic = false;

        switch(binaryExpr.getOp()) {
            case EQ -> { // ==
                kind = "==";
                bool = true;
            }
            case PLUS -> { // +
                kind = "+";
            }
            case MINUS -> { // -
                kind = "-";
            }
            case TIMES -> { // *
                kind = "*";
            }
            case DIV -> { // /
                kind = "/";
            }
            case MOD -> { // %
                kind = "%";
            }
            case LT -> { // <
                kind = "<";
                bool = true;
            }
            case LE -> { // <=
                kind = "<=";
                bool = true;
            }
            case GT -> { // >
                kind = ">";
                bool = true;
            }
            case GE -> { // >=
                kind = ">=";
                bool = true;
            }
            case OR -> { // ||
                kind = "||";
                bool = true;
                logic = true;
            }
            case AND -> { // &&
                kind = "&&";
                bool = true;
                logic = true;
            }
            case BITAND -> { // &
                kind = "&";
            }
            case BITOR -> { // |
                kind = "|";
            }
            case EXP -> { // **
                kind = "**";
                math = true;
            }
            default -> {
               error("compiler error");
            }
        }

        String left = binaryExpr.getLeft().visit(this, arg) + "";
        String right = binaryExpr.getRight().visit(this, arg) + "";

        if (kind.equals("**")) {
            binary += "(int) Math.pow(" + left + ", " + right + ")";
        }
        else {

            binary += left;

            if (logic) {
                binary += " != 0";
            }

            binary += " " + kind + " ";
            binary += right;

            if (logic) {
                binary += "!= 0 ";
            }
        }

        binary += ")";

        if (bool) {
            binary += " ? 1 : 0";
        }

        return binary;
    }

    @Override
    //Debug
    public Object visitBlock(Block block, Object arg) throws PLCException {
        String blockString = "";
        //declist
       for (int i = 0; i < block.decList.size(); i++) {
               blockString += visitDeclaration(block.decList.get(i), arg);
               blockString += ";\n";
       }
        //statementlist
        for (int i = 0; i < block.statementList.size(); i++) {
            blockString += block.statementList.get(i).visit(this, arg);
            blockString += ";\n";
        }
        return blockString;
    }

    /*public Object visitStatementList(Statement statement, Object arg) throws PLCException {
        return null;
    }*/
    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        String condition = "(";

        String guard = conditionalExpr.getGuard().visit(this, arg) + "";

        if (conditionalExpr.getGuard() instanceof BinaryExpr) {
            if (guard.charAt(guard.length() - 1) != ')') {
                guard = guard.substring(0, guard.length() - 8);
            }
        }

        if (conditionalExpr.getGuard() instanceof IdentExpr) {
            guard += "!= 0)";
            guard = "(" + guard;
        }

        condition += guard + " ? ";

        String trueCase = conditionalExpr.getTrueCase().visit(this, arg) + "";
        String falseCase = "" + conditionalExpr.getFalseCase().visit(this, arg);

        if (conditionalExpr.getTrueCase() instanceof StringLitExpr) {
        }
        else {
            trueCase = "(" + trueCase + ")";
        }

        if (conditionalExpr.getFalseCase() instanceof StringLitExpr) {
        }
        else {
            falseCase = "(" + falseCase + ")";
        }

        condition += trueCase + " : " + falseCase + ")";

        return condition;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        String decString = "";
        String type = declaration.getNameDef().getType() + "";
        if (type.equals("STRING")) {
            type = "String";
        }
        else {
            type = type.toLowerCase();
        }

        Vector<Integer> list = new Vector<>();
        list.add(1);

        String name = declaration.getNameDef().getIdent().getName();

        if (!scopes.containsKey(name)) {
            scopes.put(name, list);
        }
        else {
            scopes.get(name).add(scope);
        }

        decString = type + " " + declaration.getNameDef().getIdent().visit(this, arg);

        if (declaration.initializer != null) {
            decString += " = ";

            String initialize = declaration.getInitializer().visit(this, arg) + "";

            if (type.equals("String")) {
                if (declaration.getInitializer() instanceof NumLitExpr) {
                    initialize = "\"" + initialize + "\"";
                }
            }

            decString += initialize;
        }

        return decString;
    }
    // assignment 6
    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }
    // assignment 6
    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        String name = ident.getName();
        if (!progName && scope > 1) {
            if (scopes.containsKey(ident.getName())) {
                if (scopes.get(ident.getName()).size() > 1) {
                    name += scopes.get(ident.getName()).get(scopes.get(ident.getName()).size() - 1);
                }
            }
        }
        return name;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        String name = identExpr.getName();
        if (!progName && scope > 1) {
            if (scopes.containsKey(identExpr.getName())) {
                if (scopes.get(identExpr.getName()).size() > 1) {
                    name += scopes.get(identExpr.getName()).get(scopes.get(identExpr.getName()).size() - 1);
                }
            }
        }
        return name;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
       String LString = lValue.getIdent().visit(this, arg).toString();
       return LString;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        String type = nameDef.getType() + "";
        if (type.equals("STRING")) {
            type = "String";
        }
        else {
            type = type.toLowerCase();
        }

        String nameDef1 = type + " " + nameDef.getIdent().visit(this, arg);
        return nameDef1;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        return numLitExpr.getValue();
    }

    // assignment 6
    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }
    // assignment 6
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    // assignment 6
    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        scope++;

        progName = true;

        this.program = program.getType();
        String type = program.getType() + "";
        if (type.equals("STRING")) {
            type = "String";
        }
        else {
            type = type.toLowerCase();
        }

        String program1 = "public class " + program.getIdent().visit(this, arg) + " {\n\tpublic static " + type + " apply(";

        progName = false;

        for (int i = 0; i < program.getParamList().size(); i++) {
            program1 += visitNameDef(program.getParamList().get(i), arg);

            Vector<Integer> list = new Vector<>();
            list.add(1);

            String name = program.getParamList().get(i).getIdent().getName();

            scopes.put(name, list);


            if (i != program.getParamList().size() - 1) {
                program1 += ", ";
            }
        }

        program1 += ") {\n";

        program1 += program.getBlock().visit(this, arg);

        program1 += "\t}\n}";

        if (write) {
            program1 = "import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n" + program1;
        }
        if (math) {
            program1 = "import java.lang.Math; \n" + program1;
        }

        scope--;

        return program1;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        math = true;
        String random = "(int) Math.floor(Math.random() * 256)";
        return random;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        String expr = "" + returnStatement.getE().visit(this, arg);

        if (returnStatement.getE() instanceof NumLitExpr && program == Type.STRING) {
            expr = "\"" + expr + "\"";
        }

        String return1 = "return " + expr;
        return return1;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        String sLitString = stringLitExpr.getValue();
        sLitString = "\"" + sLitString + "\"";
        return sLitString;
    }

    // assignment 6
    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        String unaryString = "";
       /*
        switch (unaryExpr.op) {
            case BANG -> unaryString = "!";
            case MINUS -> unaryString = "-";
            case RES_sin -> unaryString = "sin";
            case RES_cos -> unaryString = "cos";
            case RES_atan -> unaryString = "atan";
        }*/
        return null;
    }

    // assignment 6
    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {

        String whileS = "while ((" + whileStatement.getGuard().visit(this, arg);

        scope++;

        whileS += ") == 1) {" + "\n" + whileStatement.getBlock().visit(this, arg) + "}";

        Integer i2 = Integer.valueOf(scope);

        for (Map.Entry<String, Vector<Integer>> i : scopes.entrySet()) {
            i.getValue().remove(i2);
        }

        scope--;

        return whileS;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        write = true;
        String statement = statementWrite.getE().visit(this, arg) + "";
        return "ConsoleIO.write(" + statement + ")";
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return "255";
    }

    private void error(String message) throws TypeCheckException {
        throw new TypeCheckException(message);
    }
}
