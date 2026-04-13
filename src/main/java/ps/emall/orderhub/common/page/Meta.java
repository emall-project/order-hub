package ps.emall.orderhub.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {
    private int page;
    private int size;
    private int totalPages;
    private long totalItems;

    private boolean hasNext;
    private boolean hasPrev;

    private int from;
    private int to;

    public static Meta fromPage(Page<?> page) {
        int from = (page.getNumber() * page.getSize()) + 1;
        int to = Math.min((page.getNumber() + 1) * page.getSize(),
                (int)page.getTotalElements());

        return Meta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .from(from)
                .to(to)
                .build();
    }

}

