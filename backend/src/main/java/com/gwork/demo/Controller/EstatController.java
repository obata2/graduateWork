package com.gwork.demo.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.estat.EstatService;
import com.gwork.demo.dto.EstatAPIParamDTO;
import com.gwork.demo.dto.PriceLatestRowDTO;
import com.gwork.demo.dto.PriceStatDTO;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/estat")
public class EstatController {
    
    private EstatService estatService;
    // コンストラクタインジェクション
    public EstatController(EstatService estatService) {
      this.estatService = estatService;
    }

    /*
    @GetMapping("/upsert")
    public void upsert() {
      estatService.savePricesLatest("admin");
    }*/

    // prices_latestテーブルの全レコードを取得(価格一覧表の表示用)
    @GetMapping("/findAll")
    public List<PriceLatestRowDTO> findAll () {
        return estatService.finadAll(); 
    }

    // prices_latestテーブルを、eStatの最新情報に更新する(価格一覧表の更新用)
    @PostMapping("/updateLatest")
      public void updateLatest(@RequestBody Map<String, String> body) {
      String cdArea = body.get("cdArea");
      String userId = body.get("userId");
      estatService.updateLatest(cdArea, userId);
    }

    // prices_latestテーブルを、ユーザーが編集した情報で更新する(価格一覧表の編集用)
    @PostMapping("/saveEdits")
    public void saveEdits(@RequestBody List<PriceLatestRowDTO> req) {
      estatService.saveEdits(req);
    }
    

    // APIを叩いて価格統計を取得(推移グラフの表示用)
    @PostMapping("fetch")
    public PriceStatDTO postMethodName(@RequestBody Map<String, String> body) {
      String cdArea = body.get("cdArea");
      return estatService.fetchPriceStat(cdArea);
    }

    // APIパラメータの用意
    @GetMapping("/apiParams")
    public EstatAPIParamDTO getAPIParams() {
      return estatService.getAPIParams();
    }


    // リクエスト受け取り用のDTOクラス
    public static class APIParams{
        private String timeFrom;
        private String timeTo;
        private String areaCode;

        public String getTimeFrom() { return timeFrom; }
        public String getTimeTo() { return timeTo; }
        public String getAreaCode() { return areaCode; }
    }   
}
