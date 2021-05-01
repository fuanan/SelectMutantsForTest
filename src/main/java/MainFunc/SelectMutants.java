package MainFunc;

import Entity.MutantInfo;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SelectMutants {


    public static void main(String[] args){

        String muInfoFileDir = "D:\\subjects(20210314)\\mart-out-0"; //String muInfoFileDir = "/home/anfu/test_print_tokens/source.modified.clang-compilable/mart-out-0";
        ArrayList<MutantInfo> muInfos = readInAllMutantInfo(muInfoFileDir);
        HashMap<String, ArrayList<MutantInfo>> locMapping = new HashMap<>();

        String currLoc;
        ArrayList<MutantInfo> currMuSet;
        assert muInfos != null;
        String muPointIdentifier = "";
        for (MutantInfo muInfo : muInfos) {
            //变异点加sourcefragment
            muPointIdentifier = muInfo.mutatedSrcFile + ":" +  muInfo.mutatedSrcLoc + ":" +muInfo.mutatedSrcColumn + muInfo.sourceFragment;
            currMuSet = locMapping.getOrDefault(muPointIdentifier, new ArrayList<>());
            currMuSet.add(muInfo);
            locMapping.put(muPointIdentifier, currMuSet);

            //以变异点分类
            //muPointIdentifier = muInfo.mutatedSrcFile + ":" + muInfo.mutatedSrcLoc + ":" +muInfo.mutatedSrcColumn;
            //currMuSet = locMapping.getOrDefault(muPointIdentifier, new ArrayList<>());
            //currMuSet.add(muInfo);
            //locMapping.put(muPointIdentifier, currMuSet);

            //以行号分类
            //currLoc = String.valueOf(muInfo.mutatedSrcLoc);
            //currMuSet = locMapping.getOrDefault(currLoc, new ArrayList<>());
            //currMuSet.add(muInfo);
            //locMapping.put(currLoc, currMuSet);
        }

        String pilotTestResult = "D:\\20210430_pilot_test";
        String xlsxName = "violation_rates_reduced_version.xlsx";
        //read test results from xlsx
        LinkedHashMap<Integer, Pair<Double, Double>> pilotTestOutcome = DataAccess.XLSXOperations.ReadPilotTestResultXlsx(pilotTestResult, xlsxName);

        int count = 0;
        for (Map.Entry<String, ArrayList<MutantInfo>> e : locMapping.entrySet()) {
            count ++;
            System.out.println(count + " Loc: " + e.getKey() + "  #:" + e.getValue().size());
            int id;
            double killNum;
            double killRate;
            for (int i = 0; i <e.getValue().size(); i ++){
                id = e.getValue().get(i).mutantID;
                assert pilotTestOutcome != null;
                killNum = pilotTestOutcome.get(id).getKey();
                killRate = pilotTestOutcome.get(id).getValue();
                System.out.println("        mutant ID: " + id + "  killNum: " + killNum + " killRate:  " + killRate  + "  type of mutation: " + e.getValue().get(i).sourceFragment + "!" + e.getValue().get(i).followFragment);
            }
        }


        //select Mutants
        //for (Map.Entry<String, ArrayList<Entity.MutantInfo>> e: locMapping.entrySet()){
        //    String currKey = e.getKey();
        //    ArrayList<Entity.MutantInfo> currValue = e.getValue();
        //    int currTotal = currValue.size();
        //    Random r = new Random();
        //    int randomSelected = r.nextInt(currTotal);
        //    String oldPath = muInfoFileDir + "mutants.out";
            //boolean b = CopyFile();

        //}
    }

    public static ArrayList<MutantInfo> readInAllMutantInfo(String dir) {

        ArrayList<MutantInfo> muInfo = new ArrayList<>();

        try{
            File muInfoJson = new File(dir + "/mutantsInfos.json");
            FileInputStream fis = new FileInputStream(muInfoJson);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            JsonReader jReader = new JsonReader(isr);
            jReader.beginObject();
            int currMuID;
            String currFuncName = null;
            ArrayList<Integer> currIRPosInFunc = null;
            String currSrcLoc = null;
            String currType = null;
            File realMuFile;

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
                realMuFile = new File(dir + "/mutants.out/" + currMuID);
                if (realMuFile.exists()){
                    muInfo.add(new MutantInfo(currMuID, currFuncName, currIRPosInFunc, currSrcLoc, currType));
                    countMu ++;
                    System.out.println(countMu + " MuID:" + currMuID + " FuncName:" + currFuncName + " SrcLoc: " + currSrcLoc + " currType:" + currType);
                }else{
                    System.out.println("Mutant " + currMuID + " does not exist!");
                }

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
