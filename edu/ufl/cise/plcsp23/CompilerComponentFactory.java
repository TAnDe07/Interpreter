/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the spring semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public web site either during or after the course.
 */

package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

public class CompilerComponentFactory {
    public static IScanner makeScanner(String input) {
        //Add statement to return an instance of your scanner
        return new Scanner(input);
    }

    public static IParser makeParser(String input) throws LexicalException {
        //add code to create a scanner and parser and return the parser.
        Scanner scanner = new Scanner(input);
        return new Parser(scanner);
    }

    public static ASTVisitor makeTypeChecker() {
        //code to instantiate a return an ASTVisitor for type checking
        //DK IF this is correct
        return new ASTVisitor() {
            @Override
            public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
                return null;
            }

            @Override
            public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
                return null;
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
                return null;
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
                return null;
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
                return null;
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
                return null;
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
        };
    }
}
