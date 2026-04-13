package ps.emall.orderhub.finance;

import java.util.Map;

public interface FinanceService {

    FinanceOverviewDto getFinanceOverview();

    ShopPayoutDto getShopPayout(Long shopId);

    Map<String, Long> getOrderItemStatusDistribution();

    Map<String, Long> getShopReturnStats(Long shopId);
}