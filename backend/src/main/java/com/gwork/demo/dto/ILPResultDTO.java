package com.gwork.demo.dto;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

// ilpの計算結果と関連する情報を保持しておく
@Data
public class ILPResultDTO {
  private int resultId;
  private LinkedHashMap<String, String> ingredients;
  private int totalPrice;
  private int totalKcal;
  private double[] pfcKcal;
  private double[] calculatedNutrients;
  private LinkedHashMap<String, double[]> nutrientsContriRate;
  private LinkedHashMap<String, double[]> pfcContriRate;
  @JsonIgnore
  private int[] solutionVector;         // このDTOはフロントにも渡されるので、不要なsolutionVectorは隠して渡すようにする
}
