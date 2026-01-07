package com.gwork.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.gwork.demo.Service.favorites.FavoritesService;
import com.gwork.demo.dto.FavoritesRequestDTO;
import com.gwork.demo.model.Favorites;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

  @GetMapping("/findAll")
  public List<Favorites> findAll() {
    return favoritesService.findAll();
  }

  @DeleteMapping("/deleteById")
  public void deleteById(@RequestParam String userId, @RequestParam Integer menuId) {
    favoritesService.deleteById(userId, menuId);
  }

}