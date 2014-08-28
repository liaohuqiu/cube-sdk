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

> 当然，例子中的情况是比较简单的，实际的情况可能比这个复杂一些。但不管如何，任何事情的本质应该是简单的。
