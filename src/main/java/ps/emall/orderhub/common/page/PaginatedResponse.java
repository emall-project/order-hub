package ps.emall.orderhub.common.page;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private Meta meta;

    public static <T> PaginatedResponse<T> of(Page<T> page) {
        if(page == null) {
            return PaginatedResponse.<T>builder().build();
        }

        return PaginatedResponse.<T>builder()
                .content(page.getContent())
                .meta(Meta.fromPage(page))
                .build();
    }

}
