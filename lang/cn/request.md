---
layout: default
title: 网络请求
lead: 简单的网络请求，方便地使用接口数据。
---

#用法
---

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
public class SampleRequest {

    /**
     * Show how to encapsulate the calling of a web API by Request
     */
    public static void reverse(final String str, final JsonRequestSuccHandler handler) {
        new SimpleRequest<JsonData>(new BeforeRequestHandler() {

                @Override
                public <T> void beforeRequest(SimpleRequest<T> request) {

                String url = "http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=" + str;
                request.setRequestUrl(url);
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

当然，例子中的情况是比较简单的，有些理想化。但是事情，本来就是应该做得很简单的。

#请求缓存

未完待续。
