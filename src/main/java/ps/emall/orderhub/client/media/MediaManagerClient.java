package ps.emall.orderhub.client.media;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "media-manager-service",
        url = "${services.media-manager.host}:${services.media-manager.port}")
public interface MediaManagerClient {

    @GetMapping("/files/{id}")
    MediaResponse<FileDto> getById(@PathVariable("id") UUID id);
}
