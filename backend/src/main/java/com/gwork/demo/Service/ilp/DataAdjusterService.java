package com.gwork.demo.Service.ilp;

import java.util.Map;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;

import com.gwork.demo.Service.estat.EstatService;
import com.gwork.demo.Service.nutrient.NutrientService;

import jakarta.annotation.PostConstruct;

//ILPのために必要な、(事前加工済みの)データを揃えるクラス
@Service
public class DataAdjusterService {

  private EstatService estatService;
  // コンストラクタインジェクション
  public DataAdjusterService(EstatService estatService) {
    this.estatService = estatService;
  }

  //このクラス内限定の変数
  private Map<String, Integer> specific = Map.of(      //excel上の目安量よりも、ここで書いたものの方がが優先される("個数"を単位としたい物たち)
    "牛乳(店頭売り,紙容器)", 100,
    "鶏卵", 60,
    "豆腐", 100,
    "納豆", 40
  );
  private double[][][] nutrientTable = NutrientService.nutrientTable;
  private double[][] standardQty = NutrientService.standardQty;
  private double[][] priceUnitQtyForILP = NutrientService.priceUnitQtyForILP;
  private String[][] id = NutrientService.id; 
  private String[][] ingName = NutrientService.name;
  private Map<String, Integer> priceLatest = new LinkedHashMap<>();
  
  //solveに渡す変数      ([0]→主食・肉類      [1]→野菜類)
  private int[] vegUnitQuantity;                  //計算の単位となる野菜類の重量(変数が1増減するときの変化する重量)
  private double[][][] adjustedNutrientTable = new double[2][][];     //計算の単位となる重量で調整した栄養テーブル
  private double[][] adjustedStandardQty = new double[2][];         //        〃                    1食分の目安量
  private double[][] adjustedPrices = new double[2][];              //        〃                    価格

  // ゲッター
  public int[] getVegUnitQuantity () {return vegUnitQuantity;}
  public double[][][] getAdjustedNutrientTable () {return adjustedNutrientTable;}
  public double[][] getAdjustedStandardQty () {return adjustedStandardQty;}
  public double[][] getAdjustedPrices () {return adjustedPrices;}
  

  @PostConstruct
  private void init() {
    priceLatest = estatService.getIdAndPriceMap();
    this.vegUnitQuantity = getUnitQuantity(ingName[1], specific);
    this.adjustedStandardQty[0] = standardQty[0];
    this.adjustedStandardQty[1] = adjustStdQtyOfVeg(vegUnitQuantity, standardQty[1], ingName[1], specific);
    this.adjustedNutrientTable[0] = adjustSAndPNutTable(nutrientTable[0], standardQty[0]);
    this.adjustedNutrientTable[1] = adjustVegNutrients(nutrientTable[1], vegUnitQuantity);
    this.adjustedPrices[0] = adjustPricesOfSAndP();
    this.adjustedPrices[1] = adjustPricesOfVeg();
  }

  // --- 野菜類の計算単位となるグラム数を返す ---
  private int[] getUnitQuantity(String[] vegName, Map<String, Integer> specific){
    int[] vegUnitQuantity = new int[vegName.length];
    int defaultQuantity = 10;      //指定が無い食材は10g単位とする
    for(int i=0; i<vegName.length; i++){
      if(specific.containsKey(vegName[i])){
        vegUnitQuantity[i] = specific.get(vegName[i]);
      }else{
        vegUnitQuantity[i] = defaultQuantity;
      }
    }
    return vegUnitQuantity;
  }

  // --- 単位重量で野菜類の栄養テーブルを修正 ---
  private double[][] adjustVegNutrients(double[][] vegNutTable, int[] unitQuantity){
    double[][] adjustedVegNutTable = new double[vegNutTable.length][vegNutTable[0].length];
    for(int i=0; i<vegNutTable.length; i++){
      for(int j=0; j<vegNutTable[i].length; j++){
        adjustedVegNutTable[i][j] = vegNutTable[i][j] * unitQuantity[i] / 100;
      }
      //System.out.println("vegNut：" + Arrays.toString(adjustedVegNutTable[i]));
    }
    return adjustedVegNutTable;
  }

  // --- 単位重量あたりの野菜類の価格をarrayとして持つ ---
  private double[] adjustPricesOfVeg(){
    double[] prices = new double[id[1].length];
    for(int i=0; i<prices.length; i++){
      if(!priceLatest.containsKey(id[1][i])){
        prices[i] = 99999;  //価格情報がないものは適当な値段を
      }else{
        double price = priceLatest.get(id[1][i]);
        price /= priceUnitQtyForILP[1][i];
        price *= vegUnitQuantity[i];
        prices[i] = price;
      }
    }
    return prices;
  }

  // --- 単位重量で野菜の1食分の目安量を修正(何グラム→何単位  にする) ---
  private double[] adjustStdQtyOfVeg(int[] vegUnitQuantity, double[] stdQtyOfVeg, String[] vegName, Map<String, Integer> specific){
    double[] adjustedStdVolOfVeg = new double[stdQtyOfVeg.length];
    for(int i=0; i<stdQtyOfVeg.length; i++){
      if(specific.containsKey(vegName[i])){
        adjustedStdVolOfVeg[i] = 1.0;   //指定のある食材は"1個"を計算の基準に
      }else{
        adjustedStdVolOfVeg[i] = stdQtyOfVeg[i] / vegUnitQuantity[i];
      }
    }
    return adjustedStdVolOfVeg;
  }


  // --- 1食分の目安量あたりの主食・肉類の価格をarrayとして持つ ---
  private double[] adjustPricesOfSAndP(){
    double[] prices = new double[id[0].length];
    for(int i=0; i<prices.length; i++){
      if(!priceLatest.containsKey(id[0][i])){
        prices[i] = 99999;  //価格情報がないものは適当な値段を
      }else{
        double price = priceLatest.get(id[0][i]);
        price /= priceUnitQtyForILP[0][i];
        price *= standardQty[0][i];
        prices[i] = price;
      }
    }
    return prices;
  }

  // --- 1食分の目安量で主食・肉類の栄養テーブルを修正 ---
  private double[][] adjustSAndPNutTable(double[][] sAndPNutTable, double[] stdVolOfsAndP){
    double[][] adjustedSAndPNutTable = new double[sAndPNutTable.length][sAndPNutTable[0].length];
    for(int i=0; i<sAndPNutTable.length; i++){
      for(int j=0; j<sAndPNutTable[i].length; j++){
        adjustedSAndPNutTable[i][j] = sAndPNutTable[i][j] * stdVolOfsAndP[i] / 100;
      }
    }
    return adjustedSAndPNutTable;
  }
}