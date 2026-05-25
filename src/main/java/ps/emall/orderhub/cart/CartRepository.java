package ps.emall.orderhub.cart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>,
        JpaSpecificationExecutor<Cart> {

    Optional<Cart> findByCustomerIdAndMallId(Long customerId, Long mallId);

    boolean existsByCustomerIdAndMallIdAndStatus(Long customerId, Long mallId, CartStatus status);

    Optional<Cart> findByCustomerIdAndStatus(Long customerId, CartStatus status);

    boolean existsByCustomerIdAndStatus(Long customerId, CartStatus status);

    List<Cart> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Page<Cart> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    long countByStatus(CartStatus status);

    List<Cart> findAllByCustomerId(Long customerId);

    List<Long> findCartIdByCustomerId(Long customerId);
}
