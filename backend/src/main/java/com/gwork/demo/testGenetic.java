package com.gwork.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Comparator;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

public class testGenetic {
  
  // --- パラメータ、使用する変数の設定 ---
  static final int POPULATION_SIZE = 10;  //個体群のサイズ
  static final int MAX_GENERATIONS = 100;  //世代数の上限
  static final double CROSSOVER_RATE = 0.8;  //交叉される確率
  static final double MUTATION_RATE = 0.1; //突然変異の確率
  static final int TOURNAMENT_SIZE = 3;  //トーナメントサイズ

  static JsonProcesserService jsonProcesserService = new JsonProcesserService();
  static NutrientService nutrientService = new NutrientService();
  static double[][] stapleAndProtein = nutrientService.getStapleAndProtein(); //主食・肉の栄養テーブル
  static double[][] vegetable = nutrientService.getVegetable(); //野菜類の栄養テーブル
  static double[] prices = setPrices(nutrientService.getUnitPrice(), jsonProcesserService.getIngAndPri()); //100gあたりの野菜類の価格情報
  static double[] minVolOfVeg = nutrientService.getMinimumVolOfVeg(); //野菜類の1食分の最低量
  static double[] staVolOfsAndP = nutrientService.getStaVolOfsAndP(); //主食・肉類の1食分の目安量
  static double[] targets = nutrientService.getTargets(); //目標値のテーブル
  
  // --- メインループ ---
  public static void main(String args[]){
    List<Individual> population = initializePopulation(); //親世代：初期集団
    Individual best = null;
    // *** 選択・交叉・突然変異のループ ***
    /*
    for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
      List<Individual> newPopulation = new ArrayList<>(); //子世代の集団
      while (newPopulation.size() < POPULATION_SIZE) {
        Individual parent1 = tournamentSelect(population);  //選択
        Individual parent2 = tournamentSelect(population);
        Individual child;
        if (Math.random() < CROSSOVER_RATE) { //交叉   <-   親個体2つから子個体1つになっていることに注意、交叉の方法次第で変える
          child = crossover(parent1, parent2);
        } else {
          child = parent1.copy();
        }
        mutate(child);  //突然変異
        newPopulation.add(child);
      }
      population = newPopulation; //子世代をそっくりそのまま親世代として、次のループへ
      best = population.stream().max(Comparator.comparing(ind -> ind.fitness)).get(); //子世代の中で最も適合度の高いもの
      System.out.println("Generation " + generation + ": Fitness = " + best.fitness);
    }*/
  }

  // --- 個体(オブジェクト)に関する定義 ---
  static class Individual {
    double[] genes; //染色体：[食材iの数量]
    double fitness; //適合度スコア
    Individual(double[] genes) {
        this.genes = genes;
        evaluateFitness(this.genes);
    }
    // *** 自身のコピーとなる個体を返す ***
    Individual copy() {
      return new Individual(genes.clone());
    }
  }


  // --- 初期集団の生成 ---           <-   価格情報の無いやつ(99999円のアレ)は、そもそも0にすべきか？
  static List<Individual> initializePopulation() {
    List<Individual> population = new ArrayList<>();
    for (int i = 0; i < POPULATION_SIZE; i++) {
      double[] genes = new double[prices.length];
      for (int j = 0; j < genes.length; j++) {
        if(j == 4 || j == 34 || j == 36){ //鶏卵・納豆・豆腐についての特別ルール
          int minMultiplier = 0; // 0単位から
          int maxMultiplier = 3; // 3単位まで
          int multiplier = minMultiplier + (int)(Math.random() * (maxMultiplier - minMultiplier + 1));
          genes[j] = minVolOfVeg[j] * multiplier;
          continue;
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


  // --- 選択（トーナメント方式） ---
  static Individual tournamentSelect(List<Individual> population) {
    List<Individual> tournament = new ArrayList<>();
    for (int i = 0; i < TOURNAMENT_SIZE; i++) {
      int index = (int)(Math.random() * population.size());
      tournament.add(population.get(index));
    }
    return tournament.stream().max(Comparator.comparing(ind -> ind.fitness)).get();
  }


  // --- 交叉（1点交叉） ---
  static Individual crossover(Individual parent1, Individual parent2) {
    double[] childGenes = new double[parent1.genes.length];
    int point = (int)(Math.random() * parent1.genes.length);
    for (int i = 0; i < parent1.genes.length; i++) {
      childGenes[i] = (i < point) ? parent1.genes[i] : parent2.genes[i];
    }
    return new Individual(childGenes);
  }


  // --- 突然変異 ---
  static void mutate(Individual individual) {
    for (int i = 0; i < individual.genes.length; i++) {
      if (Math.random() < MUTATION_RATE) {
        //突然変異の処理(卵・豆腐・納豆と、それ以外の2パターン)
      }
    }
    //individual.fitness = individual.evaluateFitness();
  }


  // --- 適合度の算出 ---
  static double evaluateFitness(double[] genes) {
    double totalPrice = calcTotaPrice(genes);
    double[] nutrients = calcNutrients(genes);
    double[] calories = calcCalories(genes);
    double fitness = 0; //スコア
    // *** 栄養に関してのスコア ***

    // *** カロリーに関してのスコア ***
    // *** 価格に関してのスコア ***
    return fitness;
  }


  // --- 栄養素の合計値を計算 ---
  private static double[] calcNutrients(double[] genes){
    double[] nutrients = new double[targets.length];
    for(int i=0; i<genes.length; i++){
      for(int j=0; j<targets.length; j++){
        nutrients[j] += genes[i] * vegetable[i][j];
      }
    }
    //System.out.println("栄養素の合計" + Arrays.toString(nutrients));
    return nutrients;
  }
  // --- カロリーを計算 ---
  private static double[] calcCalories(double[] genes){
    double[] calories = new double[4];
    for(int i=0; i<genes.length; i++){
      for(int j=21; j<=24; j++){  //"脂質のエネルギー"～"総エネルギー"
        calories[j - 21] += vegetable[i][j];
      }
    }
    //System.out.println("カロリー情報" + Arrays.toString(calories));
    return calories;
  }
  // --- 合計価格を計算 ---
  private static double calcTotaPrice(double[] genes){
    double totalPrice = 0;
    for(int i=0; i<genes.length; i++){
      totalPrice += genes[i] * ( prices[i] / 100);
    }
    //System.out.println("合計価格" + totalPrice);
    return totalPrice;
  }


  // --- 100g単位に修正した価格をarrayとして持つ ---
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
