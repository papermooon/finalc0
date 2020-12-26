package miniplc0java.analyser;

public class Element {
    String name;
    String type;
    boolean isPara;
    boolean isConst;
    boolean isGlobal;
    boolean isFuncName=false;

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isPara=" + isPara +
                ", isConst=" + isConst +
                ", isGlobal=" + isGlobal +
                ", isFuncName=" + isFuncName +
                '}';
    }
}
