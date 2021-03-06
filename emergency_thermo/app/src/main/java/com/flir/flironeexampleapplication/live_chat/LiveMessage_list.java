package com.flir.flironeexampleapplication.live_chat;

/**
 *
 */
public class LiveMessage_list {


    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;

    private int mType;
    private String mMessage;
    private String mUsername;
    private String image_uri;

    private LiveMessage_list() {}

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };

    public String getImage_uri(){return image_uri;};



    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private String mimage_uri;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder image_uri(String image_uri){

            mimage_uri = image_uri;

            return this;

        }

        public LiveMessage_list build() {
            LiveMessage_list message = new LiveMessage_list();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            message.image_uri= mimage_uri;
            return message;
        }
    }
}
