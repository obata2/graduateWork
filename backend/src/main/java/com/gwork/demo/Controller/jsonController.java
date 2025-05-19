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
    //cacheへの書き込み
    @GetMapping("/write")
    public void downloadJSON() {
        JsonNode jsonNode = jsonProcesser.downloadJSON();
        jsonProcesser.saveJSONToCache(jsonNode);
    }
    //cacheからの読み込み
    @GetMapping("/load")
    public void getJSONData() {
        jsonProcesser.readJSONFromCache();
    }
    @GetMapping("/metaData")
    public String getMetaData() {
        return jsonProcesser.getMetaData();
    }
    @GetMapping("/ingAndPri")
    public Map<String, Integer> getIngAndPri() {
        return jsonProcesser.getIngAndPri();
    }
    @GetMapping("/checkJSON")
    public JsonNode checkJSON() {
        return jsonProcesser.checkJSON();
    }
}
