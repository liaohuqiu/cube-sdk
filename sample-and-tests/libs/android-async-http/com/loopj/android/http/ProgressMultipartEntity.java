package com.loopj.android.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProgressMultipartEntity extends SimpleMultipartEntity {
	
	private final static int CHUNKSIZE = 65536;
	private AsyncHttpResponseHandler _progressHandler;

	public ProgressMultipartEntity(AsyncHttpResponseHandler progressHandler) {

		super();
		_progressHandler = progressHandler;
	}

	public void writeTo(final OutputStream outstream) throws IOException {
    	byte[] ba = out.toByteArray();
    	for (int pos = 0; pos < ba.length; pos += CHUNKSIZE) {
    		_progressHandler.sendProgressMessage(pos, ba.length);
    		outstream.write(ba, pos, pos + CHUNKSIZE <= ba.length ? CHUNKSIZE : ba.length - pos);
    	}
    	_progressHandler.sendProgressMessage(ba.length, ba.length);
    }

	private class ProgressOutputStream extends FilterOutputStream {

		private volatile long _position;
		private volatile long _total;
		private volatile OutputStream _outputStream;

		public ProgressOutputStream(OutputStream out, Long total) {
			super(out);

			_outputStream = out;
			_total = total;
		}

		@Override
		public void write(int b) throws IOException {
			_outputStream.write(b);
			_position++;

			if (_progressHandler != null) {
				_progressHandler.sendProgressMessage(_position, _total);
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			_outputStream.write(b, off, len);
			_position += len;

			if (_progressHandler != null) {
				_progressHandler.sendProgressMessage(_position, _total);
			}
		}
	}
}
