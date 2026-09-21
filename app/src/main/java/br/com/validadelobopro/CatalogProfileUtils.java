package br.com.validadelobopro;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CatalogProfileUtils {
    public static final String PROFILE_P1 = "p1";
    public static final String PROFILE_P2 = "p2";
    public static final String PROFILE_P3 = "p3";
    public static final String PROFILE_P4 = "p4";
    public static final String PROFILE_P5 = "p5";
    public static final String PROFILE_P6 = "p6";
    public static final String PROFILE_P7 = "p7";
    public static final String PROFILE_NEW_BUSINESS = "new_business";
    private static final String PREF_SELECTED_PROFILE = "selected_catalog_profile";

    private CatalogProfileUtils() {
    }

    public static String defaultProfileKey() {
        return PROFILE_NEW_BUSINESS;
    }

    public static String normalizeProfileKey(String profileKey) {
        if (profileKey == null || profileKey.trim().isEmpty()) {
            return defaultProfileKey();
        }

        String normalized = profileKey.trim().toLowerCase(Locale.ROOT).replace(" ", "");
        switch (normalized) {
            case PROFILE_P1:
                return PROFILE_P1;
            case PROFILE_P2:
                return PROFILE_P2;
            case PROFILE_P3:
                return PROFILE_P3;
            case PROFILE_P4:
                return PROFILE_P4;
            case PROFILE_P5:
                return PROFILE_P5;
            case PROFILE_P6:
                return PROFILE_P6;
            case PROFILE_P7:
                return PROFILE_P7;
            case PROFILE_NEW_BUSINESS:
            case "novacontaempresarial":
            case "nova-conta-empresarial":
            case "novaconta":
            case "nova-conta":
                return PROFILE_NEW_BUSINESS;
            default:
                return normalized.startsWith("p") && normalized.length() > 1 ? normalized : defaultProfileKey();
        }
    }

    public static List<String> profileLabels() {
        return Arrays.asList(
                "P1",
                "P2",
                "P3",
                "P4",
                "P5",
                "P6",
                "P7",
                "Padaria"
        );
    }

    public static List<String> profileKeys() {
        return Arrays.asList(PROFILE_P1, PROFILE_P2, PROFILE_P3, PROFILE_P4, PROFILE_P5, PROFILE_P6, PROFILE_P7, PROFILE_NEW_BUSINESS);
    }

    public static String labelForProfile(String profileKey) {
        String normalized = normalizeProfileKey(profileKey);
        switch (normalized) {
            case PROFILE_P1:
                return "P1";
            case PROFILE_P2:
                return "P2";
            case PROFILE_P3:
                return "P3";
            case PROFILE_P4:
                return "P4";
            case PROFILE_P5:
                return "P5";
            case PROFILE_P6:
                return "P6";
            case PROFILE_P7:
                return "P7";
            case PROFILE_NEW_BUSINESS:
                return "Padaria";
            default:
                return "Padaria";
        }
    }

    public static String catalogJsonKey(String profileKey) {
        return "catalog_json_" + normalizeProfileKey(profileKey);
    }

    public static String catalogUrlKey(String profileKey) {
        return "catalog_url_" + normalizeProfileKey(profileKey);
    }

    public static String selectedProfileKey(SharedPreferences prefs) {
        if (prefs == null) {
            return defaultProfileKey();
        }

        String saved = prefs.getString(PREF_SELECTED_PROFILE, null);
        return normalizeProfileKey(saved);
    }

    public static void saveSelectedProfile(SharedPreferences prefs, String profileKey) {
        if (prefs == null) {
            return;
        }

        prefs.edit().putString(PREF_SELECTED_PROFILE, normalizeProfileKey(profileKey)).apply();
    }

    public static boolean isNewBusinessProfile(String profileKey) {
        return PROFILE_NEW_BUSINESS.equals(normalizeProfileKey(profileKey));
    }

    public static boolean isPrivilegedProfile(String profileKey) {
        return PROFILE_P2.equals(normalizeProfileKey(profileKey));
    }

    public static String profileSummary(String profileKey) {
        String normalized = normalizeProfileKey(profileKey);
        if (isNewBusinessProfile(normalized)) {
            return "Padaria (configuracao manual)";
        }
        if (isPrivilegedProfile(normalized)) {
            return "P2 (perfil privilegiado, catalogo padrao preservado)";
        }
        return labelForProfile(normalized) + " (catalogo isolado)";
    }
}

