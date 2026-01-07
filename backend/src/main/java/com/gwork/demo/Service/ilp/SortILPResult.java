package com.gwork.demo.Service.ilp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwork.demo.dto.ILPResultDTO;
import com.fasterxml.jackson.core.type.TypeReference;

public class SortILPResult {
  public List<ILPResultDTO> ilpResultList= new ArrayList<>();

  // コンストラクタで標準の並びを持っておく
  public SortILPResult(){
    this.ilpResultList = readILPResultFromCache();
  }

  // --- キャッシュファイルを読み込む ---
  public List<ILPResultDTO> readILPResultFromCache() {
    final String FILE_PATH = "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\assets\\cachedILPResult.json";
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(
        new File(FILE_PATH),
        new TypeReference<List<ILPResultDTO>>() {}
      );
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // --- idで昇順ソート(標準に戻る) ---
  public void sortById(){
    this.ilpResultList.sort(Comparator.comparingInt((ILPResultDTO r) -> r.id));
  }

  // --- 合計金額で昇順ソート(被ったらid順) ---
  public void sortByTotalPrice(){
    this.ilpResultList.sort(Comparator.comparingInt((ILPResultDTO r) -> r.totalPrice)
      .thenComparing(r -> r.id)
    );
  }

  // --- 合計カロリーで昇順ソート(被ったらid順) ---
  public void sortByTotalKcal(){
    this.ilpResultList.sort(Comparator.comparingInt((ILPResultDTO r) -> r.totalKcal)
      .thenComparing(r -> r.id)
    );
  }

  // --- 食材の種類数でソート(被ったらid順) ---
  public void sortByTypesOfIng(){
    this.ilpResultList.sort(Comparator.comparingInt((ILPResultDTO r) -> r.ingredients.size())
      .thenComparing(r -> r.id)
    );
  }

  // --- インスタンス変数を一部トリミングする ---
  public List<ILPResultDTO> trimILPResultDTOs(){
    List<ILPResultDTO> trimmedList = this.ilpResultList.stream()
    .map(m -> {
        ILPResultDTO copy = new ILPResultDTO();
        copy.setId(m.getId());
        copy.setIngredients(m.getIngredients());
        copy.setTotalPrice(m.getTotalPrice());
        copy.setTotalKcal(m.getTotalKcal());
        return copy;
    })
    .toList();
    return trimmedList;
  }
}