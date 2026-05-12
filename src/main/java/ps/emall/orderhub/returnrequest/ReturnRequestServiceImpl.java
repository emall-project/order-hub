package ps.emall.orderhub.returnrequest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.client.media.FileDto;
import ps.emall.orderhub.client.media.MediaManagerClient;
import ps.emall.orderhub.client.media.MediaResponse;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.order.item.OrderItem;
import ps.emall.orderhub.order.item.OrderItemRepository;
import ps.emall.orderhub.order.item.OrderItemStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final MediaManagerClient mediaManagerClient;
    private final ReturnRequestEnrichmentService enrichmentService;

    // Customer

    @Override
    public ReturnRequestDto submit(Long customerId, ReturnRequestDto dto) {
        OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        // Item must be in HOLDING status to submit a return
        if (orderItem.getStatus() != OrderItemStatus.HOLDING) {
            throw ReturnRequestExceptions.itemNotInHoldingStatus();
        }

        // Item must belong to this customer's order
        if (!orderItem.getShopOrder().getCustomerId().equals(customerId)) {
            throw ReturnRequestExceptions.returnDoesNotBelongToCustomer();
        }

        // One return request per order item — block duplicates
        if (returnRequestRepository.existsByOrderItem_OrderItemId(dto.getOrderItemId())) {
            throw ReturnRequestExceptions.returnRequestAlreadyExists();
        }

        // Validate image against media-manager if provided
        if (dto.getImageUuid() != null) {
            validateImage(dto.getImageUuid());
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderItem(orderItem)
                .customerId(customerId)
                .shopId(orderItem.getShopId())
                .reason(dto.getReason())
                .description(dto.getDescription())
                .imageUuid(dto.getImageUuid())
                .status(ReturnRequestStatus.PENDING)
                .build();

        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        // Mark the order item as having a return request
        orderItem.setHasReturnRequest(true);
        orderItem.setStatus(OrderItemStatus.RETURN_REQUESTED);
        orderItemRepository.save(orderItem);

        log.info("Return request submitted: returnRequestId={}, orderItemId={}, customerId={}",
                saved.getReturnRequestId(), dto.getOrderItemId(), customerId);

        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(saved)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestDto> getMyReturnRequests(Long customerId,
                                                                   Pageable pageable) {
        Page<ReturnRequestDto> page = returnRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(ReturnRequestMapper::toDto)
                .map(this::injectImage)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestDto getMyReturnById(Long returnRequestId, Long customerId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        if (!returnRequest.getCustomerId().equals(customerId)) {
            throw ReturnRequestExceptions.returnDoesNotBelongToCustomer();
        }
        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(returnRequest)));
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestDto getByOrderItemId(Long orderItemId, Long customerId) {
        ReturnRequest returnRequest = returnRequestRepository
                .findByOrderItem_OrderItemId(orderItemId)
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        if (!returnRequest.getCustomerId().equals(customerId)) {
            throw ReturnRequestExceptions.returnDoesNotBelongToCustomer();
        }
        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(returnRequest)));

    }

    // Store owner

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestDto> getReturnsByShop(Long shopId,
                                                                ReturnRequestSpec spec,
                                                                Pageable pageable) {
        Page<ReturnRequestDto> page = returnRequestRepository
                .findAll(spec, pageable)
                .map(ReturnRequestMapper::toDto)
                .map(this::injectImage)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestDto> getReturnsByShopAndStatus(Long shopId,
                                                                         ReturnRequestStatus status,
                                                                         Pageable pageable) {
        Page<ReturnRequestDto> page = returnRequestRepository
                .findByShopIdAndStatus(shopId, status, pageable)
                .map(ReturnRequestMapper::toDto)
                .map(this::injectImage)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestDto getReturnByIdForShop(Long returnRequestId, Long shopId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        if (!returnRequest.getShopId().equals(shopId)) {
            throw ReturnRequestExceptions.returnDoesNotBelongToShop();
        }
        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(returnRequest)));
    }

    @Override
    public ReturnRequestDto approve(Long returnRequestId, Long shopId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        if (!returnRequest.getShopId().equals(shopId)) {
            throw ReturnRequestExceptions.returnDoesNotBelongToShop();
        }

        if (returnRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw ReturnRequestExceptions.returnAlreadyProcessed();
        }

        returnRequest.setStatus(ReturnRequestStatus.APPROVED);

        OrderItem orderItem = returnRequest.getOrderItem();
        orderItem.setStatus(OrderItemStatus.RETURN_APPROVED);
        orderItemRepository.save(orderItem);

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        log.info("Return request APPROVED: returnRequestId={}, shopId={}, orderItemId={}",
                returnRequestId, shopId, orderItem.getOrderItemId());
        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(saved)));
    }

    @Override
    public ReturnRequestDto reject(Long returnRequestId, Long shopId,
                                   RejectReturnRequest request) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        if (!returnRequest.getShopId().equals(shopId)) {
            throw ReturnRequestExceptions.returnDoesNotBelongToShop();
        }

        if (returnRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw ReturnRequestExceptions.returnAlreadyProcessed();
        }

        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw ReturnRequestExceptions.rejectionReasonRequired();
        }

        returnRequest.setStatus(ReturnRequestStatus.REJECTED);
        returnRequest.setRejectionReason(request.getRejectionReason());

        OrderItem orderItem = returnRequest.getOrderItem();
        orderItem.setStatus(OrderItemStatus.RETURN_REJECTED);
        orderItemRepository.save(orderItem);

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        log.info("Return request REJECTED: returnRequestId={}, shopId={}, reason={}",
                returnRequestId, shopId, request.getRejectionReason());
        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(saved)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ReturnRequestStatus, Long> getReturnStatsByShop(Long shopId) {
        Map<ReturnRequestStatus, Long> counts = new EnumMap<>(ReturnRequestStatus.class);

        for (ReturnRequestStatus status : ReturnRequestStatus.values()) {
            counts.put(status, 0L);
        }

        for (Object[] row : returnRequestRepository.countByShopIdGroupedByStatus(shopId)) {
            counts.put((ReturnRequestStatus) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    // Admin

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestDto> getAllReturns(ReturnRequestSpec spec,
                                                             Pageable pageable) {
        Page<ReturnRequestDto> page = returnRequestRepository.findAll(spec, pageable)
                .map(ReturnRequestMapper::toDto)
                .map(this::injectImage)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ReturnRequestDto> getAllReturnsByStatus(ReturnRequestStatus status,
                                                                     Pageable pageable) {
        Page<ReturnRequestDto> page = returnRequestRepository.findByStatus(status, pageable)
                .map(ReturnRequestMapper::toDto)
                .map(this::injectImage)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestDto getReturnByIdForAdmin(Long returnRequestId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(ReturnRequestExceptions::returnRequestNotFound);

        return enrichmentService.enrich(injectImage(ReturnRequestMapper.toDto(returnRequest)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ReturnRequestStatus, Long> getAllReturnStats() {
        Map<ReturnRequestStatus, Long> counts = new EnumMap<>(ReturnRequestStatus.class);

        for (ReturnRequestStatus status : ReturnRequestStatus.values()) {
            counts.put(status, 0L);
        }

        for (Object[] row : returnRequestRepository.countAllGroupedByStatus()) {
            counts.put((ReturnRequestStatus) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    // Helpers

    private void validateImage(UUID uuid) {
        try {
            MediaResponse<FileDto> response = mediaManagerClient.getById(uuid);
            if (response.getErrorCodes() != null && !response.getErrorCodes().isEmpty()) {
                throw ReturnRequestExceptions.imageNotFound();
            }
            FileDto fileDto = response.getData();
            if (fileDto == null) {
                throw ReturnRequestExceptions.imageNotFound();
            }
            if (!isImage(fileDto.getMimeType())) {
                throw ReturnRequestExceptions.invalidFileType();
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw ReturnRequestExceptions.imageNotFound();
            }
            log.warn("Could not verify image uuid={} with media-manager: status={}, message={}",
                    uuid, e.status(), e.getMessage());
            throw ReturnRequestExceptions.mediaServiceUnavailable();
        }
    }

    private ReturnRequestDto injectImage(ReturnRequestDto dto) {
        if (dto.getImageUuid() == null) return dto;
        try {
            MediaResponse<FileDto> response = mediaManagerClient.getById(dto.getImageUuid());
            if (response != null && response.getData() != null) {
                dto.setImage(response.getData());
            }
        } catch (FeignException e) {
            log.debug("Could not inject image URL for returnRequest uuid={}: status={}",
                    dto.getImageUuid(), e.status());
        } catch (Exception e) {
            log.debug("Could not inject image URL for returnRequest uuid={}: {}",
                    dto.getImageUuid(), e.getMessage());
        }
        return dto;
    }

    private boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

}
