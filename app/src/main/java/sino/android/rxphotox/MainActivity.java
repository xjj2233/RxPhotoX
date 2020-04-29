package sino.android.rxphotox;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import sino.android.rxphoto.RxPhoto;
import sino.android.rxphoto.RxPhotos;
import sino.android.rxphoto.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.photo_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCameraPermission();
                    }
                });

    }

    private void onWritePermission() {
        new RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new SubObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            onTest(MainActivity.this);
                        } else {
                            Log.d("rx", "请允许权限: ");
                        }
                    }
                });
    }

    public static void onTest(Context context) {
        try {
            onTestPath(context);
            onTestUri(context);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void onTestPath(Context context) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String timestamp = formatter.format(new Date());
        String filename = String.format("JPEG_%s.jpg", timestamp);

        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, filename);

        String path = file.getAbsolutePath();
        // /storage/emulated/0/Android/data/sino.android.rxphotox/files/Pictures/JPEG_20200426_141741_6696884749589482790.jpg
        Log.d("rx", "onTest: path= " + path);

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "sino.android.rxphotox.FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Log.d("rx", "onTest: uri= " + uri);

        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        file = new File(directory, filename);

        path = file.getAbsolutePath();
        // /storage/emulated/0/Android/data/sino.android.rxphotox/files/Pictures/JPEG_20200426_141741_6696884749589482790.jpg
        Log.d("rx", "onTest: path= " + path);

        uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "sino.android.rxphotox.FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Log.d("rx", "onTest: uri= " + uri);


    }

    private static void onTestUri(Context context) {
        Objects.requireNonNull(context);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        String state = Environment.getExternalStorageState();
        Uri externalUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // content://media/external/images/media/159486
        Log.d("rx", "onTestUri: externalUri= " + externalUri);

        // java.lang.UnsupportedOperationException: Writing to internal storage is not supported.
        //Uri internalUri = resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        //Log.d("rx", "onTest: internalUri= " + internalUri);
    }


    private void onCameraPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new SubObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            onPhoto();
                        } else {
                            Log.d("rx", "请允许权限: ");
                        }
                    }
                });
    }

    private void onPhoto() {
        new RxPhotos(this)
                .request("sino.android.rxphotox.FileProvider", true)
                .subscribe(new SubObserver<RxPhoto>() {
                    @Override
                    public void onNext(RxPhoto rxPhoto) {
                        showImageView(rxPhoto.getPath(), rxPhoto.getUri());
                    }
                });
    }

    private void onCopy(final Uri uri) {
        Observable.just(uri)
                .map(new Function<Uri, String>() {
                    @Override
                    public String apply(Uri uri) throws Exception {
                        return Utils.asFilePath(MainActivity.this, uri);
                    }
                })
                .compose(RxJavas.<String>scheduler())
                .subscribe(new SubObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        showImageView(s, uri);
                    }
                });
    }

    private void showImageView(String path, Uri uri) {
        // /storage/emulated/0/Pictures/JPEG_20200426_151014.jpg
        // content://sino.android.rxphotox.FileProvider/external-path/Pictures/JPEG_20200426_151014.jpg
        Log.d("rx", "showImageView: path= " + path);
        Log.d("rx", "showImageView: uri= " + uri);
        ImageView imageView = findViewById(R.id.image_view);
        if (Utils.isAndroidQ()) {
            Glide.with(this).load(uri).into(imageView);
        } else {
            Glide.with(this).load(path).into(imageView);
        }
    }

    private void showImageView(Uri uri) {
//        FutureTarget<File> target = Glide.with(this)
//                .asFile()
//                .load("")
//                .submit();
//        File file = target.get();
    }

}
