package uz.school.attendance;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.OutputStream;

@CapacitorPlugin(name = "AppUpdater")
public class AppUpdaterPlugin extends Plugin {
    @PluginMethod
    public void openUpdate(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.trim().isEmpty()) {
            call.reject("Yangilanish manzili topilmadi.");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception error) {
            call.reject(error.getMessage());
        }
    }
    @PluginMethod
    public void getInfo(PluginCall call) {
        try {
            Context context = getContext();
            android.content.pm.PackageInfo info =
                context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0
                );
            JSObject result = new JSObject();
            result.put("versionName", info.versionName);
            result.put("versionCode", info.versionCode);
            call.resolve(result);
        } catch (Exception error) {
            call.reject(error.getMessage());
        }
    }

    @PluginMethod
    public void saveFile(PluginCall call) {
        String fileName = call.getString("fileName");
        String base64 = call.getString("base64");
        if (fileName == null || base64 == null) {
            call.reject("Fayl ma’lumotlari to‘liq emas.");
            return;
        }

        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                uri = getContext().getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                );
                if (uri == null) throw new Exception("Fayl yaratilmadi.");
                try (OutputStream output =
                         getContext().getContentResolver().openOutputStream(uri)) {
                    if (output == null) throw new Exception("Faylga yozib bo‘lmadi.");
                    output.write(bytes);
                    output.flush();
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContext().getContentResolver().update(uri, values, null, null);
            } else {
                File downloads = getContext().getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS
                );
                if (downloads == null) throw new Exception("Downloads papkasi ochilmadi.");
                if (!downloads.exists() && !downloads.mkdirs()) {
                    throw new Exception("Downloads papkasi ochilmadi.");
                }
                File file = new File(downloads, fileName);
                try (java.io.FileOutputStream output =
                         new java.io.FileOutputStream(file)) {
                    output.write(bytes);
                }
                uri = Uri.fromFile(file);
            }

            JSObject result = new JSObject();
            result.put("fileName", fileName);
            result.put("uri", uri.toString());
            call.resolve(result);
        } catch (Exception error) {
            call.reject(error.getMessage());
        }
    }
}
