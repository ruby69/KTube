package com.appskimo.app.ktube.ui.view;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.SupportLanguage;
import com.appskimo.app.ktube.event.OnSelectLanguage;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

@EViewGroup(R.layout.view_language_item)
public class LanguageItemView extends RelativeLayout {
    @ViewById(R.id.displayNameView) TextView displayNameView;
    private SupportLanguage supportLanguage;

    public LanguageItemView(Context context) {
        super(context);
    }

    public void setLanguage(SupportLanguage supportLanguage) {
        this.supportLanguage = supportLanguage;
        displayNameView.setText(supportLanguage.getDisplayName());
    }

    @Click(R.id.displayNameLayer)
    void clickDisplayNameLayer() {
        EventBus.getDefault().post(new OnSelectLanguage(supportLanguage));
    }
}