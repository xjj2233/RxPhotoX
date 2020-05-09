package sino.android.rxphoto;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
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

    public void startPicture(String authority, boolean isPublic) {
        Objects.requireNonNull(getContext());
        Intent starter = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (starter.resolveActivity(getContext().getPackageManager()) == null) {
            return;
        }

        Uri photoUri = null;
        File photoFile = null;

        if (isPublic) {
            if (Utils.isAndroidQ()) {
                photoUri = Utils.createUri(getContext());
            } else {
                photoFile = Utils.createPublicFile();
                mPath = photoFile.getAbsolutePath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoUri = FileProvider.getUriForFile(getContext(), authority, photoFile);
                } else {
                    photoUri = Uri.fromFile(photoFile);
                }
            }
        } else {
            photoFile = Utils.createFile(getContext());
            mPath = photoFile.getAbsolutePath();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(getContext(), authority, photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
        }

        if (photoUri != null) {
            mUri = photoUri;
            starter.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(starter, REQUEST_CODE);
        }
    }

    public PublishSubject<RxPhoto> getSubject() {
        return mSubject;
    }

}
