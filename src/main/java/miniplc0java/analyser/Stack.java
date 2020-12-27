package miniplc0java.analyser;
import miniplc0java.error.CompileError;

import java.util.ArrayList;

public class Stack {
    //模拟栈操作
    public ArrayList<RealType> stack = new ArrayList<>();

    public void gothrough(){
        for(int i=stack.size()-1;i>=0;i--)
            System.out.println(stack.get(i));
    }

    public RealType TopOfStack(){
        if(stack.size()>0)
            return stack.get(stack.size()-1);
        return null;
    }

    public RealType TopMinusOne(){
        if(stack.size()>=2)
            return stack.get(stack.size()-2);
        else
            return null;
    }

    public void push(RealType a){
        stack.add(a);
    }

    public RealType pop(){
        RealType tmp=TopOfStack();
        stack.remove(stack.size()-1);
        return tmp;
    }

    //检测弹出来的是不是这个类型，不然就报错
    public RealType popCheck(RealType ck) throws Error{
        RealType tmp=TopOfStack();
        if(tmp!=ck)
            throw new Error("栈顶类型不匹配,实际栈顶："+tmp+",尝试pop出"+ck);
        stack.remove(stack.size()-1);
        return tmp;
    }
}
