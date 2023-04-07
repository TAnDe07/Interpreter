# COP4020-Project

Concrete syntax
- <_expr> ::= <conditional_expr> | <or_expr>
- <conditional_expr>  ::= if <_expr> ? <_expr> ? <_expr>
- <or_expr> ::=  <and_expr> ((  |  |  ||  ) <and_expr>)*
- <and_expr> ::=  <comparison_expr> ((& | &&)  <comparison_expr>)*
- <comparison_expr> ::=   <power_expr> ( (< | > | == | <= | >=) <power_expr>)*
- <power_expr> ::= <additive_expr> (** <additive_expr>)*
- <additive_expr> ::=  <multiplicative_expr> ((+ | -) <multiplicative_expr>)*
- <multiplicative_expr> ::= <unary_expr> ((* | / | %) <unary_expr>)*
- <unary_expr> ::= (! | - | sin | cos | atan) <unary_expr> | <primary_expr>
  - <unary_expr> ::= (! | - | sin | cos | atan)* <primary_expr>
- <primary_expr> ::= STRING_LIT | NUM_LIT | IDENT | (<_expr>) | Z | rand
