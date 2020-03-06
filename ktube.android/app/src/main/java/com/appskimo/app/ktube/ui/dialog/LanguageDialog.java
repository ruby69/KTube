package com.appskimo.app.ktube.ui.dialog;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.SupportLanguage;
import com.appskimo.app.ktube.ui.adapter.CommonRecyclerViewAdapter;
import com.appskimo.app.ktube.ui.view.LanguageItemView;
import com.appskimo.app.ktube.ui.view.LanguageItemView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

@EFragment(R.layout.dialog_simple_selector)
public class LanguageDialog extends CommonDialog {
    public static final String TAG = "LanguageDialog";

    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    private LanguageRecyclerViewAdapter recyclerViewAdapter;

    @AfterInject
    void afterInject() {
        recyclerViewAdapter = new LanguageRecyclerViewAdapter();
        recyclerViewAdapter.reset(Arrays.asList(SupportLanguage.values()));
    }

    @AfterViews
    void afterViews() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private static class LanguageRecyclerViewAdapter extends CommonRecyclerViewAdapter<SupportLanguage, LanguageRecyclerViewAdapter.ViewHolder> {
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.languageItemView.setLanguage(items.get(position));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            return new ViewHolder(LanguageItemView_.build(viewGroup.getContext()));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            LanguageItemView languageItemView;
            public ViewHolder(View itemView) {
                super(itemView);
                languageItemView = (LanguageItemView) itemView;
            }
        }
    }
}