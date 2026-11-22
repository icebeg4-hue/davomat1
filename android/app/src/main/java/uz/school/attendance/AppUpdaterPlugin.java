package uz.school.attendance;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Locale;

@CapacitorPlugin(name = "AppUpdater")
public class AppUpdaterPlugin extends Plugin {
    private Uri pendingFileUri;

    @PluginMethod
    public void getInfo(PluginCall call) {
        try {
            Context context = getContext();
            String packageName = context.getPackageName();
            android.content.pm.PackageInfo info =
                context.getPackageManager().getPackageInfo(packageName, 0);
            JSObject result = new JSObject();
            result.put("versionName", info.versionName);
            result.put("versionCode", info.versionCode);
            call.resolve(result);
        } catch (Exception error) {
            call.reject(error.getMessage());
        }
    }

    @PluginMethod
    public void downloadAndInstall(PluginCall call) {
        String url = call.getString("url");
        String expectedSha256 = call.getString("sha256");
        if (url == null || url.trim().isEmpty()) {
            call.reject("APK manzili berilmadi.");
            return;
        }

        try {
            DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Maktab Davomat yangilanishi");
            request.setDescription("Yangi APK yuklanmoqda");
            request.setMimeType("application/vnd.android.package-archive");
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            File apk = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "Maktab-Davomat-update.apk");
            if (apk.exists()) apk.delete();
            request.setDestinationUri(Uri.fromFile(apk));

            DownloadManager manager =
                (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            long id = manager.enqueue(request);

            call.resolve();
            waitForDownload(manager, id, apk, expectedSha256);
        } catch (Exception error) {
            call.reject(error.getMessage());
        }
    }

    private void waitForDownload(DownloadManager manager, long id, File apk, String expectedSha256) {
        new Thread(() -> {
            boolean complete = false;
            while (!complete) {
                DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
                try (Cursor cursor = manager.query(query)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int status = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        );
                        if (status == DownloadManager.STATUS_SUCCESSFUL) complete = true;
                        if (status == DownloadManager.STATUS_FAILED) return;
                    }
                } catch (Exception ignored) {
                    return;
                }
                if (!complete) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) { return; }
                }
            }
            getActivity().runOnUiThread(() -> {
                if (!verifySha256(apk, expectedSha256)) return;
                installApk(apk);
            });
        }).start();
    }

    private boolean verifySha256(File apk, String expectedSha256) {
        if (apk == null || !apk.isFile() || expectedSha256 == null) return false;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream input = new FileInputStream(apk)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            StringBuilder hex = new StringBuilder();
            for (byte value : digest.digest()) {
                hex.append(String.format(Locale.US, "%02x", value));
            }
            return hex.toString().equals(expectedSha256.trim().toLowerCase(Locale.US));
        } catch (Exception error) {
            return false;
        }
    }

    private void installApk(File apk) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                apk
            );
        } else {
            uri = Uri.fromFile(apk);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !getContext().getPackageManager().canRequestPackageInstalls()) {
            pendingFileUri = uri;
            Intent settings = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            settings.setData(Uri.parse("package:" + getContext().getPackageName()));
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(settings);
            return;
        }
        launchInstaller(uri);
    }

    private void launchInstaller(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getContext().startActivity(intent);
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();
        if (pendingFileUri != null &&
            getContext().getPackageManager().canRequestPackageInstalls()) {
            Uri uri = pendingFileUri;
            pendingFileUri = null;
            launchInstaller(uri);
        }
    }
}
