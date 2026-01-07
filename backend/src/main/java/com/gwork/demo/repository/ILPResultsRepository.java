package com.gwork.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gwork.demo.model.ILPResults;
import com.gwork.demo.model.ILPResultsId;
import java.util.List;


public interface ILPResultsRepository extends JpaRepository<ILPResults, ILPResultsId> {

  void deleteByUserId(String userId);

  List<ILPResults> findByUserIdOrderByResultIdAsc(String userId);
}
