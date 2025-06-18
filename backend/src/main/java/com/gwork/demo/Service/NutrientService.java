package com.gwork.demo.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class NutrientService {

    Workbook workbook_sta;
    Workbook workbook_veg;

    // --- コンストラクタでファイルを読み込む ---
    public NutrientService(){
        String filePath_stapleAndProtein = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_主食・肉類.xlsx";
        String filePath_vegetables = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_野菜類.xlsx";
        try{
            FileInputStream fis_sta = new FileInputStream(new File(filePath_stapleAndProtein));
            this.workbook_sta = new XSSFWorkbook(fis_sta);
            FileInputStream fis_veg = new FileInputStream(new File(filePath_vegetables));
            this.workbook_veg = new XSSFWorkbook(fis_veg);
            System.out.println("excelファイルを読み込みました");
        }catch(IOException e){
            System.out.println("excelファイルを読み込めません");
        }
    }

    // --- 1食分の目安量で補正しながら、主食・肉類の栄養テーブルを返す --- 
    public double[][] getStapleAndProtein(){
        try{
            Sheet sheet = this.workbook_sta.getSheetAt(0);
            int startRowNum = 3;    //"うるち米(コシヒカリ)"の行
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行番号
            int startColNum = 3;    //"1食分の目安量"の列
            int colSize = sheet.getRow(startRowNum).getLastCellNum() - 1; //0-indexedに合わせた、データが最後に存在する列番号
            double[][] stapleAndProteinNutrients = new double[rowSize + 1 - 5][colSize + 1 - 4];    //5行分、4列分の栄養素データではない部分を除外したサイズ
            for(int rowIndex = startRowNum; rowIndex <= rowSize - 2; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                for (int colIndex = startColNum + 1; colIndex <= colSize; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    double correc = row.getCell(startColNum).getNumericCellValue() / 100; //1食分の目安量での補正係数
                    stapleAndProteinNutrients[rowIndex - startRowNum][colIndex - startColNum - 1] = cell.getNumericCellValue() * correc;
                }
                //System.out.println(Arrays.toString(stapleAndProteinNutrients[rowIndex - startRowNum]));
            }
            return stapleAndProteinNutrients;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // --- 目標値のテーブルを返す --- 
    public double[] getTargets(){
        try {
            Sheet sheet = this.workbook_sta.getSheetAt(0);
            double[] targets = new double[14];  //14種類の摂取目標値
            int colSize = 17;   //ビタミンCまでの範囲
            int startColNum = 4;    //"タンパク質"の列
            Row row = sheet.getRow(sheet.getLastRowNum());
            for (int colIndex = startColNum; colIndex <= colSize; colIndex++) {
                Cell cell = row.getCell(colIndex);
                targets[colIndex - startColNum] = cell.getNumericCellValue();
            }
            //System.out.println(Arrays.toString(targets));
            return targets;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // --- 野菜類の栄養テーブルを返す --- 
    public double[][] getVegetable(){
        try {
            Sheet sheet = this.workbook_veg.getSheetAt(0);
            int startRowNum = 3;    //"牛乳(店頭売り、紙容器入り)"の行
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行
            int startColNum = 4;    //"タンパク質"の列
            int colSize = sheet.getRow(startRowNum).getLastCellNum() - 1; //0-indexedに合わせた、データが最後に存在する列
            double[][] vegetable = new double[rowSize + 1 - 3][colSize + 1 - 4];    //3行分、4列分の栄養素データではない部分を除外したサイズ
            for(int rowIndex = startRowNum; rowIndex <= rowSize; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                for (int colIndex = startColNum; colIndex <= colSize; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    vegetable [rowIndex - startRowNum][colIndex - startColNum] = cell.getNumericCellValue();
                }
                //System.out.println(Arrays.toString(vegetable[rowIndex - startRowNum]));
            }
            return vegetable;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // --- 野菜類に関して、価格を補正するための辞書を返す --- 
    public Map<String, Double> getUnitPrice(){
        Map<String, Double> unitPrice = new LinkedHashMap<>();
        try {
            Sheet sheet = this.workbook_veg.getSheetAt(0);
            int startRowNum = 3;    //"牛乳(店頭売り、紙容器入り)"の行
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行番号
            int nameColNum = 2; //"食品名"の列
            int priColNum = 1;  //"価格の単位"の列
            for(int rowIndex = startRowNum; rowIndex <= rowSize; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                unitPrice.put(row.getCell(nameColNum).getStringCellValue(), row.getCell(priColNum).getNumericCellValue());
            }
            //System.out.println(unitPrice);
            return unitPrice;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // --- 1食分の目安量を返す --- 
    public double[] getMinimumVolOfVeg(){
        try {
            Sheet sheet = this.workbook_veg.getSheetAt(0);
            int startRowNum = 3;    //"牛乳(店頭売り、紙容器入り)"の行
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行番号
            int minVolColNum = 3;  //"1食分の目安量"の列
            double[] minimumVolOfVeg = new double[rowSize + 1 - 3]; //3行分の余計な部分を除外したサイズ
            for(int rowIndex = startRowNum; rowIndex <= rowSize; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                Cell cell = row.getCell(minVolColNum);
                minimumVolOfVeg[rowIndex - startRowNum] = cell.getNumericCellValue();
            }
            //System.out.println(Arrays.toString(minimumVolOfVeg));
            return minimumVolOfVeg;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
