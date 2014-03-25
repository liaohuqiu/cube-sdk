package com.srain.cube.request;

/**
 * @author http://www.liaohuqiu.net
 */
public class JsonSimpleRequest extends SimpleRequest<JsonData> {

	public JsonSimpleRequest(BeforeRequestHandler beforeRequestHandler, JsonRequestSuccHandler succHandler) {
		super(beforeRequestHandler, succHandler);
	}

	public void send() {
		setBeforeRequest();
		doQuery();
	}
}
