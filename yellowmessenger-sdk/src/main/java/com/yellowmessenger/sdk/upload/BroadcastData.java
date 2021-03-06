package com.yellowmessenger.sdk.upload;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class which contains all the data passed in broadcast intents to notify task progress, errors,
 * completion or cancellation.
 *
 * @author gotev (Aleksandar Gotev)
 */
class BroadcastData implements Parcelable {

    public enum Status {
        IN_PROGRESS,
        ERROR,
        COMPLETED,
        CANCELLED
    }

    private Status status;
    private Exception exception;
    private UploadInfo uploadInfo;
    private ServerResponse serverResponse;

    public BroadcastData() {

    }

    public Intent getIntent() {
        Intent intent = new Intent(UploadService.getActionBroadcast());
        intent.putExtra(UploadService.PARAM_BROADCAST_DATA, this);
        return intent;
    }

    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final Creator<BroadcastData> CREATOR =
            new Creator<BroadcastData>() {
                @Override
                public BroadcastData createFromParcel(final Parcel in) {
                    return new BroadcastData(in);
                }

                @Override
                public BroadcastData[] newArray(final int size) {
                    return new BroadcastData[size];
                }
            };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(status.ordinal());
        parcel.writeSerializable(exception);
        parcel.writeParcelable(uploadInfo, flags);
        parcel.writeParcelable(serverResponse, flags);
    }

    private BroadcastData(Parcel in) {
        status = Status.values()[in.readInt()];
        exception = (Exception) in.readSerializable();
        uploadInfo = in.readParcelable(UploadInfo.class.getClassLoader());
        serverResponse = in.readParcelable(ServerResponse.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Status getStatus() {
        return status;
    }

    public BroadcastData setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public BroadcastData setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    public UploadInfo getUploadInfo() {
        return uploadInfo;
    }

    public BroadcastData setUploadInfo(UploadInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
        return this;
    }

    public ServerResponse getServerResponse() {
        return serverResponse;
    }

    public BroadcastData setServerResponse(ServerResponse serverResponse) {
        this.serverResponse = serverResponse;
        return this;
    }
}
