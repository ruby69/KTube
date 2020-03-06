package com.appskimo.app.ktube.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.domain.Artist;
import com.appskimo.app.ktube.ui.view.ArtistItemView;
import com.appskimo.app.ktube.ui.view.ArtistItemView_;

import org.androidannotations.annotations.EBean;

@EBean
public class ArtistRecyclerViewAdapter extends CommonRecyclerViewAdapter<Artist, ArtistRecyclerViewAdapter.ViewHolder> {
    private Constants.ViewStyle type;

    public void setType(Constants.ViewStyle type) {
        this.type = type;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.artistItemView.setData(items.get(position), type);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(ArtistItemView_.build(viewGroup.getContext()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ArtistItemView artistItemView;
        public ViewHolder(View itemView) {
            super(itemView);
            artistItemView = (ArtistItemView) itemView;
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.artistItemView.releaseImageView();
        super.onViewRecycled(holder);
    }
}