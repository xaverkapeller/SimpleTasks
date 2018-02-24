package com.github.wrdlbrnft.simpletasks.caches;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 24/02/2018
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
            return value.getByteCount();
        }
    }
}
