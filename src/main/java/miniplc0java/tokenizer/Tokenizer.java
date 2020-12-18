package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        } else if (Character.isAlphabetic(peek)||peek=='_') {
            return lexIdentOrKeyword();
        } else {
            return lexOperatorOrUnknown();
        }
    }


    private Token lexUIntOrDouble() throws TokenizeError {
        // 直到查看下一个字符不是数字为止:
        String a=new String();

        while(it.peekChar()<='9'&&it.peekChar()>='0')
        {
            a+=it.nextChar();
        }
        if(it.peekChar()=='.')
        {
            a+=it.nextChar();
            if(Character.isDigit(it.peekChar()))
            {
                while(Character.isDigit(it.peekChar()))
                {
                    a+=it.nextChar();
                }
                if(it.peekChar()=='e'||it.peekChar()=='E')
                {
                    it.nextChar();
                    char zh=it.peekChar();
                    String tmp=new String();

                    if(Character.isDigit(zh))
                    {
                        while (Character.isDigit(it.peekChar()))
                        {
                            tmp+=it.nextChar();
                        }
                        double tmpx=Integer.parseInt(tmp);
                        double xx=Double.parseDouble(a);
                        double res=xx*Math.pow(10,tmpx);
                        return new Token(TokenType.DOUBLE_LITERAL,res, it.previousPos(), it.currentPos());
                    }
                    else if(zh=='-')
                    {
                        it.nextChar();
                        if(!Character.isDigit(it.peekChar()))
                            throw new Error("Not a number");
                        while (Character.isDigit(it.peekChar()))
                        {
                            tmp+=it.nextChar();
                        }

                        double tmpx=Integer.parseInt(tmp);
                        double xx=Double.parseDouble(a);
                        double res=xx*Math.pow(10,-tmpx);
                        return new Token(TokenType.DOUBLE_LITERAL,res, it.previousPos(), it.currentPos());

                    }
                    else if(zh=='+')
                    {
                        it.nextChar();
                        if(!Character.isDigit(it.peekChar()))
                            throw new Error("Not a number");
                        while (Character.isDigit(it.peekChar()))
                        {
                            tmp+=it.nextChar();
                        }

                        double tmpx=Integer.parseInt(tmp);
                        double xx=Double.parseDouble(a);
                        double res=xx*Math.pow(10,tmpx);
                        return new Token(TokenType.DOUBLE_LITERAL,res, it.previousPos(), it.currentPos());
                    }
                    else
                        throw new Error("Not a number");

                }
                else
                {
                    double xx=Double.parseDouble(a);
                    return new Token(TokenType.DOUBLE_LITERAL,xx, it.previousPos(), it.currentPos());
                }
            }
            else
                throw new Error("Not a number");
        }
        long x=Long.valueOf(a);
        return new Token(TokenType.UINT_LITERAL,x, it.previousPos(), it.currentPos());
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        // Token 的 Value 应填写数字的值
//        throw new Error("Not implemented");
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        String a=new String();
        while((it.peekChar()<='9'&&it.peekChar()>='0')||(it.peekChar()<='z'&&it.peekChar()>='a')||(it.peekChar()<='Z'&&it.peekChar()>='A')||it.peekChar()=='_')
        {
            a+=it.nextChar();
        }
        // -- 前进一个字符，并存储这个字符
        // 尝试将存储的字符串解释为关键字
        if(a.equals("fn"))
        {
            return new Token(TokenType.FN_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("let"))
        {
            return new Token(TokenType.LET_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("const"))
        {
            return new Token(TokenType.CONST_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("as"))
        {
            return new Token(TokenType.AS_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("while"))
        {
            return new Token(TokenType.WHILE_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("if"))
        {
            return new Token(TokenType.IF_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("else"))
        {
            return new Token(TokenType.ELSE_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("return"))
        {
            return new Token(TokenType.RETURN_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("break"))
        {
            return new Token(TokenType.BREAK_KW,a, it.previousPos(), it.currentPos());
        }
        if(a.equals("continue"))
        {
            return new Token(TokenType.CONTINUE_KW,a, it.previousPos(), it.currentPos());
        }

        return new Token(TokenType.IDENT,a,it.previousPos(), it.currentPos());
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        // Token 的 Value 应填写标识符或关键字的字符串
//        throw new Error("Not implemented");
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {

        switch (it.nextChar()) {
            case '"'://字符串字面量
                {
                    int end=1;
                    char m,prem;

                    String ans=new String();

                    m=it.nextChar();
//                    ans+=m;

                    if(m=='"')
                        return new Token(TokenType.STRING_LITERAL, "", it.previousPos(), it.currentPos());

                    while(end==1)
                    {
                        if(m=='\\')
                        {
                            char temp=it.nextChar();
                            if(temp=='\\'||temp=='\''||temp=='"'||temp=='n'||temp=='t'||temp=='r')
                            {
                                if(temp=='\\')
                                {
                                    ans+='\\';
                                }
                                if(temp=='\'')
                                {
                                    ans+='\'';
                                }
                                if(temp=='"')
                                {
                                    ans+='"';
                                }
                                if(temp=='n')
                                {
                                    char g=10;
                                    ans+=g;
                                }
                                if(temp=='r')
                                {
                                    char g=13;
                                    ans+=g;
                                }
                                if(temp=='t')
                                {
                                    char g=9;
                                    ans+=g;
                                }
                            }
                            else
                            {
                                ans+=m;
                                ans+=temp;
                            }
                            m=it.nextChar();

                            if(m=='"')
                                break;

                            continue;
                        }

                        if(m=='"')
                            break;
                        ans+=m;
                        m=it.nextChar();
                    }

                    return new Token(TokenType.STRING_LITERAL, ans, it.previousPos(), it.currentPos());
//                    while(end==1)
//                    {
//                        prem=m;
//                        m=it.nextChar();
//                        if(m=='"')
//                        {
//                            if(prem!='\\')
//                                break;
//                        }
//                        ans+=m;
//                    }
//
//                    char teem[]=ans.toCharArray();
//                    String fin=new String();
//
//                    for(int i=0;i< teem.length;)
//                    {
//                        if(teem[i]!='\\')
//                        {
//                            fin+=teem[i];
//                            i++;
//                        }
//                        else{
//                            int j=i+1;
//                            if(j< teem.length&&teem[j]=='\'')
//                            {
//                                fin+='\'';
//                                i=j+1;
//                                continue;
//                            }
//                            if(j< teem.length&&teem[j]=='"')
//                            {
//                                fin+='"';
//                                i=j+1;
//                                continue;
//                            }
//                            if(j< teem.length&&teem[j]=='\\')
//                            {
//                                fin+='\\';
//                                i=j+1;
//                                continue;
//                            }
//                            if(j< teem.length&&teem[j]=='n')
//                            {
//                                char g=10;
//                                fin+=g;
//                                i=j+1;
//                                continue;
//                            }
//                            if(j< teem.length&&teem[j]=='r')
//                            {
//                                char g=13;
//                                fin+=g;
//                                i=j+1;
//                                continue;
//                            }
//                            if(j< teem.length&&teem[j]=='t')
//                            {
//                                char g=9;
//                                fin+=g;
//                                i=j+1;
//                                continue;
//                            }
//                        }
//                    }

//                    return new Token(TokenType.STRING_LITERAL, fin, it.previousPos(), it.currentPos());

                }


            case '\''://字符字面量：
                {
                    char tmp= it.nextChar();
                    if(tmp=='\\')
                    {
                        char tmp2=it.nextChar();
                        char end=it.nextChar();
                        if(end=='\'')
                        {
                            if(tmp2=='n')
                                return new Token(TokenType.CHAR_LITERAL, 10, it.previousPos(), it.currentPos());
                            if(tmp2=='t')
                                return new Token(TokenType.CHAR_LITERAL, 9, it.previousPos(), it.currentPos());
                            if(tmp2=='r')
                                return new Token(TokenType.CHAR_LITERAL, 13, it.previousPos(), it.currentPos());
                            if(tmp2=='\'')
                                return new Token(TokenType.CHAR_LITERAL, 39, it.previousPos(), it.currentPos());
                        }
                        else
                            throw new Error("not a char");

                    }
                    if(tmp!='\'')
                    {
                        char tmp2=it.nextChar();
                        if(tmp2!='\''||tmp=='\\'||tmp=='\n'||tmp=='\t'||tmp=='\r')
                            throw new Error("not a char");

                        return new Token(TokenType.CHAR_LITERAL, tmp, it.previousPos(), it.currentPos());
                    }
                    else
                        throw new Error("not a char");
                }

            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                if(it.peekChar()=='>')
                {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());


            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());


            case '/':
                if(it.peekChar()=='/')
                {
                    String com=new String();
                    while(it.peekChar()!=13&&it.peekChar()!=10)
                        com+=it.nextChar();
                    it.nextChar();
                    return new Token(TokenType.COMMENT, com, it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());

            case '=':
                if(it.peekChar()=='=')
                    {
                        it.nextChar();
                        return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                    }
                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());

            case '!':
                if(it.peekChar()=='=')
                {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                else
                    throw new Error("Not A Legal Sign");

            case '<':
                if(it.peekChar()=='=')
                {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());

            case '>':
                if(it.peekChar()=='=')
                {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());


            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());


            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());

            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
