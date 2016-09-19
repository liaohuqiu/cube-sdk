package in.srain.cube.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;
import in.srain.cube.R;
import in.srain.cube.cache.DiskFileUtils;

import java.io.File;

public class UpdateController implements DownLoadListener {

    private static final int NOTIFY_ID = 10000;

    private static final String MSG_ERROR_URL = "错误的下载地址";
    private static final String MSG_DOWNLOAD_FINISH_TICKER_TEXT = "下载已完成，请到系统通知栏查看和安装";
    private static final String MSG_DOWNLOAD_FINISH_TITLE = "下载已完成，点击安装";
    private static final String MSG_DOWN_LOAD_START = "已转入后台下载，请稍候";
    private static final String MSG_DOWNLOAD_FAIL = "下载失败";

    private static final String ACTION_CANCEL_DOWNLOAD = ".cancelDownloadApk";
    private static UpdateController sInstance;
    Notification mNotification = null;
    NotificationManager mNotifyManager = null;
    private String mDownLoadErrorMsg = MSG_DOWNLOAD_FAIL;
    private String mPackageName;
    private DownloadTask mDownloadTask;

    private Context mContext;
    private String mActionCancel;
    private String mApkPath;
    private PackageInfo mPackageInfo;

    private int mIcon;

    private UpdateController() {
    }

    public static UpdateController getInstance() {
        if (sInstance == null) {
            sInstance = new UpdateController();
        }
        return sInstance;
    }

    /**
     * @param context
     * @param icon
     */
    public void init(Context context, int icon) {

        if (context == null) {
            throw new IllegalArgumentException("How content can be null?");
        }

        mContext = context;
        mIcon = icon;

        PackageManager manager = context.getPackageManager();
        try {
            mPackageInfo = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Can not find package information");
        }
        mPackageName = mPackageInfo.packageName;
        mActionCancel = mPackageName + ACTION_CANCEL_DOWNLOAD;

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotification = new Notification();
        mNotification.icon = mIcon;
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
    }

    private void notifyDownloadFinish() {

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openIntent.setDataAndType(Uri.fromFile(new File(mApkPath)), "application/vnd.android.package-archive");
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, openIntent, 0);

        Notification notification = new Notification();
        notification.icon = mIcon;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.contentView = new RemoteViews(mPackageName, R.layout.cube_mints_update_notify);
        notification.contentView.setImageViewResource(R.id.update_notification_icon, mIcon);
        notification.contentView.setProgressBar(R.id.update_notification_progress, 100, 100, false);
        notification.contentView.setTextViewText(R.id.update_notification_text, MSG_DOWNLOAD_FINISH_TITLE);
        notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;

        notification.tickerText = MSG_DOWNLOAD_FINISH_TICKER_TEXT;

        notification.contentIntent = contentIntent;
        mNotifyManager.notify(NOTIFY_ID, notification);

        openFile();
    }

    private void notifyDownLoadStart() {

        showToastMessage(MSG_DOWN_LOAD_START);

        RemoteViews contentView = new RemoteViews(mPackageName, R.layout.cube_mints_update_notify);
        mNotification.icon = mIcon;
        mNotification.tickerText = MSG_DOWN_LOAD_START;
        mNotification.contentView = contentView;
        mNotification.contentView.setProgressBar(R.id.update_notification_progress, 100, 0, false);
        mNotification.contentView.setImageViewResource(R.id.update_notification_icon, mIcon);

        mNotifyManager.notify(NOTIFY_ID, mNotification);
    }

    private void notifyDownLoading(int updatePercent) {

        Intent intent = new Intent(mActionCancel);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        mNotification.contentView = new RemoteViews(mPackageName, R.layout.cube_mints_update_notify);
        mNotification.contentView.setImageViewResource(R.id.update_notification_icon, mIcon);
        mNotification.contentView.setProgressBar(R.id.update_notification_progress, 100, updatePercent, false);
        mNotification.contentView.setTextViewText(R.id.update_notification_text, "下载进度  " + updatePercent + "%, 点击取消下载");

        mNotification.contentView.setOnClickPendingIntent(R.id.update_notification_layout, contentIntent);

        mNotifyManager.notify(NOTIFY_ID, mNotification);
    }

    public void beginDownLoad(String url) {
        beginDownLoad(url, false);
    }

    public void beginDownLoad(String url, boolean forceReDownload) {

        String dir = DiskFileUtils.wantFilesPath(mContext, true);
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        mApkPath = dir + File.separator + "downloads" + File.separator + fileName;

        mDownloadTask = new DownloadTask(this, url, mApkPath);
        notifyDownLoadStart();
        if (forceReDownload) {
            deleteApkFile();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(mActionCancel);
        mContext.registerReceiver(new CancelBroadcastReceiver(), filter);

        Thread thread = new Thread(mDownloadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(mApkPath)), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        mNotifyManager.cancel(NOTIFY_ID);
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onDone(boolean canceled, int result) {
        if (canceled) {
            return;
        }
        switch (result) {
            case DownloadTask.RESULT_OK:
                mNotifyManager.cancelAll();
                notifyDownloadFinish();
                // openFile();
                break;

            case DownloadTask.RESULT_DOWNLOAD_ERROR:
                mNotifyManager.cancel(NOTIFY_ID);
                deleteApkFile();
                showToastMessage(mDownLoadErrorMsg);
                break;

            case DownloadTask.RESULT_NO_ENOUGH_SPACE:
                break;

            case DownloadTask.RESULT_URL_ERROR:
                mNotifyManager.cancel(NOTIFY_ID);
                deleteApkFile();
                showToastMessage(MSG_ERROR_URL);
                break;
        }
    }

    @Override
    public void onPercentUpdate(int percent) {
        notifyDownLoading(percent);
    }

    private void deleteApkFile() {
    }

    private void showToastMessage(String msg) {
        Toast toast = Toast.makeText(mContext, msg, 5000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private class CancelBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mNotifyManager.cancel(NOTIFY_ID);
            mDownloadTask.cancel();
        }
    }
}
