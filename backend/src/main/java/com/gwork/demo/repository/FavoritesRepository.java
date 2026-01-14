package com.gwork.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gwork.demo.dto.FavoritesExistenceResDTO;
import com.gwork.demo.model.Favorites;
import java.util.List;


public interface FavoritesRepository extends JpaRepository<Favorites, Integer> {
  // userIdとmenuIdで検索して、memoを更新する
  @Modifying
  @Query("""
    UPDATE Favorites f
    SET f.memo = :memo
    WHERE f.userId = :userId
      AND f.menuId = :menuId
  """)
  int updateMemoByUserIdAndMenuId(
      @Param("userId") String userId,
      @Param("menuId") Integer menuId,
      @Param("memo") String memo
  );

  // userIdとhashで検索して存在を判定し、併せてmemoも取り出す
  @Query("""
  SELECT new com.gwork.demo.dto.FavoritesExistenceResDTO(
    COUNT(f) > 0,
    MAX(f.memo)
  )
  FROM Favorites f
  WHERE f.userId = :userId
    AND f.hash = :hash
""")
FavoritesExistenceResDTO findByUserIdAndHash(
      @Param("userId") String userId,
      @Param("hash") String hash
  );
  //↓クエリ自動生成
  // userIdで検索してレコードのリストを取得
  List<Favorites> findByUserIdOrderByMenuIdAsc(String userId);

  // userIdとmenuIdで検索して該当レコードを削除
  void deleteByUserIdAndMenuId(String userId, Integer menuId);
}
