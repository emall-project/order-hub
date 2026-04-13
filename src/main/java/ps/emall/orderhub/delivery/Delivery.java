package ps.emall.orderhub.delivery;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import ps.emall.orderhub.common.base.EMallsBaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "deliveries",
        schema = "orders",
        indexes = {
                @Index(name = "idx_delivery_cart", columnList = "cart_id"),
                @Index(name = "idx_delivery_status", columnList = "status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "deliveries_audit", schema = "audit")
public class Delivery extends EMallsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deliveries_seq")
    @SequenceGenerator(
            name = "deliveries_seq",
            sequenceName = "deliveries_seq",
            schema = "orders",
            allocationSize = 1
    )
    @Column(name = "delivery_id")
    private Long deliveryId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    // Nullable — populated if/when a real delivery company is integrated
    @Column(name = "delivery_company_id")
    private Long deliveryCompanyId;

    // Nullable — populated when real tracking is available
    @Column(name = "tracking_id")
    private String trackingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.CREATED;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
