package ua.telecomplus.app;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public abstract class PortalResult<T> {

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Success<T> extends PortalResult<T> {
        final T value;

        public Success(T t) {
            super(0);
            this.value = t;
        }
    }

    public /* synthetic */ PortalResult(int i) {
        this();
    }

    public static <T> Failure<T> failure(String str) {
        return new Failure<>(str, false);
    }

    public static <T> Success<T> success(T t) {
        return new Success<>(t);
    }

    private PortalResult() {
    }

    public static <T> Failure<T> failure(String str, boolean z) {
        return new Failure<>(str, z);
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Failure<T> extends PortalResult<T> {
        final String message;
        final boolean unauthorized;

        public Failure(String str, boolean z) {
            super(0);
            this.message = (str == null || str.trim().isEmpty()) ? "Неизвестная ошибка" : str.trim();
            this.unauthorized = z;
        }

        public Failure(String str) {
            this(str, false);
        }
    }
}
