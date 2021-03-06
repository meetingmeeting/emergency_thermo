package com.flir.flironeexampleapplication.live_chat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flir.flironeexampleapplication.R;

import java.util.List;

/**
 *
 */
public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<LiveMessage_list> mMessages;
    private int[] mUsernameColors;

    public MessageAdapter(Context context, List<LiveMessage_list> LiveMessage_lists) {
        mMessages = LiveMessage_lists;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case LiveMessage_list.TYPE_MESSAGE:
                layout = R.layout.messageadapter_____item_message;
                break;
            case LiveMessage_list.TYPE_LOG:
                layout = R.layout.messageadapter_____item_log;
                break;
            case LiveMessage_list.TYPE_ACTION:
                layout = R.layout.messageadapter_____item_action;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        LiveMessage_list message = mMessages.get(position);

        viewHolder.setMessage(message.getMessage());
        viewHolder.setUsername(message.getUsername());
        viewHolder.setChat_image(message.getImage_uri());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private TextView mMessageView;
        private ImageView chat_image;

        public ViewHolder(View itemView) {
            super(itemView);

            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            chat_image = (ImageView) itemView.findViewById(R.id.chat_image);

        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
            mUsernameView.setTextColor(getUsernameColor(username));
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
        }
        public void setChat_image(String image_uri){
            if(image_uri==null){
                return ;
            }
           // chat_image.setImageBitmap();
            chat_image.setImageBitmap(BitmapFactory.decodeFile(image_uri));

        }

        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }
    }
}
