package miniplc0java.analyser;

import java.util.ArrayList;

public class BigFunc {

    int Funid;
    RealType Ftype;
    int ShouldHaveAtLeastOneReturn;
    String Funname;
    ArrayList<ArrayList<Byte>> insSet=new ArrayList<>();
    ArrayList<String> debug= new ArrayList<>();

    ArrayList<Element> have_local=new ArrayList<>();
    ArrayList<Element> have_params=new ArrayList<>();

    int findLocal(Element tar) throws Error{
        for(int i=0;i<have_local.size();i++)
            if(have_local.get(i).name.equals(tar.name))
                return i;
        throw new Error("找不到名字叫做"+tar.name+"的局部变量");
    }

    int findLocal(Element tar,int lll) throws Error{
        for(int i=have_local.size()-1;i>=0;i--)
            if(have_local.get(i).name.equals(tar.name)&&have_local.get(i).levelshit<=lll)
                return i;
        throw new Error("找不到名字叫做"+tar.name+"的局部变量");
    }

    int findPara(Element tar)throws Error{
        for(int i=0;i<have_params.size();i++)
            if(have_params.get(i).name.equals(tar.name))
                return i;
        throw new Error("找不到名字叫做"+tar.name+"的参数");
    }
}


