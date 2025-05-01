package com.gwork.demo.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class NutrientService {
    private ArrayList<String> ingredients = new ArrayList<>();
    private ArrayList<ArrayList<Double>> nutrientsList = new ArrayList<>();
    private ArrayList<Double> targetsList = new ArrayList<>();

    //コンストラクタで3つの配列を用意する
    public NutrientService(){
        String filePath = "C:\\Users\\81809\\Desktop\\demo\\食品標準成分表示_生野菜.xlsx";
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0); //最初のシートを取得
            int lastRowNum = sheet.getLastRowNum(); // データが入力されている最後の行番号を取得
            //10行目取得    (excelの10行目(1-indexed)←→コードで9行を指定(0-indexed))
            for(int rowIndex=9; rowIndex<lastRowNum - 2 ; rowIndex++){
                Row row = sheet.getRow(rowIndex);
                nutrientsList.add(new ArrayList<>());
                for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    //0列目のデータ→strとしてingredientsに格納   1列目の廃棄率は無視     それ以降の列のデータ→intとしてnutrientsListに格納(数値以外の可能性もあり)
                    if(colIndex == 0){
                        ingredients.add(cell.getStringCellValue());
                        continue;
                    }else if(colIndex == 1){
                        continue;
                    }
                    CellType type = cell.getCellType();
                    try {
                        switch (type) {
                            case NUMERIC:
                                nutrientsList.get(rowIndex - 9).add((double) cell.getNumericCellValue());
                                break;
                            case STRING:
                                String raw = cell.getStringCellValue().replaceAll("[^0-9.-]", ""); // 数字と小数点以外を除去
                                if(raw.isEmpty()){
                                    nutrientsList.get(rowIndex - 9).add(0.0);
                                }else{
                                    nutrientsList.get(rowIndex - 9).add(Double.parseDouble(raw));
                                }
                                break;
                            default:
                            nutrientsList.get(rowIndex - 9).add(0.0);
                        }
                    }catch(Exception e){
                        nutrientsList.get(rowIndex - 9).add(0.0);
                    }
                }
            }
            //摂取目標の部分を取得
            Row row = sheet.getRow(lastRowNum);
            for (int colIndex = 2; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                targetsList.add((double) cell.getNumericCellValue());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //ingredientsを返す
    public ArrayList<String> getIngredients(){
        return ingredients;
    }

    //nutrientsを、二次元配列に変換して返す         ←     そもそもデータサイズが固定なのだから、最初からArrayで持っておけば良いのでは？
    public double[][] getNutrients(){
        double[][] nutrientsArray = new double[nutrientsList.size()][nutrientsList.get(0).size()];
        for(int i=0; i<nutrientsList.size(); i++){
            for(int j=0; j<nutrientsList.get(0).size(); j++){
                nutrientsArray[i][j] = nutrientsList.get(i).get(j);
            }
        }
        return nutrientsArray;
    }

    //targetsを、配列に変換して返す
    public double[] getTargets(){
        double[] targetsArray = new double[targetsList.size()];
        for(int i=0; i<targetsList.size(); i++){
            targetsArray[i] = targetsList.get(i);
        }
        return targetsArray;
    }

}
