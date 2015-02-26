package in.srain.cube.update;

import android.webkit.URLUtil;
import in.srain.cube.cache.DiskFileUtils;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.diskcache.FileUtils;
import in.srain.cube.util.CLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends SimpleTask {

    private static final String LOG_TAG = "cube-update";

    public static final int RESULT_OK = 1;
    public static final int RESULT_URL_ERROR = 2;
    public static final int RESULT_DOWNLOAD_ERROR = 3;
    public static final int RESULT_NO_ENOUGH_SPACE = 4;

    private int mResult = RESULT_OK;
    private String mUrl;
    private String mFileName;
    private DownLoadListener mDownLoadListener;

    public DownloadTask(DownLoadListener listener, String url, String fileName) {
        mDownLoadListener = listener;
        mUrl = url;
        mFileName = fileName;
    }

    private void setResult(int result) {
        mResult = result;
    }

    @Override
    public void doInBackground() {

        if (!URLUtil.isNetworkUrl(mUrl)) {
            setResult(RESULT_URL_ERROR);
            return;
        }

        int updatePercent;
        String updateUrl = mUrl;

        int totalRead = 0;
        int totalSize = 0;

        try {
            URL myURL = new URL(updateUrl);
            HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
            conn.setConnectTimeout(30 * 1000);
            conn.connect();
            totalSize = conn.getContentLength();
            File dstFile = new File(mFileName);

            if (dstFile.exists() && totalSize == dstFile.length()) {
                if (conn != null) {
                    conn.disconnect();
                }
                setResult(RESULT_OK);
                return;
            }

            if (dstFile.exists()) {
                dstFile.delete();
            }

            File dir = dstFile.getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                setResult(RESULT_DOWNLOAD_ERROR);
                return;
            }

            long free = DiskFileUtils.getUsableSpace(dir);
            if (free < totalSize) {
                setResult(RESULT_NO_ENOUGH_SPACE);
                return;
            }
            InputStream is = conn.getInputStream();
            if (is == null) {
                setResult(RESULT_DOWNLOAD_ERROR);
                return;
            }

            FileOutputStream fos = new FileOutputStream(dstFile);
            byte buf[] = new byte[409200];
            while (!isCancelled()) {
                int read = is.read(buf);
                if (read <= 0) {
                    break;
                }
                fos.write(buf, 0, read);
                totalRead += read;
                updatePercent = (int) (100f * totalRead / totalSize);
                if (!isCancelled()) {
                    mDownLoadListener.onPercentUpdate(updatePercent);
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (Exception e) {
            setResult(RESULT_DOWNLOAD_ERROR);
            return;
        }

        if (isCancelled()) {
            CLog.d(LOG_TAG, "task has been canceled");
            return;
        }

        if (totalRead != totalSize) {
            CLog.d(LOG_TAG, "download fail, file not complete");
            setResult(RESULT_DOWNLOAD_ERROR);
        } else {
            FileUtils.chmod("666", mFileName);
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onCancel() {
        mDownLoadListener.onCancel();
    }

    @Override
    public void onFinish(boolean canceled) {
        mDownLoadListener.onDone(canceled, mResult);
    }
}
