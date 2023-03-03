package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.ast.Dimension;

import javax.naming.Name;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser {

    IToken firstToken;
    Scanner scanner;
    IToken currToken;

    public Parser (Scanner scanner) throws LexicalException {
        this.scanner = scanner;
        firstToken = this.scanner.next();
        currToken = firstToken;
    }

    @Override
    public AST parse() throws SyntaxException, LexicalException {
        if (firstToken.getKind() == IToken.Kind.EOF) {
            error("empty program");
        }

        return program();
    }

    ///// recently added

    //Program = Type IDENT ( ParamList ) Block --> parentheses included
    public Program program() throws SyntaxException, LexicalException {
        Program program = null;
        IToken firstToken = currToken; // type

        Type type = type();
        currToken = scanner.next(); // ident

        Ident ident = new Ident(currToken);
        currToken = scanner.next(); // left parenthesis
        currToken = scanner.next(); // ParamList or )

        List<NameDef> paramList = new ArrayList<>();

        while (currToken.getKind() != Token.Kind.RPAREN) {
            if (currToken.getKind() == Token.Kind.COMMA) {
                currToken = scanner.next(); // next name def
            }
            paramList.add(param_list());
        }
        currToken = scanner.next(); // block

        Block block = block();

        return new Program(firstToken, type, ident, paramList, block);
    }

    //Block = { DecList  StatementList } --> brackets included
    public Block block() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // {
        currToken = scanner.next(); // dec list

        List<Declaration> decList = new ArrayList<>();

        while (true) {
            Declaration dec = dec_list();
            if (dec == null) { // there was no declaration
                break;
            }
            decList.add(dec);
            if (currToken.getKind() == Token.Kind.DOT) {
                currToken = scanner.next(); // declaration or statement list
            }
        }

        List<Statement> statList = new ArrayList<>();

        while (true) {
            Statement stat = statement_list();
            if (stat == null) { // there was no statement
                break;
            }
            statList.add(stat);
            if (currToken.getKind() == Token.Kind.DOT) {
                currToken = scanner.next(); // statement or }
            }
        }

        return new Block(firstToken, decList, statList);
    }

    //DecList = ( Declaration . )* --> parentheses not included
    public Declaration dec_list() throws SyntaxException, LexicalException {
        return declaration();
    }

    //StatementList = ( Statement . ) * --> parentheses not included
    public Statement statement_list() throws SyntaxException, LexicalException {
        return statement();
    }

    //ParamList = ε |  NameDef  ( , NameDef ) * --> parentheses not included
    public NameDef param_list() throws SyntaxException, LexicalException {
        return nameDef();
    }

    //Name Def = Type ( ε | Dimension ) IDENT --> parentheses not included
    public NameDef nameDef() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // type
        Type left = type();
        currToken = scanner.next(); // ident or dimension

        Dimension right = null;

        if (currToken.getKind() == IToken.Kind.LSQUARE) { // dimension
            right = dimension();
            if (currToken.getKind() == Token.Kind.RSQUARE) {
                currToken = scanner.next(); // ident
            }
        }
        // just ident
        Ident i = new Ident(currToken);
        currToken = scanner.next(); // , or ) or

        return new NameDef(firstToken, left, right, i);
    }

    //Type = image | pixel | int | string | void
    public Type type() throws SyntaxException, LexicalException {
        /*Type p  = null;
        if (currToken.getKind() == IToken.Kind.RES_image) {
            //p = ?
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_pixel){
            //p = ?
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_int) {
            //p = ?
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_string) {
            //p = ?
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_void) {
            //p = ?
            currToken = scanner.next();
        }
        else {
            error("invalid Type");
        }*/
        return Type.getType(currToken);
    }

    // Declaration = NameDef ( ε | = Expr) --> parentheses not included
    public Declaration declaration() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr expr = null;
        NameDef nameDef = null;

        if (currToken.getKind() == Token.Kind.RES_image || currToken.getKind() == Token.Kind.RES_pixel ||
                currToken.getKind() == Token.Kind.RES_int || currToken.getKind() == Token.Kind.RES_string ||
                currToken.getKind() == Token.Kind.RES_void) { // if there is a declaration
            nameDef = nameDef();
            currToken = scanner.next(); // . or =
            if (currToken.getKind() == Token.Kind.ASSIGN) { // =
                currToken = scanner.next(); // expr
                expr = expr();
            }
            return new Declaration(firstToken, nameDef, expr);
        }

        return null; // no declaration
    }

 /////

    // expression ::= conditional | or
    public Expr expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        if (firstToken.getKind() == IToken.Kind.RES_if) { // first token is if --> conditional
           left = cond_expr(); // maybe
        }
        else {  // otherwise --> or
            left = or_expr();
        }

        return left;
    }
    // conditional ::= if expr ? expr ? expr
    public Expr cond_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        Expr T = null;
        Expr F = null;
        while (currToken.getKind() == IToken.Kind.RES_if || currToken.getKind() ==  IToken.Kind.QUESTION) {
            IToken op = currToken;
            currToken = scanner.next();
            right = expr();
            op = currToken;
            currToken = scanner.next();
            T = expr();
            op = currToken;
            currToken = scanner.next();
            F = expr();
            left = new ConditionalExpr(firstToken, right, T, F);
            return left;
        }
        return left;
    }
    // or ::= and ((| | ||) and)*
    public Expr or_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = and_expr();
        while (currToken.getKind() == IToken.Kind.BITOR || currToken.getKind() == IToken.Kind.OR) {
            IToken op = currToken;
            currToken = scanner.next();
            right = and_expr();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }
    // and ::= comparison ((& | &&) comparison)*
    public Expr and_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = comp_expr();
        while (currToken.getKind() == IToken.Kind.BITAND || currToken.getKind() == IToken.Kind.AND) {
            IToken op = currToken;
            currToken = scanner.next();
            right = comp_expr();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }
    // comparison ::= power ((< | > | == | <= | >=) power)*
    public Expr comp_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = pow_expr();
        while (currToken.getKind() == IToken.Kind.LT || currToken.getKind() == IToken.Kind.GT || currToken.getKind() == IToken.Kind.EQ
                || currToken.getKind() == IToken.Kind.GE || currToken.getKind() == IToken.Kind.LE) {
            IToken op = currToken;
            currToken = scanner.next();
            right = pow_expr();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }
    // power ::= add ((**) add)*
    public Expr pow_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = add_expr();
        if (currToken.getKind() == IToken.Kind.EXP) {
            IToken op = currToken;
            currToken = scanner.next();
            right = pow_expr();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }
    // additive ::= multiplicative ((+ | -) multiplicative)*
    public Expr add_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = mult_expr();
        while (currToken.getKind() == IToken.Kind.PLUS || currToken.getKind() == IToken.Kind.MINUS) {
            IToken op = currToken;
            currToken = scanner.next();
            right = mult_expr();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }
    // multiplicative ::= unary ((* | / | %) unary)*
    public Expr mult_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = un_expr();
        while (currToken.getKind() == IToken.Kind.TIMES || currToken.getKind() == IToken.Kind.DIV || currToken.getKind() == IToken.Kind.MOD) {
            IToken op = currToken;
            currToken = scanner.next();
            right = un_expr();
            left = new BinaryExpr(firstToken, left, op.getKind(), right);
        }
        return left;
    }
    // unary ::= (! | - | sin | cos | atan)* primary
    // unary ::= (! | - | sin | cos | atan) unary | primary
    public Expr un_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        if (currToken.getKind() == IToken.Kind.BANG || currToken.getKind() == IToken.Kind.MINUS || currToken.getKind() == IToken.Kind.RES_sin
                || currToken.getKind() == IToken.Kind.RES_cos || currToken.getKind() == IToken.Kind.RES_atan) {
            IToken op = currToken;
            currToken = scanner.next();
            right = un_expr();
            left = new UnaryExpr(firstToken, op.getKind(), right);
        }
        else {
            left = unary_postfix();
        }
        return left;
    }
    // primary ::= STRING_LIT | NUM_LIT | IDENT | (expression) | Z | rand
    public Expr prim_expr() throws SyntaxException, LexicalException {
        Expr e = null;

        if (currToken.getKind() == IToken.Kind.NUM_LIT) {
            e = new NumLitExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.STRING_LIT) {
            e = new StringLitExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.IDENT) {
            e = new IdentExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_Z) {
            e = new ZExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_rand) {
            e = new RandomExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.LPAREN) {
            currToken = scanner.next();
            e = expr();
            match(IToken.Kind.RPAREN);
        }
        //new
        else if (currToken.getKind() == IToken.Kind.RES_x) {
            e = new PredeclaredVarExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_y) {
            e = new PredeclaredVarExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_a) {
            e = new PredeclaredVarExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_r) {
            e = new PredeclaredVarExpr(currToken);
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.LSQUARE) {
            e = expanded_pixel();
            currToken = scanner.next();
        }
        else if (currToken.getKind() == IToken.Kind.RES_x_cart || currToken.getKind() == IToken.Kind.RES_y_cart ||
            currToken.getKind() == IToken.Kind.RES_a_polar || currToken.getKind() == IToken.Kind.RES_r_polar) {
            e = pixel_function_expr();
            currToken = scanner.next();
        }
        else {
            error("didn't end with primary");
        }
        return e;
    }

    //// recently added

    // UnaryExprPostfix = PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
    public Expr unary_postfix() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = prim_expr();
        PixelSelector pix = null;
        ColorChannel color = null;

        if (currToken.getKind() == IToken.Kind.LSQUARE) { // there is a pixel selector
            pix = pixel_selector();
            if (currToken.getKind() == IToken.Kind.RSQUARE) {
                currToken = scanner.next(); // empty or channel selector (:)
            }
        }

        if (currToken.getKind() == IToken.Kind.COLON) { // there is a channel selector
            currToken = scanner.next();
            color = channel_selector();
        }

        if (pix != null || color != null) {
            return new UnaryExprPostfix(firstToken, left, pix, color);
        }

        return left;
    }

    //Channel Selector = : red | : grn | : blu
    public ColorChannel channel_selector() throws SyntaxException, LexicalException {
        return ColorChannel.getColor(currToken);
    }

    //Pixel Selector = [ Expr , Expr ] --> (same as Dimension)
    public PixelSelector pixel_selector() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // [
        currToken = scanner.next(); // expr

        Expr left = expr();
        if (currToken.getKind() == Token.Kind.COMMA) {
            currToken = scanner.next(); // expr
        }

        Expr right = expr();

        return new PixelSelector(firstToken, left, right);
    }

    //Expanded Pixel = [ Expr , Expr , Expr ]
    public ExpandedPixelExpr expanded_pixel() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // [
        currToken = scanner.next(); // expr

        Expr left = expr();
        if (currToken.getKind() == Token.Kind.COMMA) {
            currToken = scanner.next(); // expr
        }

        Expr mid = expr();
        if (currToken.getKind() == Token.Kind.COMMA) {
            currToken = scanner.next(); // expr
        }

        Expr right = expr();

        return new ExpandedPixelExpr(firstToken, left, mid, right);
    }

    //Pixel Function Expr = ( x_cart | y_cart | a_polar | r_polar ) PixelSelector --> parentheses not included
    public PixelFuncExpr pixel_function_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // x, y, a, r
        IToken.Kind function = currToken.getKind();
        currToken = scanner.next(); // pixel selector

        PixelSelector pix = pixel_selector();

        return new PixelFuncExpr(firstToken, function, pix);
    }

    //Dimension = [ Expr , Expr ]
    public Dimension dimension() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // [
        currToken = scanner.next(); // expr

        Expr left = expr();
        if (currToken.getKind() == Token.Kind.COMMA) {
            currToken = scanner.next(); // expr
        }

        Expr right = expr();

        return new Dimension(firstToken, left, right);
    }

    //LValue = IDENT (PixelSelector | ε ) (ChannelSelector | ε ) --> parentheses not included
    public LValue lValue() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // ident
        Ident i = new Ident(currToken);
        PixelSelector pix = null;
        ColorChannel color = null;

        currToken = scanner.next(); // empty or pixel selector ([) or channel selector (:)

        if (currToken.getKind() == IToken.Kind.LSQUARE) { // there is a pixel selector
            pix = pixel_selector();
            if (currToken.getKind() == IToken.Kind.RSQUARE) {
                currToken = scanner.next(); // empty or channel selector (:)
            }
        }

        if (currToken.getKind() == IToken.Kind.COLON) { // there is a channel selector
            currToken = scanner.next();
            color = channel_selector();
        }

        return new LValue(firstToken, i, pix, color);
    }

    //Statement = LValue = Expr | write Expr | while Expr Block
    public Statement statement() throws SyntaxException, LexicalException {
        IToken firstToken = currToken; // LValue or write or while
        Expr expr = null;
        NameDef nameDef = null;
        LValue lValue = null;

        switch (currToken.getKind()) {
            case IDENT -> {
                lValue = lValue();
                currToken = scanner.next(); // expr
                expr = expr();
                return new AssignmentStatement(firstToken, lValue, expr);
            }
            case RES_write -> {
                currToken = scanner.next(); // expr
                expr = expr();
                return new WriteStatement(firstToken, expr);
            }
            case RES_while -> {
                currToken = scanner.next(); // expr
                expr = expr();
                Block block = block();
                return new WhileStatement(firstToken, expr, block);
            }
            default -> { // no statement
                return null;
            }
        }
    }

    //////

    private void error(String message) throws SyntaxException {
        throw new SyntaxException(message);
    }

    public void match(IToken.Kind c) throws SyntaxException, LexicalException {
        if (currToken.getKind() == c) {
            currToken = scanner.next();
        }
        else {
            error("no right parenthesis found");
        }
    }
}
