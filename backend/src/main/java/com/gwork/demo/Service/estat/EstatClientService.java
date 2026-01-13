package com.gwork.demo.Service.estat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EstatClientService {
  @Value("${estat.api.key}")
  private String appId; // 自分のAPIキー

  // --- 統計データをAPIで取ってくる ---
  public JsonNode fetchPriceStat (String cdArea) {
    final String BASE_URL = "http://api.e-stat.go.jp/rest/3.0/app/json/getStatsData"; // 小売物価統計のurl
    final String statsDataId = "0003421913"; // 統計表ID

    final String cdTimeFrom = "2023001212"; // 2023年12月からの
    //final String cdArea = "23100"; // 名古屋における
    
    final String cdCat02From = "01001"; // うるち米(コシヒカリ)から
    final String cdCat02To = "01584"; // パイナップルまで

    final String LIMIT = "100000"; // 取得件数上限
    JsonNode jsonResult = null; // 初期化
    try {
      System.out.println("APIを叩いて統計情報をとってきます");
      RestTemplate restTemplate = new RestTemplate();
      String apiUrl = String.format(
        "%s?appId=%s&statsDataId=%s&cdTimeFrom=%s&cdArea=%s&cdCat02From=%s&cdCat02To=%s&limit=%s",
        BASE_URL, this.appId, statsDataId, cdTimeFrom, cdArea, cdCat02From, cdCat02To, LIMIT);
      String strResult = restTemplate.getForObject(apiUrl, String.class);
      ObjectMapper mapper = new ObjectMapper();
      jsonResult = mapper.readTree(strResult);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonResult;
  }

  // --- メタデータをAPIで取ってくる ---
  public JsonNode fetchStatMetaData () {
    final String BASE_URL = "http://api.e-stat.go.jp/rest/3.0/app/json/getMetaInfo";  // メタデータ取得のurl
    final String statsDataId = "0003421913"; // 統計表ID
    JsonNode jsonResult = null; // 初期化
    try {
      System.out.println("APIを叩いてメタデータを取得します");
      RestTemplate restTemplate = new RestTemplate();
      String apiUrl = String.format(
        "%s?appId=%s&statsDataId=%s",
        BASE_URL, this.appId, statsDataId);
      String strResult = restTemplate.getForObject(apiUrl, String.class);
      ObjectMapper mapper = new ObjectMapper();
      jsonResult = mapper.readTree(strResult);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonResult;
  }


  // ↓キャッシュファイルを使ってAPI呼び出しを回避する
  // --- 取ってきたメタデータをキャッシュに書き込む ---
  public void saveMetaDataToCache(JsonNode jsonNode) {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedMetaData.json";
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(FILE_PATH), jsonNode);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // --- キャッシュから読み込む ---
  public JsonNode readMetaDataFromCache() {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedMetaData.json";
    System.out.println("キャッシュからメタデータを読み込みます");
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
