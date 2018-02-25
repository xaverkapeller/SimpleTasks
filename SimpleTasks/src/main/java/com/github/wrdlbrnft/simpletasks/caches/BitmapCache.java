package com.github.wrdlbrnft.simpletasks.caches;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
 * <p>
 * Implementation of the {@link Cache} interface for memory efficiently storing {@link Bitmap}
 * instances. This implementation is backed by a {@link LruCache}. When creating an instance
 * of this class a maximum size for {@link Cache} has to be set. When {@link Bitmap} images
 * are added to the {@link Cache} and the maximum size is reached the least recently used
 * {@link Bitmap} is automatically evicted from the {@link Cache}. This caching strategy enables
 * efficient caching of images on memory constrained devices and prevents {@link OutOfMemoryError}
 * Exceptions and other memory related issues which are usually encountered when caching objects
 * which require a lot of memory.
 *
 * @param <K> Type of the keys used to identify {@link Bitmap Bitmaps} in the {@link Cache}.
 */
public class BitmapCache<K> implements Cache<K, Bitmap> {

    private final LruImageCache<K> mInternalCache;

    public BitmapCache(int maxSize) {
        mInternalCache = new LruImageCache<>(maxSize);
    }

    @Override
    public void put(K key, Bitmap item) {
        mInternalCache.put(key, item);
    }

    @Override
    public Bitmap get(K key) {
        return mInternalCache.get(key);
    }

    @Override
    public void evict(K key) {
        mInternalCache.remove(key);
    }

    @Override
    public void clear() {
        mInternalCache.evictAll();
    }

    private static class LruImageCache<K> extends LruCache<K, Bitmap> {

        public LruImageCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(K key, Bitmap value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return value.getAllocationByteCount();
            }

            return value.getByteCount();
        }
    }
}
