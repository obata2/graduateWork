package com.gwork.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.gwork.demo.Service.FavoritesService;
import com.gwork.demo.dto.FavoritesRequestDTO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/psqlFavorites")
public class FavoritesController {
  @Autowired
  FavoritesService favoritesService;
  
  @PostMapping("/save")
  public void save(@RequestBody FavoritesRequestDTO favoritesRequestDTO) {
    favoritesService.save(favoritesRequestDTO);
  }
  
  @PostMapping("/exist")
  public boolean exist(@RequestBody FavoritesRequestDTO favoritesRequestDTO) {
    return favoritesService.existsByUserIdAndObjectHash(favoritesRequestDTO);
  }
}