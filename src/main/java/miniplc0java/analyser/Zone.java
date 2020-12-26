package miniplc0java.analyser;

import java.util.ArrayList;

public class Zone {

    ArrayList<ArrayList<Element>>SYM=new ArrayList<>();
    int level_now;

    public int tryFound(Element tar){
        for(int j=level_now;j>=0;j--)
        {
            var entry=SYM.get(j);
            for(int i=0;i<entry.size();i++)
            {
                if(entry.get(i).name.equals(tar.name))
                {
                    return j;
                }
            }
        }
        return -1;
    }


    public boolean AlreadyExist(Element tar){
        if(tryFound(tar)!=-1)
            return true;
        return false;

    }





}
