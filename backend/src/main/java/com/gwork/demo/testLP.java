package com.gwork.demo;

import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.optim.PointValuePair;

public class testLP {
  public static void main(String args[]){

    JsonProcesserService jsonProcesserService = new JsonProcesserService();
    NutrientService nutrientService = new NutrientService();

    double[][] stapleAndProtein = nutrientService.getStapleAndProtein(); //主食・肉の栄養テーブル
    double[][] vegetable = transpose(nutrientService.getVegetable()); //野菜類の栄養テーブル <- (constarintsに合わせるため、ここで転置)
    double[] prices = setPrices(nutrientService.getUnitPrice(), jsonProcesserService.getIngAndPri()); //100gあたりの野菜類の価格情報
    double[] minVolOfVeg = nutrientService.getMinimumVolOfVeg();
    String[] spIng = {"うるち米(単一原料米,「コシヒカリ」)","ゆでうどん","スパゲッティ","中華麺","牛肉(かた)","牛肉(かたロース)","牛肉(リブロース)","牛肉(サーロイン)","牛肉(ばら)","牛肉(もも)","牛肉(そともも)","牛肉(ランプ)","牛肉(ヒレ)","豚肉(かた)","豚肉(かたロース)","豚肉(ロース)","豚肉(ばら)","豚肉(もも)","豚肉(そともも)","豚肉(ヒレ)","鶏肉(手羽)","鶏肉(手羽さき)","鶏肉(手羽もと)","鶏肉(むね)","鶏肉(もも)","鶏肉(ささみ)","鶏肉(ひきにく)"};

    long startTime = System.currentTimeMillis(); // 開始時刻を記録
    long timeout = 6000; // 時間制限(ミリ秒)
    
    //主食：こめ~中華麺
    for(int i=0; i<4; i++){
      //たんぱく源：牛・豚・鶏の各部位
      for(int j=4; j<stapleAndProtein.length; j++){
        double[] targets = modifyTargets(nutrientService.getTargets(), stapleAndProtein, i, j); //補正済みの目標値
        double[] fixedEnergyValue = fixEnergy(stapleAndProtein, i, j);  //主食・肉類を所与としたエネルギーの固定値
        //解く     (ただし、prices,nutrientsは固定し、targetだけ補正して返す→目的関数は固定)
        Optional<double[]> resultOpt = solveLinear(prices, targets, vegetable, fixedEnergyValue);  //選択された食材
        if(!resultOpt.isPresent()){   //計算不可ならばスキップ
          System.out.println(spIng[i] + " , " + spIng[j] + " -> 計算不可能です");
          break;
        }
        double[] result = resultOpt.get();
        for(int l=0; l<result.length; l++){
          result[l] = Math.round(result[l] * 1000.0) / 1000.0;
        }
        System.out.println(spIng[i] + " , " + spIng[j] + " -> " + formatResult(result));
        double[] realize = checkRealize(targets, result, stapleAndProtein, transpose(vegetable), i, j);  //実現値
        System.out.println("実現値 : " +formatRealize(realize));
        
        break;
      }
      long elapsed = System.currentTimeMillis() - startTime;
      if (elapsed > timeout) {
          System.out.println("6秒経過したので処理を中断します");
          break;
      }

      break;
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
  public static Optional<double[]> solveLinear(double[] prices, double[] targets, double[][] vegetable, double[] fixedEnergyValue){
    // 最小化する目的関数   Σ(価格*数量)
    LinearObjectiveFunction objective = new LinearObjectiveFunction(prices, 0);
    //制約条件  Σ(栄養*数量)>=目標値  ：栄養素ごとに制約を追加
    Collection<LinearConstraint> constraints = new ArrayList<>();
    for (int i = 1; i < targets.length; i++) {
        constraints.add(new LinearConstraint(vegetable[i], Relationship.GEQ, targets[i]));
    }
    //1食分の最低量に関する制約(非負)
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
    try {
      SimplexSolver solver = new SimplexSolver();
      PointValuePair solution = solver.optimize(
          objective,
          new LinearConstraintSet(constraints),
          GoalType.MINIMIZE
      );
      return Optional.of(solution.getPoint());
    } catch (Exception e) {
      return Optional.empty();
    }
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
    double totalkcal = stapleAndProtein[i][24] + stapleAndProtein[j][24];
    double pkcal = stapleAndProtein[i][22] + stapleAndProtein[j][22];
    double fkcal = stapleAndProtein[i][21] + stapleAndProtein[j][21];
    double ckcal = stapleAndProtein[i][23] + stapleAndProtein[j][23];
    for(int k=0; k<targets.length; k++){
      realize[k] += stapleAndProtein[i][k] + stapleAndProtein[j][k];
    }
    for(int m=0; m<vegetable.length; m++){
      totalkcal +=  vegetable[m][24] * result[m];
      pkcal += vegetable[m][22] * result[m];
      fkcal += vegetable[m][21] * result[m];
      ckcal += vegetable[m][23] * result[m];
      for(int n=0; n<targets.length; n++){
        realize[n] += vegetable[m][n] * result[m];
      }
    }
    System.out.print("総カロリー : " + totalkcal + " (p:" + pkcal + " , f:" + fkcal + " , c:" + ckcal);
    System.out.println(" -> " + String.format("%.1f", pkcal / totalkcal * 100) + "% : " + String.format("%.1f", fkcal / totalkcal * 100) + "% : " + String.format("%.1f", ckcal / totalkcal * 100) + "%");
    return realize;
  }


  //resultを分かりやすく表示
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


  //realizeを分かりやすく表示
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
