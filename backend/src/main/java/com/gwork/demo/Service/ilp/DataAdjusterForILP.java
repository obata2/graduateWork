package com.gwork.demo.Service.ilp;

import java.util.Map;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.gwork.demo.Service.estat.ProcessEstatService;
import com.gwork.demo.Service.nutrient.NutrientService;

//ILPのために必要な、(事前加工済みの)データを揃えるクラス
public class DataAdjusterForILP {

  //このクラス内限定の変数
  private static Map<String, Integer> specific = Map.of(      //excel上の目安量よりも、ここで書いたものの方がが優先される("個数"を単位としたい物たち)
    "牛乳(店頭売り,紙容器)", 100,
    "鶏卵", 60,
    "豆腐", 100,
    "納豆", 40
  );
  
  private static double[][][] nutrientTable = NutrientService.nutrientTable;
  private static double[][] standardQty = NutrientService.standardQty;
  private static double[][] priceUnitQtyForILP = NutrientService.priceUnitQtyForILP;
  private static String[][] id = NutrientService.id; 
  private static String[][] ingName = NutrientService.name;
  private static ProcessEstatService processEstatService = new ProcessEstatService();
  private static Map<String, Integer> priceLatest = processEstatService.priceLatest;
  
  //[0]→主食・肉類      [1]→野菜類
  public static int[] vegUnitQuantity;                  //計算の単位となる野菜類の重量(変数が1増減するときの変化する重量)
  public static double[][][] adjustedNutrientTable = new double[2][][];     //計算の単位となる重量で調整した栄養テーブル
  public static double[][] adjustedStandardQty = new double[2][];         //        〃                    1食分の目安量
  public static double[][] adjustedPrices = new double[2][];              //        〃                    価格
  public static String[] nutrientsName = {"たんぱく質","食物繊維総量","カリウム","カルシウム","マグネシウム","鉄","亜鉛","ビタミンA","ビタミンD","ビタミンB1","ビタミンB2","ビタミンB6","葉酸","ビタミンC"};
  public static double[] originalTargets = NutrientService.targets;

  static {
    vegUnitQuantity = getUnitQuantity(ingName[1], specific);
    adjustedStandardQty[0] = standardQty[0];
    adjustedStandardQty[1] = adjustStdQtyOfVeg(vegUnitQuantity, standardQty[1], ingName[1], specific);
    adjustedNutrientTable[0] = adjustSAndPNutTable(nutrientTable[0], standardQty[0]);
    adjustedNutrientTable[1] = adjustVegNutrients(nutrientTable[1], vegUnitQuantity);
    adjustedPrices[0] = adjustPricesOfSAndP();
    adjustedPrices[1] = adjustPricesOfVeg();
  }

  //状態に依存する変数  →  i,jを引数とするコンストラクタで、状態を保存する
  public double[] adjustedTargets;
  public double[] fixedEnergyValue;
  public double fixedPrice;


  // --- コンストラクタ ---
  public DataAdjusterForILP (int stapleIndex, int proteinIndex){
    this.adjustedTargets = adjustTargets(stapleIndex, proteinIndex);
    this.fixedEnergyValue = fixEnergy(stapleIndex, proteinIndex);
    this.fixedPrice = fixPrice(stapleIndex, proteinIndex);
  }
  // --- 主食・肉類を所与とした栄養素目標値の補正 --- 
  private static double[] adjustTargets(int stapleIndex, int proteinIndex){
    double[] adjustedTargets = new double[originalTargets.length];
    for(int k=0; k<originalTargets.length; k++){
      adjustedTargets[k] = Math.max(0, (originalTargets[k] - nutrientTable[0][stapleIndex][k] - nutrientTable[0][proteinIndex][k]));   //1食分の目安量を使って、目標から固定値を引く
    }
    return adjustedTargets;
  }
  // --- 〃 エネルギーの固定値 --- 
  private static double[] fixEnergy(int stapleIndex, int proteinIndex){
    double[] fixedEnergyValue = new double[6];  //"pi-0.13ti" ～ "ci-0.65ti"まで
    int lastColNum = nutrientTable[0][stapleIndex].length - 1;
    for(int k=0; k<fixedEnergyValue.length; k++){
      fixedEnergyValue[k] = nutrientTable[0][stapleIndex][lastColNum - 5 + k] + nutrientTable[0][proteinIndex][lastColNum - 5 + k];
    }
    return fixedEnergyValue;
  }
  // --- 〃 価格の固定値 --- 
  private static double fixPrice(int stapleIndex, int proteinIndex){
    double fixedPrice = adjustedPrices[0][stapleIndex] + adjustedPrices[0][proteinIndex];
    return fixedPrice;
  }


  // --- 野菜類の計算単位となるグラム数を返す ---
  private static int[] getUnitQuantity(String[] vegName, Map<String, Integer> specific){
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
  private static double[][] adjustVegNutrients(double[][] vegNutTable, int[] unitQuantity){
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
  private static double[] adjustPricesOfVeg(){
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
  private static double[] adjustStdQtyOfVeg(int[] vegUnitQuantity, double[] stdQtyOfVeg, String[] vegName, Map<String, Integer> specific){
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
  private static double[] adjustPricesOfSAndP(){
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
  private static double[][] adjustSAndPNutTable(double[][] sAndPNutTable, double[] stdVolOfsAndP){
    double[][] adjustedSAndPNutTable = new double[sAndPNutTable.length][sAndPNutTable[0].length];
    for(int i=0; i<sAndPNutTable.length; i++){
      for(int j=0; j<sAndPNutTable[i].length; j++){
        adjustedSAndPNutTable[i][j] = sAndPNutTable[i][j] * stdVolOfsAndP[i] / 100;
      }
    }
    return adjustedSAndPNutTable;
  }
}