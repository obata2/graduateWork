package com.gwork.demo.Service.ilp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.gwork.demo.dto.ILPReplaceRequestDTO;
import com.gwork.demo.dto.ILPResultDTO;
import com.gwork.demo.model.ILPResults;
import com.gwork.demo.repository.ILPResultsRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ILPService {
  private final SolveILPService solveILPService;
  private final ILPResultsRepository repository;
  public ILPService (SolveILPService solveILPService, ILPResultsRepository repository) {
    this.solveILPService = solveILPService;
    this.repository = repository;
  }

  // DBからレコードを取得する
  public List<ILPResultDTO> findAllByUserId (String userId) {
    System.out.println("DB漁るよ");
    List<ILPResults> entityList = repository.findByUserIdOrderByResultIdAsc(userId);
    List<ILPResultDTO> iLPResultsList = new ArrayList<>();
    for (ILPResults entity : entityList) {
      ILPResultDTO dto = new ILPResultDTO();
      BeanUtils.copyProperties(entity, dto);
      iLPResultsList.add(dto);
    }
    return iLPResultsList;
  }

  // 整数計画法で計算を行い、さらに結果をDBに保存する
  public void replaceResults (ILPReplaceRequestDTO ilpReplaceRequestDTO) {
    String userId = ilpReplaceRequestDTO.getUserId();
    Set<String> selected = ilpReplaceRequestDTO.getSelected();
    Set<String> excluded = ilpReplaceRequestDTO.getExcluded();
    ArrayList<ILPResultDTO> iLPResultsList = solveILPService.solveILP(selected, excluded);
    ArrayList<ILPResults> entityList = new ArrayList<>();
    for(ILPResultDTO dto : iLPResultsList) {
      ILPResults entity = new ILPResults();
      BeanUtils.copyProperties(dto, entity);
      entity.setUserId(userId);
      entityList.add(entity);
    }
    // 一度userIdで該当するレコードを削除
    repository.deleteByUserId(userId);
    // 新規に登録する
    repository.saveAll(entityList);
  }
}
