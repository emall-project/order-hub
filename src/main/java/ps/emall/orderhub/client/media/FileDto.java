package ps.emall.orderhub.client.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDto {
    private UUID id;
    private String name;
    private Long folderId;
    private String mimeType;
    private String extension;
    private Integer size;
    private String originalFileUrl;
    private String mediumFileUrl;
    private String smallFileUrl;
}
