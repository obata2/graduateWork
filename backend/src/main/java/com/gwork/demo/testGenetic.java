package com.gwork.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

public class testGenetic {
  
  // --- パラメータ、使用する変数の設定 ---
    static final int POPULATION_SIZE = 100;  //個体群のサイズ
    static final int MAX_GENERATIONS = 100;  //世代数の上限
    static final double CROSSOVER_RATE = 0.8;  //交叉される確率
    static final double MUTATION_RATE = 0.1; //突然変異の確率
    static final int TOURNAMENT_SIZE = 3;  //トーナメントサイズ

    static JsonProcesserService jsonProcesserService = new JsonProcesserService();
    static NutrientService nutrientService = new NutrientService();
    static double[][] stapleAndProtein = nutrientService.getStapleAndProtein(); //主食・肉の栄養テーブル
    static double[][] vegetable = nutrientService.getVegetable(); //野菜類の栄養テーブル
    static double[] prices = setPrices(nutrientService.getUnitPrice(), jsonProcesserService.getIngAndPri()); //100gあたりの野菜類の価格情報
    static double[] minVolOfVeg = nutrientService.getMinimumVolOfVeg(); //1食分の最低量
  
  // --- メインループ ---
  public static void main(String args[]){
    

    
  }

  // --- 個体(オブジェクト)に関する定義 ---
  static class Individual {
    double[] genes; //染色体：[食材iの数量]
    double fitness; //適合度スコア
    Individual(double[] genes) {
        this.genes = genes;
        this.fitness = evaluateFitness();
    }
    // *** 適合度の算出 ***
    double evaluateFitness() {
        return 0.0; // 実装する
    }
    // *** 自身のコピーとなる個体を返す ***
    Individual copy() {
        return new Individual(genes.clone());
    }
  }


  // --- 初期集団の生成 ---
  static List<Individual> initializePopulation() {
    List<Individual> population = new ArrayList<>();
    for (int i = 0; i < POPULATION_SIZE; i++) {
      double[] genes = new double[prices.length];
      for (int j = 0; j < genes.length; j++) {
        if(i == 4 || i == 34 || i == 36){ //鶏卵・納豆・豆腐についての特別ルール
          int minMultiplier = 0; // 0単位から
          int maxMultiplier = 5; // 5単位まで
          int multiplier = minMultiplier + (int)(Math.random() * (maxMultiplier - minMultiplier + 1));
          genes[j] = minVolOfVeg[j] * multiplier;
        }
        if(Math.random() < 0.2){
          genes[j] = 0; //20%の確率で0グラム
        }else{
          double min = minVolOfVeg[j] * 0.5;
          double max = minVolOfVeg[j] * 2.0;
          double step = 10.0;
          int minStep = (int)(min / step);
          int maxStep = (int)(max / step);
          int chosenStep = minStep + (int)(Math.random() * (maxStep - minStep + 1));
          genes[j] = chosenStep * step; //80%の確率で、目安量の50~200%の間で10g刻みに選ぶ
        }
      }
      population.add(new Individual(genes));
    }
    return population;
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
