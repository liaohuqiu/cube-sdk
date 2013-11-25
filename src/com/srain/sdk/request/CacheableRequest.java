package com.srain.sdk.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.srain.sdk.request.RequestCache.Cacheable;

public class CacheableRequest extends Request implements Cacheable {

	private CacheableRequestOnSuccHandler mCacheableRequestHandler;
	private CacheableRequestPreHandler mCacheableRequestPreHandler;

	public CacheableRequest(CacheableRequestPreHandler cacheableRequestPreHandler, CacheableRequestOnSuccHandler cacheableRequestHandler) {
		super(cacheableRequestPreHandler, cacheableRequestHandler);
		mCacheableRequestPreHandler = cacheableRequestPreHandler;
		mCacheableRequestHandler = cacheableRequestHandler;
	}

	/**
	 * Override to query cache before query
	 */
	@Override
	public Request send() {
		prepare();
		RequestCache.getInstance().requestCache(this);
		return this;
	}

	/**
	 * cache data after request
	 */
	@Override
	public void onRequestSucc(JsonData jsonData) {
		super.onRequestSucc(jsonData);
		RequestCache.getInstance().cacheRequest(this, jsonData);
	}

	// ===========================================================
	// Implements Interface @Cacheable
	// ===========================================================
	@Override
	public void onNoCacheDataAvailable() {
		doQuery();
	}

	@Override
	public void onCachedPreviousData(JsonData previousJsonData) {
		mCacheableRequestHandler.onCachedPreviousData(previousJsonData);
		doQuery();
	}

	@Override
	public int getCacheTime() {
		return mCacheableRequestPreHandler.getCacheTime();
	}

	@Override
	public String getCacheKey() {
		String cachKey = mCacheableRequestPreHandler.getSpecificCacheKey();
		if (cachKey != null && cachKey.length() > 0)
			return cachKey;

		String url = getRequestUrl();
		try {
			URI uri = null;
			uri = new URI(url);
			cachKey = uri.getPath();
			if (cachKey.startsWith("/"))
				cachKey = cachKey.substring(1);
			cachKey = cachKey.replace("/", "-");
			return cachKey;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return md5(url);
	}

	public static final String md5(final String s) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void onCacheData(JsonData cacheJsonData) {
		mCacheableRequestHandler.onRequestSucc(cacheJsonData);
	}

	@Override
	public String getAssertInitDataPath() {
		return mCacheableRequestPreHandler.getInitFileAssertPath();
	}
}
