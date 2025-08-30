package com.gwork.demo.util;

import java.util.LinkedHashMap;

public class ILPResult {
  public LinkedHashMap<String, String> ingredients;
  public int totalPrice;
  public int totalKcal;
  public int[] pfcKcal;
  public LinkedHashMap<String, Double> nutrients;
  public int[] solutionVector;
  
  public void setIngredients(LinkedHashMap<String, String> ingredients){ this.ingredients = ingredients; }
  public void setTotalPrice(int totalPrice){ this.totalPrice = totalPrice; }
  public void setTotalKcal(int totalKcal){ this.totalKcal = totalKcal; }
  public void setpfcKcal(int[] pfcKcal){ this.pfcKcal = pfcKcal; }
  public void setNutrients(LinkedHashMap<String, Double> nutrients){ this.nutrients = nutrients; }
  public void setSolutionVector(int[] solutionVector){ this.solutionVector = solutionVector; }
}
