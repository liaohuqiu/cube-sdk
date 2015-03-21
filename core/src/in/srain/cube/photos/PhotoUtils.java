package in.srain.cube.photos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Toast;
import in.srain.cube.R;

import java.io.File;
import java.util.List;

public class PhotoUtils {

    public static boolean isCameraUseAble(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static void toCamera(Activity activity, File outputFile, int requestCode) {
        if (!isCameraUseAble(activity)) {
            Toast.makeText(activity, R.string.cube_photo_no_camera, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfoList == null && resolveInfoList.size() == 0) {
            Toast.makeText(activity, R.string.cube_photo_can_not_use_camera, Toast.LENGTH_LONG).show();
            return;
        }
        Uri imageUri = Uri.fromFile(outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void toAlbum(Activity activity, int requestCode) {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast toast = Toast.makeText(activity, R.string.cube_photo_can_not_open_album, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public static void toCrop(Activity activity, File file, File outputFile, CropOption info, int requestCode) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (info.outputX >= 0) {
            intent.putExtra("outputX", info.outputX);
        }
        if (info.outputY > 0) {
            intent.putExtra("outputY", info.outputY);
        }
        intent.putExtra("aspectX", info.aspectX);
        intent.putExtra("aspectY", info.aspectY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        activity.startActivityForResult(intent, requestCode);
    }

    @SuppressLint("NewApi")
    public static String uriToPath(Context activity, Uri uri) {
        if (null == uri) {
            return null;
        }
        String urlStr = uri.toString();
        if (urlStr.startsWith("file://")) {
            return uri.getPath();
        }
        Cursor cursor = null;
        String idWhere;
        String id;
        String[] columns = {MediaStore.Images.Media.DATA};
        try {
            if (Build.VERSION.SDK_INT == 19 && DocumentsContract.isDocumentUri(activity, uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                id = split[1];
                idWhere = MediaStore.Images.Media._ID + "=?";
                cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, idWhere, new String[]{id}, null);
            } else {
                cursor = activity.getContentResolver().query(uri, columns, null, null, null);
            }
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
