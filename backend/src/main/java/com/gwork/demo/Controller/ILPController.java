package com.gwork.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.ILPResultDTO;
import com.gwork.demo.Service.ilp.SolveILPService;
import com.gwork.demo.Service.ilp.SortILPResult;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/ILP")
public class ILPController {
  @Autowired
  private final SolveILPService solveILPService = new SolveILPService();

  // キャッシュファイルに結果を保存
  @GetMapping("/save")
  public void solve() {
    solveILPService.saveILPResultList();
  }

  // 計算結果のオブジェクトをソートして取得する
  @GetMapping("/checkResult")
  public List<ILPResultDTO> getILPResultList() {
    SortILPResult sortILPResult = new SortILPResult();
    return sortILPResult.ilpResultList;
  }
  

  // 計算結果のオブジェクトの一部情報をトリミングして取得する
}
