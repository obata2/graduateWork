package com.gwork.demo.Controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.gwork.demo.Service.JsonProcesserService;


@RestController
@RequestMapping("/json")
public class jsonController {
    private final JsonProcesserService jsonProcesser = new JsonProcesserService();
    JsonNode jsonNode = null;
    // --- ダウンロード ---
    @GetMapping("/download")
    public String download() {
        this.jsonNode = jsonProcesser.downloadJSON();
        return "データのダウンロードに成功しました";
    }
    // --- cacheへの書き込み ---
    @GetMapping("/write")
    public String write() {
        if(jsonNode != null){
            jsonProcesser.saveJSONToCache(jsonNode);
            return "CachedDataに書き込みました";
        }else{
            return "ダウンロードされたデータがありません";
        }
    }
    // --- cacheからの読み込み ---
    @GetMapping("/load")
    public String load() {
        jsonProcesser.readJSONFromCache();
        return "CachedDataからの読み込みに成功しました";
    }
    // --- メタデータの確認 ---
    @GetMapping("/metaData")
    public String getMetaData() {
        return jsonProcesser.getMetaData();
    }
    // --- {食品名：価格}のmapを取得 ---
    @GetMapping("/ingAndPri")
    public Map<String, Integer> getIngAndPri() {
        return jsonProcesser.getIngAndPri();
    }
    /// --- JSONの生データを確認 ---
    @GetMapping("/checkJSON")
    public JsonNode checkJSON() {
        return jsonProcesser.checkJSON();
    }
}
