package br.com.validadelobopro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements LifecycleOwner {
    private static final String TAG = "ValidadeLoboPro";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_CONNECT = 260;
    private static final int REQUEST_PRICE_IMAGE = 261;
    private static final int REQUEST_VALIDITY_IMAGE = 262;
    private static final int REQUEST_ONLY_PRICE_IMAGE = 263;
    private static final int REQUEST_POST_NOTIFICATIONS = 264;
    private static final int REQUEST_CAMERA = 265;
    private static final int REQUEST_IMAGE_ONLY_IMAGE = 266;
    private static final int REQUEST_PHOTO_50X30_IMAGE = 267;
    private static final String PREFS_NAME = "catalogo";
    private static final String PREF_CATALOG_JSON = "catalog_json";
    private static final String PREF_CATALOG_URL = "catalog_url";
    private static final String PREF_BLUETOOTH_MODE = "bluetooth_mode";
    private static final String PREF_PRINTER_MODEL = "printer_model";
    private static final String PREF_PRINTER_SETTINGS_BACKUP_JSON = "printer_settings_backup_json";
    private static final String PREF_PRINTER_FAILURE_COUNT = "printer_failure_count";
    private static final String PREF_BETA_UNLOCKED = "beta_unlocked";
    private static final String PREF_MAIN_PAGE = "main_page";
    private static final String PREF_PEIXARIA_LOTE_SEQUENCE = "padaria_lote_sequence";
    private static final String PREF_PEIXARIA_LOTE_YEAR = "padaria_lote_year";
    private static final String PREF_PEIXARIA_HISTORY_JSON = "padaria_history_json";
    private static final String PREF_PADARIA_ADDRESS = "padaria_address";
    private static final String PREF_ESTABLISHMENT_NAME = "establishment_name";
    private static final String DEFAULT_PADARIA_ADDRESS = "Av. Mal. Floriano Peixoto, 260 - Poiares, Caraguatatuba - SP, 11673-000";
    private static final String PEIXARIA_CATALOG_FILE = "produtos-rastreabilidade-padaria.json";
    private static final String PREF_PRINT_HISTORY_JSON = "print_history_json";
    private static final String PREF_PENDING_PRINT_SYNC_JSON = "pending_print_sync_json";
    private static final String PREF_OCR_ALERTS_JSON = "ocr_alerts_json";
    private static final String PREF_LAST_REMINDER_SIGNATURE = "last_reminder_signature";
    private static final String PREF_INSTALLATION_ID = "installation_id";
    private static final String PREF_SUPABASE_URL = "supabase_url";
    private static final String PREF_SUPABASE_ANON_KEY = "supabase_anon_key";
    private static final String PREF_SUPABASE_CONFIG_CHECKED_AT = "supabase_config_checked_at";
    private static final String PREF_ACTIVE_ESTABLISHMENT_SLUG = "active_establishment_slug";
    private static final String PREF_ACTIVE_ESTABLISHMENT_PASSWORD = "active_establishment_password";
    private static final String SUPABASE_RPC_UPSERT_DEVICE = "lobo_pro_upsert_device";
    private static final String SUPABASE_RPC_RECORD_PRINT = "lobo_pro_record_print";
    private static final String SUPABASE_RPC_RECORD_CHECK = "lobo_pro_record_check";
    private static final String SUPABASE_RPC_CLOUD_DASHBOARD = "lobo_pro_cloud_dashboard";
    private static final String SUPABASE_RPC_CREATE_ESTABLISHMENT = "lobo_pro_create_establishment";
    private static final String BETA_LOGIN = "lobo";
    private static final String BETA_PASSWORD = "lobopt260";
    private static final String LEGACY_BETA_LOGIN = "beta";
    private static final String LEGACY_BETA_PASSWORD = "pt260";
    private static final String MAIN_PAGE_VALIDITY = "validity";
    private static final String MAIN_PAGE_PEIXARIA = "padaria";
    private static final int MAX_PEIXARIA_HISTORY = 500;
    private static final String DEFAULT_CATALOG_URL = "https://raw.githubusercontent.com/fabiodkk/validade-lobo-pro-pt260-apk/master/produtos-validade-lobo.json";
    private static final String UPDATE_INFO_URL = "https://raw.githubusercontent.com/fabiodkk/validade-lobo-pro-pt260-apk/master/update.json";
    private static final String SUPABASE_CONFIG_URL = "https://raw.githubusercontent.com/fabiodkk/validade-lobo-pro-pt260-apk/master/supabase-config.json";
    private static final String PT260_BLE_WRITE_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3";
    private static final String PRINTER_MODEL_AUTO = "Automatico";
    private static final String PRINTER_MODEL_PT260 = "PT260";
    private static final String PRINTER_MODEL_PT260_XD210 = "PT260 XD210";
    private static final String PRINTER_MODEL_B1 = "NIIMBOT B1";
    private static final String NIIMBOT_B1_MAC = "03:05:01:F4:C1:4F";
    private static final int PRINT_FAILURE_RESTORE_THRESHOLD = 10;
    private static final String NOTIFICATION_CHANNEL_ID = "validade_recolhimento";
    private static final String NOTIFICATION_OCR_CHANNEL_ID = "validade_ocr_alertas";
    private static final String EXTRA_OPEN_ALERTS = "open_alerts";
    private static final int NOTIFICATION_EXPIRY_REMINDER_ID = 26010;
    private static final int NOTIFICATION_OCR_ALERT_ID = 26011;
    private static final int PRINT_ALERT_THRESHOLD = 10;
    private static final int MAX_PRINT_HISTORY = 250;
    private static final int MAX_OCR_ALERTS = 250;
    private static final long OCR_SCAN_INTERVAL_MS = 900;
    private static final long OCR_DUPLICATE_INTERVAL_MS = 8000;
    private static final long SUPABASE_CONFIG_REFRESH_INTERVAL_MS = 10 * 60 * 1000;
    private static final Pattern OCR_DATE_PATTERN = Pattern.compile("\\b(\\d{1,2})[\\/\\-.](\\d{1,2})(?:[\\/\\-.](\\d{2,4}))?\\b");
    private static final int COLOR_BACKGROUND = 0xFF07131B;
    private static final int COLOR_SURFACE = 0xD91A2A33;
    private static final int COLOR_SURFACE_ALT = 0xCC123B43;
    private static final int COLOR_PRIMARY = 0xFF238F84;
    private static final int COLOR_PRIMARY_DARK = 0xFF67D9D0;
    private static final int COLOR_ACCENT = 0xFF67D9D0;
    private static final int COLOR_TEXT = 0xFFF1F7F7;
    private static final int COLOR_MUTED = 0xFFB4C5C8;
    private static final int COLOR_BORDER = 0xFF52747A;
    private static final int COLOR_STATUS = 0xCC153743;

    private static final List<String> PRODUTOS = Arrays.asList(
            "Fatia de pizza estufa 70C",
            "Pizza mussarela fatia",
            "Pizza calabresa fatia",
            "Pizza frango catupiry fatia",
            "Salgados estufa 70C",
            "Coxinha",
            "Bolinho de carne",
            "Kibe",
            "Quibe",
            "Bolinho de pernil",
            "Bolinha de queijo",
            "Risole",
            "Croquete",
            "Empada",
            "Esfiha",
            "Enroladinho de salsicha",
            "Pastel assado",
            "Mousse morango",
            "Mousse goiaba",
            "Pudim",
            "Mousse ninho",
            "Creme de leite / ninho",
            "Gelatina mosaica",
            "Creme de abacaxi",
            "Mousse abacaxi",
            "Mousse amora",
            "Mousse limao",
            "Abacaxi/coco",
            "Mousse chocolate/creme",
            "Pimenta apos aberta",
            "Torta maracuja",
            "Pave argentina",
            "Bolo",
            "Brigadeiro",
            "Mousse coco",
            "Doce de leite",
            "Chocolate c/ ninho",
            "Mousse maracuja",
            "Mousse abacaxi c/ coco",
            "Mousse pudim",
            "Mousse chocolate",
            "Mousse manga",
            "Mousse uva",
            "Mousse pessego",
            "Mousse cupuacu",
            "Mousse acai",
            "Mousse leite ninho",
            "Mousse ninho c/ morango",
            "Mousse nutella",
            "Mousse oreo",
            "Mousse doce de leite",
            "Mousse pacoca",
            "Mousse brigadeiro",
            "Mousse beijinho",
            "Mousse leite condensado",
            "Mousse ameixa",
            "Mousse goiabada",
            "Mousse banana",
            "Mousse banana c/ canela",
            "Mousse kiwi",
            "Mousse frutas vermelhas",
            "Mousse cereja",
            "Mousse framboesa",
            "Mousse mirtilo",
            "Mousse tangerina",
            "Mousse laranja",
            "Mousse melancia",
            "Mousse melao",
            "Mousse cafe",
            "Mousse capuccino",
            "Mousse caramelo",
            "Mousse chocolate branco",
            "Mousse choc. meio amargo",
            "Mousse alpino",
            "Mousse sonho de valsa",
            "Mousse ouro branco",
            "Mousse ovomaltine",
            "Mousse churros",
            "Mousse coco queimado",
            "Mousse tapioca",
            "Mousse baunilha",
            "Mousse pistache",
            "Mousse amendoim",
            "Bife ancho",
            "Mandioca",
            "Berinjela",
            "Carne de yakssoba",
            "File mignon",
            "Recheio de pastel carne",
            "Recheio de pastel frango",
            "Recheio de pastel camarao",
            "Recheio de pastel palmito",
            "Recheio de pastel calabresa",
            "Recheio de pastel pizza"
    );

    private static final List<ValidityRule> DEFAULT_RULES = Arrays.asList(
            new ValidityRule("pimenta", 30, 0, "Aberto"),
            new ValidityRule("recheio de pastel", 90, 0, "Fab"),
            new ValidityRule("bife ancho", 90, 0, "Fab"),
            new ValidityRule("mandioca", 90, 0, "Fab"),
            new ValidityRule("berinjela", 90, 0, "Fab"),
            new ValidityRule("carne de yakssoba", 90, 0, "Fab"),
            new ValidityRule("file mignon", 90, 0, "Fab"),
            new ValidityRule("pizza", 0, 6, "Forno"),
            new ValidityRule("salgado", 0, 6, "Pronto"),
            new ValidityRule("coxinha", 0, 6, "Pronto"),
            new ValidityRule("bolinho", 0, 6, "Pronto"),
            new ValidityRule("bolinha", 0, 6, "Pronto"),
            new ValidityRule("kibe", 0, 6, "Pronto"),
            new ValidityRule("quibe", 0, 6, "Pronto"),
            new ValidityRule("risole", 0, 6, "Pronto"),
            new ValidityRule("empada", 0, 6, "Pronto"),
            new ValidityRule("esfiha", 0, 6, "Pronto"),
            new ValidityRule("enroladinho", 0, 6, "Pronto"),
            new ValidityRule("pastel", 0, 6, "Pronto"),
            new ValidityRule("croquete", 0, 6, "Pronto"),
            new ValidityRule("*", 5, 0, "Fab")
    );

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    private final SimpleDateFormat shortDateTimeFormat = new SimpleDateFormat("dd/MM HH:mm", new Locale("pt", "BR"));

    private Spinner produtoSpinner;
    private EditText produtoEdit;
    private EditText dataEdit;
    private EditText validadeEdit;
    private EditText validadeManualEdit;
    private EditText copiasEdit;
    private EditText priceNameEdit;
    private EditText priceValueEdit;
    private EditText priceCopiesEdit;
    private EditText onlyPriceValueEdit;
    private EditText onlyPriceCopiesEdit;
    private EditText textOnlyEdit;
    private EditText textOnlyCopiesEdit;
    private EditText imageOnlyCopiesEdit;
    private EditText photo50x30CopiesEdit;
    private CheckBox onlyPriceRepeatCheck;
    private EditText catalogUrlEdit;
    private Spinner peixeiroSpinner;
    private TextView currentProfileBanner;
    private EditText establishmentNameEdit;
    private EditText aboutAppEdit;
    private EditText betaRuleMatchEdit;
    private EditText betaRuleDaysEdit;
    private EditText betaRuleHoursEdit;
    private EditText betaRuleLabelEdit;
    private Spinner metodoSpinner;
    private Spinner printerModelSpinner;
    private Spinner fontSizeSpinner;
    private Spinner priceFontSizeSpinner;
    private Spinner onlyPriceFontSizeSpinner;
    private Spinner onlyPriceRepeatCountSpinner;
    private Spinner betaRuleSpinner;
    private TextView statusText;
    private TextView imageStatusText;
    private TextView validityImageStatusText;
    private TextView onlyPriceImageStatusText;
    private TextView textOnlyPreviewText;
    private TextView imageOnlyStatusText;
    private TextView photo50x30StatusText;
    private TextView alertsSummaryText;
    private TextView alertsListText;
    private TextView historySummaryText;
    private TextView historyListText;
    private TextView cloudSummaryText;
    private TextView cloudExpiringText;
    private TextView cloudDevicesText;
    private TextView cloudRecentPrintsText;
    private TextView cloudRecentChecksText;
    private TextView ocrStatusText;
    private TextView ocrResultText;
    // transient override used to prefer a specific printer model for the current print
    // This is set when we detect a known device (e.g. PT-260_7D0C) and cleared after the
    // print operation so global settings are not changed.
    private volatile String transientPrinterModelOverride = null;

    @FunctionalInterface
    private interface PayloadBuilder {
        List<byte[]> build() throws IOException;
    }

    private void setTransientPrinterModelOverrideForDevice(BluetoothDevice device) {
        transientPrinterModelOverride = null;
        if (device == null) return;
        try {
            String name = device.getName();
            if (name == null) return;
            String lower = name.toLowerCase(Locale.ROOT);
            if (name.equalsIgnoreCase("PT-260_7D0C") || lower.contains("xd210")) {
                transientPrinterModelOverride = PRINTER_MODEL_PT260_XD210;
            } else if (lower.contains("pt-260") || lower.contains("pt260")) {
                transientPrinterModelOverride = PRINTER_MODEL_PT260;
            }
        } catch (SecurityException ignored) {
        }
    }
    private Button imprimirButton;
    private Button changeProfileButton;
    private Button checkUpdateButton;
    private Button validityTabButton;
    private Button alertsTabButton;
    private Button historyTabButton;
    private Button priceTabButton;
    private Button moreTabButton;
    private Button onlyPriceTabButton;
    private Button textOnlyTabButton;
    private Button imageOnlyTabButton;
    private Button photo50x30TabButton;
    private Button settingsTabButton;
    private Button ocrTabButton;
    private Button betaTabButton;
    private Button cloudTabButton;
    private Button peixariaTabButton;
    private Button catalogTabButton;
    private LinearLayout betaToolsRow;
    private LinearLayout validityPage;
    private LinearLayout alertsPage;
    private LinearLayout historyPage;
    private LinearLayout pricePage;
    private LinearLayout catalogPage;
    private LinearLayout morePage;
    private LinearLayout onlyPricePage;
    private LinearLayout textOnlyPage;
    private LinearLayout imageOnlyPage;
    private LinearLayout photo50x30Page;
    private LinearLayout settingsPage;
    private LinearLayout ocrPage;
    private LinearLayout betaPage;
    private LinearLayout cloudPage;
    private LinearLayout peixariaPage;
    private LinearLayout peixariaPanelPage;
    private EditText peixariaProductEdit;
    private EditText peixariaWeightEdit;
    private EditText peixariaCopiesEdit;
    private EditText padariaAddressEdit;
    private CheckBox peixariaProductManualCheck;
    private CheckBox peixariaWeightManualCheck;
    private CheckBox peixariaCopiesManualCheck;
    private Spinner peixariaProductSpinner;
    private Spinner peixariaWeightSpinner;
    private Spinner peixariaCopiesSpinner;
    private TextView peixariaPreviewText;
    private TextView peixariaPanelText;
    private Spinner catalogCategorySpinner;
    private ArrayAdapter<String> catalogCategoryAdapter;
    private TextView catalogPageText;
    private PreviewView ocrPreviewView;
    private ImageView imageOnlyPreview;
    private ImageView photo50x30Preview;
    private Uri labelImageUri;
    private Uri validityImageUri;
    private Uri onlyPriceImageUri;
    private Uri imageOnlyUri;
    private Uri photo50x30Uri;
    private boolean updateDialogVisible;
    private boolean betaRuleDraftNew;
    private String pendingBetaPage = "ocr";
    private long lastUpdateCheckMs;
    private ArrayAdapter<String> produtoAdapter;
    private ArrayAdapter<String> betaRuleAdapter;
    private BroadcastReceiver screenReceiver;
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    private ExecutorService ocrExecutor;
    private TextRecognizer textRecognizer;
    private ProcessCameraProvider cameraProvider;
    private ToneGenerator ocrTone;
    private boolean ocrUseFrontCamera = true;
    private boolean ocrCameraRunning;
    private boolean ocrFrameBusy;
    private long lastOcrScanMs;
    private long lastOcrHandledMs;
    private String lastOcrSignature = "";
    private volatile String lastPrinterName = "";
    private volatile String lastPrinterAddressHash = "";
    private volatile String lastPrinterAddressLast4 = "";
    private Spinner setorSpinner;
    private ArrayAdapter<String> setorAdapter;
    private final List<String> categoriasLista = new ArrayList<>();
    private final java.util.Map<String, List<String>> categoriasMap = new java.util.LinkedHashMap<>();
    private final List<String> todosProdutos = new ArrayList<>();
    private final List<String> produtos = new ArrayList<>();
    private final List<ValidityRule> rules = new ArrayList<>();
    private volatile boolean printSyncRunning = false;

    private void loadEnvProperties() {
        try {
            InputStream is = getAssets().open(".env");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            SharedPreferences.Editor editor = prefs().edit();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                switch (key) {
                    case "PRINTER_MODEL":
                        editor.putString(PREF_PRINTER_MODEL, val);
                        break;
                    case "BLUETOOTH_MODE":
                        editor.putString(PREF_BLUETOOTH_MODE, val);
                        break;
                    case "BETA_UNLOCKED":
                        editor.putBoolean(PREF_BETA_UNLOCKED, "1".equals(val) || "true".equalsIgnoreCase(val));
                        break;
                    case "MAIN_PAGE":
                        editor.putString(PREF_MAIN_PAGE, val);
                        break;
                    case "SUPABASE_URL":
                        editor.putString(PREF_SUPABASE_URL, val);
                        break;
                    case "SUPABASE_ANON_KEY":
                        editor.putString(PREF_SUPABASE_ANON_KEY, val);
                        break;
                }
            }
            editor.apply();
            br.close();
            is.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        dateFormat.setLenient(false);
        dateTimeFormat.setLenient(false);
        shortDateTimeFormat.setLenient(false);
        // Load optional runtime config from assets/.env (local test overrides)
        loadEnvProperties();
        CatalogData catalog = loadCatalog();
        todosProdutos.addAll(catalog.products);
        categoriasMap.clear();
        categoriasMap.putAll(categorizeProducts(todosProdutos, catalog.categories));
        categoriasLista.clear();
        categoriasLista.addAll(categoriasMap.keySet());
        produtos.addAll(categoriasMap.containsKey("Todos") ? categoriasMap.get("Todos") : todosProdutos);
        rules.addAll(catalog.rules);
        buildLayout();
        savePrinterSettingsBackup();
        CatalogSyncJobService.schedule(this);
        registerScreenReminderReceiver();
        requestBluetoothPermissionIfNeeded();
        requestCatalogNotificationPermissionIfNeeded();
        updateDates();
        refreshPrinterStatus();
        checkForUpdates(false);
        handleOpenAlertsIntent(getIntent());
        refreshSupabaseConfigAsync();
        syncDeviceRegistrationAsync();
        syncPendingPrintHistoryAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
        if (statusText != null && System.currentTimeMillis() - lastUpdateCheckMs > 60000) {
            checkForUpdates(false);
        }
        maybeShowExpiryReminder(false);
        syncPendingPrintHistoryAsync();
        if (ocrPage != null && ocrPage.getVisibility() == View.VISIBLE) {
            startOcrCameraIfReady();
        }
    }

    @Override
    protected void onPause() {
        stopOcrCamera();
        lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopOcrCamera();
        if (ocrExecutor != null) {
            ocrExecutor.shutdown();
            ocrExecutor = null;
        }
        if (textRecognizer != null) {
            textRecognizer.close();
            textRecognizer = null;
        }
        if (ocrTone != null) {
            ocrTone.release();
            ocrTone = null;
        }
        if (screenReceiver != null) {
            unregisterReceiver(screenReceiver);
            screenReceiver = null;
        }
        lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        super.onDestroy();
    }

    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOpenAlertsIntent(intent);
    }

    private void buildLayout() {
        var scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundResource(R.drawable.app_background);
        var root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(14));
        root.setBackgroundResource(R.drawable.app_background);
        scroll.addView(root);

        var header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(8), dp(12), dp(8));
        header.setBackground(rounded(COLOR_SURFACE, 16, COLOR_BORDER, 1));

        var titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        var title = new TextView(this);
        title.setText("Validade Pro");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titleBox.addView(title, fullWidth(-2));

        var subtitle = new TextView(this);
        subtitle.setText("Etiquetas rapidas e personalizadas");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subtitle.setTextColor(COLOR_MUTED);
        titleBox.addView(subtitle, fullWidth(-2));

        currentProfileBanner = new TextView(this);
        currentProfileBanner.setText("Estabelecimento atual: " + currentEstablishmentName());
        currentProfileBanner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        currentProfileBanner.setTextColor(Color.parseColor("#0f766e"));
        currentProfileBanner.setPadding(0, dp(8), 0, 0);
        titleBox.addView(currentProfileBanner, fullWidth(-2));

        header.addView(titleBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(header, withBottomMargin(fullWidth(-2), dp(10)));

        root.addView(buildTabs(), fullWidth(-2));

        pricePage = page();
        root.addView(pricePage, fullWidth(-2));

        priceNameEdit = new EditText(this);
        priceNameEdit.setSingleLine(true);
        priceNameEdit.setHint("Nome do produto");
        pricePage.addView(field("Produto", priceNameEdit), fullWidth(-2));

        priceValueEdit = new EditText(this);
        priceValueEdit.setSingleLine(true);
        priceValueEdit.setHint("Ex: 12,90");
        priceValueEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        pricePage.addView(field("Preco", priceValueEdit), fullWidth(-2));

        priceCopiesEdit = createCopiesEdit();
        pricePage.addView(field("Copias", priceCopiesEdit), fullWidth(-2));

        priceFontSizeSpinner = createFontSizeSpinner();
        pricePage.addView(field("Tamanho letras", priceFontSizeSpinner), fullWidth(-2));

        var imageActions = new LinearLayout(this);
        imageActions.setOrientation(LinearLayout.HORIZONTAL);
        imageActions.setGravity(Gravity.CENTER_VERTICAL);

        var chooseImageButton = new Button(this);
        chooseImageButton.setText("Imagem");
        chooseImageButton.setOnClickListener(v -> pickLabelImage());
        imageActions.addView(chooseImageButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearImageButton = new Button(this);
        clearImageButton.setText("Limpar imagem");
        clearImageButton.setOnClickListener(v -> clearLabelImage());
        imageActions.addView(clearImageButton, new LinearLayout.LayoutParams(dp(156), dp(52)));
        pricePage.addView(imageActions, fullWidth(-2));

        imageStatusText = new TextView(this);
        imageStatusText.setText("Sem imagem na etiqueta.");
        imageStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        imageStatusText.setTextColor(COLOR_MUTED);
        imageStatusText.setPadding(0, 0, 0, dp(8));
        pricePage.addView(imageStatusText, fullWidth(-2));

        var printPriceButton = new Button(this);
        printPriceButton.setText("Imprimir preco");
        printPriceButton.setTag("primary");
        printPriceButton.setOnClickListener(v -> printPriceLabel());
        pricePage.addView(printPriceButton, fullWidth(dp(52)));

        onlyPricePage = page();
        root.addView(onlyPricePage, fullWidth(-2));

        onlyPriceValueEdit = new EditText(this);
        onlyPriceValueEdit.setSingleLine(true);
        onlyPriceValueEdit.setHint("Ex: 12,90");
        onlyPriceValueEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        onlyPricePage.addView(field("Preco", onlyPriceValueEdit), fullWidth(-2));

        onlyPriceCopiesEdit = createCopiesEdit();
        onlyPricePage.addView(field("Copias", onlyPriceCopiesEdit), fullWidth(-2));

        onlyPriceFontSizeSpinner = createOnlyPriceSizeSpinner();
        onlyPriceFontSizeSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                updateOnlyPriceRepeatControls();
            }
        });
        onlyPricePage.addView(field("Tamanho do preco", onlyPriceFontSizeSpinner), fullWidth(-2));

        onlyPriceRepeatCheck = new CheckBox(this);
        onlyPriceRepeatCheck.setText("Repetir quando estiver pequeno");
        onlyPriceRepeatCheck.setOnCheckedChangeListener((buttonView, isChecked) -> updateOnlyPriceRepeatControls());
        onlyPricePage.addView(onlyPriceRepeatCheck, fullWidth(dp(48)));

        onlyPriceRepeatCountSpinner = new Spinner(this);
        ArrayAdapter<String> onlyPriceRepeatAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("4 precos na etiqueta", "6 precos na etiqueta")
        );
        onlyPriceRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        onlyPriceRepeatCountSpinner.setAdapter(onlyPriceRepeatAdapter);
        onlyPriceRepeatCountSpinner.setSelection(1);
        onlyPricePage.addView(field("Quantidade pequena", onlyPriceRepeatCountSpinner), fullWidth(-2));
        updateOnlyPriceRepeatControls();

        var onlyPriceImageActions = new LinearLayout(this);
        onlyPriceImageActions.setOrientation(LinearLayout.HORIZONTAL);
        onlyPriceImageActions.setGravity(Gravity.CENTER_VERTICAL);

        var chooseOnlyPriceImageButton = new Button(this);
        chooseOnlyPriceImageButton.setText("Imagem");
        chooseOnlyPriceImageButton.setOnClickListener(v -> pickOnlyPriceImage());
        onlyPriceImageActions.addView(chooseOnlyPriceImageButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearOnlyPriceImageButton = new Button(this);
        clearOnlyPriceImageButton.setText("Limpar imagem");
        clearOnlyPriceImageButton.setOnClickListener(v -> clearOnlyPriceImage());
        onlyPriceImageActions.addView(clearOnlyPriceImageButton, new LinearLayout.LayoutParams(dp(156), dp(52)));
        onlyPricePage.addView(onlyPriceImageActions, fullWidth(-2));

        onlyPriceImageStatusText = new TextView(this);
        onlyPriceImageStatusText.setText("Sem imagem na etiqueta so preco.");
        onlyPriceImageStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        onlyPriceImageStatusText.setTextColor(COLOR_MUTED);
        onlyPriceImageStatusText.setPadding(0, 0, 0, dp(8));
        onlyPricePage.addView(onlyPriceImageStatusText, fullWidth(-2));

        var printOnlyPriceButton = new Button(this);
        printOnlyPriceButton.setText("Imprimir so preco");
        printOnlyPriceButton.setTag("primary");
        printOnlyPriceButton.setOnClickListener(v -> printOnlyPriceLabel());
        onlyPricePage.addView(printOnlyPriceButton, fullWidth(dp(52)));

        textOnlyPage = page();
        root.addView(textOnlyPage, fullWidth(-2));
        buildTextOnlyPage();

        imageOnlyPage = page();
        root.addView(imageOnlyPage, fullWidth(-2));
        buildImageOnlyPage();

        photo50x30Page = page();
        root.addView(photo50x30Page, fullWidth(-2));
        buildPhoto50x30Page();

        validityPage = page();
        root.addView(validityPage, fullWidth(-2));

        catalogPage = page();
        root.addView(catalogPage, fullWidth(-2));

        catalogCategorySpinner = new Spinner(this);
        catalogCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasLista);
        catalogCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catalogCategorySpinner.setAdapter(catalogCategoryAdapter);
        catalogCategorySpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                refreshCatalogPage();
            }
        });
        catalogPage.addView(field("Categoria", catalogCategorySpinner), fullWidth(-2));

        catalogPageText = new TextView(this);
        catalogPageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        catalogPageText.setTextColor(COLOR_TEXT);
        catalogPageText.setPadding(dp(12), dp(10), dp(12), dp(10));
        catalogPageText.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        catalogPage.addView(catalogPageText, fullWidth(-2));

        setorSpinner = new Spinner(this);
        setorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasLista);
        setorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setorSpinner.setAdapter(setorAdapter);
        setorSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                updateProdutosSpinnerPorSetor();
            }
        });
        validityPage.addView(field("Setor (Categoria)", setorSpinner), fullWidth(-2));

        produtoSpinner = new Spinner(this);
        produtoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, produtos);
        produtoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        produtoSpinner.setAdapter(produtoAdapter);
        produtoSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                String selectedProduct = produtos.get(position);
                if (produtoEdit != null) {
                    produtoEdit.setText(selectedProduct);
                    updateDates();
                }
                if (priceNameEdit != null && priceNameEdit.getText().toString().trim().isEmpty()) {
                    priceNameEdit.setText(selectedProduct);
                }
            }
        });
        validityPage.addView(field("Lista pronta", produtoSpinner), fullWidth(-2));

        produtoEdit = new EditText(this);
        produtoEdit.setSingleLine(true);
        produtoEdit.setHint("Nome do produto (NIIMBOT B1)");
        produtoEdit.setText(firstProduct());
        produtoEdit.addTextChangedListener(simpleTextWatcher(this::updateDates));
        validityPage.addView(field("Nome para imprimir", produtoEdit), fullWidth(-2));

        dataEdit = new EditText(this);
        dataEdit.setSingleLine(true);
        dataEdit.setFocusable(false);
        dataEdit.setOnClickListener(v -> openDatePicker());
        validityPage.addView(field("Fabricacao / abertura", dataEdit), fullWidth(-2));

        validadeEdit = new EditText(this);
        validadeEdit.setSingleLine(true);
        validadeEdit.setFocusable(false);
        validadeEdit.setEnabled(false);
        validityPage.addView(field("Validade automatica", validadeEdit), fullWidth(-2));

        validadeManualEdit = new EditText(this);
        validadeManualEdit.setSingleLine(true);
        validadeManualEdit.setHint("Opcional: 31/12/2026 ou 31/12/2026 18:00");
        validityPage.addView(field("Validade manual", validadeManualEdit), fullWidth(-2));

        copiasEdit = createCopiesEdit();
        validityPage.addView(field("Copias", copiasEdit), fullWidth(-2));

        fontSizeSpinner = createFontSizeSpinner();
        validityPage.addView(field("Tamanho letras", fontSizeSpinner), fullWidth(-2));

        var validityImageActions = new LinearLayout(this);
        validityImageActions.setOrientation(LinearLayout.HORIZONTAL);
        validityImageActions.setGravity(Gravity.CENTER_VERTICAL);

        var chooseValidityImageButton = new Button(this);
        chooseValidityImageButton.setText("Imagem");
        chooseValidityImageButton.setOnClickListener(v -> pickValidityImage());
        validityImageActions.addView(chooseValidityImageButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearValidityImageButton = new Button(this);
        clearValidityImageButton.setText("Limpar imagem");
        clearValidityImageButton.setOnClickListener(v -> clearValidityImage());
        validityImageActions.addView(clearValidityImageButton, new LinearLayout.LayoutParams(dp(156), dp(52)));
        validityPage.addView(validityImageActions, fullWidth(-2));

        validityImageStatusText = new TextView(this);
        validityImageStatusText.setText("Sem imagem na etiqueta de validade.");
        validityImageStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        validityImageStatusText.setTextColor(COLOR_MUTED);
        validityImageStatusText.setPadding(0, 0, 0, dp(8));
        validityPage.addView(validityImageStatusText, fullWidth(-2));

        imprimirButton = new Button(this);
        imprimirButton.setText("Imprimir validade");
        imprimirButton.setTag("primary");
        imprimirButton.setOnClickListener(v -> printEtiqueta());
        validityPage.addView(imprimirButton, fullWidth(dp(52)));

        morePage = page();
        root.addView(morePage, fullWidth(-2));
        buildMorePage();

        historyPage = page();
        root.addView(historyPage, fullWidth(-2));
        buildHistoryPage();

        alertsPage = page();
        root.addView(alertsPage, fullWidth(-2));
        buildAlertsPage();

        settingsPage = page();
        root.addView(settingsPage, fullWidth(-2));

        metodoSpinner = new Spinner(this);
        ArrayAdapter<String> metodoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(
                        "Automatico",
                        "Canal 1",
                        "Canal 2",
                        "Canal 3",
                        "Canal 4",
                        "Canal 5",
                        "Canal 6",
                        "Canal 7",
                        "Canal 8",
                        "Seguro",
                        "Inseguro"
                )
        );
        metodoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        metodoSpinner.setAdapter(metodoAdapter);
        applySavedBluetoothMode();
        metodoSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                prefs().edit().putString(PREF_BLUETOOTH_MODE, selectedBluetoothMode()).apply();
            }
        });
        settingsPage.addView(field("Metodo Bluetooth", metodoSpinner), fullWidth(-2));

        printerModelSpinner = new Spinner(this);
        ArrayAdapter<String> printerModelAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(
                        PRINTER_MODEL_AUTO,
                        PRINTER_MODEL_PT260,
                        PRINTER_MODEL_PT260_XD210,
                        PRINTER_MODEL_B1
                )
        );
        printerModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        printerModelSpinner.setAdapter(printerModelAdapter);
        settingsPage.addView(field("Modelo da impressora", printerModelSpinner), fullWidth(-2));
        applySavedPrinterModel();
        printerModelSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                persistPrinterModel();
            }
        });

        peixeiroSpinner = new Spinner(this);
        ArrayAdapter<String> peixeiroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CatalogProfileUtils.profileLabels()
        );
        peixeiroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peixeiroSpinner.setAdapter(peixeiroAdapter);
        applySelectedCatalogProfile();
        peixeiroSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                if (position < 0 || position >= CatalogProfileUtils.profileKeys().size()) {
                    return;
                }
                String profileKey = CatalogProfileUtils.profileKeys().get(position);
                if (!CatalogProfileUtils.normalizeProfileKey(profileKey).equals(currentCatalogProfileKey())) {
                    switchCatalogProfile(profileKey);
                }
            }
        });
        establishmentNameEdit = new EditText(this);
        establishmentNameEdit.setSingleLine(true);
        establishmentNameEdit.setHint("Nome do estabelecimento");
        establishmentNameEdit.setText(currentEstablishmentName());
        establishmentNameEdit.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                saveEstablishmentName();
            }
        });
        settingsPage.addView(field("Nome do estabelecimento", establishmentNameEdit), fullWidth(-2));
        settingsPage.addView(field("Estabelecimento / conta", peixeiroSpinner), fullWidth(-2));

        catalogUrlEdit = new EditText(this);
        catalogUrlEdit.setSingleLine(true);
        catalogUrlEdit.setHint("URL GitHub Raw ou Supabase");
        syncCatalogUrlFieldFromCurrentProfile();
        settingsPage.addView(field("Catalogo online", catalogUrlEdit), fullWidth(-2));

        var catalogButton = new Button(this);
        catalogButton.setText("Atualizar lista");
        catalogButton.setOnClickListener(v -> updateCatalog());
        settingsPage.addView(catalogButton, fullWidth(dp(52)));

        var reloadLocalButton = new Button(this);
        reloadLocalButton.setText("Recarregar catálogo local");
        reloadLocalButton.setOnClickListener(v -> reloadLocalCatalog());
        settingsPage.addView(reloadLocalButton, fullWidth(dp(52)));

        var backupRow = new LinearLayout(this);
        backupRow.setOrientation(LinearLayout.HORIZONTAL);
        backupRow.setGravity(Gravity.CENTER_VERTICAL);

        var saveBackupButton = new Button(this);
        saveBackupButton.setText("Criar backup");
        saveBackupButton.setOnClickListener(v -> {
            savePrinterSettingsBackup();
            setStatus("Backup das preferencias salvo.");
        });
        backupRow.addView(saveBackupButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var restoreBackupButton = new Button(this);
        restoreBackupButton.setText("Restaurar backup");
        restoreBackupButton.setOnClickListener(v -> restorePrinterSettingsBackup(true));
        backupRow.addView(restoreBackupButton, new LinearLayout.LayoutParams(dp(164), dp(52)));
        settingsPage.addView(backupRow, fullWidth(-2));

        var updateButton = new Button(this);
        updateButton.setText("Verificar atualizacao");
        updateButton.setOnClickListener(v -> checkForUpdates(true));
        settingsPage.addView(updateButton, fullWidth(dp(52)));

        var actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        var testButton = new Button(this);
        testButton.setText("Diagnostico");
        testButton.setOnClickListener(v -> printTestLabel());
        actions.addView(testButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var bluetoothButton = new Button(this);
        bluetoothButton.setText("Bluetooth");
        bluetoothButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));
        actions.addView(bluetoothButton, new LinearLayout.LayoutParams(dp(132), dp(52)));
        settingsPage.addView(actions, fullWidth(-2));

        var bleButton = new Button(this);
        bleButton.setText("Teste BLE PT260");
        bleButton.setOnClickListener(v -> printBleDiagnostic());
        settingsPage.addView(bleButton, fullWidth(dp(52)));

        aboutAppEdit = new EditText(this);
        aboutAppEdit.setSingleLine(true);
        aboutAppEdit.setFocusable(false);
        aboutAppEdit.setText("Validade Pro " + BuildConfig.VERSION_NAME);
        aboutAppEdit.setOnClickListener(v -> showAboutDialog());
        aboutAppEdit.setOnLongClickListener(v -> {
            showBetaLoginDialog();
            return true;
        });
        settingsPage.addView(field("Sobre o app", aboutAppEdit), fullWidth(-2));

        ocrPage = page();
        root.addView(ocrPage, fullWidth(-2));
        buildOcrPage();

        betaPage = page();
        root.addView(betaPage, fullWidth(-2));
        buildBetaPage();

        cloudPage = page();
        root.addView(cloudPage, fullWidth(-2));
        buildCloudPage();

        peixariaPage = page();
        root.addView(peixariaPage, fullWidth(-2));
        buildPeixariaPage();

        peixariaPanelPage = page();
        root.addView(peixariaPanelPage, fullWidth(-2));
        buildPeixariaPanelPage();

        statusText = new TextView(this);
        statusText.setPadding(dp(12), dp(12), dp(12), dp(12));
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        statusText.setTextColor(COLOR_PRIMARY_DARK);
        statusText.setBackground(rounded(COLOR_STATUS, 12, 0xFFBFDBFE, 1));
        root.addView(statusText, withTopMargin(fullWidth(-2), dp(10)));

        polishTree(root);
        setContentView(scroll);
        showPage(isPeixariaMainPage() && isBetaUnlocked() ? MAIN_PAGE_PEIXARIA : MAIN_PAGE_VALIDITY);
    }

    private void buildTextOnlyPage() {
        var title = new TextView(this);
        title.setText("So texto");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        textOnlyPage.addView(title, fullWidth(-2));

        textOnlyEdit = new EditText(this);
        textOnlyEdit.setSingleLine(false);
        textOnlyEdit.setMinLines(3);
        textOnlyEdit.setGravity(Gravity.TOP);
        textOnlyEdit.setHint("Digite o texto da etiqueta");
        textOnlyEdit.setTag("tall");
        textOnlyEdit.addTextChangedListener(simpleTextWatcher(this::refreshTextOnlyPreview));
        textOnlyPage.addView(field("Texto", textOnlyEdit), fullWidth(-2));

        textOnlyCopiesEdit = createCopiesEdit();
        textOnlyPage.addView(field("Copias", textOnlyCopiesEdit), fullWidth(-2));

        var previewTitle = new TextView(this);
        previewTitle.setText("Pre-visualizacao");
        previewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        previewTitle.setTextColor(COLOR_TEXT);
        previewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        previewTitle.setPadding(0, dp(8), 0, dp(6));
        textOnlyPage.addView(previewTitle, fullWidth(-2));

        textOnlyPreviewText = new TextView(this);
        textOnlyPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textOnlyPreviewText.setTextColor(COLOR_TEXT);
        textOnlyPreviewText.setGravity(Gravity.CENTER);
        textOnlyPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
        textOnlyPreviewText.setMinHeight(dp(176));
        textOnlyPreviewText.setPadding(dp(10), dp(8), dp(10), dp(8));
        textOnlyPreviewText.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        textOnlyPage.addView(textOnlyPreviewText, withBottomMargin(fullWidth(dp(176)), dp(8)));

        var printButton = new Button(this);
        printButton.setText("Imprimir texto");
        printButton.setTag("primary");
        printButton.setOnClickListener(v -> printTextOnlyLabel());
        textOnlyPage.addView(printButton, fullWidth(dp(52)));

        refreshTextOnlyPreview();
    }

    private void buildImageOnlyPage() {
        var title = new TextView(this);
        title.setText("So imagem");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        imageOnlyPage.addView(title, fullWidth(-2));

        var imageActions = new LinearLayout(this);
        imageActions.setOrientation(LinearLayout.HORIZONTAL);
        imageActions.setGravity(Gravity.CENTER_VERTICAL);

        var chooseButton = new Button(this);
        chooseButton.setText("Escolher imagem");
        chooseButton.setOnClickListener(v -> pickImageOnlyImage());
        imageActions.addView(chooseButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearButton = new Button(this);
        clearButton.setText("Limpar");
        clearButton.setOnClickListener(v -> clearImageOnlyImage());
        imageActions.addView(clearButton, new LinearLayout.LayoutParams(dp(112), dp(52)));
        imageOnlyPage.addView(imageActions, fullWidth(-2));

        imageOnlyStatusText = new TextView(this);
        imageOnlyStatusText.setText("Sem imagem para imprimir.");
        imageOnlyStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        imageOnlyStatusText.setTextColor(COLOR_MUTED);
        imageOnlyStatusText.setPadding(0, 0, 0, dp(8));
        imageOnlyPage.addView(imageOnlyStatusText, fullWidth(-2));

        imageOnlyCopiesEdit = createCopiesEdit();
        imageOnlyPage.addView(field("Copias", imageOnlyCopiesEdit), fullWidth(-2));

        var previewTitle = new TextView(this);
        previewTitle.setText("Pre-visualizacao");
        previewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        previewTitle.setTextColor(COLOR_TEXT);
        previewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        previewTitle.setPadding(0, dp(8), 0, dp(6));
        imageOnlyPage.addView(previewTitle, fullWidth(-2));

        imageOnlyPreview = new ImageView(this);
        imageOnlyPreview.setAdjustViewBounds(true);
        imageOnlyPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageOnlyPreview.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        imageOnlyPreview.setPadding(dp(8), dp(8), dp(8), dp(8));
        imageOnlyPage.addView(imageOnlyPreview, withBottomMargin(fullWidth(dp(176)), dp(8)));

        var printButton = new Button(this);
        printButton.setText("Imprimir imagem");
        printButton.setTag("primary");
        printButton.setOnClickListener(v -> printImageOnlyLabel());
        imageOnlyPage.addView(printButton, fullWidth(dp(52)));

        refreshImageOnlyPreview();
    }

    private void buildPhoto50x30Page() {
        var title = new TextView(this);
        title.setText("Foto 50x30 (completa)");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        photo50x30Page.addView(title, fullWidth(-2));

        var subtitle = new TextView(this);
        subtitle.setText("Aba pronta para imprimir uma foto ocupando toda a etiqueta 50x30.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, 0, 0, dp(8));
        photo50x30Page.addView(subtitle, fullWidth(-2));

        var imageActions = new LinearLayout(this);
        imageActions.setOrientation(LinearLayout.HORIZONTAL);
        imageActions.setGravity(Gravity.CENTER_VERTICAL);

        var chooseButton = new Button(this);
        chooseButton.setText("Escolher foto");
        chooseButton.setOnClickListener(v -> pickPhoto50x30Image());
        imageActions.addView(chooseButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearButton = new Button(this);
        clearButton.setText("Limpar");
        clearButton.setOnClickListener(v -> clearPhoto50x30Image());
        imageActions.addView(clearButton, new LinearLayout.LayoutParams(dp(112), dp(52)));
        photo50x30Page.addView(imageActions, fullWidth(-2));

        photo50x30StatusText = new TextView(this);
        photo50x30StatusText.setText("Selecione uma foto para a etiqueta 50x30.");
        photo50x30StatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        photo50x30StatusText.setTextColor(COLOR_MUTED);
        photo50x30StatusText.setPadding(0, 0, 0, dp(8));
        photo50x30Page.addView(photo50x30StatusText, fullWidth(-2));

        photo50x30CopiesEdit = createCopiesEdit();
        photo50x30Page.addView(field("Copias", photo50x30CopiesEdit), fullWidth(-2));

        var previewTitle = new TextView(this);
        previewTitle.setText("Pre-visualizacao");
        previewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        previewTitle.setTextColor(COLOR_TEXT);
        previewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        previewTitle.setPadding(0, dp(8), 0, dp(6));
        photo50x30Page.addView(previewTitle, fullWidth(-2));

        photo50x30Preview = new ImageView(this);
        photo50x30Preview.setAdjustViewBounds(true);
        photo50x30Preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        photo50x30Preview.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        photo50x30Preview.setPadding(dp(8), dp(8), dp(8), dp(8));
        photo50x30Page.addView(photo50x30Preview, withBottomMargin(fullWidth(dp(220)), dp(8)));

        var printButton = new Button(this);
        printButton.setText("Imprimir foto 50x30");
        printButton.setTag("primary");
        printButton.setOnClickListener(v -> printPhoto50x30Label());
        photo50x30Page.addView(printButton, fullWidth(dp(52)));

        refreshPhoto50x30Preview();
    }

    private void buildMorePage() {
        var title = new TextView(this);
        title.setText("Mais");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(8));
        morePage.addView(title, fullWidth(-2));

        addMoreRow("Avisos", "alerts", "Historico", "history");
        addMoreRow("So Precos", "only_prices", "So texto", "text_only");
        addMoreRow("So imagem", "image_only", "Ajustes", "settings");
        addMoreRow("OCR", "ocr", "Beta", "beta");
        addMoreRow("Padaria", "padaria", "Painel Padaria", "padaria_panel");

        morePage.addView(morePageActionButton("Adicionar produto ao catalogo", v -> showAddPeixariaProductDialog()), withBottomMargin(fullWidth(dp(56)), dp(8)));
        morePage.addView(morePageActionButton("Mudar Local", v -> showPage("settings")), withBottomMargin(fullWidth(dp(56)), dp(8)));
        morePage.addView(morePageActionButton("Ajustes da rastreabilidade", v -> showAdminConfirmDialog(this::showTraceabilitySettingsDialog)), withBottomMargin(fullWidth(dp(56)), dp(8)));
        morePage.addView(morePageActionButton("Novo estabelecimento", v -> showAdminConfirmDialog(this::showCreateEstablishmentDialog)), withBottomMargin(fullWidth(dp(56)), dp(8)));
        morePage.addView(morePageActionButton("Verificar atualização", v -> checkForUpdates(true)), withBottomMargin(fullWidth(dp(56)), dp(8)));
        morePage.addView(morePageButton("Foto 50x30", "photo_50x30"), withBottomMargin(fullWidth(dp(56)), dp(8)));
    }

    private void addMoreRow(String leftLabel, String leftPage, String rightLabel, String rightPage) {
        var row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        Button left = morePageButton(leftLabel, leftPage);
        if (left != null) {
            row.addView(left, new LinearLayout.LayoutParams(0, dp(56), 1));
        }

        Button right = morePageButton(rightLabel, rightPage);
        if (right != null) {
            row.addView(right, new LinearLayout.LayoutParams(0, dp(56), 1));
        }

        morePage.addView(row, withBottomMargin(fullWidth(dp(56)), dp(8)));
    }

    private Button morePageButton(String label, String pageName) {
        if (pageName == null || pageName.isEmpty()) {
            return null;
        }
        var button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(v -> showPage(pageName));
        return button;
    }

    private Button morePageActionButton(String label, View.OnClickListener listener) {
        var button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        return button;
    }

    private void buildOcrPage() {
        var title = new TextView(this);
        title.setText("OCR validade");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        ocrPage.addView(title, fullWidth(-2));

        var version = new TextView(this);
        version.setText("Beta OCR - Versao " + BuildConfig.VERSION_NAME);
        version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        version.setTextColor(COLOR_MUTED);
        version.setPadding(0, 0, 0, dp(8));
        ocrPage.addView(version, fullWidth(-2));

        ocrPreviewView = new PreviewView(this);
        ocrPreviewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        ocrPreviewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        ocrPreviewView.setBackgroundColor(Color.BLACK);
        ocrPage.addView(ocrPreviewView, withBottomMargin(fullWidth(dp(300)), dp(8)));

        ocrStatusText = new TextView(this);
        ocrStatusText.setText("Aguardando permissao da camera.");
        ocrStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        ocrStatusText.setTextColor(COLOR_PRIMARY_DARK);
        ocrStatusText.setPadding(dp(12), dp(10), dp(12), dp(10));
        ocrStatusText.setBackground(rounded(COLOR_SURFACE_ALT, 10, COLOR_BORDER, 1));
        ocrPage.addView(ocrStatusText, withBottomMargin(fullWidth(-2), dp(8)));

        ocrResultText = new TextView(this);
        ocrResultText.setText("OCR aguardando etiqueta.");
        ocrResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        ocrResultText.setTextColor(COLOR_TEXT);
        ocrResultText.setPadding(dp(12), dp(10), dp(12), dp(10));
        ocrResultText.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        ocrPage.addView(ocrResultText, withBottomMargin(fullWidth(-2), dp(8)));

        var ocrActions = new LinearLayout(this);
        ocrActions.setOrientation(LinearLayout.HORIZONTAL);
        ocrActions.setGravity(Gravity.CENTER_VERTICAL);

        var switchCameraButton = new Button(this);
        switchCameraButton.setText("Trocar camera");
        switchCameraButton.setOnClickListener(v -> switchOcrCamera());
        ocrActions.addView(switchCameraButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var openAlertsButton = new Button(this);
        openAlertsButton.setText("Avisos");
        openAlertsButton.setOnClickListener(v -> showPage("alerts"));
        ocrActions.addView(openAlertsButton, new LinearLayout.LayoutParams(dp(116), dp(52)));
        ocrPage.addView(ocrActions, fullWidth(-2));
    }

    private void buildBetaPage() {
        var title = new TextView(this);
        title.setText("Beta liberada");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        betaPage.addView(title, fullWidth(-2));

        var version = new TextView(this);
        version.setText("Versao " + BuildConfig.VERSION_NAME + " / codigo " + BuildConfig.VERSION_CODE);
        version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        version.setTextColor(COLOR_MUTED);
        version.setPadding(0, 0, 0, dp(8));
        betaPage.addView(version, fullWidth(-2));

        betaRuleSpinner = new Spinner(this);
        betaRuleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, betaRuleLabels());
        betaRuleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        betaRuleSpinner.setAdapter(betaRuleAdapter);
        betaRuleSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                loadBetaRule(position);
            }
        });
        betaPage.addView(field("Regra de validade", betaRuleSpinner), fullWidth(-2));

        betaRuleMatchEdit = new EditText(this);
        betaRuleMatchEdit.setSingleLine(true);
        betaRuleMatchEdit.setHint("Ex: pimenta ou *");
        betaPage.addView(field("Produto / palavra", betaRuleMatchEdit), fullWidth(-2));

        var betaDeadlineRow = new LinearLayout(this);
        betaDeadlineRow.setOrientation(LinearLayout.HORIZONTAL);
        betaDeadlineRow.setGravity(Gravity.CENTER_VERTICAL);

        betaRuleDaysEdit = new EditText(this);
        betaRuleDaysEdit.setSingleLine(true);
        betaRuleDaysEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        betaRuleDaysEdit.setHint("Dias");
        betaDeadlineRow.addView(field("Dias", betaRuleDaysEdit), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        betaRuleHoursEdit = new EditText(this);
        betaRuleHoursEdit.setSingleLine(true);
        betaRuleHoursEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        betaRuleHoursEdit.setHint("Horas");
        betaDeadlineRow.addView(field("Horas", betaRuleHoursEdit), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        betaPage.addView(betaDeadlineRow, fullWidth(-2));

        betaRuleLabelEdit = new EditText(this);
        betaRuleLabelEdit.setSingleLine(true);
        betaRuleLabelEdit.setHint("Fab, Aberto, Pronto...");
        betaPage.addView(field("Rotulo inicial", betaRuleLabelEdit), fullWidth(-2));

        var betaActions = new LinearLayout(this);
        betaActions.setOrientation(LinearLayout.HORIZONTAL);
        betaActions.setGravity(Gravity.CENTER_VERTICAL);

        var newRuleButton = new Button(this);
        newRuleButton.setText("Nova regra");
        newRuleButton.setOnClickListener(v -> newBetaRuleDraft());
        betaActions.addView(newRuleButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var removeRuleButton = new Button(this);
        removeRuleButton.setText("Remover");
        removeRuleButton.setOnClickListener(v -> removeSelectedBetaRule());
        betaActions.addView(removeRuleButton, new LinearLayout.LayoutParams(dp(128), dp(52)));
        betaPage.addView(betaActions, fullWidth(-2));

        var saveRuleButton = new Button(this);
        saveRuleButton.setText("Salvar prazo");
        saveRuleButton.setTag("primary");
        saveRuleButton.setOnClickListener(v -> saveSelectedBetaRule());
        betaPage.addView(saveRuleButton, fullWidth(dp(52)));

        var cloudButton = new Button(this);
        cloudButton.setText("Painel de validades");
        cloudButton.setTag("primary");
        cloudButton.setOnClickListener(v -> showPage("cloud"));
        betaPage.addView(cloudButton, fullWidth(dp(52)));

        var peixariaButton = new Button(this);
        peixariaButton.setText("Padaria - rastreabilidade");
        peixariaButton.setTag("primary");
        peixariaButton.setOnClickListener(v -> showPage(MAIN_PAGE_PEIXARIA));
        betaPage.addView(peixariaButton, fullWidth(dp(52)));

        var addPeixariaProductButton = new Button(this);
        addPeixariaProductButton.setText("Adicionar produto ao catalogo");
        addPeixariaProductButton.setOnClickListener(v -> showAddPeixariaProductDialog());
        betaPage.addView(addPeixariaProductButton, fullWidth(dp(52)));

        var peixariaPanelButton = new Button(this);
        peixariaPanelButton.setText("Painel Padaria");
        peixariaPanelButton.setOnClickListener(v -> showPage("padaria_panel"));
        betaPage.addView(peixariaPanelButton, fullWidth(dp(52)));

        var primaryPageButton = new Button(this);
        primaryPageButton.setText(isPeixariaMainPage() ? "Usar Validade como primeira aba" : "Usar Padaria como primeira aba");
        primaryPageButton.setOnClickListener(v -> {
            boolean peixariaMain = !isPeixariaMainPage();
            setPeixariaMainPage(peixariaMain);
            primaryPageButton.setText(peixariaMain ? "Usar Validade como primeira aba" : "Usar Padaria como primeira aba");
            updatePrimaryTabButton();
            showPage(peixariaMain ? MAIN_PAGE_PEIXARIA : "validity");
            setStatus(peixariaMain ? "Padaria sera a primeira aba ao abrir o app." : "Validade sera a primeira aba ao abrir o app.");
        });
        betaPage.addView(primaryPageButton, fullWidth(dp(52)));

        var testButton = new Button(this);
        testButton.setText("Teste etiqueta");
        testButton.setOnClickListener(v -> printTestLabel());
        betaPage.addView(testButton, fullWidth(dp(52)));

        var hideButton = new Button(this);
        hideButton.setText("Ocultar beta");
        hideButton.setOnClickListener(v -> setBetaUnlocked(false));
        betaPage.addView(hideButton, fullWidth(dp(52)));

        if (!rules.isEmpty()) {
            loadBetaRule(0);
        }
    }

    private void buildCloudPage() {
        var title = new TextView(this);
        title.setText("Painel nuvem");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        cloudPage.addView(title, fullWidth(-2));

        var subtitle = new TextView(this);
        subtitle.setText("Historico geral Supabase - acesso beta");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, 0, 0, dp(8));
        cloudPage.addView(subtitle, fullWidth(-2));

        var actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        var refreshButton = new Button(this);
        refreshButton.setText("Atualizar");
        refreshButton.setTag("primary");
        refreshButton.setOnClickListener(v -> refreshCloudDashboard());
        actions.addView(refreshButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var betaButton = new Button(this);
        betaButton.setText("Beta");
        betaButton.setOnClickListener(v -> showPage("beta"));
        actions.addView(betaButton, new LinearLayout.LayoutParams(dp(112), dp(52)));
        cloudPage.addView(actions, fullWidth(-2));

        cloudSummaryText = cloudCardText();
        cloudPage.addView(cloudSummaryText, withBottomMargin(fullWidth(-2), dp(10)));

        cloudPage.addView(sectionTitle("Proximos vencimentos"), fullWidth(-2));
        cloudExpiringText = cloudCardText();
        cloudPage.addView(cloudExpiringText, withBottomMargin(fullWidth(-2), dp(10)));

        cloudPage.addView(sectionTitle("Aparelhos"), fullWidth(-2));
        cloudDevicesText = cloudCardText();
        cloudPage.addView(cloudDevicesText, withBottomMargin(fullWidth(-2), dp(10)));

        cloudPage.addView(sectionTitle("Ultimas impressoes"), fullWidth(-2));
        cloudRecentPrintsText = cloudCardText();
        cloudPage.addView(cloudRecentPrintsText, withBottomMargin(fullWidth(-2), dp(10)));

        cloudPage.addView(sectionTitle("Ultimas checagens OCR"), fullWidth(-2));
        cloudRecentChecksText = cloudCardText();
        cloudPage.addView(cloudRecentChecksText, fullWidth(-2));

        setCloudLoadingText("Abra o painel e toque em Atualizar.");
    }

    private void buildPeixariaPage() {
        var title = new TextView(this);
        title.setText("Padaria - rastreabilidade");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        peixariaPage.addView(title, fullWidth(-2));

        var subtitle = new TextView(this);
        subtitle.setText("Etiqueta 50x30 com lote anual sequencial. Fornecedor: Padaria Lobo.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, 0, 0, dp(8));
        peixariaPage.addView(subtitle, fullWidth(-2));

        peixariaProductEdit = new EditText(this);
        peixariaProductEdit.setSingleLine(true);
        peixariaProductEdit.setHint("Ex: File de tilapia");
        peixariaProductSpinner = new Spinner(this);
        List<String> peixariaProducts = loadPeixariaProductCatalog();
        if (!peixariaProducts.contains("Outros")) {
            peixariaProducts.add("Outros");
        }
        ArrayAdapter<String> peixariaProductAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, peixariaProducts);
        peixariaProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peixariaProductSpinner.setAdapter(peixariaProductAdapter);
        peixariaProductSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                String value = peixariaProductAdapter.getItem(position);
                if ((peixariaProductManualCheck == null || !peixariaProductManualCheck.isChecked())
                    && value != null && !value.equals("Outros")) {
                    peixariaProductEdit.setText(value);
                }
            }
        });
        var productRow = new LinearLayout(this);
        productRow.setOrientation(LinearLayout.HORIZONTAL);
        productRow.setGravity(Gravity.CENTER_VERTICAL);
        productRow.addView(field("Tipo de produto", peixariaProductSpinner), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        var manualProductBox = new LinearLayout(this);
        manualProductBox.setOrientation(LinearLayout.VERTICAL);
        peixariaProductManualCheck = new CheckBox(this);
        peixariaProductManualCheck.setText("Digitar produto");
        manualProductBox.addView(peixariaProductManualCheck, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
        View manualProductField = field("Tipo (manual)", peixariaProductEdit);
        manualProductBox.addView(manualProductField, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        configureManualField(peixariaProductManualCheck, peixariaProductEdit, manualProductField);
        productRow.addView(manualProductBox, manualSideParams());
        peixariaPage.addView(productRow, fullWidth(-2));

        peixariaWeightEdit = new EditText(this);
        peixariaWeightEdit.setSingleLine(true);
        peixariaWeightEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        peixariaWeightEdit.setHint("Ex: 18,500");
        // Add weight spinner with common weights plus manual input
        peixariaWeightSpinner = new Spinner(this);
        ArrayAdapter<String> peixariaWeightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(
                "0,500",
                "1,000",
                "2,000",
                "5,000",
                "10,000",
                "13,000",
                "15,000",
                "18,500",
                "20,000",
                "25,000",
                "50,000",
                "Outro"
        ));
        peixariaWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peixariaWeightSpinner.setAdapter(peixariaWeightAdapter);
        peixariaWeightSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                String value = peixariaWeightAdapter.getItem(position);
                if ((peixariaWeightManualCheck == null || !peixariaWeightManualCheck.isChecked())
                    && value != null && !value.equals("Outro")) {
                    peixariaWeightEdit.setText(value.replace(',', '.'));
                }
            }
        });
        var weightRow = new LinearLayout(this);
        weightRow.setOrientation(LinearLayout.HORIZONTAL);
        weightRow.setGravity(Gravity.CENTER_VERTICAL);
        weightRow.addView(field("Peso (kg) - predefinidos", peixariaWeightSpinner), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        var manualWeightBox = new LinearLayout(this);
        manualWeightBox.setOrientation(LinearLayout.VERTICAL);
        peixariaWeightManualCheck = new CheckBox(this);
        peixariaWeightManualCheck.setText("Digitar peso");
        manualWeightBox.addView(peixariaWeightManualCheck, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
        View manualWeightField = field("Peso (kg) - manual", peixariaWeightEdit);
        manualWeightBox.addView(manualWeightField, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        configureManualField(peixariaWeightManualCheck, peixariaWeightEdit, manualWeightField);
        weightRow.addView(manualWeightBox, manualSideParams());
        peixariaPage.addView(weightRow, fullWidth(-2));

        // Copies spinner for quick selection plus manual field
        peixariaCopiesSpinner = new Spinner(this);
        ArrayAdapter<String> copiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(
                "1","2","3","4","5","6","10","12","20","Outro"
        ));
        copiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peixariaCopiesSpinner.setAdapter(copiesAdapter);
        peixariaCopiesEdit = createCopiesEdit();
        peixariaCopiesSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                String v = copiesAdapter.getItem(position);
                if ((peixariaCopiesManualCheck == null || !peixariaCopiesManualCheck.isChecked())
                    && v != null && !v.equals("Outro")) {
                    peixariaCopiesEdit.setText(v);
                }
            }
        });
        var copiesRow = new LinearLayout(this);
        copiesRow.setOrientation(LinearLayout.HORIZONTAL);
        copiesRow.setGravity(Gravity.CENTER_VERTICAL);
        copiesRow.addView(field("Copias (rapido)", peixariaCopiesSpinner), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        var manualCopiesBox = new LinearLayout(this);
        manualCopiesBox.setOrientation(LinearLayout.VERTICAL);
        peixariaCopiesManualCheck = new CheckBox(this);
        peixariaCopiesManualCheck.setText("Digitar copias");
        manualCopiesBox.addView(peixariaCopiesManualCheck, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
        View manualCopiesField = field("Copias (manual)", peixariaCopiesEdit);
        manualCopiesBox.addView(manualCopiesField, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        configureManualField(peixariaCopiesManualCheck, peixariaCopiesEdit, manualCopiesField);
        copiesRow.addView(manualCopiesBox, manualSideParams());
        peixariaPage.addView(copiesRow, fullWidth(-2));

        peixariaPreviewText = cloudCardText();
        peixariaPreviewText.setText("O proximo lote sera " + nextPeixariaLotPreview() + ".\nRecebimento e processamento: data de hoje.\nValidade: " + peixariaValiditySummary(""));
        peixariaPage.addView(peixariaPreviewText, withBottomMargin(fullWidth(-2), dp(8)));

        var labelActions = new LinearLayout(this);
        labelActions.setOrientation(LinearLayout.HORIZONTAL);
        labelActions.setGravity(Gravity.CENTER_VERTICAL);
        labelActions.setPadding(0, 0, 0, dp(8));

        var previewButton = new Button(this);
        previewButton.setText("VER ETIQUETA");
        previewButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        previewButton.setPadding(dp(4), 0, dp(4), 0);
        previewButton.setMinHeight(0);
        previewButton.setMinWidth(0);
        previewButton.setOnClickListener(v -> showPeixariaPreviewDialog());
        labelActions.addView(previewButton, new LinearLayout.LayoutParams(0, dp(48), 1));

        var addressButton = new Button(this);
        addressButton.setText("END. ORIGEM");
        addressButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        addressButton.setPadding(dp(4), 0, dp(4), 0);
        addressButton.setMinHeight(0);
        addressButton.setMinWidth(0);
        addressButton.setOnClickListener(v -> showAdminConfirmDialog(this::showTraceabilitySettingsDialog));
        var addressParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        addressParams.setMargins(dp(6), 0, 0, 0);
        labelActions.addView(addressButton, addressParams);

        var printButton = new Button(this);
        printButton.setText("IMPRIMIR");
        printButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        printButton.setPadding(dp(4), 0, dp(4), 0);
        printButton.setMinHeight(0);
        printButton.setMinWidth(0);
        printButton.setTag("primary");
        printButton.setOnClickListener(v -> printPeixariaLabel());
        var printParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        printParams.setMargins(dp(6), 0, 0, 0);
        labelActions.addView(printButton, printParams);
        peixariaPage.addView(labelActions, fullWidth(-2));

        var panelButton = new Button(this);
        panelButton.setText("Painel Padaria");
        panelButton.setOnClickListener(v -> showPage("padaria_panel"));
        peixariaPage.addView(panelButton, fullWidth(dp(52)));
    }

    private void buildPeixariaPanelPage() {
        var title = new TextView(this);
        title.setText("Painel Padaria");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        peixariaPanelPage.addView(title, fullWidth(-2));

        var subtitle = new TextView(this);
        subtitle.setText("Rastreabilidade organizada por data de impressao e lote.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, 0, 0, dp(8));
        peixariaPanelPage.addView(subtitle, fullWidth(-2));

        var actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        var refreshButton = new Button(this);
        refreshButton.setText("Atualizar");
        refreshButton.setTag("primary");
        refreshButton.setOnClickListener(v -> refreshPeixariaPanel());
        actions.addView(refreshButton, new LinearLayout.LayoutParams(0, dp(52), 1));
        var backButton = new Button(this);
        backButton.setText("Padaria");
        backButton.setOnClickListener(v -> showPage(MAIN_PAGE_PEIXARIA));
        actions.addView(backButton, new LinearLayout.LayoutParams(dp(120), dp(52)));
        var reprintButton = new Button(this);
        reprintButton.setText("Reimprimir lote");
        reprintButton.setOnClickListener(v -> {
            // Ask for lot to reprint and require admin confirmation
            var input = new EditText(this);
            input.setSingleLine(true);
            input.setHint("Ex: 001/2026");
            new AlertDialog.Builder(this)
                    .setTitle("Reimprimir lote")
                    .setMessage("Informe o lote a reimprimir:")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> {
                        String lot = input.getText().toString().trim();
                        if (lot.isEmpty()) {
                            showError("Informe o lote.");
                            return;
                        }
                        List<PeixariaEntry> entries = loadPeixariaHistory();
                        PeixariaEntry found = null;
                        for (PeixariaEntry e : entries) {
                            if (lot.equals(e.lot)) { found = e; break; }
                        }
                        if (found == null) {
                            showError("Lote nao encontrado no historico.");
                            return;
                        }
                        final PeixariaEntry selectedEntry = found;
                        // Ask copies number
                        var copiesInput = new EditText(this);
                        copiesInput.setSingleLine(true);
                        copiesInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        copiesInput.setText(String.valueOf(found.copies <= 0 ? 1 : found.copies));
                        new AlertDialog.Builder(this)
                                .setTitle("Copias")
                                .setView(copiesInput)
                                .setPositiveButton("Imprimir", (d2, w2) -> {
                                    int copies = 1;
                                    try { copies = Integer.parseInt(copiesInput.getText().toString().trim()); } catch (Exception ignored) {}
                                    final int finalCopies = Math.max(1, copies);
                                    showAdminConfirmDialog(() -> printExistingPeixariaEntry(selectedEntry, finalCopies));
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        actions.addView(reprintButton, new LinearLayout.LayoutParams(dp(120), dp(52)));
        peixariaPanelPage.addView(actions, fullWidth(-2));

        peixariaPanelText = cloudCardText();
        peixariaPanelPage.addView(peixariaPanelText, fullWidth(-2));
        refreshPeixariaPanel();
    }

    private TextView sectionTitle(String text) {
        var title = new TextView(this);
        title.setText(text);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(8), 0, dp(6));
        return title;
    }

    private TextView cloudCardText() {
        var text = new TextView(this);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        text.setTextColor(COLOR_TEXT);
        text.setPadding(dp(12), dp(10), dp(12), dp(10));
        text.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        return text;
    }

    private void buildHistoryPage() {
        var title = new TextView(this);
        title.setText("Historico de impressoes");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        historyPage.addView(title, fullWidth(-2));

        historySummaryText = new TextView(this);
        historySummaryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        historySummaryText.setTextColor(COLOR_PRIMARY_DARK);
        historySummaryText.setPadding(dp(12), dp(10), dp(12), dp(10));
        historySummaryText.setBackground(rounded(COLOR_SURFACE_ALT, 10, COLOR_BORDER, 1));
        historyPage.addView(historySummaryText, withBottomMargin(fullWidth(-2), dp(10)));

        var actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        var refreshButton = new Button(this);
        refreshButton.setText("Atualizar");
        refreshButton.setOnClickListener(v -> refreshHistoryPage());
        actions.addView(refreshButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearButton = new Button(this);
        clearButton.setText("Limpar");
        clearButton.setOnClickListener(v -> confirmClearPrintHistory());
        actions.addView(clearButton, new LinearLayout.LayoutParams(dp(116), dp(52)));
        historyPage.addView(actions, fullWidth(-2));

        var recentTitle = new TextView(this);
        recentTitle.setText("Ultimas impressoes");
        recentTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        recentTitle.setTextColor(COLOR_TEXT);
        recentTitle.setTypeface(Typeface.DEFAULT_BOLD);
        recentTitle.setPadding(0, dp(12), 0, dp(6));
        historyPage.addView(recentTitle, fullWidth(-2));

        historyListText = new TextView(this);
        historyListText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        historyListText.setTextColor(COLOR_TEXT);
        historyListText.setPadding(dp(12), dp(10), dp(12), dp(10));
        historyListText.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        historyPage.addView(historyListText, fullWidth(-2));

        refreshHistoryPage();
    }

    private void buildAlertsPage() {
        var title = new TextView(this);
        title.setText("Avisos");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        alertsPage.addView(title, fullWidth(-2));

        alertsSummaryText = new TextView(this);
        alertsSummaryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        alertsSummaryText.setTextColor(COLOR_PRIMARY_DARK);
        alertsSummaryText.setPadding(dp(12), dp(10), dp(12), dp(10));
        alertsSummaryText.setBackground(rounded(COLOR_SURFACE_ALT, 10, COLOR_BORDER, 1));
        alertsPage.addView(alertsSummaryText, withBottomMargin(fullWidth(-2), dp(10)));

        var actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        var refreshButton = new Button(this);
        refreshButton.setText("Atualizar");
        refreshButton.setOnClickListener(v -> refreshAlertsPage());
        actions.addView(refreshButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        var clearButton = new Button(this);
        clearButton.setText("Limpar OCR");
        clearButton.setOnClickListener(v -> confirmClearOcrAlerts());
        actions.addView(clearButton, new LinearLayout.LayoutParams(dp(132), dp(52)));
        alertsPage.addView(actions, fullWidth(-2));

        var recentTitle = new TextView(this);
        recentTitle.setText("Alertas OCR");
        recentTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        recentTitle.setTextColor(COLOR_TEXT);
        recentTitle.setTypeface(Typeface.DEFAULT_BOLD);
        recentTitle.setPadding(0, dp(12), 0, dp(6));
        alertsPage.addView(recentTitle, fullWidth(-2));

        alertsListText = new TextView(this);
        alertsListText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        alertsListText.setTextColor(COLOR_TEXT);
        alertsListText.setPadding(dp(12), dp(10), dp(12), dp(10));
        alertsListText.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        alertsPage.addView(alertsListText, fullWidth(-2));

        refreshAlertsPage();
    }

    private LinearLayout buildTabs() {
        var tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.VERTICAL);
        tabs.setPadding(0, 0, 0, dp(10));

        validityTabButton = tabButton(isPeixariaMainPage() && isBetaUnlocked() ? "Peixaria" : "Validade", "validity");
        validityTabButton.setOnClickListener(v -> showPage(isPeixariaMainPage() && isBetaUnlocked() ? MAIN_PAGE_PEIXARIA : "validity"));
        priceTabButton = tabButton("Precos", "prices");
        catalogTabButton = tabButton("Catálogo", "catalog");
        moreTabButton = tabButton("Mais", "more");

        var firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        firstRow.setGravity(Gravity.CENTER_VERTICAL);
        firstRow.addView(validityTabButton, new LinearLayout.LayoutParams(0, dp(48), 1));
        firstRow.addView(priceTabButton, new LinearLayout.LayoutParams(0, dp(48), 1));
        firstRow.addView(catalogTabButton, new LinearLayout.LayoutParams(0, dp(48), 1));
        firstRow.addView(moreTabButton, new LinearLayout.LayoutParams(0, dp(48), 1));

        tabs.addView(firstRow, fullWidth(dp(48)));
        return tabs;
    }

    private Button tabButton(String label, String pageName) {
        var button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(v -> showPage(pageName));
        return button;
    }

    private void updatePrimaryTabButton() {
        if (validityTabButton == null) {
            return;
        }
        boolean peixariaMain = isPeixariaMainPage() && isBetaUnlocked();
        validityTabButton.setText(peixariaMain ? "Peixaria" : "Validade");
        validityTabButton.setOnClickListener(v -> showPage(peixariaMain ? MAIN_PAGE_PEIXARIA : "validity"));
    }

    private LinearLayout page() {
        var page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(0, dp(4), 0, 0);
        return page;
    }

    private EditText createCopiesEdit() {
        var editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editText.setText("1");
        return editText;
    }

    private LinearLayout.LayoutParams manualSideParams() {
        var params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(dp(6), 0, 0, 0);
        return params;
    }

    private void configureManualField(CheckBox checkBox, EditText editText, View fieldView) {
        checkBox.setChecked(false);
        editText.setFocusable(false);
        fieldView.setVisibility(View.GONE);
        checkBox.setOnCheckedChangeListener((button, checked) -> {
            fieldView.setVisibility(checked ? View.VISIBLE : View.GONE);
            editText.setFocusable(checked);
            editText.setFocusableInTouchMode(checked);
            if (checked) {
                editText.requestFocus();
            } else {
                editText.clearFocus();
            }
        });
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && checkBox.isChecked() && editText.getText().toString().trim().isEmpty()) {
                checkBox.setChecked(false);
            }
        });
    }

    private Spinner createFontSizeSpinner() {
        var spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Normal", "Grande", "Muito grande")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private Spinner createOnlyPriceSizeSpinner() {
        var spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Pequeno", "Normal", "Grande", "Muito grande", "Maximo")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(2);
        return spinner;
    }

    private void showPage(String pageName) {
        if (("beta".equals(pageName) || "ocr".equals(pageName) || "cloud".equals(pageName)
                || MAIN_PAGE_PEIXARIA.equals(pageName) || "peixaria_panel".equals(pageName)) && !isBetaUnlocked()) {
            pendingBetaPage = pageName;
            showBetaLoginDialog();
            pageName = "settings";
        }

        boolean prices = "prices".equals(pageName);
        boolean onlyPrices = "only_prices".equals(pageName);
        boolean validity = "validity".equals(pageName);
        boolean catalog = "catalog".equals(pageName);
        boolean alerts = "alerts".equals(pageName);
        boolean history = "history".equals(pageName);
        boolean settings = "settings".equals(pageName);
        boolean more = "more".equals(pageName);
        boolean textOnly = "text_only".equals(pageName);
        boolean imageOnly = "image_only".equals(pageName);
        boolean photo50x30 = "photo_50x30".equals(pageName);
        boolean ocr = "ocr".equals(pageName);
        boolean beta = "beta".equals(pageName);
        boolean cloud = "cloud".equals(pageName);
        boolean peixaria = MAIN_PAGE_PEIXARIA.equals(pageName);
        boolean peixariaPanel = "padaria_panel".equals(pageName);
        boolean secondary = alerts || history || onlyPrices || settings || more || textOnly || imageOnly || photo50x30 || ocr || beta || cloud || peixaria || peixariaPanel;

        if (ocr) {
            startOcrCameraIfReady();
        } else {
            stopOcrCamera();
        }

        if (pricePage != null) {
            pricePage.setVisibility(prices ? View.VISIBLE : View.GONE);
        }
        if (onlyPricePage != null) {
            onlyPricePage.setVisibility(onlyPrices ? View.VISIBLE : View.GONE);
        }
        if (textOnlyPage != null) {
            textOnlyPage.setVisibility(textOnly ? View.VISIBLE : View.GONE);
            if (textOnly) {
                refreshTextOnlyPreview();
            }
        }
        if (imageOnlyPage != null) {
            imageOnlyPage.setVisibility(imageOnly ? View.VISIBLE : View.GONE);
            if (imageOnly) {
                refreshImageOnlyPreview();
            }
        }
        if (photo50x30Page != null) {
            photo50x30Page.setVisibility(photo50x30 ? View.VISIBLE : View.GONE);
            if (photo50x30) {
                refreshPhoto50x30Preview();
            }
        }
        if (validityPage != null) {
            validityPage.setVisibility(validity ? View.VISIBLE : View.GONE);
        }
        if (catalogPage != null) {
            catalogPage.setVisibility(catalog ? View.VISIBLE : View.GONE);
            if (catalog) {
                refreshCatalogPage();
            }
        }
        if (morePage != null) {
            morePage.setVisibility(more ? View.VISIBLE : View.GONE);
        }
        if (changeProfileButton != null) {
            changeProfileButton.setVisibility(more ? View.GONE : View.VISIBLE);
        }
        if (checkUpdateButton != null) {
            checkUpdateButton.setVisibility(more ? View.GONE : View.VISIBLE);
        }
        if (historyPage != null) {
            historyPage.setVisibility(history ? View.VISIBLE : View.GONE);
            if (history) {
                refreshHistoryPage();
            }
        }
        if (alertsPage != null) {
            alertsPage.setVisibility(alerts ? View.VISIBLE : View.GONE);
            if (alerts) {
                refreshAlertsPage();
            }
        }
        if (settingsPage != null) {
            settingsPage.setVisibility(settings ? View.VISIBLE : View.GONE);
        }
        if (ocrPage != null) {
            ocrPage.setVisibility(ocr ? View.VISIBLE : View.GONE);
        }
        if (betaPage != null) {
            betaPage.setVisibility(beta ? View.VISIBLE : View.GONE);
        }
        if (cloudPage != null) {
            cloudPage.setVisibility(cloud ? View.VISIBLE : View.GONE);
            if (cloud) {
                refreshCloudDashboard();
            }
        }
        if (peixariaPage != null) {
            peixariaPage.setVisibility(peixaria ? View.VISIBLE : View.GONE);
            if (peixaria) {
                refreshPeixariaPreview();
            }
        }
        if (peixariaPanelPage != null) {
            peixariaPanelPage.setVisibility(peixariaPanel ? View.VISIBLE : View.GONE);
            if (peixariaPanel) {
                refreshPeixariaPanel();
            }
        }

        styleTab(priceTabButton, prices);
        styleTab(onlyPriceTabButton, onlyPrices);
        styleTab(textOnlyTabButton, textOnly);
        styleTab(imageOnlyTabButton, imageOnly);
        styleTab(photo50x30TabButton, photo50x30);
        styleTab(validityTabButton, validity);
        styleTab(catalogTabButton, catalog);
        styleTab(alertsTabButton, alerts);
        styleTab(historyTabButton, history);
        styleTab(settingsTabButton, settings);
        styleTab(ocrTabButton, ocr);
        styleTab(betaTabButton, beta);
        styleTab(cloudTabButton, cloud);
        styleTab(moreTabButton, secondary);
    }

    private void showAboutDialog() {
        String message = "Validade Pro\n"
                + "Versao " + BuildConfig.VERSION_NAME + "\n"
                + "Codigo " + BuildConfig.VERSION_CODE;

        new AlertDialog.Builder(this)
                .setTitle("Sobre o app")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showBetaLoginDialog() {
        if (isBetaUnlocked()) {
            showPage(pendingBetaPage);
            return;
        }
        showCredentialDialog("Acesso beta", () -> {
            setBetaUnlocked(true);
            showPage(pendingBetaPage);
            setStatus("Aba beta liberada.");
        });
    }

    private void showAdminConfirmDialog(Runnable onSuccess) {
        showCredentialDialog("Confirmação administrativa", onSuccess);
    }

    private void showCredentialDialog(String title, Runnable onSuccess) {
        var form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(4), dp(8), dp(4), 0);

        TextView hint = new TextView(this);
        hint.setText("Digite usuario e senha para continuar");
        hint.setTextColor(COLOR_MUTED);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        form.addView(hint, fullWidth(-2));

        LinearLayout loginRow = new LinearLayout(this);
        loginRow.setGravity(Gravity.CENTER);
        loginRow.setPadding(0, dp(10), 0, dp(12));
        form.addView(field("Usuario (4 letras)", loginRow), fullWidth(-2));
        List<EditText> loginBoxes = addCredentialBoxes(loginRow, 4, false);

        LinearLayout passwordRow = new LinearLayout(this);
        passwordRow.setGravity(Gravity.CENTER);
        form.addView(field("Senha", passwordRow), fullWidth(-2));
        List<EditText>[] passwordBoxesHolder = new List[]{new ArrayList<>()};
        passwordBoxesHolder[0] = addCredentialBoxes(passwordRow, LEGACY_BETA_PASSWORD.length(), true);

        TextView status = new TextView(this);
        status.setTextColor(COLOR_MUTED);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(10), 0, 0);
        form.addView(status, fullWidth(-2));

        TextWatcher loginWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String login = credentialValue(loginBoxes);
                if (login.length() == 4) {
                    int passwordLength = expectedPasswordForLogin(login).length();
                    passwordRow.removeAllViews();
                    passwordBoxesHolder[0] = addCredentialBoxes(passwordRow, passwordLength, true);
                    passwordBoxesHolder[0].get(0).requestFocus();
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        for (EditText box : loginBoxes) {
            box.addTextChangedListener(loginWatcher);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(form)
                .setPositiveButton("Entrar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(v -> {
            Button enterButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            enterButton.setOnClickListener(button -> {
                String login = credentialValue(loginBoxes);
                String password = credentialValue(passwordBoxesHolder[0]);
                if (isValidCredential(login, password)) {
                    status.setText("Credenciais confirmadas");
                    animateCredentialSuccess(loginBoxes, passwordBoxesHolder[0], () -> {
                        dialog.dismiss();
                        onSuccess.run();
                    });
                    return;
                }
                status.setText("Usuario ou senha invalidos");
                for (EditText box : passwordBoxesHolder[0]) {
                    box.setText("");
                }
                if (!passwordBoxesHolder[0].isEmpty()) {
                    passwordBoxesHolder[0].get(0).requestFocus();
                }
                setStatus("Login admin recusado.");
                Toast.makeText(this, "Login ou senha invalidos.", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
        loginBoxes.get(0).requestFocus();
    }

    private List<EditText> addCredentialBoxes(LinearLayout row, int count, boolean password) {
        List<EditText> boxes = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            EditText box = new EditText(this);
            box.setGravity(Gravity.CENTER);
            box.setSingleLine(true);
            box.setTextColor(COLOR_TEXT);
            box.setHintTextColor(COLOR_MUTED);
            box.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            box.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            box.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(1)});
            if (password) {
                box.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            box.setBackground(rounded(0xFF101E27, 12, COLOR_BORDER, 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(52), 1f);
            params.setMargins(dp(3), 0, dp(3), 0);
            row.addView(box, params);
            boxes.add(box);
            final int position = index;
            box.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        box.setBackground(rounded(0xFF123A3B, 12, COLOR_ACCENT, 2));
                        box.animate().scaleX(1.08f).scaleY(1.08f).setDuration(140).withEndAction(() ->
                                box.animate().scaleX(1f).scaleY(1f).setDuration(140).start()).start();
                        if (position + 1 < boxes.size()) {
                            boxes.get(position + 1).requestFocus();
                        }
                    }
                }
                @Override public void afterTextChanged(Editable s) { }
            });
            box.setOnKeyListener((view, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN
                        && box.getText().length() == 0 && position > 0) {
                    boxes.get(position - 1).requestFocus();
                }
                return false;
            });
        }
        return boxes;
    }

    private String credentialValue(List<EditText> boxes) {
        StringBuilder value = new StringBuilder();
        for (EditText box : boxes) {
            value.append(box.getText());
        }
        return value.toString();
    }

    private String expectedPasswordForLogin(String login) {
        return LEGACY_BETA_LOGIN.equalsIgnoreCase(login) ? LEGACY_BETA_PASSWORD : BETA_PASSWORD;
    }

    private boolean isValidCredential(String login, String password) {
        return (BETA_LOGIN.equalsIgnoreCase(login) && BETA_PASSWORD.equals(password))
                || (LEGACY_BETA_LOGIN.equalsIgnoreCase(login) && LEGACY_BETA_PASSWORD.equals(password));
    }

    private void animateCredentialSuccess(List<EditText> loginBoxes, List<EditText> passwordBoxes, Runnable onComplete) {
        List<EditText> allBoxes = new ArrayList<>(loginBoxes);
        allBoxes.addAll(passwordBoxes);
        for (EditText box : allBoxes) {
            box.setBackground(rounded(0xFF164A42, 12, 0xFF6EE7B7, 2));
            box.animate().scaleX(1.12f).scaleY(1.12f).setDuration(180).start();
        }
        new android.os.Handler(getMainLooper()).postDelayed(onComplete, 500);
    }

    private void printExistingPeixariaEntry(PeixariaEntry original, int copies) {
        if (original == null) return;
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }
        PeixariaEntry entry = new PeixariaEntry(System.currentTimeMillis(), original.lot, original.product, original.weightKg, copies, original.expiryAt);
        setStatus("Reimprimindo lote " + entry.lot + "...");
        new Thread(() -> {
            try {
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    List<byte[]> payloads = buildPeixariaPayloadVariants(entry);
                    payloadsRef.set(payloads);
                    return payloads;
                }, () -> {
                    // On success, save history entry without changing reserved sequence
                    savePeixariaHistoryEntry(entry);
                        syncPadariaLotAsync(entry);
                });
                recordPrintSuccess();
                List<byte[]> payloads = payloadsRef.get();
                setStatus("Reimpressao enviada: lote " + entry.lot + " (" + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes).");
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void savePeixariaHistoryEntry(PeixariaEntry entry) {
        if (entry == null) return;
        List<PeixariaEntry> entries = loadPeixariaHistory();
        entries.add(0, entry);
        entries.sort((left, right) -> Long.compare(right.printedAt, left.printedAt));
        savePeixariaHistory(entries);
        refreshPeixariaPreview();
        refreshPeixariaPanel();
    }

    private boolean isBetaUnlocked() {
        return prefs().getBoolean(PREF_BETA_UNLOCKED, false);
    }

    private void setBetaUnlocked(boolean unlocked) {
        prefs().edit().putBoolean(PREF_BETA_UNLOCKED, unlocked).apply();
        if (ocrTabButton != null) {
            ocrTabButton.setVisibility(unlocked ? View.VISIBLE : View.GONE);
        }
        if (betaTabButton != null) {
            betaTabButton.setVisibility(unlocked ? View.VISIBLE : View.GONE);
        }
        if (betaToolsRow != null) {
            betaToolsRow.setVisibility(unlocked ? View.VISIBLE : View.GONE);
        }
        if (!unlocked) {
            stopOcrCamera();
            showPage("settings");
            setStatus("Aba beta ocultada.");
        }
    }

    private void loadBetaRule(int position) {
        if (position < 0 || position >= rules.size()
                || betaRuleMatchEdit == null
                || betaRuleDaysEdit == null
                || betaRuleHoursEdit == null
                || betaRuleLabelEdit == null) {
            return;
        }

        betaRuleDraftNew = false;
        ValidityRule rule = rules.get(position);
        betaRuleMatchEdit.setText(rule.match);
        betaRuleDaysEdit.setText(rule.validityDays > 0 ? String.valueOf(rule.validityDays) : "");
        betaRuleHoursEdit.setText(rule.validityHours > 0 ? String.valueOf(rule.validityHours) : "");
        betaRuleLabelEdit.setText(rule.startLabel);
    }

    private void newBetaRuleDraft() {
        betaRuleDraftNew = true;
        if (betaRuleMatchEdit != null) {
            betaRuleMatchEdit.setText("");
            betaRuleMatchEdit.requestFocus();
        }
        if (betaRuleDaysEdit != null) {
            betaRuleDaysEdit.setText("");
        }
        if (betaRuleHoursEdit != null) {
            betaRuleHoursEdit.setText("");
        }
        if (betaRuleLabelEdit != null) {
            betaRuleLabelEdit.setText("Fab");
        }
        setStatus("Nova regra pronta para preencher.");
    }

    private void saveSelectedBetaRule() {
        String match = betaRuleMatchEdit == null ? "" : betaRuleMatchEdit.getText().toString().trim();
        String label = betaRuleLabelEdit == null ? "" : betaRuleLabelEdit.getText().toString().trim();
        int days = parsePositiveInt(betaRuleDaysEdit);
        int hours = parsePositiveInt(betaRuleHoursEdit);

        if (match.isEmpty()) {
            showError("Informe o produto ou palavra da regra.");
            return;
        }
        if (days <= 0 && hours <= 0) {
            showError("Informe dias ou horas de validade.");
            return;
        }
        if (label.isEmpty()) {
            label = "Fab";
        }
        if (hours > 0) {
            days = 0;
        }

        ValidityRule updated = new ValidityRule(match, days, hours, label);
        int position = betaRuleSpinner == null ? -1 : betaRuleSpinner.getSelectedItemPosition();
        if (betaRuleDraftNew || position < 0 || position >= rules.size()) {
            rules.add(updated);
            position = rules.size() - 1;
        } else {
            rules.set(position, updated);
        }

        betaRuleDraftNew = false;
        persistCurrentCatalog("Prazo salvo no catalogo.");
        if (betaRuleSpinner != null && position >= 0 && position < rules.size()) {
            betaRuleSpinner.setSelection(position);
        }
        loadBetaRule(position);
    }

    private void removeSelectedBetaRule() {
        if (betaRuleSpinner == null || rules.isEmpty()) {
            return;
        }

        int position = betaRuleSpinner.getSelectedItemPosition();
        if (position < 0 || position >= rules.size()) {
            return;
        }

        ValidityRule rule = rules.get(position);
        if ("*".equals(rule.match)) {
            showError("A regra * precisa ficar como prazo padrao.");
            return;
        }

        rules.remove(position);
        persistCurrentCatalog("Regra removida do catalogo.");
        int next = Math.min(position, rules.size() - 1);
        if (next >= 0) {
            betaRuleSpinner.setSelection(next);
            loadBetaRule(next);
        }
    }

    private int parsePositiveInt(EditText editText) {
        if (editText == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(editText.getText().toString().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void persistCurrentCatalog(String status) {
        try {
            String profileKey = currentCatalogProfileKey();
            String json = buildCatalogJson();
            String url = catalogUrlEdit == null ? "" : catalogUrlEdit.getText().toString().trim();
            prefs().edit()
                    .putString(PREF_CATALOG_JSON, json)
                    .putString(CatalogProfileUtils.catalogJsonKey(profileKey), json)
                    .putString(PREF_CATALOG_URL, url)
                    .putString(CatalogProfileUtils.catalogUrlKey(profileKey), url)
                    .apply();

            File catalogFile = getCatalogFile();
            File parent = catalogFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Nao foi possivel criar a pasta do catalogo.");
            }
            try (OutputStream output = new FileOutputStream(catalogFile)) {
                output.write(json.getBytes(StandardCharsets.UTF_8));
            }

            refreshBetaRuleAdapter();
            refreshCatalogAdapter();
            setStatus(status);
        } catch (Exception e) {
            showError("Nao foi possivel salvar o catalogo: " + (e.getMessage() == null ? e.toString() : e.getMessage()));
        }
    }

    private void applySelectedCatalogProfile() {
        if (peixeiroSpinner == null) {
            return;
        }
        List<String> keys = CatalogProfileUtils.profileKeys();
        String selected = currentCatalogProfileKey();
        int position = keys.indexOf(selected);
        if (position < 0) {
            position = keys.indexOf(CatalogProfileUtils.defaultProfileKey());
        }
        if (position >= 0) {
            peixeiroSpinner.setSelection(position);
        }
        updateCurrentProfileBanner();
        syncCatalogUrlFieldFromCurrentProfile();
    }

    private void switchCatalogProfile(String profileKey) {
        String normalized = CatalogProfileUtils.normalizeProfileKey(profileKey);
        CatalogProfileUtils.saveSelectedProfile(prefs(), normalized);
        updateCurrentProfileBanner();
        syncCatalogUrlFieldFromCurrentProfile();
        CatalogData loaded = loadCatalog();
        applyCatalog(loaded, "Perfil " + CatalogProfileUtils.labelForProfile(normalized) + " carregado.");
    }

    private void updateCurrentProfileBanner() {
        if (currentProfileBanner == null) {
            return;
        }
        currentProfileBanner.setText("Estabelecimento atual: " + currentEstablishmentName());
    }

    private String currentEstablishmentName() {
        return prefs().getString(PREF_ESTABLISHMENT_NAME, "Padaria Lobo");
    }

    private void saveEstablishmentName() {
        if (establishmentNameEdit == null) {
            return;
        }
        String name = establishmentNameEdit.getText().toString().trim();
        prefs().edit().putString(PREF_ESTABLISHMENT_NAME,
                name.isEmpty() ? "Padaria Lobo" : name).apply();
        updateCurrentProfileBanner();
    }

    private void syncCatalogUrlFieldFromCurrentProfile() {
        if (catalogUrlEdit == null) {
            return;
        }
        String profileKey = currentCatalogProfileKey();
        String storedUrl = prefs().getString(CatalogProfileUtils.catalogUrlKey(profileKey), "");
        if (storedUrl == null || storedUrl.trim().isEmpty()) {
            storedUrl = CatalogProfileUtils.isPrivilegedProfile(profileKey)
                    ? prefs().getString(PREF_CATALOG_URL, DEFAULT_CATALOG_URL)
                    : DEFAULT_CATALOG_URL;
        }
        catalogUrlEdit.setText(storedUrl);
    }

    private String currentCatalogProfileKey() {
        return CatalogProfileUtils.selectedProfileKey(prefs());
    }

    private String buildCatalogJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("version", 2);

        JSONArray rulesArray = new JSONArray();
        for (ValidityRule rule : rules) {
            JSONObject item = new JSONObject();
            item.put("match", rule.match);
            if (rule.usesHours()) {
                item.put("validityHours", rule.validityHours);
            } else {
                item.put("validityDays", rule.validityDays > 0 ? rule.validityDays : 5);
            }
            item.put("startLabel", rule.startLabel);
            rulesArray.put(item);
        }
        object.put("rules", rulesArray);

        JSONArray productsArray = new JSONArray();
        for (String product : todosProdutos) {
            productsArray.put(product);
        }
        object.put("products", productsArray);

        JSONObject categoriesObj = new JSONObject();
        for (java.util.Map.Entry<String, List<String>> entry : categoriasMap.entrySet()) {
            if ("Todos".equals(entry.getKey())) {
                continue;
            }
            JSONArray catArr = new JSONArray();
            for (String p : entry.getValue()) {
                catArr.put(p);
            }
            categoriesObj.put(entry.getKey(), catArr);
        }
        object.put("categories", categoriesObj);

        return object.toString(2);
    }

    private void refreshBetaRuleAdapter() {
        if (betaRuleAdapter == null) {
            return;
        }
        betaRuleAdapter.clear();
        betaRuleAdapter.addAll(betaRuleLabels());
        betaRuleAdapter.notifyDataSetChanged();
    }

    private List<String> betaRuleLabels() {
        List<String> labels = new ArrayList<>();
        for (ValidityRule rule : rules) {
            String name = "*".equals(rule.match) ? "Padrao" : rule.match;
            labels.add(name + " - " + rule.displayDuration() + " - " + rule.startLabel);
        }
        return labels;
    }

    private void styleTab(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.setTextColor(active ? Color.WHITE : COLOR_PRIMARY_DARK);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackground(active
                ? rounded(COLOR_PRIMARY, 12, COLOR_PRIMARY_DARK, 1)
                : rounded(COLOR_SURFACE, 12, COLOR_BORDER, 1));
    }

    private void polishTree(View view) {
        if (view instanceof CheckBox) {
            styleCheckBox((CheckBox) view);
        } else if (view instanceof Button) {
            Object tag = view.getTag();
            styleButton((Button) view, "primary".equals(tag));
        } else if (view instanceof EditText) {
            styleInput((EditText) view);
        } else if (view instanceof Spinner) {
            styleSpinner((Spinner) view);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                polishTree(group.getChildAt(i));
            }
        }
    }

    private void styleButton(Button button, boolean primary) {
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(primary ? Color.WHITE : COLOR_PRIMARY_DARK);
        button.setBackground(primary
                ? rounded(COLOR_PRIMARY, 12, COLOR_PRIMARY_DARK, 1)
                : rounded(COLOR_SURFACE, 12, COLOR_BORDER, 1));
        button.setPadding(dp(10), 0, dp(10), 0);
    }

    private void styleInput(EditText input) {
        input.setTextColor(COLOR_TEXT);
        input.setHintTextColor(COLOR_MUTED);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        input.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        input.setPadding(dp(12), 0, dp(12), 0);
    }

    private void styleSpinner(Spinner spinner) {
        spinner.setBackground(rounded(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        spinner.setPadding(dp(8), 0, dp(8), 0);
    }

    private void styleCheckBox(CheckBox checkBox) {
        checkBox.setTextColor(COLOR_TEXT);
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        checkBox.setPadding(dp(4), 0, 0, 0);
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private LinearLayout.LayoutParams withTopMargin(LinearLayout.LayoutParams params, int margin) {
        params.topMargin = margin;
        return params;
    }

    private LinearLayout.LayoutParams withBottomMargin(LinearLayout.LayoutParams params, int margin) {
        params.bottomMargin = margin;
        return params;
    }

    private void updateOnlyPriceRepeatControls() {
        if (onlyPriceRepeatCheck == null || onlyPriceRepeatCountSpinner == null || onlyPriceFontSizeSpinner == null) {
            return;
        }
        boolean isSmall = onlyPriceFontSizeSpinner.getSelectedItemPosition() == 0;
        onlyPriceRepeatCheck.setEnabled(isSmall);
        onlyPriceRepeatCountSpinner.setEnabled(isSmall && onlyPriceRepeatCheck.isChecked());
    }

    private int onlyPriceItemsPerLabel() {
        if (onlyPriceRepeatCheck == null || onlyPriceFontSizeSpinner == null || onlyPriceRepeatCountSpinner == null) {
            return 1;
        }
        if (!onlyPriceRepeatCheck.isChecked() || onlyPriceFontSizeSpinner.getSelectedItemPosition() != 0) {
            return 1;
        }
        return onlyPriceRepeatCountSpinner.getSelectedItemPosition() == 0 ? 4 : 6;
    }

    private CatalogData loadCatalog() {
        String profileKey = currentCatalogProfileKey();
        String savedJson = prefs().getString(CatalogProfileUtils.catalogJsonKey(profileKey), null);
        if (savedJson == null && CatalogProfileUtils.isPrivilegedProfile(profileKey)) {
            savedJson = prefs().getString(PREF_CATALOG_JSON, null);
        }
        if (savedJson != null) {
            try {
                return parseCatalogJson(savedJson);
            } catch (JSONException ignored) {
            }
        }

        File localFile = getCatalogFile();
        if (localFile.exists()) {
            try (InputStream input = new FileInputStream(localFile)) {
                return parseCatalogJson(readAll(input));
            } catch (Exception ignored) {
            }
        }

        return new CatalogData(new ArrayList<>(PRODUTOS), new ArrayList<>(DEFAULT_RULES));
    }

    private void updateCatalog() {
        String profileKey = currentCatalogProfileKey();
        String url = catalogUrlEdit == null ? "" : catalogUrlEdit.getText().toString().trim();
        if (url.isEmpty()) {
            reloadLocalCatalog();
            return;
        }

        prefs().edit()
                .putString(CatalogProfileUtils.catalogUrlKey(profileKey), url)
                .putString(PREF_CATALOG_URL, url)
                .apply();
        setStatus("Atualizando lista...");

        new Thread(() -> {
            try {
                String json = downloadText(url);
                CatalogData loaded = parseCatalogJson(json);
                String previousJson = prefs().getString(CatalogProfileUtils.catalogJsonKey(profileKey), prefs().getString(PREF_CATALOG_JSON, null));
                List<CatalogSyncJobService.CatalogAddition> additions = CatalogSyncJobService.findNewProducts(previousJson, json);
                prefs().edit()
                        .putString(CatalogProfileUtils.catalogUrlKey(profileKey), url)
                        .putString(CatalogProfileUtils.catalogJsonKey(profileKey), json)
                        .putString(PREF_CATALOG_URL, url)
                        .putString(PREF_CATALOG_JSON, json)
                        .putLong(CatalogSyncJobService.PREF_LAST_CATALOG_SYNC_AT, System.currentTimeMillis())
                        .apply();
                runOnUiThread(() -> {
                    applyCatalog(loaded, "Lista atualizada online: " + loaded.products.size() + " itens.");
                    maybeShowCatalogUpdateNotification(additions, true);
                });
            } catch (Exception e) {
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void reloadLocalCatalog() {
        File localFile = getCatalogFile();
        if (!localFile.exists()) {
            applyCatalog(new CatalogData(new ArrayList<>(PRODUTOS), new ArrayList<>(DEFAULT_RULES)), "Lista padrao carregada.");
            return;
        }

        try (InputStream input = new FileInputStream(localFile)) {
            CatalogData loaded = parseCatalogJson(readAll(input));
            applyCatalog(loaded, "Lista local atualizada: " + loaded.products.size() + " itens.");
        } catch (Exception e) {
            showError(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private void checkForUpdates(boolean manual) {
        checkForUpdates(manual, !manual);
    }

    private void checkForUpdates(boolean manual, boolean forceWhenAvailable) {
        lastUpdateCheckMs = System.currentTimeMillis();
        if (manual) {
            setStatus("Verificando atualizacao...");
        }

        new Thread(() -> {
            try {
                String updateUrl = UPDATE_INFO_URL + "?t=" + System.currentTimeMillis();
                JSONObject update = new JSONObject(downloadText(updateUrl));
                int latestCode = update.optInt("latestVersionCode", 0);
                String latestName = update.optString("latestVersionName", "");
                String apkUrl = update.optString("apkUrl", "");
                String notes = update.optString("notes", "");

                if (latestCode > BuildConfig.VERSION_CODE && !apkUrl.isEmpty()) {
                    runOnUiThread(() -> showUpdateDialog(latestName, notes, apkUrl, forceWhenAvailable));
                } else if (manual) {
                    setStatus("App ja esta atualizado. Versao " + BuildConfig.VERSION_NAME + ".");
                }
            } catch (Exception e) {
                if (manual) {
                    showError("Nao foi possivel verificar atualizacao: " + (e.getMessage() == null ? e.toString() : e.getMessage()));
                }
            }
        }).start();
    }

    private void showUpdateDialog(String latestName, String notes, String apkUrl, boolean forced) {
        if (updateDialogVisible) {
            return;
        }
        updateDialogVisible = true;

        String message = "Nova versao disponivel"
                + (latestName.isEmpty() ? "" : ": " + latestName)
                + "\n\n"
                + (notes.isEmpty() ? "Baixe para continuar usando a versao mais nova." : notes);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Atualizacao do app")
                .setMessage(message)
                .setPositiveButton("Atualizar agora", (dialog, which) -> {
                    updateDialogVisible = false;
                    downloadAndInstallUpdate(apkUrl);
                })
                .setOnDismissListener(dialog -> updateDialogVisible = false);

        if (!forced) {
            builder.setNegativeButton("Depois", null);
        }

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(!forced);
        dialog.setCancelable(!forced);
        dialog.show();
    }

    private void downloadAndInstallUpdate(String apkUrl) {
        setStatus("Atualizacao baixando...");
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                File updateDir = new File(getCacheDir(), "updates");
                if (!updateDir.exists() && !updateDir.mkdirs()) {
                    throw new IOException("Nao foi possivel preparar a pasta da atualizacao.");
                }

                File apkFile = new File(updateDir, "ValidadeLoboPro-atualizacao.apk");
                if (apkFile.exists() && !apkFile.delete()) {
                    throw new IOException("Nao foi possivel trocar o APK antigo.");
                }

                connection = openUpdateConnection(apkUrl);
                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    throw new IOException("HTTP " + code + " ao baixar atualizacao.");
                }

                long total = connection.getContentLengthLong();
                long downloaded = 0;
                long lastStatusMs = 0;
                byte[] buffer = new byte[16 * 1024];

                try (InputStream input = connection.getInputStream();
                     OutputStream output = new FileOutputStream(apkFile)) {
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                        downloaded += read;

                        long now = System.currentTimeMillis();
                        if (now - lastStatusMs > 700) {
                            lastStatusMs = now;
                            if (total > 0) {
                                int percent = (int) Math.min(99, (downloaded * 100) / total);
                                setStatus("Atualizacao baixando... " + percent + "%");
                            } else {
                                setStatus("Atualizacao baixando... " + (downloaded / 1024) + " KB");
                            }
                        }
                    }
                }

                if (apkFile.length() < 1024) {
                    throw new IOException("Download vazio.");
                }

                Uri apkUri = ApkUpdateProvider.uriFor(this, apkFile);
                runOnUiThread(() -> openDownloadedApk(apkUri));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Falha ao baixar atualizacao: " + (e.getMessage() == null ? e.toString() : e.getMessage()));
                    openUpdateInBrowser(apkUrl);
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private HttpURLConnection openUpdateConnection(String apkUrl) throws IOException {
        String currentUrl = apkUrl;
        for (int redirects = 0; redirects < 6; redirects++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(currentUrl).openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/vnd.android.package-archive,*/*");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("User-Agent", "ValidadePT260/" + BuildConfig.VERSION_NAME);

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_PERM
                    || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER
                    || code == 307
                    || code == 308) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null || location.trim().isEmpty()) {
                    throw new IOException("Redirecionamento sem destino.");
                }
                currentUrl = new URL(new URL(currentUrl), location).toString();
                continue;
            }
            return connection;
        }
        throw new IOException("Muitos redirecionamentos ao baixar atualizacao.");
    }

    private void openDownloadedApk(Uri apkUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(apkUri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            setStatus("Atualizacao baixada. Confirme a instalacao no Android.");
        } catch (Exception e) {
            showError("APK baixado, mas nao foi possivel abrir o instalador: " + (e.getMessage() == null ? e.toString() : e.getMessage()));
        }
    }

    private void openUpdateInBrowser(String apkUrl) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)));
        } catch (Exception ignored) {
        }
    }

    private void applyCatalog(CatalogData loaded, String status) {
        todosProdutos.clear();
        todosProdutos.addAll(loaded.products.isEmpty() ? PRODUTOS : loaded.products);
        categoriasMap.clear();
        categoriasMap.putAll(categorizeProducts(todosProdutos, loaded.categories));
        categoriasLista.clear();
        categoriasLista.addAll(categoriasMap.keySet());
        if (setorAdapter != null) {
            setorAdapter.notifyDataSetChanged();
        }
        if (setorSpinner != null) {
            setorSpinner.setSelection(0);
        }
        updateProdutosSpinnerPorSetor();
        refreshCatalogPage();
        rules.clear();
        rules.addAll(loaded.rules.isEmpty() ? DEFAULT_RULES : loaded.rules);
        refreshBetaRuleAdapter();
        setStatus(status);
    }

    private void refreshCatalogAdapter() {
        if (produtoAdapter != null) {
            produtoAdapter.clear();
            produtoAdapter.addAll(produtos);
            produtoAdapter.notifyDataSetChanged();
        }
        if (produtoEdit != null) {
            produtoEdit.setText(firstProduct());
        }
        updateDates();
    }

    private void refreshCatalogPage() {
        if (catalogCategoryAdapter != null) {
            catalogCategoryAdapter.notifyDataSetChanged();
        }
        if (catalogCategorySpinner != null && catalogCategorySpinner.getSelectedItemPosition() < 0 && !categoriasLista.isEmpty()) {
            catalogCategorySpinner.setSelection(0);
        }

        if (catalogPageText != null) {
            String selectedCategory = "Todos";
            if (catalogCategorySpinner != null && catalogCategorySpinner.getSelectedItemPosition() >= 0
                    && catalogCategorySpinner.getSelectedItemPosition() < categoriasLista.size()) {
                selectedCategory = categoriasLista.get(catalogCategorySpinner.getSelectedItemPosition());
            }

            StringBuilder builder = new StringBuilder();
            builder.append("Catálogo de produtos\n");
            builder.append("Categoria selecionada: ").append(selectedCategory).append("\n");
            builder.append("\n");

            for (String category : categoriasLista) {
                if ("Todos".equals(category)) {
                    continue;
                }
                List<String> productsInCategory = categoriasMap.getOrDefault(category, new ArrayList<>());
                boolean active = category.equals(selectedCategory);
                builder.append(active ? "→ " : "   ");
                builder.append(category).append(" (" ).append(productsInCategory.size()).append(" itens)");
                builder.append(active ? "  [SELECIONADO]" : "");
                builder.append("\n");
                if (productsInCategory.isEmpty()) {
                    builder.append("      Nenhum produto nesta categoria.\n");
                } else {
                    for (String product : productsInCategory) {
                        builder.append("      • ").append(product).append("\n");
                    }
                }
                builder.append("\n");
            }

            catalogPageText.setText(builder.toString());
        }
    }

    private CatalogData parseCatalogJson(String json) throws JSONException {
        String trimmed = json.trim();
        JSONArray array;
        JSONArray rulesArray = null;
        if (trimmed.startsWith("[")) {
            array = new JSONArray(trimmed);
        } else {
            JSONObject object = new JSONObject(trimmed);
            array = object.getJSONArray("products");
            rulesArray = object.optJSONArray("rules");
        }

        List<String> loaded = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object item = array.get(i);
            String name;
            if (item instanceof JSONObject) {
                name = ((JSONObject) item).optString("name", "");
            } else {
                name = String.valueOf(item);
            }

            name = name.trim();
            if (!name.isEmpty() && !loaded.contains(name)) {
                loaded.add(name);
            }
        }

        if (loaded.isEmpty()) {
            throw new JSONException("Catalogo sem produtos.");
        }

        List<ValidityRule> loadedRules = new ArrayList<>();
        if (rulesArray != null) {
            for (int i = 0; i < rulesArray.length(); i++) {
                JSONObject rule = rulesArray.optJSONObject(i);
                if (rule == null) {
                    continue;
                }

                String match = rule.optString("match", "").trim();
                int days = rule.optInt("validityDays", 0);
                int hours = rule.optInt("validityHours", 0);
                String label = rule.optString("startLabel", "Fab").trim();
                if (!match.isEmpty() && (days > 0 || hours > 0)) {
                    loadedRules.add(new ValidityRule(match, days, hours, label.isEmpty() ? "Fab" : label));
                }
            }
        }
        java.util.Map<String, List<String>> loadedCategories = new java.util.LinkedHashMap<>();
        if (!trimmed.startsWith("[")) {
            try {
                JSONObject object = new JSONObject(trimmed);
                JSONObject categoriesObj = object.optJSONObject("categories");
                if (categoriesObj != null) {
                    java.util.Iterator<String> keys = categoriesObj.keys();
                    while (keys.hasNext()) {
                        String categoryName = keys.next();
                        JSONArray catProducts = categoriesObj.optJSONArray(categoryName);
                        if (catProducts != null) {
                            List<String> catList = new ArrayList<>();
                            for (int j = 0; j < catProducts.length(); j++) {
                                catList.add(catProducts.getString(j).trim());
                            }
                            loadedCategories.put(categoryName, catList);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return new CatalogData(loaded, loadedRules.isEmpty() ? new ArrayList<>(DEFAULT_RULES) : loadedRules, loadedCategories);
    }

    private java.util.Map<String, List<String>> categorizeProducts(List<String> allProducts, java.util.Map<String, List<String>> parsedCategories) {
        java.util.Map<String, List<String>> result = new java.util.LinkedHashMap<>();
        
        if (parsedCategories != null && !parsedCategories.isEmpty()) {
            result.put("Todos", new ArrayList<>(allProducts));
            List<String> categorized = new ArrayList<>();
            for (java.util.Map.Entry<String, List<String>> entry : parsedCategories.entrySet()) {
                if ("Todos".equals(entry.getKey())) {
                    continue;
                }
                List<String> cleanList = new ArrayList<>();
                for (String product : entry.getValue()) {
                    String name = product == null ? "" : product.trim();
                    if (!name.isEmpty() && allProducts.contains(name) && !cleanList.contains(name)) {
                        cleanList.add(name);
                        if (!categorized.contains(name)) {
                            categorized.add(name);
                        }
                    }
                }
                if (!cleanList.isEmpty()) {
                    result.put(entry.getKey(), cleanList);
                }
            }
            List<String> uncategorized = new ArrayList<>();
            for (String product : allProducts) {
                if (!categorized.contains(product)) {
                    uncategorized.add(product);
                }
            }
            if (!uncategorized.isEmpty()) {
                List<String> cozinha = result.get("Cozinha");
                if (cozinha == null) {
                    result.put("Cozinha", uncategorized);
                } else {
                    cozinha.addAll(uncategorized);
                }
            }
            return result;
        }

        List<String> roticeria = new ArrayList<>();
        List<String> confeitaria = new ArrayList<>();
        List<String> cozinha = new ArrayList<>();
        List<String> salao = new ArrayList<>();

        for (String p : allProducts) {
            String pLower = p.toLowerCase(Locale.ROOT);
            if (pLower.contains("pizza") || pLower.contains("salgado") || pLower.contains("coxinha")
                    || pLower.contains("bolinho") || pLower.contains("bolinha") || pLower.contains("kibe")
                    || pLower.contains("quibe") || pLower.contains("risole") || pLower.contains("croquete")
                    || pLower.contains("empada") || pLower.contains("esfiha") || pLower.contains("enroladinho")
                    || pLower.contains("pastel assado") || pLower.contains("quiche")) {
                roticeria.add(p);
            } else if (pLower.contains("mousse") || pLower.contains("pudim") || pLower.contains("bolo")
                    || pLower.contains("brigadeiro") || pLower.contains("doce de leite") || pLower.contains("chocolate")
                    || pLower.contains("torta") || pLower.contains("pave") || pLower.contains("gelatina")
                    || pLower.contains("creme de abacaxi") || pLower.contains("creme de leite")) {
                confeitaria.add(p);
            } else if (pLower.contains("pimenta") || pLower.contains("azeite")) {
                salao.add(p);
            } else {
                cozinha.add(p);
            }
        }

        result.put("Todos", new ArrayList<>(allProducts));
        if (!roticeria.isEmpty()) result.put("Roticeria", roticeria);
        if (!confeitaria.isEmpty()) result.put("Confeitaria", confeitaria);
        if (!cozinha.isEmpty()) result.put("Cozinha", cozinha);
        if (!salao.isEmpty()) result.put("Salão", salao);

        return result;
    }

    private void updateProdutosSpinnerPorSetor() {
        if (setorSpinner == null || produtoAdapter == null) return;
        int sectorPos = setorSpinner.getSelectedItemPosition();
        if (sectorPos < 0 || sectorPos >= categoriasLista.size()) return;
        String category = categoriasLista.get(sectorPos);
        List<String> filtered = categoriasMap.get(category);
        
        produtos.clear();
        if (filtered != null) {
            produtos.addAll(filtered);
        }
        produtoAdapter.notifyDataSetChanged();
        
        if (produtoSpinner != null && produtoAdapter.getCount() > 0) {
            produtoSpinner.setSelection(0);
        }
    }

    private String downloadText(String urlText) throws IOException {
        URL url = new URL(urlText);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Pragma", "no-cache");
        try {
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code + " ao baixar catalogo.");
            }
            try (InputStream input = connection.getInputStream()) {
                return readAll(input);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private File getCatalogFile() {
        return getCatalogFileForProfile(currentCatalogProfileKey());
    }

    private File getCatalogFileForProfile(String profileKey) {
        File dir = getExternalFilesDir(null);
        if (dir == null) {
            dir = getFilesDir();
        }
        File profileFile = new File(dir, "produtos-validade-" + CatalogProfileUtils.normalizeProfileKey(profileKey) + ".json");
        if (profileFile.exists()) {
            return profileFile;
        }
        if (CatalogProfileUtils.isPrivilegedProfile(profileKey)) {
            File legacyFile = new File(dir, "produtos-validade.json");
            if (legacyFile.exists()) {
                return legacyFile;
            }
        }
        return profileFile;
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private boolean isPeixariaMainPage() {
        String page = prefs().getString(PREF_MAIN_PAGE, MAIN_PAGE_VALIDITY);
        return MAIN_PAGE_PEIXARIA.equals(page);
    }

    private void setPeixariaMainPage(boolean peixaria) {
        prefs().edit().putString(PREF_MAIN_PAGE, peixaria ? MAIN_PAGE_PEIXARIA : MAIN_PAGE_VALIDITY).apply();
    }

    private boolean isSupabaseConfigured() {
        return resolvedSupabaseUrl().startsWith("http")
                && resolvedSupabaseAnonKey().length() > 20;
    }

    private String supabaseBaseUrl() {
        String value = resolvedSupabaseUrl();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String resolvedSupabaseUrl() {
        String configured = BuildConfig.SUPABASE_URL == null ? "" : BuildConfig.SUPABASE_URL.trim();
        if (configured.startsWith("http")) {
            return configured;
        }
        return prefs().getString(PREF_SUPABASE_URL, "").trim();
    }

    private String resolvedSupabaseAnonKey() {
        String configured = BuildConfig.SUPABASE_ANON_KEY == null ? "" : BuildConfig.SUPABASE_ANON_KEY.trim();
        if (configured.length() > 20) {
            return configured;
        }
        return prefs().getString(PREF_SUPABASE_ANON_KEY, "").trim();
    }

    private String selectedPrinterModel() {
        // If a transient override is set (per-device), prefer it for this print only.
        if (transientPrinterModelOverride != null && !transientPrinterModelOverride.trim().isEmpty()) {
            return transientPrinterModelOverride;
        }
        if (printerModelSpinner == null || printerModelSpinner.getSelectedItem() == null) {
            return PRINTER_MODEL_AUTO;
        }
        return printerModelSpinner.getSelectedItem().toString();
    }

    private boolean isDetectedPrinterModelXd210() {
        return PRINTER_MODEL_PT260_XD210.equals(transientPrinterModelOverride);
    }

    private void applySavedBluetoothMode() {
        if (metodoSpinner == null) {
            return;
        }
        String saved = prefs().getString(PREF_BLUETOOTH_MODE, PRINTER_MODEL_AUTO);
        applySpinnerValue(metodoSpinner, saved);
    }

    private void applySavedPrinterModel() {
        if (printerModelSpinner == null) {
            return;
        }
        String saved = prefs().getString(PREF_PRINTER_MODEL, PRINTER_MODEL_AUTO);
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) printerModelSpinner.getAdapter();
        if (adapter == null) {
            return;
        }
        int index = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (saved.equals(adapter.getItem(i))) {
                index = i;
                break;
            }
        }
        printerModelSpinner.setSelection(index);
    }

    private void persistPrinterModel() {
        prefs().edit().putString(PREF_PRINTER_MODEL, selectedPrinterModel()).apply();
    }

    private void savePrinterSettingsBackup() {
        JSONObject backup = new JSONObject();
        try {
            backup.put("printerModel", selectedPrinterModel());
            backup.put("bluetoothMode", selectedBluetoothMode());
            backup.put("catalogUrl", catalogUrlEdit == null ? "" : catalogUrlEdit.getText().toString().trim());
            backup.put("savedAt", System.currentTimeMillis());
        } catch (JSONException e) {
            Log.w(TAG, "Nao foi possivel montar backup de impressao", e);
        }
        prefs().edit().putString(PREF_PRINTER_SETTINGS_BACKUP_JSON, backup.toString()).apply();
    }

    private void restorePrinterSettingsBackup(boolean notifyUser) {
        String json = prefs().getString(PREF_PRINTER_SETTINGS_BACKUP_JSON, "");
        if (json == null || json.trim().isEmpty()) {
            if (notifyUser) {
                showError("Nao existe backup de preferencias salvo.");
            }
            return;
        }

        try {
            JSONObject backup = new JSONObject(json);
            String printerModel = backup.optString("printerModel", PRINTER_MODEL_AUTO);
            String bluetoothMode = backup.optString("bluetoothMode", PRINTER_MODEL_AUTO);
            String catalogUrl = backup.optString("catalogUrl", "");

            applySpinnerValue(printerModelSpinner, printerModel);
            applySpinnerValue(metodoSpinner, bluetoothMode);
            if (catalogUrlEdit != null && !catalogUrl.isEmpty()) {
                catalogUrlEdit.setText(catalogUrl);
            }

            prefs().edit()
                    .putString(PREF_PRINTER_MODEL, printerModel)
                    .putInt(PREF_PRINTER_FAILURE_COUNT, 0)
                    .apply();

            if (notifyUser) {
                setStatus("Backup de impressao restaurado.");
            }
        } catch (JSONException e) {
            if (notifyUser) {
                showError("Backup de preferencias invalido.");
            }
        }
    }

    private void applySpinnerValue(Spinner spinner, String value) {
        if (spinner == null || spinner.getAdapter() == null) {
            return;
        }
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item != null && value.equals(item.toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void recordPrintSuccess() {
        prefs().edit().putInt(PREF_PRINTER_FAILURE_COUNT, 0).apply();
        savePrinterSettingsBackup();
    }

    private void recordPrintFailure(Exception e) {
        int failures = prefs().getInt(PREF_PRINTER_FAILURE_COUNT, 0) + 1;
        prefs().edit().putInt(PREF_PRINTER_FAILURE_COUNT, failures).apply();
        if (failures >= PRINT_FAILURE_RESTORE_THRESHOLD) {
            restorePrinterSettingsBackup(false);
            prefs().edit().putInt(PREF_PRINTER_FAILURE_COUNT, 0).apply();
            runOnUiThread(() -> Toast.makeText(this, "Configuração de impressão restaurada após 10 falhas.", Toast.LENGTH_LONG).show());
        }
        Log.w(TAG, "Falha de impressao " + failures + "/" + PRINT_FAILURE_RESTORE_THRESHOLD, e);
    }

    private void refreshSupabaseConfigAsync() {
        if (BuildConfig.SUPABASE_URL != null && BuildConfig.SUPABASE_URL.trim().startsWith("http")) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastChecked = prefs().getLong(PREF_SUPABASE_CONFIG_CHECKED_AT, 0);
        if (now - lastChecked < SUPABASE_CONFIG_REFRESH_INTERVAL_MS) {
            return;
        }
        prefs().edit().putLong(PREF_SUPABASE_CONFIG_CHECKED_AT, now).apply();
        new Thread(() -> {
            try {
                JSONObject config = new JSONObject(downloadText(SUPABASE_CONFIG_URL));
                String url = config.optString("SUPABASE_URL", config.optString("supabaseUrl", "")).trim();
                String anonKey = config.optString("SUPABASE_ANON_KEY", config.optString("supabaseAnonKey", "")).trim();
                if (url.startsWith("http") && anonKey.length() > 20) {
                    prefs().edit()
                            .putString(PREF_SUPABASE_URL, url)
                            .putString(PREF_SUPABASE_ANON_KEY, anonKey)
                            .apply();
                    Log.i(TAG, "Configuracao Supabase remota carregada.");
                    syncDeviceRegistrationAsync();
                    syncPendingPrintHistoryAsync();
                }
            } catch (Exception e) {
                Log.w(TAG, "Nao foi possivel carregar configuracao Supabase remota", e);
            }
        }).start();
    }

    private void syncDeviceRegistrationAsync() {
        if (!isSupabaseConfigured()) {
            Log.i(TAG, "Supabase nao configurado; sincronizacao em nuvem desativada.");
            return;
        }
        new Thread(() -> {
            try {
                postSupabaseRpc(SUPABASE_RPC_UPSERT_DEVICE, buildDevicePayload());
            } catch (Exception e) {
                Log.w(TAG, "Falha ao registrar dispositivo no Supabase", e);
            }
        }).start();
    }

    private void syncPendingPrintHistoryAsync() {
        if (printSyncRunning) {
            return;
        }
        if (!isSupabaseConfigured()) {
            refreshSupabaseConfigAsync();
            return;
        }

        List<PrintHistoryEntry> snapshot = loadPendingPrintSyncs();
        if (snapshot.isEmpty()) {
            return;
        }

        printSyncRunning = true;
        new Thread(() -> {
            try {
                postSupabaseRpc(SUPABASE_RPC_UPSERT_DEVICE, buildDevicePayload());
                List<String> syncedEventIds = new ArrayList<>();
                for (PrintHistoryEntry pending : snapshot) {
                    try {
                        postSupabaseRpc(SUPABASE_RPC_RECORD_PRINT, buildPrintPayload(pending));
                        syncedEventIds.add(printEventId(pending));
                    } catch (Exception e) {
                        Log.w(TAG, "Falha ao sincronizar impressao pendente no Supabase", e);
                    }
                }
                List<PrintHistoryEntry> currentPending = loadPendingPrintSyncs();
                List<PrintHistoryEntry> remaining = new ArrayList<>();
                for (PrintHistoryEntry pending : currentPending) {
                    if (!syncedEventIds.contains(printEventId(pending))) {
                        remaining.add(pending);
                    }
                }
                savePendingPrintSyncs(remaining);
                if (!syncedEventIds.isEmpty() && remaining.isEmpty()) {
                    runOnUiThread(() -> setStatus("Relatorios de impressao enviados ao Supabase."));
                }
            } catch (Exception e) {
                Log.w(TAG, "Falha ao sincronizar impressao no Supabase", e);
            } finally {
                printSyncRunning = false;
            }
        }).start();
    }

    private void syncPadariaLotAsync(PeixariaEntry entry) {
        if (entry == null) {
            return;
        }
        if (!isSupabaseConfigured()) {
            refreshSupabaseConfigAsync();
            return;
        }
        new Thread(() -> {
            try {
                JSONObject payload = buildBaseTrackingPayload();
                payload.put("client_event_id", sha256(installationId() + "|padaria|" + entry.lot + "|" + entry.printedAt));
                payload.put("printed_at_ms", entry.printedAt);
                payload.put("product", entry.product);
                payload.put("copies", entry.copies);
                payload.put("expiry_at_ms", entry.expiryAt);
                payload.put("source", "android_padaria_lote");
                payload.put("company_name", currentEstablishmentName());
                JSONObject metadata = new JSONObject();
                metadata.put("lote", entry.lot);
                metadata.put("peso_kg", entry.weightKg);
                metadata.put("origem", currentPadariaAddress());
                metadata.put("tipo", "padaria");
                payload.put("metadata", metadata);
                postSupabaseRpc(SUPABASE_RPC_RECORD_PRINT, payload);
                runOnUiThread(() -> setStatus("Lote " + entry.lot + " salvo na nuvem."));
            } catch (Exception e) {
                Log.w(TAG, "Falha ao sincronizar lote da padaria no Supabase", e);
                runOnUiThread(() -> setStatus("Lote salvo no aparelho; nuvem pendente."));
            }
        }).start();
    }

    private void syncOcrCheckAsync(OcrScanResult result) {
        if (!isSupabaseConfigured() || result == null) {
            return;
        }
        new Thread(() -> {
            try {
                postSupabaseRpc(SUPABASE_RPC_UPSERT_DEVICE, buildDevicePayload());
                postSupabaseRpc(SUPABASE_RPC_RECORD_CHECK, buildCheckPayload(result));
            } catch (Exception e) {
                Log.w(TAG, "Falha ao sincronizar checagem OCR no Supabase", e);
            }
        }).start();
    }

    private void refreshCloudDashboard() {
        if (!isBetaUnlocked()) {
            showBetaLoginDialog();
            return;
        }
        if (!isSupabaseConfigured()) {
            setCloudLoadingText("Supabase ainda nao configurado neste aparelho.");
            refreshSupabaseConfigAsync();
            return;
        }

        setCloudLoadingText("Carregando dados da nuvem...");
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("days", 7);
                String response = postSupabaseRpcForText(SUPABASE_RPC_CLOUD_DASHBOARD, payload);
                JSONObject dashboard = new JSONObject(response);
                runOnUiThread(() -> renderCloudDashboard(dashboard));
            } catch (Exception e) {
                Log.w(TAG, "Falha ao carregar painel nuvem", e);
                runOnUiThread(() -> {
                    setCloudLoadingText("Nao foi possivel carregar a nuvem agora.");
                    setStatus("Falha ao carregar painel nuvem.");
                });
            }
        }).start();
    }

    private void setCloudLoadingText(String message) {
        if (cloudSummaryText != null) {
            cloudSummaryText.setText(message);
        }
        if (cloudExpiringText != null) {
            cloudExpiringText.setText("Aguardando dados.");
        }
        if (cloudDevicesText != null) {
            cloudDevicesText.setText("Aguardando dados.");
        }
        if (cloudRecentPrintsText != null) {
            cloudRecentPrintsText.setText("Aguardando dados.");
        }
        if (cloudRecentChecksText != null) {
            cloudRecentChecksText.setText("Aguardando dados.");
        }
    }

    private void renderCloudDashboard(JSONObject dashboard) {
        JSONObject summary = dashboard.optJSONObject("summary");
        if (cloudSummaryText != null) {
            cloudSummaryText.setText(formatCloudSummary(summary, dashboard.optString("generated_label", "")));
        }
        if (cloudExpiringText != null) {
            cloudExpiringText.setText(formatCloudExpiring(dashboard.optJSONArray("expiring")));
        }
        if (cloudDevicesText != null) {
            cloudDevicesText.setText(formatCloudDevices(dashboard.optJSONArray("devices")));
        }
        if (cloudRecentPrintsText != null) {
            cloudRecentPrintsText.setText(formatCloudRecentPrints(dashboard.optJSONArray("recent_prints")));
        }
        if (cloudRecentChecksText != null) {
            cloudRecentChecksText.setText(formatCloudRecentChecks(dashboard.optJSONArray("recent_checks")));
        }
        setStatus("Painel nuvem atualizado.");
    }

    private String formatCloudSummary(JSONObject summary, String generatedLabel) {
        if (summary == null) {
            return "Sem resumo da nuvem.";
        }
        return "Atualizado: " + emptyFallback(generatedLabel, "agora")
                + "\nAparelhos: " + summary.optInt("devices", 0)
                + " (" + summary.optInt("active_devices_24h", 0) + " ativos em 24h)"
                + "\nHoje: " + summary.optInt("prints_24h", 0) + " impressoes / "
                + labelCount(summary.optInt("labels_24h", 0))
                + "\nTotal: " + summary.optInt("prints_total", 0) + " impressoes / "
                + labelCount(summary.optInt("labels_total", 0))
                + "\nVencem em 24h: " + labelCount(summary.optInt("labels_24h_to_expire", 0))
                + "\nVencem em 7 dias: " + labelCount(summary.optInt("labels_7d_to_expire", 0))
                + "\nOCR hoje: " + summary.optInt("checks_24h", 0) + " checagens";
    }

    private String formatCloudExpiring(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "Nada vencendo nos proximos dias.";
        }
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(array.length(), 18);
        for (int i = 0; i < limit; i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            int hoursLeft = item.optInt("hours_left", 0);
            String urgency = hoursLeft < 0 ? "Vencido" : (hoursLeft < 24 ? "Ate 24h" : hoursLeft / 24 + " dias");
            builder.append(urgency)
                    .append(" - ")
                    .append(item.optString("product", "Produto"))
                    .append("\n")
                    .append(labelCount(item.optInt("labels", 0)))
                    .append(" - Val: ")
                    .append(emptyFallback(item.optString("expiry_label", ""), "sem data"))
                    .append("\n")
                    .append(emptyFallback(item.optString("device_label", ""), "Aparelho"))
                    .append(" - impresso ")
                    .append(emptyFallback(item.optString("printed_label", ""), "sem horario"));
        }
        if (array.length() > limit) {
            builder.append("\n\n+").append(array.length() - limit).append(" itens");
        }
        return builder.toString();
    }

    private String formatCloudDevices(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "Nenhum aparelho registrado.";
        }
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(array.length(), 12);
        for (int i = 0; i < limit; i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(emptyFallback(item.optString("device_label", ""), "Aparelho"))
                    .append("\n")
                    .append("Ultimo uso: ")
                    .append(emptyFallback(item.optString("last_seen_label", ""), "sem data"))
                    .append(" - ")
                    .append(item.optInt("prints", 0))
                    .append(" impressoes / ")
                    .append(labelCount(item.optInt("labels", 0)));

            String nextProduct = item.optString("next_product", "");
            String nextExpiry = item.optString("next_expiry_label", "");
            if (!nextProduct.trim().isEmpty() || !nextExpiry.trim().isEmpty()) {
                builder.append("\nProximo: ")
                        .append(emptyFallback(nextProduct, "Produto"))
                        .append(" - ")
                        .append(emptyFallback(nextExpiry, "sem data"));
            }

            int labels24h = item.optInt("labels_24h", 0);
            int labels7d = item.optInt("labels_7d", 0);
            if (labels24h > 0 || labels7d > 0) {
                builder.append("\nAtencao: ")
                        .append(labelCount(labels24h))
                        .append(" em 24h / ")
                        .append(labelCount(labels7d))
                        .append(" em 7d");
            }
        }
        return builder.toString();
    }

    private String formatCloudRecentPrints(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "Nenhuma impressao na nuvem ainda.";
        }
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(array.length(), 16);
        for (int i = 0; i < limit; i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(emptyFallback(item.optString("printed_label", ""), "sem hora"))
                    .append(" - ")
                    .append(emptyFallback(item.optString("product", ""), "Produto"))
                    .append("\n")
                    .append(labelCount(item.optInt("copies", 0)))
                    .append(" - Val: ")
                    .append(emptyFallback(item.optString("expiry_label", ""), "sem data"))
                    .append(item.optString("lote", "").trim().isEmpty() ? "" : "\nLote: " + item.optString("lote"))
                    .append(item.optString("peso_kg", "").trim().isEmpty() ? "" : " - " + item.optString("peso_kg") + " kg")
                    .append("\n")
                    .append(emptyFallback(item.optString("device_label", ""), "Aparelho"));
        }
        if (array.length() > limit) {
            builder.append("\n\n+").append(array.length() - limit).append(" registros");
        }
        return builder.toString();
    }

    private String formatCloudRecentChecks(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "Nenhuma checagem OCR na nuvem ainda.";
        }
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(array.length(), 12);
        for (int i = 0; i < limit; i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            String state = item.optBoolean("danger", false)
                    ? "Remover"
                    : (item.optBoolean("complete", false) ? "OK" : "Incompleto");
            builder.append(state)
                    .append(" - ")
                    .append(emptyFallback(item.optString("product", ""), "Produto OCR"))
                    .append("\n")
                    .append(emptyFallback(item.optString("checked_label", ""), "sem hora"))
                    .append(" - Val: ")
                    .append(emptyFallback(item.optString("expiry_label", ""), "sem data"))
                    .append("\n")
                    .append(emptyFallback(item.optString("device_label", ""), "Aparelho"));
            String status = item.optString("status", "");
            if (!status.trim().isEmpty()) {
                builder.append("\n").append(status);
            }
        }
        return builder.toString();
    }

    private String emptyFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private JSONObject buildDevicePayload() throws JSONException {
        JSONObject payload = buildBaseTrackingPayload();
        payload.put("android_id_hash", androidIdHash());
        payload.put("device_fingerprint", sha256(
                installationId()
                        + "|" + nullToEmpty(Build.MANUFACTURER)
                        + "|" + nullToEmpty(Build.BRAND)
                        + "|" + nullToEmpty(Build.MODEL)
                        + "|" + nullToEmpty(Build.DEVICE)
        ));
        payload.put("manufacturer", nullToEmpty(Build.MANUFACTURER));
        payload.put("brand", nullToEmpty(Build.BRAND));
        payload.put("model", nullToEmpty(Build.MODEL));
        payload.put("device_name", nullToEmpty(Build.DEVICE));
        payload.put("product_name", nullToEmpty(Build.PRODUCT));
        payload.put("android_release", nullToEmpty(Build.VERSION.RELEASE));
        payload.put("sdk_int", Build.VERSION.SDK_INT);
        payload.put("app_version_name", BuildConfig.VERSION_NAME);
        payload.put("app_version_code", BuildConfig.VERSION_CODE);
        payload.put("printer_name", lastPrinterName);
        payload.put("printer_address_hash", lastPrinterAddressHash);
        payload.put("printer_address_last4", lastPrinterAddressLast4);

        JSONObject metadata = new JSONObject();
        metadata.put("board", nullToEmpty(Build.BOARD));
        metadata.put("hardware", nullToEmpty(Build.HARDWARE));
        metadata.put("host", nullToEmpty(Build.HOST));
        metadata.put("id", nullToEmpty(Build.ID));
        metadata.put("mac_note", "Android nao expoe o MAC do aparelho; o app salva Android ID e endereco da impressora apenas como hash.");
        payload.put("metadata", metadata);
        return payload;
    }

    private JSONObject buildPrintPayload(PrintHistoryEntry entry) throws JSONException {
        JSONObject payload = buildBaseTrackingPayload();
        payload.put("client_event_id", printEventId(entry));
        payload.put("printed_at_ms", entry.printedAt);
        payload.put("product", entry.product);
        payload.put("copies", entry.copies);
        payload.put("start_at_ms", entry.startAt);
        payload.put("expiry_at_ms", entry.expiryAt);
        payload.put("validity_label", entry.validityLabel);
        payload.put("uses_hours", entry.usesHours);
        payload.put("printer_name", lastPrinterName);
        payload.put("printer_address_hash", lastPrinterAddressHash);
        payload.put("printer_address_last4", lastPrinterAddressLast4);
        payload.put("source", "android_validity_print");
        return payload;
    }

    private JSONObject buildCheckPayload(OcrScanResult result) throws JSONException {
        JSONObject payload = buildBaseTrackingPayload();
        payload.put("checked_at_ms", System.currentTimeMillis());
        payload.put("product", result.product);
        payload.put("start_at_ms", result.startAt);
        payload.put("expiry_at_ms", result.expiryAt);
        payload.put("status", result.status);
        payload.put("danger", result.danger);
        payload.put("complete", result.complete);
        payload.put("raw_text", result.rawText);
        payload.put("source", "android_ocr");
        return payload;
    }

    private JSONObject buildBaseTrackingPayload() throws JSONException {
        Locale locale = Locale.getDefault();
        JSONObject payload = new JSONObject();
        payload.put("app_install_id", installationId());
        payload.put("locale", locale.toLanguageTag());
        payload.put("timezone", TimeZone.getDefault().getID());
        payload.put("region_country", nullToEmpty(locale.getCountry()));
        payload.put("region_language", nullToEmpty(locale.getLanguage()));
        payload.put("app_version_name", BuildConfig.VERSION_NAME);
        payload.put("app_version_code", BuildConfig.VERSION_CODE);
        payload.put("establishment_slug", prefs().getString(PREF_ACTIVE_ESTABLISHMENT_SLUG, "padaria-lobo"));
        payload.put("establishment_password", prefs().getString(PREF_ACTIVE_ESTABLISHMENT_PASSWORD, BETA_PASSWORD));
        return payload;
    }

    private void postSupabaseRpc(String functionName, JSONObject payload) throws IOException, JSONException {
        postSupabaseRpcForText(functionName, payload);
    }

    private String postSupabaseRpcForText(String functionName, JSONObject payload) throws IOException, JSONException {
        URL url = new URL(supabaseBaseUrl() + "/rest/v1/rpc/" + functionName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        String anonKey = resolvedSupabaseAnonKey();
        connection.setRequestProperty("apikey", anonKey);
        if (anonKey.startsWith("eyJ")) {
            connection.setRequestProperty("Authorization", "Bearer " + anonKey);
        }
        connection.setRequestProperty("User-Agent", "ValidadePT260/" + BuildConfig.VERSION_NAME);

        JSONObject body = new JSONObject();
        body.put("payload", payload);
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(bytes);
        }

        int code = connection.getResponseCode();
        try {
            if (code < 200 || code >= 300) {
                InputStream errorStream = connection.getErrorStream();
                String errorBody = errorStream == null ? "" : readAll(errorStream);
                throw new IOException("Supabase RPC " + functionName + " HTTP " + code + " " + errorBody);
            }
            try (InputStream input = connection.getInputStream()) {
                return readAll(input);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String installationId() {
        SharedPreferences preferences = prefs();
        String existing = preferences.getString(PREF_INSTALLATION_ID, "");
        if (existing != null && !existing.trim().isEmpty()) {
            return existing.trim();
        }
        String created = UUID.randomUUID().toString();
        preferences.edit().putString(PREF_INSTALLATION_ID, created).apply();
        return created;
    }

    private String androidIdHash() {
        try {
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            return sha256(nullToEmpty(androidId));
        } catch (Exception e) {
            return "";
        }
    }

    private String sha256(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format(Locale.US, "%02x", item & 0xFF));
            }
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void recordSuccessfulValidityPrint(String produto, int copies, Date start, Date expiry, String validityLabel, boolean usesHours) {
        String productName = produto == null || produto.trim().isEmpty() ? firstProduct() : produto.trim();
        int safeCopies = Math.max(1, Math.min(99, copies));
        PrintHistoryEntry entry = new PrintHistoryEntry(
                System.currentTimeMillis(),
                productName,
                safeCopies,
                start.getTime(),
                expiry.getTime(),
                validityLabel == null ? "" : validityLabel,
                usesHours
        );

        List<PrintHistoryEntry> entries = loadPrintHistory();
        entries.add(0, entry);
        savePrintHistory(entries);
        enqueuePendingPrintSync(entry);
        syncPendingPrintHistoryAsync();

        runOnUiThread(() -> {
            refreshHistoryPage();
            setStatus("Impressao registrada: " + labelCount(safeCopies) + " de " + productName + ".");
            maybeShowExpiryReminder(true);
        });
    }

    private List<PrintHistoryEntry> loadPrintHistory() {
        return loadPrintHistoryFromJson(prefs().getString(PREF_PRINT_HISTORY_JSON, "[]"));
    }

    private List<PrintHistoryEntry> loadPendingPrintSyncs() {
        return loadPrintHistoryFromJson(prefs().getString(PREF_PENDING_PRINT_SYNC_JSON, "[]"));
    }

    private List<PrintHistoryEntry> loadPrintHistoryFromJson(String json) {
        List<PrintHistoryEntry> entries = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json == null ? "[]" : json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                String product = object.optString("product", "").trim();
                if (product.isEmpty()) {
                    continue;
                }
                entries.add(new PrintHistoryEntry(
                        object.optLong("printedAt", 0),
                        product,
                        Math.max(1, object.optInt("copies", 1)),
                        object.optLong("startAt", 0),
                        object.optLong("expiryAt", 0),
                        object.optString("validityLabel", ""),
                        object.optBoolean("usesHours", false)
                ));
            }
        } catch (JSONException ignored) {
            return entries;
        }

        entries.sort((left, right) -> Long.compare(right.printedAt, left.printedAt));
        return entries;
    }

    private void savePrintHistory(List<PrintHistoryEntry> entries) {
        prefs().edit().putString(PREF_PRINT_HISTORY_JSON, printHistoryToJson(entries)).apply();
    }

    private void savePendingPrintSyncs(List<PrintHistoryEntry> entries) {
        prefs().edit().putString(PREF_PENDING_PRINT_SYNC_JSON, printHistoryToJson(entries)).apply();
    }

    private String printHistoryToJson(List<PrintHistoryEntry> entries) {
        JSONArray array = new JSONArray();
        int limit = Math.min(entries.size(), MAX_PRINT_HISTORY);
        for (int i = 0; i < limit; i++) {
            PrintHistoryEntry entry = entries.get(i);
            JSONObject object = new JSONObject();
            try {
                object.put("printedAt", entry.printedAt);
                object.put("product", entry.product);
                object.put("copies", entry.copies);
                object.put("startAt", entry.startAt);
                object.put("expiryAt", entry.expiryAt);
                object.put("validityLabel", entry.validityLabel);
                object.put("usesHours", entry.usesHours);
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        return array.toString();
    }

    private void enqueuePendingPrintSync(PrintHistoryEntry entry) {
        if (entry == null) {
            return;
        }

        List<PrintHistoryEntry> pending = loadPendingPrintSyncs();
        String newEventId = printEventId(entry);
        List<PrintHistoryEntry> deduped = new ArrayList<>();
        deduped.add(entry);
        for (PrintHistoryEntry item : pending) {
            if (!newEventId.equals(printEventId(item))) {
                deduped.add(item);
            }
        }
        savePendingPrintSyncs(deduped);
    }

    private String printEventId(PrintHistoryEntry entry) {
        if (entry == null) {
            return "";
        }
        return sha256(installationId()
                + "|" + entry.printedAt
                + "|" + cleanPrinterText(entry.product).toLowerCase(Locale.ROOT)
                + "|" + entry.copies
                + "|" + entry.startAt
                + "|" + entry.expiryAt
                + "|" + entry.validityLabel
                + "|" + entry.usesHours);
    }

    private void refreshHistoryPage() {
        List<PrintHistoryEntry> entries = loadPrintHistory();
        if (historySummaryText != null) {
            historySummaryText.setText(buildHistorySummaryText(alertSummariesForToday(entries)));
        }
        if (historyListText != null) {
            historyListText.setText(buildHistoryListText(entries));
        }
    }

    private String buildHistorySummaryText(List<PrintSummary> summaries) {
        if (summaries.isEmpty()) {
            return "Hoje nao ha produto com mais de " + PRINT_ALERT_THRESHOLD + " etiquetas vencendo no historico.";
        }

        StringBuilder builder = new StringBuilder("Recolher hoje:\n");
        int limit = Math.min(6, summaries.size());
        for (int i = 0; i < limit; i++) {
            PrintSummary summary = summaries.get(i);
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(summary.product)
                    .append(" - ")
                    .append(labelCount(summary.totalCopies))
                    .append(" - Val: ")
                    .append(summary.validityLabel.isEmpty() ? "hoje" : summary.validityLabel);
        }
        if (summaries.size() > limit) {
            builder.append("\n+").append(summaries.size() - limit).append(" produtos");
        }
        return builder.toString();
    }

    private String buildHistoryListText(List<PrintHistoryEntry> entries) {
        if (entries.isEmpty()) {
            return "Nenhuma impressao registrada ainda.";
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(40, entries.size());
        for (int i = 0; i < limit; i++) {
            PrintHistoryEntry entry = entries.get(i);
            if (i > 0) {
                builder.append("\n\n");
            }
            builder.append(shortDateTimeFormat.format(new Date(entry.printedAt)))
                    .append(" - ")
                    .append(entry.product)
                    .append("\n")
                    .append(labelCount(entry.copies))
                    .append(" - Val: ")
                    .append(displayValidity(entry));
        }
        if (entries.size() > limit) {
            builder.append("\n\n+").append(entries.size() - limit).append(" registros antigos");
        }
        return builder.toString();
    }

    private String displayValidity(PrintHistoryEntry entry) {
        if (entry.validityLabel != null && !entry.validityLabel.trim().isEmpty()) {
            return entry.validityLabel;
        }
        Date expiry = new Date(entry.expiryAt);
        return entry.usesHours ? dateTimeFormat.format(expiry) : dateFormat.format(expiry);
    }

    private void confirmClearPrintHistory() {
        if (loadPrintHistory().isEmpty()) {
            setStatus("Historico ja esta vazio.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Limpar historico")
                .setMessage("Apagar os registros de impressoes salvos neste celular?")
                .setPositiveButton("Limpar", (dialog, which) -> clearPrintHistory())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void clearPrintHistory() {
        prefs().edit()
                .remove(PREF_PRINT_HISTORY_JSON)
                .remove(PREF_LAST_REMINDER_SIGNATURE)
                .apply();
        refreshHistoryPage();
        refreshAlertsPage();
        setStatus("Historico limpo.");
    }

    private void refreshAlertsPage() {
        List<OcrAlertEntry> ocrAlerts = loadOcrAlerts();
        List<PrintSummary> printSummaries = alertSummariesForToday(loadPrintHistory());
        if (alertsSummaryText != null) {
            alertsSummaryText.setText(buildAlertsSummaryText(ocrAlerts, printSummaries));
        }
        if (alertsListText != null) {
            alertsListText.setText(buildOcrAlertsListText(ocrAlerts));
        }
    }

    private String buildAlertsSummaryText(List<OcrAlertEntry> ocrAlerts, List<PrintSummary> printSummaries) {
        int todayOcr = 0;
        int expired = 0;
        long now = System.currentTimeMillis();
        for (OcrAlertEntry entry : ocrAlerts) {
            if (isSameDay(entry.scannedAt, now)) {
                todayOcr++;
                if (entry.expiryAt < startOfTodayMillis()) {
                    expired++;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Hoje voce recolheu ")
                .append(todayOcr)
                .append(todayOcr == 1 ? " produto pelo OCR" : " produtos pelo OCR")
                .append(" que venciam hoje ou ja estavam vencidos.");
        if (expired > 0) {
            builder.append("\nVencidos detectados: ").append(expired).append('.');
        }

        if (printSummaries.isEmpty()) {
            builder.append("\nImpressoes: sem grupo acima de ")
                    .append(PRINT_ALERT_THRESHOLD)
                    .append(" etiquetas vencendo hoje.");
        } else {
            builder.append("\nImpressoes para recolher: ");
            int totalLabels = 0;
            for (PrintSummary summary : printSummaries) {
                totalLabels += summary.totalCopies;
            }
            builder.append(printSummaries.size())
                    .append(printSummaries.size() == 1 ? " produto, " : " produtos, ")
                    .append(labelCount(totalLabels))
                    .append('.');
        }
        return builder.toString();
    }

    private String buildOcrAlertsListText(List<OcrAlertEntry> entries) {
        if (entries.isEmpty()) {
            return "Nenhum recolhimento OCR registrado ainda.";
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(50, entries.size());
        long todayStart = startOfTodayMillis();
        for (int i = 0; i < limit; i++) {
            OcrAlertEntry entry = entries.get(i);
            if (i > 0) {
                builder.append("\n\n");
            }
            builder.append(shortDateTimeFormat.format(new Date(entry.scannedAt)))
                    .append(" - ")
                    .append(entry.product)
                    .append("\nInicio: ")
                    .append(entry.startAt > 0 ? dateFormat.format(new Date(entry.startAt)) : "nao salvo")
                    .append("\nVal: ")
                    .append(dateFormat.format(new Date(entry.expiryAt)))
                    .append(" - ")
                    .append(entry.expiryAt < todayStart ? "vencido" : "vence hoje");
        }
        if (entries.size() > limit) {
            builder.append("\n\n+").append(entries.size() - limit).append(" alertas antigos");
        }
        return builder.toString();
    }

    private void confirmClearOcrAlerts() {
        if (loadOcrAlerts().isEmpty()) {
            setStatus("Avisos OCR ja estao vazios.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Limpar avisos OCR")
                .setMessage("Apagar os recolhimentos OCR salvos neste celular?")
                .setPositiveButton("Limpar", (dialog, which) -> clearOcrAlerts())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void clearOcrAlerts() {
        prefs().edit().remove(PREF_OCR_ALERTS_JSON).apply();
        refreshAlertsPage();
        setStatus("Avisos OCR limpos.");
    }

    private List<OcrAlertEntry> loadOcrAlerts() {
        List<OcrAlertEntry> entries = new ArrayList<>();
        String json = prefs().getString(PREF_OCR_ALERTS_JSON, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                long scannedAt = object.optLong("scannedAt", 0);
                long startAt = object.optLong("startAt", 0);
                long expiryAt = object.optLong("expiryAt", 0);
                String product = object.optString("product", "").trim();
                String rawText = object.optString("rawText", "");
                if (scannedAt <= 0 || expiryAt <= 0) {
                    continue;
                }
                entries.add(new OcrAlertEntry(
                        scannedAt,
                        product.isEmpty() ? "Produto OCR" : product,
                        startAt,
                        expiryAt,
                        rawText
                ));
            }
        } catch (JSONException ignored) {
            return entries;
        }

        entries.sort((left, right) -> Long.compare(right.scannedAt, left.scannedAt));
        return entries;
    }

    private void saveOcrAlerts(List<OcrAlertEntry> entries) {
        JSONArray array = new JSONArray();
        int limit = Math.min(entries.size(), MAX_OCR_ALERTS);
        for (int i = 0; i < limit; i++) {
            OcrAlertEntry entry = entries.get(i);
            JSONObject object = new JSONObject();
            try {
                object.put("scannedAt", entry.scannedAt);
                object.put("product", entry.product);
                object.put("startAt", entry.startAt);
                object.put("expiryAt", entry.expiryAt);
                object.put("rawText", entry.rawText);
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        prefs().edit().putString(PREF_OCR_ALERTS_JSON, array.toString()).apply();
    }

    private void recordOcrAlert(OcrScanResult result) {
        OcrAlertEntry entry = new OcrAlertEntry(
                System.currentTimeMillis(),
                result.product,
                result.startAt,
                result.expiryAt,
                result.rawText
        );
        List<OcrAlertEntry> entries = loadOcrAlerts();
        entries.add(0, entry);
        saveOcrAlerts(entries);
        refreshAlertsPage();
        maybeShowOcrAlertNotification(true);
    }

    private List<PrintSummary> alertSummariesForToday(List<PrintHistoryEntry> entries) {
        List<PrintSummary> summaries = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (PrintHistoryEntry entry : entries) {
            if (entry.expiryAt <= 0 || !isSameDay(entry.expiryAt, now)) {
                continue;
            }

            PrintSummary summary = null;
            for (PrintSummary item : summaries) {
                if (item.product.equalsIgnoreCase(entry.product)) {
                    summary = item;
                    break;
                }
            }

            if (summary == null) {
                summary = new PrintSummary(entry.product, entry.expiryAt, entry.validityLabel);
                summaries.add(summary);
            }

            summary.totalCopies += Math.max(1, entry.copies);
            summary.latestPrintedAt = Math.max(summary.latestPrintedAt, entry.printedAt);
            if (summary.validityLabel.isEmpty() && entry.validityLabel != null) {
                summary.validityLabel = entry.validityLabel;
            }
        }

        for (int i = summaries.size() - 1; i >= 0; i--) {
            if (summaries.get(i).totalCopies <= PRINT_ALERT_THRESHOLD) {
                summaries.remove(i);
            }
        }
        summaries.sort((left, right) -> Integer.compare(right.totalCopies, left.totalCopies));
        return summaries;
    }

    private boolean isSameDay(long leftMillis, long rightMillis) {
        Calendar left = Calendar.getInstance();
        left.setTimeInMillis(leftMillis);
        Calendar right = Calendar.getInstance();
        right.setTimeInMillis(rightMillis);
        return left.get(Calendar.YEAR) == right.get(Calendar.YEAR)
                && left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR);
    }

    private long startOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String labelCount(int copies) {
        return copies == 1 ? "1 etiqueta" : copies + " etiquetas";
    }

    private Date resolvePrintedExpiry(Date calculatedExpiry, String manualValidity) {
        String manual = cleanPrinterText(manualValidity == null ? "" : manualValidity).trim();
        if (manual.isEmpty()) {
            return calculatedExpiry;
        }

        Date parsed = tryParsePrintDate(manual);
        return parsed == null ? calculatedExpiry : parsed;
    }

    private void maybeShowExpiryReminder(boolean requestPermissionIfNeeded) {
        List<PrintSummary> summaries = alertSummariesForToday(loadPrintHistory());
        if (summaries.isEmpty()) {
            return;
        }

        String signature = buildReminderSignature(summaries);
        if (signature.equals(prefs().getString(PREF_LAST_REMINDER_SIGNATURE, ""))) {
            return;
        }

        if (!canPostNotifications(requestPermissionIfNeeded)) {
            return;
        }

        showExpiryReminderNotification(summaries);
        prefs().edit().putString(PREF_LAST_REMINDER_SIGNATURE, signature).apply();
    }

    private String buildReminderSignature(List<PrintSummary> summaries) {
        List<String> products = new ArrayList<>();
        for (PrintSummary summary : summaries) {
            products.add(cleanPrinterText(summary.product).toLowerCase(Locale.ROOT));
        }
        products.sort(String::compareTo);
        StringBuilder builder = new StringBuilder(dayKey(System.currentTimeMillis())).append('|');
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(products.get(i));
        }
        return builder.toString();
    }

    private String dayKey(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return String.format(
                Locale.ROOT,
                "%04d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private boolean canPostNotifications(boolean requestPermissionIfNeeded) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (requestPermissionIfNeeded) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
            setStatus("Permissao de notificacao pendente.");
        }
        return false;
    }

    private void requestCatalogNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
        }
    }

    private void maybeShowCatalogUpdateNotification(List<CatalogSyncJobService.CatalogAddition> additions, boolean requestPermissionIfNeeded) {
        if (additions == null || additions.isEmpty() || !canPostNotifications(requestPermissionIfNeeded)) {
            return;
        }

        CatalogSyncJobService.showCatalogUpdateNotification(this, additions);
    }

    private void showExpiryReminderNotification(List<PrintSummary> summaries) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Alertas de validade",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Avisos de produtos impressos que vencem hoje.");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_OPEN_ALERTS, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_EXPIRY_REMINDER_ID, intent, flags);

        String title = summaries.size() == 1 ? "Produto para recolher hoje" : "Produtos para recolher hoje";
        String content = buildReminderContentText(summaries);
        String bigText = buildReminderBigText(summaries);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_printer_label)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new Notification.BigTextStyle().bigText(bigText))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(COLOR_PRIMARY);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }

        manager.notify(NOTIFICATION_EXPIRY_REMINDER_ID, builder.build());
    }

    private void maybeShowOcrAlertNotification(boolean requestPermissionIfNeeded) {
        List<OcrAlertEntry> entries = loadOcrAlerts();
        int todayCount = 0;
        int expiredCount = 0;
        long now = System.currentTimeMillis();
        long todayStart = startOfTodayMillis();
        for (OcrAlertEntry entry : entries) {
            if (!isSameDay(entry.scannedAt, now)) {
                continue;
            }
            todayCount++;
            if (entry.expiryAt < todayStart) {
                expiredCount++;
            }
        }
        if (todayCount == 0 || !canPostNotifications(requestPermissionIfNeeded)) {
            return;
        }

        showOcrAlertNotification(entries, todayCount, expiredCount);
    }

    private void showOcrAlertNotification(List<OcrAlertEntry> entries, int todayCount, int expiredCount) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_OCR_CHANNEL_ID,
                    "Alertas OCR de validade",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Avisos imediatos de produtos vencidos ou vencendo hoje encontrados pelo OCR.");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_OPEN_ALERTS, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_OCR_ALERT_ID, intent, flags);

        int dueToday = Math.max(0, todayCount - expiredCount);
        String title = expiredCount > 0 ? "Produto vencido detectado" : "Produto vence hoje";
        String content = expiredCount > 0
                ? "Voce encontrou " + expiredCount + (expiredCount == 1 ? " produto vencido hoje." : " produtos vencidos hoje.")
                : dueToday + (dueToday == 1 ? " produto vence hoje." : " produtos vencem hoje.");
        if (expiredCount > 0 && dueToday > 0) {
            content += " Mais " + dueToday + (dueToday == 1 ? " vence hoje." : " vencem hoje.");
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, NOTIFICATION_OCR_CHANNEL_ID)
                : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_printer_label)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new Notification.BigTextStyle().bigText(buildOcrNotificationBigText(entries, todayCount, expiredCount)))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(expiredCount > 0 ? 0xFFB91C1C : COLOR_PRIMARY);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        manager.notify(NOTIFICATION_OCR_ALERT_ID, builder.build());
    }

    private String buildOcrNotificationBigText(List<OcrAlertEntry> entries, int todayCount, int expiredCount) {
        int dueToday = Math.max(0, todayCount - expiredCount);
        StringBuilder builder = new StringBuilder();
        if (expiredCount > 0) {
            builder.append("Voce encontrou ")
                    .append(expiredCount)
                    .append(expiredCount == 1 ? " produto vencido hoje." : " produtos vencidos hoje.");
        }
        if (dueToday > 0) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(dueToday)
                    .append(dueToday == 1 ? " produto vence hoje." : " produtos vencem hoje.");
        }

        int added = 0;
        long now = System.currentTimeMillis();
        long todayStart = startOfTodayMillis();
        for (OcrAlertEntry entry : entries) {
            if (!isSameDay(entry.scannedAt, now)) {
                continue;
            }
            if (added >= 4) {
                break;
            }
            builder.append('\n')
                    .append(entry.product)
                    .append(" - ")
                    .append(entry.expiryAt < todayStart ? "vencido" : "vence hoje")
                    .append(" - Val: ")
                    .append(dateFormat.format(new Date(entry.expiryAt)));
            added++;
        }
        builder.append("\nToque para abrir Avisos.");
        return builder.toString();
    }

    private String buildReminderContentText(List<PrintSummary> summaries) {
        PrintSummary first = summaries.get(0);
        if (summaries.size() == 1) {
            return first.product + ": " + labelCount(first.totalCopies) + " vencem hoje.";
        }
        return summaries.size() + " produtos vencem hoje. Primeiro: " + first.product + " (" + first.totalCopies + ").";
    }

    private String buildReminderBigText(List<PrintSummary> summaries) {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(5, summaries.size());
        for (int i = 0; i < limit; i++) {
            PrintSummary summary = summaries.get(i);
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(summary.product)
                    .append(": ")
                    .append(labelCount(summary.totalCopies))
                    .append(" vencem hoje");
        }
        if (summaries.size() > limit) {
            builder.append("\n+").append(summaries.size() - limit).append(" produtos no historico");
        }
        builder.append("\nToque para ver os avisos.");
        return builder.toString();
    }

    private void handleOpenAlertsIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_ALERTS, false)) {
            return;
        }
        intent.removeExtra(EXTRA_OPEN_ALERTS);
        if (alertsPage != null) {
            showPage("alerts");
            setStatus("Avisos abertos pelo alerta.");
        }
    }

    private void registerScreenReminderReceiver() {
        if (screenReceiver != null) {
            return;
        }

        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent == null ? "" : intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)
                        || Intent.ACTION_SCREEN_OFF.equals(action)
                        || Intent.ACTION_USER_PRESENT.equals(action)) {
                    maybeShowExpiryReminder(false);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);
    }

    private void pickLabelImage() {
        openImagePicker(REQUEST_PRICE_IMAGE);
    }

    private void pickValidityImage() {
        openImagePicker(REQUEST_VALIDITY_IMAGE);
    }

    private void pickOnlyPriceImage() {
        openImagePicker(REQUEST_ONLY_PRICE_IMAGE);
    }

    private void pickImageOnlyImage() {
        openImagePicker(REQUEST_IMAGE_ONLY_IMAGE);
    }

    private void pickPhoto50x30Image() {
        openImagePicker(REQUEST_PHOTO_50X30_IMAGE);
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, requestCode);
    }

    private void clearLabelImage() {
        labelImageUri = null;
        if (imageStatusText != null) {
            imageStatusText.setText("Sem imagem na etiqueta.");
        }
        setStatus("Imagem removida da etiqueta.");
    }

    private void clearValidityImage() {
        validityImageUri = null;
        if (validityImageStatusText != null) {
            validityImageStatusText.setText("Sem imagem na etiqueta de validade.");
        }
        setStatus("Imagem removida da validade.");
    }

    private void clearOnlyPriceImage() {
        onlyPriceImageUri = null;
        if (onlyPriceImageStatusText != null) {
            onlyPriceImageStatusText.setText("Sem imagem na etiqueta so preco.");
        }
        setStatus("Imagem removida da etiqueta so preco.");
    }

    private void clearImageOnlyImage() {
        imageOnlyUri = null;
        refreshImageOnlyPreview();
        setStatus("Imagem removida da etiqueta so imagem.");
    }

    private void clearPhoto50x30Image() {
        photo50x30Uri = null;
        refreshPhoto50x30Preview();
        setStatus("Imagem removida da aba Foto 50x30.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_PRICE_IMAGE
                || requestCode == REQUEST_VALIDITY_IMAGE
                || requestCode == REQUEST_ONLY_PRICE_IMAGE
                || requestCode == REQUEST_IMAGE_ONLY_IMAGE
                || requestCode == REQUEST_PHOTO_50X30_IMAGE)
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            Uri selectedUri = data.getData();
            try {
                int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(selectedUri, flags);
            } catch (Exception ignored) {
            }
            if (requestCode == REQUEST_PRICE_IMAGE) {
                labelImageUri = selectedUri;
                if (imageStatusText != null) {
                    imageStatusText.setText("Imagem selecionada para o lado esquerdo.");
                }
                setStatus("Imagem pronta para a proxima etiqueta de preco.");
            } else {
                if (requestCode == REQUEST_VALIDITY_IMAGE) {
                    validityImageUri = selectedUri;
                    if (validityImageStatusText != null) {
                        validityImageStatusText.setText("Imagem selecionada para o lado esquerdo.");
                    }
                    setStatus("Imagem pronta para a proxima etiqueta de validade.");
                } else if (requestCode == REQUEST_ONLY_PRICE_IMAGE) {
                    onlyPriceImageUri = selectedUri;
                    if (onlyPriceImageStatusText != null) {
                        onlyPriceImageStatusText.setText("Imagem selecionada para o lado esquerdo.");
                    }
                    setStatus("Imagem pronta para a proxima etiqueta so preco.");
                } else if (requestCode == REQUEST_IMAGE_ONLY_IMAGE) {
                    imageOnlyUri = selectedUri;
                    refreshImageOnlyPreview();
                    setStatus("Imagem pronta para etiqueta so imagem.");
                } else {
                    photo50x30Uri = selectedUri;
                    refreshPhoto50x30Preview();
                    setStatus("Imagem pronta para a aba Foto 50x30.");
                }
            }
        }
    }

    private int selectedFontMode() {
        return selectedFontMode(fontSizeSpinner);
    }

    private int selectedFontMode(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItemPosition() < 0) {
            return 0;
        }
        return spinner.getSelectedItemPosition();
    }

    private LinearLayout field(String label, android.view.View input) {
        var box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(5), 0, dp(8));

        var text = new TextView(this);
        text.setText(label);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        text.setTextColor(COLOR_MUTED);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(text, fullWidth(-2));
        Object tag = input.getTag();
        box.addView(input, fullWidth("tall".equals(tag) ? dp(112) : dp(48)));
        return box;
    }

    private LinearLayout.LayoutParams fullWidth(int height) {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void updateDates() {
        try {
            Date startDate = getStartDate();
            ValidityRule rule = resolveRule(produtoEdit == null ? "" : produtoEdit.getText().toString());
            Date validade = addRule(startDate, rule);
            if (validadeEdit != null) {
                String formatted = rule.usesHours() ? dateTimeFormat.format(validade) : dateFormat.format(validade);
                validadeEdit.setText(formatted + " (" + rule.displayDuration() + ")");
            }
        } catch (ParseException ignored) {
            if (validadeEdit != null) {
                validadeEdit.setText("");
            }
        }
    }

    private Date getStartDate() throws ParseException {
        if (dataEdit == null || dataEdit.getText().toString().trim().isEmpty()) {
            Date now = new Date();
            if (dataEdit != null) {
                dataEdit.setText(dateTimeFormat.format(now));
            }
            return now;
        }
        String value = dataEdit.getText().toString().trim();
        if (value.length() <= 10) {
            return dateFormat.parse(value);
        }
        return dateTimeFormat.parse(value);
    }

    private void openDatePicker() {
        try {
            Date date = getStartDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                        new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    selected.set(Calendar.MINUTE, minute);
                                    dataEdit.setText(dateTimeFormat.format(selected.getTime()));
                                    updateDates();
                                },
                                selected.get(Calendar.HOUR_OF_DAY),
                                selected.get(Calendar.MINUTE),
                                true
                        ).show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        } catch (ParseException e) {
            Toast.makeText(this, "Data invalida", Toast.LENGTH_SHORT).show();
        }
    }

    private String nextPeixariaLotPreview() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int savedYear = prefs().getInt(PREF_PEIXARIA_LOTE_YEAR, currentYear);
        int sequence = savedYear == currentYear ? prefs().getInt(PREF_PEIXARIA_LOTE_SEQUENCE, 0) + 1 : 1;
        return String.format(Locale.US, "%03d/%d", Math.max(1, sequence), currentYear);
    }

    // Reserve and persist the next lot for the current year, returning the reserved lot string.
    private String reserveNextPeixariaLot() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int savedYear = prefs().getInt(PREF_PEIXARIA_LOTE_YEAR, currentYear);
        int nextSeq = savedYear == currentYear ? prefs().getInt(PREF_PEIXARIA_LOTE_SEQUENCE, 0) + 1 : 1;
        prefs().edit()
                .putInt(PREF_PEIXARIA_LOTE_YEAR, currentYear)
                .putInt(PREF_PEIXARIA_LOTE_SEQUENCE, nextSeq)
                .apply();
        return String.format(Locale.US, "%03d/%d", Math.max(1, nextSeq), currentYear);
    }

    private PeixariaEntry createPeixariaEntry(String product, String weight, int copies) {
        long printedAt = System.currentTimeMillis();
        return new PeixariaEntry(printedAt, nextPeixariaLotPreview(), product, weight, copies, peixariaExpiryAtFor(product, printedAt));
    }

    private void commitPeixariaLot(PeixariaEntry entry) {
        if (entry == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int slash = entry.lot.indexOf('/');
        int sequence = slash > 0 ? safeParseInt(entry.lot.substring(0, slash), 1) : 1;
        prefs().edit()
                .putInt(PREF_PEIXARIA_LOTE_YEAR, currentYear)
                .putInt(PREF_PEIXARIA_LOTE_SEQUENCE, sequence)
                .apply();
        List<PeixariaEntry> entries = loadPeixariaHistory();
        entries.add(0, entry);
        entries.sort((left, right) -> Long.compare(right.printedAt, left.printedAt));
        savePeixariaHistory(entries);
        refreshPeixariaPreview();
        refreshPeixariaPanel();
    }

    private int safeParseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private List<String> loadPeixariaProductCatalog() {
        List<String> products = new ArrayList<>(Arrays.asList(
                "Pao frances",
                "Pao de sal",
                "Pao de leite",
                "Pao integral",
                "Pao de forma",
                "Pao de queijo",
                "Pao doce",
                "Bisnaga",
                "Sonho",
                "Rosca",
                "Bolo simples",
                "Bolo de chocolate",
                "Salgado assado"
        ));

        File file = new File(getFilesDir(), PEIXARIA_CATALOG_FILE);
        try {
            if (file.exists()) {
                String json = readAll(new FileInputStream(file));
                JSONObject obj = new JSONObject(json);
                JSONArray items = obj.optJSONArray("products");
                if (items != null) {
                    products.clear();
                    for (int i = 0; i < items.length(); i++) {
                        String value = items.optString(i, "").trim();
                        if (!value.isEmpty() && !products.contains(value)) {
                            products.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return products;
    }

    private void persistPeixariaProductCatalog(List<String> products) {
        try {
            File file = new File(getFilesDir(), PEIXARIA_CATALOG_FILE);
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            for (String product : products) {
                String value = product == null ? "" : product.trim();
                if (!value.isEmpty() && !value.equals("Outros")) {
                    array.put(value);
                }
            }
            object.put("version", 1);
            object.put("products", array);
            try (OutputStream output = new FileOutputStream(file)) {
                output.write(object.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            showError("Nao foi possivel salvar o catalogo de peixes: " + (e.getMessage() == null ? e.toString() : e.getMessage()));
        }
    }

    private boolean isKnownPeixariaProduct(String product) {
        if (product == null) {
            return false;
        }
        for (String item : loadPeixariaProductCatalog()) {
            if (item.equalsIgnoreCase(product.trim())) {
                return true;
            }
        }
        return false;
    }

    private void showAddPeixariaProductDialog() {
        var input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Nome do produto de padaria");
        styleInput(input);
        new AlertDialog.Builder(this)
                .setTitle("Novo produto de padaria")
                .setMessage("Adicione um produto que ainda nao esteja no catalogo da rastreabilidade.")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String product = input.getText().toString().trim();
                    if (product.isEmpty()) {
                        showError("Digite o nome do produto.");
                        return;
                    }
                    List<String> products = loadPeixariaProductCatalog();
                    for (String existing : products) {
                        if (existing.equalsIgnoreCase(product)) {
                            setStatus("Produto ja existe no catalogo da padaria.");
                            return;
                        }
                    }
                    products.add(product);
                    persistPeixariaProductCatalog(products);
                    setStatus("Produto salvo: " + product);
                    if (peixariaProductSpinner != null && peixariaProductEdit != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, products);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        peixariaProductSpinner.setAdapter(adapter);
                        peixariaProductEdit.setText(product);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void refreshPeixariaPreview() {
        savePadariaAddress();
        if (peixariaPreviewText != null) {
            String product = peixariaProductEdit == null ? "" : peixariaProductEdit.getText().toString().trim();
            peixariaPreviewText.setText("Proximo lote: " + nextPeixariaLotPreview()
                    + "\nOrigem: Padaria Lobo"
                    + "\nEndereco de origem: " + currentPadariaAddress()
                    + "\nRecebimento e processamento: hoje | Validade: " + peixariaValiditySummary(product));
        }
    }

    private String peixariaValiditySummary(String product) {
        return peixariaValidityDaysForProduct(product) + " dias";
    }

    private int peixariaValidityDaysForProduct(String product) {
        String cleanProduct = cleanPrinterText(product == null ? "" : product).toLowerCase(Locale.ROOT);
        if (cleanProduct.contains("pao frances") || cleanProduct.contains("frances")) {
            return 2;
        }
        if (cleanProduct.contains("pao de sal") || cleanProduct.contains("pao sal") || cleanProduct.contains("salgado") || cleanProduct.contains("salgados")) {
            return 3;
        }
        if (cleanProduct.contains("pao de leite") || cleanProduct.contains("pao leite") || cleanProduct.contains("doce") || cleanProduct.contains("sonho") || cleanProduct.contains("rosca")) {
            return 4;
        }
        if (cleanProduct.contains("integral") || cleanProduct.contains("forma") || cleanProduct.contains("queijo")) {
            return 5;
        }
        if (cleanProduct.contains("bolo") || cleanProduct.contains("torta")) {
            return 7;
        }
        return 5;
    }

    private long peixariaExpiryAtFor(String product, long printedAt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(printedAt);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, peixariaValidityDaysForProduct(product));
        return calendar.getTimeInMillis();
    }

    private void savePadariaAddress() {
        if (padariaAddressEdit == null) {
            return;
        }
        String address = padariaAddressEdit.getText().toString().trim();
        prefs().edit().putString(PREF_PADARIA_ADDRESS,
                address.isEmpty() ? DEFAULT_PADARIA_ADDRESS : address).apply();
    }

    private void showTraceabilitySettingsDialog() {
        EditText addressEdit = new EditText(this);
        addressEdit.setSingleLine(false);
        addressEdit.setHint("Endereco de origem");
        addressEdit.setText(currentPadariaAddress());
        styleInput(addressEdit);

        new AlertDialog.Builder(this)
                .setTitle("Ajustes da rastreabilidade")
                .setMessage("Defina o endereco de origem que sera impresso nos lotes da padaria. Destino nao e preenchido.")
                .setView(field("Endereco de origem", addressEdit))
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String value = addressEdit.getText().toString().trim();
                    prefs().edit().putString(PREF_PADARIA_ADDRESS,
                            value.isEmpty() ? DEFAULT_PADARIA_ADDRESS : value).apply();
                    refreshPeixariaPreview();
                    setStatus("Endereco de origem atualizado.");
                })
                .show();
    }

    private void showCreateEstablishmentDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(4), 0, dp(4), 0);

        EditText nameEdit = new EditText(this);
        nameEdit.setSingleLine(true);
        nameEdit.setHint("Ex: Mercado Central");
        styleInput(nameEdit);
        form.addView(field("Nome do estabelecimento", nameEdit), fullWidth(-2));

        EditText passwordEdit = new EditText(this);
        passwordEdit.setSingleLine(true);
        passwordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setHint("Senha do estabelecimento");
        styleInput(passwordEdit);
        form.addView(field("Senha (minimo 4 caracteres)", passwordEdit), fullWidth(-2));

        EditText addressEdit = new EditText(this);
        addressEdit.setSingleLine(false);
        addressEdit.setHint("Endereco de origem (opcional)");
        styleInput(addressEdit);
        form.addView(field("Endereco de origem", addressEdit), fullWidth(-2));

        new AlertDialog.Builder(this)
                .setTitle("Novo estabelecimento")
                .setMessage("O painel e o historico serao isolados automaticamente para este estabelecimento.")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Criar", (dialog, which) -> createEstablishmentAsync(
                        nameEdit.getText().toString().trim(),
                        passwordEdit.getText().toString(),
                        addressEdit.getText().toString().trim()))
                .show();
    }

    private void createEstablishmentAsync(String name, String password, String address) {
        if (name.isEmpty() || password.length() < 4) {
            showError("Informe o nome e uma senha com pelo menos 4 caracteres.");
            return;
        }
        if (!isSupabaseConfigured()) {
            refreshSupabaseConfigAsync();
            showError("Supabase ainda nao configurado neste aparelho.");
            return;
        }
        setStatus("Criando estabelecimento na nuvem...");
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("admin_login", BETA_LOGIN);
                payload.put("admin_password", BETA_PASSWORD);
                payload.put("display_name", name);
                payload.put("establishment_password", password);
                payload.put("address", address);
                String response = postSupabaseRpcForText(SUPABASE_RPC_CREATE_ESTABLISHMENT, payload);
                JSONObject created = new JSONObject(response);
                prefs().edit()
                    .putString(PREF_ACTIVE_ESTABLISHMENT_SLUG, created.optString("slug", ""))
                    .putString(PREF_ACTIVE_ESTABLISHMENT_PASSWORD, password)
                    .apply();
                runOnUiThread(() -> setStatus("Estabelecimento criado: " + created.optString("display_name", name) + " (" + created.optString("slug", "") + ")"));
            } catch (Exception e) {
                Log.w(TAG, "Falha ao criar estabelecimento no Supabase", e);
                runOnUiThread(() -> showError("Nao foi possivel criar o estabelecimento: " + (e.getMessage() == null ? e.toString() : e.getMessage())));
            }
        }).start();
    }

    private String currentPadariaAddress() {
        return prefs().getString(PREF_PADARIA_ADDRESS, DEFAULT_PADARIA_ADDRESS);
    }

    private void showPeixariaPreviewDialog() {
        if (!validatePeixariaInputs()) {
            return;
        }
        String product = peixariaProductEdit == null ? "" : peixariaProductEdit.getText().toString().trim();
        String weight = peixariaWeightEdit == null ? "" : peixariaWeightEdit.getText().toString().trim().replace(',', '.');
        int copies = parseCopies(peixariaCopiesEdit);
        if (!isKnownPeixariaProduct(product)) {
            new AlertDialog.Builder(this)
                    .setTitle("Produto novo")
                    .setMessage("Salvar \"" + product + "\" no catalogo da padaria antes de imprimir?")
                    .setPositiveButton("Salvar", (dialog, which) -> {
                        List<String> products = loadPeixariaProductCatalog();
                        products.add(product);
                        persistPeixariaProductCatalog(products);
                        if (peixariaProductSpinner != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, products);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            peixariaProductSpinner.setAdapter(adapter);
                        }
                        showPeixariaPreviewDialog();
                    })
                    .setNegativeButton("Continuar", null)
                    .show();
            return;
        }
        double parsedWeight;
        try {
            parsedWeight = Double.parseDouble(weight);
        } catch (Exception e) {
            showError("Digite um peso valido em kg.");
            return;
        }

        String formattedWeight = String.format(new Locale("pt", "BR"), "%.3f", parsedWeight);
        String previewLot = nextPeixariaLotPreview();
        long previewPrintedAt = System.currentTimeMillis();
        PeixariaEntry previewEntry = new PeixariaEntry(previewPrintedAt, previewLot, product, formattedWeight, copies, peixariaExpiryAtFor(product, previewPrintedAt));

        Bitmap bitmap = renderPeixariaLabelBitmap(previewEntry);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pré-visualização da etiqueta - Lote " + previewLot);
        builder.setView(iv);
        builder.setPositiveButton("Imprimir", (dialog, which) -> {
            String reserved = reserveNextPeixariaLot();
            PeixariaEntry reservedEntry = new PeixariaEntry(
                    previewEntry.printedAt,
                    reserved,
                    previewEntry.product,
                    previewEntry.weightKg,
                    previewEntry.copies,
                    previewEntry.expiryAt
            );
            new Thread(() -> {
                try {
                    AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                    scanBleAndSend(() -> {
                        List<byte[]> payloads = buildPeixariaPayloadVariants(reservedEntry);
                        payloadsRef.set(payloads);
                        return payloads;
                    }, () -> {
                        commitPeixariaLot(reservedEntry);
                        syncPadariaLotAsync(reservedEntry);
                    });
                    recordPrintSuccess();
                } catch (Exception e) {
                    recordPrintFailure(e);
                    showError(e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }).start();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private boolean validatePeixariaInputs() {
        String product = peixariaProductEdit == null ? "" : peixariaProductEdit.getText().toString().trim();
        String weight = peixariaWeightEdit == null ? "" : peixariaWeightEdit.getText().toString().trim().replace(',', '.');
        String copies = peixariaCopiesEdit == null ? "" : peixariaCopiesEdit.getText().toString().trim();

        if (peixariaProductManualCheck != null && peixariaProductManualCheck.isChecked() && product.isEmpty()) {
            peixariaProductManualCheck.setChecked(false);
            showError("Digite o tipo de produto ou escolha um produto predefinido.");
            return false;
        }
        if (product.isEmpty()) {
            showError("Escolha um tipo de produto.");
            return false;
        }
        if (peixariaWeightManualCheck != null && peixariaWeightManualCheck.isChecked() && weight.isEmpty()) {
            peixariaWeightManualCheck.setChecked(false);
            showError("Digite o peso ou escolha um peso predefinido.");
            return false;
        }
        try {
            if (weight.isEmpty() || Double.parseDouble(weight) <= 0) {
                showError("Informe um peso maior que zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Informe um peso valido em kg.");
            return false;
        }
        if (peixariaCopiesManualCheck != null && peixariaCopiesManualCheck.isChecked() && copies.isEmpty()) {
            peixariaCopiesManualCheck.setChecked(false);
            showError("Digite a quantidade de copias.");
            return false;
        }
        try {
            int parsedCopies = Integer.parseInt(copies);
            if (parsedCopies < 1 || parsedCopies > 99) {
                showError("As copias devem estar entre 1 e 99.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Informe uma quantidade de copias valida.");
            return false;
        }
        return true;
    }

    private Bitmap renderPeixariaLabelBitmap(PeixariaEntry entry) {
        int width = 400;
        int height = 300;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("ETIQUETA DE RASTREABILIDADE", 10f, 36f, paint);

        paint.setStrokeWidth(2f);
        canvas.drawLine(10f, 44f, width - 10f, 44f, paint);

        paint.setTextSize(18f);
        paint.setFakeBoldText(false);
        canvas.drawText("PROD: " + cleanPrinterText(entry.product), 12f, 80f, paint);
        canvas.drawText("ORIGEM: PADARIA LOBO", 12f, 110f, paint);
        List<String> addressLines = splitLabelText("END: " + cleanPrinterText(currentPadariaAddress()), 26);
        paint.setTextSize(14f);
        for (int index = 0; index < Math.min(3, addressLines.size()); index++) {
            canvas.drawText(addressLines.get(index), 12f, 136f + (index * 19f), paint);
        }

        SimpleDateFormat df = dateFormat;
        String received = df.format(new Date(entry.printedAt));
        String expiry = df.format(new Date(entry.expiryAt));
        canvas.drawText("REC: " + received + " PROC: " + received, 12f, 193f, paint);
        canvas.drawText("VAL: " + expiry + "   KG: " + entry.weightKg, 12f, 217f, paint);

        paint.setFakeBoldText(true);
        paint.setTextSize(24f);
        canvas.drawText("LOTE: " + entry.lot, 12f, 266f, paint);

        return bitmap;
    }

    private List<String> splitLabelText(String text, int maxCharacters) {
        List<String> lines = new ArrayList<>();
        String remaining = text == null ? "" : text.trim();
        while (remaining.length() > maxCharacters) {
            int breakAt = remaining.lastIndexOf(' ', maxCharacters);
            if (breakAt <= 0) {
                breakAt = maxCharacters;
            }
            lines.add(remaining.substring(0, breakAt).trim());
            remaining = remaining.substring(breakAt).trim();
        }
        if (!remaining.isEmpty()) {
            lines.add(remaining);
        }
        return lines;
    }

    private void refreshPeixariaPanel() {
        if (peixariaPanelText == null) {
            return;
        }
        List<PeixariaEntry> entries = loadPeixariaHistory();
        if (entries.isEmpty()) {
            peixariaPanelText.setText("Nenhuma etiqueta de rastreabilidade impressa neste aparelho ainda.");
            return;
        }
        StringBuilder text = new StringBuilder("Registros neste aparelho: ").append(entries.size());
        int limit = Math.min(entries.size(), 80);
        for (int i = 0; i < limit; i++) {
            PeixariaEntry entry = entries.get(i);
            text.append("\n\n")
                    .append(shortDateTimeFormat.format(new Date(entry.printedAt)))
                    .append(" | Lote ").append(entry.lot)
                    .append("\n").append(entry.product)
                    .append(" - ").append(entry.weightKg).append(" kg")
                    .append(" - ").append(labelCount(entry.copies))
                    .append("\nVal: ").append(dateFormat.format(new Date(entry.expiryAt)));
        }
        if (entries.size() > limit) {
            text.append("\n\n+").append(entries.size() - limit).append(" registros antigos");
        }
        peixariaPanelText.setText(text.toString());
    }

    private List<PeixariaEntry> loadPeixariaHistory() {
        List<PeixariaEntry> entries = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs().getString(PREF_PEIXARIA_HISTORY_JSON, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                long printedAt = item.optLong("printedAt", 0);
                String lot = item.optString("lot", "");
                String product = item.optString("product", "");
                if (printedAt <= 0 || lot.trim().isEmpty() || product.trim().isEmpty()) continue;
                entries.add(new PeixariaEntry(printedAt, lot, product, item.optString("weightKg", ""), item.optInt("copies", 1), item.optLong("expiryAt", printedAt)));
            }
        } catch (JSONException e) {
            Log.w(TAG, "Historico Peixaria invalido", e);
        }
        entries.sort((left, right) -> Long.compare(right.printedAt, left.printedAt));
        return entries;
    }

    private void savePeixariaHistory(List<PeixariaEntry> entries) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < Math.min(entries.size(), MAX_PEIXARIA_HISTORY); i++) {
            PeixariaEntry entry = entries.get(i);
            JSONObject item = new JSONObject();
            try {
                item.put("printedAt", entry.printedAt);
                item.put("lot", entry.lot);
                item.put("product", entry.product);
                item.put("weightKg", entry.weightKg);
                item.put("copies", entry.copies);
                item.put("expiryAt", entry.expiryAt);
                array.put(item);
            } catch (JSONException ignored) {
            }
        }
        prefs().edit().putString(PREF_PEIXARIA_HISTORY_JSON, array.toString()).apply();
    }

    private void printPeixariaLabel() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }
        // Open preview dialog; reservation and actual print occur after user confirms in the preview
        showPeixariaPreviewDialog();
    }

    private void printEtiqueta() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        imprimirButton.setEnabled(false);
        setStatus("Imprimindo...");

        final String produtoFinal;
        final String validadeManualFinal;
        final Date startFinal;
        final int copiasFinal;
        final int fontModeFinal;
        final Uri imageUriFinal;

        try {
            String produto = produtoEdit.getText().toString().trim();
            produtoFinal = produto.isEmpty() ? firstProduct() : produto;
            validadeManualFinal = validadeManualEdit == null ? "" : validadeManualEdit.getText().toString().trim();
            startFinal = getStartDate();
            copiasFinal = parseCopies();
            fontModeFinal = selectedFontMode();
            imageUriFinal = validityImageUri;
        } catch (Exception e) {
            imprimirButton.setEnabled(true);
            showError(e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }

        new Thread(() -> {
            try {
                String produto = produtoFinal;
                Date start = startFinal;
                ValidityRule rule = resolveRule(produto);
                Date validade = addRule(start, rule);
                int copias = copiasFinal;

                PrintLines printLines = buildPrintLines(produto, start, validade, rule, validadeManualFinal);
                Date printedExpiry = resolvePrintedExpiry(validade, validadeManualFinal);
                boolean historyUsesHours = rule.usesHours() || printLines.statusValidade.length() > 10;
                TsplImage image = imageUriFinal == null ? null : buildTsplImage(imageUriFinal);
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    List<byte[]> payloads = buildPrintPayloadVariants(
                            printLines.title,
                            printLines.startLine,
                            printLines.validadeLine,
                            copias,
                            fontModeFinal,
                            image
                    );
                    payloadsRef.set(payloads);
                    return payloads;
                }, () -> recordSuccessfulValidityPrint(
                        produto,
                        copias,
                        start,
                        printedExpiry,
                        printLines.statusValidade,
                        historyUsesHours
                ));
                recordPrintSuccess();

                List<byte[]> payloads = payloadsRef.get();
                String statusFormatStart = rule.usesHours() ? dateTimeFormat.format(start) : dateFormat.format(start);
                setStatus("Enviando " + selectedPrinterModel() + " BLE (" + (payloads == null ? 0 : payloads.size()) + " tentativas, " + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes): " + produto
                        + " | " + rule.startLabel + " " + statusFormatStart + " | Val " + printLines.statusValidade);
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            } finally {
                runOnUiThread(() -> imprimirButton.setEnabled(true));
            }
        }).start();
    }

    private void printPriceLabel() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        final String name = priceNameEdit == null ? "" : priceNameEdit.getText().toString().trim();
        final String price = priceValueEdit == null ? "" : priceValueEdit.getText().toString().trim();
        final int copies = parseCopies(priceCopiesEdit);
        final int fontMode = selectedFontMode(priceFontSizeSpinner);
        final Uri imageUri = labelImageUri;

        if (name.isEmpty()) {
            showError("Digite o nome do produto para o preco.");
            return;
        }
        if (price.isEmpty()) {
            showError("Digite o preco.");
            return;
        }

        setStatus("Imprimindo preco...");
        new Thread(() -> {
            try {
                TsplImage image = imageUri == null ? null : buildTsplImage(imageUri);
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    List<byte[]> payloads = buildPricePayloadVariants(name, price, copies, fontMode, image);
                    payloadsRef.set(payloads);
                    return payloads;
                }, null);
                recordPrintSuccess();
                List<byte[]> payloads = payloadsRef.get();
                setStatus("Enviando preco (" + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes, " + (payloads == null ? 0 : payloads.size()) + " tentativas): " + name + " | " + formatPrice(price));
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void printOnlyPriceLabel() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        final String price = onlyPriceValueEdit == null ? "" : onlyPriceValueEdit.getText().toString().trim();
        final int copies = parseCopies(onlyPriceCopiesEdit);
        final int fontMode = selectedFontMode(onlyPriceFontSizeSpinner);
        final int itemsPerLabel = onlyPriceItemsPerLabel();
        final Uri imageUri = onlyPriceImageUri;

        if (price.isEmpty()) {
            showError("Digite o preco.");
            return;
        }

        setStatus("Imprimindo so preco...");
        new Thread(() -> {
            try {
                TsplImage image = imageUri == null ? null : buildTsplImage(imageUri);
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    List<byte[]> payloads = buildOnlyPricePayloadVariants(price, copies, fontMode, image, itemsPerLabel);
                    payloadsRef.set(payloads);
                    return payloads;
                }, null);
                recordPrintSuccess();
                List<byte[]> payloads = payloadsRef.get();
                setStatus("Enviando so preco (" + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes, " + (payloads == null ? 0 : payloads.size()) + " tentativas): "
                        + formatPrice(price)
                        + (itemsPerLabel > 1 ? " x" + itemsPerLabel + " por etiqueta" : ""));
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void refreshTextOnlyPreview() {
        if (textOnlyPreviewText == null) {
            return;
        }
        String text = textOnlyEdit == null ? "" : cleanPrinterText(textOnlyEdit.getText().toString()).trim();
        if (text.isEmpty()) {
            textOnlyPreviewText.setText("Previa do texto");
            textOnlyPreviewText.setTextColor(COLOR_MUTED);
            return;
        }

        TextOnlyLayout layout = layoutTextOnly(text);
        textOnlyPreviewText.setText(String.join("\n", layout.lines));
        textOnlyPreviewText.setTextColor(COLOR_TEXT);
        textOnlyPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, switch (layout.multiplier) {
            case 3 -> 30;
            case 2 -> 25;
            default -> 20;
        });
    }

    private void refreshImageOnlyPreview() {
        if (imageOnlyStatusText != null) {
            imageOnlyStatusText.setText(imageOnlyUri == null
                    ? "Sem imagem para imprimir."
                    : "Imagem selecionada. A impressao usa o maior tamanho possivel da etiqueta.");
        }
        if (imageOnlyPreview != null) {
            if (imageOnlyUri == null) {
                imageOnlyPreview.setImageDrawable(null);
            } else {
                imageOnlyPreview.setImageURI(imageOnlyUri);
            }
        }
    }

    private void refreshPhoto50x30Preview() {
        if (photo50x30StatusText != null) {
            photo50x30StatusText.setText(photo50x30Uri == null
                    ? "Selecione uma foto para a etiqueta 50x30."
                    : "Foto pronta para ocupar toda a etiqueta 50x30.");
        }
        if (photo50x30Preview != null) {
            if (photo50x30Uri == null) {
                photo50x30Preview.setImageDrawable(null);
            } else {
                photo50x30Preview.setImageURI(photo50x30Uri);
            }
        }
    }

    private void printTextOnlyLabel() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        final String text = textOnlyEdit == null ? "" : textOnlyEdit.getText().toString().trim();
        final int copies = parseCopies(textOnlyCopiesEdit);
        if (cleanPrinterText(text).trim().isEmpty()) {
            showError("Digite o texto da etiqueta.");
            return;
        }

        setStatus("Imprimindo texto...");
        new Thread(() -> {
            try {
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    List<byte[]> payloads = buildTextPayloadVariants(text, copies);
                    payloadsRef.set(payloads);
                    return payloads;
                }, null);
                recordPrintSuccess();
                List<byte[]> payloads = payloadsRef.get();
                setStatus("Enviando texto (" + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes, " + (payloads == null ? 0 : payloads.size()) + " tentativas).");
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void printImageOnlyLabel() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        final Uri imageUri = imageOnlyUri;
        final int copies = parseCopies(imageOnlyCopiesEdit);
        if (imageUri == null) {
            showError("Escolha uma imagem.");
            return;
        }

        setStatus("Imprimindo imagem...");
        new Thread(() -> {
            try {
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    TsplImage image = buildFullLabelTsplImage(imageUri);
                    List<byte[]> payloads = buildImagePayloadVariants(image, copies);
                    payloadsRef.set(payloads);
                    return payloads;
                }, null);
                recordPrintSuccess();
                List<byte[]> payloads = payloadsRef.get();
                setStatus("Enviando imagem (" + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes, " + (payloads == null ? 0 : payloads.size()) + " tentativas).");
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void printPhoto50x30Label() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        final Uri imageUri = photo50x30Uri;
        final int copies = parseCopies(photo50x30CopiesEdit);
        if (imageUri == null) {
            showError("Escolha uma foto para a etiqueta 50x30.");
            return;
        }

        setStatus("Imprimindo foto 50x30...");
        new Thread(() -> {
            try {
                AtomicReference<List<byte[]>> payloadsRef = new AtomicReference<>();
                scanBleAndSend(() -> {
                    TsplImage image = buildPhoto50x30TsplImage(imageUri);
                    List<byte[]> payloads = buildPhoto50x30PayloadVariants(image, copies);
                    payloadsRef.set(payloads);
                    return payloads;
                }, null);
                recordPrintSuccess();
                List<byte[]> payloads = payloadsRef.get();
                setStatus("Enviando foto 50x30 (" + (payloads == null ? 0 : totalPayloadBytes(payloads)) + " bytes, " + (payloads == null ? 0 : payloads.size()) + " tentativas).");
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void printTestLabel() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        setStatus("Enviando diagnostico PT260...");
        new Thread(() -> {
            try {
                BluetoothDevice device = findPrinter();
                String selectedMode = selectedBluetoothMode();
                int totalBytes = 0;

                if ("Automatico".equals(selectedMode)) {
                    List<String> scanModes = PrinterConnectionUtils.buildConnectionModes("Automatico");
                    int attempts = 0;
                    for (String mode : scanModes) {
                        attempts++;
                        setStatus("Diagnostico: testando " + mode + " (" + attempts + "/" + scanModes.size() + ")");
                        try {
                            byte[] payload = buildDiagnosticPayload(mode);
                            PrinterConnection connection = connectToDevice(device, mode);
                            try (BluetoothSocket socket = connection.socket) {
                                sendPayload(socket, payload, connection.method, device, 8000);
                                totalBytes += payload.length;
                                setStatus("Diagnostico: " + mode + " respondeu. " + attempts + "/" + scanModes.size() + " testados.");
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Diagnostico falhou em " + mode, e);
                            setStatus("Diagnostico: " + mode + " falhou. " + attempts + "/" + scanModes.size() + " testados.");
                        }
                        Thread.sleep(1500);
                    }
                    setStatus("Varredura enviada (" + totalBytes + " bytes). Testados: " + scanModes.size() + " modos.");
                    recordPrintSuccess();
                    return;
                }

                byte[] payload = buildDiagnosticPayload(selectedMode);
                PrinterConnection connection = connectToDevice(device, selectedMode);
                try (BluetoothSocket socket = connection.socket) {
                    sendPayload(socket, payload, connection.method, device, 25000);
                }
                recordPrintSuccess();
                setStatus("Diagnostico enviado (" + connection.method + ", " + payload.length + " bytes).");
            } catch (Exception e) {
                recordPrintFailure(e);
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    private void sendPayload(BluetoothSocket socket, byte[] payload, String method, BluetoothDevice device, int keepOpenMs) throws Exception {
        rememberPrinterDevice(device);
        // If this device looks like the new PT-260/XD210, prefer PT260 protocol for this send only.
        setTransientPrinterModelOverrideForDevice(device);
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
            Log.i(TAG, "Enviando " + payload.length + " bytes via " + method + " para " + device.getName());

            // Debug: if this looks like a TSPL 50x30 image payload for the XD210, log its ASCII prefix and a hex prefix
            try {
                String asAscii = new String(payload, java.nio.charset.StandardCharsets.US_ASCII);
                if (isDetectedPrinterModelXd210() && asAscii.contains("SIZE 50 mm,30 mm")) {
                    int asciiPreviewLen = Math.min(asAscii.length(), 1024);
                    Log.i(TAG, "TSPL 50x30 payload ASCII preview (" + asciiPreviewLen + " chars): " + asAscii.substring(0, asciiPreviewLen));
                    int hexPreview = Math.min(payload.length, 512);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < hexPreview; i++) {
                        sb.append(String.format("%02X ", payload[i]));
                    }
                    if (payload.length > hexPreview) sb.append("...");
                    Log.i(TAG, "TSPL 50x30 payload HEX prefix (" + hexPreview + " bytes): " + sb.toString());
                }
            } catch (Exception e) {
                Log.w(TAG, "Falha ao gerar preview do payload", e);
            }

            PrinterTransportUtils.writePayload(outputStream, payload);

            // For PT260/XD210, advance the label a bit after printing so the next label is not too close.
            try {
                String asAscii = null;
                try {
                    asAscii = new String(payload, java.nio.charset.StandardCharsets.US_ASCII);
                } catch (Exception ignored) {
                }
                if (isDetectedPrinterModelXd210()) {
                    int feedDots = (asAscii != null && asAscii.contains("SIZE 50 mm,30 mm")) ? 320 : 120;
                    Log.i(TAG, "Sending XD210 label feed after print: " + feedDots + " dots");
                    byte[] escFeed = new byte[]{0x1B, 0x4A, (byte) feedDots};
                    outputStream.write(escFeed);
                    outputStream.flush();
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to send extra feed", e);
            }

            Thread.sleep(Math.max(0, keepOpenMs));
        } finally {
            // Clear transient override so we don't change global behaviour for other devices.
            transientPrinterModelOverride = null;
        }
    }

    private void printBleDiagnostic() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermissionIfNeeded();
            return;
        }

        setStatus("Conectando BLE PT260...");
        new Thread(() -> {
            try {
                byte[] payload = buildTsplPayload();
                scanBleAndSend(payload);
            } catch (Exception e) {
                showError(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void scanBleAndSend(byte[] payload) throws IOException {
        scanBleAndSend(Arrays.asList(payload), null);
    }

    @SuppressLint("MissingPermission")
    private void scanBleAndSend(List<byte[]> payloads) throws IOException {
        scanBleAndSend(payloads, null);
    }

    @SuppressLint("MissingPermission")
    private void scanBleAndSend(byte[] payload, Runnable onSent) throws IOException {
        scanBleAndSend(Arrays.asList(payload), onSent);
    }

    @SuppressLint("MissingPermission")
    private void scanBleAndSend(PayloadBuilder payloadBuilder, Runnable onSent) throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            throw new IOException("Bluetooth desligado ou indisponivel.");
        }

        BluetoothDevice printerDevice = findPrinter();
        setTransientPrinterModelOverrideForDevice(printerDevice);
        List<byte[]> payloads = payloadBuilder.build();

        try {
            setStatus("Conectando via SPP " + selectedPrinterModel() + "...");
            sendPayloadsViaClassicSocket(printerDevice, payloads, onSent);
            return;
        } catch (Exception classicError) {
            Log.w(TAG, "Falha na conexao SPP, tentando BLE", classicError);
            setStatus("SPP falhou. Tentando BLE...");
        }

        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) {
            connectBleAndSend(printerDevice, payloads, onSent);
            return;
        }

        setStatus("Escaneando BLE " + selectedPrinterModel() + "...");
        final boolean[] found = new boolean[]{false};
        final int[] seenCount = new int[]{0};
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                seenCount[0]++;
                BluetoothDevice discoveredDevice = result.getDevice();
                String name = discoveredDevice.getName();
                if (name == null && result.getScanRecord() != null) {
                    name = result.getScanRecord().getDeviceName();
                }
                String services = result.getScanRecord() == null || result.getScanRecord().getServiceUuids() == null
                        ? ""
                        : result.getScanRecord().getServiceUuids().toString();
                Log.i(TAG, "BLE scan: " + discoveredDevice.getAddress() + " name=" + name + " services=" + services);
                if (seenCount[0] == 1 || seenCount[0] % 10 == 0) {
                    setStatus("Escaneando BLE... " + seenCount[0] + " anuncios vistos.");
                }

                if (found[0] || !(looksLikeBlePrinter(name, services) || isKnownB1Address(discoveredDevice.getAddress()))) {
                    return;
                }

                found[0] = true;
                scanner.stopScan(this);
                setStatus("BLE achou " + (name == null ? discoveredDevice.getAddress() : name) + ". Conectando...");
                setTransientPrinterModelOverrideForDevice(discoveredDevice);
                connectBleAndSend(discoveredDevice, payloads, onSent);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.w(TAG, "BLE scan falhou: " + errorCode);
                if (!found[0]) {
                    found[0] = true;
                    try {
                        BluetoothDevice fallbackDevice = findPrinter();
                        connectBleAndSend(fallbackDevice, payloads, onSent);
                    } catch (IOException e) {
                        showError(e.getMessage() == null ? e.toString() : e.getMessage());
                    }
                }
            }
        };

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(null, settings, callback);

        new Thread(() -> {
            try {
                Thread.sleep(45000);
            } catch (InterruptedException ignored) {
            }
            if (!found[0]) {
                found[0] = true;
                try {
                    scanner.stopScan(callback);
                } catch (Exception ignored) {
                }
                setStatus("BLE " + selectedPrinterModel() + " nao apareceu. Vistos: " + seenCount[0] + ". Tentando pareado...");
                try {
                    BluetoothDevice fallbackDevice = findPrinter();
                    connectBleAndSend(fallbackDevice, payloads, onSent);
                } catch (IOException e) {
                    showError(e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void scanBleAndSend(List<byte[]> payloads, Runnable onSent) throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            throw new IOException("Bluetooth desligado ou indisponivel.");
        }

        BluetoothDevice printerDevice = findPrinter();
        try {
            setStatus("Conectando via SPP " + selectedPrinterModel() + "...");
            sendPayloadsViaClassicSocket(printerDevice, payloads, onSent);
            return;
        } catch (Exception classicError) {
            Log.w(TAG, "Falha na conexao SPP, tentando BLE", classicError);
            setStatus("SPP falhou. Tentando BLE...");
        }

        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) {
            connectBleAndSend(printerDevice, payloads, onSent);
            return;
        }

        setStatus("Escaneando BLE " + selectedPrinterModel() + "...");
        final boolean[] found = new boolean[]{false};
        final int[] seenCount = new int[]{0};
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                seenCount[0]++;
                BluetoothDevice discoveredDevice = result.getDevice();
                String name = discoveredDevice.getName();
                if (name == null && result.getScanRecord() != null) {
                    name = result.getScanRecord().getDeviceName();
                }
                String services = result.getScanRecord() == null || result.getScanRecord().getServiceUuids() == null
                        ? ""
                        : result.getScanRecord().getServiceUuids().toString();
                Log.i(TAG, "BLE scan: " + discoveredDevice.getAddress() + " name=" + name + " services=" + services);
                if (seenCount[0] == 1 || seenCount[0] % 10 == 0) {
                    setStatus("Escaneando BLE... " + seenCount[0] + " anuncios vistos.");
                }

                if (found[0] || !(looksLikeBlePrinter(name, services) || isKnownB1Address(discoveredDevice.getAddress()))) {
                    return;
                }

                found[0] = true;
                scanner.stopScan(this);
                setStatus("BLE achou " + (name == null ? discoveredDevice.getAddress() : name) + ". Conectando...");
                connectBleAndSend(discoveredDevice, payloads, onSent);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.w(TAG, "BLE scan falhou: " + errorCode);
                if (!found[0]) {
                    found[0] = true;
                    try {
                        BluetoothDevice fallbackDevice = findPrinter();
                        connectBleAndSend(fallbackDevice, payloads, onSent);
                    } catch (IOException e) {
                        showError(e.getMessage() == null ? e.toString() : e.getMessage());
                    }
                }
            }
        };

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(null, settings, callback);

        new Thread(() -> {
            try {
                Thread.sleep(45000);
            } catch (InterruptedException ignored) {
            }
            if (!found[0]) {
                found[0] = true;
                try {
                    scanner.stopScan(callback);
                } catch (Exception ignored) {
                }
                setStatus("BLE " + selectedPrinterModel() + " nao apareceu. Vistos: " + seenCount[0] + ". Tentando pareado...");
                try {
                    BluetoothDevice fallbackDevice = findPrinter();
                    connectBleAndSend(fallbackDevice, payloads, onSent);
                } catch (IOException e) {
                    showError(e.getMessage() == null ? e.toString() : e.getMessage());
                }
            }
        }).start();
    }

    private void sendPayloadsViaClassicSocket(BluetoothDevice device, List<byte[]> payloads, Runnable onSent) throws Exception {
        PrinterConnection connection = connectToDevice(device, selectedBluetoothMode());
        try (BluetoothSocket socket = connection.socket) {
            setStatus("Conectando via " + connection.method + "...");
            for (int index = 0; index < payloads.size(); index++) {
                byte[] payload = payloads.get(index);
                String label = payloads.size() > 1 ? ("protocolo " + (index + 1) + "/" + payloads.size()) : "payload";
                setStatus("Enviando " + label + " via " + connection.method + "...");
                sendPayload(socket, payload, connection.method, device, index == payloads.size() - 1 ? 1200 : 250);
                if (index < payloads.size() - 1) {
                    Thread.sleep(250);
                }
            }
            if (onSent != null) {
                onSent.run();
            }
        }
    }

    private boolean looksLikeBlePrinter(String name, String services) {
        String cleanName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        String cleanServices = services == null ? "" : services.toLowerCase(Locale.ROOT);
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            return looksLikeNiimbotB1(cleanName)
                    || cleanServices.contains("e7810a71")
                    || cleanServices.contains("bef8d6c9")
                    || (cleanName.isEmpty() && (cleanServices.contains("0000fee7") || cleanServices.contains("0000ffe0")));
        }
        if (PRINTER_MODEL_PT260.equals(selectedPrinterModel())) {
            return looksLikePt260(cleanName)
                    || cleanServices.contains("e7810a71")
                    || cleanServices.contains("49535343")
                    || cleanServices.contains("000018f0");
        }
        return looksLikePrinter(cleanName)
                || cleanName.contains("printer")
                || cleanName.contains("print")
                || cleanServices.contains("e7810a71")
                || cleanServices.contains("49535343")
                || cleanServices.contains("000018f0")
                || cleanServices.contains("0000fee7")
                || cleanServices.contains("0000ffe0");
    }

    @SuppressLint("MissingPermission")
    private void connectBleAndSend(BluetoothDevice device, List<byte[]> payloads, Runnable onSent) {
        rememberPrinterDevice(device);
        final BluetoothGatt[] holder = new BluetoothGatt[1];
        BluetoothGattCallback callback = new BluetoothGattCallback() {
            private int mtu = 20;
            private boolean discovered = false;
            private boolean startedWriting = false;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i(TAG, "BLE state status=" + status + " newState=" + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    setStatus("BLE conectado. Descobrindo servicos...");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        gatt.requestMtu(185);
                    } else {
                        gatt.discoverServices();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (!discovered) {
                        setStatus("BLE desconectado antes de imprimir.");
                    }
                    gatt.close();
                }
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                this.mtu = Math.max(20, mtu - 3);
                Log.i(TAG, "BLE MTU=" + mtu + " status=" + status);
                if (!discovered) {
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                discovered = true;
                if (startedWriting) {
                    return;
                }
                startedWriting = true;

                List<BluetoothGattCharacteristic> writable = findWritableBleCharacteristics(gatt);
                StringBuilder serviceLog = new StringBuilder();
                for (BluetoothGattService service : gatt.getServices()) {
                    serviceLog.append(service.getUuid()).append(" ");
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        serviceLog.append(characteristic.getUuid())
                                .append("(0x")
                                .append(Integer.toHexString(characteristic.getProperties()))
                                .append(") ");
                    }
                }
                Log.i(TAG, "BLE servicos: " + serviceLog);

                if (writable.isEmpty()) {
                    setStatus("BLE conectado, mas sem caracteristica WRITE.");
                    gatt.disconnect();
                    gatt.close();
                    return;
                }

                BluetoothGattCharacteristic target = writable.get(0);
                if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
                    enableB1Notifications(gatt, target);
                    try {
                        Thread.sleep(120);
                    } catch (InterruptedException ignored) {
                    }
                }
                setStatus("BLE: enviando " + payloads.size() + " tentativa(s) no canal " + target.getUuid() + "...");
                new Thread(() -> {
                    int sent = 0;
                    boolean success = false;
                    try {
                        for (int index = 0; index < payloads.size(); index++) {
                            byte[] payload = payloads.get(index);
                            String label = payloads.size() > 1 ? ("protocolo " + (index + 1) + "/" + payloads.size()) : "payload";
                            setStatus("BLE: " + label + "...");
                            writeBleChunks(gatt, target, payload, mtu, shouldPaceBleWrites());
                            sent += payload.length;
                            success = true;
                            if (index < payloads.size() - 1) {
                                Thread.sleep(shouldPaceBleWrites() ? 80 : 1200);
                            }
                        }
                        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
                            int targetPages = extractB1TargetPages(payloads);
                            waitForB1PrintCompletion(gatt, target, targetPages, mtu);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Falha BLE char " + target.getUuid(), e);
                    }
                    setStatus("BLE enviado: " + sent + " bytes.");
                    if (success && onSent != null) {
                        try {
                            onSent.run();
                        } catch (Exception e) {
                            Log.w(TAG, "Falha ao registrar historico", e);
                        }
                    }
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ignored) {
                    }
                    gatt.disconnect();
                    gatt.close();
                }).start();
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                processB1Notification(characteristic.getValue());
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.i(TAG, "BLE write callback " + characteristic.getUuid() + " status=" + status);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder[0] = device.connectGatt(this, false, callback, BluetoothDevice.TRANSPORT_LE);
        } else {
            holder[0] = device.connectGatt(this, false, callback);
        }
    }

    private List<BluetoothGattCharacteristic> findWritableBleCharacteristics(BluetoothGatt gatt) {
        List<BluetoothGattCharacteristic> writable = new ArrayList<>();
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                int properties = characteristic.getProperties();
                if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                        || (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    if (blePriority(characteristic.getUuid().toString()) < 5) {
                        writable.add(characteristic);
                    }
                }
            }
        }

        writable.sort(Comparator.comparingInt(characteristic -> blePriority(characteristic.getUuid().toString())));
        return writable;
    }

    private int blePriority(String uuid) {
        String value = uuid.toLowerCase(Locale.ROOT);
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel()) && value.equals("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f")) {
            return 0;
        }
        if (value.equals(PT260_BLE_WRITE_UUID)) {
            return 1;
        }
        if (value.equals("0000fec7-0000-1000-8000-00805f9b34fb")) {
            return 2;
        }
        if (value.contains("ffe1") || value.contains("ff02")) {
            return 3;
        }
        if (value.equals("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f")) {
            return 4;
        }
        return 10;
    }

    private boolean shouldPaceBleWrites() {
        return PRINTER_MODEL_B1.equals(selectedPrinterModel());
    }

    @SuppressLint("MissingPermission")
    private void writeBleChunks(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] payload, int mtuPayload, boolean paceWrites) throws InterruptedException, IOException {
        int writeType = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
                ? BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                : BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
        int chunkSize = Math.max(20, Math.min(mtuPayload, 180));

        Log.i(TAG, "BLE escrevendo " + payload.length + " bytes em " + characteristic.getUuid() + " chunk=" + chunkSize + " pace=" + paceWrites);
        for (int offset = 0; offset < payload.length; offset += chunkSize) {
            int length = Math.min(chunkSize, payload.length - offset);
            byte[] chunk = Arrays.copyOfRange(payload, offset, offset + length);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                int status = gatt.writeCharacteristic(characteristic, chunk, writeType);
                if (status != 0) {
                    throw new IOException("BLE recusou parte da etiqueta: " + status);
                }
            } else {
                characteristic.setWriteType(writeType);
                characteristic.setValue(chunk);
                if (!gatt.writeCharacteristic(characteristic)) {
                    throw new IOException("BLE recusou parte da etiqueta.");
                }
            }
            Thread.sleep(paceWrites ? 120 : (writeType == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE ? 90 : 180));
        }
    }

    private void performB1Handshake(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int mtuPayload) throws InterruptedException, IOException {
        byte[][] handshakeFrames = new byte[][]{
                new byte[]{0x03, 0x55, 0x55, (byte) 0xC1, 0x01, 0x01, (byte) 0xC1, (byte) 0xAA, (byte) 0xAA},
                buildNiimbotB1Frame(0xA5, new byte[]{0x01}),
                buildNiimbotB1Frame(0x40, new byte[]{0x08}),
                buildNiimbotB1Frame(0x40, new byte[]{0x0B}),
                buildNiimbotB1Frame(0x40, new byte[]{0x0D}),
                buildNiimbotB1Frame(0x40, new byte[]{0x0A}),
                buildNiimbotB1Frame(0x40, new byte[]{0x07}),
                buildNiimbotB1Frame(0x40, new byte[]{0x03}),
                buildNiimbotB1Frame(0x40, new byte[]{0x0C}),
                buildNiimbotB1Frame(0x40, new byte[]{0x09}),
                buildNiimbotB1Frame(0xDC, new byte[]{0x04})
        };

        for (byte[] frame : handshakeFrames) {
            try {
                writeBleChunks(gatt, characteristic, frame, mtuPayload, true);
            } catch (IOException e) {
                Log.w(TAG, "B1 handshake write falhou", e);
            }
            Thread.sleep(120);
        }
    }

    private String firstProduct() {
        return produtos.isEmpty() ? PRODUTOS.get(0) : produtos.get(0);
    }

    private int parseCopies() {
        return parseCopies(copiasEdit);
    }

    private int parseCopies(EditText editText) {
        if (editText == null) {
            return 1;
        }
        try {
            int value = Integer.parseInt(editText.getText().toString().trim());
            return Math.max(1, Math.min(99, value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private PrintLines buildPrintLines(String produto, Date start, Date validade, ValidityRule rule, String validadeManual) {
        SimpleDateFormat printFormat = rule.usesHours() ? shortDateTimeFormat : dateFormat;
        String title = cleanPrinterText(produto);
        String startLine = rule.startLabel + ": " + printFormat.format(start);
        String manual = cleanPrinterText(validadeManual).trim();

        if (!manual.isEmpty()) {
            Date manualDate = tryParsePrintDate(manual);
            if (manualDate != null) {
                SimpleDateFormat manualFormat = manual.length() > 10 ? shortDateTimeFormat : dateFormat;
                String formatted = manualFormat.format(manualDate);
                return new PrintLines(title, startLine, "Val: " + formatted, formatted);
            }

            String validadeLine = manual.toLowerCase(Locale.ROOT).startsWith("val") ? manual : "Val: " + manual;
            return new PrintLines(title, startLine, validadeLine, manual);
        }

        String formatted = printFormat.format(validade);
        return new PrintLines(title, startLine, "Val: " + formatted, formatted);
    }

    private Date tryParsePrintDate(String value) {
        try {
            if (value.length() <= 10) {
                return dateFormat.parse(value);
            }
            return dateTimeFormat.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private List<byte[]> buildPrintPayloadVariants(String title, String startLine, String validadeLine, int copies, int fontMode, TsplImage image) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads(title, startLine, validadeLine, copies));
            return payloads;
        }
        if (isDetectedPrinterModelXd210()) {
            payloads.add(buildWindowsLikePayload(title, startLine, validadeLine, copies));
            return payloads;
        }
        payloads.add(buildTsplPayload(title, startLine, validadeLine, copies, fontMode, image));
        return payloads;
    }

    private List<byte[]> buildPeixariaPayloadVariants(PeixariaEntry entry) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        String lotLine = "LOTE: " + entry.lot + "  " + entry.weightKg + " kg";
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads(entry.product, lotLine, "VAL: " + dateFormat.format(new Date(entry.expiryAt)), entry.copies));
            return payloads;
        }
        if (isDetectedPrinterModelXd210()) {
            payloads.add(buildWindowsLikePayload(entry.product, lotLine, "VAL: " + dateFormat.format(new Date(entry.expiryAt)), entry.copies));
            return payloads;
        }
        payloads.add(buildPeixariaTsplPayload(entry));
        return payloads;
    }

    private byte[] buildPeixariaTsplPayload(PeixariaEntry entry) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String product = cleanPrinterText(entry.product);
        String received = dateFormat.format(new Date(entry.printedAt));
        String expiry = dateFormat.format(new Date(entry.expiryAt));
        int copies = Math.max(1, Math.min(99, entry.copies));
        List<String> addressLines = splitLabelText("END: " + tsplText(currentPadariaAddress()), 26);
        for (int i = 0; i < copies; i++) {
            writeAscii(output, "SIZE 50 mm,30 mm\r\n");
            writeAscii(output, "GAP 2 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            writeAscii(output, "TEXT 12,8,\"3\",0,1,1,\"ETIQUETA DE RASTREABILIDADE\"\r\n");
            writeAscii(output, "BAR 10,35,370,2\r\n");
            writeAscii(output, "TEXT 12,45,\"2\",0,1,1,\"PROD: " + tsplText(product) + "\"\r\n");
            writeAscii(output, "TEXT 12,67,\"2\",0,1,1,\"ORIGEM: PADARIA LOBO\"\r\n");
            for (int line = 0; line < Math.min(3, addressLines.size()); line++) {
                writeAscii(output, "TEXT 12," + (89 + line * 16) + ",\"1\",0,1,1,\"" + addressLines.get(line) + "\"\r\n");
            }
            writeAscii(output, "TEXT 12,145,\"2\",0,1,1,\"REC: " + received + " PROC: " + received + "\"\r\n");
            writeAscii(output, "TEXT 12,167,\"1\",0,1,1,\"VAL: " + expiry + "   KG: " + tsplText(entry.weightKg) + "\"\r\n");
            writeAscii(output, "BAR 10,188,370,2\r\n");
            writeAscii(output, "TEXT 12,197,\"3\",0,1,1,\"LOTE: " + tsplText(entry.lot) + "\"\r\n");
            writeAscii(output, "PRINT 1\r\n");
        }
        return output.toByteArray();
    }

    private List<byte[]> buildPricePayloadVariants(String name, String price, int copies, int fontMode, TsplImage image) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads(name, "Preco: " + formatPrice(price), "", copies));
            return payloads;
        }
        if (isDetectedPrinterModelXd210()) {
            payloads.add(buildWindowsLikePayload(name, "Preco: " + formatPrice(price), "", copies));
            return payloads;
        }
        payloads.add(buildPriceTsplPayload(name, price, copies, fontMode, image));
        return payloads;
    }

    private List<byte[]> buildOnlyPricePayloadVariants(String price, int copies, int fontMode, TsplImage image, int itemsPerLabel) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads("", "Preco", formatPrice(price), copies));
            return payloads;
        }
        if (isDetectedPrinterModelXd210()) {
            payloads.add(buildWindowsLikePayload("Preco", formatPrice(price), "", copies));
            return payloads;
        }
        payloads.add(buildOnlyPriceTsplPayload(price, copies, fontMode, image, itemsPerLabel));
        return payloads;
    }

    private List<byte[]> buildTextPayloadVariants(String text, int copies) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads(text, "", "", copies));
            return payloads;
        }
        if (isDetectedPrinterModelXd210()) {
            payloads.add(buildWindowsLikePayload(text, "", "", copies));
            return payloads;
        }
        payloads.add(buildTextOnlyTsplPayload(text, copies));
        return payloads;
    }

    private List<byte[]> buildImagePayloadVariants(TsplImage image, int copies) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads("IMAGEM", "", "", copies));
            return payloads;
        }
        payloads.add(buildImageOnlyTsplPayload(image, copies));
        return payloads;
    }

    private List<byte[]> buildPhoto50x30PayloadVariants(TsplImage image, int copies) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            payloads.addAll(buildNiimbotB1Payloads("FOTO 50X30", "", "", copies));
            return payloads;
        }
        if (isDetectedPrinterModelXd210()) {
            Log.i(TAG, "Building photo50x30 payload for PT260_XD210 (ESC/POS raster)");
            byte[] p = buildPhoto50x30EscPosPayload(image, copies);
            Log.i(TAG, "Photo50x30 ESC/POS payload bytes=" + p.length);
            payloads.add(p);
            return payloads;
        }
        payloads.add(buildPhoto50x30TsplPayload(image, copies));
        return payloads;
    }

    private int totalPayloadBytes(List<byte[]> payloads) {
        int total = 0;
        for (byte[] payload : payloads) {
            total += payload.length;
        }
        return total;
    }

    private List<byte[]> buildNiimbotB1Payloads(String title, String startLine, String validadeLine, int copies) {
        List<byte[]> payloads = new ArrayList<>();
        payloads.add(new byte[]{0x03, 0x55, 0x55, (byte) 0xC1, 0x01, 0x01, (byte) 0xC1, (byte) 0xAA, (byte) 0xAA});
        payloads.add(buildNiimbotB1Frame(0xA5, new byte[]{0x01}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x08}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x0B}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x0D}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x0A}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x07}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x03}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x0C}));
        payloads.add(buildNiimbotB1Frame(0x40, new byte[]{0x09}));
        payloads.add(buildNiimbotB1Frame(0xDC, new byte[]{0x04}));
        payloads.add(buildNiimbotB1Frame(0x21, new byte[]{0x03}));
        payloads.add(buildNiimbotB1Frame(0x23, new byte[]{0x01}));
        payloads.add(buildNiimbotB1Frame(0x01, new byte[]{0x00, (byte) Math.max(1, Math.min(99, copies)), 0x00, 0x00, 0x00, 0x00, 0x00}));
        payloads.add(buildNiimbotB1Frame(0x03, new byte[]{0x01}));
        payloads.add(buildNiimbotB1Frame(0x13, new byte[]{0x00, (byte) 0xF0, 0x01, (byte) 0x80, 0x00, 0x01}));

        Bitmap bitmap = buildNiimbotB1Bitmap(title, startLine, validadeLine);
        int rows = bitmap.getHeight();
        int stride = (bitmap.getWidth() + 7) / 8;
        for (int row = 0; row < rows; row++) {
            byte[] rowBytes = buildNiimbotB1RowBytes(bitmap, row, stride);
            int blackPixels = countNiimbotB1RowBlackPixels(rowBytes);
            if (blackPixels == 0) {
                payloads.add(buildNiimbotB1Frame(0x84, new byte[]{(byte) (row >> 8), (byte) row, 0x01}));
            } else {
                byte[] data = new byte[6 + rowBytes.length];
                data[0] = (byte) (row >> 8);
                data[1] = (byte) row;
                data[2] = 0x00;
                data[3] = (byte) (blackPixels & 0xFF);
                data[4] = (byte) ((blackPixels >> 8) & 0xFF);
                data[5] = 0x01;
                System.arraycopy(rowBytes, 0, data, 6, rowBytes.length);
                payloads.add(buildNiimbotB1Frame(0x85, data));
            }
        }

        payloads.add(buildNiimbotB1Frame(0xE3, new byte[]{0x01}));
        return payloads;
    }

    private byte[] buildNiimbotB1Frame(int command, byte[] data) {
        byte[] payload = new byte[4 + data.length];
        payload[0] = 0x55;
        payload[1] = 0x55;
        payload[2] = (byte) command;
        payload[3] = (byte) data.length;
        System.arraycopy(data, 0, payload, 4, data.length);
        byte crc = (byte) (payload[2] ^ payload[3]);
        for (int i = 0; i < data.length; i++) {
            crc = (byte) (crc ^ data[i]);
        }
        byte[] frame = new byte[payload.length + 3];
        System.arraycopy(payload, 0, frame, 0, payload.length);
        frame[frame.length - 3] = crc;
        frame[frame.length - 2] = (byte) 0xAA;
        frame[frame.length - 1] = (byte) 0xAA;
        return frame;
    }

    private int extractB1TargetPages(List<byte[]> payloads) {
        for (byte[] payload : payloads) {
            if (payload.length >= 6 && payload[0] == 0x55 && payload[1] == 0x55 && (payload[2] & 0xFF) == 0x01) {
                int len = payload[3] & 0xFF;
                if (len >= 2) {
                    return ((payload[4] & 0xFF) << 8) | (payload[5] & 0xFF);
                }
            }
        }
        return 1;
    }

    private Bitmap buildNiimbotB1Bitmap(String title, String startLine, String validadeLine) {
        int width = 384;
        int height = 240;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1f);
        paint.setTextSize(34f);
        paint.setFakeBoldText(true);
        canvas.drawText(cleanPrinterText(title), 16f, 44f, paint);

        paint.setTextSize(26f);
        paint.setFakeBoldText(false);
        canvas.drawText(cleanPrinterText(startLine), 16f, 94f, paint);
        canvas.drawText(cleanPrinterText(validadeLine), 16f, 134f, paint);

        return bitmap;
    }

    static boolean shouldRenderNiimbotB1Pixel(int alpha, int red, int green, int blue) {
        if (alpha <= 80) {
            return false;
        }
        int luminance = (red * 299 + green * 587 + blue * 114) / 1000;
        return luminance < 128;
    }

    private byte[] buildNiimbotB1RowBytes(Bitmap bitmap, int row, int stride) {
        byte[] rowBytes = new byte[stride];
        for (int column = 0; column < bitmap.getWidth(); column++) {
            int pixel = bitmap.getPixel(column, row);
            if (shouldRenderNiimbotB1Pixel(Color.alpha(pixel), Color.red(pixel), Color.green(pixel), Color.blue(pixel))) {
                rowBytes[column / 8] |= (byte) (0x80 >> (column % 8));
            }
        }
        return rowBytes;
    }

    private int countNiimbotB1RowBlackPixels(byte[] rowBytes) {
        int count = 0;
        for (byte rowByte : rowBytes) {
            int value = rowByte & 0xFF;
            while (value != 0) {
                count += value & 1;
                value >>>= 1;
            }
        }
        return count;
    }

    private volatile int b1LastStatusPage = 0;

    private void enableB1Notifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        BluetoothGattCharacteristic notifyChar = characteristic;
        int properties = notifyChar.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0
                && (properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0) {
            notifyChar = findB1NotificationCharacteristic(gatt);
        }

        if (notifyChar == null) {
            Log.w(TAG, "Falha ao encontrar caracteristica de notificacao BLE B1");
            return;
        }

        if (!gatt.setCharacteristicNotification(notifyChar, true)) {
            Log.w(TAG, "Falha ao habilitar notificacoes BLE B1");
            return;
        }

        BluetoothGattDescriptor descriptor = notifyChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            int props = notifyChar.getProperties();
            if ((props & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            if (!gatt.writeDescriptor(descriptor)) {
                Log.w(TAG, "Falha ao escrever descriptor de notificacao B1");
            }
        } else {
            Log.w(TAG, "Descriptor de notificacao nao encontrado em B1");
        }
    }

    private BluetoothGattCharacteristic findB1NotificationCharacteristic(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                int properties = characteristic.getProperties();
                if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                        || (properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                    return characteristic;
                }
            }
        }
        return null;
    }

    private void waitForB1PrintCompletion(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int copies, int mtuPayload) throws InterruptedException, IOException {
        b1LastStatusPage = 0;
        long startTime = System.currentTimeMillis();
        int targetPage = Math.max(1, copies);
        while (System.currentTimeMillis() - startTime < 25000) {
            setStatus("B1: aguardando status de print... page=" + b1LastStatusPage);
            writeBleChunks(gatt, characteristic, buildNiimbotB1Frame(0xA3, new byte[]{0x01}), mtuPayload, shouldPaceBleWrites());
            Thread.sleep(300);
            if (b1LastStatusPage >= targetPage) {
                setStatus("B1: status completo page=" + b1LastStatusPage + ", enviando PrintEnd...");
                writeBleChunks(gatt, characteristic, buildNiimbotB1Frame(0xF3, new byte[]{0x01}), mtuPayload, shouldPaceBleWrites());
                return;
            }
        }
        Log.w(TAG, "Timeout esperando conclusao do print B1; enviando PrintEnd mesmo assim.");
        writeBleChunks(gatt, characteristic, buildNiimbotB1Frame(0xF3, new byte[]{0x01}), mtuPayload, shouldPaceBleWrites());
    }

    private void processB1Notification(byte[] value) {
        if (value == null || value.length < 6) {
            return;
        }
        if (value[0] != 0x55 || value[1] != 0x55 || value[value.length - 2] != (byte) 0xAA || value[value.length - 1] != (byte) 0xAA) {
            return;
        }
        int cmd = value[2] & 0xFF;
        int len = value[3] & 0xFF;
        if (cmd == 0xB3 && len >= 2) {
            int page = ((value[4] & 0xFF) << 8) | (value[5] & 0xFF);
            b1LastStatusPage = page;
            Log.i(TAG, "B1 status page=" + page);
        }
    }

    private byte[] buildSimpleAsciiPayload(String title, String startLine, String validadeLine, int copies) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 0; i < Math.max(1, copies); i++) {
            writeAscii(output, cleanPrinterText(title));
            writeLf(output);
            writeAscii(output, cleanPrinterText(startLine));
            writeLf(output);
            writeAscii(output, cleanPrinterText(validadeLine));
            output.write(new byte[]{0x0A, 0x0A, 0x0A});
        }
        return output.toByteArray();
    }

    private byte[] buildEscPosPayload(String produto, Date start, Date validade, int copias) throws IOException {
        String productText = cleanPrinterText(produto);
        ValidityRule rule = resolveRule(produto);
        SimpleDateFormat printFormat = rule.usesHours() ? shortDateTimeFormat : dateFormat;
        return buildWindowsLikePayload(
                productText,
                rule.startLabel + ": " + printFormat.format(start),
                "Val: " + printFormat.format(validade),
                copias
        );
    }

    private byte[] buildWindowsLikePayload(String title, String startLine, String validadeLine, int copias) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int i = 0; i < copias; i++) {
            output.write(new byte[]{0x1B, 0x40});
            output.write(new byte[]{0x1B, 0x61, 0x01});
            output.write(new byte[]{0x1B, 0x4D, 0x00});
            output.write(new byte[]{0x1B, 0x21, 0x00});
            output.write(new byte[]{0x1B, 0x45, 0x01});

            List<String> titleLines = wrapText(title, 24);
            if (titleLines.isEmpty()) {
                writeAscii(output, title);
            } else {
                for (int lineIndex = 0; lineIndex < titleLines.size() && lineIndex < 2; lineIndex++) {
                    if (lineIndex > 0) {
                        writeLf(output);
                    }
                    writeAscii(output, titleLines.get(lineIndex));
                }
            }

            output.write(new byte[]{0x0A, 0x1B, 0x45, 0x00});
            writeAscii(output, startLine);
            writeLf(output);
            writeAscii(output, validadeLine);
            output.write(new byte[]{0x0A, 0x0A, 0x0A});
        }

        return output.toByteArray();
    }

    private byte[] buildDiagnosticPayload(String modeLabel) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writeAscii(output, "DIAG " + cleanPrinterText(modeLabel));
        writeLf(output);
        writeAscii(output, "TEXTO PURO LF");
        writeLf(output);
        writeAscii(output, "Fab: 19/06/2026");
        writeLf(output);
        writeAscii(output, "Val: 24/06/2026");
        output.write(new byte[]{0x0A, 0x0A, 0x0A});
        output.write(new byte[]{0x1B, 0x64, 0x03});

        writeAscii(output, "DIAG " + cleanPrinterText(modeLabel));
        writeCrLf(output);
        writeAscii(output, "TEXTO PURO CRLF");
        writeCrLf(output);
        writeAscii(output, "Fab: 19/06/2026");
        writeCrLf(output);
        writeAscii(output, "Val: 24/06/2026");
        output.write(new byte[]{0x0D, 0x0A, 0x0D, 0x0A, 0x0D, 0x0A});
        output.write(new byte[]{0x1B, 0x64, 0x03});

        output.write(buildWindowsLikePayload(
                "ESC POS " + cleanPrinterText(modeLabel),
                "Fab: 19/06/2026",
                "Val: 24/06/2026",
                1
        ));

        return output.toByteArray();
    }

    private byte[] buildBlePayload() throws IOException {
        return buildBlePayload("BLE");
    }

    private byte[] buildBlePayload(String label) throws IOException {
        String cleanLabel = cleanPrinterText(label);
        if (cleanLabel.length() > 20) {
            cleanLabel = cleanLabel.substring(0, 20);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(new byte[]{0x1B, 0x40});
        writeAscii(output, "BLE PT260 " + cleanLabel);
        output.write(new byte[]{0x0A, 0x0D});
        writeAscii(output, "Mousse maracuja");
        output.write(new byte[]{0x0A, 0x0D});
        writeAscii(output, "Fab: 19/06/2026");
        output.write(new byte[]{0x0A, 0x0D});
        writeAscii(output, "Val: 24/06/2026");
        output.write(new byte[]{0x0A, 0x0D, 0x0A, 0x0D, 0x0A, 0x0D});
        output.write(new byte[]{0x1B, 0x64, 0x03});
        return output.toByteArray();
    }

    private byte[] buildBleSimplePayload(String label) throws IOException {
        String cleanLabel = cleanPrinterText(label);
        if (cleanLabel.length() > 16) {
            cleanLabel = cleanLabel.substring(0, 16);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeAscii(output, "BLE " + cleanLabel);
        output.write(new byte[]{0x0A, 0x0D});
        writeAscii(output, "Mousse maracuja");
        output.write(new byte[]{0x0A, 0x0D});
        writeAscii(output, "Fab: 19/06/2026");
        output.write(new byte[]{0x0A, 0x0D});
        writeAscii(output, "Val: 24/06/2026");
        output.write(new byte[]{0x0A, 0x0D, 0x0A, 0x0D, 0x0A, 0x0D, 0x0A, 0x0D});
        return output.toByteArray();
    }

    private List<byte[]> buildBleProtocolPayloads(String label) throws IOException {
        List<byte[]> payloads = new ArrayList<>();
        payloads.add(buildTsplPayload());
        payloads.add(buildCpclPayload());
        payloads.add(buildBleSimplePayload(label));
        return payloads;
    }

    private byte[] buildTsplPayload() throws IOException {
        return buildTsplPayload(
                "Mousse maracuja",
                "Fab: 19/06/2026",
                "Val: 24/06/2026",
                1
        );
    }

    private byte[] buildTsplPayload(String produto, Date start, Date validade, int copias) throws IOException {
        String productText = cleanPrinterText(produto);
        ValidityRule rule = resolveRule(produto);
        SimpleDateFormat printFormat = rule.usesHours() ? shortDateTimeFormat : dateFormat;
        return buildTsplPayload(
                productText,
                rule.startLabel + ": " + printFormat.format(start),
                "Val: " + printFormat.format(validade),
                copias
        );
    }

    private byte[] buildTsplPayload(String title, String startLine, String validadeLine, int copias) throws IOException {
        return buildTsplPayload(title, startLine, validadeLine, copias, 0, null);
    }

    private byte[] buildTsplPayload(String title, String startLine, String validadeLine, int copias, int fontMode, TsplImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String cleanTitle = cleanPrinterText(title);
        boolean hasImage = image != null;
        int textX = hasImage ? 78 : 16;
        int titleLimit = hasImage ? (fontMode >= 1 ? 10 : 14) : (fontMode >= 1 ? 14 : 20);
        List<String> titleLines = wrapText(cleanTitle, titleLimit);
        if (titleLines.isEmpty()) {
            titleLines.add(cleanTitle);
        }

        int safeCopies = Math.max(1, Math.min(99, copias));
        for (int i = 0; i < safeCopies; i++) {
            writeAscii(output, "SIZE 30 mm,22 mm\r\n");
            writeAscii(output, "GAP 6 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            if (image != null) {
                writeTsplImage(output, 8, 30, image);
            }
            int titleMul = isDetectedPrinterModelXd210() ? 2 : (fontMode >= 1 ? 2 : 1);
            int bodyMul = isDetectedPrinterModelXd210() ? 2 : (fontMode >= 2 ? 2 : 1);
            writeAscii(output, "TEXT " + textX + ",12,\"3\",0," + titleMul + "," + titleMul + ",\"" + tsplText(titleLines.get(0)) + "\"\r\n");
            if (fontMode == 0 && titleLines.size() > 1) {
                writeAscii(output, "TEXT " + textX + ",48,\"2\",0,1,1,\"" + tsplText(titleLines.get(1)) + "\"\r\n");
            }
            writeAscii(output, "TEXT " + textX + ",86,\"2\",0," + bodyMul + "," + bodyMul + ",\"" + tsplText(startLine) + "\"\r\n");
            writeAscii(output, "TEXT " + textX + ",124,\"2\",0," + bodyMul + "," + bodyMul + ",\"" + tsplText(validadeLine) + "\"\r\n");
            writeAscii(output, "PRINT 1\r\n");
        }

        return output.toByteArray();
    }

    private byte[] buildPriceTsplPayload(String name, String price, int copies, int fontMode, TsplImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String cleanName = cleanPrinterText(name);
        String cleanPrice = formatPrice(price);
        boolean hasImage = image != null;
        int textX = hasImage ? 78 : 16;
        int nameLimit = hasImage ? 14 : 22;
        List<String> nameLines = wrapText(cleanName, nameLimit);
        if (nameLines.isEmpty()) {
            nameLines.add(cleanName);
        }

        int safeCopies = Math.max(1, Math.min(99, copies));
        boolean largeNameFits = fontMode >= 2
                && nameLines.size() == 1
                && cleanName.length() <= (hasImage ? 8 : 12);
        int nameMul = isDetectedPrinterModelXd210() ? 2 : (largeNameFits ? 2 : 1);
        int priceMul = isDetectedPrinterModelXd210() ? 2 : (fontMode >= 1 ? 2 : 1);
        for (int i = 0; i < safeCopies; i++) {
            writeAscii(output, "SIZE 30 mm,22 mm\r\n");
            writeAscii(output, "GAP 6 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            if (image != null) {
                writeTsplImage(output, 8, 34, image);
            }
            writeAscii(output, "TEXT " + textX + ",16,\"3\",0," + nameMul + "," + nameMul + ",\"" + tsplText(nameLines.get(0)) + "\"\r\n");
            if (nameLines.size() > 1) {
                writeAscii(output, "TEXT " + textX + ",52,\"2\",0,1,1,\"" + tsplText(nameLines.get(1)) + "\"\r\n");
            }
            writeAscii(output, "TEXT " + textX + ",94,\"3\",0," + priceMul + "," + priceMul + ",\"" + tsplText(cleanPrice) + "\"\r\n");
            writeAscii(output, "PRINT 1\r\n");
        }
        return output.toByteArray();
    }

    private byte[] buildOnlyPriceTsplPayload(String price, int copies, int fontMode, TsplImage image, int itemsPerLabel) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String cleanPrice = formatPrice(price);
        int safeItemsPerLabel = (fontMode == 0 && (itemsPerLabel == 4 || itemsPerLabel == 6)) ? itemsPerLabel : 1;
        boolean hasImage = image != null;
        int textX = hasImage ? 78 : 16;
        int availableWidth = hasImage ? 154 : 208;
        int multiplier = onlyPriceMultiplier(fontMode, cleanPrice, availableWidth);
        int y = Math.max(18, (176 - (34 * multiplier)) / 2);

        int safeCopies = Math.max(1, Math.min(99, copies));
        for (int i = 0; i < safeCopies; i++) {
            writeAscii(output, "SIZE 30 mm,22 mm\r\n");
            writeAscii(output, "GAP 6 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            if (image != null) {
                writeTsplImage(output, 8, 50, image);
            }
            if (safeItemsPerLabel > 1) {
                writeRepeatedOnlyPrices(output, cleanPrice, safeItemsPerLabel, hasImage);
            } else {
                int x = hasImage ? textX : centeredTextX(cleanPrice, multiplier, availableWidth, textX);
                writeAscii(output, "TEXT " + x + "," + y + ",\"3\",0," + multiplier + "," + multiplier + ",\"" + tsplText(cleanPrice) + "\"\r\n");
            }
            writeAscii(output, "PRINT 1\r\n");
        }
        return output.toByteArray();
    }

    private void writeRepeatedOnlyPrices(ByteArrayOutputStream output, String price, int itemsPerLabel, boolean hasImage) throws IOException {
        if (hasImage) {
            int[] ys = itemsPerLabel == 4
                    ? new int[]{20, 58, 96, 134}
                    : new int[]{8, 35, 62, 89, 116, 143};
            for (int y : ys) {
                writeAscii(output, "TEXT 84," + y + ",\"2\",0,1,1,\"" + tsplText(price) + "\"\r\n");
            }
            return;
        }

        int[] xs = new int[]{18, 128};
        int[] ys = itemsPerLabel == 4 ? new int[]{34, 108} : new int[]{20, 76, 132};
        int count = 0;
        for (int y : ys) {
            for (int x : xs) {
                if (count >= itemsPerLabel) {
                    return;
                }
                writeAscii(output, "TEXT " + x + "," + y + ",\"2\",0,1,1,\"" + tsplText(price) + "\"\r\n");
                count++;
            }
        }
    }

    private byte[] buildTextOnlyTsplPayload(String text, int copies) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TextOnlyLayout layout = layoutTextOnly(text);
        int safeCopies = Math.max(1, Math.min(99, copies));
        int lineHeight = 30 * layout.multiplier;
        int totalHeight = lineHeight * layout.lines.size();
        int startY = Math.max(8, (176 - totalHeight) / 2);

        for (int i = 0; i < safeCopies; i++) {
            writeAscii(output, "SIZE 30 mm,22 mm\r\n");
            writeAscii(output, "GAP 6 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            for (int lineIndex = 0; lineIndex < layout.lines.size(); lineIndex++) {
                String line = layout.lines.get(lineIndex);
                int x = centeredTextX(line, layout.multiplier, 224, 8);
                int y = startY + (lineIndex * lineHeight);
                writeAscii(output, "TEXT " + x + "," + y + ",\"3\",0," + layout.multiplier + "," + layout.multiplier + ",\"" + tsplText(line) + "\"\r\n");
            }
            writeAscii(output, "PRINT 1\r\n");
        }
        return output.toByteArray();
    }

    private TextOnlyLayout layoutTextOnly(String text) {
        String clean = cleanPrinterText(text).replace('\r', '\n').trim();
        for (int multiplier = 3; multiplier >= 1; multiplier--) {
            int maxChars = Math.max(6, 22 / multiplier);
            List<String> lines = wrapMultiLineText(clean, maxChars);
            int maxLines = Math.max(1, 156 / (30 * multiplier));
            if (lines.size() <= maxLines) {
                return new TextOnlyLayout(multiplier, lines);
            }
        }

        List<String> lines = wrapMultiLineText(clean, 22);
        int limit = Math.min(5, lines.size());
        return new TextOnlyLayout(1, new ArrayList<>(lines.subList(0, limit)));
    }

    private List<String> wrapMultiLineText(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        String[] rawLines = text.split("\\n+");
        for (String rawLine : rawLines) {
            List<String> wrapped = wrapText(rawLine.trim(), maxChars);
            if (wrapped.isEmpty() && !rawLine.trim().isEmpty()) {
                lines.add(rawLine.trim());
            } else {
                lines.addAll(wrapped);
            }
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private byte[] buildImageOnlyTsplPayload(TsplImage image, int copies) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int safeCopies = Math.max(1, Math.min(99, copies));
        int x = Math.max(0, (240 - image.widthPixels) / 2);
        int y = Math.max(0, (176 - image.height) / 2);

        for (int i = 0; i < safeCopies; i++) {
            writeAscii(output, "SIZE 30 mm,22 mm\r\n");
            writeAscii(output, "GAP 6 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            writeTsplImage(output, x, y, image);
            writeAscii(output, "PRINT 1\r\n");
        }
        return output.toByteArray();
    }

    private byte[] buildPhoto50x30TsplPayload(TsplImage image, int copies) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int safeCopies = Math.max(1, Math.min(99, copies));
        int x = Math.max(0, (400 - image.widthPixels) / 2);
        int y = Math.max(0, (240 - image.height) / 2);

        for (int i = 0; i < safeCopies; i++) {
            writeAscii(output, "SIZE 50 mm,30 mm\r\n");
            writeAscii(output, "GAP 6 mm,0 mm\r\n");
            writeAscii(output, "DIRECTION 1\r\n");
            writeAscii(output, "CLS\r\n");
            writeTsplImage(output, x, y, image);
            writeAscii(output, "PRINT 1\r\n");
        }
        return output.toByteArray();
    }

    private int onlyPriceMultiplier(int fontMode, String price, int availableWidth) {
        int desired = switch (fontMode) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 2;
            case 3 -> 3;
            default -> 4;
        };
        int multiplier = Math.max(1, Math.min(4, desired));
        while (multiplier > 1 && estimatedTextWidth(price, multiplier) > availableWidth) {
            multiplier--;
        }
        return multiplier;
    }

    private int centeredTextX(String text, int multiplier, int availableWidth, int leftX) {
        int textWidth = estimatedTextWidth(text, multiplier);
        return Math.max(leftX, leftX + ((availableWidth - textWidth) / 2));
    }

    private int estimatedTextWidth(String text, int multiplier) {
        return Math.max(1, cleanPrinterText(text).length()) * 10 * multiplier;
    }

    private String formatPrice(String value) {
        String clean = cleanPrinterText(value).trim();
        if (clean.toLowerCase(Locale.ROOT).startsWith("r$") || clean.contains("$")) {
            return clean;
        }
        return "R$ " + clean;
    }

    private TsplImage buildTsplImage(Uri uri) throws IOException {
        return buildTsplImage(uri, 56, 64);
    }

    private TsplImage buildFullLabelTsplImage(Uri uri) throws IOException {
        return buildTsplImage(uri, 224, 160);
    }

    private TsplImage buildPhoto50x30TsplImage(Uri uri) throws IOException {
        // Use 384 pixel width to match the working ESC/POS test payload and printer width.
        return buildTsplImage(uri, 384, 240);
    }

    private TsplImage buildTsplImage(Uri uri, int maxWidth, int maxHeight) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("Nao foi possivel ler a imagem.");
        }

        int sample = 1;
        while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) {
            sample *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        Bitmap source;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            source = BitmapFactory.decodeStream(input, null, options);
        }
        if (source == null) {
            throw new IOException("Imagem invalida.");
        }

        float scale = Math.min(maxWidth / (float) source.getWidth(), maxHeight / (float) source.getHeight());
        scale = Math.min(1f, scale);
        int width = Math.max(1, Math.round(source.getWidth() * scale));
        int height = Math.max(1, Math.round(source.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(source, width, height, true);
        if (scaled != source) {
            source.recycle();
        }

        int widthBytes = (width + 7) / 8;
        byte[] data = new byte[widthBytes * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = scaled.getPixel(x, y);
                int alpha = Color.alpha(pixel);
                int luminance = (Color.red(pixel) * 299 + Color.green(pixel) * 587 + Color.blue(pixel) * 114) / 1000;
                if (alpha > 80 && luminance < 180) {
                    int index = y * widthBytes + (x / 8);
                    data[index] |= (byte) (0x80 >> (x % 8));
                }
            }
        }
        scaled.recycle();
        return new TsplImage(width, widthBytes, height, data);
    }

    private static void writeTsplImage(ByteArrayOutputStream output, int x, int y, TsplImage image) throws IOException {
        writeAscii(output, "BITMAP " + x + "," + y + "," + image.widthBytes + "," + image.height + ",0,");
        output.write(image.data);
        writeAscii(output, "\r\n");
    }

    private byte[] buildPhoto50x30EscPosPayload(TsplImage image, int copies) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int widthBytes = image.widthBytes;
        int height = image.height;
        byte m = 0x00; // mode 0 (normal)
        int xL = widthBytes & 0xFF;
        int xH = (widthBytes >> 8) & 0xFF;
        int yL = height & 0xFF;
        int yH = (height >> 8) & 0xFF;
        for (int c = 0; c < Math.max(1, copies); c++) {
            out.write(0x1D); // GS
            out.write(0x76); // 'v'
            out.write(0x30); // '0'
            out.write(m);
            out.write(xL);
            out.write(xH);
            out.write(yL);
            out.write(yH);
            out.write(image.data);
        }
        return out.toByteArray();
    }

    private static String tsplText(String value) {
        return cleanPrinterText(value).replace("\\", "/").replace("\"", "'");
    }

    private byte[] buildCpclPayload() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeAscii(output, "! 0 200 200 176 1\r\n");
        writeAscii(output, "PAGE-WIDTH 240\r\n");
        writeAscii(output, "TEXT 4 0 10 10 CPCL MOUSSE\r\n");
        writeAscii(output, "TEXT 4 0 10 60 Fab: 19/06/2026\r\n");
        writeAscii(output, "TEXT 4 0 10 100 Val: 24/06/2026\r\n");
        writeAscii(output, "FORM\r\n");
        writeAscii(output, "PRINT\r\n");
        return output.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream output, String value) throws IOException {
        output.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static void writeLf(ByteArrayOutputStream output) throws IOException {
        output.write(0x0A);
    }

    private static void writeCrLf(ByteArrayOutputStream output) throws IOException {
        output.write(new byte[]{0x0D, 0x0A});
    }

    private static List<String> wrapText(String text, int maxLength) {
        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        String line = "";

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (line.isEmpty()) {
                line = word;
            } else if (line.length() + 1 + word.length() <= maxLength) {
                line += " " + word;
            } else {
                lines.add(line);
                line = word;
            }
        }

        if (!line.isEmpty()) {
            lines.add(line.length() <= maxLength ? line : line.substring(0, maxLength));
        }

        return lines;
    }

    private static String cleanPrinterText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }
            builder.append(c <= 127 ? c : ' ');
        }
        return builder.toString().trim();
    }

    private Date addRule(Date start, ValidityRule rule) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        if (rule.usesHours()) {
            calendar.add(Calendar.HOUR_OF_DAY, rule.validityHours);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DAY_OF_YEAR, rule.validityDays > 0 ? rule.validityDays : 5);
        }
        return calendar.getTime();
    }

    private ValidityRule resolveRule(String produto) {
        String cleanProduct = cleanPrinterText(produto).toLowerCase(Locale.ROOT);
        for (ValidityRule rule : rules) {
            if (rule.matches(cleanProduct)) {
                return rule;
            }
        }
        return DEFAULT_RULES.get(DEFAULT_RULES.size() - 1);
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice findPrinter() throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Bluetooth nao disponivel neste celular.");
        }
        if (!adapter.isEnabled()) {
            throw new IOException("Bluetooth desligado.");
        }

        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            String address = device.getAddress();
            String name = device.getName();
            if (isKnownB1Address(address) || looksLikeSelectedPrinter(name)) {
                return device;
            }
        }

        throw new IOException("PT260 ou NIIMBOT B1 nao encontrada nos dispositivos pareados.");
    }

    @SuppressLint("MissingPermission")
    private PrinterConnection connectToDevice(BluetoothDevice device, String selectedMode) throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && hasBluetoothScanPermission()) {
            adapter.cancelDiscovery();
        }

        List<String> modes = PrinterConnectionUtils.buildConnectionModes(selectedMode);

        IOException lastError = null;
        for (String mode : modes) {
            try {
                BluetoothSocket socket = openSocket(device, mode);
                socket.connect();
                Log.i(TAG, "Conectado via " + mode);
                return new PrinterConnection(socket, mode);
            } catch (Exception e) {
                Log.w(TAG, "Falha " + mode, e);
                if (e instanceof IOException) {
                    lastError = (IOException) e;
                } else {
                    lastError = new IOException(e);
                }
            }
        }

        throw lastError == null ? new IOException("Nao foi possivel conectar na impressora.") : lastError;
    }

    private BluetoothSocket openSocket(BluetoothDevice device, String mode) throws Exception {
        if (mode.startsWith("Canal ")) {
            int channel = Integer.parseInt(mode.substring("Canal ".length()));
            Method method = device.getClass().getMethod("createRfcommSocket", int.class);
            return (BluetoothSocket) method.invoke(device, channel);
        }

        if ("Seguro".equals(mode)) {
            return device.createRfcommSocketToServiceRecord(SPP_UUID);
        }

        return device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
    }

    private String selectedBluetoothMode() {
        if (metodoSpinner == null || metodoSpinner.getSelectedItem() == null) {
            return "Automatico";
        }
        return metodoSpinner.getSelectedItem().toString();
    }

    private static boolean looksLikePrinter(String name) {
        if (name == null) {
            return false;
        }
        String value = name.toLowerCase(Locale.ROOT);
        return looksLikePt260(value) || looksLikeNiimbotB1(value);
    }

    private boolean looksLikeSelectedPrinter(String name) {
        String value = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (PRINTER_MODEL_B1.equals(selectedPrinterModel())) {
            return looksLikeNiimbotB1(value);
        }
        if (PRINTER_MODEL_PT260.equals(selectedPrinterModel())) {
            return looksLikePt260(value);
        }
        return looksLikePrinter(value);
    }

    private static boolean looksLikePt260(String value) {
        return value != null
                && (value.contains("pt260")
                || value.contains("pt-260")
                || value.contains("xd210")
                || value.contains("xd-210"));
    }

    private static boolean looksLikeNiimbotB1(String value) {
        return PrinterConnectionUtils.looksLikeNiimbotB1(value);
    }

    private static String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        return address.replace(":", "").replace("-", "").toLowerCase(Locale.ROOT);
    }

    private static boolean isKnownB1Address(String address) {
        return normalizeAddress(address).equals(normalizeAddress(NIIMBOT_B1_MAC));
    }

    private void refreshPrinterStatus() {
        if (!hasBluetoothPermission()) {
            setStatus("Permissao Bluetooth pendente.");
            return;
        }

        try {
            BluetoothDevice device = findPrinter();
            setStatus("Impressora detectada: " + device.getName());
        } catch (Exception e) {
            setStatus(e.getMessage() == null ? "Impressora nao detectada." : e.getMessage());
        }
    }

    private void startOcrCameraIfReady() {
        if (ocrPreviewView == null || !isBetaUnlocked()) {
            return;
        }
        if (!hasCameraPermission()) {
            requestCameraPermission();
            return;
        }
        if (ocrCameraRunning) {
            return;
        }

        if (ocrExecutor == null) {
            ocrExecutor = Executors.newSingleThreadExecutor();
        }
        if (textRecognizer == null) {
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        }

        updateOcrStatus("OCR ativo. Camera " + (ocrUseFrontCamera ? "frontal" : "traseira") + ".");
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                cameraProvider = providerFuture.get();
                bindOcrCamera(true);
            } catch (Exception e) {
                updateOcrStatus("Nao foi possivel abrir a camera OCR.");
                Log.w(TAG, "Falha ao abrir camera OCR", e);
            }
        }, this::runOnUiThread);
    }

    private void bindOcrCamera(boolean allowFallback) {
        if (cameraProvider == null || ocrPreviewView == null || ocrExecutor == null) {
            return;
        }
        if (ocrPage == null || ocrPage.getVisibility() != View.VISIBLE) {
            return;
        }

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(ocrPreviewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(ocrExecutor, this::analyzeOcrFrame);

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(ocrUseFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, selector, preview, analysis);
            ocrCameraRunning = true;
            updateOcrStatus("OCR ativo. Camera " + (ocrUseFrontCamera ? "frontal" : "traseira") + ".");
        } catch (IllegalArgumentException e) {
            if (allowFallback && ocrUseFrontCamera) {
                ocrUseFrontCamera = false;
                bindOcrCamera(false);
                return;
            }
            updateOcrStatus("Camera OCR indisponivel neste aparelho.");
            Log.w(TAG, "Camera OCR indisponivel", e);
        }
    }

    private void stopOcrCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        ocrCameraRunning = false;
        ocrFrameBusy = false;
    }

    private void switchOcrCamera() {
        ocrUseFrontCamera = !ocrUseFrontCamera;
        stopOcrCamera();
        startOcrCameraIfReady();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeOcrFrame(ImageProxy imageProxy) {
        long now = System.currentTimeMillis();
        if (ocrFrameBusy || now - lastOcrScanMs < OCR_SCAN_INTERVAL_MS || textRecognizer == null) {
            imageProxy.close();
            return;
        }

        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        ocrFrameBusy = true;
        lastOcrScanMs = now;
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        textRecognizer.process(image)
                .addOnSuccessListener(text -> handleOcrText(text))
                .addOnFailureListener(e -> {
                    updateOcrStatus("OCR nao conseguiu ler esta imagem.");
                    Log.w(TAG, "Falha no OCR", e);
                })
                .addOnCompleteListener(task -> {
                    ocrFrameBusy = false;
                    imageProxy.close();
                });
    }

    private void handleOcrText(Text text) {
        OcrScanResult result = analyzeOcrText(text == null ? "" : text.getText());
        runOnUiThread(() -> applyOcrScanResult(result));
    }

    private OcrScanResult analyzeOcrText(String rawText) {
        String cleanText = cleanPrinterText(rawText == null ? "" : rawText).replace('\r', '\n').trim();
        if (cleanText.length() < 5) {
            return null;
        }

        String[] lines = cleanText.split("\\n+");
        List<DateCandidate> candidates = new ArrayList<>();
        int order = 0;
        for (String line : lines) {
            String cleanLine = cleanPrinterText(line).trim();
            if (cleanLine.isEmpty()) {
                continue;
            }
            String lower = cleanLine.toLowerCase(Locale.ROOT);
            boolean lineExpiryHint = containsOcrExpiryKeyword(lower);
            boolean lineStartHint = containsOcrStartKeyword(lower);
            Matcher matcher = OCR_DATE_PATTERN.matcher(cleanLine);
            while (matcher.find()) {
                long parsed = parseOcrDate(matcher.group(1), matcher.group(2), matcher.group(3));
                if (parsed > 0) {
                    String before = lower.substring(Math.max(0, matcher.start() - 32), matcher.start());
                    String after = lower.substring(matcher.end(), Math.min(lower.length(), matcher.end() + 14));
                    String context = before + " " + after;
                    boolean expiryHint = containsOcrExpiryKeyword(context);
                    boolean startHint = containsOcrStartKeyword(context);
                    if (!expiryHint && !startHint) {
                        expiryHint = lineExpiryHint && !lineStartHint;
                        startHint = lineStartHint && !lineExpiryHint;
                    }
                    candidates.add(new DateCandidate(parsed, cleanLine, expiryHint, startHint, order++));
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        List<DateCandidate> expiryCandidates = new ArrayList<>();
        List<DateCandidate> startCandidates = new ArrayList<>();
        for (DateCandidate candidate : candidates) {
            if (candidate.expiryHint && !candidate.startHint) {
                expiryCandidates.add(candidate);
            }
            if (candidate.startHint && !candidate.expiryHint) {
                startCandidates.add(candidate);
            }
        }

        if (expiryCandidates.isEmpty()) {
            return OcrScanResult.incomplete("Validade nao detectada.", cleanText);
        }
        if (startCandidates.isEmpty()) {
            return OcrScanResult.incomplete("Fabricacao, abertura ou pronto nao detectado.", cleanText);
        }

        DateCandidate expiry = chooseLatestDate(expiryCandidates);
        DateCandidate start = chooseStartDate(startCandidates, expiry.dateAt);

        if (start == null) {
            return OcrScanResult.incomplete("Data inicial incoerente com a validade.", cleanText);
        }

        String product = inferOcrProductName(lines);
        long todayStart = startOfTodayMillis();
        boolean danger = expiry.dateAt <= todayStart;
        String status = danger
                ? (expiry.dateAt < todayStart ? "VENCIDO" : "VENCE HOJE")
                : "DENTRO DA VALIDADE";
        return OcrScanResult.complete(product, start.dateAt, expiry.dateAt, danger, status, cleanText);
    }

    private DateCandidate chooseLatestDate(List<DateCandidate> candidates) {
        DateCandidate chosen = null;
        for (DateCandidate candidate : candidates) {
            if (chosen == null
                    || candidate.dateAt > chosen.dateAt
                    || (candidate.dateAt == chosen.dateAt && candidate.order > chosen.order)) {
                chosen = candidate;
            }
        }
        return chosen;
    }

    private DateCandidate chooseStartDate(List<DateCandidate> candidates, long expiryAt) {
        DateCandidate chosen = null;
        for (DateCandidate candidate : candidates) {
            if (candidate.dateAt > expiryAt) {
                continue;
            }
            if (chosen == null
                    || candidate.dateAt > chosen.dateAt
                    || (candidate.dateAt == chosen.dateAt && candidate.order > chosen.order)) {
                chosen = candidate;
            }
        }
        return chosen;
    }

    private boolean containsOcrExpiryKeyword(String line) {
        return line.contains("validade")
                || line.contains("valid")
                || line.contains("venc")
                || line.contains("vcto")
                || line.contains("vence")
                || line.contains("consumir ate")
                || line.contains("usar ate")
                || line.contains("val:")
                || line.startsWith("val ")
                || line.contains(" val ")
                || line.contains("val.")
                || line.contains("val-");
    }

    private boolean containsOcrStartKeyword(String line) {
        return line.contains("fabric")
                || line.contains("fab:")
                || line.startsWith("fab ")
                || line.contains("abert")
                || line.contains("abertura")
                || line.contains("pront")
                || line.contains("preparo")
                || line.contains("produz")
                || line.contains("manipul");
    }

    private long parseOcrDate(String dayText, String monthText, String yearText) {
        try {
            int day = Integer.parseInt(dayText);
            int month = Integer.parseInt(monthText);
            Calendar calendar = Calendar.getInstance();
            int year;
            if (yearText == null || yearText.trim().isEmpty()) {
                year = calendar.get(Calendar.YEAR);
            } else {
                if (yearText.trim().length() == 3) {
                    return 0;
                }
                year = Integer.parseInt(yearText);
                if (year < 100) {
                    year += year >= 70 ? 1900 : 2000;
                }
            }
            if (year < 2000 || year > 2100) {
                return 0;
            }

            calendar.clear();
            calendar.setLenient(false);
            calendar.set(year, month - 1, day, 0, 0, 0);
            return calendar.getTimeInMillis();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String inferOcrProductName(String[] lines) {
        for (String line : lines) {
            String cleanLine = cleanPrinterText(line).trim();
            if (cleanLine.length() < 3 || OCR_DATE_PATTERN.matcher(cleanLine).find()) {
                continue;
            }
            String lower = cleanLine.toLowerCase(Locale.ROOT);
            if (containsOcrExpiryKeyword(lower) || containsOcrStartKeyword(lower)) {
                continue;
            }
            if (lower.startsWith("lote") || lower.startsWith("preco") || lower.startsWith("preço")) {
                continue;
            }
            int digits = 0;
            for (int i = 0; i < cleanLine.length(); i++) {
                if (Character.isDigit(cleanLine.charAt(i))) {
                    digits++;
                }
            }
            if (digits > cleanLine.length() / 2) {
                continue;
            }
            return cleanLine.length() <= 40 ? cleanLine : cleanLine.substring(0, 40).trim();
        }
        return "Produto OCR";
    }

    private void applyOcrScanResult(OcrScanResult result) {
        if (result == null) {
            if (ocrStatusText != null && ocrCameraRunning) {
                ocrStatusText.setText("OCR ativo. Buscando validade clara.");
            }
            return;
        }

        String signature = result.status + "|" + result.expiryAt + "|" + result.product;
        long now = System.currentTimeMillis();
        if (signature.equals(lastOcrSignature) && now - lastOcrHandledMs < OCR_DUPLICATE_INTERVAL_MS) {
            return;
        }
        lastOcrSignature = signature;
        lastOcrHandledMs = now;
        syncOcrCheckAsync(result);

        if (!result.complete) {
            playOcrAlertBeeps();
            updateOcrStatus(result.status);
            updateOcrResult(result.status + "\nConfira se a etiqueta tem data inicial e validade.");
            setStatus("OCR incompleto: " + result.status);
            return;
        }

        if (result.danger) {
            recordOcrAlert(result);
            playOcrAlertBeeps();
            updateOcrStatus(result.status + ": remova o produto imediatamente.");
            updateOcrResult(result.product
                    + "\nInicio: " + dateFormat.format(new Date(result.startAt))
                    + "\nValidade: " + dateFormat.format(new Date(result.expiryAt))
                    + "\nObrigado.");
            setStatus("OCR registrou recolhimento em Avisos.");
        } else {
            playOcrOkBeep();
            updateOcrStatus("Produto dentro da validade.");
            updateOcrResult(result.product
                    + "\nInicio: " + dateFormat.format(new Date(result.startAt))
                    + "\nValidade: " + dateFormat.format(new Date(result.expiryAt)));
        }
    }

    private void updateOcrStatus(String message) {
        runOnUiThread(() -> {
            if (ocrStatusText != null) {
                ocrStatusText.setText(message);
            }
        });
    }

    private void updateOcrResult(String message) {
        runOnUiThread(() -> {
            if (ocrResultText != null) {
                ocrResultText.setText(message);
            }
        });
    }

    private ToneGenerator getOcrTone() {
        if (ocrTone == null) {
            try {
                ocrTone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            } catch (RuntimeException e) {
                Log.w(TAG, "Nao foi possivel criar beep OCR", e);
            }
        }
        return ocrTone;
    }

    private void playOcrOkBeep() {
        ToneGenerator tone = getOcrTone();
        if (tone != null) {
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 130);
        }
    }

    private void playOcrAlertBeeps() {
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                ToneGenerator tone = getOcrTone();
                if (tone != null) {
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 120);
                }
                try {
                    Thread.sleep(170);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void rememberPrinterDevice(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String name = device.getName();
            String address = device.getAddress();
            lastPrinterName = name == null ? "" : name;
            lastPrinterAddressHash = sha256(address == null ? "" : address);
            if (address == null || address.length() < 4) {
                lastPrinterAddressLast4 = "";
            } else {
                lastPrinterAddressLast4 = address.substring(address.length() - 4);
            }
        } catch (SecurityException e) {
            Log.w(TAG, "Sem permissao para ler dados da impressora", e);
        }
    }

    private boolean hasBluetoothPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasBluetoothScanPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermissionIfNeeded() {
        if (!hasBluetoothPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_CONNECT
            );
        }
    }

    private boolean hasCameraPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            updateOcrStatus("Permissao da camera pendente.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            refreshPrinterStatus();
        } else if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            maybeShowExpiryReminder(false);
        } else if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startOcrCameraIfReady();
            } else {
                updateOcrStatus("Camera negada. OCR pausado.");
            }
        }
    }

    private void setStatus(String message) {
        runOnUiThread(() -> statusText.setText(message));
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            statusText.setText("Erro: " + message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private TextWatcher simpleTextWatcher(Runnable afterChanged) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                afterChanged.run();
            }
        };
    }

    private static class OcrAlertEntry {
        final long scannedAt;
        final String product;
        final long startAt;
        final long expiryAt;
        final String rawText;

        OcrAlertEntry(long scannedAt, String product, long startAt, long expiryAt, String rawText) {
            this.scannedAt = scannedAt;
            this.product = product == null || product.trim().isEmpty() ? "Produto OCR" : product;
            this.startAt = startAt;
            this.expiryAt = expiryAt;
            this.rawText = rawText == null ? "" : rawText;
        }
    }

    private static class OcrScanResult {
        final String product;
        final long startAt;
        final long expiryAt;
        final boolean danger;
        final boolean complete;
        final String status;
        final String rawText;

        private OcrScanResult(String product, long startAt, long expiryAt, boolean danger, boolean complete, String status, String rawText) {
            this.product = product == null || product.trim().isEmpty() ? "Produto OCR" : product;
            this.startAt = startAt;
            this.expiryAt = expiryAt;
            this.danger = danger;
            this.complete = complete;
            this.status = status == null ? "" : status;
            this.rawText = rawText == null ? "" : rawText;
        }

        static OcrScanResult complete(String product, long startAt, long expiryAt, boolean danger, String status, String rawText) {
            return new OcrScanResult(product, startAt, expiryAt, danger, true, status, rawText);
        }

        static OcrScanResult incomplete(String status, String rawText) {
            return new OcrScanResult("Produto OCR", 0, 0, false, false, status, rawText);
        }
    }

    private static class DateCandidate {
        final long dateAt;
        final String line;
        final boolean expiryHint;
        final boolean startHint;
        final int order;

        DateCandidate(long dateAt, String line, boolean expiryHint, boolean startHint, int order) {
            this.dateAt = dateAt;
            this.line = line == null ? "" : line;
            this.expiryHint = expiryHint;
            this.startHint = startHint;
            this.order = order;
        }
    }

    private static class TextOnlyLayout {
        final int multiplier;
        final List<String> lines;

        TextOnlyLayout(int multiplier, List<String> lines) {
            this.multiplier = multiplier;
            this.lines = lines;
        }
    }

    private static class PrintHistoryEntry {
        final long printedAt;
        final String product;
        final int copies;
        final long startAt;
        final long expiryAt;
        final String validityLabel;
        final boolean usesHours;

        PrintHistoryEntry(long printedAt, String product, int copies, long startAt, long expiryAt, String validityLabel, boolean usesHours) {
            this.printedAt = printedAt;
            this.product = product;
            this.copies = copies;
            this.startAt = startAt;
            this.expiryAt = expiryAt;
            this.validityLabel = validityLabel == null ? "" : validityLabel;
            this.usesHours = usesHours;
        }
    }

    private static class PrintSummary {
        final String product;
        final long expiryAt;
        int totalCopies;
        long latestPrintedAt;
        String validityLabel;

        PrintSummary(String product, long expiryAt, String validityLabel) {
            this.product = product;
            this.expiryAt = expiryAt;
            this.validityLabel = validityLabel == null ? "" : validityLabel;
        }
    }

    private static class PrintLines {
        final String title;
        final String startLine;
        final String validadeLine;
        final String statusValidade;

        PrintLines(String title, String startLine, String validadeLine, String statusValidade) {
            this.title = title;
            this.startLine = startLine;
            this.validadeLine = validadeLine;
            this.statusValidade = statusValidade;
        }
    }

    private static class PeixariaEntry {
        final long printedAt;
        final String lot;
        final String product;
        final String weightKg;
        final int copies;
        final long expiryAt;

        PeixariaEntry(long printedAt, String lot, String product, String weightKg, int copies, long expiryAt) {
            this.printedAt = printedAt;
            this.lot = lot == null ? "" : lot;
            this.product = product == null ? "" : product;
            this.weightKg = weightKg == null ? "" : weightKg;
            this.copies = Math.max(1, copies);
            this.expiryAt = expiryAt;
        }
    }

    private static class TsplImage {
        final int widthPixels;
        final int widthBytes;
        final int height;
        final byte[] data;

        TsplImage(int widthPixels, int widthBytes, int height, byte[] data) {
            this.widthPixels = widthPixels;
            this.widthBytes = widthBytes;
            this.height = height;
            this.data = data;
        }
    }

    private static class CatalogData {
        final List<String> products;
        final List<ValidityRule> rules;
        final java.util.Map<String, List<String>> categories;

        CatalogData(List<String> products, List<ValidityRule> rules) {
            this(products, rules, new java.util.LinkedHashMap<>());
        }

        CatalogData(List<String> products, List<ValidityRule> rules, java.util.Map<String, List<String>> categories) {
            this.products = products;
            this.rules = rules;
            this.categories = categories;
        }
    }

    private static class ValidityRule {
        final String match;
        final int validityDays;
        final int validityHours;
        final String startLabel;

        ValidityRule(String match, int validityDays, int validityHours, String startLabel) {
            this.match = match;
            this.validityDays = validityDays;
            this.validityHours = validityHours;
            this.startLabel = startLabel;
        }

        boolean usesHours() {
            return validityHours > 0;
        }

        boolean matches(String cleanProduct) {
            return "*".equals(match)
                    || cleanProduct.contains(cleanPrinterText(match).toLowerCase(Locale.ROOT));
        }

        String displayDuration() {
            return usesHours() ? validityHours + " horas" : (validityDays > 0 ? validityDays : 5) + " dias";
        }
    }

    private static class PrinterConnection {
        final BluetoothSocket socket;
        final String method;

        PrinterConnection(BluetoothSocket socket, String method) {
            this.socket = socket;
            this.method = method;
        }
    }

    private abstract static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
            onSelected(position);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }

        public abstract void onSelected(int position);
    }
}

