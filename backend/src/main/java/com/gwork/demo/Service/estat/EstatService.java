package com.gwork.demo.Service.estat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gwork.demo.Service.nutrient.NutrientService;
import com.gwork.demo.dto.EstatAPIParamDTO;
import com.gwork.demo.dto.PriceLatestRowDTO;
import com.gwork.demo.dto.PriceLatestEditReqDTO;
import com.gwork.demo.dto.PriceStatDTO;
import com.gwork.demo.model.MIngredients;
import com.gwork.demo.model.PricesLatest;
import com.gwork.demo.repository.PricesLatestRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EstatService {

  private final EstatClientService estatClientService;
  private final PriceStatTransoformService priceStatTransoformService;
  private final MetaDataTransformService metaDataTransformService;
  private final PricesLatestRepository pricesLatestRepository;

  // コンストラクタインジェクションでTransformのインスタンスを受け取る
  public EstatService(EstatClientService estatClientService, PriceStatTransoformService priceStatTransoformService,
    MetaDataTransformService metaDataTransformService, PricesLatestRepository repository) {
    this.estatClientService = estatClientService;
    this.priceStatTransoformService = priceStatTransoformService;
    this.metaDataTransformService = metaDataTransformService;
    this.pricesLatestRepository = repository;
  }

  // API経由で価格統計を取得し、DTOで値を返す ←引数としてcdAreaとcdTimeFromが欲しい
  public PriceStatDTO fetchPriceStat(String cdArea) {
    JsonNode jsonNode = estatClientService.fetchPriceStat(cdArea);
    PriceStatDTO priceStatDTO = priceStatTransoformService.transform(jsonNode);
    return priceStatDTO;
  }

  // 〃 、prices_latestテーブルを更新する
  public void updateLatest(String cdArea, String userId) {
    JsonNode jsonNode = estatClientService.fetchPriceStat(cdArea);
    PriceStatDTO priceStatDTO = priceStatTransoformService.transform(jsonNode);
    for (Map.Entry<String, Integer> entry : priceStatDTO.getPriceLatest().entrySet()) {
      pricesLatestRepository.updateLatest(userId, entry.getKey(), entry.getValue());
    }
  }

  // ユーザーの編集した情報を受け取り、prices_latestテーブルを更新する
  public void saveEdits(String userId, List<PriceLatestRowDTO> req) {
    System.out.println("レコードを更新するよ");
    for (PriceLatestRowDTO editedRow : req) {
      PriceLatestEditReqDTO editReq = new PriceLatestEditReqDTO();
      BeanUtils.copyProperties(editedRow, editReq);
      // ログイン中のユーザー自身に紐づくデータのみ、更新するようにする
      if(!editReq.getUserId().equals(userId)){
        editReq.setUserId(userId);
      }
      pricesLatestRepository.saveEdits(editReq.getUserId(), editReq.getIngredientId(), editReq.getIsFixed(), editReq.getPriceLatest());
    }
  }

  // prices_latestテーブルの全レコードを取得する
  public List<PriceLatestRowDTO> findByUserIdOrderRows(String userId) {
    return pricesLatestRepository.findByUserIdOrderRows(userId);
  }

  // {id : 最新価格}の辞書を作成する
  public LinkedHashMap<String, Integer> getIdAndPriceMap(String userId) {
    List<PriceLatestRowDTO> entityList = pricesLatestRepository.findByUserIdOrderRows(userId);
    LinkedHashMap<String, Integer> idAndPrice = new LinkedHashMap<>();
    for (PriceLatestRowDTO entity : entityList) {
      idAndPrice.put(entity.ingredientId(), entity.priceLatest());
    }
    return idAndPrice;
  }

  // APIパラメータの確認
  public EstatAPIParamDTO getAPIParams() {
    return metaDataTransformService.transform();
  }
}
