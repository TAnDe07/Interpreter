@ -30,122 +30,122 @@ public class CompilerComponentFactory {
		return new ASTVisitor() {
        @Override
        public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
            return null;
            return visitAssignmentStatement(statementAssign, arg);
        }

        @Override
        public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
            return null;
            return visitBinaryExpr(binaryExpr, arg);
        }

        @Override
        public Object visitBlock(Block block, Object arg) throws PLCException {
            return null;
            return visitBlock(block, arg);
        }

        @Override
        public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
            return null;
            return visitConditionalExpr(conditionalExpr, arg);
        }

        @Override
        public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
            return null;
            return visitDeclaration(declaration, arg);
        }

        @Override
        public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
            return null;
            return visitDimension(dimension, arg);
        }

        @Override
        public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
            return null;
            return visitExpandedPixelExpr(expandedPixelExpr, arg);
        }

        @Override
        public Object visitIdent(Ident ident, Object arg) throws PLCException {
            return null;
            return visitIdent(ident, arg);
        }

        @Override
        public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
            return null;
            return visitIdentExpr(identExpr, arg);
        }

        @Override
        public Object visitLValue(LValue lValue, Object arg) throws PLCException {
            return null;
            return visitLValue(lValue, arg);
        }

        @Override
        public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
            return null;
            return visitNameDef(nameDef, arg);
        }

        @Override
        public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
            return null;
            return visitNumLitExpr(numLitExpr, arg);
        }

        @Override
        public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
            return null;
            return visitPixelFuncExpr(pixelFuncExpr, arg);
        }

        @Override
        public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
            return null;
            return visitPixelSelector(pixelSelector, arg);
        }

        @Override
        public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
            return null;
            return visitPredeclaredVarExpr(predeclaredVarExpr, arg);
        }

        @Override
        public Object visitProgram(Program program, Object arg) throws PLCException {
            return null;
            return visitProgram(program, arg);
        }

        @Override
        public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
            return null;
            return visitRandomExpr(randomExpr, arg);
        }

        @Override
        public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
            return null;
            return visitReturnStatement(returnStatement, arg);
        }

        @Override
        public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
            return null;
            return visitStringLitExpr(stringLitExpr, arg);
        }

        @Override
        public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
            return null;
            return visitUnaryExpr(unaryExpr, arg);
        }

        @Override
        public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
            return null;
            return visitUnaryExprPostFix(unaryExprPostfix, arg);
        }

        @Override
        public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
            return null;
            return visitWhileStatement(whileStatement, arg);
        }

        @Override
        public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
            return null;
            return visitWriteStatement(statementWrite, arg);
        }

        @Override
        public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
            return null;
            return visitZExpr(zExpr, arg);
        }
    };
}
