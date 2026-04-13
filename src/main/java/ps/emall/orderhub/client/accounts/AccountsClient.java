package ps.emall.orderhub.client.accounts;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "accounts-service", url = "${services.accounts.host}:${services.accounts.port}")
public interface AccountsClient {

    @GetMapping("/api/shops/info/{shopId}")
    AccountsShopResponse getShopById(@PathVariable("shopId") Long shopId);

    @GetMapping("/api/users/{userId}/info")
    AccountsUserResponse getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/cities/{cityId}")
    AccountsCityResponse getCityById(@PathVariable("cityId") Long cityId);

    @GetMapping("/api/malls/{mallId}")
    AccountsMallResponse getMallById(@PathVariable("mallId") Long mallId);
}
