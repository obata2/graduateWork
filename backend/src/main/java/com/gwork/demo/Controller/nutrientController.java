package com.gwork.demo.Controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.DataAdjusterForILP;
import com.gwork.demo.Service.ilp.ILPResultDTO;
import com.gwork.demo.Service.ilp.SolveILPService;
import com.gwork.demo.Service.nutrient.NutrientService;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/nutrient")
public class NutrientController {
    private final NutrientService nutrientService = new NutrientService();
    @GetMapping("/targets")
    public double[] getTargets(){
        return NutrientService.targets;
    }

    @GetMapping("check")
    public LinkedHashMap<String, Object> check(){
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("unitQuantity", DataAdjusterForILP.vegUnitQuantity);
        result.put("adjustenNutrientTable", DataAdjusterForILP.adjustedNutrientTable);
        result.put("adjustedPrices", DataAdjusterForILP.adjustedPrices);
        result.put("adjustedStdQty", DataAdjusterForILP.adjustedStandardQty);
        return result;
    }
}
