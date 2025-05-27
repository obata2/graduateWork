package com.gwork.demo;

import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.math3.optim.PointValuePair;

public class check {
  public static void main(String args[]){

    JsonProcesserService jsonProcesserService = new JsonProcesserService();
    NutrientService nutrientService = new NutrientService();

    double[][] stapleAndProtein = nutrientService.getStapleAndProtein(); //主食・肉の栄養テーブル
    double[][] vegetable = transpose(nutrientService.getVegetable()); //野菜類の栄養テーブル(constarintsに合わせるため、ここで転置する)
    double[] prices = setPrices(nutrientService.getUnitPrice(), jsonProcesserService.getIngAndPri()); //100gあたりの野菜類の価格情報

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)
    
    
    //主食：こめ~中華麺
    for(int i=0; i<4; i++){
      //たんぱく源：牛・豚・鶏の各部位
      for(int j=4; j<stapleAndProtein.length; j++){
        double[] targets = modifyTargets(nutrientService.getTargets(), stapleAndProtein, i, j); //補正済みの目標値
        double[] fixedEnergyValue = fixEnergy(stapleAndProtein, i, j);  //主食・肉類を所与としたエネルギーの固定値
        //解く     (ただし、prices,nutrientsは固定し、targetだけ補正して返す→目的関数は固定)
        double[] result = solveLinear(prices, targets, vegetable, fixedEnergyValue);
        for(int l=0; l<result.length; l++){
          result[l] = Math.round(result[l] * 1000.0) / 1000.0;
        }
        System.out.println("解" + Arrays.toString(result));
        
        double[] realize = checkRealize(targets, result, stapleAndProtein, transpose(vegetable), i, j);  //実現値
        System.out.println("実現値" + Arrays.toString(realize));
        
      }
      long elapsed = System.currentTimeMillis() - startTime;
      if (elapsed > timeout) {
          System.out.println("6秒経過したので処理を中断します");
          break;
      }
    }
    System.out.println((System.currentTimeMillis() - startTime) + "ミリ秒で処理完了");
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

  
  //線形計画法で解く
  public static double[] solveLinear(double[] prices, double[] targets, double[][] vegetable, double[] fixedEnergyValue){
    // 最小化する目的関数   Σ(価格*数量)
    LinearObjectiveFunction objective = new LinearObjectiveFunction(prices, 0);
    //制約条件  Σ(栄養*数量)>=目標値  ：栄養素ごとに制約を追加
    Collection<LinearConstraint> constraints = new ArrayList<>();
    for (int i = 0; i < targets.length; i++) {
        constraints.add(new LinearConstraint(vegetable[i], Relationship.GEQ, targets[i]));
    }
    //非負制約は明示的に加える
    for (int i = 0; i < prices.length; i++) {
        double[] coeff = new double[prices.length];
        coeff[i] = 1;
        constraints.add(new LinearConstraint(coeff, Relationship.GEQ, 0));
    }
    //エネルギーの比率に関する制約も追加
    int lastRowNum = vegetable.length - 1;
    for(int i = 0; i < fixedEnergyValue.length; i++){
      if(i%2 == 0){
        constraints.add(new LinearConstraint(vegetable[lastRowNum -  5 + i], Relationship.GEQ, fixedEnergyValue[i]));
      }else{
        constraints.add(new LinearConstraint(vegetable[lastRowNum - 5 + i], Relationship.LEQ, fixedEnergyValue[i]));
      }
    }
    // 解く
    SimplexSolver solver = new SimplexSolver();
    PointValuePair solution = solver.optimize(
      objective,
      new LinearConstraintSet(constraints),
      GoalType.MINIMIZE
    );
    double[] result = solution.getPoint();
    return result;
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


  //主食・肉類を所与とした目標値の補正
  public static double[] modifyTargets(double[] targets, double[][] stapleAndProtein, int i, int j){
    for(int k=0; k<targets.length; k++){
      targets[k] = Math.max(0, (targets[k] - stapleAndProtein[i][k] - stapleAndProtein[j][k]));
    }
    return targets;
  }


  //主食・肉類を所与としたエネルギーの固定値
  public static double[] fixEnergy(double[][] stapleAndProtein, int i, int j){
    double[] fixedEnergyValue = new double[6];
    int lastColNum = stapleAndProtein[i].length - 1;
    for(int k=0; k<fixedEnergyValue.length; k++){
      fixedEnergyValue[k] = stapleAndProtein[i][lastColNum - 5 + i] + stapleAndProtein[j][lastColNum - 5 + i];
    }
    return fixedEnergyValue;
  }


  //実現値を確認する
  public static double[] checkRealize(double[] targets, double[] result, double[][] stapleAndProtein, double[][] vegetable, int i, int j){
    double[] realize = new double[targets.length];
    for(int k=0; k<targets.length; k++){
      realize[k] += stapleAndProtein[i][k] + stapleAndProtein[j][k];
    }
    for(int m=0; m<vegetable.length; m++){
      for(int n=0; n<targets.length; n++){
        realize[n] += vegetable[m][n] * result[m];
      }
    }
    return realize;
  }


  //制約条件を視覚化
  public static String formatConstraint(LinearConstraint lc) {
    double[] coefficients = lc.getCoefficients().toArray();
    Relationship relationship = lc.getRelationship();
    double rhs = lc.getValue();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < coefficients.length; i++) {
        double coef = coefficients[i];
        if (coef == 0) continue;
        if (sb.length() > 0) {
            sb.append(coef >= 0 ? " + " : " - ");
        } else if (coef < 0) {
            sb.append("-");
        }
        sb.append(String.format("%.2f", Math.abs(coef))).append("*x").append(i + 1);
    }
    // 関係記号
    String rel = switch (relationship) {
        case LEQ -> " <= ";
        case GEQ -> " >= ";
        case EQ  -> " = ";
    };
    sb.append(rel).append(rhs);
    return sb.toString();
  }
}
