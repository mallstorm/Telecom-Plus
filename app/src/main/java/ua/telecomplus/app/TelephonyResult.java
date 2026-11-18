package ua.telecomplus.app;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public abstract class TelephonyResult<T> {

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Failure<T> extends TelephonyResult<T> {
        final String code;
        final String message;

        public Failure(String str, String str2) {
            super(0);
            this.code = str == null ? "UNKNOWN" : str;
            this.message = (str2 == null || str2.trim().isEmpty()) ? "Неизвестная ошибка телефонии" : str2.trim();
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Success<T> extends TelephonyResult<T> {
        final T value;

        public Success(T t) {
            super(0);
            this.value = t;
        }
    }

    public /* synthetic */ TelephonyResult(int i) {
        this();
    }

    public static <T> Failure<T> failure(String str, String str2) {
        return new Failure<>(str, str2);
    }

    public static <T> Success<T> success(T t) {
        return new Success<>(t);
    }

    private TelephonyResult() {
    }
}
