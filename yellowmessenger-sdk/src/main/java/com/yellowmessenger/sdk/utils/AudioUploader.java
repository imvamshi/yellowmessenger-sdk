package com.yellowmessenger.sdk.utils;

import android.content.Context;
import android.util.Log;

import com.yellowmessenger.sdk.upload.MultipartUploadRequest;

public class AudioUploader {

    public static void uploadMultipart(final Context context, String filePath, String language) {

        final MultipartUploadRequest request =
                new MultipartUploadRequest(context,
                        "audio",
                        "https://dev.liv.ai/liv_transcription_api/recordings/");

        try{
            request.addFileToUpload(filePath,
                    "audio_file",
                    null,
                    "audio/mp3");
        }catch (Exception e){
            return;
        }

        //and parameters
        request.addParameter("language", language);
        request.addHeader("Authorization","Token a2cd6fa0fa6d0267436c3a832cd5efeb6029cc4f");


        request.setMaxRetries(2);

        try {
            //Start upload service and display the notification
            request.startUpload();

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete upload request
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }
    }

}
