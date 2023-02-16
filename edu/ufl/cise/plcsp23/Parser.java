package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

public class Parser implements IParser {

    IToken firstToken;
    Scanner scanner;
    IToken currToken;

    public Parser (Scanner scanner) throws LexicalException {
        firstToken = scanner.next();
        currToken = firstToken;
        this.scanner = scanner;
    }

    @Override
    public AST parse() throws SyntaxException, LexicalException {
        if (firstToken.getKind() == IToken.Kind.EOF) {
            error("empty program");
        }

        return expr();
    }
    // expression ::= conditional | or
    public Expr expr() throws SyntaxException, LexicalException {
        Expr left = null;
        Expr right = null;
        if (firstToken.getKind() == IToken.Kind.RES_if) { // first token is if --> conditional
           cond_expr(); // maybe
        }
        else {  // otherwise --> or
            left = or_expr();
        }

        return left;
    }
    // conditional ::= if expr ? expr ? expr
    public ConditionalExpr cond_expr() throws SyntaxException {
        ConditionalExpr e = null;
        return e;
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
    // power ::= add (** power | empty)
    public Expr pow_expr() throws SyntaxException, LexicalException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = add_expr();
        while (currToken.getKind() == IToken.Kind.EXP) {
            IToken op = currToken;
            currToken = scanner.next();
            right = add_expr();
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
            left = prim_expr();
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
        else {
            error("didn't end with primary");
        }
        return e;
    }

    private void error(String message) throws SyntaxException {
        throw new SyntaxException(message);
    }

    public void match(IToken.Kind c) throws SyntaxException, LexicalException {
        if (currToken.getKind() == c) {
            scanner.next();
        }
        else {
            error("");
        }
    }
}
