package com.gwork.demo.Service.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gwork.demo.Service.estat.ProcessEstatService;
import com.gwork.demo.Service.nutrient.NutrientService;

public class PriceDataFilterService {
  private static LinkedHashMap<String, ArrayList<String>> dateLabel = ProcessEstatService.dateLabel; 
  private static LinkedHashMap<String, ArrayList<Integer>> priceTransition = ProcessEstatService.priceTransition;
  private static String[] sAndPId = NutrientService.sAndPId;
  private static String[] vegId = NutrientService.vegId;
  private static String[] allId;

  static {
    allId = new String[sAndPId.length + vegId.length];
    System.arraycopy(sAndPId, 0, allId, 0, sAndPId.length);
    System.arraycopy(vegId, 0, allId, sAndPId.length, vegId.length);
  }

  // --- allIdに存在するidだけに絞り込む ---
  public static Map<String, ArrayList<String>> filterDateLabelById(){
    LinkedHashMap<String, ArrayList<String>> filteredDateLabel = new LinkedHashMap<>();
    for(int i=0; i<allId.length; i++){
      String key = allId[i];
      if(dateLabel.containsKey(key)){
        ArrayList<String> value = dateLabel.get(key);
        filteredDateLabel.put(key, value);
      }
    }
    return filteredDateLabel;
  }

  // --- allIdに存在するidだけに絞り込む ---
  public static Map<String, ArrayList<Integer>> filterPriceTransitionById(){
    LinkedHashMap<String, ArrayList<Integer>> filteredPriceTransition = new LinkedHashMap<>();
    for(int i=0; i<allId.length; i++){
      String key = allId[i];
      if(priceTransition.containsKey(key)){
        ArrayList<Integer> value = priceTransition.get(key);
        filteredPriceTransition.put(key, value);
      }
    }
    return filteredPriceTransition;
  }
}
