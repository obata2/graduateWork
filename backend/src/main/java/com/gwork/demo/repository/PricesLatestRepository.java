package com.gwork.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.gwork.demo.dto.PriceLatestRowDTO;
import com.gwork.demo.model.PricesLatestId;
import com.gwork.demo.model.PricesLatest;

public interface PricesLatestRepository extends JpaRepository<PricesLatest, PricesLatestId> {
  @Modifying
  @Query(value = """
      INSERT INTO prices_latest (user_id, ingredient_id, ingredient_name, price_latest, price_unit_qty)
      VALUES (:userId, :ingredientId, :ingredientName, :priceLatest, :priceUnitQty)
      ON CONFLICT (user_Id, ingredient_Id) DO UPDATE SET
      price_latest = EXCLUDED.price_latest
      WHERE prices_latest.is_fixed = false
      """, nativeQuery = true)
  void upsertIfNotFixed(
      @Param("userId") String userId,
      @Param("ingredientId") String ingredientId,
      @Param("ingredientName") String ingredientName,
      @Param("priceLatest") Integer priceLatest,
      @Param("priceUnitQty") String priceUnitQty);

  // テーブル結合した結果のレコードをDTOとして、それらをリストに格納
  @Query("""
        SELECT new com.gwork.demo.dto.PriceLatestRowDTO(
          pl.userId,
          pl.ingredientId,
          pl.isFixed,
          mi.ingredientName,
          pl.priceLatest,
          mi.priceUnitQty
        )
        FROM PricesLatest pl
        JOIN pl.mIngredients mi
        ORDER BY CAST(pl.ingredientId AS integer) ASC
      """)
  List<PriceLatestRowDTO> findOrderRows();

  // 最新の統計情報に更新する
  @Modifying
  @Query("""
        update PricesLatest pl
           set pl.priceLatest = :priceLatest
         where pl.userId = :userId
           and pl.ingredientId = :ingredientId
           and pl.isFixed = false
      """)
  void updateLatest(
      @Param("userId") String userId,
      @Param("ingredientId") String ingredientId,
      @Param("priceLatest") Integer priceLatest);

  // ユーザーが編集した情報でテーブルを更新する
  @Modifying
  @Query("""
        update PricesLatest pl
           set pl.isFixed = :isFixed,
               pl.priceLatest = :priceLatest
         where pl.userId = :userId
           and pl.ingredientId = :ingredientId
      """)
  void saveEdits(
      @Param("userId") String userId,
      @Param("ingredientId") String ingredientId,
      @Param("isFixed") Boolean isFixed,
      @Param("priceLatest") Integer priceLatest);
}