package ps.emall.orderhub.client.catalog;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "catalog-service", url = "${services.catalog.host}:${services.catalog.port}")
public interface CatalogClient {

    @GetMapping("/products/{productId}/info")
    CatalogResponse getProductInfo(@PathVariable("productId") Long productId);

    @PostMapping("/products/by-ids")
    CatalogProductsResponse getProductsByIds(@RequestBody ProductIdsRequest request);
}
