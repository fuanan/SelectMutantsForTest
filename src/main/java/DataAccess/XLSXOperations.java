package DataAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javafx.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSXOperations {

    public static LinkedHashMap<Integer, Pair<Double, Double>> ReadPilotTestResultXlsx(String path, String fileName){
        try{
            File xlsxFile = new File(path + "/" + fileName);
            FileInputStream fis = new FileInputStream(xlsxFile);
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheet("Sheet1");

            int rowID = 0;
            XSSFRow currRow = sheet.getRow(rowID);
            XSSFCell currCell = currRow.getCell(1);
            String currStr = null;
            if (currCell != null){
                currStr = currCell.toString();
            }
            double totalTCRecordNum = -1;
            if (currStr!=null){
                totalTCRecordNum = Double.parseDouble(currStr);
            }

            XSSFRow mutantIDRow = sheet.getRow(2);
            XSSFRow numberOfKillsRow = sheet.getRow(4);

            XSSFCell muIDCell;
            XSSFCell numKillsCell;
            LinkedHashMap<Integer, Pair<Double, Double>> muIDandKills = new LinkedHashMap<>();
            int cellID = 1;
            muIDCell = mutantIDRow.getCell(cellID);
            numKillsCell = numberOfKillsRow.getCell(cellID);
            int currMutantID = -1;
            double currMutantKills = -1;
            double currMutantKillRate = -1.0;
            while ((muIDCell != null)&&(numKillsCell != null)){
                currMutantID = Integer.parseInt(muIDCell.toString());
                currMutantKills = Double.parseDouble(numKillsCell.toString());
                currMutantKillRate = (double)currMutantKills / totalTCRecordNum;
                muIDandKills.put(currMutantID, new Pair<>(currMutantKills, currMutantKillRate));
                cellID++;
                muIDCell = mutantIDRow.getCell(cellID);
                numKillsCell = numberOfKillsRow.getCell(cellID);
            }
            return muIDandKills;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
