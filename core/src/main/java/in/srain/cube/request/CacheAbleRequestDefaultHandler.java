package in.srain.cube.request;

import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

public abstract class CacheAbleRequestDefaultHandler<T1> implements CacheAbleRequestHandler<T1> {

    public void onCacheData(T1 data, boolean outOfDate) {
    }

    @Override
    public void onRequestFail(FailData failData) {
        if (failData != null && failData.getRequest() != null && failData.getRequest().getRequestData() != null) {
            CLog.e(CubeDebug.DEBUG_REQUEST_LOG_TAG, "onRequestFail: %s", failData.getRequest().getRequestData().getRequestUrl());
        }
    }

    @Override
    public void onRequestFinish(T1 data) {
        if (CubeDebug.DEBUG_REQUEST) {
            CLog.d(CubeDebug.DEBUG_REQUEST_LOG_TAG, "onRequestFinish: %s", data);
        }
    }
}