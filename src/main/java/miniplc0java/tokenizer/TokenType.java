package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    FN_KW,
    LET_KW,
    CONST_KW,
    AS_KW,
    WHILE_KW,
    IF_KW,
    ELSE_KW,
    RETURN_KW,
    BREAK_KW,
    CONTINUE_KW,
    digit,
    UINT_LITERAL,
    DOUBLE_LITERAL,
    escape_sequence,
    string_regular_char,
    STRING_LITERAL,
    char_regular_char,
    CHAR_LITERAL,
    IDENT,
    PLUS,
    MINUS,
    MUL,
    DIV,
    ASSIGN,
    EQ,
    NEQ,
    LT,
    GT,
    LE,
    GE,
    L_PAREN,
    R_PAREN,
    L_BRACE,
    R_BRACE,
    ARROW,
    COMMA,
    COLON,
    SEMICOLON,
    COMMENT,

    /**无符号整数*/
    Uint,

    /**标识符*/
    Ident,

    /**Begin*/
    Begin,

    /**End*/
    End,

    /**Var*/
    Var,

    /**Const*/
    Const,

    /**Print*/
    Print,

    /**加号*/
    Plus,

    /**减号*/
    Minus,

    /**乘号*/
    Mult,

    /**除号*/
    Div,

    /**等号*/
    Equal,

    /**分号*/
    Semicolon,

    /**左括号*/
    LParen,

    /**右括号*/
    RParen,

    /**文件尾*/
    EOF;


    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case FN_KW:
                return "Function";
            case LET_KW:
                return "Let";
            case CONST_KW:
                return "Const";
            case AS_KW:
                return "As";
            case WHILE_KW:
                return "While";
            case IF_KW:
                return "If";
            case ELSE_KW:
                return "Else";
            case RETURN_KW:
                return "Return";
            case BREAK_KW:
                return "Break";
            case CONTINUE_KW:
                return "Continue";
            case UINT_LITERAL:
                return "UnsignedInt";
            case DOUBLE_LITERAL:
                return "Double";
            case STRING_LITERAL:
                return "String";
            case CHAR_LITERAL:
                return "Char";
            case IDENT:
                return "Identifier";
            case PLUS:
                return "Plus";
            case MINUS:
                return "Minus";
            case MUL:
                return "Mul";
            case DIV:
                return "Div";
            case ASSIGN:
                return "Assign";
            case EQ:
                return "Equal";
            case NEQ:
                return "Noeq";
            case LT:
                return "Lt";
            case GT:
                return "Gt";
            case LE:
                return "Le";
            case GE:
                return "Ge";
            case L_PAREN:
                return "Lparen";
            case R_PAREN:
                return "Rparen";
            case L_BRACE:
                return "Lbrace";
            case R_BRACE:
                return "Rbrace";
            case ARROW:
                return "Arrow";
            case COMMA:
                return "Comma";
            case COLON:
                return "Colon";
            case SEMICOLON:
                return "Semicolon";
            case COMMENT:
                return "Comment";

            default:
                return "InvalidToken";
        }
    }
}
