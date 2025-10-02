package com.gwork.demo.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.gwork.demo.util.DataAdjusterForILP;
import com.gwork.demo.util.ILPResultDTO;


@Service
@Lazy
public class SolveILPService {
  
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
  public ArrayList<ILPResultDTO> getILPResultList (){
    loadDLL();

    ArrayList<ILPResultDTO> iLPResultList = new ArrayList<>();
    double[][] sAndPNutTable = DataAdjusterForILP.sAndPNutTable;
    String[] sAndPName = DataAdjusterForILP.sAndPName;

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)
    
    //計算結果を得る
    int id = 1;
    for(int stapleIndex=0; stapleIndex<4; stapleIndex++){
      for(int proteinIndex=4; proteinIndex<sAndPNutTable.length; proteinIndex++){
        ILPResultDTO result = new ILPResultDTO();
        result.setId(id);
        id++;
        DataAdjusterForILP dataAdjuster = new DataAdjusterForILP(stapleIndex, proteinIndex);   //ここで栄養素目標・カロリーバランスの固定値で補正が入る
        result = solveILP(dataAdjuster, result);   // ** totalPrice, solutionVectorをセット **
        if(result.solutionVector == null){   //計算不可ならばスキップ
          System.out.println(sAndPName[stapleIndex] + " , " + sAndPName[proteinIndex] + " -> 計算不可能です");
          break;
        }
        
        result = formatIngredients(stapleIndex, proteinIndex, result);   // ** ingredientsをセット **
        result = calcKcal(stapleIndex, proteinIndex, dataAdjuster, result);    // ** totalKcal, pfcKcalをセット **
        result = calcNutrients(stapleIndex, proteinIndex, dataAdjuster, result);
        result = calcNutPfcContriRate(stapleIndex, proteinIndex, dataAdjuster, result);
        //ILPResultをリストに格納していく
        iLPResultList.add(result);
      }
      break;
    }
    saveResultToCache(iLPResultList);
    System.out.println((System.currentTimeMillis() - startTime) + "ミリ秒で処理完了");
    return iLPResultList;
  }

  

  // --- ILPで解く(totalPrice, solutionVectorをセット) ---
  private static ILPResultDTO solveILP(DataAdjusterForILP dataAdjuster, ILPResultDTO result) {
    final double[] prices = DataAdjusterForILP.prices;
    final double[][] vegNutTable = DataAdjusterForILP.vegNutTable;
    final double[] staVolOfVeg = DataAdjusterForILP.staVolOfVeg;
    final double[] adjustedTargets = dataAdjuster.adjustedTargets;
    final double[] fixedEnergyValue = dataAdjuster.fixedEnergyValue;
    MPSolver solver = MPSolver.createSolver("CBC");
    if (solver == null) {
      System.err.println("Solverを生成できません。");
      return result;
    }

    int vegNum = vegNutTable.length;
    int nutNum = adjustedTargets.length;
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
      MPConstraint constraint = solver.makeConstraint(adjustedTargets[j], Double.POSITIVE_INFINITY, "nutrient_" + j);
      for (int i = 0; i < vegNum; i++) {
        //食材iに含まれる量を係数とする  (左辺)
        constraint.setCoefficient(x[i], vegNutTable[i][j]);
      }
    }

    //カロリーバランスの制約    sum_i (pi - 0.13ti) * x[i] >= - (P - 0.13T) : proteinLower の例
    MPConstraint proteinLower = solver.makeConstraint(-fixedEnergyValue[0], Double.POSITIVE_INFINITY, "protein_lower"); //(右辺)
    MPConstraint proteinUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[1], "protein_upper");
    MPConstraint fatLower = solver.makeConstraint(-fixedEnergyValue[2], Double.POSITIVE_INFINITY, "fat_lower");
    MPConstraint fatUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[3], "fat_upper");
    MPConstraint carbohydrateLower = solver.makeConstraint(-fixedEnergyValue[4], Double.POSITIVE_INFINITY, "carbohydrate_lower");
    MPConstraint carbohydrateUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[5], "carbohydrate_upper");
    int lastRowNum = vegNutTable[0].length - 1; //"ci-0.65ti"の列番号  (0-indexed)
    for (int i = 0; i < vegNum; i++) {
      //食材iに含まれる量を係数とする
      proteinLower.setCoefficient(x[i], vegNutTable[i][lastRowNum - 5]);  //(左辺)
      proteinUpper.setCoefficient(x[i], vegNutTable[i][lastRowNum - 4]);
      fatLower.setCoefficient(x[i], vegNutTable[i][lastRowNum - 3]);
      fatUpper.setCoefficient(x[i], vegNutTable[i][lastRowNum - 2]);
      carbohydrateLower.setCoefficient(x[i], vegNutTable[i][lastRowNum - 1]);
      carbohydrateUpper.setCoefficient(x[i], vegNutTable[i][lastRowNum]);
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


  // --- カロリーの実現値を算出する ---
  private static ILPResultDTO calcKcal(int stapleIndex, int proteinIndex, DataAdjusterForILP dataAdjuster, ILPResultDTO result){
    final int[] solutionVector = result.solutionVector;
    final double[][] sAndPNutTable = DataAdjusterForILP.sAndPNutTable;
    final double[][] vegNutTable = DataAdjusterForILP.vegNutTable;
    int pCalColNum = (sAndPNutTable[0].length - 1) - 9;  //"タンパク質のエネルギー"の列番号
    int fCalColNum = pCalColNum + 1;
    int cCalColNum = pCalColNum + 2;
    int tCalColNum = pCalColNum + 3;
    //主食・肉類のカロリーと
    double totalkcal = sAndPNutTable[stapleIndex][tCalColNum] + sAndPNutTable[proteinIndex][tCalColNum];
    double pkcal = sAndPNutTable[stapleIndex][pCalColNum] + sAndPNutTable[proteinIndex][pCalColNum];
    double fkcal = sAndPNutTable[stapleIndex][fCalColNum] + sAndPNutTable[proteinIndex][fCalColNum];
    double ckcal = sAndPNutTable[stapleIndex][cCalColNum] + sAndPNutTable[proteinIndex][cCalColNum];
    //野菜類のカロリーを算出する
    for(int m=0; m<vegNutTable.length; m++){
      totalkcal +=  vegNutTable[m][tCalColNum] * solutionVector[m];
      pkcal += vegNutTable[m][pCalColNum] * solutionVector[m];
      fkcal += vegNutTable[m][fCalColNum] * solutionVector[m];
      ckcal += vegNutTable[m][cCalColNum] * solutionVector[m];
    }
    double[] pfcKcal = {pkcal, fkcal, ckcal};    //計算はすべて小数で行い、誤差を避ける
    result.setTotalKcal((int) totalkcal);
    result.setpfcKcal(pfcKcal);
    return result;
  }


  // --- 栄養素の実現値を算出する ---
  private static ILPResultDTO calcNutrients(int stapleIndex, int proteinIndex, DataAdjusterForILP dataAdjuster, ILPResultDTO result){
    final int[] solutionVector = result.solutionVector;
    final int nutNum = DataAdjusterForILP.nutrientsName.length;
    final double[][] sAndPNutTable = DataAdjusterForILP.sAndPNutTable;
    final double[][] vegNutTable = DataAdjusterForILP.vegNutTable;
    double[] calculatedNutrients = new double[nutNum];
    //主食・肉類の栄養素を算出
    for(int k=0; k<nutNum; k++){
      calculatedNutrients[k] += sAndPNutTable[stapleIndex][k] + sAndPNutTable[proteinIndex][k];
    }
    //野菜類の栄養素を算出
    for(int m=0; m<vegNutTable.length; m++){
      if(solutionVector[m] == 0){
        continue;
      }
      for(int n=0; n<nutNum; n++){
        calculatedNutrients[n] += vegNutTable[m][n] * solutionVector[m];
      }
    }
    result.setCalculatedNutrients(calculatedNutrients);
    return result;
  }


  // --- 栄養素・pfcカロリーの寄与率を計算する ---
  private static ILPResultDTO calcNutPfcContriRate(int stapleIndex, int proteinIndex, DataAdjusterForILP dataAdjuster, ILPResultDTO result){
    final int nutNum = DataAdjusterForILP.nutrientsName.length;
    final double[][] sAndPNutTable = DataAdjusterForILP.sAndPNutTable;
    final String[] sAndPName = DataAdjusterForILP.sAndPName;
    final double[] originalTargets = DataAdjusterForILP.originalTargets;
    final int totalKcal = result.totalKcal;
    LinkedHashMap<String, double[]> nutrientsContriRate = new LinkedHashMap<>();
    LinkedHashMap<String, double[]> pfcContriRate = new LinkedHashMap<>();
    
    //主食・肉類について
    nutrientsContriRate.put(sAndPName[stapleIndex], new double[nutNum]);          //栄養素の数だけの配列
    nutrientsContriRate.put(sAndPName[proteinIndex], new double[nutNum]);
    pfcContriRate.put(sAndPName[stapleIndex], new double[3]);                    //pfcの3要素の配列
    pfcContriRate.put(sAndPName[proteinIndex], new double[3]);
    //栄養素の寄与率
    for(int j=0; j<nutNum; j++){
      nutrientsContriRate.get(sAndPName[stapleIndex])[j] = (sAndPNutTable[stapleIndex][j]) / originalTargets[j] * 100;
      nutrientsContriRate.get(sAndPName[proteinIndex])[j] = (sAndPNutTable[proteinIndex][j]) / originalTargets[j] * 100;
    }
    //pfcカロリーの寄与率
    for(int j=0; j<3; j++){
      pfcContriRate.get(sAndPName[stapleIndex])[j] = (sAndPNutTable[stapleIndex][nutNum + j]) / totalKcal * 100;
      pfcContriRate.get(sAndPName[proteinIndex])[j] = (sAndPNutTable[proteinIndex][nutNum + j])  / totalKcal * 100;
    }

    final int[] solutionVector = result.solutionVector;
    final double[][] vegNutTable = DataAdjusterForILP.vegNutTable;
    final String[] vegName = DataAdjusterForILP.vegName;
    //野菜類について
    for(int j=0; j<vegNutTable.length; j++){
      if(solutionVector[j] == 0){
        continue;
      }else{
        nutrientsContriRate.put(vegName[j], new double[nutNum]);                  //カロリーの部分のサイズだけ縮めているのに注意
        pfcContriRate.put(vegName[j], new double[3]);
      }
      //栄養素の寄与率
      for(int k=0; k<nutNum; k++){
        nutrientsContriRate.get(vegName[j])[k] = (vegNutTable[j][k] * solutionVector[j]) / originalTargets[k] * 100; 
      }
      //pfcカロリーの寄与率
      for(int k=0; k<3; k++){
        pfcContriRate.get(vegName[j])[k] = (vegNutTable[j][nutNum + k] * solutionVector[j]) / totalKcal * 100;
      }
    }
    result.setNutrientsContriRate(nutrientsContriRate);
    result.setPfcContriRate(pfcContriRate);
    return result;
  }


  // --- solution を{材料名：グラム数}の辞書に変換 --- 
  private static ILPResultDTO formatIngredients(int stapleIndex, int proteinIndex, ILPResultDTO result){
    LinkedHashMap<String, String> ingredients = new LinkedHashMap<>();
    final int[] solutionVector = result.solutionVector;
    final String[] vegName = DataAdjusterForILP.vegName;
    final int[] unitQuantity = DataAdjusterForILP.unitQuantity;
    final String[] sAndPName = DataAdjusterForILP.sAndPName;
    final double[] staVolOfsAndP = DataAdjusterForILP.staVolOfsAndP;
    ingredients.put(sAndPName[stapleIndex], (staVolOfsAndP[stapleIndex] + "g"));
    ingredients.put(sAndPName[proteinIndex], (staVolOfsAndP[proteinIndex] + "g"));
    for(int i=0; i<solutionVector.length; i++){
      if(solutionVector[i] != 0){
        ingredients.put(vegName[i], (solutionVector[i] * unitQuantity[i]) + "g");
      }
    }
    result.setIngredients(ingredients);
    return result;
  }


  // --- 計算結果をjsonファイルに書き込む ---
  public void saveResultToCache(ArrayList<ILPResultDTO> iLPResultList) {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedILPResult.json";
    ObjectMapper mapper = new ObjectMapper();
    try {
        mapper.writeValue(new File(FILE_PATH), iLPResultList);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}