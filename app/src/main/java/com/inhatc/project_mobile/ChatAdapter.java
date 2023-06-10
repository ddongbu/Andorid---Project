package com.inhatc.project_mobile;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {
    private Context mContext;
    private List<ChatMessage> mChatMessages;
    private String uid;


    public ChatAdapter(Context context, List<ChatMessage> chatMessages, String uid) {
        super(context, 0, chatMessages);
        mContext = context;
        mChatMessages = chatMessages;
        this.uid = uid;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = getItem(position);
        String chatUid = chatMessage.getUid().trim();
            if(chatUid.equals(uid)) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_my_message, parent, false);
            } else{
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_other_message, parent, false);
            }

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);

        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        simpleDate.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Timestamp timestamp = Timestamp.valueOf(chatMessage.getTimestamp());
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);


        messageTextView.setText(chatMessage.getMessage());
        nameTextView.setText(chatMessage.getName());
        timeTextView.setText(simpleDate.format(cal.getTime()));

        if (chatUid.equals(uid)) {
            nameTextView.setGravity(Gravity.END); // 우측 정렬
            messageTextView.setGravity(Gravity.END); // 우측 정렬
        } else {
            nameTextView.setGravity(Gravity.START); // 우측 정렬
            messageTextView.setGravity(Gravity.START); // 좌측 정렬

        }
        return convertView;
    }
}
