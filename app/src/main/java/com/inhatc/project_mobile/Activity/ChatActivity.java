package com.inhatc.project_mobile.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.inhatc.project_mobile.ChatAdapter;
import com.inhatc.project_mobile.ChatMessage;
import com.inhatc.project_mobile.KeyboardVisibilityUtils;
import com.inhatc.project_mobile.R;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private KeyboardVisibilityUtils keyboardVisibilityUtils;
    private String loginUID;
    private String otherUID;
    private String roomKey;
    private String roomName;
    private String userName;
    private List<ChatMessage> chatMessages;

    private FirebaseDatabase mFirebase;
    private DatabaseReference mDatabase = null;
    private ListView chatListView;
    private TextView txtTitle;
    private EditText edtInputMessage;
    private ImageButton btnInput;
    private ChatAdapter chatAdapter;
    private ChatMessage chatMessage;

    private boolean roomCheck = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        /*
            loginUID[로그인 User UID]              :-> 내가 포함된 채팅방을 찾을 때 사용
            otherUID[이전 Activity 클릭한 사람의 UID] :-> 내가 클릭한 사람의 채팅방 정보를 받거나, 생성할 때 사용
            roomKey[채팅방의 고유한 값]               :-> 채팅방의 대한 메세지 기록이 보관된 칼럼의 UID, 이전 메시지 기록들을 찾을때 사용
            roomName[채팅방 상단 제목]                :-> 내가 클릭한 사람의 이름으로 채팅방 제목이 설정됨
         */

        // 이전 액티비티에서 넘어온 값 받기
        Bundle extras = getIntent().getExtras();
        loginUID = extras.getString("loginUID");
        otherUID = extras.getString("otherUID");
        roomKey = extras.getString("roomKey");
        roomName = extras.getString("roomName");


        // 초기화
        txtTitle = findViewById(R.id.txtRoomName);
        txtTitle.setText(roomName+"님과의 채팅방");
        chatListView = findViewById(R.id.chatListView);
        edtInputMessage = findViewById(R.id.edtChat);
        btnInput = findViewById(R.id.btnSend);
        btnInput.setOnClickListener(this);

        //scrollView
        ScrollView sv_root = findViewById(R.id.sv_root);
        keyboardVisibilityUtils = new KeyboardVisibilityUtils(getWindow(), new KeyboardVisibilityUtils.OnKeyboardVisibilityChangeListener(){
            @Override
            public void onShowKeyboard(int keyboardHeight) {
                sv_root.post(new Runnable() {
                    @Override
                    public void run() {
                        sv_root.smoothScrollTo(sv_root.getScrollX(), sv_root.getScrollY() + keyboardHeight);
                    }
                });
            }
            @Override
            public void onHideKeyboard(){

            }
        });

        // 채팅 리스트 초기화
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatActivity.this, chatMessages, loginUID);
        chatListView.setAdapter(chatAdapter);

        mFirebase = FirebaseDatabase.getInstance();
        // 최초로 값 가져오기
        getMessageList();
    }

    @Override
    public void onClick(View v){
        if(v == btnInput){
            mDatabase = mFirebase.getReference("messages");

            // 시간순대로 정렬할 Timestamp 선언, 모종의 이유로 KST가 이상한 값으로 나옴
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // chatMessage 객체에 담을 값 초기화
            String message = edtInputMessage.getText().toString();
            String name = userName;
            String timestamp = now.toString();
            UUID uid = UUID.randomUUID();

            // chatMessage 객체 생성
            chatMessage = new ChatMessage(message, name, timestamp, loginUID);

            // Firebase RealTime Db에 넣을 Object 선언
            HashMap<String, Object> map = new HashMap<>();
            map.put("message",message);
            map.put("name",name);
            map.put("timestamp",timestamp.toString());
            map.put("uid",loginUID);

            // Firebase RealTime DB 칼럼 유무 확인
            // onCreate()에서 최초 getMessageList() 실행시 roomCheck 결정남
            // roomCheck : true  -> 기존 칼럼 있음, 값 업데이트
            // roomCheck : false -> 기존 칼럼 없음, 칼럼 생성 및 값 생성
            if(roomCheck) {
                mDatabase.child(roomKey).child(uid.toString()).updateChildren(map);
                mFirebase.getReference("lastmessage").child(roomKey).updateChildren(map);
                getMessage();
            }
            else{
                mDatabase.child(roomKey).child(uid.toString()).setValue(chatMessage);
                mFirebase.getReference("lastmessage").child(roomKey).setValue(map);
                getMessageList();
                roomCheck = true;
            }
            edtInputMessage.setText("");
        }
    }

    public void getMessage(){
        // 추가되는 메시지만 ListView에 추가하는 메소드
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.add(chatMessage);
                chatAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
    }

    public void getMessageList() {
        // ListView에 채팅기록을 등록하는 메소드
        // roomCheck로 칼럼 유무도 확인함
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                // 채팅방 제목을 표출하기 위한 임시값
                HashMap<String,String> map = (HashMap<String, String>) task.getResult().child("users").child(loginUID).getValue();
                userName = map.get("name");

                // messages 칼럼 및에 roomKey가 있다면, 기존 채팅목록 불러오기
                if (task.getResult().child("messages").hasChild(roomKey)) {
                    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
                    Query query = messagesRef.child(roomKey).orderByChild("timestamp");
                    // timestamp 기준으로 정렬
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            chatMessages.add(chatMessage);
                            chatAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                } else{
                    roomCheck = false;
                }
            }
        });
    }
}