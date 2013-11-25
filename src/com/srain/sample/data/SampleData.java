package com.srain.sample.data;

import com.srain.sdk.request.CacheableRequest;
import com.srain.sdk.request.CacheableRequestOnSuccHandler;
import com.srain.sdk.request.CacheableRequestPreHandler;
import com.srain.sdk.request.Request;

public class SampleData {

	public static void getSampleData(final CacheableRequestOnSuccHandler handler) {

		new CacheableRequest(new CacheableRequestPreHandler() {

			@Override
			public void beforeRequest(Request request) {
				request.setRequestUrl("http://www.taobao.com/go/rgn/etaoh5/search_index_php.php");

				// more actions
				// RequestParams params = request.getParams();
				// params.put(key, file);
			}

			@Override
			public String getSpecificCacheKey() {
				return "sample_data";
			}

			@Override
			public int getCacheTime() {
				return 100;
			}

			@Override
			public String getInitFileAssertPath() {
				return "request_init/sample_data.json";
			}
		}, handler).send();
	}
}
