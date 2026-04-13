package ps.emall.orderhub.returnrequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long>,
        JpaSpecificationExecutor<ReturnRequest> {

    Optional<ReturnRequest> findByOrderItem_OrderItemId(Long orderItemId);

    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Page<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    List<ReturnRequest> findByShopIdOrderByCreatedAtDesc(Long shopId);

    Page<ReturnRequest> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    List<ReturnRequest> findByShopIdAndStatus(Long shopId, ReturnRequestStatus status);

    Page<ReturnRequest> findByShopIdAndStatus(Long shopId, ReturnRequestStatus status,
                                               Pageable pageable);

    Page<ReturnRequest> findByStatus(ReturnRequestStatus status, Pageable pageable);

    long countByShopIdAndStatus(Long shopId, ReturnRequestStatus status);

    long countByStatus(ReturnRequestStatus status);
}
