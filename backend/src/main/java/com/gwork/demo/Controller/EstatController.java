package com.gwork.demo.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.estat.EstatService;
import com.gwork.demo.dto.EstatAPIParamDTO;
import com.gwork.demo.dto.PriceLatestRowDTO;
import com.gwork.demo.dto.PriceStatDTO;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/estat")
public class EstatController {
  private EstatService estatService;
  // コンストラクタインジェクション
  public EstatController(EstatService estatService) {
    this.estatService = estatService;
  }

  // prices_latestテーブルの全レコードを取得(価格一覧表の表示用)
  @GetMapping("/prices-latest/{userId}")
  public List<PriceLatestRowDTO> findByUserIdOrderRows (@PathVariable("userId") String userId) {
    return estatService.findByUserIdOrderRows(userId); 
  }

  // prices_latestテーブルを、eStatの最新情報に更新する(価格一覧表の更新用)
  @PutMapping("/prices-latest/{userId}/sync")
    public void updateLatest(@PathVariable("userId") String userId, @RequestBody Map<String, String> body) {
    String cdArea = body.get("cdArea");
    estatService.updateLatest(cdArea, userId);
  }

  // prices_latestテーブルを、ユーザーが編集した情報で更新する(価格一覧表の編集用)   ←   userIdはPriceLatestRowDTO内に含まれている
  @PutMapping("/prices-latest/{userId}")
  public void saveEdits(@PathVariable("userId") String userId, @RequestBody List<PriceLatestRowDTO> req) {
    estatService.saveEdits(userId, req);
  }

  // estatのAPIパラメータの用意
  @GetMapping("/api-params")
  public EstatAPIParamDTO getAPIParams() {
    return estatService.getAPIParams();
  }
  
  // estatのAPIを叩いて価格統計を取得(推移グラフの表示用)
  @GetMapping("fetch")
  public PriceStatDTO postMethodName(@RequestParam String cdArea) {
    return estatService.fetchPriceStat(cdArea);
  }
}
