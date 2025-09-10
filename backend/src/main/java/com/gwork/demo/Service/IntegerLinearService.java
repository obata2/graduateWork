package com.gwork.demo.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.gwork.demo.util.DataAdjuster;
import com.gwork.demo.util.ILPResult;


@Service
@Lazy
public class IntegerLinearService {
  
  //ロード済みフラグ
  private static boolean loaded = false;

  // --- DLLのロード処理を行う ---
  private void loadDLL() {
    if (!loaded) {
      System.out.println("(ロード完了)    パス一覧：" + System.getProperty("java.library.path"));
      System.out.println("Loading DLL in classloader: " + this.getClass().getClassLoader());
      String nativePath = "C:\\Users\\81809\\Desktop\\demo\\backend\\libs\\native\\jniortools.dll";
      System.load(nativePath);
      loaded = true;
    }else{
      System.out.println("ロード済みです");
    }
  }

  // --- 計算を行い、その結果をリストに格納していく ---
  public ArrayList<ILPResult> getILPResultList (){
    loadDLL();

    ArrayList<ILPResult> ilpResultList = new ArrayList<>();
    double[][] stapleAndProtein = DataAdjuster.stapleAndProtein;
    String[] spIng = DataAdjuster.spIng;

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)
    
    //計算結果を得る
    int id = 1;
    for(int stapleIndex=0; stapleIndex<4; stapleIndex++){
      for(int proteinIndex=4; proteinIndex<stapleAndProtein.length; proteinIndex++){
        ILPResult result = new ILPResult();
        result.setId(id);
        id++;
        DataAdjuster dataAdjuster = new DataAdjuster(stapleIndex, proteinIndex);   //ここで栄養素目標・カロリーバランスの固定値で補正が入る
        result = solveILP(dataAdjuster, result);   // ** totalPrice, solutionVectorをセット **
        if(result.solutionVector == null){   //計算不可ならばスキップ
          System.out.println(spIng[stapleIndex] + " , " + spIng[proteinIndex] + " -> 計算不可能です");
          break;
        }
        
        result = formatSolution(stapleIndex, proteinIndex, result);   // ** ingredientsをセット **
        result = calcKcalAndNutrients(stapleIndex, proteinIndex, dataAdjuster, result);    // ** totalKcal, pfcKcal, nutrientsをセット **
        //ILPResultをリストに格納していく
        ilpResultList.add(result);
      }
    }
    saveResultToCache(ilpResultList);
    System.out.println((System.currentTimeMillis() - startTime) + "ミリ秒で処理完了");
    return ilpResultList;
  }

  

  // --- ILPで解く(totalPrice, solutionVectorをセット) ---
  public static ILPResult solveILP(DataAdjuster dataAdjuster, ILPResult result) {
    final double[] prices = DataAdjuster.prices;
    final double[][] vegetable = DataAdjuster.vegetable;
    final double[] staVolOfVeg = DataAdjuster.staVolOfVeg;
    final double[] modifiedTargets = dataAdjuster.modifiedTargets;
    final double[] fixedEnergyValue = dataAdjuster.fixedEnergyValue;
    MPSolver solver = MPSolver.createSolver("CBC");
    if (solver == null) {
      System.err.println("Solverを生成できません。");
      return result;
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

    //目的関数（価格の総和を最小化）
    MPObjective objective = solver.objective();
    for (int i = 0; i < vegNum; i++) {
      objective.setCoefficient(x[i], prices[i]);
    }
    objective.setMinimization();

    // 解く
    MPSolver.ResultStatus resultStatus = solver.solve();
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {    //解が見つかった場合
      int[] solutionVector = new int[vegNum];
      for (int i = 0; i < vegNum; i++) {
        solutionVector[i] = (int) Math.round(x[i].solutionValue());
      }
      result.setTotalPrice((int) objective.value());    //totalPriceをセット
      result.setSolutionVector(solutionVector);   //solutionVectorをセット
      return result;
    } else {    //解が見つからなかった場合
      System.out.println("optimal solution が見つけられませんでした");
      return result;
    }
  }


  // --- 実現値を確認する --- 
  public static ILPResult calcKcalAndNutrients(int stapleIndex, int proteinIndex, DataAdjuster dataAdjuster, ILPResult result){
    final int[] solution = result.solutionVector;
    final double[] modifiedTargets = dataAdjuster.modifiedTargets;
    final double[][] stapleAndProtein = DataAdjuster.stapleAndProtein;
    final double[] staVolOfsAndP = DataAdjuster.staVolOfsAndP;
    final double[][] vegetable = DataAdjuster.vegetable;
    final double[] nutrients = new double[modifiedTargets.length];
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
      nutrients[k] += stapleAndProtein[stapleIndex][k] * (staVolOfsAndP[stapleIndex] / 100) + stapleAndProtein[proteinIndex][k] * (staVolOfsAndP[proteinIndex] / 100);
    }
    //野菜類の
    for(int m=0; m<vegetable.length; m++){
      //カロリーを足して
      totalkcal +=  vegetable[m][tCalColNum] * solution[m];
      pkcal += vegetable[m][pCalColNum] * solution[m];
      fkcal += vegetable[m][fCalColNum] * solution[m];
      ckcal += vegetable[m][cCalColNum] * solution[m];
      //栄養素を足す
      for(int n=0; n<modifiedTargets.length; n++){
        nutrients[n] += vegetable[m][n] * solution[m];
      }
    }
    //栄養素は小数点第2位までに丸める
    for(int n=0; n<modifiedTargets.length; n++){
      nutrients[n] = Math.round(nutrients[n] * 100.0) / 100.0;
    }
    int[] pfcKcal = {(int) pkcal, (int) fkcal, (int) ckcal};    //計算はすべて小数で行い、誤差を避ける
    result.setTotalKcal((int) totalkcal);
    result.setpfcKcal(pfcKcal);
    result.setNutrients(formatNutrients(nutrients));
    return result;
  }


  // --- solution を{材料名：グラム数}の辞書に変換 --- 
  private static ILPResult formatSolution(int stapleIndex, int proteinIndex, ILPResult result){
    LinkedHashMap<String, String> formatSolution = new LinkedHashMap<>();
    final int[] solution = result.solutionVector;
    final String[] vegIng = DataAdjuster.vegIng;
    final int[] unitQuantity = DataAdjuster.unitQuantity;
    final String[] spIng = DataAdjuster.spIng;
    final double[] staVolOfsAndP = DataAdjuster.staVolOfsAndP;
    formatSolution.put(spIng[stapleIndex], (staVolOfsAndP[stapleIndex] + "g"));
    formatSolution.put(spIng[proteinIndex], (staVolOfsAndP[proteinIndex] + "g"));
    for(int i=0; i<solution.length; i++){
      if(solution[i] != 0){
        formatSolution.put(vegIng[i], (solution[i] * unitQuantity[i]) + "g");
      }
    }
    result.setIngredients(formatSolution);
    return result;
  }


  // --- nutrientsを {栄養素名：実現値} の辞書に変換 --- 
  private static LinkedHashMap<String, Double> formatNutrients(double[] nutrients){
    String[] nutrientsName = {"たんぱく質","食物繊維総量","カリウム","カルシウム","マグネシウム","鉄","亜鉛","ビタミンA","ビタミンD","ビタミンB1","ビタミンB2","ビタミンB6","葉酸","ビタミンC"};
    LinkedHashMap<String, Double> formatNutrients = new LinkedHashMap<>();
    for(int i=0; i<nutrients.length; i++){
      if(nutrients[i] != 0){
        formatNutrients.put(nutrientsName[i], nutrients[i]);
      }
    }
    return formatNutrients;
  }


  // --- 計算結果をjsonファイルに書き込む ---
  public void saveResultToCache(ArrayList<ILPResult> ilpResultList) {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedILPResult.json";
    ObjectMapper mapper = new ObjectMapper();
    try {
        mapper.writeValue(new File(FILE_PATH), ilpResultList);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}