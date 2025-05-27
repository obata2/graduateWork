package com.gwork.demo.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class NutrientService {

    final String filePath_stapleAndProtein = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_主食・肉類.xlsx";
    final String filePath_vegetables = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_野菜類.xlsx";

    //1食分の目安量で補正しながら、主食・肉類の栄養テーブルを返す
    public double[][] getStapleAndProtein(){
        try (FileInputStream fis = new FileInputStream(new File(filePath_stapleAndProtein));
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(1); //シート2を取得
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行番号
            int colSize = sheet.getRow(3).getLastCellNum() - 1; //0-indexedに合わせた、データが最後に存在する行番号
            double[][] stapleAndProteinNutrients = new double[rowSize - 4][colSize - 3];
            for(int rowIndex = 3; rowIndex <= rowSize - 2; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                for (int colIndex = 4; colIndex <= colSize; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    double correc = row.getCell(2).getNumericCellValue() / 100;
                    stapleAndProteinNutrients[rowIndex - 3][colIndex - 4] = cell.getNumericCellValue() * correc;
                }
                //System.out.println(Arrays.toString(stapleAndProteinNutrients[rowIndex - 3]));
            }
            return stapleAndProteinNutrients;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    //目標値のテーブルを返す
    public double[] getTargets(){
        try (FileInputStream fis = new FileInputStream(new File(filePath_stapleAndProtein));
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(1); //シート2を取得
            double[] targets = new double[14];  //14種類の摂取目標値
            int colSize = 17;   //ビタミンCまでの範囲
            Row row = sheet.getRow(sheet.getLastRowNum());
            for (int colIndex = 4; colIndex <= colSize; colIndex++) {
                Cell cell = row.getCell(colIndex);
                targets[colIndex - 4] = cell.getNumericCellValue();
            }
            //System.out.println(Arrays.toString(targets));
            return targets;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    //野菜類の栄養テーブルを返す
    public double[][] getVegetable(){
        try (FileInputStream fis = new FileInputStream(new File(filePath_vegetables));
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(1); //シート2を取得
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行番号
            int colSize = sheet.getRow(3).getLastCellNum() - 1; //0-indexedに合わせた、データが最後に存在する行番号
            double[][] vegetable = new double[rowSize - 2][colSize - 3];
            for(int rowIndex = 3; rowIndex <= rowSize; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                for (int colIndex = 4; colIndex <= colSize; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    vegetable [rowIndex - 3][colIndex - 4] = cell.getNumericCellValue();
                }
                //System.out.println(Arrays.toString(vegetable[rowIndex - 3]));
            }
            return vegetable;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    //野菜類に関して、価格を補正するための辞書を返す
    public Map<String, Double> getUnitPrice(){
        Map<String, Double> unitPrice = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath_vegetables));
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(1); //シート2を取得
            int rowSize = sheet.getLastRowNum();    //0-indexedの、データが最後に存在する行番号
            for(int rowIndex = 3; rowIndex <= rowSize; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                unitPrice.put(row.getCell(3).getStringCellValue(), row.getCell(1).getNumericCellValue());
            }
            //System.out.println(unitPrice);
            return unitPrice;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
