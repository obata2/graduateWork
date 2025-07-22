package com.gwork.demo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

import com.google.ortools.Loader;

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
    JsonProcesserService jsonProcesserService = new JsonProcesserService();
    NutrientService nutrientService = new NutrientService();

    double[][] stapleAndProtein = nutrientService.getStapleAndProtein(); //主食・肉の栄養テーブル
    double[][] vegetable = nutrientService.getVegetable(); //野菜類の栄養テーブル <- (constarintsに合わせるため、ここで転置)
    double[] prices = setPrices(nutrientService.getPriceUnit(), jsonProcesserService.getIngAndPri()); //100gあたりの野菜類の価格情報
    double[] minVolOfVeg = nutrientService.getStaVolOfVeg();
    double[] staVolOfsAndP = nutrientService.getStaVolOfsAndP();
    String[] spIng = {"うるち米(単一原料米,「コシヒカリ」)","ゆでうどん","スパゲッティ","中華麺","牛肉(かた)","牛肉(かたロース)","牛肉(リブロース)","牛肉(サーロイン)","牛肉(ばら)","牛肉(もも)","牛肉(そともも)","牛肉(ランプ)","牛肉(ヒレ)","豚肉(かた)","豚肉(かたロース)","豚肉(ロース)","豚肉(ばら)","豚肉(もも)","豚肉(そともも)","豚肉(ヒレ)","鶏肉(手羽)","鶏肉(手羽さき)","鶏肉(手羽もと)","鶏肉(むね)","鶏肉(もも)","鶏肉(ささみ)","鶏肉(ひきにく)"};

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)


    System.out.println("Google OR-Tools version: " + OrToolsVersion.getVersionString());
    //解いてみる  (米×牛かた肉)
    double[] targets = modifyTargets(nutrientService.getTargets(), stapleAndProtein, staVolOfsAndP,  0, 4); //補正済みの目標値
    double[] fixedEnergyValue = fixEnergy(stapleAndProtein, staVolOfsAndP, 0, 4);  //主食・肉類を所与としたエネルギーの固定値
    Optional<int []> resultOpt = solveILP(prices, targets, vegetable, fixedEnergyValue);
    if(!resultOpt.isPresent()){   //計算不可ならばスキップ
      System.out.println(spIng[0] + " , " + spIng[4] + " -> 計算不可能です");
      return;
    }
    int[] result = resultOpt.get();
    double[] realize = checkRealize(targets, result, stapleAndProtein, staVolOfsAndP, vegetable, 0, 0);
    System.out.println("実現された栄養素：" + formatRealize(realize));
  }



  // --- ILPで解く ---
  public static Optional<int []> solveILP(double[] prices, double[] targets, double[][] vegetable, double[] fixedEnergyValue) {
    MPSolver solver = MPSolver.createSolver("CBC");
    if (solver == null) {
      System.err.println("Solverを生成できません。");
      return Optional.empty();
    }
    int vegNum = prices.length;
    int nutNum = targets.length;
    //変数を定義する    x[]:食材iの数量
    MPVariable[] x = new MPVariable[vegNum];
    for (int i = 0; i < vegNum; i++) {
      x[i] = solver.makeIntVar(0.0, Double.POSITIVE_INFINITY, "x_" + i);
    }
    System.out.println("変数の数は " + solver.numVariables()  + "、野菜類の数も " + vegNum);
    // 栄養素目標の制約    sum_i (vegetable[i][j] * x[i]) >= targets[j]
    for (int j = 0; j < nutNum; j++) {
      //栄養素jの目標値を設定
      MPConstraint constraint = solver.makeConstraint(targets[j], Double.POSITIVE_INFINITY, "nutrient_" + j);
      for (int i = 0; i < vegNum; i++) {
        //食材iに含まれる量を係数とする
        constraint.setCoefficient(x[i], vegetable[i][j]);
      }
    }
    /*
    //カロリーバランスの制約(後回し)
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
    */
     System.out.println("制約の本数は = " + solver.numConstraints());
    //目的関数（価格の総和を最小化）
    MPObjective objective = solver.objective();
    for (int i = 0; i < vegNum; i++) {
      objective.setCoefficient(x[i], prices[i]);
    }
    objective.setMinimization();
    // 解く
    MPSolver.ResultStatus resultStatus = solver.solve();
    System.out.println(solver.wallTime() + "ミリ秒で解かれました");
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
      int[] solution = new int[vegNum];
      for (int i = 0; i < vegNum; i++) {
        solution[i] = (int) Math.round(x[i].solutionValue());
      }
      System.out.println("合計価格は " + objective.value());
      System.out.println("計算結果は" + Arrays.toString(solution));
      return Optional.of(solution);
    } else {
      System.out.println("optiml solution が見つけられませんでした");
      return Optional.empty();
    }
  }


  // --- 価格をarrayとして持つ ---
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


   // --- 主食・肉類を所与とした栄養素目標値の補正 --- 
  public static double[] modifyTargets(double[] targets, double[][] stapleAndProtein, double[] staVolOfsAndP, int i, int j){
    for(int k=0; k<targets.length; k++){
      double sVolCoeff = staVolOfsAndP[i] / 100;  //100gを1単位とした時の係数
      double pVolCoeff = staVolOfsAndP[j] / 100;  //同様に
      targets[k] = Math.max(0, (targets[k] - stapleAndProtein[i][k] * sVolCoeff - stapleAndProtein[j][k] * pVolCoeff));
    }
    return targets;
  }


  // --- 主食・肉類を所与としたエネルギーの固定値 --- 
  public static double[] fixEnergy(double[][] stapleAndProtein, double[] staVolOfsAndP, int i, int j){
    double[] fixedEnergyValue = new double[6];  //"pi-0.13ti" ～ "ci-0.65ti"まで
    int lastColNum = stapleAndProtein[i].length - 1;
    double sVolCoeff = staVolOfsAndP[i] / 100;  //100gを1単位とした時の係数
    double pVolCoeff = staVolOfsAndP[j] / 100;  //同様に
    for(int k=0; k<fixedEnergyValue.length; k++){
      fixedEnergyValue[k] = stapleAndProtein[i][lastColNum - 5 + k] * sVolCoeff + stapleAndProtein[j][lastColNum - 5 + k] * pVolCoeff;
    }
    return fixedEnergyValue;
  }


  // --- 実現値を確認する --- 
  public static double[] checkRealize(double[] targets, int[] result, double[][] stapleAndProtein, double[] staVolOfsAndP, double[][] vegetable, int i, int j){    
    double[] realize = new double[targets.length];
    int pCalColNum = (stapleAndProtein[0].length - 1) - 9;  //"タンパク質のエネルギー"の列番号
    int fCalColNum = pCalColNum + 1;
    int cCalColNum = pCalColNum + 2;
    int tCalColNum = pCalColNum + 3;
    //主食・肉類のカロリーと
    double totalkcal = stapleAndProtein[i][tCalColNum] * (staVolOfsAndP[i] / 100) + stapleAndProtein[j][tCalColNum] * (staVolOfsAndP[j] / 100);
    double pkcal = stapleAndProtein[i][pCalColNum] * (staVolOfsAndP[i] / 100) + stapleAndProtein[j][pCalColNum] * (staVolOfsAndP[j] / 100);
    double fkcal = stapleAndProtein[i][fCalColNum] * (staVolOfsAndP[i] / 100) + stapleAndProtein[j][fCalColNum] * (staVolOfsAndP[j] / 100);
    double ckcal = stapleAndProtein[i][cCalColNum] * (staVolOfsAndP[i] / 100) + stapleAndProtein[j][cCalColNum] * (staVolOfsAndP[j] / 100);
    //栄養素を足していく
    for(int k=0; k<targets.length; k++){
      realize[k] += stapleAndProtein[i][k] * (staVolOfsAndP[i] / 100) + stapleAndProtein[j][k] * (staVolOfsAndP[j] / 100);
    }
    //野菜類の
    for(int m=0; m<vegetable.length; m++){
      //カロリーを足して
      totalkcal +=  vegetable[m][tCalColNum] * result[m];
      pkcal += vegetable[m][pCalColNum] * result[m];
      fkcal += vegetable[m][fCalColNum] * result[m];
      ckcal += vegetable[m][cCalColNum] * result[m];
      //栄養素を足す
      for(int n=0; n<targets.length; n++){
        realize[n] += vegetable[m][n] * result[m];
      }
    }
    System.out.print("総カロリー : " + totalkcal + " (p:" + pkcal + " , f:" + fkcal + " , c:" + ckcal);
    System.out.println(" -> " + String.format("%.1f", pkcal / totalkcal * 100) + "% : " + String.format("%.1f", fkcal / totalkcal * 100) + "% : " + String.format("%.1f", ckcal / totalkcal * 100) + "%");
    return realize;
  }


  // --- 合計価格の計算 --- 
  public static double getTotalPrice(double[] result, double[] prices){
    double totalPrice = 0;
    for(int i=0; i<result.length; i++){
      totalPrice += result[i] * prices[i];
    }
    return totalPrice;
  }


  // --- resultを分かりやすく表示 --- 
  private static Map<String, Double> formatResult(double[] result){
    String[] vegIng = {"牛乳(店頭売り,紙容器入り)","チーズ(国産品)","チーズ(輸入品)","ヨーグルト","鶏卵","キャベツ","ほうれんそう","はくさい","ねぎ","レタス","もやし","ブロッコリー","アスパラガス","さつまいも","じゃがいも","さといも","だいこん","にんじん","ごぼう","たまねぎ","れんこん","ながいも","えだまめ","さやいんげん","かぼちゃ","きゅうり","なす","トマト","ピーマン","生しいたけ","えのきたけ","しめじ","わかめ","ひじき","豆腐","油揚げ","納豆","こんにゃく"};
    LinkedHashMap<String, Double> formatResult = new LinkedHashMap<>();
    for(int i=0; i<result.length; i++){
      if(result[i] != 0){
        formatResult.put(vegIng[i], result[i]);
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