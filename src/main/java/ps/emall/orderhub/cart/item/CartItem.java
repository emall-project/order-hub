package ps.emall.orderhub.cart.item;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import ps.emall.orderhub.cart.Cart;
import ps.emall.orderhub.common.base.EMallsBaseEntity;

import java.math.BigDecimal;

@Entity
@Table(
        name = "cart_items",
        schema = "orders",
        indexes = {
                @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
                @Index(name = "idx_cart_item_variant", columnList = "cart_id, variant_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@AuditTable(value = "cart_items_audit", schema = "audit")
public class CartItem extends EMallsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_items_seq")
    @SequenceGenerator(
            name = "cart_items_seq",
            sequenceName = "cart_items_seq",
            schema = "orders",
            allocationSize = 1
    )
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "mall_id", nullable = false)
    private Long mallId;

    // Price snapshotted at the moment of adding to cart
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // Discounted price if an active offer existed at add-to-cart time; null if no discount
    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "offer_id")
    private Long offerId;

    public BigDecimal getEffectiveUnitPrice() {
        return discountedPrice != null ? discountedPrice : basePrice;
    }
}
