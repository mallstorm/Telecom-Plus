package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class CallHistorySnapshot {
    final List<CallDetail> calls;
    final String periodLabel;
    final String totalDuration;
    final long updatedAt;

    public CallHistorySnapshot(String str, String str2, List<CallDetail> list, long j) {
        this.periodLabel = safe(str);
        this.totalDuration = safe(str2);
        this.calls = list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList(list));
        this.updatedAt = j;
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }

    public CallHistorySnapshot(String str, String str2, List<CallDetail> list) {
        this(str, str2, list, System.currentTimeMillis());
    }
}
