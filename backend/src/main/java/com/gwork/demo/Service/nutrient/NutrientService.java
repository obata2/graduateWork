package com.gwork.demo.Service.nutrient;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
public class NutrientService {

  private static Workbook workbook_sAndP;
  private static Workbook workbook_veg;
  private static Workbook workbook_targets;

  //[0]→主食・肉類      [1]→野菜類
  public static double[][] priceUnitQtyForILP = new double[2][];    // 価格統計の単位となる重量
  public static String[][] priceUnitQtyForView = new String[2][];
  public static String[][] name = new String[2][];
  public static double[][] standardQty = new double[2][];           // 1食分の標準的な重量
  public static double[][][] nutrientTable = new double[2][][];
  public static String[][] id = new String[2][];

  public static double[] targets;

  // priceLatestテーブルの初期保存に使うための変数
  //public static LinkedHashMap<String, String> idAndName = new LinkedHashMap<>();        //お肉の部位をある部位に代表させた、{食材名:id}
  //public static LinkedHashMap<String, String> idAndPriceUnitQty = new LinkedHashMap<>();   //価格が何g単位かを示す辞書

  static {
    readNutrientTable();
    processSAndPTable();
    processVegTable();
    processTargets();
    //setIdMap();
  }


  // --- ファイルを読み込む ---
  private static void readNutrientTable() {
    String filePath_stapleAndProtein = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_主食・肉類.xlsx";
    String filePath_vegetables = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_野菜類.xlsx";
    String filePath_targets = "C:\\Users\\81809\\Desktop\\demo\\backend\\食品標準成分表示_摂取目標値.xlsx";
    try {
      FileInputStream fis_sta = new FileInputStream(new File(filePath_stapleAndProtein));
      workbook_sAndP = new XSSFWorkbook(fis_sta);
      FileInputStream fis_veg = new FileInputStream(new File(filePath_vegetables));
      workbook_veg = new XSSFWorkbook(fis_veg);
      FileInputStream fis_targets = new FileInputStream(new File(filePath_targets));
      workbook_targets = new XSSFWorkbook(fis_targets);
    } catch (IOException e) {
      System.out.println("excelファイルの読み込みに失敗しました");
    }
  }

