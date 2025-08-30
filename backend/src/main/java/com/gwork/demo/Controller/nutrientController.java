package com.gwork.demo.Controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.IntegerLinearService;
import com.gwork.demo.Service.NutrientService;
import com.gwork.demo.util.ILPResult;

@RestController
@RequestMapping("/nutrient")
public class nutrientController {
    private final NutrientService nutrientService = new NutrientService();
    @GetMapping("/targets")
    public double[] getTargets(){
        return nutrientService.getTargets();
    }

    @Autowired
    private final IntegerLinearService integerLinearService = new IntegerLinearService();

    @GetMapping("/calc")
    public ArrayList<ILPResult> calc(){
        return integerLinearService.getILPResultList();
    }
}
