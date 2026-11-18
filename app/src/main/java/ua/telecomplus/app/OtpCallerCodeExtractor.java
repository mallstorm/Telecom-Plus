package ua.telecomplus.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class OtpCallerCodeExtractor {
    private static final Pattern PHONE_LIKE = Pattern.compile("(?<![0-9])(?:\\+?[0-9][0-9 ()\\-.]{7,}[0-9])(?![0-9])");
    private static final Pattern DATE_LIKE = Pattern.compile("(?:\\b[0-9]{1,2}[./-][0-9]{1,2}[./-][0-9]{2,4}\\b|\\b[0-9]{4}[./-][0-9]{1,2}[./-][0-9]{1,2}\\b)");

    private OtpCallerCodeExtractor() {
    }

    public static String extract(String str) {
        Pattern pattern = PHONE_LIKE;
        if (str == null) {
            str = "";
        }
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            if (!DATE_LIKE.matcher(group).find()) {
                String replaceAll = group.replaceAll("\\D+", "");
                if (replaceAll.length() >= 9 && replaceAll.length() <= 15) {
                    return replaceAll.substring(replaceAll.length() - 4);
                }
            }
        }
        return null;
    }
}
