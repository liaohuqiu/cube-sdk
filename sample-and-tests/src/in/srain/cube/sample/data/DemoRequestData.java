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

        SimpleRequest<JsonData> request = new SimpleRequest<JsonData>(handler);
        String url = "http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=" + str;
        request.getRequestData().setRequestUrl(url);
        request.send();
    }

    /**
     * customized callback, notified when data loaded
     */
    public static interface ImageListDataHandler {
        public void onData(JsonData data, CacheAbleRequest.ResultType type, boolean outOfDate);
    }

    /**
     * Demo for using {@link CacheAbleRequest}
     *
     * @param noCache
     */
    public static void getImageList(final boolean noCache, final ImageListDataHandler handler) {

        CacheAbleRequestHandler requestHandler = new CacheAbleRequestHandler<JsonData>() {
            @Override
            public void onCacheData(JsonData data, boolean outOfDate) {
                CLog.d("demo-request", "data has been loaded form cache, out of date: %s", outOfDate);
            }

            @Override
            public void onCacheAbleRequestFinish(JsonData data, CacheAbleRequest.ResultType type,
                                                 boolean outOfDate) {

                CLog.d("demo-request",
                        "onData: result type: %s, out of date: %s", type, outOfDate);

                handler.onData(data, type, outOfDate);
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

        CacheAbleRequest<JsonData> request = new CacheAbleRequest<JsonData>(requestHandler);

        String url = "http://cube-server.liaohuqiu.net/api_demo/image-list.php";
        request.getRequestData().setRequestUrl(url);
        request.setInitDataPath("request_init/demo/image-list.json");
        request.setCacheKey("image-list-1");

        // Uncomment following line to use the data from cache when cache is available
        // no matter whether it is expired or not.
        // request.useCacheAnyway(true);

        // When cache is available and request time has exceeded the timeout time,
        // cache data will be used.
        // request.setTimeout(1000);
        request.send();
    }
}
