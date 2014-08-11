package in.srain.cube.request;

public abstract class CacheAbleRequestDefaultPreHandler implements CacheAbleRequestPreHandler {

    @Override
    public <T> void beforeRequest(RequestBase<T> request) {

    }

    @Override
    public boolean disableCache() {
        return false;
    }
}