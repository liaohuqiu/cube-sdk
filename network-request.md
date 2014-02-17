---
layout: default
title: Network Request
lead: Request data in a simple way, then use the data easily.
---

#Usage
---

##The Web API
A sample API to reverse string:
[http://cube-server.liaohuqiu.net/api_demo/reverse.php](http://cube-server.liaohuqiu.net/api_demo/reverse.php)

It takes a input parameter: `str`, then reverse it.

The conent of the `reverse.php`:

```
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

####A sample request:

[http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=123](http://cube-server.liaohuqiu.net/api_demo/reverse.php?str=123)

The output json:

```
{
    ok: 1,
    str: "123"
    result: "321",
    server_time: "2014-02-14 15:56:25"
}
```

##Encapsulate Reuqest

It is a good practice to encapsulate all the request logic in a module, for example:

```
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

##The UI

<div class='row'>
    <div class="col-sm-10">
        <h3>Request Demo</h3>
        <p class='lead'>Click the button in the buttom, fetch the data, then display the data.</p>
    </div>
</div>
<div class='row'>
    <div class="col-sm-4">
        <div class="thumbnail">
            <img src="/assets/img/sample-snapshot/request-demo.png" alt="">
        </div>
    </div>
</div>


The controls:

```
final EditText inpuText = (EditText) view.findViewById(R.id.input_request_demo_str);
final TextView okTextView = (TextView) view.findViewById(R.id.tv_request_demo_ok);
final TextView resultTimeTextView = (TextView) view.findViewById(R.id.tv_request_demo_result);
final TextView serverTimeTextView = (TextView) view.findViewById(R.id.tv_request_demo_server_time);
final TextView button = (TextView) view.findViewById(R.id.btn_request_demo_time);

button.setText("Click to request");
```

In the following code, you can see it is realy very simple to call the web API and use the data.

```
OnClickListener onClickListener = new OnClickListener() {

    @Override
    public void onClick(View v) {

        button.setText("Requesting...");

        String str = inpuText.getText().toString();

        SampleRequest.reverse(str, new JsonRequestSuccHandler() {

                @Override
                public void onRequestFinish(JsonData jsonData) {

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
#Request Cache

To be continued.
