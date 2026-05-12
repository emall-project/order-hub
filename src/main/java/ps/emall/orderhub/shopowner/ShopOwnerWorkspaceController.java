package ps.emall.orderhub.shopowner;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.common.response.EMallsResponseEntity;

@RestController
@RequestMapping("/shop-owner")
@RequiredArgsConstructor
public class ShopOwnerWorkspaceController {

    private final ShopOwnerWorkspaceService workspaceService;

    @GetMapping("/shops/{shopId}/workspace")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ShopOwnerWorkspaceDto> getWorkspace(
            @PathVariable @Positive Long shopId) {
        return EMallsResponseEntity.ok(workspaceService.getWorkspace(shopId));
    }
}
