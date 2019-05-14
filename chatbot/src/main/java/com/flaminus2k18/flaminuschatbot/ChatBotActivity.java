package com.flaminus2k18.flaminuschatbot;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flaminus2k18.flaminuschatbot.adapter.ChatsAdapter;
import com.flaminus2k18.flaminuschatbot.model.Cards;
import com.flaminus2k18.flaminuschatbot.model.Message;
import com.flaminus2k18.flaminuschatbot.model.MessageType;
import com.flaminus2k18.flaminuschatbot.model.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.Intent;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;


public class ChatBotActivity extends AppCompatActivity {
    private static final String TAG = ChatBotActivity.class.getSimpleName();
    private Vector<Message> chatMessages = new Vector<>();
    private ChatsAdapter chatsAdapter;
    private RecyclerView chatList;
    private TextView messageText;
    private Message currentMessage;
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private UUID uuid = UUID.randomUUID();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(getResources().openRawResource(R.raw.dialogflow_credential));
            String projectID = ((ServiceAccountCredentials) credentials).getProjectId();
            sessionsClient = SessionsClient.create(SessionsSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build());
            sessionName = SessionName.of(projectID, uuid.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendWelcomeEvent();

        final FloatingActionButton fab = findViewById(R.id.move_to_down);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatList.scrollToPosition(chatsAdapter.getItemCount() - 1);
            }
        });

        chatsAdapter = new ChatsAdapter(this, chatMessages);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        chatList = findViewById(R.id.chat_list_view);
        chatList.setLayoutManager(linearLayoutManager);
        chatList.setAdapter(chatsAdapter);
        chatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (position != RecyclerView.NO_POSITION && position >= chatsAdapter.getItemCount() - 4) {
                    fab.hide();
                } else if (fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        messageText = findViewById(R.id.chat_edit_text1);
        ImageView send = findViewById(R.id.enter_chat1);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(messageText.getText().toString().trim())) {
                    return;
                }
                final Message message = new Message();
                message.setText(messageText.getText().toString());
                message.setStatus(Status.WAIT);
                message.setTimeStamp(new Date().getTime());
                message.setMessageType(MessageType.MINE);
                chatMessages.add(message);
                currentMessage = message;
                sendMessage(message.getText());
                chatsAdapter.notifyDataSetChanged();
                messageText.setText("");
                chatList.smoothScrollToPosition(chatsAdapter.getItemCount());
            }
        });
    }

    void sendWelcomeEvent() {
        new RequestTask(this).execute();
    }

    private void sendMessage(String message) {
        new RequestTask(this).execute(message);
    }

    static class RequestTask extends AsyncTask<String, Void, DetectIntentResponse> {

        private WeakReference<ChatBotActivity> activity;
        private SessionsClient sessionsClient;

        RequestTask(ChatBotActivity activity) {
            this.activity = new WeakReference<>(activity);
            this.sessionsClient = activity.sessionsClient;
        }

        @Override
        protected DetectIntentResponse doInBackground(String... requests) {
            try {
                return sessionsClient.detectIntent(activity.get().sessionName,
                        QueryInput.newBuilder()
                                .setText(TextInput.newBuilder()
                                        .setText(requests[0])
                                        .setLanguageCode("en-US")
                                        .build())
                                .build());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DetectIntentResponse response) {
            if (response != null) {
                if (activity.get().currentMessage != null) {
                    activity.get().currentMessage.setStatus(com.flaminus2k18.flaminuschatbot.model.Status.SENT);
                }

                ArrayList<Cards> cards = null;
                Message cardMessage = null;

                List<Intent.Message> messages = response.getQueryResult().getFulfillmentMessagesList();
                for (Intent.Message m : messages) {
                    if (m.hasPayload()) {
                        boolean isEventsLists = m.getPayload().getFieldsMap().containsKey("EVENT_LISTS");
                        if (isEventsLists)
                            isEventsLists = m.getPayload().getFieldsMap().get("EVENT_LISTS").getBoolValue();
                        if (isEventsLists) {
                            new RequestTask(activity.get()).execute("technical events");
                            new RequestTask(activity.get()).execute("non technical events");
                            new RequestTask(activity.get()).execute("online events");
                            return;
                        }
                    } else if (m.hasCard()) {
                        if (cards == null) {
                            cards = new ArrayList<>();
                            cardMessage = new Message();
                            cardMessage.setTimeStamp(new Date().getTime());
                            cardMessage.setMessageType(MessageType.OTHER_CARDS);
                        }
                        Cards card = new Cards();
                        card.setTitle(m.getCard().getTitle());
                        card.setSubtitle(m.getCard().getSubtitle());
                        card.setImgUrl(m.getCard().getImageUri());
                        cards.add(card);
                    } else if (m.hasText()) {
                        Message msg = new Message();
                        msg.setTimeStamp(new Date().getTime());
                        msg.setMessageType(MessageType.OTHER);
                        msg.setText(m.getText().getText(0));
                        addMessage(msg);
                    }
                }
                if (cardMessage != null) {
                    cardMessage.setCards(cards);
                    addMessage(cardMessage);
                }
            } else {
                Toast.makeText(activity.get(), "Oops! Something went wrong.\nPlease Check your Network.", Toast.LENGTH_SHORT).show();
            }
        }

        void addMessage(Message message) {
            activity.get().currentMessage.setStatus(com.flaminus2k18.flaminuschatbot.model.Status.SENT);
            activity.get().chatMessages.add(message);
            activity.get().chatsAdapter.notifyDataSetChanged();
            activity.get().chatList.smoothScrollToPosition(activity.get().chatsAdapter.getItemCount());
        }
    }
}
