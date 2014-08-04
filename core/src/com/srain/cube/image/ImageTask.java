package com.srain.cube.image;

import java.lang.ref.WeakReference;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.util.Encrypt;

/**
 * A wrapper of the related information used in loading a bitmap
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageTask {

    private static int sId = 0;

    private int mFlag;
    protected int mId;

    // the origin request url for the image.
    protected String mOriginUrl;

    // In some situations, we may store the same image in some different servers. So the same image will related to some different urls.
    private String mIdentityUrl;

    // The key related to the image this ImageTask is requesting.
    private String mIdentityKey;

    // cache for toString();
    private String mStr;

    protected Point mRequestSize = new Point();
    protected Point mBitmapOriginSize = new Point();

    private final static String SIZE_SP = "_";
    private final static int STATUS_PRE_LOAD = 0x01;
    private final static int STATUS_LOADING = 0x02;
    private final static int STATUS_DONE = 0x04;
    private final static int STATUS_CANCELED = 0x08;

    private ImageViewHolder mFirstImageViewHolder;
    protected ImageReuseInfo mReuseInfo;

    public ImageTask(String originUrl, int requestWidth, int requestHeight, ImageReuseInfo imageReuseInfo) {

        mId = ++sId;

        mOriginUrl = originUrl;
        mRequestSize = new Point(requestWidth, requestHeight);
        if (imageReuseInfo != null) {
            mReuseInfo = imageReuseInfo;
        }
    }

    /**
     * For accessing the identity url
     *
     * @return
     */
    public String getIdentityUrl() {
        if (null == mIdentityUrl) {
            mIdentityUrl = generateIdentityUrl(mOriginUrl);
        }
        return mIdentityUrl;
    }

    /**
     * In some situations, we may store the same image in some different servers. So the same image will related to some different urls.
     * <p/>
     * Generate the identity url according your situation.
     * <p/>
     * {@link #mIdentityUrl}
     *
     * @return
     */
    protected String generateIdentityUrl(String originUrl) {
        return originUrl;

    }

    /**
     * Generate the identity key.
     * <p/>
     * This key should be related to the unique image this task is requesting: the size, the remote url.
     *
     * @return
     */
    protected String generateIdentityKey() {
        if (mReuseInfo == null) {
            return joinSizeInfoToKey(getIdentityUrl(), mRequestSize.x, mRequestSize.y);
        } else {
            return joinSizeTagToKey(getIdentityUrl(), mReuseInfo.getIdentitySize());
        }
    }

    public boolean isPreLoad() {
        return (mFlag & STATUS_PRE_LOAD) == STATUS_PRE_LOAD;
    }

    public void setIsPreLoad() {
        mFlag = mFlag | STATUS_PRE_LOAD;
    }

    public boolean isLoading() {
        return (mFlag & STATUS_LOADING) != 0;
    }

    /**
     * Check the given url is loading.
     *
     * @param url
     * @return Identify the given url, if same to {@link #mIdentityUrl} return true.
     */
    public boolean isLoadingThisUrl(String url) {
        return getIdentityUrl().equals(generateIdentityUrl(url));
    }

    /**
     * Bind ImageView with ImageTask
     *
     * @param imageView
     */
    public void addImageView(CubeImageView imageView) {
        if (null == imageView) {
            return;
        }
        if (null == mFirstImageViewHolder) {
            mFirstImageViewHolder = new ImageViewHolder(imageView);
            return;
        }

        ImageViewHolder holder = mFirstImageViewHolder;
        for (; ; holder = holder.mNext) {
            if (holder.contains(imageView)) {
                return;
            }
            if (holder.mNext == null) {
                break;
            }
        }

        ImageViewHolder newHolder = new ImageViewHolder(imageView);
        newHolder.mPrev = holder;
        holder.mNext = newHolder;
    }

    /**
     * Remove the ImageView from ImageTask
     *
     * @param imageView
     */
    public void removeImageView(CubeImageView imageView) {

        if (null == imageView || null == mFirstImageViewHolder) {
            return;
        }

        ImageViewHolder holder = mFirstImageViewHolder;

        do {
            if (holder.contains(imageView)) {

                // Make sure entry is right.
                if (holder == mFirstImageViewHolder) {
                    mFirstImageViewHolder = holder.mNext;
                }
                if (null != holder.mNext) {
                    holder.mNext.mPrev = holder.mPrev;
                }
                if (null != holder.mPrev) {
                    holder.mPrev.mNext = holder.mNext;
                }
            }

        } while ((holder = holder.mNext) != null);
    }

    /**
     * Check if this ImageTask has any related ImageViews.
     *
     * @return
     */
    public boolean stillHasRelatedImageView() {
        if (null == mFirstImageViewHolder || mFirstImageViewHolder.getImageView() == null) {
            return false;
        } else {
            return true;
        }
    }

    public void onLoading(ImageLoadHandler handler) {
        mFlag = mFlag | STATUS_LOADING;

        if (null == handler || null == mFirstImageViewHolder) {
            return;
        }

        ImageViewHolder holder = mFirstImageViewHolder;
        do {
            final CubeImageView imageView = holder.getImageView();
            if (null != imageView) {
                handler.onLoading(this, imageView);
            }
        } while ((holder = holder.mNext) != null);
    }

    /**
     * Will be called when begin load image data from dish or network
     *
     * @param drawable
     */
    public void onLoadFinish(BitmapDrawable drawable, ImageLoadHandler handler) {

        mFlag &= ~STATUS_LOADING;
        mFlag |= STATUS_DONE;

        if (null == handler || null == mFirstImageViewHolder) {
            return;
        }

        ImageViewHolder holder = mFirstImageViewHolder;
        do {
            final CubeImageView imageView = holder.getImageView();
            if (null != imageView) {
                imageView.onLoadFinish();
                handler.onLoadFinish(this, imageView, drawable);
            }
        } while ((holder = holder.mNext) != null);
    }

    public void onCancel() {
        mFlag &= ~STATUS_LOADING;
        mFlag |= STATUS_CANCELED;
    }

    /**
     * If you have a thumbnail web service which can return multiple size image according the url,
     * <p/>
     * you can implements this method to return the specified url according the request size.
     *
     * @return
     */
    public String getRemoteUrl() {
        return mOriginUrl;
    }

    /**
     * Return the origin request url
     *
     * @return
     */
    public String getOriginUrl() {
        return mOriginUrl;
    }

    public void setBitmapOriginSize(int width, int height) {
        mBitmapOriginSize = new Point(width, height);
    }

    public Point getBitmapOriginSize() {
        return mBitmapOriginSize;
    }

    public Point getRequestSize() {
        return mRequestSize;
    }

    /**
     * Return the key which identifies this Image Wrapper object.
     */
    public String getIdentityKey() {
        if (mIdentityKey == null) {
            mIdentityKey = generateIdentityKey();
        }
        return mIdentityKey;
    }

    /**
     * Join the key and the size information.
     *
     * @param key
     * @param w
     * @param h
     * @return "$key" + "_" + "$w" + "_" + "$h"
     */
    public static String joinSizeInfoToKey(String key, int w, int h) {
        if (w > 0 && h != Integer.MAX_VALUE && h > 0 && h != Integer.MAX_VALUE) {
            return new StringBuilder(key).append(SIZE_SP).append(w).append(SIZE_SP).append(h).toString();
        }
        return key;
    }

    /**
     * Join the tag with the key.
     *
     * @param key
     * @param tag
     * @return "$key" + "_" + "$tag"
     */
    public static String joinSizeTagToKey(String key, String tag) {
        return new StringBuilder(key).append(SIZE_SP).append(tag).toString();
    }

    /**
     * Return the cache key for file cache.
     *
     * @return the cache key for file cache.
     */
    public String getFileCacheKey() {
        return Encrypt.md5(getIdentityKey());
    }

    /**
     * @param sizeKey
     * @return
     */
    public String generateFileCacheKeyForReuse(String sizeKey) {
        return Encrypt.md5(joinSizeTagToKey(getIdentityUrl(), sizeKey));
    }

    public ImageReuseInfo getImageReuseInfo() {
        return mReuseInfo;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof ImageTask) {
            return ((ImageTask) object).getIdentityKey().equals(getIdentityKey());
        }
        return false;
    }

    @Override
    public String toString() {
        if (mStr == null) {
            mStr = String.format("%s %sx%s", mId, mRequestSize.x, mRequestSize.y);
        }
        return mStr;
    }

    /**
     * A tiny and light linked-list like container to hold all the ImageViews related to the ImageTask.
     */
    private static class ImageViewHolder {
        private WeakReference<CubeImageView> mImageViewRef;
        private ImageViewHolder mNext;
        private ImageViewHolder mPrev;

        public ImageViewHolder(CubeImageView imageView) {
            mImageViewRef = new WeakReference<CubeImageView>(imageView);
        }

        boolean contains(CubeImageView imageView) {
            return mImageViewRef != null && imageView == mImageViewRef.get();
        }

        CubeImageView getImageView() {
            if (null == mImageViewRef) {
                return null;
            }
            return mImageViewRef.get();
        }
    }
}
