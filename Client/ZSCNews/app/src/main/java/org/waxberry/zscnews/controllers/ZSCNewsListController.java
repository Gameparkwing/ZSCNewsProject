package org.waxberry.zscnews.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.waxberry.zscnews.R;
import org.waxberry.zscnews.events.OnLoadMoreListener;
import org.waxberry.zscnews.exceptions.NoEmptyViewException;
import org.waxberry.zscnews.exceptions.NoListViewException;
import org.waxberry.zscnews.listeners.OnZSCNewsListScrollListener;
import org.waxberry.zscnews.utils.EmptyViewManager;
import org.waxberry.zscnews.utils.ResInflater;
import org.waxberry.zscnews.view.FloatingActionButton;

/**
 * Created by GJH-08 on 2014/12/15.
 */
public class ZSCNewsListController {

    public static final int DEFAULT_INT = -1;

    private Activity activity;
    private ListView listView;
    private View loadingView;
    private View footerLoadingView;
    private View emptyView;
    private int maxItemsCount = DEFAULT_INT;
    private OnLoadMoreListener onLoadMoreListener;
    private AbsListView.OnScrollListener onScrollListener;

    private boolean isLoading;
    private ViewGroup emptyViewParent;

    public void register(ListView listView) throws NoListViewException {
        this.listView = listView;

        init();
    }

    public void register(ListView listView, FloatingActionButton fab) throws NoListViewException {
        this.listView = listView;

        init(fab);
    }

    private void init() throws NoListViewException {
        if (listView == null) {
            throw new NoListViewException();
        }

        activity = (Activity) listView.getContext();

        OnZSCNewsListScrollListenerImpl onZSCNewsListScrollListenerImpl = new OnZSCNewsListScrollListenerImpl(this);
        listView.setOnScrollListener(onZSCNewsListScrollListenerImpl);
    }

    private void init(FloatingActionButton fab) throws NoListViewException {
        if (listView == null) {
            throw new NoListViewException();
        }

        activity = (Activity) listView.getContext();

        OnZSCNewsListScrollListenerImpl onZSCNewsListScrollListenerImpl = new OnZSCNewsListScrollListenerImpl(this, fab);

        onZSCNewsListScrollListenerImpl.setListView(listView);
        onZSCNewsListScrollListenerImpl.setScrollThreshold(fab.getScrollThreshold());
        listView.setOnScrollListener(onZSCNewsListScrollListenerImpl);
    }

    public void setFooterLoadViewVisibility(boolean visible) {
        if (footerLoadingView != null) {
            footerLoadingView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void finishLoading() {
        setFooterLoadViewVisibility(false);
        setLoading(false);
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public View getLoadingView() {
        return loadingView;
    }

    public void setLoadingView(View loadingView) {
        this.loadingView = loadingView;
    }

    public View getFooterLoadingView() {
        return footerLoadingView;
    }

    public void setFooterLoadingView(View footerLoadingView) {
        this.footerLoadingView = footerLoadingView;

        if (footerLoadingView == null) {
            this.footerLoadingView = ResInflater.inflate(activity,
                    R.layout.loading, null, false);
        }

        listView.addFooterView(this.footerLoadingView);
    }

    public void setFooterLoadingView(int loadingViewRes) {
        footerLoadingView = ResInflater.inflate(activity, loadingViewRes, null,
                false);

        setFooterLoadingView(loadingView);
    }

    public boolean hasFooter() {
        if (listView == null) {
            return false;

        } else {
            return listView.getFooterViewsCount() > 0;
        }
    }

    public int getHeaderViewsCount() {
        if (listView == null) {
            return 0;

        } else {
            return listView.getHeaderViewsCount();
        }
    }

    public int getFooterViewsCount() {
        if (listView == null) {
            return 0;

        } else {
            return listView.getFooterViewsCount();
        }
    }

    public void addDefaultLoadingFooterView() {
        footerLoadingView = ResInflater.inflate(activity, R.layout.loading,
                null, false);

        setFooterLoadingView(loadingView);
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        listView.setEmptyView(this.emptyView);
        this.emptyViewParent = (ViewGroup) emptyView.getParent();
    }

    public View getEmptyView() {
        return this.emptyView;
    }

    public boolean hasEmptyView() {
        return listView.getEmptyView() != null;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;

        EmptyViewManager.switchEmptyContentView(activity, listView,
                this.isLoading, emptyViewParent, emptyView);
    }

    public void startCentralLoading() throws NoEmptyViewException {
        if (this.emptyView == null) {
            throw new NoEmptyViewException();

        } else {
            setLoading(true);
        }
    }

    public int getMaxItemsCount() {
        return maxItemsCount;
    }

    public void setMaxItemsCount(int maxItemsCount) {
        this.maxItemsCount = maxItemsCount;
    }

    public boolean isThereMaxItemsCount() {
        return maxItemsCount > DEFAULT_INT;
    }

    public void removeMaxItemsCount() {
        this.maxItemsCount = DEFAULT_INT;
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return onLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public AbsListView.OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }

    public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public class OnZSCNewsListScrollListenerImpl extends OnZSCNewsListScrollListener
    {

        private FloatingActionButton mFab;

        public OnZSCNewsListScrollListenerImpl(ZSCNewsListController zscNewsListController)
        {
            super(zscNewsListController);
        }

        public OnZSCNewsListScrollListenerImpl(ZSCNewsListController zscNewsListController, FloatingActionButton fab)
        {
            super(zscNewsListController);
            this.mFab = fab;
        }

        @Override
        public void onScrollDown() {
            mFab.show();
        }

        @Override
        public void onScrollUp() {
            mFab.hide();
        }
    }

}
