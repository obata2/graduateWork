package com.gwork.demo.Service.estat;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProcessEstatService {
  //idをkeyとしていることに注意
  public LinkedHashMap<String, ArrayList<String>> dateLabel = new LinkedHashMap<>(); 
  public LinkedHashMap<String, ArrayList<Integer>> priceTransition = new LinkedHashMap<>();
  public LinkedHashMap<String, Integer> priceLatest = new LinkedHashMap<>();                  //←たぶんいらない、最後尾をつかえばいいので

  private LinkedHashMap<String, String> formatOfDate = new LinkedHashMap<>();
  private JsonNode jsonNode;

  //コンストラクタで変数を準備する
  public ProcessEstatService(String cdTimeFrom, String cdTimeTo, String cdArea){
    /* ↓APIを叩いてとってくる処理
    this.jsonNode =  downloadFromEstat(cdTimeFrom, cdTimeTo, cdArea);
    processStatData(this.jsonNode);
    // ↓cacheに書き込む
    savePriceStatToCache(cdArea);
    */

    //  ↓cacheから読み込む処理
    this.jsonNode = readPriceStatFromCache(cdArea);
    processStatData(this.jsonNode);
    
  }


  // --- 統計データをAPIで取ってくる ---
  public JsonNode downloadFromEstat(String cdTimeFrom, String cdTimeTo, String cdArea) {
    final String BASE_URL = "http://api.e-stat.go.jp/rest/3.0/app/json/getStatsData"; // 小売物価統計のurl
    final String appId = "ea754c2b09a9735a2f2ea4c33874796724e1e347"; // 自分のAPIキー
    final String statsDataId = "0003421913"; // 統計表ID
    //final String cdTimeFrom = "2024001212"; // 2024年12月からの
    //final String cdArea = "23100"; // 名古屋における
    
    final String cdCat02From = "01001"; // うるち米(コシヒカリ)から
    final String cdCat02To = "01584"; // パイナップルまで

    final String LIMIT = "100000"; // 取得件数上限
    JsonNode jsonResult = null; // 初期化
    try {
      System.out.println("APIを叩いて統計情報をとる");
      RestTemplate restTemplate = new RestTemplate();
      String apiUrl = String.format(
        "%s?appId=%s&statsDataId=%s&cdTimeFrom=%s&cdArea=%s&cdCat02From=%s&cdCat02To=%s&limit=%s",
        BASE_URL, appId, statsDataId, cdTimeFrom, cdArea, cdCat02From, cdCat02To, LIMIT);
      String strResult = restTemplate.getForObject(apiUrl, String.class);
      ObjectMapper mapper = new ObjectMapper();
      jsonResult = mapper.readTree(strResult);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonResult;
  }

  // --- 取ってきたデータをCachedPriceStatに書き込む ---
  private void savePriceStatToCache(String cdArea) {
    final String FILE_PATH;
    if(cdArea.equals("23100")){
      FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedMainPriceStat.json";
    }else{
      FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedCompPriceStat.json";
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(FILE_PATH), this.jsonNode);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // --- CachedPriceStatを読み込む ---
  private JsonNode readPriceStatFromCache(String cdArea) {
    JsonNode jsonNode = null;
    final String FILE_PATH;
    if(cdArea.equals("23100")){
      FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedMainPriceStat.json";
    }else{
      FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedCompPriceStat.json";
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      jsonNode = mapper.readTree(new File(FILE_PATH)); // ファイルからJsonNodeを読み込む(元データ)
    } catch (IOException e) {
      e.printStackTrace();
    }
    return jsonNode;
  }

  // --- 変数の準備 ---
  private void processStatData(JsonNode jsonNode){
    if (jsonNode == null) {
      return;
    }
    ObjectMapper mapper = new ObjectMapper();
    /*idと食材名の関係を保持する
    JsonNode ingredients = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("CLASS_INF").get("CLASS_OBJ").get(2).get("CLASS"); // 食材名が配列で保管されている部分
    for (int i=0; i<ingredients.size(); i++) {
      String ing = mapper.convertValue(ingredients.get(i).get("@name"), String.class).substring(5); // 前5文字(1001　 等のid部分)は切り取り
      String id = mapper.convertValue(ingredients.get(i).get("@code"), String.class);
      if (!ing.contains("調査終了")) {
        //idAndIngName.put(id, ing);
      }
    }*/
    //日付の形式を整えるための準備
    JsonNode dates = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("CLASS_INF").get("CLASS_OBJ").get(4).get("CLASS"); // 時間軸が配列で保管されている部分
    for (int i=0; i<dates.size(); i++) {
      String dateCode = mapper.convertValue(dates.get(i).get("@code"), String.class);
      String dateName = mapper.convertValue(dates.get(i).get("@name"), String.class);
      this.formatOfDate.put(dateCode, dateName);
    }
    //ここから変数の用意
    JsonNode prices = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("DATA_INF").get("VALUE"); // 食材の価格が配列で保管されている部分
    for (int i=0; i<prices.size(); i++) {
      String id = mapper.convertValue(prices.get(i).get("@cat02"), String.class);
      String dateCode = mapper.convertValue(prices.get(i).get("@time"), String.class); 
      String pri = mapper.convertValue(prices.get(i).get("$"), String.class);
      //価格推移の配列への追加
      if(!priceTransition.containsKey(id)){
        this.priceTransition.put(id, new ArrayList<>());
      }
      if(pri.matches("\\d+")){
        this.priceTransition.get(id).add(Integer.parseInt(pri));
      }else{
        this.priceTransition.get(id).add(null);
      }
      //最新の価格を追加
      if(pri.matches("\\d+") && !priceLatest.containsKey(id)){
        this.priceLatest.put(id, Integer.parseInt(pri));
      }
      //日付ラベルの追加
      if(!dateLabel.containsKey(id)){
        this.dateLabel.put(id, new ArrayList<>());
      }
      this.dateLabel.get(id).add(this.formatOfDate.get(dateCode));
    }
  }


  // cahcedに書き込まれているJSONの元データを確認
  public JsonNode checkJSON() {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedPriceStat.json";
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(new File(FILE_PATH)); // ファイルからJsonNodeを読み込む(元データ)
      return jsonNode;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}