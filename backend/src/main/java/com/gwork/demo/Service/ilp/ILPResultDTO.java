package com.gwork.demo.Service.ilp;

import java.util.LinkedHashMap;

public class ILPResultDTO {
  public int id;
  public LinkedHashMap<String, String> ingredients;                 //{食材名, グラム数}
  public int totalPrice;
  public int totalKcal;
  public double[] pfcKcal;
  public double[] calculatedNutrients;                              //{栄養素名, 算出値}
  public LinkedHashMap<String, double[]> nutrientsContriRate;       //{食材名, [栄養素の寄与率の配列]}
  public LinkedHashMap<String, double[]> pfcContriRate;             //{食材名, [pfcの寄与率の配列]}
  public int[] solutionVector;

  // setter
  public void setId(int id){ this.id = id; }
  public void setIngredients(LinkedHashMap<String, String> ingredients){ this.ingredients = ingredients; }
  public void setTotalPrice(int totalPrice){ this.totalPrice = totalPrice; }
  public void setTotalKcal(int totalKcal){ this.totalKcal = totalKcal; }
  public void setpfcKcal(double[] pfcKcal){ this.pfcKcal = pfcKcal; }
  public void setCalculatedNutrients(double[] calculatedNutrients){ this.calculatedNutrients = calculatedNutrients; }
  public void setNutrientsContriRate(LinkedHashMap<String, double[]> nutrientsContriRate){ this.nutrientsContriRate = nutrientsContriRate; }
  public void setPfcContriRate(LinkedHashMap<String, double[]> pfcContriRate){ this.pfcContriRate = pfcContriRate; }
  public void setSolutionVector(int[] solutionVector){ this.solutionVector = solutionVector; }

  // getter
  public int getId(){ return this.id; }
  public LinkedHashMap<String, String> getIngredients(){ return this.ingredients; }
  public int getTotalPrice(){ return this.totalPrice; }
  public int getTotalKcal(){ return this.totalKcal; }
}
