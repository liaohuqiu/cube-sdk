package in.srain.cube.request;

public abstract class CacheAbleRequestDefaultPrePreHandler implements CacheAbleRequestPrePreHandler {

    @Override
    public boolean disableCache() {
        return false;
    }
}