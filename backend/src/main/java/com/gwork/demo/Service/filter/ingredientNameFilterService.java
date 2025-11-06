package com.gwork.demo.Service.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.gwork.demo.Service.nutrient.NutrientService;

public class ingredientNameFilterService {
  public static LinkedHashMap<String, String> nameAndId = new LinkedHashMap<>();        //お肉の部位をある部位に代表させた、{食材名:id}
  public static LinkedHashMap<String, String> idAndPriceUnit = new LinkedHashMap<>();   //価格が何g単位かを示す辞書

  /*
  private static String[] sAndPId = NutrientService.sAndPId;
  private static String[] sAndPName = NutrientService.sAndPName;
  private static double[] priceUnitOfSAndP = NutrientService.priceUnitOfSAndP;
  private static String[] vegId = NutrientService.vegId;
  private static String[] vegName = NutrientService.vegName;
  private static double[] priceUnitOfVeg = NutrientService.priceUnitOfVeg;
  */
  private static String[][] idList = NutrientService.id;
  private static String[][] nameList = NutrientService.name;
  private static String[][] priceUnitQtyForView = NutrientService.priceUnitQtyForView; 
  
  static {
    ArrayList<String> used = new ArrayList<>();
    //主食・肉類と、野菜類の2パターン
    for(int i=0; i<2; i++){
      for(int j=0; j<idList[i].length; j++){
        String id = idList[i][j];
        String name = nameList[i][j];
        String priceUnit = priceUnitQtyForView[i][j];
        if(!used.contains(id)){
          nameAndId.put(name, id);
          idAndPriceUnit.put(id, priceUnit);
          used.add(id);
        }
      }
    }
  }
}