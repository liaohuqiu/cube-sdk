package in.srain.cube.request;

public abstract class CacheAbleRequestDefaultHandler<T1> implements CacheAbleRequestHandler<T1> {

    public void onCacheData(T1 data, boolean outOfDate) {
    }

    @Override
    public void onRequestFail(FailData failData) {
    }

    @Override
    public void onRequestFinish(T1 data) {

    }
}