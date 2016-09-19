package in.srain.cube.image.impl;

import android.os.Build;
import in.srain.cube.image.ImageTask;
import in.srain.cube.image.iface.ImageDownloader;
import in.srain.cube.image.iface.ProgressUpdateHandler;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple class that fetches images from a URL.
 */
public class SimpleDownloader implements ImageDownloader {

    protected static final String LOG_TAG = CubeDebug.DEBUG_IMAGE_LOG_TAG_PROVIDER;

    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static SimpleDownloader sInstance;

    public static SimpleDownloader getInstance() {
        if (sInstance == null) {
            sInstance = new SimpleDownloader();
        }
        return sInstance;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     */
    public boolean downloadToStream(ImageTask imageTask, String urlString, OutputStream outputStream, ProgressUpdateHandler progressUpdateHandler) {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(0);
            int len = urlConnection.getContentLength();
            int total = 0;
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                total++;
                out.write(b);
                if (progressUpdateHandler != null) {
                    progressUpdateHandler.onProgressUpdate(total, len);
                }
            }
            return true;
        } catch (final IOException e) {
            CLog.e(LOG_TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return false;
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
