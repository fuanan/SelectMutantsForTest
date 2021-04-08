import java.util.ArrayList;

public class MutantInfo {

    //ID
    public int mutantID;
    //FuncName
    public String funcName;
    //IRPosInFunc
    public ArrayList<Integer> IRPosInFunc;
    //SrcLoc
    public String mutatedSrcFile;
    public int mutatedSrcLoc;
    public int mutatedSrcColumn;
    //Type
    public String sourceFragment;
    public String followFragment;

    public MutantInfo(int mutantID, String funcName, ArrayList<Integer> IRPosInFunc, String mutatedSrcLoc, String mutationType){
        this.mutantID = mutantID;
        this.funcName = funcName;
        this.IRPosInFunc = IRPosInFunc;
        //this.mutatedSrcLoc = mutatedSrcLoc;
        //this.mutationType = mutationType;
        String[] s1;
        if (!mutatedSrcLoc.equals("")){
            s1 = mutatedSrcLoc.split(":");
        }else{
            s1 = new String[3];
            s1[2] = s1[1] = s1[0] = "-1";
        }
        this.mutatedSrcFile = s1[0];
        this.mutatedSrcLoc = Integer.parseInt(s1[1]);
        this.mutatedSrcColumn = Integer.parseInt(s1[2]);

        String[] s2;
        if (!mutationType.equals("")){
            s2 = mutationType.split("!");
        }else {
            s2 = new String[2];
            s2[1] = s2[0] = "";
        }
        this.sourceFragment = s2[0];
        this.followFragment = s2[1];
    }

}
