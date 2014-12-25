package org.waxberry.zscnews.view;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.waxberry.zscnews.controllers.ZSCNewsListController;
import org.waxberry.zscnews.events.OnLoadMoreListener;
import org.waxberry.zscnews.exceptions.NoEmptyViewException;
import org.waxberry.zscnews.exceptions.NoListViewException;

/**
 * Created by GJH-08 on 2014/12/15.
 */
public class ZSCNewsList {

    private ZSCNewsListController zscnewsListController;

    public ZSCNewsList() {
        super();

        zscnewsListController = new ZSCNewsListController();
    }

    public void register(ListView listView) throws NoListViewException {
        zscnewsListController.register(listView);
    }

    public void register(ListView listView, FloatingActionButton fab) throws NoListViewException {
        zscnewsListController.register(listView, fab);
    }

    public void finishLoading() {
        zscnewsListController.finishLoading();
    }

    public ListView getListView() {
        return zscnewsListController.getListView();
    }

    public void setListView(ListView listView) {
        zscnewsListController.setListView(listView);
    }

    public View getFooterLoadingView() {
        return zscnewsListController.getFooterLoadingView();
    }

    public void setFooterLoadingView(View footerLoadingView) {
        zscnewsListController.setFooterLoadingView(footerLoadingView);
    }

    public void setFooterLoadingView(int loadingViewRes) {
        zscnewsListController.setFooterLoadingView(loadingViewRes);
    }

    public void addDefaultLoadingFooterView() {
        zscnewsListController.addDefaultLoadingFooterView();
    }

    public void setEmptyView(View emptyView) {
        zscnewsListController.setEmptyView(emptyView);
    }

    public View getEmptyView() {
        return zscnewsListController.getEmptyView();
    }

    public int getMaxItemsCount() {
        return zscnewsListController.getMaxItemsCount();
    }

    public void setMaxItemsCount(int maxItemsCount) {
        zscnewsListController.setMaxItemsCount(maxItemsCount);
    }

    public void removeMaxItemsCount() {
        zscnewsListController.removeMaxItemsCount();
    }

    public boolean isLoading() {
        return zscnewsListController.isLoading();
    }

    public void startCentralLoading() throws NoEmptyViewException {
        zscnewsListController.startCentralLoading();
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return zscnewsListController.getOnLoadMoreListener();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        zscnewsListController.setOnLoadMoreListener(onLoadMoreListener);
    }

    public AbsListView.OnScrollListener getOnScrollListener() {
        return zscnewsListController.getOnScrollListener();
    }

    public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        zscnewsListController.setOnScrollListener(onScrollListener);
    }

}
