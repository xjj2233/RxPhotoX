package sino.android.rxphoto;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.subjects.PublishSubject;

public class RxPhotoFragment extends Fragment {

    private static final int REQUEST_CODE = 47;
    private PublishSubject<RxPhoto> mSubject = PublishSubject.create();

    private Uri mUri;
    private String mPath;

    public RxPhotoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode != REQUEST_CODE) {
            return;
        }

        onResult();
    }

    private void onResult() {
        RxPhoto data = new RxPhoto(mPath, mUri);
        mSubject.onNext(data);
        mSubject.onComplete();
    }

    public void startPicture(String authority) throws IOException {
        Objects.requireNonNull(getContext());
        Intent starter = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (starter.resolveActivity(getContext().getPackageManager()) != null) {
            Uri photoUri = null;
            File photoFile = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                photoUri = createImageUri();
            } else {
                photoFile = createImageFile();
                mPath = photoFile.getAbsolutePath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoUri = FileProvider.getUriForFile(getContext(), authority, photoFile);
                } else {
                    photoUri = Uri.fromFile(photoFile);
                }
            }

            mUri = photoUri;
            if (photoUri != null) {
                starter.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                starter.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(starter, REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        Objects.requireNonNull(getContext());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String timestamp = formatter.format(new Date());
        String filename = String.format("JPEG_%s_", timestamp);
        File directory = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, ".jpg", directory);
    }

    private Uri createImageUri() {
        Objects.requireNonNull(getContext());
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues values = new ContentValues();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    public PublishSubject<RxPhoto> getSubject() {
        return mSubject;
    }

}
