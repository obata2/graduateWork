package com.gwork.demo.util;

import java.util.Map;
import java.util.Arrays;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

//ILPのために必要な、(事前加工済みの)データを揃えるクラス
public class DataAdjusterForILP {

  //このクラス内限定の変数
  private static JsonProcesserService jsonProcesserService = new JsonProcesserService();
  private static  NutrientService nutrientService = new NutrientService();
  private static Map<String, Integer> specific = Map.of(      //excel上の目安量よりも、ここで書いたものの方がが優先される
    "牛乳(店頭売り,紙容器入り)", 100,
    "鶏卵", 60,
    "豆腐", 100,
    "納豆", 40
  );
  private static Map<String, Double> priceUnit = nutrientService.getPriceUnit();
  private static Map<String, Integer> ingAndPri = jsonProcesserService.getIngAndPri();

  //SolveILPServiceで用いるstatic変数
  public static double[][] sAndPNutTable;
  public static double[][] vegNutTable;
  public static double[] staVolOfsAndP;
  public static double[] staVolOfVeg;
  public static double[] prices;
  public static String[] sAndPName ={"うるち米(単一原料米,「コシヒカリ」)","ゆでうどん","スパゲッティ","中華麺","牛肉(かた)","牛肉(かたロース)","牛肉(リブロース)","牛肉(サーロイン)","牛肉(ばら)","牛肉(もも)","牛肉(そともも)","牛肉(ランプ)","牛肉(ヒレ)","豚肉(かた)","豚肉(かたロース)","豚肉(ロース)","豚肉(ばら)","豚肉(もも)","豚肉(そともも)","豚肉(ヒレ)","鶏肉(手羽)","鶏肉(手羽さき)","鶏肉(手羽もと)","鶏肉(むね)","鶏肉(もも)","鶏肉(ささみ)","鶏肉(ひきにく)"};
  public static String[] vegName = {"牛乳(店頭売り,紙容器入り)","チーズ(国産品)","チーズ(輸入品)","ヨーグルト","鶏卵","キャベツ","ほうれんそう","はくさい","ねぎ","レタス","もやし","ブロッコリー","アスパラガス","さつまいも","じゃがいも","さといも","だいこん","にんじん","ごぼう","たまねぎ","れんこん","ながいも","えだまめ","さやいんげん","かぼちゃ","きゅうり","なす","トマト","ピーマン","生しいたけ","えのきたけ","しめじ","わかめ","ひじき","豆腐","油揚げ","納豆","こんにゃく"};
  public static String[] nutrientsName = {"たんぱく質","食物繊維総量","カリウム","カルシウム","マグネシウム","鉄","亜鉛","ビタミンA","ビタミンD","ビタミンB1","ビタミンB2","ビタミンB6","葉酸","ビタミンC"};
  public static int[] unitQuantity;
  public static double[] originalTargets = nutrientService.getTargets();

  static {
    unitQuantity = getUnitQuantity(vegName, specific);
    staVolOfsAndP = nutrientService.getStaVolOfsAndP();
    vegNutTable = modifyVegNutrients(nutrientService.getVegetable(), unitQuantity);
    sAndPNutTable = modifySAndPNutTable(nutrientService.getStapleAndProtein(), staVolOfsAndP);
    staVolOfVeg = getStaVolOfVeg(unitQuantity, nutrientService.getStaVolOfVeg(), vegName, specific);
    prices = setPrices(priceUnit, ingAndPri, unitQuantity);
  }

  //状態に依存する変数  →  i,jを引数とするコンストラクタで、状態を保存する
  public double[] adjustedTargets;
  public double[] fixedEnergyValue;


