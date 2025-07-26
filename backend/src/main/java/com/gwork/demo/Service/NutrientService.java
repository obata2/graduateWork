package com.gwork.demo.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedHashMap;

@Service
public class NutrientService {

    Workbook workbook_sta;
    Workbook workbook_veg;
    Workbook workbook_targets;

    // --- コンストラクタでファイルを読み込む ---
    public NutrientService(){
        String filePath_stapleAndProtein = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_主食・肉類.xlsx";
        String filePath_vegetables = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_野菜類.xlsx";
        String filePath_targets = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_摂取目標値.xlsx";
        try{
            FileInputStream fis_sta = new FileInputStream(new File(filePath_stapleAndProtein));
            this.workbook_sta = new XSSFWorkbook(fis_sta);
            FileInputStream fis_veg = new FileInputStream(new File(filePath_vegetables));
            this.workbook_veg = new XSSFWorkbook(fis_veg);
            FileInputStream fis_targets = new FileInputStream(new File(filePath_targets));
            this.workbook_targets = new XSSFWorkbook(fis_targets);
            System.out.println("NUtrientServiceのインスタンスを生成、excelファイルを読み込みました");
        }catch(IOException e){
            System.out.println("NUtrientServiceのインスタンスを生成、excelファイルを読み込めません");
        }
    }

    //以下、excelの行・列指定は基本的に0-indexedで行う

    // ---主食・肉類の栄養テーブルを返す --- 
    public double[][] getStapleAndProtein(){
        //System.out.println("主食・肉類の栄養素テーブル：");
        try{
            Sheet sheet = this.workbook_sta.getSheetAt(0);
            int startRowNum = 3;    //"うるち米(コシヒカリ)"の行から
            int lastRowNum = sheet.getLastRowNum();    //"鶏肉(ひき肉)"の行まで  (0-indexed)
            int startColNum = 4;    //"タンパク質"の列から
            int lastColNum = sheet.getRow(startRowNum).getLastCellNum() - 1; //"ci-0.65ti"の列まで  (0-indexed)
            double[][] stapleAndProteinNutrients = new double[(lastRowNum + 1) - 3][(lastColNum + 1) - 4];    //3行分、4列分の栄養素データではない部分を除外したサイズ
            for(int i = startRowNum; i <= lastRowNum; i++){
                Row row = sheet.getRow(i);
                for (int j = startColNum; j <= lastColNum; j++) {
                    Cell cell = row.getCell(j);
                    stapleAndProteinNutrients[i - startRowNum][j - startColNum] = cell.getNumericCellValue();
                }
                //System.out.println(Arrays.toString(stapleAndProteinNutrients[i - startRowNum]));
            }
            return stapleAndProteinNutrients;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    // ---主食・肉類の1食分の目安量を返す ---
    public int[] getStaVolOfsAndP(){
        try{
            Sheet sheet = this.workbook_sta.getSheetAt(0);
            int startRowNum = 3;    //"うるち米(コシヒカリ)"の行から
            int lastRowNum = sheet.getLastRowNum();    //"鶏肉(ひき肉)"の行まで  (0-indexed)
            int colNum = 3;    //"1食分の目安量"の列
            int[] staVolOfsAndP = new int[(lastRowNum + 1) - 3];    //3行分の余計な部分を除外したサイズ
            for(int i = startRowNum; i <= lastRowNum; i++){
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(colNum);
                staVolOfsAndP[i - startRowNum] = (int) cell.getNumericCellValue();
            }
            //System.out.println("主食・肉類1食分の目安量：" + Arrays.toString(staVolOfsAndP));
            return staVolOfsAndP;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    // --- 目標値のテーブルを返す --- 
    public double[] getTargets(){
        try {
            Sheet sheet = this.workbook_targets.getSheetAt(0);
            double[] targets = new double[14];  //14種類の摂取目標値
            int startColNum = 1;    //"タンパク質"の列から
            int lastColNum = 14;   //ビタミンCまでの範囲 (0-indexed)
            Row row = sheet.getRow(sheet.getLastRowNum());  //最終行から取得する
            for (int j = startColNum; j <= lastColNum; j++) {
                Cell cell = row.getCell(j);
                targets[j - startColNum] = cell.getNumericCellValue();
            }
            //System.out.println("栄養素目標値：" + Arrays.toString(targets));
            return targets;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    // --- 野菜類の栄養テーブルを返す --- 
    public double[][] getVegetable(){
        //System.out.println("野菜類の栄養素テーブル：");
        try {
            Sheet sheet = this.workbook_veg.getSheetAt(0);
            int startRowNum = 3;    //"牛乳(店頭売り、紙容器入り)"の行から
            int lastRowNum = sheet.getLastRowNum();    //"こんにゃく"の行まで  (0-indexed)
            int startColNum = 4;    //"たんぱく質"の列から
            int lastColNum = sheet.getRow(startRowNum).getLastCellNum() - 1; //"ci-0.65ti"の列まで  (0-indexed)
            double[][] vegetable = new double[(lastRowNum + 1) - 3][(lastColNum + 1) - 4];    //3行分、4列分の栄養素データではない部分を除外したサイズ
            for(int i = startRowNum; i <= lastRowNum; i++){
                Row row = sheet.getRow(i);
                for (int j = startColNum; j <= lastColNum; j++) {
                    Cell cell = row.getCell(j);
                    vegetable [i - startRowNum][j - startColNum] = cell.getNumericCellValue();
                }
                //System.out.println(Arrays.toString(vegetable[i - startRowNum]));
            }
            return vegetable;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // --- 野菜類に関して、価格の単位グラム数の辞書を返す --- 
    public Map<String, Double> getPriceUnit(){
        Map<String, Double> priceUnit = new LinkedHashMap<>();
        try {
            Sheet sheet = this.workbook_veg.getSheetAt(0);
            int startRowNum = 3;    //"牛乳(店頭売り、紙容器入り)"の行から
            int lastRowNum = sheet.getLastRowNum();    //"こんにゃく"の行まで  (0-indexed)
            int nameColNum = 2; //"食品名"の列
            int priUniColNum = 1;  //"価格の単位"の列
            for(int i = startRowNum; i <= lastRowNum; i++){
                Row row = sheet.getRow(i);
                priceUnit.put(row.getCell(nameColNum).getStringCellValue(), row.getCell(priUniColNum).getNumericCellValue());
            }
            //System.out.println("価格の単位をまとめた辞書：" + priceUnit);
            return priceUnit;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // --- 野菜類の1食分の目安量を返す --- 
    public int[] getStaVolOfVeg(){
        try {
            Sheet sheet = this.workbook_veg.getSheetAt(0);
            int startRowNum = 3;    //"牛乳(店頭売り、紙容器入り)"の行から
            int lastRowNum = sheet.getLastRowNum();    //"こんにゃく"の行まで  (0-indexed)
            int staVolColNum = 3;  //"1食分の目安量"の列
            int[] staVolOfVeg = new int[(lastRowNum + 1) - 3]; //3行分の余計な部分を除外したサイズ
            for(int i = startRowNum; i <= lastRowNum; i++){
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(staVolColNum);
                staVolOfVeg[i - startRowNum] = (int) cell.getNumericCellValue();
            }
            //System.out.println("野菜類1食分の目安量：" + Arrays.toString(staVolOfVeg));
            return staVolOfVeg;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
