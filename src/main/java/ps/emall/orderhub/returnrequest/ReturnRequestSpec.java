package ps.emall.orderhub.returnrequest;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(params = "customerId",    path = "customerId", spec = Equal.class),
        @Spec(params = "shopId",        path = "shopId",     spec = Equal.class),
        @Spec(params = "status",        path = "status",     spec = Equal.class),
        @Spec(params = "createdAfter",  path = "createdAt",  spec = GreaterThanOrEqual.class),
        @Spec(params = "createdBefore", path = "createdAt",  spec = LessThanOrEqual.class)
})
public interface ReturnRequestSpec extends Specification<ReturnRequest> {}
