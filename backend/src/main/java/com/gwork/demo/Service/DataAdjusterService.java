package com.gwork.demo.Service;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DataAdjusterService {

  //ILPに用いる変数
  public static double[][] stapleAndProtein;
  public static double[][] vegetable;
  public static int[] staVolOfsAndP;
  public static int[] staVolOfVeg;
  public static double[] prices;
  public static String[] vegIng = {"牛乳(店頭売り,紙容器入り)","チーズ(国産品)","チーズ(輸入品)","ヨーグルト","鶏卵","キャベツ","ほうれんそう","はくさい","ねぎ","レタス","もやし","ブロッコリー","アスパラガス","さつまいも","じゃがいも","さといも","だいこん","にんじん","ごぼう","たまねぎ","れんこん","ながいも","えだまめ","さやいんげん","かぼちゃ","きゅうり","なす","トマト","ピーマン","生しいたけ","えのきたけ","しめじ","わかめ","ひじき","豆腐","油揚げ","納豆","こんにゃく"};
  public static int[] unitQuantity;

  //このクラス内限定の変数
  private static JsonProcesserService jsonProcesserService = new JsonProcesserService();
  private static  NutrientService nutrientService = new NutrientService();
  private static Map<String, Integer> specific = Map.of(
      "牛乳(店頭売り,紙容器入り)", 100,
      "鶏卵", 60,
      "豆腐", 50,
      "納豆", 40
    );
  private static Map<String, Double> priceUnit = nutrientService.getPriceUnit();
  private static Map<String, Integer> ingAndPri = jsonProcesserService.getIngAndPri();
  private static double[] targets = nutrientService.getTargets();

  static {
    unitQuantity = getUnitQuantity(vegIng, specific);
    stapleAndProtein = nutrientService.getStapleAndProtein();
    vegetable = modifyVegNutrients(nutrientService.getVegetable(), unitQuantity);
    staVolOfsAndP = nutrientService.getStaVolOfsAndP();
    staVolOfVeg = nutrientService.getStaVolOfVeg();
    prices = setPrices(priceUnit, ingAndPri, unitQuantity);
  }


  //状態に依存する変数  →  i,jを引数とするコンストラクタで、状態を保存する
  public double[] modifiedTargets;
  public double[] fixedEnergyValue;


  // --- コンストラクタ ---
  public DataAdjusterService (int stapleIndex, int proteinIndex){
    this.modifiedTargets = modifyTargets(stapleIndex, proteinIndex);
    this.fixedEnergyValue = fixEnergy(stapleIndex, proteinIndex);
  }
  

  // --- 主食・肉類を所与とした栄養素目標値の補正 --- 
  public static double[] modifyTargets(int stapleIndex, int proteinIndex){
    double[] modifiedTargets = new double[targets.length];
    double sVolCoeff = staVolOfsAndP[stapleIndex] / 100.0;  //100gを1単位とした時の係数
    double pVolCoeff = staVolOfsAndP[proteinIndex] / 100.0;  //同様に
    for(int k=0; k<targets.length; k++){
      modifiedTargets[k] = Math.max(0, (targets[k] - stapleAndProtein[stapleIndex][k] * sVolCoeff - stapleAndProtein[proteinIndex][k] * pVolCoeff));   //1食分の目安量を使って固定値を引く
    }
    return modifiedTargets;
  }


  // --- 主食・肉類を所与としたエネルギーの固定値 --- 
  public static double[] fixEnergy(int stapleIndex, int proteinIndex){
    double[] fixedEnergyValue = new double[6];  //"pi-0.13ti" ～ "ci-0.65ti"まで
    int lastColNum = stapleAndProtein[stapleIndex].length - 1;
    double sVolCoeff = staVolOfsAndP[stapleIndex] / 100;  //100gを1単位とした時の係数
    double pVolCoeff = staVolOfsAndP[proteinIndex] / 100;  //同様に
    for(int k=0; k<fixedEnergyValue.length; k++){
      fixedEnergyValue[k] = stapleAndProtein[stapleIndex][lastColNum - 5 + k] * sVolCoeff + stapleAndProtein[proteinIndex][lastColNum - 5 + k] * pVolCoeff;
    }
    return fixedEnergyValue;
  }


  // --- 計算に用いる単位重量を返す ---
  public static int[] getUnitQuantity(String[] vegIng, Map<String, Integer> specific){
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
  public static double[] setPrices(Map<String, Double> priceUnit, Map<String, Integer> ingAndPri, int[] unitQuantity){
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


  // --- 単位重量で野菜類の栄養テーブルを修正 ---
  public static double[][] modifyVegNutrients(double[][] vegetable, int[] unitQuantity){
    for(int i=0; i<vegetable.length; i++){
      for(int j=0; j<vegetable[i].length; j++){
        vegetable[i][j] = vegetable[i][j] * unitQuantity[i] / 100;
      }
      //System.out.println(Arrays.toString(vegetable[i]));
    }
    return vegetable;
  }
}