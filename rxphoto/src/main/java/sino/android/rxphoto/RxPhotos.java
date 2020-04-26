package sino.android.rxphoto;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class RxPhotos {

    private static final String TAG = RxPhotos.class.getSimpleName();
    private static final Object TRIGGER = new Object();

    private Lazy<RxPhotoFragment> mFragmentLazy;

    public RxPhotos(FragmentActivity activity) {
        mFragmentLazy = getLazySingleton(activity.getSupportFragmentManager());
    }

    public RxPhotos(Fragment fragment) {
        mFragmentLazy = getLazySingleton(fragment.getChildFragmentManager());
    }

    public Observable<RxPhoto> request(final String authority) {
        return Observable.just(TRIGGER)
                .flatMap(new Function<Object, ObservableSource<RxPhoto>>() {
                    @Override
                    public ObservableSource<RxPhoto> apply(Object o) throws Exception {
                        mFragmentLazy.get().startPicture(authority);
                        return mFragmentLazy.get().getSubject();

                    }
                });
    }

    private Lazy<RxPhotoFragment> getLazySingleton(final FragmentManager fragmentManager) {
        return new Lazy<RxPhotoFragment>() {
            private RxPhotoFragment fragment;

            @Override
            public synchronized RxPhotoFragment get() {
                if (fragment == null) {
                    fragment = getFragment(fragmentManager);
                }
                return fragment;
            }
        };
    }

    private RxPhotoFragment getFragment(FragmentManager fragmentManager) {
        RxPhotoFragment fragment = findFragment(fragmentManager);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new RxPhotoFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commitNow();
        }
        return fragment;
    }

    private RxPhotoFragment findFragment(FragmentManager fragmentManager) {
        return (RxPhotoFragment) fragmentManager.findFragmentByTag(TAG);
    }

    @FunctionalInterface
    public interface Lazy<V> {
        V get();
    }
}
