package ps.emall.orderhub.returnrequest;

import org.springframework.data.domain.Pageable;
import ps.emall.orderhub.common.page.PaginatedResponse;

import java.util.Map;

public interface ReturnRequestService {

    // Customer
    ReturnRequestDto submit(Long customerId, ReturnRequestDto dto);

    PaginatedResponse<ReturnRequestDto> getMyReturnRequests(Long customerId, Pageable pageable);

    ReturnRequestDto getMyReturnById(Long returnRequestId, Long customerId);

    ReturnRequestDto getByOrderItemId(Long orderItemId, Long customerId);

    // Store owner
    PaginatedResponse<ReturnRequestDto> getReturnsByShop(Long shopId,
                                                          ReturnRequestSpec spec,
                                                          Pageable pageable);

    PaginatedResponse<ReturnRequestDto> getReturnsByShopAndStatus(Long shopId,
                                                                   ReturnRequestStatus status,
                                                                   Pageable pageable);

    ReturnRequestDto getReturnByIdForShop(Long returnRequestId, Long shopId);

    ReturnRequestDto approve(Long returnRequestId, Long shopId);

    ReturnRequestDto reject(Long returnRequestId, Long shopId, RejectReturnRequest request);

    Map<ReturnRequestStatus, Long> getReturnStatsByShop(Long shopId);

    // Admin
    PaginatedResponse<ReturnRequestDto> getAllReturns(ReturnRequestSpec spec, Pageable pageable);

    PaginatedResponse<ReturnRequestDto> getAllReturnsByStatus(ReturnRequestStatus status,
                                                              Pageable pageable);

    ReturnRequestDto getReturnByIdForAdmin(Long returnRequestId);

    Map<ReturnRequestStatus, Long> getAllReturnStats();
}
