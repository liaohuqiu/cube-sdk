---
layout: default
title: 请求缓存
lead: ""
---
<h1 id='cache-able-request'>CacheAbleRequest</h1>
---

### 目的

设计和使用缓存为的是给用户带来更好的体验，比如减少网络请求，在无网络时也可有数据展示。而非使程序结构变得更加复杂。

缓存的终极目的是在任何情况下，都有最新的数据可用。这就需要:

1.  灵活控制缓存的新鲜度

1.  有效快速的缓存
    *   内存缓存，速度快
    *   文件缓存，可缓存更多内容

1.  在极端情况下也有数据可用
    *   没有网络情况下，有缓存数据可展示
    *   用户初安装，没有网络的情况下，也有数据展示

1.  请求失败，可展示缓存数据

1.  请求耗时较长(未超时)，超过一定时间的请求可直接使用缓存数据。

<br/>

---

### 返回结果

使用缓存，关心的是数据的新鲜度，以及需要的结果和使用场景是否相符。

在不禁用缓存的情况下，且有初始化缓存的情况下，无论如何，都可以获取到数据。

1.  `USE_CACHE_NOT_EXPIRED`: 如果有缓存，没有过期，使用未过期的缓存数据。

2.  `USE_DATA_FROM_SERVER`: 如果缓存过期，会进行请求，请求成功之后，使用服务器数据。

    **一般的缓存设计，都能满足以上两点，但在实际开发中，你可能需要以下功能**:

3.  `USE_CACHE_ON_FAIL`:   请求失败，使用缓存数据。

4.  `USE_CACHE_ON_TIMEOUT`:  请求时间可能很长，设定一个超时时间，超过次时间，直接使用缓存数据，缓存会在请求完成后更新。

5.  `USE_CACHE_ANYWAY`:   强制使用缓存。不管缓存过期与否，都使用缓存。如果缓存过期，会请求数据，并更新缓存。

    基于服务器配置的App 启动界面，都是利用这个方式。


结果类型，有如下枚举:

```java
public class CacheAbleRequest {
    public static enum ResultType {
        USE_CACHE_NOT_EXPIRED,
        USE_CACHE_ANYWAY,
        USE_CACHE_ON_TIMEOUT,
        USE_DATA_FROM_SERVER,
        USE_CACHE_ON_FAIL,
    }
}
```

<br/>

---

### 回调

在 `CacheAbleRequestHandler` 中有我们需要的几个回调。

1.  数据加载完回调。这个是开发人员最常关心的一个回调。

    ```java
    public void onCacheAbleRequestFinish(data, resultType, outOfDate);
    ```

1.  缓存数据加载完回调

    ```java
    public void onCacheData(data, outOfDate);
    ```

1.  服务器数据加载完回调

    ```java
    public void onRequestFinish(data);
    ```

1.  数据加载失败回调

    ```java
    public void onRequestFail(failData)
    ```
<br/>

---

### 控制

每个请求需要有一个`CacheAbleRequestPrePreHandler`控制请求行为，另外`CacheAbleRequest`也有几个公开方法可供控制行为。

1.   `CacheAbleRequestPrePreHandler` 接口
    * `public String getSpecificCacheKey();`

        请求的缓存key根据url和get参数生成，如果我们想更好控制key，可以重写此方法。
    * `public String getInitFileAssertPath();`
        
        如果有初始化缓存文件，返回`assets`目录下缓存文件路径。
    * `public boolean disableCache();`

        可返回`true`在特定场景下禁用缓存
    * `public int getCacheTime();`

        控制缓存时间。

1.  `CacheAbleRequest`的公开方法
    * setTimeout(int timeout)

        当请求超过此时间，在有有缓存情况下，使用缓存数据。
    * useCacheAnyway(boolean yes)

        只要有缓存数据存在，不管过期与否，都使用缓存数据。

#### 使用场景

1.  强制使用缓存

    `CacheAbleRequest::useCacheAnyway()`

    ```java
    CacheAbleRequest request = new CacheAbleRequest(....);
    request.useCacheAnyway(true);
    ```

2.  设置超时时间

    `CacheAbleRequest::setTimeout()`

    ```java
    CacheAbleRequest request = new CacheAbleRequest(....);
    request.setTimeout(1000);
    ```

3.  禁用缓存

    `CacheAbleRequestPrePreHandler::disableCache()`

    ```java
    @Override
    public boolean disableCache() {
        return page != 1;
    }
    ```

