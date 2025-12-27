package com.gwork.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gwork.demo.model.MIngredients;

public interface MIngredientsRepository extends JpaRepository<MIngredients, String> {
  
}
