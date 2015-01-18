package in.srain.cube.request;

import in.srain.cube.cache.ICacheAble;

public interface ICacheAbleRequest<T> extends ICacheAble<T>, IRequest<T> {

    /**
     * set a timeout, when request time over this value, cache data will be used.
     *
     * @param timeOut
     */
    public void setTimeout(int timeOut);
}
