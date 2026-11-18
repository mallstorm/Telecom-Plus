package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class FundsFlowSnapshot {
    final String closingBalance;
    final String creditsTotal;
    final String debitsTotal;
    final String openingBalance;
    final String periodLabel;
    final List<FundsTransaction> transactions;
    final long updatedAt;

    public FundsFlowSnapshot(String str, String str2, String str3, String str4, String str5, List<FundsTransaction> list, long j) {
        this.periodLabel = safe(str);
        this.openingBalance = safe(str2);
        this.creditsTotal = safe(str3);
        this.debitsTotal = safe(str4);
        this.closingBalance = safe(str5);
        this.transactions = list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList(list));
        this.updatedAt = j;
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }

    public int creditCount() {
        Iterator<FundsTransaction> it = this.transactions.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().isCredit()) {
                i++;
            }
        }
        return i;
    }

    public int debitCount() {
        Iterator<FundsTransaction> it = this.transactions.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().isDebit()) {
                i++;
            }
        }
        return i;
    }

    public FundsFlowSnapshot(String str, String str2, String str3, String str4, String str5, List<FundsTransaction> list) {
        this(str, str2, str3, str4, str5, list, System.currentTimeMillis());
    }
}
