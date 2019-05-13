package com.flaminus2k18.flaminuschatbot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;
import com.flaminus2k18.flaminuschatbot.R;
import com.flaminus2k18.flaminuschatbot.app.AppController;
import com.flaminus2k18.flaminuschatbot.model.Cards;

import java.util.Vector;

/**
 * Created by jaisel on 12/1/18.
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {
    private Vector<Cards> cards;
    private Context context;

    public CardsAdapter(Context context, Vector<Cards> cards) {
        this.context = context;
        this.cards = cards;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cards card = cards.elementAt(position);
        holder.title.setText(card.getTitle());
        if (card.getSubtitle() != null) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(card.getSubtitle());
        }
        holder.image.setImageUrl(card.getImgUrl(), AppController.getInstance().getImageLoader());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, subtitle;
        private NetworkImageView image;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            image = v.findViewById(R.id.image);
        }
    }
}
