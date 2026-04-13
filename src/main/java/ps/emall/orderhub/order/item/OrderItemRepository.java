package ps.emall.orderhub.order.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByShopOrder_ShopOrderId(Long shopOrderId);

    List<OrderItem> findByShopIdAndStatus(Long shopId, OrderItemStatus status);

    List<OrderItem> findByStatus(OrderItemStatus status);

    // Used by scheduler: items in HOLDING whose window has now expired
    @Query("""
            SELECT i FROM OrderItem i
            WHERE i.status = 'HOLDING'
            AND i.holdingExpiresAt IS NOT NULL
            AND i.holdingExpiresAt <= :now
            AND i.hasReturnRequest = false
            """)
    List<OrderItem> findExpiredHoldingItems(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE OrderItem i SET i.status = :status WHERE i.shopOrder.shopOrderId = :shopOrderId")
    void updateStatusByShopOrderId(
            @Param("shopOrderId") Long shopOrderId,
            @Param("status") OrderItemStatus status);

    long countByShopIdAndStatus(Long shopId, OrderItemStatus status);

    long countByStatus(OrderItemStatus status);

    @Query("SELECT SUM(i.unitPrice * i.quantity) FROM OrderItem i WHERE i.shopId = :shopId AND i.status = 'READY_FOR_PAYOUT'")
    BigDecimal sumReadyForPayoutByShopId(@Param("shopId") Long shopId);


    @Query("SELECT SUM(i.unitPrice * i.quantity) FROM OrderItem i " +
            "WHERE i.shopId = :shopId " +
            "AND i.status IN ('READY_FOR_PAYOUT', 'RETURN_REJECTED') " +
            "AND i.status != 'DELIVERY_FAILED'")
    BigDecimal sumEarnedAmountByShopId(@Param("shopId") Long shopId);
}
