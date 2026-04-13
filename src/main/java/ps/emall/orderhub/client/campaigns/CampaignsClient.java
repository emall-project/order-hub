package ps.emall.orderhub.client.campaigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "campaigns-service", url = "${services.campaigns.host}:${services.campaigns.port}")
public interface CampaignsClient {

    @GetMapping("/api/offers/product/{productId}/active-price")
    CampaignsOfferResponse getActiveOfferForProduct(@PathVariable("productId") Long productId);
}
