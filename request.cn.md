---
layout: default
title: 网络请求
lead: 简单的网络请求，方便地使用接口数据。
---

<h1 id='simple-request'>SimpleRequest</h1>
---

1. 请求的发送和结果处理

    对于API请求，处理业务逻辑是，关注发送的具体数据，以及请求完成，失败后的逻辑处理。
    
    如下接口定义:
    
    ```
    public void onRequestFinish(T data);
    public void onRequestFail(RequestResultType requestResultType);
    ```

2. 数据转换

    在实际开发中，我们可能会对服务器返回的数据做一些处理，比如数据拼装和转换。

    另外服务器返回数据映射成实体类或者反序列化都是比较耗时的操作，这样的操作，也应该在后台线程中完成。
    
    所以开发中，还关心数据转化这个操作:
    
    ```
    public T processOriginData(JsonData jsonData);
    ```

##服务器端API
在Demo中，我们使用了一个反转字符串的服务器端接口：`reverse`，
[http://cube-server.liaohuqiu.net/api_demo/reverse.php](http://cube-server.liaohuqiu.net/api_demo/reverse.php)

传入一个参数:
`str`，反转，然后输出。比如输入`123`，输入`321`。输出为`json`格式。

下面是这个接口的内容，非常简单，`reverse.php`:

```php
<?php
date_default_timezone_set('UTC');
$msg = $_REQUEST['str'];

$data = array();

if (!$msg) {
    $data['ok'] = 0;
} else {
    $data['ok'] = 1;
}

$data['str'] = $msg;
$data['result'] = strrev($msg);
$data['server_time'] = date('Y-m-d H:i:s');
echo json_encode($data);
```

####一个请求的例子:

[http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=123](http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=123)

输出的结果为：

```json
{
    "ok": 1,
    "str": "123",
    "result": "321",
    "server_time": "2014-02-14 23:56:25"
}
```

##请求封装

我们建议，把网络请求进行封装，放在数据层。使用的时候，调用数据层，将数据显示在界面上。如下：

```java
public class DemoRequestData {

    /**
     * Show how to encapsulate the calling of a web API by Request
     */
    public static void reverse(final String str, final RequestJsonHandler handler) {

        new SimpleRequest<JsonData>(new BeforeRequestHandler() {

            public <T> void beforeRequest(RequestBase<T> request) {

                String url = "http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=" + str;
                request.getRequestData().setRequestUrl(url);

            }
        }, handler).send();
    }
}
```

##界面

<div class='row'>
    <div class="col-sm-10">
        <h3>网络请求的Demo</h3>
        <p>功能：点击底部的按钮，利用输入的输入数据访问服务器端接口，然后将结果显示出来。</p>
    </div>
</div>
<div class='row'>
    <div class="col-sm-4">
        <div class="thumbnail">
            <img src="/assets/img/sample-snapshot/request-demo.png" alt="">
        </div>
    </div>
</div>


界面上的元素:

```java
final EditText inpuText = (EditText) view.findViewById(R.id.input_request_demo_str);
final TextView okTextView = (TextView) view.findViewById(R.id.tv_request_demo_ok);
final TextView resultTimeTextView = (TextView) view.findViewById(R.id.tv_request_demo_result);
final TextView serverTimeTextView = (TextView) view.findViewById(R.id.tv_request_demo_server_time);
final TextView button = (TextView) view.findViewById(R.id.btn_request_demo_time);

button.setText("Click to request");
```

点击按钮的时候，调用数据层接口，然后显示出来：

```java
OnClickListener onClickListener = new OnClickListener() {

    @Override
    public void onClick(View v) {

        button.setText("Requesting...");

        String str = inpuText.getText().toString();

        // 调用数据层接口
        SampleRequest.reverse(str, new JsonRequestSuccHandler() {

                @Override
                public void onRequestFinish(JsonData jsonData) {

                // 更新显示
                button.setText("Click to request");
                okTextView.setText(jsonData.optString("ok"));
                resultTimeTextView.setText(jsonData.optString("result"));
                serverTimeTextView.setText(jsonData.optString("server_time"));

            }
        });
    }
};
button.setOnClickListener(onClickListener);
```

当然，例子中的情况是比较简单的，实际的情况可能比这个复杂一些。但不管如何，本质是简单的。

<h1 id='cache-able-request'>CacheAbleRequest</h1>
---

##需要解决的问题

在实际开发中，我们经常需要缓存请求的结果，以期减少网络请求，在无网络时有缓存数据可展示。

在处理结果缓存的时候，我们会关心以下几个方面：

1.  什么样的内容该缓存，如何分辨缓存的内容。

    同一个接口，请求参数不同，应该有不同的缓存。

    比如列表接口，首页内容可以缓存，第二页内容一般不做缓存。

2.  缓存时间。根据不同的业务逻辑，缓存时间各有不同，即使同一个接口，缓存时间有可能不同。比如在数据更新频繁的时段，缓存时间短一些。

3.  缓存数据和远程服务器数据的辨识

    哪些数据是来自缓存，哪些数据又是来自于服务器？

4.  请求完成，数据的展示问题

    如果缓存中有数据，可能会优先展示缓存中数据。如果数据过期，选择加载服务器数据，当服务器数据请求完成，重新加载，导致界面闪烁。

> 当然简单是第一要求

对于以上需求，抽象成:

```java
public String getSpecificCacheKey();

public int getCacheTime();

public void onCacheData(T1 data, boolean outOfDate);

public void onCacheAbleRequestFinish(T1 data, boolean fromCache, boolean outOfDate);
```