4.  设置初始化文件

    `CacheAbleRequestPrePreHandler::getInitFileAssertPath()`

    将服务器返回数据放入文件。文件放到 `assets` 文件下。如果文件路径是 `assets/request_init/demo/image-list.json`

    如下:

    ```java
    @Override
    public String getInitFileAssertPath() {
        return "request_init/demo/image-list.json";
    }
    ```
<br/>

---

### 容量和缓存管理

*   容量控制

    `RequestCacheManager::init(Context content, cacheDir, memoryCacheSizeInKB, fileCacheSizeInKB);`
    
    初始化时，可指定文件缓存路径，内存和文件缓存的大小

*   缓存管理
    
    `RequestCacheManager`提供了管理缓存的方法

    *   删除缓存

        `public void invalidateCache(String key);`

    *   清除内存缓存

        `public void clearMemoryCache();`

    *   指定的最大可用内存

        `public int getMemoryCacheMaxSpace();`

    *   目前已用的内存大小

        `public int getMemoryCacheUsedSpace();`

    *   清除文件缓存

        `public void clearDiskCache();`

    *   获取文件缓存，实际磁盘路径

        `public String getFileCachePath();`

    *   已用文件缓存空间

        `public long getFileCacheUsedSpace();`

    *   指定的最大可用文件缓存空间

        `public long getFileCacheMaxSpace();`

*   demo

<div class='row'>
    <div class='col-md-offset-4 col-md-4'>
        <img src='http://cube-sdk.liaohuqiu.net/assets/img/request-cache-management.png'/>
    </div>
</div>

<br/>

---

### 设计

*   原则
    1.  没有缓存数据，或者缓存数据过期，都会获取服务器数据。
    1.  如果禁用缓存，不会加载缓存，请求完成之后，也不会更新缓存数据。
    1.  `onCacheAbleRequestFinish()` 只会通知一次。这个是由设计和编码双重保证的。

*   简单的流程
    1.  如有缓存，加载缓存。
    2.  如果设置强制使用缓存，不管数据是否过期，都使用缓存数据回调。
    3.  未设置强制使用缓存，如果数据没有过期，使用缓存数据回调。
    4.  数据过期，开始请求服务器数据。
    5.  如果设置请求超时，超过时间，使用缓存数据回调。请求继续
    6.  请求完成，更新缓存，如果**未设置超时也未强制缓存**，使用请求结果回调。
    7.  请求失败，如果有缓存，使用缓存数据。
    8.  请求失败，也没有缓存，请求失败。

<br/>

<div class='row'>
    <div class='col-md-offset-1 col-md-10'>
        <img src='http://cube-sdk.liaohuqiu.net/assets/img/request-cache-workflow.png'/>
    </div>
</div>

<br/>

---

###示例

*   服务器接口

    [`http://cube-server.liaohuqiu.net/api_demo/image-list.php`](http://cube-server.liaohuqiu.net/api_demo/image-list.php)

    这个接口返回的数据变化不大，适合做缓存。

<br/>

```java

/**
 * customized callback, notified when data loaded
 */
public static interface ImageListDataHandler {
    public void onData(JsonData data, CacheAbleRequest.ResultType type, boolean outOfDate);
}

/**
 * Demo for using {@link CacheAbleRequest}
 *
 * @param noCache disableCache / 禁用缓存
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
            return "image-list";
        }

        @Override
        public String getInitFileAssertPath() {
            return "/request_init/demo/image-list.json";
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
            CLog.d("demo-request", 
                "data has been loaded form cache, out of date: %s", outOfDate);
        }

        @Override
        public void onCacheAbleRequestFinish(JsonData data, CacheAbleRequest.ResultType type, 
                                                    boolean outOfDate) {
            CLog.d("demo-request", 
                "onCacheAbleRequestFinish: result type: %s, out of date: %s", type, outOfDate);

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

    CacheAbleRequest<JsonData> request = 
                new CacheAbleRequest<JsonData>(prePreHandler, requestHandler);

    // Uncomment following line to use the data from cache when cache is available
    //  no matter whether it is expired or not.
    // 取消注释下面一行，强制使用缓存。
    // request.useCacheAnyway(true);

    // When cache is available and request time has exceeded the timeout time
    // cache data will be used.
    // 可以在这里设置超时时间
    // request.setTimeout(1000);
    request.send();
}
```
