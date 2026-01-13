package com.gwork.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gwork.demo.model.Favorites;
import java.util.List;


public interface FavoritesRepository extends JpaRepository<Favorites, Integer> {

  //↓クエリ自動生成
  // userIdで検索してレコードのリストを取得
  List<Favorites> findByUserIdOrderByMenuIdAsc(String userId);

  // userIdとhashで検索して存在を判定
  boolean existsByUserIdAndHash(String userId, String hash);

  // userIdとmenuIdで検索して該当レコードを削除
  void deleteByUserIdAndMenuId(String userId, Integer menuId);
}
