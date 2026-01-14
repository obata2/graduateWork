package com.gwork.demo.Service.ilp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.gwork.demo.Service.nutrient.NutrientService;
import com.gwork.demo.dto.ILPResultDTO;

import jakarta.annotation.PostConstruct;


@Service
@Lazy
public class SolveILPService {

  // コンストラクタインジェクション
  private final FixValueFactory fixValueFactory;
  private final DataAdjusterFactory dataAdjusterFactory;
  public SolveILPService (FixValueFactory fixValueFactory, DataAdjusterFactory dataAdjusterFactory) {
    this.fixValueFactory = fixValueFactory;
    this.dataAdjusterFactory = dataAdjusterFactory;
  }
  
  // DLLのロード処理を行う
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

  //ロード済みフラグ
  private boolean loaded = false;

  private DataAdjuster dataAdjusterService;
  private double[][][] adjustedNutrientTable;
  private double[][] adjustedPrices;
  private double[][] adjustedStandardQty;
  private int[] vegUnitQuantity;

  private String[][] ingName = NutrientService.name;
  private String [][] id = NutrientService.id;
  private String[] nutrientsName = {"たんぱく質","食物繊維総量","カリウム","カルシウム","マグネシウム","鉄","亜鉛","ビタミンA","ビタミンD","ビタミンB1","ビタミンB2","ビタミンB6","葉酸","ビタミンC"};

  // 計算に用いる変数の初期化処理
  private void init (String userId) {
    dataAdjusterService = dataAdjusterFactory.create(userId);
    adjustedNutrientTable = dataAdjusterService.getAdjustedNutrientTable();
    adjustedPrices = dataAdjusterService.getAdjustedPrices();
    adjustedStandardQty = dataAdjusterService.getAdjustedStandardQty();
    vegUnitQuantity = dataAdjusterService.getVegUnitQuantity();
  }

  // 整数計画法で解き、結果をまとめてリストにする
  public ArrayList<ILPResultDTO> solveILP (String userId, Set<String> selected, Set<String> excluded) {
    loadDLL();
    init(userId);
    ArrayList<ILPResultDTO> iLPResultList = new ArrayList<>();
    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 60000; // 時間制限(ミリ秒)
    //計算結果を得る
    int resultId = 1;
    for(int stapleIndex=0; stapleIndex<4; stapleIndex++){
      ArrayList<String> used = new ArrayList<>();
      for(int proteinIndex=4; proteinIndex<adjustedNutrientTable[0].length; proteinIndex++){
        // 1度計算を行ったお肉の部位はもうそれ以上計算は行わない(牛かたロース、豚ロース、鶏ももだけに限定するやつ)
        if(!used.contains(id[0][proteinIndex])){
          used.add(id[0][proteinIndex]);
        }else{
          continue;
        }
        ArrayList<ILPResultDTO> twoPatternsOfresults = new ArrayList<>();
        FixValue fixValue = fixValueFactory.create(userId, stapleIndex, proteinIndex);   //ここで栄養素目標・カロリーバランスの固定値で補正が入る
        twoPatternsOfresults = solveILP(twoPatternsOfresults, fixValue, selected, excluded);   // ** totalPrice, solutionVectorをセット **
        for(ILPResultDTO result : twoPatternsOfresults){ 
          if(result.getSolutionVector() == null){   //計算不可ならばスキップ
            System.out.println(ingName[0][stapleIndex] + " , " + ingName[0][proteinIndex] + " -> 計算不可能です");
            continue;
          }
          result = formatIngredients(stapleIndex, proteinIndex, result);   // ** ingredientsをセット **
          result = calcKcal(stapleIndex, proteinIndex, result);    // ** totalKcal, pfcKcalをセット **
          result = calcNutrients(stapleIndex, proteinIndex, result);
          result = calcNutPfcContriRate(stapleIndex, proteinIndex, result);
          calcPriceBreakDown(stapleIndex, proteinIndex, result);
          result.setResultId(resultId);
          resultId++;

          // ILPResultをリストに格納していく
          iLPResultList.add(result);
        }
      }
      break;
    }
    System.out.println((System.currentTimeMillis() - startTime) + "ミリ秒で処理完了");
    return iLPResultList;
  }

  

