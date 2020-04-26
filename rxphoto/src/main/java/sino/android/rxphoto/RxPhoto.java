package sino.android.rxphoto;

import android.net.Uri;

public class RxPhoto {

    private String path;
    private Uri uri;

    public RxPhoto(String path, Uri uri) {
        this.path = path;
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
