package com.gwork.demo.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;


@Service
public class JsonProcesserService {
    Map<String, String> idAndIng = new LinkedHashMap<>();
    Map<String, Integer> idAndPri = new LinkedHashMap<>();
    Map<String, Integer> ingAndPri = new LinkedHashMap<>();

    //JSONのダウンロード
    public JsonNode downloadJSON() {
        final String BASE_URL = "http://api.e-stat.go.jp/rest/3.0/app/json/getStatsData";  //小売物価統計のurl
        final String appId = "ea754c2b09a9735a2f2ea4c33874796724e1e347"; //自分のAPIキー
        final String statsDataId = "0003421913";   //統計表ID
        final String cdTimeFrom = "2024001212";         //2024年12月からの
        final String cdArea = "23100";        //名古屋における

        /*
        final String cdCat02From = "01401";      //キャベツから
        final String cdCat02To = "01443";      //しめじまで
        */

        
        final String cdCat02From = "01001";      //うるち米(コシヒカリ)から
        final String cdCat02To = "01584";      //パイナップルまで
        
        final String LIMIT = "1000";     //取得件数上限
        JsonNode jsonResult = null; // 初期化
        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = String.format(
                "%s?appId=%s&statsDataId=%s&cdTimeFrom=%s&cdArea=%s&cdCat02From=%s&cdCat02To=%s&limit=%s",
                BASE_URL, appId, statsDataId, cdTimeFrom, cdArea, cdCat02From, cdCat02To, LIMIT
            );
            String strResult = restTemplate.getForObject(apiUrl, String.class);
            ObjectMapper mapper = new ObjectMapper();
            jsonResult = mapper.readTree(strResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResult;
    }


    //データをCachedDataに書き込む
    public void saveJSONToCache(JsonNode jsonNode) {
        final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\CachedData.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(FILE_PATH), jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //CachedDataを読み込み、変数()を用意
    public void readJSONFromCache() {
        final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\CachedData.json";
        Map<String, String> idAndIng = new LinkedHashMap<>();
        Map<String, Integer> idAndPri = new LinkedHashMap<>();
        Map<String, Integer> ingAndPri = new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(new File(FILE_PATH));   //ファイルからJsonNodeを読み込む(元データ)
            //食材名を取得してkeyにセット
            JsonNode ingredients = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("CLASS_INF").get("CLASS_OBJ").get(2).get("CLASS");    //食材名が配列で保管されている部分
            for(int i=0; i<ingredients.size(); i++){
                String ing = mapper.convertValue(ingredients.get(i).get("@name"), String.class);
                String id = mapper.convertValue(ingredients.get(i).get("@code"), String.class);
                if(!ing.contains("調査終了")){
                    ingAndPri.put(ing, null);
                    idAndPri.put(id, null);
                    idAndIng.put(id, ing);
                }
            }
            //価格を取得してvalueにセット
            JsonNode prices = jsonNode.get("GET_STATS_DATA").get("STATISTICAL_DATA").get("DATA_INF").get("VALUE"); //食材の価格が配列で保管されている部分
            for(int i=0; i<prices.size(); i++){
                //価格が数字表記であり、かつ日時が最新のものだけを追加していく
                String id = mapper.convertValue(prices.get(i).get("@cat02"), String.class);
                String pri = mapper.convertValue(prices.get(i).get("$"), String.class);
                if(pri.matches("\\d+") && idAndPri.get(id) == null){
                    idAndPri.put(id, Integer.parseInt(pri));
                }
            }
            //価格がnullのままではダメなので、適当な値を
            for(String id : idAndPri.keySet()){
                String ing = idAndIng.get(id);
                Integer pri = idAndPri.get(id);
                if(pri != null){
                    ingAndPri.put(ing, pri);
                }else{
                    idAndPri.put(id, 99999);
                    ingAndPri.put(ing, 99999);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.idAndIng = idAndIng;
        this.idAndPri = idAndPri;
        this.ingAndPri = ingAndPri;
    }
    

    //価格の配列を返す
    public double[] getPri(){
        readJSONFromCache();
        double[] prices = new double[this.ingAndPri.size()];
        int i = 0;
        for(Integer value : this.ingAndPri.values()){
            prices[i] = value;
            i++;
        }
        return prices;
    }

    //{食材：価格}のmapを返す
    public Map<String, Integer> getIngAndPri(){
        readJSONFromCache();
        return this.ingAndPri;
    }

    
    //メタデータの確認
    public String getMetaData() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String META_DATA_URL = "http://api.e-stat.go.jp/rest/3.0/app/json/getMetaInfo?appId=ea754c2b09a9735a2f2ea4c33874796724e1e347&statsDataId=0003421913";
            String metaData = restTemplate.getForObject(META_DATA_URL, String.class);       //APIでメタデータを取得
            String decodedData = StringEscapeUtils.unescapeJava(metaData);      // Unicodeエスケープをデコード
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(decodedData);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);        // JSONを整形して表示
            return prettyJson.replace("\n", "<br>");
        } catch (Exception e) {
            return "データ取得エラー: " + e.getMessage();
        }
    }


    //JSONの元データを確認
    public JsonNode checkJSON(){
        final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\CachedData.json";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(new File(FILE_PATH));   //ファイルからJsonNodeを読み込む(元データ)
            return jsonNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}