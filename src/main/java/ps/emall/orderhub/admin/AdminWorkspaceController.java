package ps.emall.orderhub.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.common.response.EMallsResponseEntity;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWorkspaceController {

    private final AdminWorkspaceService workspaceService;

    @GetMapping("/workspace")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<AdminWorkspaceDto> getWorkspace() {
        return EMallsResponseEntity.ok(workspaceService.getWorkspace());
    }
}
