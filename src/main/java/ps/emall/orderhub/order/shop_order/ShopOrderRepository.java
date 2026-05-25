package ps.emall.orderhub.order.shop_order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long>,
        JpaSpecificationExecutor<ShopOrder> {

    List<ShopOrder> findByCartId(Long cartId);

    List<ShopOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Page<ShopOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    List<ShopOrder> findByShopIdOrderByCreatedAtDesc(Long shopId);

    Page<ShopOrder> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    List<ShopOrder> findByShopIdAndStatus(Long shopId, ShopOrderStatus status);

    Page<ShopOrder> findByShopIdAndStatus(Long shopId, ShopOrderStatus status, Pageable pageable);

    long countByShopIdAndStatus(Long shopId, ShopOrderStatus status);

    long countByStatus(ShopOrderStatus status);

    @Query("SELECT SUM(o.total) FROM ShopOrder o WHERE o.shopId = :shopId AND o.status = 'DELIVERED'")
    java.math.BigDecimal sumDeliveredTotalByShopId(@Param("shopId") Long shopId);
}
