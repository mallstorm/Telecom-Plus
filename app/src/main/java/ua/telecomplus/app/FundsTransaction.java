package ua.telecomplus.app;

import java.util.Locale;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class FundsTransaction {
    static final String CREDIT = "credit";
    static final String DEBIT = "debit";
    static final String NEUTRAL = "neutral";
    final String amount;
    final String balanceAfter;
    final String category;
    final String dateTime;
    final String description;
    final String type;

    public FundsTransaction(String str, String str2, String str3, String str4, String str5, String str6) {
        this.dateTime = safe(str);
        this.description = safe(str2);
        this.category = safe(str3);
        this.amount = safe(str4);
        this.balanceAfter = safe(str5);
        String lowerCase = str6 == null ? "" : str6.trim().toLowerCase(Locale.ROOT);
        if (!CREDIT.equals(lowerCase) && !DEBIT.equals(lowerCase)) {
            lowerCase = NEUTRAL;
        }
        this.type = lowerCase;
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }

    public boolean isCredit() {
        return CREDIT.equals(this.type);
    }

    public boolean isDebit() {
        return DEBIT.equals(this.type);
    }
}
