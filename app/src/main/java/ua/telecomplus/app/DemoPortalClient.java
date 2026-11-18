package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ua.telecomplus.app.PortalResult;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class DemoPortalClient implements SubscriberPortalGateway {
    private static final String KEY = "demo_session_v3";
    private boolean loggedIn;
    private final SecureVault vault;

    public DemoPortalClient(SecureVault secureVault) {
        this.vault = secureVault;
        this.loggedIn = "yes".equals(secureVault.get(KEY));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<CallHistorySnapshot> acceptCallHistoryHtml(String str) {
        return getCallHistory();
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<FundsFlowSnapshot> acceptFundsFlowHtml(String str) {
        return getFundsFlow();
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> confirmOtp(String str, String str2) {
        if (!"1498".equals(str2)) {
            return PortalResult.failure("В demo-режиме код: 1498");
        }
        this.loggedIn = true;
        this.vault.put(KEY, "yes");
        return PortalResult.success(null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public CallHistorySnapshot getCachedCallHistory() {
        PortalResult<CallHistorySnapshot> callHistory = getCallHistory();
        if (callHistory instanceof PortalResult.Success) {
            return (CallHistorySnapshot) ((PortalResult.Success) callHistory).value;
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public DashboardSnapshot getCachedDashboard() {
        PortalResult<DashboardSnapshot> dashboard = getDashboard();
        if (dashboard instanceof PortalResult.Success) {
            return (DashboardSnapshot) ((PortalResult.Success) dashboard).value;
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public FundsFlowSnapshot getCachedFundsFlow() {
        PortalResult<FundsFlowSnapshot> fundsFlow = getFundsFlow();
        if (fundsFlow instanceof PortalResult.Success) {
            return (FundsFlowSnapshot) ((PortalResult.Success) fundsFlow).value;
        }
        return null;
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public List<ServiceItem> getCachedServices() {
        PortalResult<List<ServiceItem>> services = getServices();
        return services instanceof PortalResult.Success ? (List) ((PortalResult.Success) services).value : new ArrayList();
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<CallHistorySnapshot> getCallHistory() {
        return PortalResult.success(new CallHistorySnapshot("Текущий месяц", "8 мин 57 сек", Arrays.asList(new CallDetail("29.07.2026 14:32", "Украина — мобильный", "050 123 45 67", "6 мин 44 сек", "0.00 грн"), new CallDetail("28.07.2026 18:10", "Украина — городской", "044 555 12 34", "2 мин 13 сек", "0.00 грн"))));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<DashboardSnapshot> getDashboard() {
        return PortalResult.success(new DashboardSnapshot("094 000 00 00", "Активный", "40.01 грн", "40.01 грн", "IT Phone Online", Arrays.asList(new PackageBalance("Сеть ИТ", "Неограниченно до 07.08.2026", 100), new PackageBalance("Украина (фикс+моб)", "1 час 29 минут 50 секунд до 07.08.2026", 72)), new SubscriberProfile("1000000", "Без контракта", "0940000000", "Не зарегистрирован", "IT Phone Online", "1000000000", "0500000000", "demo@example.com", "29.08.2024", "04.2027", "1 год 11 месяцев", "1 год 11 месяцев"), "06 минут 44 секунды"));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<FundsFlowSnapshot> getFundsFlow() {
        return PortalResult.success(new FundsFlowSnapshot("Июль 2026", "10.36 грн", "+50.00 грн", "-20.35 грн", "40.01 грн", Arrays.asList(new FundsTransaction("29.07.2026 09:14", "Абонентская плата по тарифу IT Phone Online", "Тариф", "-20.00 грн", "40.01 грн", "debit"), new FundsTransaction("26.07.2026 18:42", "Пополнение банковской картой", "Пополнение", "+50.00 грн", "60.01 грн", "credit"), new FundsTransaction("21.07.2026 11:05", "Исходящий вызов вне пакета", "Связь", "-0.35 грн", "10.01 грн", "debit"))));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<List<ServiceItem>> getServices() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ServiceItem("1", "МГ + МН связь + голосовой контент", "бесплатно", "Подключена"));
        arrayList.add(new ServiceItem("2", "Услуга «Комплит»", "бесплатно", "Подключена"));
        arrayList.add(new ServiceItem("3", "Отправка SMS", "бесплатно", "Подключена"));
        arrayList.add(new ServiceItem("4", "SIP-номер (30 дней)", "бесплатно", "Подключена"));
        arrayList.add(new ServiceItem("5", "ИНТЕРНЕТ-доступ", "бесплатно", "Подключена"));
        return PortalResult.success(arrayList);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public String getSessionCookieHeader() {
        return "";
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public boolean hasSavedSession() {
        return this.loggedIn;
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> importBrowserSession(String str, String str2) {
        this.loggedIn = true;
        this.vault.put(KEY, "yes");
        return PortalResult.success(null);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> loginWithPassword(String str, String str2) {
        if ((str != null ? str.replaceAll("\\D+", "") : "").length() != 9) {
            return PortalResult.failure("Введите 9-значный логин");
        }
        if (!"demo".equals(str2)) {
            return PortalResult.failure("В demo-режиме пароль: demo");
        }
        this.loggedIn = true;
        this.vault.put(KEY, "yes");
        return PortalResult.success(null);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> logout(boolean z) {
        this.loggedIn = false;
        SecureVault secureVault = this.vault;
        if (z) {
            secureVault.clear();
        } else {
            secureVault.remove(KEY);
        }
        return PortalResult.success(null);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Integer> requestOtp(String str) {
        return (str != null ? str.replaceAll("\\D+", "") : "").length() >= 9 ? PortalResult.success(600) : PortalResult.failure("Введите номер");
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> validateSession() {
        return this.loggedIn ? PortalResult.success(null) : PortalResult.failure("Нет сессии", true);
    }
}
