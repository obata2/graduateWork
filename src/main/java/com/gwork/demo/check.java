package com.gwork.demo;

import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.NutrientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;

public class check {
  public static void main(String args[]){
    
    JsonProcesserService jsonProcesserService = new JsonProcesserService();
    double[] prices = jsonProcesserService.getPri();
    NutrientService nutrientService = new NutrientService();
    double[] targets = nutrientService.getTargets();
    double[][] nutrients = transpose(nutrientService.getNutrients());    //ここの渡し方が間違っている、横ではなく縦にする必要があるので、転置する
    

    /*
    //確認用データ  (2x+3y の最大化、制約条件は 4x+2y>=1 && 3x+5y>=2 && 非負)
    double[] prices = {2.0, 3.0};
    double[] targets = {1.0, 2.0};
    double[][] nutrients = transpose(new double[][]{{4.0, 3.0}, {2.0, 5.0}});
    */
     

    // 最小化する目的関数   Σ(価格*数量)
    LinearObjectiveFunction objective = new LinearObjectiveFunction(prices, 0);

    //制約条件  Σ(栄養*数量)>=目標値  ：栄養素ごとに制約を追加
    Collection<LinearConstraint> constraints = new ArrayList<>();
    for (int i = 0; i < targets.length; i++) {
        constraints.add(new LinearConstraint(nutrients[i], Relationship.GEQ, targets[i]));
    }
    //非負制約は明示的に加える
    for (int i = 0; i < prices.length; i++) {
        double[] coeff = new double[prices.length];
        coeff[i] = 1;
        constraints.add(new LinearConstraint(coeff, Relationship.GEQ, 0));
    }

    /*
    //制約条件の表示
    for (LinearConstraint lc : constraints) {
      System.out.println(formatConstraint(lc));
    }
      */

    
    // 解く
    SimplexSolver solver = new SimplexSolver();
    PointValuePair solution = solver.optimize(
      objective,                                    //目的関数
      new LinearConstraintSet(constraints),         //制約条件
      GoalType.MINIMIZE                            //最小化する
    );

    double[] result = solution.getPoint();
    System.out.println("合計価格: " + solution.getValue());
    System.out.println("選ばれた量: " + Arrays.toString(result));

    double[] realized = new double[targets.length];
    for(int i=0; i<result.length; i++){
      for(int j=0; j<nutrients.length; j++){
        realized[j] += result[i] * nutrients[j][i];
      }
    }
    System.out.println(realized.length + " " + targets.length);
    System.out.println("目標 : 実現値");
    for(int i=0; i<realized.length; i++){
      System.out.println(targets[i] + " : " + realized[i]);
    }
  }


  //二次元配列を転置する
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


  //制約条件を数式に見えるように
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
