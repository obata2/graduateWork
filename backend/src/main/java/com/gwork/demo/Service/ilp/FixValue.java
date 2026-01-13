package com.gwork.demo.Service.ilp;

import com.gwork.demo.Service.nutrient.NutrientService;

public class FixValue {

  // コンストラクタインジェクション
  private DataAdjuster dataAdjusterService;
  public FixValue (DataAdjuster dataAdjusterService) {
    this.dataAdjusterService = dataAdjusterService;
  }

  // 状態に依存する変数  →  i,jを引数とする初期化処理で、状態を保存する
  private double[] adjustedTargets;
  private double[] fixedEnergyValue;
  private double fixedPrice;

  // ゲッター
  public double[] getAdjustedTargets () {return adjustedTargets;}
  public double[] getFixedEnergyValue () {return fixedEnergyValue;}
  public double getFixedPrice () {return fixedPrice;}

  // 初期化処理
  public void init (int stapleIndex, int proteinIndex){
    this.adjustedTargets = adjustTargets(stapleIndex, proteinIndex);
    this.fixedEnergyValue = fixEnergy(stapleIndex, proteinIndex);
    this.fixedPrice = fixPrice(stapleIndex, proteinIndex);
  }
  // --- 主食・肉類を所与とした栄養素目標値の補正 --- 
  private double[] adjustTargets(int stapleIndex, int proteinIndex){
    double[] originalTargets = NutrientService.targets;
    double[][][] nutrientTable = NutrientService.nutrientTable;
    double[] adjustedTargets = new double[originalTargets.length];
    for(int k=0; k<originalTargets.length; k++){
      adjustedTargets[k] = Math.max(0, (originalTargets[k] - nutrientTable[0][stapleIndex][k] - nutrientTable[0][proteinIndex][k]));   //1食分の目安量を使って、目標から固定値を引く
    }
    return adjustedTargets;
  }
  // --- 〃 エネルギーの固定値 --- 
  private double[] fixEnergy(int stapleIndex, int proteinIndex){
    double[][][] nutrientTable = NutrientService.nutrientTable;
    double[] fixedEnergyValue = new double[6];  //"pi-0.13ti" ～ "ci-0.65ti"まで
    int lastColNum = nutrientTable[0][stapleIndex].length - 1;
    for(int k=0; k<fixedEnergyValue.length; k++){
      fixedEnergyValue[k] = nutrientTable[0][stapleIndex][lastColNum - 5 + k] + nutrientTable[0][proteinIndex][lastColNum - 5 + k];
    }
    return fixedEnergyValue;
  }
  // --- 〃 価格の固定値 --- 
  private double fixPrice(int stapleIndex, int proteinIndex){
    double[][] adjustedPrices = dataAdjusterService.getAdjustedPrices();
    double fixedPrice = adjustedPrices[0][stapleIndex] + adjustedPrices[0][proteinIndex];
    return fixedPrice;
  }
}
