package com.e.androidtalk.views;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e.androidtalk.R;
import com.e.androidtalk.adapters.MessageListAdapter;
import com.e.androidtalk.models.Chat;
import com.e.androidtalk.models.Message;
import com.e.androidtalk.models.PhotoMessage;
import com.e.androidtalk.models.TextMessage;
import com.e.androidtalk.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    private String mChatId;

    @BindView(R.id.senderBtn)
    ImageView mSenderButton;

    @BindView(R.id.edtContent)
    EditText mMessageText;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.chat_rec_view)
    RecyclerView mChatRecyclerView;
    private MessageListAdapter messageListAdapter;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemeberRef;
    private DatabaseReference mChatMessageRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mFirebaseUser;
    private static final int TAKE_PHOTO_REQUEST_CODE = 201;
    private StorageReference mImageStorageRef;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_chat);
        ButterKnife.bind(this);

        mChatId = getIntent().getStringExtra("chat_id");
        mFirebaseDb = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mFirebaseDb.getReference("users");
//        mToolbar.setTitleTextColor(Color.WHITE);
        if ( mChatId != null ) {
            mChatRef = mFirebaseDb.getReference("users").child(mFirebaseUser.getUid()).child("chats").child(mChatId);
            mChatMessageRef = mFirebaseDb.getReference("chat_messages").child(mChatId);
            mChatMemeberRef = mFirebaseDb.getReference("chat_members").child(mChatId);
            ChatFragment.JOINED_ROOM = mChatId;
            initTotalunreadCount();
        } else {
            Log.d("log1","mchatid is null");
            mChatRef = mFirebaseDb.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        }
        messageListAdapter = new MessageListAdapter();
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setAdapter(messageListAdapter);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void initTotalunreadCount(){
        mChatRef.child("totalUnreadCount").setValue(0);
    }

    MessageEventListener mMessageEventListener = new MessageEventListener();

    @Override
    protected void onPause() {
        super.onPause();
        if (mChatId != null) {
            removeMessageListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatId != null) {

            // 총 메세지의 카운터를 가져온다.
            // onchildadded 호출한 변수의 값이 총메세지의 값과 크거나 같다면, 포커스를 맨아래로 내려줍니다.
            mChatMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long totalMessageCount =  dataSnapshot.getChildrenCount();
                    mMessageEventListener.setTotalMessageCount(totalMessageCount);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            messageListAdapter.clearItem();
            addChatListener();
            addMessageListener();
        }
    }

    private void addChatListener(){
        //Single 에서 Value 이벤트로 바꿔서 계속 실행되도록
        mChatRef.child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.getValue(String.class);
                if ( title != null ) {
                    mToolbar.setTitle(title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addMessageListener(){
        mChatMessageRef.addChildEventListener(mMessageEventListener);
    }

    private void removeMessageListener() {
        mChatMessageRef.removeEventListener(mMessageEventListener);
    }


    private class MessageEventListener implements ChildEventListener {

        private long totalMessageCount;

        private long callCount = 1;

        public void setTotalMessageCount(long totalMessageCount) {
            this.totalMessageCount = totalMessageCount;
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


            //여기 임의로 추가함 나중에 바꾸기
            ///////////////////////////////////////
            Message item = snapshot.getValue(Message.class);
            List<String> readUserUIDList = item.getReadUserList();
            if ( readUserUIDList != null ) {
                if (!readUserUIDList.contains(mFirebaseUser.getUid())) {
                    // chat_messages > {chat_id} > {message_id} >  unreadCount -= 1
                    // readUserList에 내 uid 추가
                    // messageRef.setValue();
                    // 동시에 값이 반영되는 경우에는 트랜잭션을 이용
                    snapshot.getRef().runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Message mutableMessage = currentData.getValue(Message.class);
                            // readUserList에 내 uid 추가
                            // unreadCount -= 1

                            List<String> mutabledReadUserList = mutableMessage.getReadUserList();
                            mutabledReadUserList.add(mFirebaseUser.getUid());
                            int mutableUnreadCount = mutableMessage.getUnreadCount() - 1;

                            if (mutableMessage.getMessageType() == Message.MessageType.PHOTO) {
                                PhotoMessage mutablePhotoMessage = currentData.getValue(PhotoMessage.class);
                                mutablePhotoMessage.setReadUserList(mutabledReadUserList);
                                mutablePhotoMessage.setUnreadCount(mutableUnreadCount);
                                currentData.setValue(mutablePhotoMessage);
                            } else {
                                TextMessage mutableTextMessage = currentData.getValue(TextMessage.class);
                                mutableTextMessage.setReadUserList(mutabledReadUserList);
                                mutableTextMessage.setUnreadCount(mutableUnreadCount);
                                currentData.setValue(mutableTextMessage);
                            }
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                            //0.5 초 정도 후에 언리드카운트의 값을 초기화.
                            // Timer // TimeTask
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    initTotalunreadCount();
                                }
                            }, 500);
                        }
                    });
                }
            }
            // 읽음 처리
            // chat_messages > {chat_id} > {message_id} > readUserList
            // 내가 존재 하는지를 확인
            // 존재한다면
            // 존재 하지 않는다면
            // chat_messages > {chat_id} > {message_id} >  unreadCount -= 1
            // readUserList에 내 uid 추가

            // ui
            if ( item.getMessageType() == Message.MessageType.TEXT ) {
                TextMessage textMessage = snapshot.getValue(TextMessage.class);
                messageListAdapter.addItem(textMessage);
            } else if ( item.getMessageType() == Message.MessageType.PHOTO ){
                PhotoMessage photoMessage = snapshot.getValue(PhotoMessage.class);
                messageListAdapter.addItem(photoMessage);
            } else if ( item.getMessageType() == Message.MessageType.EXIT ){
                messageListAdapter.addItem(item);
            }

            if ( callCount >= totalMessageCount) {
                // 스크롤을 맨 마지막으로 내린다.
                mChatRecyclerView.scrollToPosition(messageListAdapter.getItemCount() - 1);
            }
            callCount++;
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            // 변경된 메세지 ( unreadCount)
            // 아답터쪽에 변경된 메세지데이터를 전달하고
            // 메시지 아이디 번호로 해당 메세지의 위치를 알아내서
            // 알아낸 위치값을 이용해서 메세지 리스트의 값을 변경할 예정입니다.
            Message item = snapshot.getValue(Message.class);

            if ( item.getMessageType() == Message.MessageType.TEXT ) {
                TextMessage textMessage = snapshot.getValue(TextMessage.class);
                messageListAdapter.updateItem(textMessage);
            } else if ( item.getMessageType() == Message.MessageType.PHOTO ){
                PhotoMessage photoMessage = snapshot.getValue(PhotoMessage.class);
                messageListAdapter.updateItem(photoMessage);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    @OnClick(R.id.senderBtn)
    public void onSendEvent(View v){
        if ( mChatId != null ) {
            sendMessage();
        } else {
            createChat();
        }
    }

    @OnClick(R.id.photoSend)
    public void onPhotoSendEvent(View v) {
        // 안드로이드 파일창 오픈 (갤러리 오픈)
        // requestcode = 201
        //TAKE_PHOTO_REQUEST_CODE

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == TAKE_PHOTO_REQUEST_CODE ) {
            if ( data != null ) {

                // 업로드 이미지를 처리 합니다.
                // 이미지 업로드가 완료된 경우
                // 실제 web 에 업로드 된 주소를 받아서 photoUrl로 저장
                // 그다음 포토메세지 발송
                Log.d("log1","requset image");
                uploadImage(data.getData());

            }
        }
    }
