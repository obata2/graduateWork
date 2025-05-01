package com.gwork.demo.Service;


import org.springframework.stereotype.Service;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;

@Service
public class LinearService {

    public void SolveLinearProblem () {
        // 価格の配列
        JsonProcesserService jsonProcesserService = new JsonProcesserService();
        double[] prices = jsonProcesserService.getPri();
        System.out.println("価格テーブル : " + Arrays.toString(prices));

        // 最小化する目的関数
        LinearObjectiveFunction objective = new LinearObjectiveFunction(prices, 0);

        // 制約条件：各栄養素について Σ(ni*qi) >= bi を追加
        NutrientService nutrientService = new NutrientService();
        Collection<LinearConstraint> constraints = new ArrayList<>();

        // 例：栄養素ごとに制約を追加（ここでは仮のデータ）
        double[][] nutritionMatrix = nutrientService.getNutrients();        //食品中の栄養素テーブル
        double[] targets = nutrientService.getTargets();                    //摂取目標のテーブル
        System.out.print("栄養素テーブル : ");
        for(int i=0; i<nutritionMatrix.length; i++){
            System.out.print(Arrays.toString(nutritionMatrix[i]));
        }
        System.out.println("");
        System.out.println("目標値テーブル : " + Arrays.toString(targets));


        for (int i = 0; i < targets.length; i++) {
            constraints.add(new LinearConstraint(nutritionMatrix[i], Relationship.GEQ, targets[i]));
        }

        // 非負制約は内部で自動処理されないため、明示的に加える
        for (int i = 0; i < nutritionMatrix.length; i++) {
            double[] coeff = new double[28];
            coeff[i] = 1;
            constraints.add(new LinearConstraint(coeff, Relationship.GEQ, 0));
        }

        /*
        // 解く
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(
                new LinearConstraintSet(constraints),
                objective,
                GoalType.MINIMIZE,
                new NonNegativeConstraint(true)
        );

        double[] result = solution.getPoint();
        //System.out.println("合計価格: " + solution.getValue());
        //System.out.println("選ばれた量: " + Arrays.toString(result));
        return result;
        */
    }
}
