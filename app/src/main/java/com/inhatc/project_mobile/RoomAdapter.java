package com.inhatc.project_mobile;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class RoomAdapter extends ArrayAdapter<ChatMessage> {
    private Context mContext;
    private List<ChatMessage> mChatMessages;


    public RoomAdapter(Context context, List<ChatMessage> chatMessages) {
        super(context, 0, chatMessages);
        mContext = context;
        mChatMessages = chatMessages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = getItem(position);
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_room, parent, false);

        TextView titleTextView = convertView.findViewById(R.id.edtRoomTitle);
        TextView timeTextView = convertView.findViewById(R.id.edtRoomTime);
        TextView messageTextView = convertView.findViewById(R.id.edtRoomLastMessage);

        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        simpleDate.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Timestamp timestamp = Timestamp.valueOf(chatMessage.getTimestamp());
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);

        String[] names = chatMessage.getName().split("=");
        String message = (names[0].equals("같음")) ? "나: " : names[1]+": ";

        messageTextView.setText(message+chatMessage.getMessage());
        titleTextView.setText(names[1]+"님과의 채팅방");
        timeTextView.setText(simpleDate.format(cal.getTime()));

        return convertView;
    }
}
