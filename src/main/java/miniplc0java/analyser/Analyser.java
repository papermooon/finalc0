package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public final class Analyser {

    ArrayList<Byte> FFFFFFFF;
    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    ArrayList<BigFunc> Funcs=new ArrayList<>();
    Zone Standard;
    boolean isCreateFun;
    Stack zhan=new Stack();

    boolean seeReturn;

    //用来存while的存档点的，其中的0号用来存按什么跳转，0上是1表示为真跳转，为-1表示为假跳转
    ArrayList<Integer> WhileList=new ArrayList<>();

    //if的条件跳转
    boolean FalseJump=true;

    //找到main函数是第几个函数
    public int findMainEntry(){
        var tmp=Standard.SYM.get(0);
        int index=0;
        for(int i=0;i<tmp.size();i++)
        {
            if(tmp.get(i).isFuncName)
                if(tmp.get(i).name.equals("main"))
                    return index;
                else
                    index++;
        }
        return -1;
    }

    //最终结果输出
    public void WORK(FileOutputStream x){
        byte[] tmp=new byte[this.FFFFFFFF.size()];
        for(int i=0;i<FFFFFFFF.size();i++)
            tmp[i]=FFFFFFFF.get(i);

        try {
            x.write(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //只考虑三种给的ele，全局变量，参数，局部变量
    public void pushGiveDebug(Element ele){

        if(ele.isGlobal){
            BigFunc taget=Funcs.get(Funcs.size()-1);

            int i=0;
            for(;i<Standard.SYM.get(0).size();i++)
                if(Standard.SYM.get(0).get(i).name.equals(ele.name))
                    break;

            taget.debug.add("Global"+i);
            zhan.push(RealType.Addr);
        }
        else if(ele.isPara){
            BigFunc taget=Funcs.get(Funcs.size()-1);
            taget.debug.add("arga"+taget.findPara(ele));
            zhan.push(RealType.Addr);
        }
        else{
            BigFunc taget=Funcs.get(Funcs.size()-1);
            taget.debug.add("Local"+taget.findLocal(ele));
            zhan.push(RealType.Addr);
        }
    }

    //给你个element返回一个Type
    public RealType checkElementType(Element tar) throws Error{
        if(tar.type.equals("int"))
            return RealType.Int;
        else if(tar.type.equals("double"))
            return RealType.Double;
        else if(tar.type.equals("void"))
            return RealType.Void;
       else
           throw new Error("不支持的类型");
    }

    //找用户自定义的函数
    public int findFunc(String Name){
        for(int i=0;i<Funcs.size();i++){
            if(Funcs.get(i).Funname.equals(Name))
                return i;
            else {}
        }
        return -1;
    }

    //测试
    public void PrintALLBitches(){
        for(int i=0;i<Funcs.size();i++){
            System.out.println(Funcs.get(i).debug);
            System.out.println(Funcs.get(i).have_local);
            System.out.println(Funcs.get(i).have_params);
        }

    }

    //把元素加到最新的函数里面。
    public void AddToLatestFuc(Element tar){
        Funcs.get(Funcs.size()-1).have_local.add(tar);
    }

    //传入一个名字的字符串，根据这个字符串在整个符号表里找到对应的element
    public Element fromSignTableReturnElem(String name) throws Error{

        for(int i=Standard.SYM.size()-1;i>=0;i--){
            var en=Standard.SYM.get(i);
            for(int j=0;j<en.size();j++){
                if(en.get(j).name.equals(name))
                {
                    return en.get(j);
                }
            }
        }
        throw new Error("没有在符号表找到这个量，应该是没定义:"+name);
    }

    /** 当前偷看的 token */
    Token peekedToken = null;


    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }


    private void analyseProgram() throws CompileError {
        isCreateFun=false;
        WhileList.add(0);

        ////符号表第零层
        Standard=new Zone();
        Standard.level_now=0;
        ArrayList<Element> BASE=new ArrayList();
        Standard.SYM.add(BASE);

        //符号表里全局的start
        Element ST=new Element();
        ST.name="_start";
        ST.isConst=true;
        ST.isFuncName=true;
        BASE.add(ST);

        //funcs的第0位永远是startfunc
        BigFunc Startfun=new BigFunc();
        Startfun.Funid=0;
        Startfun.Funname="_start";
        Funcs.add(Startfun);


        if(nextIf(TokenType.EOF)!=null)
        {
            System.out.println("好的，你是个空函数");
            return ;
        }
        var sign=peek();
        while(sign.getTokenType()==TokenType.CONST_KW||sign.getTokenType()==TokenType.LET_KW||sign.getTokenType()==TokenType.FN_KW)
        {
            if(sign.getTokenType()==TokenType.FN_KW)
                analyseFunction();
            else if(sign.getTokenType()==TokenType.LET_KW)
                analyseLetdeclstmt();
            else
                analyseConstdeclstmt();

            sign=peek();
        }

        Funcs.get(0).debug.add("call"+findMainEntry());

        PrintALLBitches();

        Parser LOL=new Parser();
        LOL.init();
        LOL.global(Standard.SYM.get(0));
        LOL.function(Funcs,Standard.SYM.get(0));

        this.FFFFFFFF=LOL.INS;

        LOL.checkINS();


        expect(TokenType.EOF);
    }

    private void analyseFunction() throws CompileError {
        isCreateFun=true;
        seeReturn=false;


        BigFunc Afun=new BigFunc();
        Afun.Funid=Funcs.size();
        Funcs.add(Afun);


        expect(TokenType.FN_KW);

        var Funcname=expect(TokenType.IDENT);

        //函数名字不能重复
        if(Funcname.getValueString().equals("_start"))
            throw new Error("this _start name has been used");
        //函数名字不能重复
        for (int i=0;i<Standard.SYM.get(0).size();i++){
            var tmp=Standard.SYM.get(0).get(i);
            if(tmp.name.equals(Funcname.getValueString()))
                throw new Error("this name has been used");
        }


        Element tmp=new Element();
        tmp.isConst=true;
        tmp.isGlobal=true;
        tmp.isFuncName=true;
        tmp.name=Funcname.getValueString();

        Afun.Funname=Funcname.getValueString();

        Standard.SYM.get(0).add(tmp);//第0层，放函数名字和全局变量
        System.out.println(tmp+"当前层数"+Standard.level_now);

        //开始函数部分，符号栈长一层
        Standard.level_now++;
        ArrayList<Element> Nextlevel=new ArrayList();
        Standard.SYM.add(Nextlevel);


        expect(TokenType.L_PAREN);

        if(peek().getTokenType()!=TokenType.R_PAREN)
        {
            analyseFunctionparamlist();
        }
            expect(TokenType.R_PAREN);
            expect(TokenType.ARROW);

        var Functype=expect(TokenType.IDENT);

        tmp.type=Functype.getValueString();

        //检查函数的类型
        var can=checkElementType(tmp);
        Afun.Ftype=can;
        //返回值的检测
        if(Afun.Ftype==RealType.Int)
            Afun.ShouldHaveAtLeastOneReturn=1;
        if(Afun.Ftype==RealType.Double)
            Afun.ShouldHaveAtLeastOneReturn=2;
        if(Afun.Ftype==RealType.Void)
            Afun.ShouldHaveAtLeastOneReturn=0;

        if(can==RealType.Int||can==RealType.Double)
        {
            //参数全体后移一位，给返回值让位置
            Element test=new Element();
            test.name="777";
            Afun.have_params.add(test);
            for(int i=Afun.have_params.size()-1;i>0;i--)
            {
                Afun.have_params.set(i,Afun.have_params.get(i-1));
            }
            Afun.have_params.set(0,test);
        }

            analyseBlockstmt();
        //返回值的检测
        if(seeReturn){
            if(Afun.ShouldHaveAtLeastOneReturn==0)
                throw new Error("void不该有返回值");
        }
        else{
            if(Afun.ShouldHaveAtLeastOneReturn!=0)
                throw new Error("int/double该有返回值");
            else
                Funcs.get(Funcs.size()-1).debug.add("ret");
        }


        //如果是main的话，因为默认被start call所以如果不是void类型就要stackalloc一个位置
        if(Afun.Funname.equals("main")&&can!=RealType.Void){
            Funcs.get(0).debug.add("stackalloc1");
        }
    }


    //function_param -> 'const'? IDENT ':' ty
    //function_param_list -> function_param (',' function_param)*
    private void analyseFunctionparamlist() throws CompileError
    {
        analyseFunctionparam();
        while (check(TokenType.COMMA))
        {
            expect(TokenType.COMMA);
            analyseFunctionparam();
        }
    }

    private void analyseFunctionparam() throws CompileError{

        int sign=0;//是不是Const？

        if(check(TokenType.CONST_KW))
        {
            sign=1;
            expect(TokenType.CONST_KW);
        }

        var tmp=expect(TokenType.IDENT);//参数名字
        expect(TokenType.COLON);
        var tmp2=expect(TokenType.IDENT);//参数类型

        Element para=new Element();
        if(sign==1)
            para.isConst=true;
        para.isPara=true;
        para.isGlobal=false;
        para.type=tmp2.getValueString();
        para.name=tmp.getValueString();

        Funcs.get(Funcs.size()-1).have_params.add(para);

        System.out.println(para+"当前层数"+Standard.level_now);
        Standard.SYM.get(Standard.level_now).add(para);
    }


    private void analyseBlockstmt() throws CompileError
    {
        expect(TokenType.L_BRACE);

        if(isCreateFun){isCreateFun=false;}
        else
            {
                Standard.level_now++;
                ArrayList<Element> Nextlevel=new ArrayList();
                Standard.SYM.add(Nextlevel);
            }


        var sign=peek();
        if(sign.getTokenType()==TokenType.R_BRACE)
        {
            expect(TokenType.R_BRACE);

            Standard.SYM.remove(Standard.level_now);
            Standard.level_now--;

            return;
        }
        else{
            while(sign.getTokenType()==TokenType.MINUS||sign.getTokenType()==TokenType.IDENT||
                    sign.getTokenType()==TokenType.UINT_LITERAL||sign.getTokenType()==TokenType.CHAR_LITERAL||
                    sign.getTokenType()==TokenType.STRING_LITERAL||sign.getTokenType()==TokenType.DOUBLE_LITERAL||
                    sign.getTokenType()==TokenType.L_PAREN||sign.getTokenType()==TokenType.LET_KW||
                    sign.getTokenType()==TokenType.IF_KW||sign.getTokenType()==TokenType.WHILE_KW||
                    sign.getTokenType()==TokenType.BREAK_KW||sign.getTokenType()==TokenType.CONTINUE_KW||
                    sign.getTokenType()==TokenType.CONST_KW||sign.getTokenType()==TokenType.RETURN_KW||
                    sign.getTokenType()==TokenType.SEMICOLON||sign.getTokenType()==TokenType.L_BRACE)
            {
                analyseStmt();
                sign=peek();
            }
            expect(TokenType.R_BRACE);

            Standard.SYM.remove(Standard.level_now);
            Standard.level_now--;
        }
    }

    private void analyseStmt() throws CompileError
    {
        var sign=peek();
        if(sign.getTokenType()==TokenType.MINUS||sign.getTokenType()==TokenType.IDENT||
                sign.getTokenType()==TokenType.UINT_LITERAL||sign.getTokenType()==TokenType.CHAR_LITERAL||
                sign.getTokenType()==TokenType.STRING_LITERAL||sign.getTokenType()==TokenType.DOUBLE_LITERAL||
                sign.getTokenType()==TokenType.L_PAREN)
            analyseExprstmt();

        else if(sign.getTokenType()==TokenType.LET_KW)
            analyseLetdeclstmt();

        else if(sign.getTokenType()==TokenType.CONST_KW)
            analyseConstdeclstmt();

        else if(sign.getTokenType()==TokenType.IF_KW)
            analyseIfstmt();

        else if(sign.getTokenType()==TokenType.WHILE_KW)
            analyseWhilestmt();

        else if(sign.getTokenType()==TokenType.BREAK_KW)
            analyseBreakstmt();

        else if(sign.getTokenType()==TokenType.CONTINUE_KW)
            analyseContinuestmt();

        else if(sign.getTokenType()==TokenType.RETURN_KW)
            analyseReturnstmt();

        else if(sign.getTokenType()==TokenType.L_BRACE)
            analyseBlockstmt();

        else if(sign.getTokenType()==TokenType.SEMICOLON)
            analyseEmptystmt();
    }

    //已完成
    private void analyseExprstmt() throws CompileError{
        analyseExpr();
        expect(TokenType.SEMICOLON);
    }

    private void analyseIfstmt() throws CompileError{
        expect(TokenType.IF_KW);
        analyseExpr();

        int ST=Funcs.get(Funcs.size()-1).debug.size();
        Funcs.get(Funcs.size()-1).debug.add(FalseJump?"br.false":"br.true");

        analyseBlockstmt();


        int MID2=Funcs.get(Funcs.size()-1).debug.size();
        Funcs.get(Funcs.size()-1).debug.add("br");
        int MID=Funcs.get(Funcs.size()-1).debug.size();

        var proto=Funcs.get(Funcs.size()-1).debug.get(ST);
        var bias=MID-ST-1;
        Funcs.get(Funcs.size()-1).debug.set(ST,proto+bias);

        if(check(TokenType.ELSE_KW))
        {
            expect(TokenType.ELSE_KW);
            //if else if
            if(check(TokenType.IF_KW))
            {
                analyseIfstmt();
            }
            //if else
            else
            { analyseBlockstmt(); }
        }
        //单if
        else{ }

        int ED=Funcs.get(Funcs.size()-1).debug.size();

        var p2=Funcs.get(Funcs.size()-1).debug.get(MID2);
        int bias2=ED-MID2-1;
        Funcs.get(Funcs.size()-1).debug.set(MID2,p2+bias2);

    }



    //已完成
    private void analyseReturnstmt() throws CompileError{
        expect(TokenType.RETURN_KW);

        boolean goExpr=false;

        if(!check(TokenType.SEMICOLON))
        {

            Funcs.get(Funcs.size()-1).debug.add("arga0");
            zhan.push(RealType.Addr);

            analyseExpr();
            goExpr=true;

            Funcs.get(Funcs.size()-1).debug.add("store.64");

            zhan.popCheck(Funcs.get(Funcs.size()-1).Ftype);
            zhan.popCheck(RealType.Addr);

        }
        expect(TokenType.SEMICOLON);

        BigFunc Th=Funcs.get(Funcs.size()-1);
        if(Th.ShouldHaveAtLeastOneReturn!=0){
            seeReturn=true;
            if(!goExpr)
                throw new Error("对应函数类型return后面应该有个expr");
            if((Th.ShouldHaveAtLeastOneReturn==1&&zhan.TopOfStack()==RealType.Int)||
                    (Th.ShouldHaveAtLeastOneReturn==2&&zhan.TopOfStack()==RealType.Double))
            {
                Th.debug.add("ret");
            }
            else
                throw new Error("函数是"+Th.Ftype+"但是return了个"+zhan.TopOfStack());
        }
        else{
            if(!goExpr)
                Th.debug.add("ret");
            else
                throw new Error("对应函数类型return后面不应该有个expr"+Th.Ftype+Th.Funname+Th.ShouldHaveAtLeastOneReturn);
        }




    }

    //已完成
    private void analyseEmptystmt() throws CompileError{expect(TokenType.SEMICOLON);}

    //（已完成）
    private void analyseContinuestmt() throws CompileError {
        expect(TokenType.CONTINUE_KW);
        expect(TokenType.SEMICOLON);
        if(WhileList.size()==1)
            throw new Error("没有循环，不能continue");
        else{
            var bias=WhileList.get(WhileList.size()-1)-Funcs.get(Funcs.size()-1).debug.size()-1;
            Funcs.get(Funcs.size()-1).debug.add("br"+bias);
        }

    }

    //（已完成）
    private void analyseBreakstmt() throws CompileError {
        expect(TokenType.BREAK_KW);
        expect(TokenType.SEMICOLON);
        if(WhileList.size()==1)
            throw new Error("没有循环，不能break");
        else
            Funcs.get(Funcs.size()-1).debug.add("break");
    }

    //已完成
    private void analyseWhilestmt() throws CompileError{
        //记录开始while地方
        int startpoint=Funcs.get(Funcs.size()-1).debug.size();
        WhileList.add(startpoint);

        expect(TokenType.WHILE_KW);
        analyseExpr();

        //记录循环开始的地方
        int rollpoint=Funcs.get(Funcs.size()-1).debug.size();

        if(WhileList.get(0)==1)
            Funcs.get(Funcs.size()-1).debug.add("br.false");
        else if(WhileList.get(0)==-1)
            Funcs.get(Funcs.size()-1).debug.add("br.true");
        else
            throw new Error("???");


        analyseBlockstmt();

        //把WhilList顶上的那个记录去掉
        WhileList.remove(WhileList.size()-1);

        //记录while{}之后的位置
        int endpoint=Funcs.get(Funcs.size()-1).debug.size();

        var proto=Funcs.get(Funcs.size()-1).debug.get(rollpoint);
        var bias=endpoint-rollpoint;
        Funcs.get(Funcs.size()-1).debug.set(rollpoint,proto+bias);


        //替换所有的break
        for(int i=startpoint;i<endpoint;i++){
            var tmp=Funcs.get(Funcs.size()-1).debug.get(i);
            if(tmp.equals("break")){
                var tt=endpoint-i;
                Funcs.get(Funcs.size()-1).debug.set(i,"br"+tt);
            }
        }

        var b2=startpoint-endpoint-1;
        Funcs.get(Funcs.size()-1).debug.add("br"+b2);
    }

    //已完成
    private void analyseLetdeclstmt() throws CompileError{
        //let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'

        expect(TokenType.LET_KW);
        var NAME=expect(TokenType.IDENT);
        expect(TokenType.COLON);
        var TYPE=expect(TokenType.IDENT);

        if(!TYPE.getValueString().equals("int")&&!TYPE.getValueString().equals("double"))
            throw new Error("常量声明类型错误");


        Element newEle=new Element();
        newEle.name=NAME.getValueString();
        newEle.type=TYPE.getValueString();
        newEle.isGlobal=Standard.level_now==0?true:false;
        newEle.isPara=false;
        newEle.isConst=false;
        Standard.SYM.get(Standard.level_now).add(newEle);
        System.out.println(newEle+"当前层数"+Standard.level_now);

        AddToLatestFuc(newEle);

        var tmp=peek();
        if(tmp.getTokenType()==TokenType.ASSIGN)
        {
            pushGiveDebug(newEle);

            expect(TokenType.ASSIGN);
            analyseExpr();

            zhan.popCheck(this.checkElementType(newEle));
            zhan.popCheck(RealType.Addr);

            BigFunc taget=Funcs.get(Funcs.size()-1);
            taget.debug.add("store.64");

        }
        expect(TokenType.SEMICOLON);
    }


    //常量赋值语句，类型只有int double（已完成）
    private void analyseConstdeclstmt() throws CompileError{
        //const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'

        expect(TokenType.CONST_KW);
        var NAME=expect(TokenType.IDENT);
        expect(TokenType.COLON);
        var TYPE=expect(TokenType.IDENT);

        if(!TYPE.getValueString().equals("int")&&!TYPE.getValueString().equals("double"))
            throw new Error("常量声明类型错误");

        Element newEle=new Element();
        newEle.name=NAME.getValueString();
        newEle.type=TYPE.getValueString();
        newEle.isGlobal=Standard.level_now==0?true:false;
        newEle.isPara=false;
        newEle.isConst=true;
        Standard.SYM.get(Standard.level_now).add(newEle);
        System.out.println(newEle+"当前层数"+Standard.level_now);

        AddToLatestFuc(newEle);

        //变量的地址入栈
        pushGiveDebug(newEle);

        expect(TokenType.ASSIGN);
        analyseExpr();
        expect(TokenType.SEMICOLON);


        zhan.popCheck(this.checkElementType(newEle));
        zhan.popCheck(RealType.Addr);

        BigFunc taget=Funcs.get(Funcs.size()-1);
        taget.debug.add("store.64");

    }


    // expr 删除左递归的表达
    // expr -> expr_1 （ = expr_1）？
    // expr_1 -> expr_2 { sign_1 expr_2}   sign_1 -> > < >= <= == !=
    // expr_2 -> expr_3 { sign_2 expr_3}   sign_2 -> + -
    // expr_3 -> expr_4 { sign_3 expr_4}   sign_3 -> * /
    // expr_4 -> expr_5 { as ty}
    // expr_5 -> -expr_5 | Ident(expr,expr...) |(expr)| 15| 15.6E4 | "sadfasd"

    //根部(已完成)
    private void analyseExpr() throws CompileError{
        analyseE1();
        if(check(TokenType.ASSIGN))
        {
            next();
            analyseE1();
            var x1=zhan.TopOfStack();
            var x2=zhan.TopMinusOne();
            if(x1==null||x2==null)
                throw new Error("栈上的操作数不够！"+ peek().getStartPos()+x1+x2+zhan.stack.size());
            if(x2==RealType.Addr&&(x1==RealType.Double||x1==RealType.Int)){
                Funcs.get(Funcs.size()-1).debug.add("store.64");
                zhan.pop();
                zhan.popCheck(RealType.Addr);
            }
            else
                throw new Error("栈上的类型不对呀！"+x1+x2);
        }

    }

    //比较运算（已完成）
    // expr_1 -> expr_2 { sign_1 expr_2}   sign_1 -> > < >= <= == !=
    private void analyseE1() throws CompileError{
        analyseE2();
        while(check(TokenType.LT)||check(TokenType.GT)||check(TokenType.LE)||check(TokenType.GE)||check(TokenType.EQ)
                ||check(TokenType.NEQ)){
            var key=next();
            analyseE2();

            //类型不同的是不能被比较的
            var x1=zhan.TopOfStack();
            var x2=zhan.TopMinusOne();
            if(x1==null||x2==null)
                throw new Error("栈上的操作数不够！"+ peek().getStartPos()+x1+x2+zhan.stack.size());
            if(x1!=x2)
                throw new Error("两者类型不一样不能比"+x1+x2);
            if(!(x1==RealType.Int||x1==RealType.Double))
                throw new Error("两者的类型既不是int也不是double"+x1);


            if(x1==RealType.Int)
                Funcs.get(Funcs.size()-1).debug.add("cmp.i");
            else
                Funcs.get(Funcs.size()-1).debug.add("cmp.f");

            zhan.pop();
            zhan.pop();
            zhan.push(RealType.CmpTrue);

            if(key.getTokenType()==TokenType.LT)
            {
                Funcs.get(Funcs.size()-1).debug.add("set.lt");
                WhileList.set(0,1);
                FalseJump=true;
            }
            else if(key.getTokenType()==TokenType.GT)
            {
                Funcs.get(Funcs.size()-1).debug.add("set.gt");
                WhileList.set(0,1);
                FalseJump=true;
            }
            else if(key.getTokenType()==TokenType.LE)
            {
                Funcs.get(Funcs.size()-1).debug.add("set.lt");
                WhileList.set(0,-1);
                FalseJump=false;
            }
            else if(key.getTokenType()==TokenType.GE)
            {
                Funcs.get(Funcs.size()-1).debug.add("set.gt");
                WhileList.set(0,-1);
                FalseJump=false;
            }
            else if(key.getTokenType()==TokenType.EQ)
            {WhileList.set(0,-1);   FalseJump=false;}
            else if(key.getTokenType()==TokenType.NEQ)
            {WhileList.set(0,1);    FalseJump=true;}

        }
    }

    //加减运算（已完成）
    private void analyseE2() throws CompileError{
        analyseE3();
        while(check(TokenType.PLUS)||check(TokenType.MINUS)){
            var key=next();
            analyseE3();

            var x1=zhan.TopOfStack();
            var x2=zhan.TopMinusOne();
            if(x1==RealType.Int&&x2==RealType.Int){
                if(key.getValueString().equals("+")){
                    Funcs.get(Funcs.size()-1).debug.add("add.i");
                    zhan.popCheck(RealType.Int);
                }
                else{
                    Funcs.get(Funcs.size()-1).debug.add("sub.i");
                    zhan.popCheck(RealType.Int);
                }
            }
            else if(x1==RealType.Double&&x2==RealType.Double){
                if(key.getValueString().equals("+")){
                    Funcs.get(Funcs.size()-1).debug.add("add.f");
                    zhan.popCheck(RealType.Double);
                }
                else{
                    Funcs.get(Funcs.size()-1).debug.add("sub.f");
                    zhan.popCheck(RealType.Double);
                }
            }
            else
                throw new Error("不支持的运算类型：栈顶"+x1+key.getValueString()+"栈次"+x2);

        }
    }

    //乘除运算（已完成）
    //expr3 -> expr4 { *或者/ expr4 }
    private void analyseE3() throws CompileError{
        analyseE4();
        while(check(TokenType.MUL)||check(TokenType.DIV)){
            var key=next();
            analyseE4();

            var x1=zhan.TopOfStack();
            var x2=zhan.TopMinusOne();
            if(x1==RealType.Int&&x2==RealType.Int){
                if(key.getValueString().equals("*")){
                    Funcs.get(Funcs.size()-1).debug.add("mul.i");
                    zhan.popCheck(RealType.Int);
                }
                else{
                    Funcs.get(Funcs.size()-1).debug.add("div.i");
                    zhan.popCheck(RealType.Int);
                }
            }
            else if(x1==RealType.Double&&x2==RealType.Double){
                if(key.getValueString().equals("*")){
                    Funcs.get(Funcs.size()-1).debug.add("mul.f");
                    zhan.popCheck(RealType.Double);
                }
                else{
                    Funcs.get(Funcs.size()-1).debug.add("div.f");
                    zhan.popCheck(RealType.Double);
                }
            }
            else
                throw new Error("不支持的运算类型：栈顶"+x1+key.getValueString()+"栈次"+x2);
        }
    }

    //类型转换表达式 int&double（已完成）
    private void analyseE4() throws CompileError{
        analyseE5();
        while(check(TokenType.AS_KW)){
            expect(TokenType.AS_KW);//AS
            var key=next();//ty
            var ty=key.getValueString();
            if(ty.equals("int")&&zhan.TopOfStack()==RealType.Double)
            {
                Funcs.get(Funcs.size()-1).debug.add("ftoi");
                zhan.popCheck(RealType.Double);
                zhan.push(RealType.Int);
            }
            else if(ty.equals("double")&&zhan.TopOfStack()==RealType.Int)
            {
                Funcs.get(Funcs.size()-1).debug.add("itof");
                zhan.popCheck(RealType.Int);
                zhan.push(RealType.Double);
            }
            else
                throw new Error("Analyse as_expr Error"+ key.getStartPos());
        }
    }


    // expr_5 -> -expr_5 | Ident(expr,expr...) |(expr)| 15| 15.6E4 | "sadfasd"（已完成50%）
    private void analyseE5() throws CompileError{

        //符号“最后”再加到栈上
        Boolean isNeg=false;
        while (check(TokenType.MINUS)){
            next();
            isNeg=!isNeg;
        }

        if(check(TokenType.IDENT))//Ident(expr,expr...)?
        {
            var key=next();

            if(check(TokenType.L_PAREN))
            {
                expect(TokenType.L_PAREN);
                //先看是否是builtin函数
                var bt=key.getValueString();
                boolean isbuitin=false;
                if(bt.equals("getint")||bt.equals("getdouble")||bt.equals("getchar")||
                        bt.equals("putint")||bt.equals("putdouble")||bt.equals("putchar")||
                        bt.equals("putstr")||bt.equals("putln"))
                    isbuitin=true;

                if (isbuitin)
                    handleBuiltin(key);
                else
                    {
                        int isitAfuc=findFunc(key.getValueString());
                        if(isitAfuc==-1)
                            throw new Error("查无此函数"+key.getValueString());

                        else
                            {
                                if(Funcs.get(isitAfuc).Ftype!=RealType.Void){
                                    Funcs.get(Funcs.size()-1).debug.add("stackalloc1");
                                }
                                int paramnum=Funcs.get(isitAfuc).have_params.size();
                                /////////////////1226
                                if(Funcs.get(isitAfuc).Ftype!=RealType.Void)
                                    paramnum--;
                                /////////////////1226

                                if(paramnum==0)
                                { }
                                else {
                                    analyseExpr();
                                    for (int i=0;i<paramnum-1;i++)
                                    {
                                        expect(TokenType.COMMA);
                                        analyseExpr();
                                    }
                                }

                                Funcs.get(Funcs.size()-1).debug.add("call"+isitAfuc);

                                //1226
                                zhan.push(Funcs.get(isitAfuc).Ftype);
                                //1226

                                expect(TokenType.R_PAREN);
                            }
                    }
            }

            else//参数
                {
                    //先找这个参数
                    Element tar=null;
                    tar=fromSignTableReturnElem(key.getValueString());

                    //防一手常量赋值
                    if(check(TokenType.ASSIGN)){
                        if(tar.isConst)
                            throw new Error("常量不能再被赋值了大叔...");
                    }

                    //左边是加载地址，右边是加载值
                    pushGiveDebug(tar);
                    if(!check(TokenType.ASSIGN)){
                        Funcs.get(Funcs.size()-1).debug.add("load.64");
                        zhan.popCheck(RealType.Addr);
                        zhan.push(checkElementType(tar));
                    }


                }


        }
        else if (check(TokenType.L_PAREN))//(expr)
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
        }
        else if(check(TokenType.STRING_LITERAL))
        {
           var num=next();
           //当成一个全局变量,push他的偏移
            Element newEle=new Element();
            newEle.name=num.getValueString();
            newEle.type="String";
            newEle.isGlobal=true;
            newEle.isPara=false;
            newEle.isConst=true;

            Standard.SYM.get(0).add(newEle);
            Funcs.get(0).have_local.add(newEle);

            int offset=Funcs.get(0).findLocal(newEle);
            Funcs.get(Funcs.size()-1).debug.add("push"+offset);

            zhan.push(RealType.Int);

        }
        else if(check(TokenType.DOUBLE_LITERAL))
        {
            var num=next();
            zhan.push(RealType.Double);

            BigFunc taget=Funcs.get(Funcs.size()-1);
            taget.debug.add("push"+num.getValue());
        }
        else if(check(TokenType.UINT_LITERAL))
        {
            var num=next();
            zhan.push(RealType.Int);

            WhileList.set(0,1);
            FalseJump=true;

            BigFunc taget=Funcs.get(Funcs.size()-1);
            taget.debug.add("push"+num.getValue());
        }
        else
            throw new Error(peek().getStartPos()+"的前一个有问题");


        if(isNeg){
            //只有int和double可以加-号，别的类型的值直接报错
            if(zhan.TopOfStack()==RealType.Double)
                Funcs.get(Funcs.size()-1).debug.add("neg.f");
            else if(zhan.TopOfStack()==RealType.Int)
                Funcs.get(Funcs.size()-1).debug.add("neg.i");
            else
                throw new Error("当前类型不能取负号"+zhan.TopOfStack());
        }

    }

    //这里已经吃了左括号了,还要吃参数和右括号（已完成）
    private void handleBuiltin(Token key)throws CompileError{
        var BuildName=key.getValueString();
        if(BuildName.equals("getint"))
        {
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("scan.i");
            zhan.push(RealType.Int);
            return;
        }
        if(BuildName.equals("getdouble"))
        {
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("scan.f");
            zhan.push(RealType.Double);
            return;
        }
        if(BuildName.equals("getchar"))
        {
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("scan.c");
            zhan.push(RealType.Int);
            return;
        }
        if(BuildName.equals("putint"))
        {
            analyseExpr();
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("print.i");
            zhan.popCheck(RealType.Int);
            return;
        }
        if(BuildName.equals("putdouble"))
        {

            analyseExpr();
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("print.f");
            zhan.popCheck(RealType.Double);
            return;
        }
        if(BuildName.equals("putchar"))
        {
            analyseExpr();
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("print.c");
            zhan.popCheck(RealType.Int);
            return;
        }
        if(BuildName.equals("putstr"))
        {
            analyseExpr();
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("print.s");
            zhan.popCheck(RealType.Int);
            return;
        }
        if(BuildName.equals("putln"))
        {
            expect(TokenType.R_PAREN);
            Funcs.get(Funcs.size()-1).debug.add("println");
            return;
        }
    }

}