  // ILPで解く(totalPrice, solutionVectorをセット)
  private ArrayList<ILPResultDTO> solveILP(ArrayList<ILPResultDTO> twoPatternsOfresult, FixValue fixValue, Set<String> selected, Set<String> excluded) {
    final double[] adjustedTargets = fixValue.getAdjustedTargets();
    final double fixedPrice = fixValue.getFixedPrice();
    int vegNum = adjustedNutrientTable[1].length;
    int nutNum = adjustedTargets.length;

    boolean[] used = null;

    for(int pattern=0; pattern<2; pattern++){
      // 以下、ループで処理
      ILPResultDTO result = new ILPResultDTO();
      MPSolver solver = MPSolver.createSolver("CBC");
      if (solver == null) {
        System.err.println("Solverを生成できません。");
        //return result;
        continue;
      }

      /*
      //変数を定義する    x[]:食材iの数量     0~(1食分の目安量の3倍まで)        ←    連続な値
      MPVariable[] x = new MPVariable[vegNum];
      for (int i = 0; i < vegNum; i++) {
        x[i] = solver.makeIntVar(0.0, staVolOfVeg[i] * 3, "x_" + i);
      }*/

      // 変数を定義する    x[]:食材iの数量     (0  または  1食分の目安量の0.5 ~ 3倍)        ←    非連続な値、バイナリ変数を用いて表現
      MPVariable[] x = new MPVariable[vegNum];        //主変数
      MPVariable[] y = new MPVariable[vegNum];        //変数が非連続な値をとるようにさせる補助バイナリ変数(0 or 1)
      MPVariable[] z = new MPVariable[vegNum];        //変数が非ゼロであるかを管理する補助変数(0 or 1)
      for (int i = 0; i < vegNum; i++) {
        double minQty = adjustedStandardQty[1][i] * 0.5;
        double maxQty = adjustedStandardQty[1][i] * 3;
        // 指定・除外の設定をここで反映させる
        if (selected.contains(ingName[1][i])){
          x[i] = solver.makeIntVar(minQty, maxQty, "x_" + i);
        }else if (excluded.contains(ingName[1][i])){
          x[i] = solver.makeIntVar(0, 0, "x_" + i);
        }else {
          x[i] = solver.makeIntVar(0, maxQty, "x_" + i);
        }
        y[i] = solver.makeIntVar(0, 1, "y_" + i);
        z[i] = solver.makeIntVar(0, 1, "z_" + i);
        // 非連続な変数にする
        // 制約1: x[i] >= minVol * y[i]     (y[i]が1であるなら、x[i]は1食分の目安量の0.5倍より多い)
        MPConstraint c1 = solver.makeConstraint(0, Double.POSITIVE_INFINITY);
        c1.setCoefficient(x[i], 1);
        c1.setCoefficient(y[i], - minQty);
        // 制約2: x[i] <= maxVol * y[i]       (y[i]が1であるなら、〃 3倍より小さい)
        MPConstraint c2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0);
        c2.setCoefficient(x[i], 1);
        c2.setCoefficient(y[i], - maxQty);
        // x[i]とz[i]の連動
        // 制約 A: x[i] - z[i] >= 0  (x >= z)
        MPConstraint cA = solver.makeConstraint(0.0, Double.POSITIVE_INFINITY);
        cA.setCoefficient(x[i], 1.0);
        cA.setCoefficient(z[i], -1.0);
        // 制約 B: x[i] - M*z[i] <= 0  (x <= M * z)
        MPConstraint cB = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0);
        cB.setCoefficient(x[i], 1.0);
        cB.setCoefficient(z[i], -maxQty);
      }

