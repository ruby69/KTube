package com.appskimo.app.ktube.ui.frags;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.R;
import com.appskimo.app.ktube.domain.Loadable;
import com.appskimo.app.ktube.domain.More;
import com.appskimo.app.ktube.event.OnFavoriteVideo;
import com.appskimo.app.ktube.event.OnListedVideo;
import com.appskimo.app.ktube.event.OnSelectVideo;
import com.appskimo.app.ktube.service.KtubeService;
import com.appskimo.app.ktube.service.MiscService;
import com.appskimo.app.ktube.support.EventBusObserver;
import com.appskimo.app.ktube.ui.activity.PlayerMyActivity_;
import com.appskimo.app.ktube.ui.adapter.VideoRecyclerViewAdapter;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EFragment(R.layout.fragment_my_video)
public class MyVideoFragment extends Fragment implements Loadable {
    @ViewById(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    @ViewById(R.id.actionMenu) View actionMenu;
    @ViewById(R.id.actions) View actions;

    @Bean KtubeService ktubeService;
    @Bean MiscService miscService;
    @Bean VideoRecyclerViewAdapter recyclerViewAdapter;

    @FragmentArg("videoDomain") Constants.VideoDomain videoDomain;

    private More currentMore;
    private boolean openMenu = false;

    private SwipeRefreshLayout.OnRefreshListener refreshListener;

    @AfterInject
    void afterInject() {
        recyclerViewAdapter.setPageLoader(this);
        recyclerViewAdapter.setVideoDomain(videoDomain);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(new EventBusObserver.AtStartStop(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshListener.onRefresh();
    }

    @AfterViews
    void afterViews() {
        recyclerView.setAdapter(recyclerViewAdapter);

        refreshListener = () -> {
            if (refreshLayout != null) {
                refreshLayout.setRefreshing(true);
            }

            if (ktubeService != null) {
                ktubeService.retrieveMyVideo(videoDomain, new More(), new On<More>().addSuccessListener(more -> {
                    currentMore = more;
                    if (recyclerViewAdapter != null) {
                        recyclerViewAdapter.reset(more.getContent());
                    }
                    showHideActions(more.getContent().isEmpty());
                    refresh(more);
                }));
            }
        };

        refreshLayout.setOnRefreshListener(refreshListener);
        refreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    @Subscribe
    public void onEvent(OnSelectVideo event) {
        if (videoDomain == event.getVideoDomain()) {
            miscService.showAdDialog(getActivity(), event.getVideo().getTitle(), R.string.label_continue, (dialog, i) -> PlayerMyActivity_.intent(getContext()).video(event.getVideo()).videoDomain(videoDomain.clone(Constants.ViewStyle.MEDIUM)).start());
        }
    }

    @Click(R.id.actionMenu)
    void onClickActionMenu() {
        if (openMenu) {
            YoYo.with(Techniques.FadeOutUp).duration(100).playOn(actions);
            openMenu = false;
        } else {
            YoYo.with(Techniques.FadeInDown).interpolate(new OvershootInterpolator()).duration(300).playOn(actions);
            openMenu = true;
        }
    }

    @Click(R.id.deleteAll)
    void onClickDeleteAll() {
        ktubeService.deleteMyVideo(videoDomain, new On<Void>().addSuccessListener(aVoid -> getActivity().runOnUiThread(() -> {
            if (videoDomain.isFavorite()) {
                EventBus.getDefault().post(new OnFavoriteVideo());
            } else if (videoDomain.isPlaylist()) {
                EventBus.getDefault().post(new OnListedVideo());
            }

            if (refreshListener != null) {
                refreshListener.onRefresh();
            }
        })));
    }

    @Click(R.id.refresh)
    void onClickRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    @UiThread
    void showHideActions(boolean hide) {
        if (actionMenu != null) {
            actionMenu.setVisibility(hide ? View.GONE : View.VISIBLE);
            actions.setVisibility(hide ? View.GONE : View.VISIBLE);

            openMenu = false;
            YoYo.with(Techniques.FadeOut).duration(0).playOn(actions);
        }
    }

    @UiThread
    void refresh(More more) {
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }

        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void load() {
        if (ktubeService != null && currentMore != null && currentMore.isHasMore()) {
            ktubeService.retrieveMyVideo(videoDomain, currentMore, new On<More>().addSuccessListener(more -> {
                currentMore = more;
                if (recyclerViewAdapter != null) {
                    recyclerViewAdapter.add(more.getContent());
                }
            }));
        }
    }

    @Override
    public void onDestroyView() {
        currentMore = null;

        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.clear();
            recyclerViewAdapter.setPageLoader(null);
            recyclerViewAdapter = null;
        }

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        super.onDestroyView();
    }
}
