package com.appskimo.app.ktube.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.YoutubeVideo;
import com.appskimo.app.ktube.ui.view.VideoItemView;
import com.appskimo.app.ktube.ui.view.VideoItemView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.res.IntegerRes;

import java.util.List;

import lombok.Setter;

@EBean
public class VideoRecyclerViewAdapter extends CommonRecyclerViewAdapter<YoutubeVideo, VideoRecyclerViewAdapter.ViewHolder> {
    @IntegerRes(R.integer.page_check_count) int pageCheckCount;
    @Setter private Constants.VideoDomain videoDomain;

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (pageLoader != null) {
            int itemCount = this.getItemCount();
            if(itemCount > pageCheckCount && position == itemCount - pageCheckCount) {
                pageLoader.load();
            }
        }

        viewHolder.videoItemView.setData(items.get(position), videoDomain);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(VideoItemView_.build(viewGroup.getContext()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        VideoItemView videoItemView;
        public ViewHolder(View itemView) {
            super(itemView);
            videoItemView = (VideoItemView) itemView;
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.videoItemView.releaseImageView();
        super.onViewRecycled(holder);
    }

    public List<YoutubeVideo> getItems() {
        return items;
    }

    public int getItemPosition(YoutubeVideo video) {
        int indexOf = items.indexOf(video);
        return indexOf < 0  ? 0 : indexOf;
    }
}