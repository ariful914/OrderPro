package com.arif.smartordermanagement.data.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class PrefUtils {
    private static final String PREFS_NAME = "orderpro_private_preferences";
    private static final String KEY_ALIAS = "orderpro_credentials_key";
    private static final String USER_INFO = "user_info";
    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String PERMISSION_STATUS = "permission_status";
    private static final String MARKER_DATA = "markerData";
    private static final String VISIT_TIME = "visit_time";
    private static final String KEY_TRACKING_ENABLED = "KEY_TRACKING_ENABLED";
    private static final String KEY_STORE_SERVICE_ENABLED = "KEY_STORE_SERVICE";
    private static final String TOKEN = "login_token";
    private static final String KEY_FIRST_LAUNCH = "KEY_FIRST_LAUNCH";
    private static final String KEY_LAST_UPLOAD_TIME = "KEY_LAST_UPLOAD_TIME";
    private static final String KEY_REMEMBER_ME = "KEY_REMEMBER_ME";
    private static final String KEY_SAVED_USERNAME = "KEY_SAVED_USERNAME";
    private static final String KEY_SAVED_PASSWORD = "KEY_SAVED_PASSWORD";
    private static final String SESSION_EXPIRES_AT = "session_expires_at";

    private static volatile PrefUtils instance;
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    private PrefUtils(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static PrefUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (PrefUtils.class) {
                if (instance == null) instance = new PrefUtils(context);
            }
        }
        return instance;
    }

    public synchronized <T> void setUserInfo(T userInfo) {
        preferences.edit().putString(USER_INFO, gson.toJson(userInfo)).apply();
    }

    public <T> T getUserInfo(Class<T> type) {
        String json = preferences.getString(USER_INFO, null);
        return json == null ? null : gson.fromJson(json, type);
    }

    public <T> T getUserInfo(TypeToken<T> typeToken) {
        String json = preferences.getString(USER_INFO, null);
        return json == null ? null : gson.fromJson(json, typeToken.getType());
    }

    public void setToken(String token) {
        String encrypted = token == null || token.isEmpty() ? "" : encrypt(token);
        preferences.edit().putString(TOKEN, encrypted).apply();
    }

    public String getToken() {
        String encrypted = preferences.getString(TOKEN, "");
        if (encrypted.isEmpty()) return "";
        try {
            return decrypt(encrypted);
        } catch (Exception invalidOrLegacyToken) {
            preferences.edit().remove(TOKEN).apply();
            return "";
        }
    }

    public void setLoginInfo(boolean loggedIn) {
        preferences.edit().putBoolean(IS_LOGGED_IN, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(IS_LOGGED_IN, false);
    }

    public void setSessionExpiresAt(long expiresAtMillis) {
        preferences.edit().putLong(SESSION_EXPIRES_AT, expiresAtMillis).apply();
    }

    public long getSessionExpiresAt() {
        return preferences.getLong(SESSION_EXPIRES_AT, 0L);
    }

    public boolean hasValidSession() {
        if (!isLoggedIn() || getSessionExpiresAt() <= System.currentTimeMillis()) return false;
        String token = getToken();
        if (token.isEmpty()) return false;
        long tokenExpiry = getTokenExpiresAtMillis(token);
        return tokenExpiry > System.currentTimeMillis();
    }

    public long getTokenExpiresAtMillis(String token) {
        if (token == null) return 0L;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return 0L;
        try {
            byte[] payload = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            long expirySeconds = new org.json.JSONObject(new String(payload, StandardCharsets.UTF_8))
                    .optLong("exp", 0L);
            return expirySeconds > 0L ? expirySeconds * 1000L : 0L;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    public void clearSession() {
        preferences.edit()
                .remove(USER_INFO)
                .remove(TOKEN)
                .remove(SESSION_EXPIRES_AT)
                .putBoolean(IS_LOGGED_IN, false)
                .apply();
    }

    public void setVisitMinTime(String visitTime) {
        preferences.edit().putString(VISIT_TIME, visitTime).apply();
    }

    public void setMarkerData(String markerData) {
        preferences.edit().putString(MARKER_DATA, markerData).apply();
    }

    public String getMarkerData() {
        return preferences.getString(MARKER_DATA, "");
    }

    public void setPermissionStatus(boolean result) {
        preferences.edit().putBoolean(PERMISSION_STATUS, result).apply();
    }

    public boolean isRememberMeEnabled() {
        return preferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void saveRememberedCredentials(String username, String password) {
        if (username == null || password == null) {
            clearRememberedCredentials();
            return;
        }
        try {
            String encryptedPassword = encrypt(password);
            preferences.edit()
                    .putBoolean(KEY_REMEMBER_ME, true)
                    .putString(KEY_SAVED_USERNAME, username)
                    .putString(KEY_SAVED_PASSWORD, encryptedPassword)
                    .apply();
        } catch (RuntimeException encryptionFailure) {
            clearRememberedCredentials();
        }
    }

    public String getSavedUsername() {
        return preferences.getString(KEY_SAVED_USERNAME, "");
    }

    public String getSavedPassword() {
        String encrypted = preferences.getString(KEY_SAVED_PASSWORD, null);
        if (encrypted == null || encrypted.isEmpty()) return "";
        try {
            return decrypt(encrypted);
        } catch (Exception e) {
            clearRememberedCredentials();
            return "";
        }
    }

    public void setRememberMeEnabled(boolean enabled) {
        if (!enabled) clearRememberedCredentials();
        else preferences.edit().putBoolean(KEY_REMEMBER_ME, true).apply();
    }

    public void clearRememberedCredentials() {
        preferences.edit()
                .putBoolean(KEY_REMEMBER_ME, false)
                .remove(KEY_SAVED_USERNAME)
                .remove(KEY_SAVED_PASSWORD)
                .apply();
    }

    private String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            ByteBuffer payload = ByteBuffer.allocate(iv.length + ciphertext.length);
            payload.put(iv).put(ciphertext);
            return Base64.encodeToString(payload.array(), Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Could not securely save remembered credentials.", e);
        }
    }

    private String decrypt(String encrypted) throws Exception {
        byte[] payload = Base64.decode(encrypted, Base64.NO_WRAP);
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        byte[] iv = new byte[12];
        buffer.get(iv);
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Key existing = keyStore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    public boolean isFirstLaunch() { return preferences.getBoolean(KEY_FIRST_LAUNCH, true); }
    public void setFirstLaunch(boolean value) { preferences.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply(); }
    public boolean isUserLoggedIn() { return isLoggedIn(); }
    public void setUserLoggedIn(boolean value) { setLoginInfo(value); }
    public void setBoolean(String key, boolean value) { preferences.edit().putBoolean(key, value).apply(); }
    public boolean getBoolean(String key, boolean defaultValue) { return preferences.getBoolean(key, defaultValue); }
    public boolean isTrackingEnabled() { return preferences.getBoolean(KEY_TRACKING_ENABLED, false); }
    public void setTrackingEnabled(boolean enabled) { preferences.edit().putBoolean(KEY_TRACKING_ENABLED, enabled).apply(); }
    public boolean isStoreServiceEnabled() { return preferences.getBoolean(KEY_STORE_SERVICE_ENABLED, false); }
    public void setStoreServiceEnabled(boolean enabled) { preferences.edit().putBoolean(KEY_STORE_SERVICE_ENABLED, enabled).apply(); }
    public long getLastUploadTime() { return preferences.getLong(KEY_LAST_UPLOAD_TIME, 0); }
    public void setLastUploadTime(long time) { preferences.edit().putLong(KEY_LAST_UPLOAD_TIME, time).apply(); }
}
