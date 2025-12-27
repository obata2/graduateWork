package com.gwork.demo.Service.mapper;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gwork.demo.dto.MIngredientsSummaryDTO;
import com.gwork.demo.model.MIngredients;
import com.gwork.demo.repository.MIngredientsRepository;

@Service
public class MIngredientsMapper {
  private final MIngredientsRepository mIngredientsRepository;

  public MIngredientsMapper (MIngredientsRepository mIngredientsRepository) {
    this.mIngredientsRepository = mIngredientsRepository;
  }

  public MIngredientsSummaryDTO toSummary () {
    MIngredientsSummaryDTO mIngredientsSummaryDTO = new MIngredientsSummaryDTO();
    List<MIngredients> entityList = mIngredientsRepository.findAll();
    LinkedHashMap<String, String> nameAndId = new LinkedHashMap<>();
    LinkedHashMap<String, String> nameAndPriceUnitQty = new LinkedHashMap<>();
    for(MIngredients entity : entityList) {
      nameAndId.put(entity.getIngredientName(), entity.getIngredientId());
      nameAndPriceUnitQty.put(entity.getIngredientName(), entity.getPriceUnitQty());
    }
    mIngredientsSummaryDTO.setNameAndId(nameAndId);
    mIngredientsSummaryDTO.setNameAndPriceUnitQty(nameAndPriceUnitQty);
    return mIngredientsSummaryDTO;
  }
}
