package ps.emall.orderhub.dashboard;

public interface DashboardService {
    AdminDashboardDto getAdminDashboard();
    ShopDashboardDto getShopDashboard(Long shopId);
    CustomerDashboardDto getCustomerDashboard(Long customerId);
}