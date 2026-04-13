package ps.emall.orderhub.returnrequest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ps.emall.orderhub.client.accounts.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnRequestEnrichmentService {

    private final AccountsClient accountsClient;

    public ReturnRequestDto enrich(ReturnRequestDto dto) {
        if (dto == null) return null;
        dto.setCustomerInfo(fetchCustomerInfo(dto.getCustomerId()));
        dto.setShopInfo(fetchShopInfo(dto.getShopId()));
        return dto;
    }

    private UserInfoDto fetchCustomerInfo(Long customerId) {
        if (customerId == null) return null;
        try {
            AccountsUserResponse response = accountsClient.getUserById(customerId);
            return response != null ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Customer not found: customerId={}", customerId);
        } catch (FeignException e) {
            log.debug("Accounts unavailable fetching customerId={}, status={}", customerId, e.status());
        } catch (Exception e) {
            log.debug("Error fetching customerId={}: {}", customerId, e.getMessage());
        }
        return null;
    }

    private ShopInfoDto fetchShopInfo(Long shopId) {
        if (shopId == null) return null;
        try {
            AccountsShopResponse response = accountsClient.getShopById(shopId);
            return response != null ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Shop not found: shopId={}", shopId);
        } catch (FeignException e) {
            log.debug("Accounts unavailable fetching shopId={}, status={}", shopId, e.status());
        } catch (Exception e) {
            log.debug("Error fetching shopId={}: {}", shopId, e.getMessage());
        }
        return null;
    }
}