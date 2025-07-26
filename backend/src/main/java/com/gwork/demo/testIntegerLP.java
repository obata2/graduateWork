package com.gwork.demo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import java.util.HashMap;

import com.gwork.demo.Service.DataAdjusterService;
import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

import com.google.ortools.init.OrToolsVersion;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class testIntegerLP {

  static {
    String nativePath = "C:\\Users\\81809\\Desktop\\demo\\backend\\libs\\native";
    System.setProperty("java.library.path", nativePath);
    System.load(nativePath + "\\jniortools.dll");
  }

  public static void main(String args[]){

    double[][] stapleAndProtein = DataAdjusterService.stapleAndProtein;
    String[] spIng ={"うるち米(単一原料米,「コシヒカリ」)","ゆでうどん","スパゲッティ","中華麺","牛肉(かた)","牛肉(かたロース)","牛肉(リブロース)","牛肉(サーロイン)","牛肉(ばら)","牛肉(もも)","牛肉(そともも)","牛肉(ランプ)","牛肉(ヒレ)","豚肉(かた)","豚肉(かたロース)","豚肉(ロース)","豚肉(ばら)","豚肉(もも)","豚肉(そともも)","豚肉(ヒレ)","鶏肉(手羽)","鶏肉(手羽さき)","鶏肉(手羽もと)","鶏肉(むね)","鶏肉(もも)","鶏肉(ささみ)","鶏肉(ひきにく)"};
    int[] staVolOfsAndP = DataAdjusterService.staVolOfsAndP;

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)

    System.out.println("Google OR-Tools version: " + OrToolsVersion.getVersionString());

    //解いてみる
    for(int stapleIndex=0; stapleIndex<4; stapleIndex++){
      for(int proteinIndex=4; proteinIndex<stapleAndProtein.length; proteinIndex++){
        System.out.println("-------------------------------------------------------------");
        System.out.println(spIng[stapleIndex] + " " + staVolOfsAndP[stapleIndex] + "g , " + spIng[proteinIndex] + " " + staVolOfsAndP[proteinIndex] + "g で計算");
        DataAdjusterService dataAdjusterService = new DataAdjusterService(stapleIndex, proteinIndex);
        Optional<int []> resultOpt = solveILP(dataAdjusterService);
        if(!resultOpt.isPresent()){   //計算不可ならばスキップ
          System.out.println(spIng[stapleIndex] + " , " + spIng[proteinIndex] + " -> 計算不可能です");
          return;
        }
        int[] result = resultOpt.get();
        System.out.println(formatResult(result));
        double[] realize = checkRealize(result, dataAdjusterService, stapleIndex, proteinIndex);
        
      }
      break;
    }
    System.out.println((System.currentTimeMillis() - startTime) + "ミリ秒で処理完了");
  }



  // --- ILPで解く ---
  public static Optional<int []> solveILP(DataAdjusterService dataAdjusterService) {
    double[] prices = DataAdjusterService.prices;
    double[][] vegetable = DataAdjusterService.vegetable;
    int[] staVolOfVeg = DataAdjusterService.staVolOfVeg;
    double[] modifiedTargets = dataAdjusterService.modifiedTargets;
    double[] fixedEnergyValue = dataAdjusterService.fixedEnergyValue;
    MPSolver solver = MPSolver.createSolver("CBC");
    if (solver == null) {
      System.err.println("Solverを生成できません。");
      return Optional.empty();
    }
    int vegNum = prices.length;
    int nutNum = modifiedTargets.length;
    //変数を定義する    x[]:食材iの数量     0~(1食分の目安量の2倍まで)
    MPVariable[] x = new MPVariable[vegNum];
    for (int i = 0; i < vegNum; i++) {
      x[i] = solver.makeIntVar(0.0, Double.POSITIVE_INFINITY, "x_" + i);
    }
    // 栄養素目標の制約    sum_i (vegetable[i][j] * x[i]) >= targets[j]
    for (int j = 0; j < nutNum; j++) {
      //栄養素jの目標値を設定
      MPConstraint constraint = solver.makeConstraint(modifiedTargets[j], Double.POSITIVE_INFINITY, "nutrient_" + j);
      for (int i = 0; i < vegNum; i++) {
        //食材iに含まれる量を係数とする
        constraint.setCoefficient(x[i], vegetable[i][j]);
      }
    }
    //カロリーバランスの制約
    MPConstraint proteinLower = solver.makeConstraint(-fixedEnergyValue[0], Double.POSITIVE_INFINITY, "protein_lower");
    MPConstraint proteinUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[1], "protein_upper");
    MPConstraint fatLower = solver.makeConstraint(-fixedEnergyValue[2], Double.POSITIVE_INFINITY, "fat_lower");
    MPConstraint fatUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[3], "fat_upper");
    MPConstraint carbohydrateLower = solver.makeConstraint(-fixedEnergyValue[4], Double.POSITIVE_INFINITY, "carbohydrate_lower");
    MPConstraint carbohydrateUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[5], "carbohydrate_upper");
    int lastRowNum = vegetable[0].length - 1; //"ci-0.65ti"の列番号  (0-indexed)
    for (int i = 0; i < vegNum; i++) {
      //食材iに含まれる量を係数とする
      proteinLower.setCoefficient(x[i], vegetable[i][lastRowNum - 5]);
      proteinUpper.setCoefficient(x[i], vegetable[i][lastRowNum - 4]);
      fatLower.setCoefficient(x[i], vegetable[i][lastRowNum - 3]);
      fatUpper.setCoefficient(x[i], vegetable[i][lastRowNum - 2]);
      carbohydrateLower.setCoefficient(x[i], vegetable[i][lastRowNum - 1]);
      carbohydrateUpper.setCoefficient(x[i], vegetable[i][lastRowNum]);
    }
    //System.out.println("制約の本数は = " + solver.numConstraints());
    //目的関数（価格の総和を最小化）
    MPObjective objective = solver.objective();
    for (int i = 0; i < vegNum; i++) {
      objective.setCoefficient(x[i], prices[i]);
    }
    objective.setMinimization();
    // 解く
    MPSolver.ResultStatus resultStatus = solver.solve();
    //System.out.println(solver.wallTime() + "ミリ秒で解かれました");
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
      int[] solution = new int[vegNum];
      for (int i = 0; i < vegNum; i++) {
        solution[i] = (int) Math.round(x[i].solutionValue());
      }
      System.out.println("合計価格は " + objective.value() + "円");
      System.out.println("計算結果は" + Arrays.toString(solution));
      return Optional.of(solution);
    } else {
      System.out.println("optimal solution が見つけられませんでした");
      return Optional.empty();
    }
  }


  // --- 実現値を確認する --- 
  public static double[] checkRealize(int[] result, DataAdjusterService dataAdjusterService, int stapleIndex, int proteinIndex){
    double[] modifiedTargets = dataAdjusterService.modifiedTargets;
    double[][] stapleAndProtein = DataAdjusterService.stapleAndProtein;
    int[] staVolOfsAndP = DataAdjusterService.staVolOfsAndP;
    double[][] vegetable = DataAdjusterService.vegetable;
    double[] realize = new double[modifiedTargets.length];
    int pCalColNum = (stapleAndProtein[0].length - 1) - 9;  //"タンパク質のエネルギー"の列番号
    int fCalColNum = pCalColNum + 1;
    int cCalColNum = pCalColNum + 2;
    int tCalColNum = pCalColNum + 3;
    //主食・肉類のカロリーと
    double totalkcal = stapleAndProtein[stapleIndex][tCalColNum] * (staVolOfsAndP[stapleIndex] / 100) + stapleAndProtein[proteinIndex][tCalColNum] * (staVolOfsAndP[proteinIndex] / 100);
    double pkcal = stapleAndProtein[stapleIndex][pCalColNum] * (staVolOfsAndP[stapleIndex] / 100) + stapleAndProtein[proteinIndex][pCalColNum] * (staVolOfsAndP[proteinIndex] / 100);
    double fkcal = stapleAndProtein[stapleIndex][fCalColNum] * (staVolOfsAndP[stapleIndex] / 100) + stapleAndProtein[proteinIndex][fCalColNum] * (staVolOfsAndP[proteinIndex] / 100);
    double ckcal = stapleAndProtein[stapleIndex][cCalColNum] * (staVolOfsAndP[stapleIndex] / 100) + stapleAndProtein[proteinIndex][cCalColNum] * (staVolOfsAndP[proteinIndex] / 100);
    //栄養素を足していく
    for(int k=0; k<modifiedTargets.length; k++){
      realize[k] += stapleAndProtein[stapleIndex][k] * (staVolOfsAndP[stapleIndex] / 100) + stapleAndProtein[proteinIndex][k] * (staVolOfsAndP[proteinIndex] / 100);
    }
    //野菜類の
    for(int m=0; m<vegetable.length; m++){
      //カロリーを足して
      totalkcal +=  vegetable[m][tCalColNum] * result[m];
      pkcal += vegetable[m][pCalColNum] * result[m];
      fkcal += vegetable[m][fCalColNum] * result[m];
      ckcal += vegetable[m][cCalColNum] * result[m];
      //栄養素を足す
      for(int n=0; n<modifiedTargets.length; n++){
        realize[n] += vegetable[m][n] * result[m];
      }
    }
    System.out.print("総カロリー : " + totalkcal + " (p:" + pkcal + " , f:" + fkcal + " , c:" + ckcal);
    System.out.println(" -> " + String.format("%.1f", pkcal / totalkcal * 100) + "% : " + String.format("%.1f", fkcal / totalkcal * 100) + "% : " + String.format("%.1f", ckcal / totalkcal * 100) + "%");
    System.out.println("実現された栄養素：" + formatRealize(realize));
    return realize;
  }


  // --- 合計価格の計算 --- 
  public static double getTotalPrice(double[] result){
    double[] prices = DataAdjusterService.prices;
    double totalPrice = 0;
    for(int i=0; i<result.length; i++){
      totalPrice += result[i] * prices[i];
    }
    return totalPrice;
  }


  // --- resultを分かりやすく表示 --- 
  private static Map<String, String> formatResult(int[] result){
    String[] vegIng = DataAdjusterService.vegIng;
    int[] unitQuantity = DataAdjusterService.unitQuantity;
    LinkedHashMap<String, String> formatResult = new LinkedHashMap<>();
    for(int i=0; i<result.length; i++){
      if(result[i] != 0){
        formatResult.put(vegIng[i], (result[i] * unitQuantity[i]) + "g");
      }
    }
    return formatResult;
  }


  // --- realizeを分かりやすく表示 --- 
  private static Map<String, Double> formatRealize(double[] realize){
    String[] nutrients = {"たんぱく質","食物繊維総量","カリウム","カルシウム","マグネシウム","鉄","亜鉛","ビタミンA","ビタミンD","ビタミンB1","ビタミンB2","ビタミンB6","葉酸","ビタミンC"};
    LinkedHashMap<String, Double> formatRealize = new LinkedHashMap<>();
    for(int i=0; i<realize.length; i++){
      if(realize[i] != 0){
        formatRealize.put(nutrients[i], realize[i]);
      }
    }
    return formatRealize;
  }
}