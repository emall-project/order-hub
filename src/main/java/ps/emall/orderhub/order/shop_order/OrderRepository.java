package ps.emall.orderhub.order.shop_order;

import org.springframework.data.repository.Repository;
import ps.emall.orderhub.order.order.Order;

interface OrderRepository extends Repository<Order, Long> {
}
