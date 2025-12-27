package com.gwork.demo.Service.estat;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gwork.demo.Service.filter.IngredientNameFilterService;
import com.gwork.demo.Service.nutrient.NutrientService;
import com.gwork.demo.dto.EstatAPIParamDTO;
import com.gwork.demo.dto.PriceLatestRowDTO;
import com.gwork.demo.dto.PriceStatDTO;
import com.gwork.demo.model.PricesLatest;
import com.gwork.demo.repository.PricesLatestRepository;

@Service
public class EstatService {

  private final EstatClientService estatClientService;
  private final PriceStatTransoformService priceStatTransoformService;
  private final MetaDataTransformService metaDataTransformService;
  private final PricesLatestRepository repository;

  // コンストラクタインジェクションでTransformのインスタンスを受け取る
  public EstatService(EstatClientService estatClientService, PriceStatTransoformService priceStatTransoformService, MetaDataTransformService metaDataTransformService, PricesLatestRepository repository) {
    this.estatClientService = estatClientService;
    this.priceStatTransoformService = priceStatTransoformService;
    this.metaDataTransformService = metaDataTransformService;
    this.repository = repository;
  }
  
  /*
  // API経由で価格統計を取得し、upsertする          ←もうupsertの必要はないので、単純なupdateでprices_latestテーブルを更新するように変えたい
  public void savePricesLatest (String userId) {
    PriceStatDTO filterPriceStatDTO = estatTransoformService.transform();
    LinkedHashMap<String, String> idAndName = NutrientService.idAndName;
    LinkedHashMap<String, String> idAndPriceUnitQty = NutrientService.idAndPriceUnitQty;
    for(String ingredientId : filterPriceStatDTO.getPriceLatest().keySet()){
      String ingredientName = idAndName.get(ingredientId);
      Integer priceLatest = filterPriceStatDTO.getPriceLatest().get(ingredientId);
      String priceUnitQty = idAndPriceUnitQty.get(ingredientId);
      System.out.println("upsertします");
      repository.upsertIfNotFixed(userId, ingredientId, ingredientName, priceLatest, priceUnitQty);
    }
  }*/

  // API経由で価格統計を取得し、DTOで値を返す       ←引数としてcdAreaとcdTimeFromが欲しい
  public PriceStatDTO fetchPriceStat (String cdArea) {
    JsonNode jsonNode = estatClientService.fetchPriceStat(cdArea);
    PriceStatDTO priceStatDTO = priceStatTransoformService.transform(jsonNode);
    return priceStatDTO;
  }

  // prices_latestテーブルの全レコードを取得する
  public List<PriceLatestRowDTO> finadAll () {
    return repository.findOrderRows();
  }



  // APIパラメータの確認
  public EstatAPIParamDTO getAPIParams () {
    return metaDataTransformService.transform();
  }

  public JsonNode test () {
    return estatClientService.fetchPriceStat("23100");
  }
}
