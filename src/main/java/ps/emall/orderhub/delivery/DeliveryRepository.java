package ps.emall.orderhub.delivery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>,
        JpaSpecificationExecutor<Delivery> {

    Optional<Delivery> findByCartId(Long cartId);

    List<Delivery> findByStatus(DeliveryStatus status);

    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);

    Page<Delivery> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(DeliveryStatus status);
}