      // 栄養素目標の制約    sum_i (vegetable[i][j] * x[i]) >= targets[j]
      for (int j = 0; j < nutNum; j++) {
        //栄養素jの目標値を設定  (右辺)
        MPConstraint constraint = solver.makeConstraint(adjustedTargets[j], Double.POSITIVE_INFINITY, "nutrient_" + j);
        for (int i = 0; i < vegNum; i++) {
          //食材iに含まれる量を係数とする  (左辺)
          constraint.setCoefficient(x[i], adjustedNutrientTable[1][i][j]);
        }
      }

      /*
      // カロリーバランスの制約    sum_i (pi - 0.13ti) * x[i] >= - (P - 0.13T) : proteinLower の例
      final double[] fixedEnergyValue = dataAdjuster.fixedEnergyValue;
      MPConstraint proteinLower = solver.makeConstraint(-fixedEnergyValue[0], Double.POSITIVE_INFINITY, "protein_lower"); //(右辺)
      MPConstraint proteinUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[1], "protein_upper");
      MPConstraint fatLower = solver.makeConstraint(-fixedEnergyValue[2], Double.POSITIVE_INFINITY, "fat_lower");
      MPConstraint fatUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[3], "fat_upper");
      MPConstraint carbohydrateLower = solver.makeConstraint(-fixedEnergyValue[4], Double.POSITIVE_INFINITY, "carbohydrate_lower");
      MPConstraint carbohydrateUpper = solver.makeConstraint(Double.NEGATIVE_INFINITY, -fixedEnergyValue[5], "carbohydrate_upper");
      int lastRowNum = adjustedNutrientTable[1][0].length - 1; //"ci-0.65ti"の列番号  (0-indexed)
      for (int i = 0; i < x.length; i++) {
        //食材iに含まれる量を係数とする
        proteinLower.setCoefficient(x[i], adjustedNutrientTable[1][i][lastRowNum - 5]);  //(左辺)
        proteinUpper.setCoefficient(x[i], adjustedNutrientTable[1][i][lastRowNum - 4]);
        fatLower.setCoefficient(x[i], adjustedNutrientTable[1][i][lastRowNum - 3]);
        fatUpper.setCoefficient(x[i], adjustedNutrientTable[1][i][lastRowNum - 2]);
        carbohydrateLower.setCoefficient(x[i], adjustedNutrientTable[1][i][lastRowNum - 1]);
        carbohydrateUpper.setCoefficient(x[i], adjustedNutrientTable[1][i][lastRowNum]);
      }*/

      // 前回の解で値が0ではなかった変数の半分以上は、今回は値を0にするという制約
      if (used != null) {
        // 前回解の種類数
        int countUsed = 0;
        for (boolean u : used) {
          if (u) {
            countUsed++;
          }
        }
        // 前回使われていた変数の半分までしか今回使えない
        MPConstraint diff = solver.makeConstraint(0, countUsed / 2.0);
        for (int i = 0; i < used.length; i++) {
          if (used[i]) {
            diff.setCoefficient(z[i], 1);
          }
        }
      }

      //目的関数（価格の総和を最小化）
      MPObjective objective = solver.objective();
      for (int i = 0; i < x.length; i++) {
        objective.setCoefficient(x[i], adjustedPrices[1][i]);
      }
      objective.setMinimization();

