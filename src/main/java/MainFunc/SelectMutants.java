package MainFunc;

import Entity.MutantInfo;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SelectMutants {


    public static void main (String[] args){
        String muInfoFileDir = "D:\\subjects(20210314)\\mart-out-0"; //String muInfoFileDir = "/home/anfu/test_print_tokens/source.modified.clang-compilable/mart-out-0";
        String pilotTestResult = "D:\\20210430_pilot_test";
        String xlsxName = "violation_rates_reduced_version.xlsx";
        select(muInfoFileDir, pilotTestResult, xlsxName);
    }

    public static void select(String muInfoFileDir, String pilotTestResult, String xlsxName){

        ArrayList<MutantInfo> muInfos = readInAllMutantInfo(muInfoFileDir);
        LinkedHashMap<String, ArrayList<MutantInfo>> locMapping = new LinkedHashMap<>();

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

        //read test results from xlsx
        LinkedHashMap<Integer, Pair<Double, Double>> pilotTestOutcome = DataAccess.XLSXOperations.ReadPilotTestResultXlsx(pilotTestResult, xlsxName);

        printLinkedHashMap(locMapping, pilotTestOutcome, " total");

        //筛选规则
        //1 remove所有killRate大于80%的变异体
        LinkedHashMap<String, ArrayList<MutantInfo>> reducedMutantMapping;
        reducedMutantMapping = (LinkedHashMap<String, ArrayList<MutantInfo>>)locMapping.clone();

        ArrayList<MutantInfo> currList;
        for (Map.Entry<String, ArrayList<MutantInfo>> e: reducedMutantMapping.entrySet()){
            if (e.getKey().equals("print_tokens.c:202:8@")){
                System.out.println("Found");
            }
            currList = e.getValue();
            MutantInfo currMu;
            double currKillNum;
            double currKillRate;
            for (int i = 0; i < currList.size(); ){
                currMu = currList.get(i);
                assert pilotTestOutcome != null;
                currKillNum = pilotTestOutcome.get(currMu.mutantID).getKey();
                currKillRate = pilotTestOutcome.get(currMu.mutantID).getValue();
                if (currKillRate >= 0.9){
                    currList.remove(currMu);
                }else{
                    i++;
                }
            }
        }

        printLinkedHashMap(reducedMutantMapping, pilotTestOutcome, "filtered");

        //每个组中找到其candidate
        LinkedHashMap<String, ArrayList<MutantInfo>> candidateMutantMapping = new LinkedHashMap<>();
        int count2 = 0;
        int countNo = 0;
        for (Map.Entry<String, ArrayList<MutantInfo>> e : reducedMutantMapping.entrySet()){

            count2 ++;
            System.out.println(count2 + " Loc: " + e.getKey() + "  #:" + e.getValue().size());

            ArrayList<MutantInfo> curr = e.getValue();
            if (curr.size() == 0){
                System.out.println("        No mutants!");
                countNo ++;
            }else{
                MutantInfo currMu;
                double maxKillNum = -1.0;
                double currVal;
                for (int i = 0; i < e.getValue().size(); i ++){
                    currMu = e.getValue().get(i);
                    currVal = pilotTestOutcome.get(currMu.mutantID).getKey();
                    if (pilotTestOutcome.get(currMu.mutantID).getKey() > maxKillNum){
                        maxKillNum = currVal;
                    }
                }
                ArrayList<MutantInfo> candidateList = new ArrayList<>();
                if (maxKillNum != 0.0){
                    for (int i = 0; i < e.getValue().size(); i ++){
                        currMu = e.getValue().get(i);
                        currVal = pilotTestOutcome.get(e.getValue().get(i).mutantID).getKey();
                        if (currVal > 0.0){
                            candidateList.add(currMu);
                        }
                    }
                    candidateMutantMapping.put(e.getKey(), candidateList);
                }else{
                    candidateMutantMapping.put(e.getKey(), e.getValue());
                }
            }
        }

        System.out.println("Num of No mutants: " + countNo);
        printLinkedHashMap(candidateMutantMapping, pilotTestOutcome, "candidates");

        //每个组的candidate随机选一个

        LinkedHashMap<String, ArrayList<MutantInfo>> selectedMutants = new LinkedHashMap<>();
        ArrayList<MutantInfo> currCandidate;
        int currGroupTotalNum = -1;
        Random ra = new Random();
        int currSelectedID;
        ArrayList<MutantInfo> currSelected;
        int selectedCount = 0;
        int zeroKillRateCount = 0;
        for (Map.Entry<String, ArrayList<MutantInfo>> e : candidateMutantMapping.entrySet()){
            currCandidate = e.getValue();
            currGroupTotalNum = currCandidate.size();
            currSelectedID = ra.nextInt(currGroupTotalNum);
            currSelected = new ArrayList<>();
            currSelected.add(currCandidate.get(currSelectedID));
            selectedMutants.put(e.getKey(), currSelected);
            selectedCount ++;
            if (pilotTestOutcome.get(currSelected.get(0).mutantID).getKey() == 0.0){
                zeroKillRateCount ++;
            }
        }

        printLinkedHashMap(selectedMutants, pilotTestOutcome, "selected");
        double mutationScore = 1.0 - (double)zeroKillRateCount / (double)selectedCount;
        System.out.println("Mutation Score: " + mutationScore);

        //copy mutants to a seperate folder
        File selectedMutantBaseFolder = new File (muInfoFileDir + "/" + "mutants.selected");
        if (!selectedMutantBaseFolder.exists()){
            selectedMutantBaseFolder.mkdir();
        }
        int currID;
        File currMuFolderNew;
        String oldMutantBasePath = muInfoFileDir + "/mutants.out";
        String currMutantOldPath;
        File[] currTemp;
        File currMuFile;
        String currMuName;
        int finalCount = 0;
        for (Map.Entry<String, ArrayList<MutantInfo>> e : selectedMutants.entrySet()){
            currID = e.getValue().get(0).mutantID;
            currMuFolderNew = new File (selectedMutantBaseFolder.getAbsolutePath() + "/" + currID);
            assert !currMuFolderNew.exists();
            currMuFolderNew.mkdir();
            currMutantOldPath = oldMutantBasePath + "/" + currID;
            currTemp = new File (currMutantOldPath).listFiles();
            assert currTemp.length == 1;
            currMuFile = currTemp[0];
            currMuName = currMuFile.getName();
            DataAccess.FileOperations.CopyFile(currMuFile.getAbsolutePath(), currMuFolderNew + "/" + currMuName);
            finalCount ++;
        }
        System.out.println("Final Selected: " + finalCount);

    }

    public static void printLinkedHashMap(LinkedHashMap<String, ArrayList<MutantInfo>> mapping,
                                          LinkedHashMap<Integer, Pair<Double, Double>> pilotTestOutcome,
                                          String title){

        System.out.println("***************************** " + title + " *****************************");
        int count = 0;
        for (Map.Entry<String, ArrayList<MutantInfo>> e : mapping.entrySet()) {
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
        System.out.println("***************************** END *****************************");
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
