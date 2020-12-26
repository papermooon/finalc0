package miniplc0java.analyser;

public enum RealType {
    Int,Double,Addr,String,Void,CmpTrue,CmpFalse;


    public String toString() {
        if (this == Int)
            return "int";
        else if (this == Double)
            return "double";
        else if(this==Addr)
            return "addr";
        else if(this==Void)
            return "void";
        else if(this==CmpTrue)
            return "cmptrue";
        else if(this==CmpFalse)
            return "cmpfalse";
        else
            return "String";
    }
}
