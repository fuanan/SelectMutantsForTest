import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SelectMutants {


    public static void main(String[] args){

        String muInfoFileDir = "d:\\subjects(20210314)\\mart-out-0";
        ArrayList<MutantInfo> muInfos = readInAllMutantInfo(muInfoFileDir);
        HashMap<String, ArrayList<MutantInfo>> locMapping = new HashMap<>();

        String currLoc;
        ArrayList<MutantInfo> currMuSet;
        assert muInfos != null;
        String muPointIdentifier = "";
        for (MutantInfo muInfo : muInfos) {
            //以变异点分类
            muPointIdentifier = muInfo.mutatedSrcFile + ":" + muInfo.mutatedSrcLoc + ":" +muInfo.mutatedSrcColumn;
            currMuSet = locMapping.getOrDefault(muPointIdentifier, new ArrayList<>());
            currMuSet.add(muInfo);
            locMapping.put(muPointIdentifier, currMuSet);
            //以行号分类
            //currLoc = String.valueOf(muInfo.mutatedSrcLoc);
            //currMuSet = locMapping.getOrDefault(currLoc, new ArrayList<>());
            //currMuSet.add(muInfo);
            //locMapping.put(currLoc, currMuSet);
        }

        int count = 0;
        for (Map.Entry<String, ArrayList<MutantInfo>> e : locMapping.entrySet()) {
            count ++;
            System.out.println(count + " Loc: " + e.getKey() + "  #:" + e.getValue().size());
            for (int i = 0; i <e.getValue().size(); i ++){
                System.out.println("        type of mutation: " + e.getValue().get(i).sourceFragment + "!" + e.getValue().get(i).followFragment);
            }
        }
    }

    public static ArrayList<MutantInfo> readInAllMutantInfo(String dir) {

        ArrayList<MutantInfo> muInfo = new ArrayList<>();

        try{
            File muInfoJson = new File(dir + "/mutantsinfos.json");
            FileInputStream fis = new FileInputStream(muInfoJson);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            JsonReader jReader = new JsonReader(isr);
            jReader.beginObject();
            int currMuID;
            String currFuncName = null;
            ArrayList<Integer> currIRPosInFunc = null;
            String currSrcLoc = null;
            String currType = null;

            int countMu = 0;

            while(jReader.hasNext()){
                currMuID = Integer.parseInt(jReader.nextName());
                jReader.beginObject();
                while (jReader.hasNext()){
                    String name = jReader.nextName();
                    switch (name){
                        case "FuncName": currFuncName = jReader.nextString(); break;
                        case "IRPosInFunc":
                            currIRPosInFunc = new ArrayList<>();
                            jReader.beginArray();
                            while(jReader.hasNext()){
                                currIRPosInFunc.add(jReader.nextInt());
                            }
                            jReader.endArray();
                            break;
                        case "SrcLoc": currSrcLoc = jReader.nextString(); break;
                        case "Type": currType = jReader.nextString(); break;
                    }
                }
                muInfo.add(new MutantInfo(currMuID, currFuncName, currIRPosInFunc, currSrcLoc, currType));
                countMu ++;
                System.out.println(countMu + " MuID:" + currMuID + " FuncName:" + currFuncName + " SrcLoc: " + currSrcLoc + " currType:" + currType);
                jReader.endObject();
            }
            jReader.endObject();
            jReader.close();
            return muInfo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
