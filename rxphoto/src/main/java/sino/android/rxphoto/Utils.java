package sino.android.rxphoto;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public final class Utils {
    private Utils() {
    }

    public static boolean isAndroidQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && !Environment.isExternalStorageLegacy();
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static Uri createUri(Context context) {
        Objects.requireNonNull(context);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            // java.lang.UnsupportedOperationException: Writing to internal storage is not supported.
            return resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    public static File createPublicFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String timestamp = formatter.format(new Date());
        String filename = String.format("JPEG_%s.jpg", timestamp);
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(directory, filename);
    }

    public static File createFile(Context context) {
        Objects.requireNonNull(context);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String timestamp = formatter.format(new Date());
        String filename = String.format("JPEG_%s.jpg", timestamp);
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(directory, filename);
    }

    /**
     * Android 10，复制到应用沙盒
     *
     * @param context
     * @param uri
     * @return filepath
     * @throws IOException
     */
    public static String asFilePath(Context context, Uri uri) throws IOException {
        Objects.requireNonNull(context);
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        Objects.requireNonNull(inputStream);
        File file = Utils.createFile(context);
        OutputStream outputStream = new FileOutputStream(file);
        Utils.copy(inputStream, outputStream);
        return file.getAbsolutePath();
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int byteCount = 0;
        while ((byteCount = is.read(buffer)) != -1) {
            os.write(buffer, 0, byteCount);
        }
    }

    /**
     * Android 10，复制到应用沙盒
     *
     * @param context
     * @param uri
     * @return filepath
     * @throws IOException
     */
    public static String asString(Context context, Uri uri) throws IOException {
        Objects.requireNonNull(context);
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Objects.requireNonNull(inputStream);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        File file = Utils.createFile(context);
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeAll(source);
        sink.flush();
        sink.close();
        source.close();
        return file.getAbsolutePath();
    }

}
