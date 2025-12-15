package com.gwork.demo.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.gwork.demo.Service.estat.APIParameterService;
import com.gwork.demo.Service.estat.ProcessEstatService;
import com.gwork.demo.Service.filter.PriceDataFilterService;
import com.gwork.demo.Service.filter.ingredientNameFilterService;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/estat")
public class EstatController {

    private final APIParameterService apiParameterService = new APIParameterService();

    @PostMapping("/download")
    public ProcessEstatService downloadStatData(@RequestBody APIParams apiParams){
        String timeFrom = apiParams.getTimeFrom();
        String timeTo = apiParams.getTimeTo();
        String areaCode = apiParams.getAreaCode();
        ProcessEstatService processEstatService = new ProcessEstatService(timeFrom, timeTo, areaCode);
        return processEstatService;
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
    /* 
    // --- ダウンロード ---
    @GetMapping("/download")
    public String download() {
        jsonNode = processEstatService.downloadJSON();
        return "データのダウンロードに成功しました";
    }
    // --- cacheへの書き込み ---
    @GetMapping("/write")
    public String write() {
        if(jsonNode != null){
            processEstatService.savePriceStatToCache(jsonNode);
            return "CachedDataに書き込みました";
        }else{
            return "ダウンロードされたデータがありません";
        }
    }

    // --- {食品名：[価格推移の配列]}のmapを取得 ---
    @GetMapping("/priceTransition")
    public Map<String, ArrayList<Integer>> getPriceTransition() {
        //return jsonProcesser.getPriceTransition(jsonNode);
        return ProcessEstatService.priceTransition;
    }
    // --- {食品名：最新の価格}のmapを取得 ---
    @GetMapping("/priceLatest")
    public Map<String, Integer> getPriceLatest() {
        //return jsonProcesser.getPriceLatest(jsonNode);
        return ProcessEstatService.priceLatest;
    }
    // --- 日付ラベルを取得 ---
    @GetMapping("/dateLabel")
    public Map<String, ArrayList<String>> getDateLabel() {
        //return jsonProcesser.getPriceLatest(jsonNode);
        return ProcessEstatService.dateLabel;
    }

    // --- JSONの生データを確認 ---
    @GetMapping("/checkJSON")
    public JsonNode checkJSON() {
        return processEstatService.checkJSON();
    }*/

    // --- メタデータの確認 ---
    @GetMapping("/metaData")
    public JsonNode getMetaData() {
        return APIParameterService.getMetaData();
    }
    // --- APIパラメータとして利用可能なものを取得 ---
    @GetMapping("/params")
    public Map<String, Object> getAPIParams() {
        return APIParameterService.getAPIParams();
    }   
}
