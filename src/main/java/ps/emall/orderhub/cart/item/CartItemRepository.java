package ps.emall.orderhub.cart.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_CartIdAndVariantId(Long cartId, Long variantId);

    boolean existsByCart_CartIdAndVariantId(Long cartId, Long variantId);
}
