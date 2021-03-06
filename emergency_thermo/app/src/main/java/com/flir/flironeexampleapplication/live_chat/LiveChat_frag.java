package com.flir.flironeexampleapplication.live_chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flir.flironeexampleapplication.util.Send_to_server;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.flir.flironeexampleapplication.R;


/**
 *
 */
public class LiveChat_frag extends Fragment

    {

       public String imagePath;
        public TextView photo_uri;
        String photo_uri_string;
        public View rootView;
        public CheckBox image_check;
        private static final int REQUEST_LOGIN = 0;

        private static final int TYPING_TIMER_LENGTH = 600;

        private RecyclerView mMessagesView;
        private EditText mInputMessageView;
        private List<LiveMessage_list> mMessages = new ArrayList<LiveMessage_list>();
        private RecyclerView.Adapter mAdapter;
        private boolean mTyping = false;
        private Handler mTypingHandler = new Handler();
        private String mUsername;
        private Socket mSocket;
         public  int numUsers;
        {
            try {
                mSocket = IO.socket(Constants.CHAT_SERVER_URL);
            } catch (URISyntaxException e) {

                throw new RuntimeException(e);

            }
        }

        public LiveChat_frag() {
     super();
    }


        @Override
        public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter(activity, mMessages);
    }

        @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);



                mUsername = "patient_one";



        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);

        mSocket.on("login", onLogin);
        mSocket.on("typing", onTyping);
            mSocket.on("stop typing", onStopTyping);
        mSocket.connect();


            if (mSocket.connected() ==false){



                connectUserToSocket(mUsername);

            }


    }//end create

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

             rootView = inflater.inflate(R.layout.livechat_frag_____fragment_main, container, false); //inflater.inflate(R.layout.guide_frag, container, false);
//from getting pic

            Intent intent = getActivity().getIntent();
            imagePath = intent.getStringExtra("imagepath");


            image_check = (CheckBox) rootView.findViewById(R.id.image_checked);

            photo_uri = (TextView) rootView.findViewById(R.id.photo_uri);
            photo_uri.setText(imagePath);

            photo_uri_string = (String) photo_uri.getText();

            ImageButton imageView = (ImageButton) rootView.findViewById(R.id.send_image);
            imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));

           Toast t= Toast.makeText(getContext(), ""+imagePath,  Toast.LENGTH_SHORT);
           t.show();

        return rootView;

    }
//connect guy to socket

        public void connectUserToSocket(String username){

            mSocket.emit("add user", username);

        }//conncext to socket

        @Override
        public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
     //login off
        mSocket.off("login", onLogin);
            mSocket.off("stop typing", onStopTyping);

    }//destroy

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);



        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
         //send image
                    attemptSend(photo_uri_string, image_check);
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    photo_uri = (TextView) rootView.findViewById(R.id.photo_uri);
                    photo_uri_string = ""+ photo_uri.getText();

                    Toast tt = Toast.makeText(getActivity(), "ur:"+ photo_uri_string, Toast.LENGTH_SHORT);
                    tt.show();

                   Send_to_server sendserver = new Send_to_server(photo_uri_string, getActivity());

                    sendserver.execute();

                    AsyncTask.Status stat =  sendserver.getStatus();

                    Log.e("fuck", ""+sendserver.getStatus());

                    if(sendserver.getStatus().equals(AsyncTask.Status.RUNNING)){

                        Toast.makeText(getContext(), "finisehdd async", Toast.LENGTH_SHORT).show();

                        attemptSend(photo_uri_string, image_check);
                    }

                   // attemptSend(photo_uri_string, image_check);


            }
        });


    }//oncreate


    private void addLog(String message) {
        mMessages.add(new LiveMessage_list.Builder(LiveMessage_list.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void addMessage(String username, String message) {
        mMessages.add(new LiveMessage_list.Builder(LiveMessage_list.TYPE_MESSAGE)
                .username(username).message(message).image_uri(photo_uri_string).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        mMessages.add(new LiveMessage_list.Builder(LiveMessage_list.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            LiveMessage_list message = mMessages.get(i);
            if (message.getType() == LiveMessage_list.TYPE_ACTION && message.getUsername().equals(username)) {

                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);

            }
        }
    }
//send message
    public void attemptSend(String image_uri, CheckBox check_image) {


        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");

        addMessage(mUsername, message); //on user interface

        // perform the sending message attempt.
      //  String json = "{\"message\":\""+message+ "\", \"image\" : \"http://tanggoal.com/public/uploads/members_pic/14317887721427390234IMG_20131202_204726.jpg\"}";

        //TextView imageurl = (TextView)

        try {

            JSONObject jObjectType = new JSONObject();

            jObjectType.put("message", message);

            String[] imageAr = image_uri.split("/");

            jObjectType.put("image", "http://tanggoal.com/public/uploads/hackathon/"+imageAr[imageAr.length-1]);

            Toast ta= Toast.makeText(getContext(), "arr: "+imageAr[imageAr.length-1], Toast.LENGTH_SHORT);
            ta.show();

            mSocket.emit("new message", jObjectType);



        }catch (JSONException e){

            Log.e("json:",""+e );
        }//end json
    }

    private void startSignIn() {
        mUsername = null;
        Intent intent = new Intent(getActivity(), LiveLoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void leave() {
        mUsername = null;
        mSocket.disconnect();
       // mSocket.connect();
       // startSignIn();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    removeTyping(username);
                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(username);
                }
            });
        }//end call
    };

//onlogin

       /* private Emitter.Listener onLogin = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];


                try {
                    numUsers = data.getInt("numUsers");

                    Intent intent = new Intent();
                    intent.putExtra("username", mUsername);
                    intent.putExtra("numUsers", numUsers);
                    getActivity().setResult(getActivity().RESULT_OK, intent);



                } catch (JSONException e) {
                    return;
                }

               // addLog(getResources().getString(R.string.message_welcome));
               // addParticipantsLog(numUsers);
            }//end call
        };*/

        private Emitter.Listener onLogin = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        //String username;

                        try {
                            //username = data.getString("username");
                            numUsers = data.getInt("numUsers");
                        } catch (JSONException e) {
                            return;
                        }

                        addLog(getResources().getString(R.string.message_welcome));
                        addParticipantsLog(numUsers);
                    }
                });
            }
        };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };
}
