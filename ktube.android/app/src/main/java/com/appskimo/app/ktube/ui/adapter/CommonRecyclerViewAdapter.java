package com.appskimo.app.ktube.ui.adapter;

import android.os.Handler;
import android.os.Looper;
import androidx.recyclerview.widget.RecyclerView;

import com.appskimo.app.ktube.domain.Loadable;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonRecyclerViewAdapter<M, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected final List<M> items = new ArrayList<>();

    protected Handler handler = new Handler(Looper.getMainLooper());
    protected Loadable pageLoader;

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        handler.post(() -> {
            items.clear();
            notifyDataSetChanged();
        });
    }

    public void add(final List<? extends M> items) {
        handler.post(() -> {
            if (items != null) {
                int position = CommonRecyclerViewAdapter.this.items.size();
                CommonRecyclerViewAdapter.this.items.addAll(items);
                notifyItemRangeInserted(position, items.size());
            }
        });
    }

    public void reset(final List<? extends M> items) {
        handler.post(() -> {
            CommonRecyclerViewAdapter.this.items.clear();
            if (items != null) {
                CommonRecyclerViewAdapter.this.items.addAll(items);
            }
            notifyDataSetChanged();
        });
    }

    public void setPageLoader(Loadable pageLoader) {
        this.pageLoader = pageLoader;
    }

}