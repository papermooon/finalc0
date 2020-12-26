package miniplc0java.analyser;

import java.util.ArrayList;

public class Parser {

    ArrayList<Byte> INS = new ArrayList<>();


    void init() throws Error{
        var x=new byte[]{0x72,0x30,0x3B,0x3E,0x00,0x00,0x00,0x01};
        expand(x);
    }

    //只拿根部的符号表
    void global(ArrayList<Element> x){
        expand(int2Bytes(x.size()));

        for(int i=0;i<x.size();i++){

            var tmp=x.get(i);
            System.out.println(tmp);

            if(tmp.isConst)
                expand(new byte[]{0x01});
            else
                expand(new byte[]{0x00});

            if(tmp.isFuncName) {
                expand(int2Bytes(tmp.name.length()));
                expand(tmp.name.getBytes());
            }
            else {
                expand(int2Bytes(8));
                expand(Long2Bytes(0));
            }
        }
    }

    void function(ArrayList<BigFunc> x,ArrayList<Element> FU) throws Error{
        System.out.println("有"+x.size()+"个函数");
        expand(int2Bytes(x.size()));

        for(int i=0;i<x.size();i++){
            var tmp=x.get(i);

            //全局变量中的位置
            int index=-1;
            for(int j=0;j<FU.size();j++){
                if(FU.get(j).isFuncName)
                    if(FU.get(j).name.equals(tmp.Funname))
                    {
                        index=j;
                        break;
                    }
            }
            if(index!=-1)
                expand(int2Bytes(index));
            else
                throw new Error("找不到这个函数，填不了16进制");

            //return slot
            if(tmp.ShouldHaveAtLeastOneReturn!=0)
                expand(int2Bytes(1));
            else
                expand(int2Bytes(0));

            //param slot
            if(tmp.ShouldHaveAtLeastOneReturn!=0)
            expand(int2Bytes(tmp.have_params.size()-1));
            else
                expand(int2Bytes(tmp.have_params.size()));

            //local slot
            if(tmp.Funname.equals("_start"))
                expand(int2Bytes(0));
            else
                expand(int2Bytes(tmp.have_local.size()));

            //body count
            expand(int2Bytes(tmp.debug.size()));

            //指令
            for(int k=0;k<tmp.debug.size();k++)
            {
                var item=tmp.debug.get(k);
                PAR(item);
            }

        }

    }

    void PAR(String x) throws Error{
        if(x.matches("Local(.*)")){
            int op=Integer.parseInt(x.substring(5));
            expand(new byte[]{0x0a});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("Global(.*)")){
            int op=Integer.parseInt(x.substring(6));
            expand(new byte[]{0x0c});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("arga(.*)")){
            int op=Integer.parseInt(x.substring(4));
            expand(new byte[]{0x0b});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("call(.*)")){
            int op=Integer.parseInt(x.substring(4));
            expand(new byte[]{0x48});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("ret(.*)")){
            expand(new byte[]{0x49});
            return;
        }

        if(x.matches("stackalloc(.*)")){
            int op=Integer.parseInt(x.substring(10));
            expand(new byte[]{0x1a});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("br.false(.*)")){
            int op=Integer.parseInt(x.substring(8));
            expand(new byte[]{0x42});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("br.true(.*)")){
            int op=Integer.parseInt(x.substring(7));
            expand(new byte[]{0x43});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("br(.*)")){
            int op=Integer.parseInt(x.substring(2));
            expand(new byte[]{0x41});
            expand(int2Bytes(op));
            return;
        }

        if(x.matches("push(.*)")){
            int op=Integer.parseInt(x.substring(4));
            expand(new byte[]{0x01});
            expand(Long2Bytes(op));
            return;
        }

        if(x.matches("store.64(.*)")){
            expand(new byte[]{0x17});
            return;
        }

        if(x.matches("load.64(.*)")){
            expand(new byte[]{0x13});
            return;
        }

        if(x.matches("cmp.i(.*)")){
            expand(new byte[]{0x30});
            return;
        }
        if(x.matches("cmp.f(.*)")){
            expand(new byte[]{0x32});
            return;
        }

        if(x.matches("set.lt(.*)")){
            expand(new byte[]{0x39});
            return;
        }

        if(x.matches("set.gt(.*)")){
            expand(new byte[]{0x3a});
            return;
        }

        if(x.matches("add.i(.*)")){
            expand(new byte[]{0x20});
            return;
        }

        if(x.matches("sub.i(.*)")){
            expand(new byte[]{0x21});
            return;
        }
        if(x.matches("mul.i(.*)")){
            expand(new byte[]{0x22});
            return;
        }
        if(x.matches("div.i(.*)")){
            expand(new byte[]{0x23});
            return;
        }

        if(x.matches("add.f(.*)")){
            expand(new byte[]{0x24});
            return;
        }

        if(x.matches("sub.f(.*)")){
            expand(new byte[]{0x25});
            return;
        }
        if(x.matches("mul.f(.*)")){
            expand(new byte[]{0x26});
            return;
        }
        if(x.matches("div.f(.*)")){
            expand(new byte[]{0x27});
            return;
        }

        if(x.matches("ftoi(.*)")){
            expand(new byte[]{0x37});
            return;
        }
        if(x.matches("itof(.*)")){
            expand(new byte[]{0x36});
            return;
        }
        if(x.matches("neg.f(.*)")){
            expand(new byte[]{0x35});
            return;
        }
        if(x.matches("neg.i(.*)")){
            expand(new byte[]{0x34});
            return;
        }

        if(x.matches("scan.i(.*)")){
            expand(new byte[]{0x50});
            return;
        }
        if(x.matches("scan.f(.*)")){
            expand(new byte[]{0x52});
            return;
        }

        if(x.matches("scan.c(.*)")){
            expand(new byte[]{0x51});
            return;
        }

        if(x.matches("print.i(.*)")){
            expand(new byte[]{0x54});
            return;
        }
        if(x.matches("print.f(.*)")){
            expand(new byte[]{0x56});
            return;
        }

        if(x.matches("print.c(.*)")){
            expand(new byte[]{0x55});
            return;
        }

        if(x.matches("print.s(.*)")){
            expand(new byte[]{0x57});
            return;
        }

        if(x.matches("println(.*)")){
            expand(new byte[]{0x58});
            return;
        }

        throw new Error("没见过的符号"+x);
    }

    void expand(byte[] tmp){
        for(int i=0;i<tmp.length;i++){
            INS.add(tmp[i]);
        }
    }

    void checkINS(){

        for(int i=0;i<INS.size();i++){
            System.out.print(byteToHex(INS.get(i)));

        }
    }

    /**
     * 字节转十六进制
     * @param b 需要进行转换的byte字节
     * @return  转换后的Hex字符串
     */
    public static String byteToHex(byte b){
        String hex = Integer.toHexString(b & 0xFF);
        if(hex.length() < 2){
            hex = "0" + hex;
        }
        return hex;
    }

    byte[] Long2Bytes(long num) {
        byte[] BT = new byte[8];
        for (int tmp = 0; tmp < 8; ++tmp) {
            int bias = 64 - (tmp + 1) * 8;
            BT[tmp] = (byte) ((num >> bias) & 0xff);
        }
        return BT;
    }

    byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        for (int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }


}
