package ps.emall.orderhub.order.order;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import ps.emall.orderhub.common.base.EMallsBaseEntity;
import ps.emall.orderhub.order.item.OrderItem;
import ps.emall.orderhub.order.order.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        schema = "orders",
        indexes = {
                @Index(name = "idx_cart_customer_status", columnList = "customer_id, status"),
                @Index(name = "idx_cart_mall_customer", columnList = "mall_id, customer_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "orders_audit", schema = "audit")
public class Order extends EMallsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(
            name = "orders_seq",
            sequenceName = "orders_seq",
            schema = "orders",
            allocationSize = 1
    )
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "mall_id", nullable = false)
    private Long mallId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "delivery_name")
    private String deliveryName;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "delivery_note")
    private String deliveryNote;

    @Column(name = "delivery_location")
    private String deliveryLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.SUBMITTED;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getEffectiveUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
