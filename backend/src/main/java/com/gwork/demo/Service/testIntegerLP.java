package com.gwork.demo.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import com.gwork.demo.util.DataAdjusterForILP;

import com.google.ortools.init.OrToolsVersion;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class testIntegerLP {

  
  static {
    /*
    String nativePath = "C:\\Users\\81809\\Desktop\\demo\\backend\\libs\\native";
    System.setProperty("java.library.path", nativePath);
    System.load(nativePath + "\\jniortools.dll");
    */
    String nativePath = "C:\\Users\\81809\\Desktop\\demo\\backend\\libs\\native\\jniortools.dll";
    System.load(nativePath);
    System.out.println("ロードできました");
  }

  public static void main(String args[]){

    double[][] stapleAndProtein = DataAdjusterForILP.sAndPNutTable;
    String[] spIng = DataAdjusterForILP.sAndPName;
    double[] staVolOfsAndP = DataAdjusterForILP.staVolOfsAndP;
    //List<CalcResultService> ;

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)

    //System.out.println("Google OR-Tools version: " + OrToolsVersion.getVersionString());

    
    //計算結果を得る
    for(int stapleIndex=0; stapleIndex<4; stapleIndex++){
      for(int proteinIndex=4; proteinIndex<stapleAndProtein.length; proteinIndex++){
        int totalPrice;
        int totalKcal;
        int[] pfcKcal;
        LinkedHashMap<String, String> ingredients;
        LinkedHashMap<String, Integer> nutrients;
        System.out.println("-------------------------------------------------------------");
        System.out.println(spIng[stapleIndex] + " " + staVolOfsAndP[stapleIndex] + "g , " + spIng[proteinIndex] + " " + staVolOfsAndP[proteinIndex] + "g で計算");
        DataAdjusterForILP dataAdjusterService = new DataAdjusterForILP(stapleIndex, proteinIndex);   //ここで栄養素目標・カロリーバランスの固定値で補正が入る
        Optional<int []> solutionOpt = solveILP(dataAdjusterService);   //解ベクトルしか返ってこない
        if(!solutionOpt.isPresent()){   //計算不可ならばスキップ
          System.out.println(spIng[stapleIndex] + " , " + spIng[proteinIndex] + " -> 計算不可能です");
          break;
        }
        int[] solution = solutionOpt.get();

        //以下、解ベクトルを利用して CalcResult を整えていく
        totalPrice = getTotalPrice(solution);
        ingredients = formatSolution(solution);
        System.out.println("合計金額：" + totalPrice + " , 計算結果：" + ingredients);
        double[] realizedNutrients = checkRealize(solution, dataAdjusterService, stapleIndex, proteinIndex);    //カロリーと栄養についてはこのメソッドでひとまとめに
      }
    }
      
    /*
    JsonProcesserService jsonProcesserService = new JsonProcesserService();
    DataAdjusterService dataAdjusterService = new DataAdjusterService(0,4);
    NutrientService nutrientService = new NutrientService();
    //System.out.println(jsonProcesserService.getIngAndPri());
    //System.out.println(nutrientService.getPriceUnit());
    //System.out.println(Arrays.toString(DataAdjusterService.prices));
    */
    System.out.println((System.currentTimeMillis() - startTime) + "ミリ秒で処理完了");
  }


  // --- ILPで解く ---
  public static Optional<int []> solveILP(DataAdjusterForILP dataAdjusterService) {
    final double[] prices = DataAdjusterForILP.prices;
    final double[][] vegetable = DataAdjusterForILP.vegNutTable;
    final double[] staVolOfVeg = DataAdjusterForILP.staVolOfVeg;
    //System.out.println(Arrays.toString(staVolOfVeg));
    final double[] modifiedTargets = dataAdjusterService.modifiedTargets;
    final double[] fixedEnergyValue = dataAdjusterService.fixedEnergyValue;
    MPSolver solver = MPSolver.createSolver("CBC");
    if (solver == null) {
      System.err.println("Solverを生成できません。");
      return Optional.empty();
    }
    int vegNum = prices.length;
    int nutNum = modifiedTargets.length;

    /*
    //変数を定義する    x[]:食材iの数量     0~(1食分の目安量の3倍まで)        ←    連続な値
    MPVariable[] x = new MPVariable[vegNum];
    for (int i = 0; i < vegNum; i++) {
      x[i] = solver.makeIntVar(0.0, staVolOfVeg[i] * 3, "x_" + i);
    }
      */

    //変数を定義する    x[]:食材iの数量     (0  または  1食分の目安量の0.5 ~ 3倍)        ←    非連続な値、バイナリ変数を用いて表現
    MPVariable[] x = new MPVariable[vegNum];        //主変数
    MPVariable[] y = new MPVariable[vegNum];        //補助バイナリ変数(0 or 1)
    for (int i = 0; i < vegNum; i++) {
      x[i] = solver.makeIntVar(0, staVolOfVeg[i] * 3, "x_" + i);
      y[i] = solver.makeIntVar(0, 1, "y_" + i);
      double minVol = staVolOfVeg[i] * 0.5;
      double maxVol = staVolOfVeg[i] * 3;
      // 制約1: x[i] >= minVol * y[i]     (x[i]は1食分の目安量の0.5倍より多い)
      MPConstraint c1 = solver.makeConstraint(0, Double.POSITIVE_INFINITY);
      c1.setCoefficient(x[i], 1);
      c1.setCoefficient(y[i], - minVol);
      // 制約2: x[i] <= maxVol * y[i]       (〃3倍より小さい)
      MPConstraint c2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0);
      c2.setCoefficient(x[i], 1);
      c2.setCoefficient(y[i], - maxVol);
    }

    // 栄養素目標の制約    sum_i (vegetable[i][j] * x[i]) >= targets[j]
    for (int j = 0; j < nutNum; j++) {
      //栄養素jの目標値を設定  (右辺)
      MPConstraint constraint = solver.makeConstraint(modifiedTargets[j], Double.POSITIVE_INFINITY, "nutrient_" + j);
      for (int i = 0; i < vegNum; i++) {
        //食材iに含まれる量を係数とする  (左辺)
        constraint.setCoefficient(x[i], vegetable[i][j]);
      }
    }

    //カロリーバランスの制約    sum_i (pi - 0.13ti) * x[i] >= - (P - 0.13T) : proteinLower の例
    MPConstraint proteinLower = solver.makeConstraint(-fixedEnergyValue[0], Double.POSITIVE_INFINITY, "protein_lower"); //(右辺)
    MPConstraint proteinUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[1], "protein_upper");
    MPConstraint fatLower = solver.makeConstraint(-fixedEnergyValue[2], Double.POSITIVE_INFINITY, "fat_lower");
    MPConstraint fatUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[3], "fat_upper");
    MPConstraint carbohydrateLower = solver.makeConstraint(-fixedEnergyValue[4], Double.POSITIVE_INFINITY, "carbohydrate_lower");
    MPConstraint carbohydrateUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[5], "carbohydrate_upper");
    int lastRowNum = vegetable[0].length - 1; //"ci-0.65ti"の列番号  (0-indexed)
    for (int i = 0; i < vegNum; i++) {
      //食材iに含まれる量を係数とする
      proteinLower.setCoefficient(x[i], vegetable[i][lastRowNum - 5]);  //(左辺)
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
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {    //解が見つかった場合
      int[] solution = new int[vegNum];
      for (int i = 0; i < vegNum; i++) {
        solution[i] = (int) Math.round(x[i].solutionValue());
      }
      System.out.println("合計価格は " + objective.value() + "円");
      System.out.println("計算結果は" + Arrays.toString(solution));
      return Optional.of(solution);
    } else {    //解が見つからなかった場合
      System.out.println("optimal solution が見つけられませんでした");
      return Optional.empty();
    }
  }


  // --- 実現値を確認する --- 
  public static double[] checkRealize(int[] result, DataAdjusterForILP dataAdjusterService, int stapleIndex, int proteinIndex){
    final double[] modifiedTargets = dataAdjusterService.modifiedTargets;
    final double[][] stapleAndProtein = DataAdjusterForILP.sAndPNutTable;
    final double[] staVolOfsAndP = DataAdjusterForILP.staVolOfsAndP;
    final double[][] vegetable = DataAdjusterForILP.vegNutTable;
    final double[] realize = new double[modifiedTargets.length];
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
  public static int getTotalPrice(int[] solution){
    final double[] prices = DataAdjusterForILP.prices;
    int totalPrice = 0;
    for(int i=0; i<solution.length; i++){
      totalPrice += (int) solution[i] * prices[i];
    }
    return totalPrice;
  }


  // --- solution を{材料名：グラム数}の辞書に変換 --- 
  private static LinkedHashMap<String, String> formatSolution(int[] solution){
    LinkedHashMap<String, String> formatSolution = new LinkedHashMap<>();
    final String[] vegIng = DataAdjusterForILP.vegName;
    final int[] unitQuantity = DataAdjusterForILP.unitQuantity;
    for(int i=0; i<solution.length; i++){
      if(solution[i] != 0){
        formatSolution.put(vegIng[i], (solution[i] * unitQuantity[i]) + "g");
      }
    }
    return formatSolution;
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