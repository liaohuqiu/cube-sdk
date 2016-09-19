package in.srain.cube.photos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import in.srain.cube.R;
import in.srain.cube.cache.DiskFileUtils;
import in.srain.cube.diskcache.FileUtils;

import java.io.File;
import java.io.IOException;

public final class SelectPhotoManager {

    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_ALBUM = 2;
    private static final int REQUEST_CODE_CROP = 3;
    public static final int ACTION_TAKE_PHOTO = 0;
    public static final int ACTION_ALBUM = 1;
    public static final int ACTION_CANCEL = 2;
    private static SelectPhotoManager sInstance;

    private static String TEMP_PATH_NAME = "cube-tmp-photo";

    public interface SelectClickHandler {
        /**
         * @param action can be one of {{@link #ACTION_TAKE_PHOTO} {@link #ACTION_ALBUM} {@link #ACTION_CANCEL}}
         */
        public void onClick(int action);
    }

    private PhotoReadyHandler mPhotoReadyHandler;
    private File mTempDir;
    private File mTempFile;
    private Activity mActivity;
    private CropOption mCropOption;

    private SelectPhotoManager() {
    }

    public static SelectPhotoManager getInstance() {
        if (sInstance == null) {
            sInstance = new SelectPhotoManager();
        }
        return sInstance;
    }

    public void setCropOption(CropOption option) {
        mCropOption = option;
    }

    public void setPhotoReadyHandler(PhotoReadyHandler handler) {
        mPhotoReadyHandler = handler;
    }

    public void start(Activity activity) {
        start(activity, null);
    }

    public void start(final Activity activity, final SelectClickHandler handler) {

        DiskFileUtils.CacheDirInfo info = DiskFileUtils.getDiskCacheDir(activity, TEMP_PATH_NAME, 1024 * 1024 * 30);
        File path = info.path;
        try {
            FileUtils.deleteDirectoryQuickly(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!path.exists() && !path.mkdirs()) {
            Toast.makeText(activity, R.string.cube_photo_can_not_use_camera, Toast.LENGTH_SHORT).show();
            return;
        } else {
            path.deleteOnExit();
        }

        mTempDir = info.path;
        mActivity = activity;
        mTempFile = new File(mTempDir.getAbsolutePath(), Long.toString(System.nanoTime()) + ".jpg");

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                final int action = i;
                if (handler != null) {
                    handler.onClick(action);
                }
                switch (action) {
                    case ACTION_TAKE_PHOTO:
                        PhotoUtils.toCamera(activity, mTempFile, REQUEST_CODE_CAMERA);
                        break;
                    case ACTION_ALBUM:
                        PhotoUtils.toAlbum(activity, REQUEST_CODE_ALBUM);
                        break;
                    case ACTION_CANCEL:
                        // do nothing
                        break;
                }
                dialog.dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setItems(R.array.cube_photo_pick_options, clickListener);
        builder.show().setCanceledOnTouchOutside(true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        String imgPath = null;
        if (requestCode == REQUEST_CODE_CAMERA) {
            imgPath = mTempFile.getPath();
            if (!afterPhotoTaken(imgPath)) {
                sendMessage(PhotoReadyHandler.FROM_CAMERA, imgPath);
            }
        } else if (requestCode == REQUEST_CODE_ALBUM && data != null) {
            Uri imgUri = data.getData();
            imgPath = PhotoUtils.uriToPath(mActivity, imgUri);
            if (!afterPhotoTaken(imgPath)) {
                sendMessage(PhotoReadyHandler.FROM_ALBUM, imgPath);
            }
        } else if (requestCode == REQUEST_CODE_CROP) {
            imgPath = mTempFile.getPath();
            sendMessage(PhotoReadyHandler.FROM_CROP, imgPath);
        }
    }

    private void sendMessage(int from, String imgPath) {

        if (mPhotoReadyHandler != null) {
            mPhotoReadyHandler.onPhotoReady(from, imgPath);
        }

    }

    private boolean afterPhotoTaken(String imgPath) {
        if (TextUtils.isEmpty(imgPath)) {
            throw new RuntimeException();
        }
        if (mCropOption == null) {
            return false;
        }
        File f = new File(imgPath);
        mTempFile = new File(mTempDir.getAbsolutePath(), Long.toString(System.nanoTime()) + "_cropped.jpg");
        PhotoUtils.toCrop(mActivity, f, mTempFile, mCropOption, REQUEST_CODE_CROP);
        return true;
    }
}
