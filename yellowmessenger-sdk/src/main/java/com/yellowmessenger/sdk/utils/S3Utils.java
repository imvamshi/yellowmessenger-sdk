package com.yellowmessenger.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.yellowmessenger.sdk.models.XMPPUser;
import com.yellowmessenger.sdk.upload.MultipartUploadRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class S3Utils {

    public static void uploadMultipart(final Context context, String filePath, XMPPUser user, String policyEncoded, String signature, String accessKey, String filename) {

        Bitmap bmp = BitmapFactory.decodeFile(filePath);

        int width = bmp.getWidth();
        int height = bmp.getHeight();

        int reqWidth = width>1200?1200:width;
        int reqHeight = (int)(height*(reqWidth/(double)width));

        Matrix m = new Matrix();
        m.postRotate(getImageOrientation(filePath));
        m.setRectToRect(new RectF(0, 0, bmp.getWidth(), bmp.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        newBmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            boolean created = destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bos.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final MultipartUploadRequest request =
                new MultipartUploadRequest(context,
                        filename,
                        "https://consoleuploads.s3.amazonaws.com/");

        try{
            request.addFileToUpload(destination.getAbsolutePath(),
                    "file",
                    filename,
                    "image/jpeg");
        }catch (Exception e){
            return;
        }


        //and parameters
        request.addParameter("AWSAccessKeyId", accessKey);
        request.addParameter("policy", policyEncoded);
        request.addParameter("signature", signature);
        request.addParameter("success_action_status", "201");
        request.addParameter("acl", "public-read");
        request.addParameter("key", "uploads/"+user.getUsername() + "/" + filename);
        request.addParameter("Content-Type", "image/jpeg");

        /*//If you want to add a parameter with multiple values, you can do the following:
        request.addParameter("array-parameter-name", "value1");
        request.addParameter("array-parameter-name", "value2");
        request.addParameter("array-parameter-name", "valueN");

        //or
        String[] values = new String[] {"value1", "value2", "valueN"};
        request.addArrayParameter("array-parameter-name", values);*/

        //or
        /*
        List<String> valuesList = new ArrayList<String>();
        valuesList.add("value1");
        valuesList.add("value2");
        valuesList.add("valueN");
        request.addArrayParameter("array-parameter-name", valuesList);
        */

        // UploadNotificationConfig config = new UploadNotificationConfig();

        //configure the notification
        //the last two boolean parameters sets:
        // - whether or not to auto clear the notification after a successful upload
        // - whether or not to clear the notification after the user taps on it
        // request.setNotificationConfig(config);

        // set a custom user agent string for the upload request
        // if you comment the following line, the system default user-agent will be used
        request.setCustomUserAgent("UploadServiceDemo/1.0");

        // set the intent to perform when the user taps on the upload notification.
        // currently tested only with intents that launches an activity
        // if you comment this line, no action will be performed when the user taps
        // on the notification
        // request.setNotificationClickIntent(new Intent(context, Chat.class));

        // set the maximum number of automatic upload retries on error
        request.setMaxRetries(2);

        try {
            //Start upload service and display the notification
            request.startUpload();

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete upload request
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }
    }

    public static int getImageOrientation(String imagePath){
        int rotate = 0;
        try {

            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }
}
