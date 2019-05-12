package com.flaminus2k18.flaminuschatbot.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaminus2k18.flaminuschatbot.R;
import com.flaminus2k18.flaminuschatbot.model.Message;
import com.flaminus2k18.flaminuschatbot.model.MessageType;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by jaisel on 25/11/17.
 */

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("h:mm a", Locale.US);
    private Vector<Message> chatMessages;
    private Context context;

    public ChatsAdapter(Context context, Vector<Message> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = chatMessages.elementAt(position);

        if(message.getMessageType() == MessageType.OTHER_CARDS) {
            LinearLayoutManager lm = new LinearLayoutManager(context);
            lm.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.recyclerView.setLayoutManager(lm);
            holder.recyclerView.setAdapter(new CardsAdapter(context, message.getCards()));
            return;
        }

        holder.message.setText(message.getText());
        holder.time.setText(SIMPLE_DATE_FORMAT.format(message.getTimeStamp()));

        if (message.getMessageType() == MessageType.MINE) {
            switch (message.getStatus()) {
                case SENT:
                    holder.status.setImageDrawable(context.getResources().
                            getDrawable(R.drawable.message_sent));
                    break;
                case WAIT:
                    holder.status.setImageDrawable(context.getResources().
                            getDrawable(R.drawable.message_wait));
                    break;
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (viewType == MessageType.MINE.ordinal()) {
            v = LayoutInflater.from(context).inflate(R.layout.chat_user2_item, parent, false);
            return new ViewHolder(v, MessageType.MINE);
        } else if (viewType == MessageType.OTHER.ordinal()) {
            v = LayoutInflater.from(context).inflate(R.layout.chat_user1_item, parent, false);
            return new ViewHolder(v, MessageType.OTHER);
        } else if(viewType == MessageType.OTHER_CARDS.ordinal()) {
            v = LayoutInflater.from(context).inflate(R.layout.chat_user1_card_item, parent, false);
            return new ViewHolder(v, MessageType.OTHER_CARDS);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getMessageType().ordinal();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView message, time;
        private ImageView status;
        private RecyclerView recyclerView;

        ViewHolder(View v, MessageType messageType) {
            super(v);
            if(messageType == MessageType.OTHER_CARDS) {
                recyclerView = v.findViewById(R.id.cards);
            }
            message = v.findViewById(R.id.textview_message);
            time = v.findViewById(R.id.textview_time);
            if (messageType == MessageType.MINE) {
                status = v.findViewById(R.id.user_reply_status);
            }
        }
    }
}