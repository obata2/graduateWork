package com.gwork.demo.Service.estat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwork.demo.dto.EstatAPIParamDTO;

@Service
public class MetaDataTransformService {
  private final EstatClientService estatClientService;

  // コンストラクタインジェクションでEstatClientのインスタンスを受け取る
  public MetaDataTransformService(EstatClientService estatClientService) {
    this.estatClientService = estatClientService;
  }

  public EstatAPIParamDTO transform () {
    //JsonNode jsonNode = this.estatClientService.fetchStatMetaData();    //←APIを叩いてメタデータを見る
    JsonNode jsonNode = this.estatClientService.readMetaDataFromCache();  //←キャッシュファイルからメタデータを見る
    EstatAPIParamDTO estatAPIParamDTO = processMetaData(jsonNode);
    return estatAPIParamDTO;
  }


  // --- メタデータを読んでパラメータに使えるものを用意 ---
  private EstatAPIParamDTO processMetaData(JsonNode metaData){
    EstatAPIParamDTO estatAPIParamDTO = new EstatAPIParamDTO();
    LinkedHashMap<String, String> areaParam = new LinkedHashMap<>();
    areaParam.put("なし", null);
    LinkedHashMap<String, String> timeFromParam = new LinkedHashMap<>();
    String[] areaNamesArray = {"札幌市", "青森市", "盛岡市", "仙台市", "秋田市", "山形市", "福島市", "水戸市", "宇都宮市", "前橋市", "さいたま市", "千葉市", "特別区部", "八王子市", "横浜市", "新潟市", "富山市", "金沢市", "福井市", "甲府市", "長野市", "岐阜市", "静岡市", "名古屋市", "津市", "大津市", "京都市", "大阪市", "神戸市", "奈良市", "和歌山市", "鳥取市", "松江市", "岡山市", "広島市", "山口市", "徳島市", "高松市", "松山市", "高知市", "福岡市", "佐賀市", "長崎市", "熊本市", "大分市", "宮崎市", "鹿児島市", "那覇市"};     //47都道府県の各県庁所在地、東京のみ2種類

    if (metaData == null) {
      return null;
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
      }
    }
    estatAPIParamDTO.setAreaParam(areaParam);
    estatAPIParamDTO.setTimeFromParam(timeFromParam);
    return estatAPIParamDTO;
  }
}
