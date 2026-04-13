package ps.emall.orderhub.order;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import ps.emall.orderhub.common.base.EMallsBaseEntity;
import ps.emall.orderhub.order.item.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shop_orders",
        schema = "orders",
        indexes = {
                @Index(name = "idx_shop_order_cart", columnList = "cart_id"),
                @Index(name = "idx_shop_order_shop_status", columnList = "shop_id, status"),
                @Index(name = "idx_shop_order_customer", columnList = "customer_id, status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "shop_orders_audit", schema = "audit")
public class ShopOrder extends EMallsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_orders_seq")
    @SequenceGenerator(
            name = "shop_orders_seq",
            sequenceName = "shop_orders_seq",
            schema = "orders",
            allocationSize = 1
    )
    @Column(name = "shop_order_id")
    private Long shopOrderId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "mall_id", nullable = false)
    private Long mallId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ShopOrderStatus status = ShopOrderStatus.NEW;

    @OneToMany(mappedBy = "shopOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
