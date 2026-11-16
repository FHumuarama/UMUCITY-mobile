package com.franchicreative.umucity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> filePicker;
    private static final int FILE_REQUEST = 1024;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        web = new WebView(this);
        web.setBackgroundColor(0xff9ac9bd);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setContentView(web);
        WebSettings cfg = web.getSettings();
        cfg.setJavaScriptEnabled(true);
        cfg.setDomStorageEnabled(true);
        cfg.setDatabaseEnabled(true);
        cfg.setAllowFileAccess(false);
        cfg.setAllowContentAccess(true);
        cfg.setMediaPlaybackRequiresUserGesture(false);
        cfg.setDefaultTextEncodingName("UTF-8");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (filePicker != null) filePicker.onReceiveValue(null);
                filePicker = cb;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/json");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try { startActivityForResult(Intent.createChooser(intent, "Importar backup UMUCITY"), FILE_REQUEST); }
                catch (Exception e) { filePicker = null; cb.onReceiveValue(null); return false; }
                return true;
            }
        });
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface public void saveBackup(String json, String fileName) {
                // Only a sanitized filename is used; backup never leaves the device.
                final String safeName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
                try {
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, safeName);
                        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/UMUCITY");
                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (uri == null) throw new Exception("Não foi possível criar arquivo");
                        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                            if (out == null) throw new Exception("Não foi possível gravar arquivo");
                            out.write(json.getBytes(StandardCharsets.UTF_8));
                        }
                    } else {
                        java.io.File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS);
                        if (dir == null) throw new Exception("Diretório indisponível");
                        try (OutputStream out = new java.io.FileOutputStream(new java.io.File(dir, safeName))) {
                            out.write(json.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Backup salvo em Downloads/UMUCITY", Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro ao salvar backup: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }, "AndroidBridge");
        try (InputStream in = getAssets().open("index.html")) {
            byte[] data = new byte[in.available()];
            int count = 0;
            while (count < data.length) {
                int n = in.read(data, count, data.length - count);
                if (n < 0) break;
                count += n;
            }
            String html = new String(data, 0, count, StandardCharsets.UTF_8);
            // Stable HTTPS-like local origin makes localStorage persistent and avoids file:// restrictions.
            web.loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar UMUCITY: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST && filePicker != null) {
            filePicker.onReceiveValue(resultCode == RESULT_OK && data != null && data.getData() != null
                    ? new Uri[]{data.getData()} : null);
            filePicker = null;
        }
    }
    @Override public void onBackPressed() {
        // Android hardware back is ignored so taps cannot accidentally exit and lose progress.
        web.evaluateJavascript("(function(){var m=document.querySelector('#overlay:not(.hidden)');if(m){var b=m.querySelector('.modal-head button');if(b)b.click();return;}var sh=document.querySelector('.sheet:not(.hidden)');if(sh){var b=sh.querySelector('.close-sheet');if(b)b.click();}})()", null);
    }
    @Override protected void onPause() { web.evaluateJavascript("try{if(typeof save==='function')save()}catch(e){}", null); super.onPause(); }
    @Override protected void onDestroy() { if (web != null) web.destroy(); super.onDestroy(); }
}
