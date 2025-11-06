package com.gwork.demo.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.ILPResultDTO;
import com.gwork.demo.Service.ilp.SortILPResult;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@RestController
@RequestMapping("/sort")
public class sortTestController {
  @GetMapping
  public List<ILPResultDTO> getResults(@RequestParam(defaultValue = "default") String sort) {
    SortILPResult sortILPResult = new SortILPResult();
    switch (sort) {
      case "default":
        sortILPResult.sortById();
        return sortILPResult.ilpResultList;
      case "totalPrice":
        sortILPResult.sortByTotalPrice();
        return sortILPResult.ilpResultList;
      case "totalKcal":
        sortILPResult.sortByTotalKcal();
        return sortILPResult.ilpResultList;
      case "typesOfIng":
        sortILPResult.sortByTypesOfIng();
        return sortILPResult.ilpResultList;
      default:
        sortILPResult.sortById();
        return sortILPResult.ilpResultList;
    }
  }
}
