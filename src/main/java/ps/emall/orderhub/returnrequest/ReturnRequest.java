package ps.emall.orderhub.returnrequest;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import ps.emall.orderhub.common.base.EMallsBaseEntity;
import ps.emall.orderhub.order.item.OrderItem;

import java.util.UUID;

@Entity
@Table(
        name = "return_requests",
        schema = "orders",
        indexes = {
                @Index(name = "idx_return_order_item", columnList = "order_item_id"),
                @Index(name = "idx_return_shop_status", columnList = "shop_id, status"),
                @Index(name = "idx_return_customer", columnList = "customer_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_return_order_item",
                columnNames = {"order_item_id"}
        )
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "return_requests_audit", schema = "audit")
public class ReturnRequest extends EMallsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_requests_seq")
    @SequenceGenerator(
            name = "return_requests_seq",
            sequenceName = "return_requests_seq",
            schema = "orders",
            allocationSize = 1
    )
    @Column(name = "return_request_id")
    private Long returnRequestId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReturnRequestStatus status = ReturnRequestStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "image_uuid", nullable = false)
    private UUID imageUuid;
}
