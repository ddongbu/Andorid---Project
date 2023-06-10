package com.inhatc.project_mobile.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inhatc.project_mobile.ChatAdapter;
import com.inhatc.project_mobile.ChatMessage;
import com.inhatc.project_mobile.R;
import com.inhatc.project_mobile.RoomAdapter;
import com.inhatc.project_mobile.User;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppMainActivity extends AppCompatActivity implements View.OnClickListener{

    private TabHost tabHost;
    private TabHost.TabSpec tabSpec;
    private ListView userList;
    private FirebaseDatabase mFirebase;
    private DatabaseReference mDatabase = null;
    private ArrayAdapter<String> adpater;
    private ArrayList<ChatMessage> chatRoomMessage;
    private RoomAdapter roomAdapter;
    private TextView txtMyName;
    private TextView txtMyEmail;
    private EditText edtSearch;
    private Button btnSearch;
    private Button btnFriInsert;
    private TextView txtSearchEmail;
    private TextView txtSearchName;
    private ListView roomList;
    private String loginUID;
    private List<User> tempList;
    private String roomName;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appmain);

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnFriInsert = findViewById(R.id.btnFriInsert);
        btnSearch.setOnClickListener(this);
        btnFriInsert.setOnClickListener(this);
        txtSearchName = findViewById(R.id.txtSearchName);
        txtSearchEmail = findViewById(R.id.txtSearchEmail);
        txtMyName = findViewById(R.id.txtMyName);
        txtMyEmail = findViewById(R.id.txtMyEmail);
        userList = findViewById(R.id.lstUser);
        roomList = findViewById(R.id.lstRoom);


        TextView tab1TextView = new TextView(this);
        tab1TextView.setText("친구목록");
        tab1TextView.setTextColor(Color.WHITE); // Set the desired text color
        tab1TextView.setGravity(Gravity.CENTER); // Align the text to the center
        tab1TextView.setTypeface(null, Typeface.BOLD); // Set the font to bold

        TextView tab2TextView = new TextView(this);
        tab2TextView.setText("메시지");
        tab2TextView.setTextColor(Color.WHITE); // Set the desired text color
        tab2TextView.setGravity(Gravity.CENTER); // Align the text to the center
        tab2TextView.setTypeface(null, Typeface.BOLD); // Set the font to bold

        TextView tab3TextView = new TextView(this);
        tab3TextView.setText("친구추가");
        tab3TextView.setTextColor(Color.WHITE);
        tab3TextView.setGravity(Gravity.CENTER); // Align the text to the center
        tab3TextView.setTypeface(null, Typeface.BOLD); // Set the font to bold

        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();

        tabSpec = tabHost.newTabSpec("친구목록").setIndicator(tab1TextView).setContent(R.id.tab1);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("메시지").setIndicator(tab2TextView).setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("친구추가").setIndicator(tab3TextView).setContent(R.id.tab3);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        mFirebase = FirebaseDatabase.getInstance();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            // 전달된 데이터 가져오기
            loginUID = extras.getString("UID");
        }

        chatRoomMessage = new ArrayList<>();
        roomAdapter = new RoomAdapter(this, chatRoomMessage);
        roomList.setAdapter(roomAdapter);

        //유저 목록 출력
        getUserList();
        //채팅방 목록 출력
        getChatRoomList();

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                roomName = tempList.get(position).getName();
                isChatRoom(tempList.get(position).getUid());
            }
        });

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] info = chatRoomMessage.get(position).getUid().split("@");
                // 0 -> otherKey | 1 -> roomKey | 2 -> otherName
                roomName = info[2];
                goChatRoom(info[0], info[1]);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        //채팅방에서 뒤로 갔을때 채팅방목록 리스트 갱신
        chatRoomMessage = new ArrayList<>();
        roomAdapter = new RoomAdapter(this, chatRoomMessage);
        roomList.setAdapter(roomAdapter);
        getChatRoomList();

        // 액티비티 재실행과 관련된 로직을 여기에 작성합니다.
        // 예를 들면 데이터 업데이트, 화면 갱신 등의 작업을 수행할 수 있습니다.
    }
    @Override
    public void onClick(View v){
        if(v == btnSearch){
            //해당 user 찾는 메소드
            getSearchUser(edtSearch.getText().toString());
        }
        if(v == btnFriInsert){
            //친구 추가 메소드
            friendsListInsert(txtSearchEmail.getText().toString());
        }
    }

    public void goChatRoom(String otherUID, String roomKey){
        Intent chatIntent = new Intent(AppMainActivity.this, ChatActivity.class);
        chatIntent.putExtra("loginUID", loginUID);
        chatIntent.putExtra("otherUID", otherUID);
        chatIntent.putExtra("roomKey", roomKey);
        chatIntent.putExtra("roomName", roomName);
        startActivity(chatIntent);
    }
    public void isChatRoom(String otherUID){
        mDatabase = mFirebase.getReference("chatRoom");
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.e("isChatRoom() :", "DB 연동실패");
                }else{
                    String roomKey;
                    HashMap<String, Object> map = (HashMap<String, Object>) task.getResult().child("chatRooms").getValue();
                    /*
                        DB 구조
                        chatRoom
                            chatRooms
                                users@loginUID@otherUID:true
                     */

                    // 나랑 상대방이 포함된 방이 있는지 찾는다.
                    if(map.containsKey("users@"+loginUID+"@"+otherUID)){
                        roomKey = (String) map.get("users@"+loginUID+"@"+otherUID);
                    }else if(map.containsKey("users@"+otherUID+"@"+loginUID)){
                        roomKey = (String) map.get("users@"+otherUID+"@"+loginUID);
                    }else{
                        // 없으면 생성
                        UUID uid = UUID.randomUUID();
                        Map<String, Object> chatUidSet = new HashMap<>();
                        roomKey = uid.toString();
                        chatUidSet.put("users@"+loginUID+"@"+otherUID,roomKey);
                        mDatabase.child("chatRooms").updateChildren(chatUidSet);
                    }
                    // 해당 채팅방으로 이동하기 위한 메소드
                    goChatRoom(otherUID, roomKey);
                }
            }
        });
    }

    //email을 uid로 변환
    public String emailToUUID(@NonNull Task<DataSnapshot> task, String searchEmail){
        HashMap<String, HashMap<String, Object>> userMap = (HashMap<String, HashMap<String, Object>>) task.getResult().child("users").getValue();
        String[] uuidList = userMap.keySet().toArray(new String[0]);
        String searchUID = null;

        //검색한 이메일의 User UID 찾기
        for(String userUID : uuidList){
            // User UID 탐색중에 로그인한 UID와 같다면 패스
            if(loginUID.equals(userUID.trim())) continue;

            // 검색한 Email과 찾은 Email이 같다면
            String targetEmail = userMap.get(userUID.trim()).get("email").toString();
            if(searchEmail.equals(targetEmail)){
                searchUID = userUID.trim();
                break;
            }
        }
        return searchUID;
    }

    // 친구추가 버튼클릭시
    public void friendsListInsert(String insertEmail){
        Log.d("method :", "friendsListInsert 메소드 실행");
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("friendsListInsert :", "DB 연동실패");
                }
                else {
                    Log.d("friendsListInsert :", "DB 연동성공");

                    // 입력받은 이메일을 통해 UID를 찾는다
                    String searchUID = emailToUUID(task, insertEmail);

                    // 친구목록에 추가를 위한 HashMap
                    HashMap<String, Object> updateMap = new HashMap<>();

                    // friends라는 칼럼이 없다면 내 칼럼 생성 후 UserUID 하단에 친구 추가
                    if(task.getResult().child("friends").getChildrenCount() == 0){
                        updateMap.put(searchUID,true);
                        Log.d("friendsListInsert :", insertEmail+"("+searchUID+") 이 성공적으로 갱신되었습니다.");
                        mFirebase.getReference().child("friends").child(loginUID).setValue(updateMap);
                        return;
                    }
                    // UserUID칼럼이 없다면 생성 후 하단에 친구추가
                    if(task.getResult().child("friends").child(loginUID).getChildrenCount() == 0){
                        updateMap.put(searchUID,true);
                        Log.d("friendsListInsert :", insertEmail+"("+searchUID+") 이 성공적으로 갱신되었습니다.");
                        mFirebase.getReference().child("friends").child(loginUID).setValue(updateMap);
                        return;
                    }

                    // friends칼럼의 모든 값을 가져오는 HashMap
                    HashMap<String, HashMap<String, Object>> friendsMap = (HashMap<String, HashMap<String, Object>>) task.getResult().child("friends").getValue();
                    String[] fuidArray = friendsMap.get(loginUID).keySet().toArray(new String[0]);
                    if(fuidArray.length != 0) {
                        // 내가 찾은 Uid를 friends의 모든 키 값을 하나씩 비교해서 있으면, 친구사이
                        for (String fuid : fuidArray) {
                            if (searchUID.equals(fuid)) {
                                Log.d("friendsListInsert :", "해당 유저와 이미 친구 사이입니다.");
                                return;
                            }
                        }
                    }

                    // 위에서 return으로 종료가 안되면, friends 칼럼 갱신
                    updateMap.put(searchUID, true);
                    mFirebase.getReference().child("friends").child(loginUID).updateChildren(updateMap);
                    Log.d("friendsListInsert :", insertEmail+"("+searchUID+") 이 성공적으로 갱신되었습니다.");
                }
            }
        });
        Log.d("method :", "friendsListInsert 메소드 종료");
    }

    // 이메일 검색시
    public void getSearchUser(String searchEmail){
        Log.d("method :", "getSearchUser 메소드 실행");
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("getSearchUser :", "DB 연동실패");
                }
                else {
                    Log.e("getSearchUser :", "DB 연동성공");
                    String searchUID = emailToUUID(task, searchEmail);
                    if(searchUID == null){
                        txtSearchEmail.setText("");
                        txtSearchName.setText("");
                        btnFriInsert.setVisibility(View.INVISIBLE);
                        Log.d("getSearchUser :", "검색결과가 없습니다.");
                        return;
                    }

                    HashMap<String, HashMap<String, Object>> userMap = (HashMap<String, HashMap<String, Object>>) task.getResult().child("users").getValue();
                    txtSearchName.setText(userMap.get(searchUID).get("name").toString());
                    txtSearchEmail.setText(userMap.get(searchUID).get("email").toString());
                    btnFriInsert.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtSearchEmail.getWindowToken(), 0);
                    Log.d("getSearchUser :", "검색결과(name:"+userMap.get(searchUID).get("name")+"|email:"+userMap.get(searchUID).get("email")+")");
                }
            }
        });
        Log.d("method :", "getSearchUser 메소드 종료");
    }
    public void getUserList() {
        tempList = new ArrayList<>();
        Log.d("method :", "getUserList 메소드 실행");
        mDatabase = mFirebase.getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("getUserList :", "데이터 조회 시작");
                List<String> list = new ArrayList<>();
                List<String> friendsList = new ArrayList<>();
                String friendsKey;

                Log.d("getUserList :", "친구 관계 조회");

                if (dataSnapshot.child("friends").getChildrenCount() == 0) return;
                for (DataSnapshot postSnapshot : dataSnapshot.child("friends").getChildren()) {
                    //친구관계 Key -> (Key -> 친구UID)
                    friendsKey = postSnapshot.getKey().trim();

                    Map<String, Boolean> map = (HashMap<String, Boolean>) postSnapshot.getValue();
                    String[] friendsUID = map.keySet().toArray(new String[0]);

                    //로그인한 사람 UID랑 똑같은 키를 찾아서 저장
                    if (friendsKey.equals(loginUID)) {
                        for (String uid : friendsUID) {
                            //현재 친구인 uid들을 리스트에 저장
                            friendsList.add(uid);
                        }
                    }
                }

                Log.d("getUserList :", "친구 리스트 구현");

                //친구목록
                if (friendsList.size() == 0) return;
                for (DataSnapshot postSnapshot : dataSnapshot.child("users").getChildren()) {
                    friendsKey = postSnapshot.getKey();

                    int keyIndex = friendsList.indexOf(friendsKey);
                    if (keyIndex != -1) {
                        HashMap<String, String> map = (HashMap<String, String>) postSnapshot.getValue();
                        tempList.add(new User(map.get("name"), map.get("email"), friendsKey));
                        list.add(map.get("name")+"("+map.get("email")+")");
                    }
                }

                


                adpater = new ArrayAdapter<String>(AppMainActivity.this, android.R.layout.simple_list_item_1, list);
                userList.setAdapter(adpater);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.d("method :", "getUserList 메소드 종료");
    }


    public void getChatRoomList(){
        // 현재 사용자가 포함된 채팅방을 찾기
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.e("getChatRoomList() :", "DB 연동실패");
                }else{
                    Log.e("getChatRoomList() :", "DB 연동성공");

                    //현재 채팅방 목록을 모두 저장한다.
                    HashMap<String, String> chatRoomList = (HashMap<String, String>) task.getResult().child("chatRoom").child("chatRooms").getValue();
                    String[] chatRoomKeys = chatRoomList.keySet().toArray(new String[0]);

                    for(String key : chatRoomKeys){
                        //채팅방이름에 사용자의 UID가 포함되어있다면
                        if(key.contains(loginUID)){
                            //해당 키가 없다면 종료
                            if(!task.getResult().child("lastmessage").hasChild(chatRoomList.get(key))) continue;

                            // 채팅방에서 상대방의 키랑 이름을 가져옴
                            String targetUserKey = key.replace("users", "").replace(loginUID, "").replace("@", "");
                            String targetUserName = task.getResult().child("users").child(targetUserKey).child("name").getValue().toString();

                            // 리스트뷰에 검증용 myUserName
                            String myUserName = task.getResult().child("users").child(loginUID).child("name").getValue().toString();

                            // 해당방의 마지막채팅 정보를 가져옴
                            ChatMessage chatMessage = task.getResult().child("lastmessage").child(chatRoomList.get(key)).getValue(ChatMessage.class);
                            String checkName = myUserName.equals(chatMessage.getName()) ? "같음" : "다름";

                            // 추후 검증용
                            // checkName => RoomAdapter에서 textView 메세지 세팅용
                            // targetUserName => Roomadapter에서 대화방 이름
                            chatMessage.setName(checkName+"="+targetUserName);

                            // targetUserKey => 대화방 클릭시 상대방 키 전달용
                            // chatRoomList.get(key) => 대화방 클릭시 룸키 전달용
                            // targetUserName => 대화방 클릭시 방 제목 전달용
                            chatMessage.setUid(targetUserKey+"@"+chatRoomList.get(key)+"@"+targetUserName);

                            // listView 업데이트
                            chatRoomMessage.add(chatMessage);
                            roomAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
        // 그 채팅방 RoomKey로 마지막 메세지 찾아오기
    }

}