  // 以下、excelの行・列指定は基本的に0-indexedで行う
  // --- 主食・肉類のexcelファイルから、必要なデータを取り出す ---
  private static void processSAndPTable() {
    try {
      Sheet sheet = workbook_sAndP.getSheetAt(0);
      int startRowIndex = 3; // "うるち米(コシヒカリ)"の行から
      int lastRowIndex = sheet.getLastRowNum(); // "鶏肉(ひき肉)"の行まで
      int rowSize = (lastRowIndex + 1) - 3; // 3行分の余計な部分を除外したサイズ
      
      int priUnitILPColIndex = 1; // "価格の単位(計算用)"の列
      int priUnitViewColIndex = 2; // "価格の単位(表示用)"の列

      int idColIndex = 3; // "e-statでのid"の列

      int nameColIndex = 4; // "食品名"の列

      int stdQtyColIndex = 5; // "1食分の目安量"の列

      int startNutrientColIndex = 6; // "タンパク質"の列から
      int lastNutrientColIndex = sheet.getRow(startRowIndex).getLastCellNum() - 1; // "ci-0.65ti"の列まで (0-indexed)

      priceUnitQtyForILP[0] = new double[rowSize];
      priceUnitQtyForView[0] = new String[rowSize];
      id[0] = new String[rowSize];
      name[0] = new String[rowSize];
      standardQty[0] = new double[rowSize];
      nutrientTable[0] = new double[rowSize][(lastNutrientColIndex + 1) - 6]; // 3行分、6列分の栄養素データではない部分を除外したサイズ
      for (int i = startRowIndex; i <= lastRowIndex; i++) {
        Row row = sheet.getRow(i);
        // 価格が何グラム単位かを取り出す(計算用)
        priceUnitQtyForILP[0][i - startRowIndex] =  row.getCell(priUnitILPColIndex).getNumericCellValue();
        // 価格が何グラム単位かを取り出す(表示用)
        priceUnitQtyForView[0][i - startRowIndex] =  row.getCell(priUnitViewColIndex).getStringCellValue();
        // idを取り出す
        id[0][i - startRowIndex] = row.getCell(idColIndex).getStringCellValue();
        // 食材名を取り出す
        name[0][i - startRowIndex] = row.getCell(nameColIndex).getStringCellValue();
        // 一食分の目安量を取り出す
        standardQty[0][i - startRowIndex] = row.getCell(stdQtyColIndex).getNumericCellValue();
        // 栄養素部分を取り出す
        for (int j = startNutrientColIndex; j <= lastNutrientColIndex; j++) {
          Cell nutrientCell = row.getCell(j);
          nutrientTable[0][i - startRowIndex][j - startNutrientColIndex] = nutrientCell.getNumericCellValue();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --- 野菜類のexcelファイルから、必要なデータを取り出す ---
  private static void processVegTable() {
    try {
      Sheet sheet = workbook_veg.getSheetAt(0);
      int startRowIndex = 3; // "牛乳(店頭売り、紙容器入り)"の行から
      int lastRowIndex = sheet.getLastRowNum(); // "こんにゃく"の行まで (0-indexed)
      int rowSize = (lastRowIndex + 1) - 3; // 3行分の余計な部分を除外したサイズ

      int priUnitILPColIndex = 1; // "価格の単位(計算用)"の列
      int priUnitViewColIndex = 2; // "価格の単位(表示用)"の列

      int idColIndex = 3; // "e-statでのid"の列

      int nameColIndex = 4; // "食品名"の列

      int stdQtyColIndex = 5; // "1食分の目安量"の列

      int startNutrientColIndex = 6; // "タンパク質"の列から
      int lastNutrientColIndex = sheet.getRow(startRowIndex).getLastCellNum() - 1; // "ci-0.65ti"の列まで (0-indexed)

      priceUnitQtyForILP[1] = new double[rowSize];
      priceUnitQtyForView[1] = new String[rowSize];
      id[1] = new String[rowSize];
      name[1] = new String[rowSize];
      standardQty[1] = new double[rowSize];
      nutrientTable[1] = new double[rowSize][(lastNutrientColIndex + 1) - 6]; // 3行分、6列分の栄養素データではない部分を除外したサイズ
      for (int i = startRowIndex; i <= lastRowIndex; i++) {
        Row row = sheet.getRow(i);
        // 価格が何グラム単位かを取り出す(計算用)
        priceUnitQtyForILP[1][i - startRowIndex] =  row.getCell(priUnitILPColIndex).getNumericCellValue();
        // 価格が何グラム単位かを取り出す(表示用)
        priceUnitQtyForView[1][i - startRowIndex] =  row.getCell(priUnitViewColIndex).getStringCellValue();
        // idを取り出す
        id[1][i - startRowIndex] = row.getCell(idColIndex).getStringCellValue();
        // 食材名を取り出す
        name[1][i - startRowIndex] = row.getCell(nameColIndex).getStringCellValue();
        // 一食分の目安量を取り出す
        standardQty[1][i - startRowIndex] = row.getCell(stdQtyColIndex).getNumericCellValue();
        // 栄養素部分を取り出す
        for (int j = startNutrientColIndex; j <= lastNutrientColIndex; j++) {
          Cell nutrientCell = row.getCell(j);
          nutrientTable[1][i - startRowIndex][j - startNutrientColIndex] = nutrientCell.getNumericCellValue();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --- 目標値のデータを取り出す ---
  private static void processTargets() {
    try {
      Sheet sheet = workbook_targets.getSheetAt(0);
      targets = new double[14]; // 14種類の摂取目標値
      int startColIndex = 1; // "タンパク質"の列から
      int lastColIndex = 14; // ビタミンCまでの範囲 (0-indexed)
      Row row = sheet.getRow(sheet.getLastRowNum()); // 最終行から取得する
      for (int j = startColIndex; j <= lastColIndex; j++) {
        Cell cell = row.getCell(j);
        targets[j - startColIndex] = cell.getNumericCellValue();
      }
      // System.out.println("栄養素目標値：" + Arrays.toString(targets));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

/*
  private static void setIdMap () {
    ArrayList<String> used = new ArrayList<>();
    //主食・肉類と、野菜類の2パターン
    for(int i=0; i<id.length; i++){
      for(int j=0; j<id[i].length; j++){
        String id_str = id[i][j];
        String name_str = name[i][j];
        String priceUnit = priceUnitQtyForView[i][j];
        if(!used.contains(id_str)){
          idAndName.put(id_str, name_str);
          idAndPriceUnitQty.put(id_str, priceUnit);
          used.add(id_str);
        }
      }
    }
  }*/
}
