package sino.android.rxphotox;

import android.os.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StorageUtils {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int byteCount = 0;
        while ((byteCount = is.read(buffer)) != -1) {
            os.write(buffer, 0, byteCount);
        }
    }

}
