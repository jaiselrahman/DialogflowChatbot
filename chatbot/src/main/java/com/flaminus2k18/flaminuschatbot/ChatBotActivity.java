package com.flaminus2k18.flaminuschatbot;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;

public class ChatBotActivity extends AppCompatActivity {
    private static final String TAG = ChatBotActivity.class.getSimpleName();
    private Vector<Message> chatMessages = new Vector<>();
    private ChatsAdapter chatsAdapter;
    private RecyclerView chatList;
    private TextView messageText;
    private Message currentMessage;
    private AIRequest aiRequest;
    private AIDataService aiDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final AIConfiguration config = new AIConfiguration(getString(R.string.access_key),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
        aiRequest = new AIRequest();
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
                    fab.setVisibility(View.GONE);
                } else if (fab.getVisibility() != View.VISIBLE) {
                    fab.setVisibility(View.VISIBLE);
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
                aiRequest.setQuery(message.getText());
                currentMessage = message;
                new RequestTask(ChatBotActivity.this).execute(aiRequest);
                chatsAdapter.notifyDataSetChanged();
                messageText.setText("");
                chatList.smoothScrollToPosition(chatsAdapter.getItemCount());
            }
        });
    }

    void sendWelcomeEvent() {
        AIRequest aiRequest = new AIRequest();
        aiRequest.setEvent(new AIEvent("CUSTOM_WELCOME"));
        new RequestTask(this).execute(aiRequest);
    }

    static class RequestTask extends AsyncTask<AIRequest, Void, AIResponse> {

        private Gson gson = new Gson();
        private WeakReference<ChatBotActivity> activity;
        private AIDataService aiDataService;

        RequestTask(ChatBotActivity activity) {
            this.activity = new WeakReference<>(activity);
            this.aiDataService = activity.aiDataService;
        }

        @Override
        protected AIResponse doInBackground(AIRequest... requests) {
            final AIRequest request = requests[0];
            try {
                return aiDataService.request(request);
            } catch (AIServiceException e) {
                Log.i(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(AIResponse aiResponse) {
            if (aiResponse != null) {
                if (activity.get().currentMessage != null) {
                    activity.get().currentMessage.setStatus(com.flaminus2k18.flaminuschatbot.model.Status.SENT);
                }

                Vector<Cards> cards = null;
                Message cardMessage = null;

                List<ResponseMessage> messages = aiResponse.getResult().getFulfillment().getMessages();
                if (messages != null) {
                    try {
                        JSONArray jsonMessages = new JSONArray(gson.toJson(messages));
                        for (int i = 0; i < jsonMessages.length(); i++) {
                            String type = JsonUtils.getString(jsonMessages.getJSONObject(i), "type", "none");
                            switch (type) {
                                case "SPEECH":
                                    String text = jsonMessages.getJSONObject(i).getJSONArray("speech").get(0).toString();
                                    if (!TextUtils.isEmpty(text)) {
                                        Message msg = new Message();
                                        msg.setTimeStamp(new Date().getTime());
                                        msg.setMessageType(MessageType.OTHER);
                                        msg.setText(text);
                                        addMessage(msg);
                                    }
                                    break;
                                case "PAYLOAD":
                                    try {
                                        Boolean isEventsLists = jsonMessages.getJSONObject(i).getJSONObject("payload").getBoolean("EVENT_LISTS");
                                        if (isEventsLists) {
                                            AIRequest technicalEvent = new AIRequest();
                                            technicalEvent.setQuery("technical events");
                                            new RequestTask(activity.get()).execute(technicalEvent);

                                            AIRequest nonTechnicalEvents = new AIRequest();
                                            nonTechnicalEvents.setQuery("non technical events");
                                            new RequestTask(activity.get()).execute(nonTechnicalEvents);

                                            AIRequest onlineEvents = new AIRequest();
                                            onlineEvents.setQuery("online events");
                                            new RequestTask(activity.get()).execute(onlineEvents);
                                            return;
                                        } else {
                                            text = JsonUtils.getString(jsonMessages.getJSONObject(i).getJSONObject("payload"), "text", null);
                                            Message message = new Message();
                                            message.setTimeStamp(new Date().getTime());
                                            message.setMessageType(MessageType.OTHER);
                                            message.setText(text.replaceAll("\\\\n", "\\\n"));
                                            addMessage(message);
                                        }
                                    } catch (JSONException ex) {
                                        Log.d(TAG, ex.getMessage());
                                    }
                                    break;
                                case "CARD":
                                    if (cards == null) {
                                        cards = new Vector<>();
                                        cardMessage = new Message();
                                        cardMessage.setTimeStamp(aiResponse.getTimestamp().getTime());
                                        cardMessage.setMessageType(MessageType.OTHER_CARDS);
                                    }
                                    Cards card = new Cards();
                                    card.setTitle(JsonUtils.getString(jsonMessages.getJSONObject(i), "title", null));
                                    card.setSubtitle(JsonUtils.getString(jsonMessages.getJSONObject(i), "subtitle", null));
                                    card.setImgUrl(JsonUtils.getString(jsonMessages.getJSONObject(i), "imageUrl", null));
                                    cards.add(card);
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, e.toString());
                    }
                    if (cardMessage != null) {
                        cardMessage.setCards(cards);
                        addMessage(cardMessage);
                    }
                }
            } else {
                Toast.makeText(activity.get(), "Oops! Something went wrong.\nPlease Check your Network.", Toast.LENGTH_SHORT).show();
            }
        }

        void addMessage(Message message) {
            activity.get().chatMessages.add(message);
            activity.get().chatsAdapter.notifyDataSetChanged();
            activity.get().chatList.smoothScrollToPosition(activity.get().chatsAdapter.getItemCount());
        }
    }
}