  // --- コンストラクタ ---
  public DataAdjusterForILP (int stapleIndex, int proteinIndex){
    this.adjustedTargets = adjustTargets(stapleIndex, proteinIndex);
    this.fixedEnergyValue = fixEnergy(stapleIndex, proteinIndex);
  }
  // --- 主食・肉類を所与とした栄養素目標値の補正 --- 
  private static double[] adjustTargets(int stapleIndex, int proteinIndex){
    double[] adjustedTargets = new double[originalTargets.length];
    for(int k=0; k<originalTargets.length; k++){
      adjustedTargets[k] = Math.max(0, (originalTargets[k] - sAndPNutTable[stapleIndex][k] - sAndPNutTable[proteinIndex][k]));   //1食分の目安量を使って、目標から固定値を引く
    }
    return adjustedTargets;
  }
  // --- 主食・肉類を所与としたエネルギーの固定値 --- 
  private static double[] fixEnergy(int stapleIndex, int proteinIndex){
    double[] fixedEnergyValue = new double[6];  //"pi-0.13ti" ～ "ci-0.65ti"まで
    int lastColNum = sAndPNutTable[stapleIndex].length - 1;
    for(int k=0; k<fixedEnergyValue.length; k++){
      fixedEnergyValue[k] = sAndPNutTable[stapleIndex][lastColNum - 5 + k] + sAndPNutTable[proteinIndex][lastColNum - 5 + k];
    }
    return fixedEnergyValue;
  }


  // --- 計算に用いる単位重量を返す ---
  private static int[] getUnitQuantity(String[] vegIng, Map<String, Integer> specific){
    int[] unitQuantity = new int[vegIng.length];
    int defaultNum = 10;      //指定が無い食材は10g単位とする
    for(int i=0; i<vegIng.length; i++){
      if(specific.containsKey(vegIng[i])){
        unitQuantity[i] = specific.get(vegIng[i]);
      }else{
        unitQuantity[i] = defaultNum;
      }
    }
    return unitQuantity;
  }


  // --- 単位重量あたりの価格をarrayとして持つ ---
  private static double[] setPrices(Map<String, Double> priceUnit, Map<String, Integer> ingAndPri, int[] unitQuantity){
    for(String key : priceUnit.keySet()){
      if(ingAndPri.containsKey(key)){
        priceUnit.put(key, ingAndPri.get(key) / priceUnit.get(key) * 100);  //ひとまずは100gあたりの価格
      }
    }
    double[] prices = new double[priceUnit.size()];
    int i=0;
    for(double value : priceUnit.values()){
      prices[i] = value / 100 * unitQuantity[i];  //単位重量あたりの価格に修正
      i++;
    }
    return prices;
  }


  // --- 1食分の目安量で主食・肉類の栄養テーブルを修正 ---
  private static double[][] modifySAndPNutTable(double[][] sAndPNutTable, double[] staVolOfsAndP){
    for(int i=0; i<sAndPNutTable.length; i++){
      for(int j=0; j<sAndPNutTable[i].length; j++){
        sAndPNutTable[i][j] = sAndPNutTable[i][j] * staVolOfsAndP[i] / 100;
      }
    }
    return sAndPNutTable;
  }


  // --- 単位重量で野菜類の栄養テーブルを修正 ---
  private static double[][] modifyVegNutrients(double[][] vegetable, int[] unitQuantity){
    for(int i=0; i<vegetable.length; i++){
      for(int j=0; j<vegetable[i].length; j++){
        vegetable[i][j] = vegetable[i][j] * unitQuantity[i] / 100;
      }
    }
    return vegetable;
  }


  // --- 単位重量で野菜の1食分の目安量を修正(何グラム→何単位  にする) ---
  private static double[] getStaVolOfVeg(int[] unitQuantity, double[] staVolOfVeg, String[] vegIng, Map<String, Integer> specific){
    double[] modifiedStaVolOfVeg = new double[staVolOfVeg.length];
    for(int i=0; i<staVolOfVeg.length; i++){
      if(specific.containsKey(vegIng[i])){
        modifiedStaVolOfVeg[i] = 1.0;   //指定のある食材は"1個"を計算の基準に
      }else{
        modifiedStaVolOfVeg[i] = staVolOfVeg[i] / unitQuantity[i];
      }
    }
    return modifiedStaVolOfVeg;
  }
}