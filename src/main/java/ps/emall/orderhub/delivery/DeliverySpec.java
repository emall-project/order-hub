package ps.emall.orderhub.delivery;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(params = "status",        path = "status",    spec = Equal.class),
        @Spec(params = "cartId",        path = "cartId",    spec = Equal.class),
        @Spec(params = "createdAfter",  path = "createdAt", spec = GreaterThanOrEqual.class),
        @Spec(params = "createdBefore", path = "createdAt", spec = LessThanOrEqual.class)
})
public interface DeliverySpec extends Specification<Delivery> {}
