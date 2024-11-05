package com.management.inventory_management.repository;

import com.management.inventory_management.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item,Long> {

    @Query("SELECT COALESCE(SUM(CASE WHEN i.type = 'T' THEN i.qty ELSE -i.qty END), 0) " +
            "FROM Inventory i WHERE i.item.id = :itemId")
    Integer calculateItemStock(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Item i SET i.currentStock = :newStock WHERE i.id = :itemId")
    void updateItemStock(@Param("itemId") Long itemId, @Param("newStock") Integer newStock);
}
