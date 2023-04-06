package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.PLCException;
import edu.ufl.cise.plcsp23.TypeCheckException;

public class GenerateVisitor implements ASTVisitor {
    @Override
    //NOT DONE
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {

        String assignString = visitLValue(statementAssign.lv, arg) + "=";
        assignString += statementAssign.getE().visit(this, arg);
        return assignString;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        String binary = "(";
        String kind = "";

        switch(binaryExpr.getOp()) {
            case EQ -> { // ==
                kind = "==";
            }
            case PLUS -> { // +
                kind = "+";
            }
            case MINUS -> { // -
                kind = "-";
            }
            case TIMES -> { // *
                kind = "";
            }
            case DIV -> { // /
                kind = "/";
            }
            case MOD -> { // %
                kind = "%";
            }
            case LT -> { // <, <=, >, >=, ||, &&
                kind = "<";
            }
            case LE -> { // <=
                kind = "<=";
            }
            case GT -> { // >
                kind = ">";
            }
            case GE -> { // >=
                kind = ">=";
            }
            case OR -> { // ||
                kind = "||";
            }
            case AND -> { // &&
                kind = "&&";
            }
            case BITAND -> { // &
                kind = "&";
            }
            case BITOR -> { // |
                kind = "|";
            }
            case EXP -> { // **
                kind = "**";
            }
            default -> {
               error("compiler error");
            }
        }

        binary += binaryExpr.getLeft().visit(this, arg);
        binary += " " + kind + " ";
        binary += binaryExpr.getRight().visit(this, arg);

        binary += ")";

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

        condition += conditionalExpr.getGuard().visit(this, arg) + " ? ";
        condition += "\"" + conditionalExpr.getTrueCase().visit(this, arg) + "\" : ";
        condition += "\"" + conditionalExpr.getFalseCase().visit(this, arg) + "\")";

        return condition;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        String decString = "";
        if (declaration.initializer != null) {
            String type = declaration.getNameDef().getType() + "";
            if (type.equals("STRING")) {
                type = "String";
            }
            else {
                type = type.toLowerCase();
            }

            decString = type + " " + declaration.getNameDef().getIdent().getName() + " = ";
            decString += declaration.getInitializer().visit(this, arg);
        }
        return decString;
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
        return ident.getName();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return identExpr.getName();
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
       String LString = lValue.getIdent().visit (this, arg).toString();
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

        String nameDef1 = type + " " + nameDef.getIdent().getName();
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
        String type = program.getType() + "";
        if (type.equals("STRING")) {
            type = "String";
        }
        else {
            type = type.toLowerCase();
        }

        String program1 = "public class " + program.getIdent().getName() + " {\n\tpublic static " + type + " apply(";

        for (int i = 0; i < program.getParamList().size(); i++) {
            program1 += visitNameDef(program.getParamList().get(i), arg);
            if (i != program.getParamList().size() - 1) {
                program1 += ", ";
            }
        }

        program1 += ") {\n";

        program1 += program.getBlock().visit(this, arg);

        program1 += "\t}\n}";

        return program1;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        double randD = Math.floor(Math.random() * 256);
        String randString = Double.toString(randD);
        return randString;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        String return1 = "return " + returnStatement.getE().visit(this, arg) + "";
        return return1;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        String sLitString = stringLitExpr.getValue();
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
        String whileS = "while ( " + whileStatement.getGuard().visit(this, arg) + ") {" + "\n"
                + whileStatement.getBlock().visit(this, arg) + "\n" + "}";
        return whileS;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        //ConsoleIO.write(statementWrite.getE().visit(this, arg));
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return "255";
    }

    private void error(String message) throws TypeCheckException {
        throw new TypeCheckException(message);
    }
}
