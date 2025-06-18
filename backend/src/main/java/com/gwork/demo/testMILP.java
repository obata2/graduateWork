package com.gwork.demo;

import java.util.Map;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

public class testMILP {

  public static void main(String args[]){
    JsonProcesserService jsonProcesserService = new JsonProcesserService();
    NutrientService nutrientService = new NutrientService();

    double[][] stapleAndProtein = nutrientService.getStapleAndProtein(); //主食・肉の栄養テーブル
    double[][] vegetable = transpose(nutrientService.getVegetable()); //野菜類の栄養テーブル <- (constarintsに合わせるため、ここで転置)
    double[] prices = setPrices(nutrientService.getUnitPrice(), jsonProcesserService.getIngAndPri()); //100gあたりの野菜類の価格情報
    double[] minVolOfVeg = nutrientService.getMinimumVolOfVeg();
  }






  //二次元配列を転置して返す
  public static double[][] transpose(double[][] nutrients){
    int row = nutrients.length;
    int col = nutrients[0].length;
    double[][] transposed = new double[col][row];
    //元の行列の縦を走査し、transposedの横を埋めていく
    for(int i=0; i<col; i++){
      for(int j=0; j<row; j++){
        transposed[i][j] = nutrients[j][i];
      }
    }
    return transposed;
  }


  //価格をarrayとして持つ
  public static double[] setPrices(Map<String, Double> unitPrice, Map<String, Integer> ingAndPri){
    for(String key : unitPrice.keySet()){
      if(ingAndPri.containsKey(key)){
        unitPrice.put(key, ingAndPri.get(key) / unitPrice.get(key) * 100);
      }
    }
    double[] prices = new double[unitPrice.size()];
    int i=0;
    for(double value : unitPrice.values()){
      prices[i] = value;
      i++;
    }
    return prices;
  }
}