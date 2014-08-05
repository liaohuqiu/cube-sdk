package net.liaohuqiu.cube.request;

public interface BeforeRequestHandler {
	public <T> void beforeRequest(SimpleRequest<T> request);
}