      // 解の取得と usedの更新
      MPSolver.ResultStatus resultStatus = solver.solve();
      if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {    //解が見つかった場合
        int[] solutionVector = new int[vegNum];
        used = new boolean[vegNum];
        for (int i = 0; i < solutionVector.length; i++) {
          solutionVector[i] = (int) Math.round(x[i].solutionValue());
          used[i] = solutionVector[i] > 0 ? true : false;
        }
        //result.setTotalPrice((int) (objective.value() + fixedPrice));    //totalPriceをセット
        //System.out.println("固定の価格：" + fixedPrice);
        result.setSolutionVector(solutionVector);   //solutionVectorをセット
        twoPatternsOfresult.add(result);
      } else {    //解が見つからなかった場合
        System.out.println("optimal solution が見つけられませんでした");
      }
    }
    return twoPatternsOfresult;
  }


  // カロリーの実現値を算出する
  private ILPResultDTO calcKcal(int stapleIndex, int proteinIndex, ILPResultDTO result){
    final int[] solutionVector = result.getSolutionVector();
    int pCalColNum = (adjustedNutrientTable[0][0].length - 1) - 9;  //"タンパク質のエネルギー"の列番号
    int fCalColNum = pCalColNum + 1;
    int cCalColNum = pCalColNum + 2;
    int tCalColNum = pCalColNum + 3;
    //主食・肉類のカロリーと
    double totalkcal = adjustedNutrientTable[0][stapleIndex][tCalColNum] + adjustedNutrientTable[0][proteinIndex][tCalColNum];
    double pkcal = adjustedNutrientTable[0][stapleIndex][pCalColNum] + adjustedNutrientTable[0][proteinIndex][pCalColNum];
    double fkcal = adjustedNutrientTable[0][stapleIndex][fCalColNum] + adjustedNutrientTable[0][proteinIndex][fCalColNum];
    double ckcal = adjustedNutrientTable[0][stapleIndex][cCalColNum] + adjustedNutrientTable[0][proteinIndex][cCalColNum];
    //野菜類のカロリーを算出する
    for(int m=0; m<adjustedNutrientTable[1][0].length; m++){
      totalkcal +=  adjustedNutrientTable[1][m][tCalColNum] * solutionVector[m];
      pkcal += adjustedNutrientTable[1][m][pCalColNum] * solutionVector[m];
      fkcal += adjustedNutrientTable[1][m][fCalColNum] * solutionVector[m];
      ckcal += adjustedNutrientTable[1][m][cCalColNum] * solutionVector[m];
    }
    double[] pfcKcal = {pkcal, fkcal, ckcal};    //計算はすべて小数で行い、誤差を避ける
    result.setTotalKcal((int) totalkcal);
    result.setPfcKcal(pfcKcal);
    return result;
  }


  // 栄養素の実現値を算出する
  private ILPResultDTO calcNutrients(int stapleIndex, int proteinIndex, ILPResultDTO result){
    final int[] solutionVector = result.getSolutionVector();
    final int nutNum = nutrientsName.length;
    double[] calculatedNutrients = new double[nutNum];
    //主食・肉類の栄養素を算出
    for(int k=0; k<nutNum; k++){
      calculatedNutrients[k] += adjustedNutrientTable[0][stapleIndex][k] + adjustedNutrientTable[0][proteinIndex][k];
    }
    //野菜類の栄養素を算出
    for(int m=0; m<adjustedNutrientTable[1].length; m++){
      if(solutionVector[m] == 0){
        continue;
      }
      for(int n=0; n<nutNum; n++){
        calculatedNutrients[n] += adjustedNutrientTable[1][m][n] * solutionVector[m];
      }
    }
    result.setCalculatedNutrients(calculatedNutrients);
    return result;
  }


  // 栄養素・pfcカロリーの寄与率を計算する
  private ILPResultDTO calcNutPfcContriRate(int stapleIndex, int proteinIndex, ILPResultDTO result){
    final int nutNum = nutrientsName.length;
    final double[] originalTargets = NutrientService.targets;;
    final int totalKcal = result.getTotalKcal();
    LinkedHashMap<String, double[]> nutrientsContriRate = new LinkedHashMap<>();
    LinkedHashMap<String, double[]> pfcContriRate = new LinkedHashMap<>();
    //主食・肉類について
    nutrientsContriRate.put(ingName[0][stapleIndex], new double[nutNum]);          //栄養素の数だけの配列
    nutrientsContriRate.put(ingName[0][proteinIndex], new double[nutNum]);
    pfcContriRate.put(ingName[0][stapleIndex], new double[3]);                    //pfcの3要素の配列
    pfcContriRate.put(ingName[0][proteinIndex], new double[3]);
    //栄養素の寄与率
    for(int j=0; j<nutNum; j++){
      nutrientsContriRate.get(ingName[0][stapleIndex])[j] = (adjustedNutrientTable[0][stapleIndex][j]) / originalTargets[j] * 100;
      nutrientsContriRate.get(ingName[0][proteinIndex])[j] = (adjustedNutrientTable[0][proteinIndex][j]) / originalTargets[j] * 100;
    }
    //pfcカロリーの寄与率
    for(int j=0; j<3; j++){
      pfcContriRate.get(ingName[0][stapleIndex])[j] = (adjustedNutrientTable[0][stapleIndex][nutNum + j]) / totalKcal * 100;
      pfcContriRate.get(ingName[0][proteinIndex])[j] = (adjustedNutrientTable[0][proteinIndex][nutNum + j])  / totalKcal * 100;
    }
    final int[] solutionVector = result.getSolutionVector();
    //野菜類について
    for(int j=0; j<adjustedNutrientTable[1].length; j++){
      if(solutionVector[j] == 0){
        continue;
      }else{
        nutrientsContriRate.put(ingName[1][j], new double[nutNum]);                  //カロリーの部分のサイズだけ縮めているのに注意
        pfcContriRate.put(ingName[1][j], new double[3]);
      }
      //栄養素の寄与率
      for(int k=0; k<nutNum; k++){
        nutrientsContriRate.get(ingName[1][j])[k] = (adjustedNutrientTable[1][j][k] * solutionVector[j]) / originalTargets[k] * 100; 
      }
      //pfcカロリーの寄与率
      for(int k=0; k<3; k++){
        pfcContriRate.get(ingName[1][j])[k] = (adjustedNutrientTable[1][j][nutNum + k] * solutionVector[j]) / totalKcal * 100;
      }
    }
    result.setNutrientsContriRate(nutrientsContriRate);
    result.setPfcContriRate(pfcContriRate);
    return result;
  }


  // 価格の内訳を計算する
  private void calcPriceBreakDown (int stapleIndex, int proteinIndex, ILPResultDTO result) {
    LinkedHashMap<String, Integer> priceBreakdown = new LinkedHashMap<>();
    final int[] solutionVector = result.getSolutionVector();
    int totalPrice = 0;
    //主食・肉類の方
    priceBreakdown.put(ingName[0][stapleIndex], (int) (adjustedPrices[0][stapleIndex]));
    priceBreakdown.put(ingName[0][proteinIndex], (int) (adjustedPrices[0][proteinIndex]));
    totalPrice += (int) (adjustedPrices[0][stapleIndex]);
    totalPrice += (int) (adjustedPrices[0][proteinIndex]);
    //野菜類の方
    for (int i=0; i<solutionVector.length; i++) {
      if (solutionVector[i] == 0) {
        continue;
      } else {
        priceBreakdown.put(ingName[1][i], (int) (adjustedPrices[1][i] * solutionVector[i]));
        totalPrice += (int) (adjustedPrices[1][i] * solutionVector[i]);
      }
    }
    result.setTotalPrice(totalPrice);
    result.setPriceBreakdown(priceBreakdown);
  }

  // solution を{材料名：グラム数}の辞書に変換 
  private ILPResultDTO formatIngredients(int stapleIndex, int proteinIndex, ILPResultDTO result){
    LinkedHashMap<String, String> ingredients = new LinkedHashMap<>();
    final int[] solutionVector = result.getSolutionVector();
    ingredients.put(ingName[0][stapleIndex], (adjustedStandardQty[0][stapleIndex] + "g"));
    ingredients.put(ingName[0][proteinIndex], (adjustedStandardQty[0][proteinIndex] + "g"));
    for(int i=0; i<solutionVector.length; i++){
      if(solutionVector[i] != 0){
        ingredients.put(ingName[1][i], (solutionVector[i] * vegUnitQuantity[i]) + "g");
      }
    }
    result.setIngredients(ingredients);
    return result;
  }


  /* 計算結果をjsonファイルに書き込む
  public void saveResultToCache(ArrayList<ILPResultDTO> iLPResultList) {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedILPResult.json";
    ObjectMapper mapper = new ObjectMapper();
    try {
        mapper.writeValue(new File(FILE_PATH), iLPResultList);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }*/
}