package com.gwork.demo.dto;

import java.util.Map;

import lombok.Data;

// フロントから送られてくるJSON(favoritesに保存したいもの)から、不要なフィールドを取り除くためのDTO
@Data
public class FavoritesRequestDTO {
  private String userId;

  private Integer menuId;
	
  private Map<String, String> ingredients;

  private Integer totalPrice;
  
  private Integer totalKcal;
  
  private Integer[] pfcKcal;
  
  private double[] calculatedNutrients;
  
  private Map<String, double[]> nutrientsContriRate;
  
  private Map<String, double[]> pfcContriRate;
  
  private String menuName;
  
  private Map<String, String> dishName;
  
  private Map<String, String> instructions;
  
  private Map<String, String> seasonings;
  
  private String memo;
}