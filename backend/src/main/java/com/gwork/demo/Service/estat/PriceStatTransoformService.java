package com.gwork.demo.Service.estat;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwork.demo.Service.nutrient.NutrientService;
import com.gwork.demo.dto.PriceStatDTO;

@Service
public class PriceStatTransoformService {

  // jsonを読み込んで変数を用意し、かつ食材は必要な物のみに絞り込んでおく
  public PriceStatDTO transform (JsonNode jsonNode) {
    PriceStatDTO rawDto = processStatData(jsonNode);    // 必要のない食材までごちゃ混ぜの情報

    PriceStatDTO filteredDto = new PriceStatDTO();
    filteredDto.setDateLabel(filterDateLabelById(rawDto.getDateLabel()));
    filteredDto.setPriceTransition(filterPriceTransitionById(rawDto.getPriceTransition()));
    filteredDto.setPriceLatest(filterPriceLatestById(rawDto.getPriceLatest()));
    return filteredDto;
  }

  // 統計データのJSONを読んで変数の準備
  private PriceStatDTO processStatData(JsonNode jsonNode){
    //idをkeyとしていることに注意
    LinkedHashMap<String, ArrayList<String>> dateLabel = new LinkedHashMap<>();
    LinkedHashMap<String, ArrayList<Integer>> priceTransition = new LinkedHashMap<>();
    LinkedHashMap<String, Integer> priceLatest = new LinkedHashMap<>();
    LinkedHashMap<String, String> formatOfDate = new LinkedHashMap<>();
    PriceStatDTO rawDto = new PriceStatDTO();
    if (jsonNode == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    //日付の形式を整えるための準備
    JsonNode dates = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("CLASS_INF").get("CLASS_OBJ").get(4).get("CLASS"); // 時間軸が配列で保管されている部分
    for (int i=0; i<dates.size(); i++) {
      String dateCode = mapper.convertValue(dates.get(i).get("@code"), String.class);
      String dateName = mapper.convertValue(dates.get(i).get("@name"), String.class);
      formatOfDate.put(dateCode, dateName);
    }
    //ここから変数の用意
    JsonNode prices = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("DATA_INF").get("VALUE"); // 食材の価格が配列で保管されている部分
    for (int i=0; i<prices.size(); i++) {
      String id = mapper.convertValue(prices.get(i).get("@cat02"), String.class);
      String dateCode = mapper.convertValue(prices.get(i).get("@time"), String.class); 
      String pri = mapper.convertValue(prices.get(i).get("$"), String.class);
      //価格推移の配列への追加
      if(!priceTransition.containsKey(id)){
        priceTransition.put(id, new ArrayList<>());
      }
      if(pri.matches("\\d+")){
        priceTransition.get(id).add(Integer.parseInt(pri));
      }else{
        priceTransition.get(id).add(null);
      }
      //最新の価格を追加
      if(pri.matches("\\d+") && !priceLatest.containsKey(id)){
        priceLatest.put(id, Integer.parseInt(pri));
      }
      //日付ラベルの追加
      if(!dateLabel.containsKey(id)){
        dateLabel.put(id, new ArrayList<>());
      }
      dateLabel.get(id).add(formatOfDate.get(dateCode));
    }
    rawDto.setDateLabel(dateLabel);
    rawDto.setPriceLatest(priceLatest);
    rawDto.setPriceTransition(priceTransition);
    return rawDto;
  }

  private static String[][] id = NutrientService.id;

  // --- 栄養表示に存在する食材だけに絞り込む ---
  private static LinkedHashMap<String, ArrayList<String>> filterDateLabelById(LinkedHashMap<String, ArrayList<String>> dateLabel){
    LinkedHashMap<String, ArrayList<String>> filteredDateLabel = new LinkedHashMap<>();
    for(int i=0; i<id.length; i++){
      for(int j=0; j<id[i].length; j++){
        String key = id[i][j];
        if(dateLabel.containsKey(key)){
          ArrayList<String> value = dateLabel.get(key);
          filteredDateLabel.put(key, value);
        }
      }
    }
    return filteredDateLabel;
  }

  private static LinkedHashMap<String, ArrayList<Integer>> filterPriceTransitionById(LinkedHashMap<String, ArrayList<Integer>> priceTransition){
    LinkedHashMap<String, ArrayList<Integer>> filteredPriceTransition = new LinkedHashMap<>();
    for(int i=0; i<id.length; i++){
      for(int j=0; j<id[i].length; j++){
        String key = id[i][j];
        if(priceTransition.containsKey(key)){
          ArrayList<Integer> value = priceTransition.get(key);
          filteredPriceTransition.put(key, value);
        }
      }
    }
    return filteredPriceTransition;
  }

  private static LinkedHashMap<String, Integer> filterPriceLatestById(LinkedHashMap<String, Integer> priceLatest){
    LinkedHashMap<String, Integer> filteredPriceLatest = new LinkedHashMap<>();
    for(int i=0; i<id.length; i++){
      for(int j=0; j<id[i].length; j++){
        String key = id[i][j];
        if(priceLatest.containsKey(key)){
          Integer value = priceLatest.get(key);
          filteredPriceLatest.put(key, value);
        }
      }
    }
    return filteredPriceLatest;
  }
}