//
    private String mPhotoUrl = null;
    private Message.MessageType mMessageType = Message.MessageType.TEXT;

    private void uploadImage(Uri data){
        if ( mImageStorageRef == null ) {
            mImageStorageRef = FirebaseStorage.getInstance().getReference("/chats/").child(mChatId);
        }
        Log.d("log1","uploadimage");
//        mImageStorageRef.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//               Log.d("log1","photo complete");
//                if ( task.isSuccessful() ) {
//                    mPhotoUrl = task.getResult().getUploadSessionUri().toString();
//                    mMessageType = Message.MessageType.PHOTO;
//                    sendMessage();
//                }
//            }
//        });

        mImageStorageRef.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mImageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
//                        mPhotoUrl = taskSnapshot.getUploadSessionUri().toString();
                        mPhotoUrl = uri.toString();
                        Log.d("log1","photosuccess : " + mPhotoUrl);
                        mMessageType = Message.MessageType.PHOTO;
                        sendMessage();
                    }
                });

            }
        });
        //firebase Storage
    }


    private Message message = new Message();
    private void sendMessage(){
//        // 메세지 키 생성
        Log.d("log1","sendmessage");
        mChatMessageRef = mFirebaseDb.getReference("chat_messages").child(mChatId);
//        // chat_message>{chat_id}>{message_id} > messageInfo
        String messageId = mChatMessageRef.push().getKey();
        String messageText = mMessageText.getText().toString();
//
        final Bundle bundle = new Bundle();
        bundle.putString("me", mFirebaseUser.getEmail());
        bundle.putString("roomId", mChatId);
//
        if ( mMessageType == Message.MessageType.TEXT ) {
            if ( messageText.isEmpty()) {
                return;
            }
            message = new TextMessage();
            ((TextMessage)message).setMessageText(messageText.trim());
            bundle.putString("messageType", Message.MessageType.TEXT.toString());
        } else if ( mMessageType == Message.MessageType.PHOTO ){
            message = new PhotoMessage();
            ((PhotoMessage)message).setPhotoUrl(mPhotoUrl);
            bundle.putString("messageType", Message.MessageType.PHOTO.toString());
        }

        message.setMessageDate(new Date());
        message.setChatId(mChatId);
        message.setMessageId(messageId);
        message.setMessageType(mMessageType);
//        message.setMessageType(Message.MessageType.TEXT);
        message.setMessageUser(new User(mFirebaseUser.getUid(), mFirebaseUser.getEmail(), mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString()));
        message.setReadUserList(Arrays.asList(new String[]{mFirebaseUser.getUid()}));
//
        String [] uids = getIntent().getStringArrayExtra("uids");
        if ( uids != null ) {
            message.setUnreadCount(uids.length-1);
        }
        mMessageText.setText("");
        mMessageType = Message.MessageType.TEXT;
        mChatMemeberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
//                //unreadCount 셋팅하기 위한 대화 상대의 수를 가져 옵니다.
                long memberCount = dataSnapshot.getChildrenCount();
                message.setUnreadCount((int)memberCount - 1);
                mChatMessageRef.child(message.getMessageId()).setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
                        mFirebaseAnalytics.logEvent("sendMessage", bundle);
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        while( memberIterator.hasNext()) {
                            User chatMember = memberIterator.next().getValue(User.class);
                            mUserRef
                                    .child(chatMember.getUid())
                                    .child("chats")
                                    .child(mChatId)
                                    .child("lastMessage")
                                    .setValue(message);

                            if ( !chatMember.getUid().equals(mFirebaseUser.getUid())) {
                                // 공유되는 증가카운트의 경우 transaction을 이용하여 처리합니다.
                                mUserRef
                                        .child(chatMember.getUid())
                                        .child("chats")
                                        .child(mChatId)
                                        .child("totalUnreadCount")
                                        //방 생성과 메시지 전송 강의에서는 코드 다르다
                                        //totalunreadcount 증가안한다
                                        .runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                long totalUnreadCount = mutableData.getValue(long.class) == null ? 0 : mutableData.getValue(long.class);
                                                mutableData.setValue(totalUnreadCount + 1);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                            }
                                        });
                            }
                        }
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isSentMessage = false;
    private void createChat() {
//        // <방생성>
//
//        // 0. 방 정보 설정 <-- 기존 방이어야 가능함.
//
//        // 1. 대화 상대에 내가 선택한 사람 추가
//
//        // 2. 각 상대별 chats에 방추가
//
//        // 3. 메세지 정보 중 읽은 사람에 내 정보를 추가
//
//        // 4. 4.  첫 메세지 전송
//
        final Chat chat = new Chat();
        Log.d("log1","1");
        mChatId = mChatRef.push().getKey();
        mChatRef = mChatRef.child(mChatId);
        mChatMemeberRef = mFirebaseDb.getReference("chat_members").child(mChatId);
        chat.setChatId(mChatId);
        chat.setCreateDate(new Date());
        String uid = getIntent().getStringExtra("uid");
        String [] uids = getIntent().getStringArrayExtra("uids");
        if ( uid != null ) {
            // 1:1
            uids = new String[]{uid};
        }
        List<String> uidList = new ArrayList<>(Arrays.asList(uids));
        uidList.add(mFirebaseUser.getUid());
        for ( String userId : uidList ) {
            // uid > userInfo
            mUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    User member = dataSnapshot.getValue(User.class);
                    mChatMemeberRef.child(member.getUid())
                            .setValue(member, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                    // USERS>uid>chats>{chat_id}>chatinfo
                                    dataSnapshot.getRef().child("chats").child(mChatId).setValue(chat);
                                    if ( !isSentMessage ) {
                                        sendMessage();
                                        addChatListener();
                                        addMessageListener();
                                        isSentMessage = true;
//
                                        Bundle bundle = new Bundle();
                                        bundle.putString("me", mFirebaseUser.getEmail());
                                        bundle.putString("roomId", mChatId);
                                        mFirebaseAnalytics.logEvent("createChat", bundle);
                                        ChatFragment.JOINED_ROOM = mChatId;

                                    }

                                }
                            });
//
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
//        // users > {uid} > chats > {chat_uid}
    }

}
