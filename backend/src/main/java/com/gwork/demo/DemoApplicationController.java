package com.gwork.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.NutrientService;
import com.gwork.demo.Service.JsonProcesserService;
import com.gwork.demo.Service.LinearService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class DemoApplicationController {

    @GetMapping("/")  // ルートへこのメソッドをマップする
    public String test() {
        return "Spring boot test <br> 2行目";
    }

    @GetMapping("/aiueo")  // ルートへこのメソッドをマップする
    public String aiueo() {
        return "確認用でゴザります";
    }
    
    
    private final JsonProcesserService jsonProcesser = new JsonProcesserService();
    //cacheへの書き込み
    @GetMapping("/download")
    public void downloadJSON() {
        JsonNode jsonNode = jsonProcesser.downloadJSON();
        jsonProcesser.saveJSONToCache(jsonNode);
    }
    //cacheからの読み込み
    @GetMapping("/JSONdata")
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


    private final NutrientService nutrientService = new NutrientService();
    @GetMapping("/ingredients")
    public ArrayList<String> getIngredients(){
        return nutrientService.getIngredients();
    }
    @GetMapping("/nutrients")
    public double[][] getNutrients(){
        return nutrientService.getNutrients();
    }
    @GetMapping("/targets")
    public double[] getTargets(){
        return nutrientService.getTargets();
    }

    
    private final LinearService linearService = new LinearService();
    @GetMapping("/linear")
    public void getLinearResult(){
        linearService.SolveLinearProblem();
    }
}