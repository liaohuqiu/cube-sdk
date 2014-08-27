package in.srain.cube.sample.data;

import in.srain.cube.request.*;
import in.srain.cube.util.CLog;

/**
 * When requesting data from web API, it is a good practice to encapsulate all the request logic in a module.
 * This class has some methods to show how to encapsulate the web request.
 * Every method can take some parameters which are related to the specific business logic, and a
 * callback which will be applied after the request is finished.
 *
 * @author http://www.liaohuqiu.net
 */
public class DemoRequestData {

    /**
     * Demo for using {@link SimpleRequest}
     */
    public static void reverse(final String str, final RequestJsonHandler handler) {

        RequestPreHandler preHandler = new RequestPreHandler() {

            public <T> void prepareRequest(RequestBase<T> request) {
                String url = "http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=" + str;
                request.getRequestData().setRequestUrl(url);

            }
        };

        RequestHandler<JsonData> requestHandler = new RequestHandler<JsonData>() {
            @Override
            public JsonData processOriginData(JsonData jsonData) {
                return jsonData;
            }

            @Override
            public void onRequestFail(FailData failData) {
                CLog.d("demo-request", "onRequestFail");
            }

            @Override
            public void onRequestFinish(JsonData data) {
                CLog.d("demo-request", "onRequestFinish");
                handler.onRequestFinish(data);
            }
        };

        SimpleRequest<JsonData> request = new SimpleRequest<JsonData>(preHandler, handler);
        request.send();
    }

    public static interface ImageListDataHandler {
        public void onCacheAbleRequestFinish(JsonData data, CacheAbleRequest.ResultType type, boolean outOfDate);
    }

    /**
     * Demo for using {@link CacheAbleRequest}
     */
    public static void getImageList(final boolean noCache, final ImageListDataHandler handler) {

        CacheAbleRequestPrePreHandler prePreHandler = new CacheAbleRequestPrePreHandler() {

            @Override
            public <T> void prepareRequest(RequestBase<T> request) {
                String url = "http://cube-server.liaohuqiu.net/api_demo/image-list.php";
                request.getRequestData().setRequestUrl(url);
            }

            @Override
            public String getSpecificCacheKey() {
                return "image-list-1";
            }

            @Override
            public String getInitFileAssertPath() {
                return "request_init/demo/image-list.json";
            }

            @Override
            public boolean disableCache() {
                return noCache;
            }

            @Override
            public int getCacheTime() {
                return 30;
            }

        };

        CacheAbleRequestHandler requestHandler = new CacheAbleRequestHandler<JsonData>() {
            @Override
            public void onCacheData(JsonData data, boolean outOfDate) {
                CLog.d("demo-request", "data has been loaded form cache, out of date: %s", outOfDate);
            }

            @Override
            public void onCacheAbleRequestFinish(JsonData data, CacheAbleRequest.ResultType type, boolean outOfDate) {
                CLog.d("demo-request", "onCacheAbleRequestFinish: result type: %s, out of date: %s", type, outOfDate);
                handler.onCacheAbleRequestFinish(data, type, outOfDate);
            }

            @Override
            public JsonData processOriginData(JsonData jsonData) {
                return jsonData;
            }

            @Override
            public void onRequestFail(FailData requestResultType) {
                CLog.d("demo-request", "onRequestFail");
            }

            @Override
            public void onRequestFinish(JsonData data) {
                CLog.d("demo-request", "onRequestFinish");
            }
        };

        CacheAbleRequest<JsonData> request = new CacheAbleRequest<JsonData>(prePreHandler, requestHandler);

        // Uncomment following line, if you want to use the data from cache when cache is available no matter whether it is expired or not.
        // request.usingCacheAnyway(true);

        // When cache is available and request time has exceeded the timeout time, cache data will be used.
        // request.setTimeout(1000);
        request.send();
    }
}
