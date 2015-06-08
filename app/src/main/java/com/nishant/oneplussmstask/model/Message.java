package com.nishant.oneplussmstask.model;

import android.net.Uri;

/**
 * Created by NISHAnT on 6/2/2015.
 */
public class Message {
    private long id;
    private boolean isMe;
    private String message;
    private String phone_number;
    private String dateTime;
    private boolean isMMS;
    private Uri imageUri;

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isMMS() {
        return isMMS;
    }

    public void setMMS(boolean isMMS) {
        this.isMMS = isMMS;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
