package ps.emall.orderhub.order.item;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import ps.emall.orderhub.common.base.EMallsBaseEntity;
import ps.emall.orderhub.order.ShopOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_items",
        schema = "orders",
        indexes = {
                @Index(name = "idx_order_item_shop_order", columnList = "shop_order_id"),
                @Index(name = "idx_order_item_status", columnList = "status"),
                @Index(name = "idx_order_item_holding", columnList = "status, holding_expires_at")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "order_items_audit", schema = "audit")
public class OrderItem extends EMallsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq")
    @SequenceGenerator(
            name = "order_items_seq",
            sequenceName = "order_items_seq",
            schema = "orders",
            allocationSize = 1
    )
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_order_id", nullable = false)
    private ShopOrder shopOrder;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    // Snapshotted at checkout — never changes after order is placed
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrderItemStatus status = OrderItemStatus.CREATED;

    // Set when item is delivered — 3 days from this date customer can return
    @Column(name = "holding_expires_at")
    private LocalDateTime holdingExpiresAt;

    @Column(name = "has_return_request", nullable = false)
    @Builder.Default
    private Boolean hasReturnRequest = false;

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
