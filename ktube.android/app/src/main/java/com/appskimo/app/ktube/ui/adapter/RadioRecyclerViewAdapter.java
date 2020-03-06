package com.appskimo.app.ktube.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.ktube.domain.RadioStation;
import com.appskimo.app.ktube.ui.view.RadioItemView;
import com.appskimo.app.ktube.ui.view.RadioItemView_;

import org.androidannotations.annotations.EBean;

@EBean
public class RadioRecyclerViewAdapter extends CommonRecyclerViewAdapter<RadioStation, RadioRecyclerViewAdapter.ViewHolder> {
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.radioItemView.setData(items.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(RadioItemView_.build(viewGroup.getContext()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RadioItemView radioItemView;
        public ViewHolder(View itemView) {
            super(itemView);
            radioItemView = (RadioItemView) itemView;
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.radioItemView.releaseImageView();
        super.onViewRecycled(holder);
    }
}