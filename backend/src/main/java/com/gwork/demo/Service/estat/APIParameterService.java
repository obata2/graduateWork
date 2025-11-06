package com.gwork.demo.Service.estat;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIParameterService {
  private static LinkedHashMap<String, String> areaParam;
  private static LinkedHashMap<String, String> timeFromParam;
  private static LinkedHashMap<String, String> timeToParam;

  private static String[] areaNamesArray = {"札幌市", "青森市", "盛岡市", "仙台市", "秋田市", "山形市", "福島市", "水戸市", "宇都宮市", "前橋市", "さいたま市", "千葉市", "特別区部", "八王子市", "横浜市", "新潟市", "富山市", "金沢市", "福井市", "甲府市", "長野市", "岐阜市", "静岡市", "名古屋市", "津市", "大津市", "京都市", "大阪市", "神戸市", "奈良市", "和歌山市", "鳥取市", "松江市", "岡山市", "広島市", "山口市", "徳島市", "高松市", "松山市", "高知市", "福岡市", "佐賀市", "長崎市", "熊本市", "大分市", "宮崎市", "鹿児島市", "那覇市"};     //47都道府県の各県庁所在地、東京のみ2種類

  static {
    System.out.println("APIパラメータを、メタデータから用意します");

    JsonNode metaData = readMetaDataFromCache();
    processMetaData(metaData);
    /*
    System.out.println(areaParam.size() + " : " + areaParam);
    System.out.println(timeToParam);
    System.out.println(timeFromParam);
    */
  }

  public static LinkedHashMap<String, Object> getAPIParams(){
    LinkedHashMap<String, Object> APIParams = new LinkedHashMap<>();
    APIParams.put("areaParam", areaParam);
    APIParams.put("timeFromParam", timeFromParam);
    APIParams.put("timeToParam", timeToParam);
    return APIParams;
  }

  // --- メタデータをAPIで取得 ---
  public static JsonNode getMetaData() {
    try {
      System.out.println("APIを叩いてメタデータを取得");
      RestTemplate restTemplate = new RestTemplate();
      String META_DATA_URL = "http://api.e-stat.go.jp/rest/3.0/app/json/getMetaInfo?appId=ea754c2b09a9735a2f2ea4c33874796724e1e347&statsDataId=0003421913";
      String metaData = restTemplate.getForObject(META_DATA_URL, String.class); // APIでメタデータを取得
      String decodedData = StringEscapeUtils.unescapeJava(metaData); // Unicodeエスケープをデコード
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(decodedData);
      return jsonNode;
    } catch (Exception e) {
      return null;
    }
  }

  // --- 取ってきたメタデータをキャッシュに書き込む ---
  public static void saveMetaDataToCache(JsonNode jsonNode) {
    System.out.println("メタデータをキャッシュから読み込むよ");
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedMetaData.json";
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(FILE_PATH), jsonNode);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // --- キャッシュから読み込む ---
  public static JsonNode readMetaDataFromCache() {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedMetaData.json";
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(new File(FILE_PATH)); // ファイルからJsonNodeを読み込む(元データ)
      return jsonNode;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // --- メタデータを読んでパラメータに使えるものを用意 ---
  private static void processMetaData(JsonNode metaData){
    areaParam = new LinkedHashMap<>();
    timeToParam = new LinkedHashMap<>();
    timeFromParam = new LinkedHashMap<>();

    if (metaData == null) {
      return;
    }
    ObjectMapper mapper = new ObjectMapper();
    // 地域名と地域コード名を保持する
    JsonNode area = metaData.get("GET_META_INFO").get("METADATA_INF").get("CLASS_INF").get("CLASS_OBJ").get(3).get("CLASS"); // 地域コード名と地域が配列で保管されている部分
    List<String> areaNamesList = Arrays.asList(areaNamesArray);
    for (int i=0; i<area.size(); i++) {
      String areaCode = mapper.convertValue(area.get(i).get("@code"), String.class);
      String areaName = mapper.convertValue(area.get(i).get("@name"), String.class);
      if(areaNamesList.contains(areaName)){   // 指定した地域名のみ残す
        areaParam.put(areaName, areaCode);
      }
    }

    // 時間軸の名称とコードを保持する
    JsonNode dateLabel = metaData.get("GET_META_INFO").get("METADATA_INF").get("CLASS_INF").get("CLASS_OBJ").get(4).get("CLASS");; // 時間軸のコード名と名称が配列で保管されている部分
    for (int i=0; i<dateLabel.size(); i++) {
      String timeCode = mapper.convertValue(dateLabel.get(i).get("@code"), String.class);
      String timeName = mapper.convertValue(dateLabel.get(i).get("@name"), String.class);
      if(timeName.contains("年1月")){     // 各年1月だけ残す
        timeFromParam.put(timeName, timeCode);
        timeToParam.put(timeName, timeCode);
      }
    }
  }
}
