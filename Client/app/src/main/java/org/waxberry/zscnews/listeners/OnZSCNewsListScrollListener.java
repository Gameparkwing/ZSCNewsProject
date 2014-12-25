package org.waxberry.zscnews.listeners;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import org.waxberry.zscnews.events.OnLoadMoreListener;
import org.waxberry.zscnews.controllers.ZSCNewsListController;

/**
 * Created by GJH-08 on 2014/12/15.
 */
public abstract class OnZSCNewsListScrollListener  implements OnScrollListener {

    private ZSCNewsListController zscnewsListController;
    private int lastTotalItemCount;

    private int mLastScrollY;
    private int mPreviousFirstVisibleItem;
    private AbsListView mListView;
    private int mScrollThreshold;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    public OnZSCNewsListScrollListener(ZSCNewsListController zscnewsListController) {
        super();
        this.zscnewsListController = zscnewsListController;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        if (isSameRow(firstVisibleItem)) {
            int newScrollY = getTopItemScrollY();
            boolean isSignificantDelta = Math.abs(mLastScrollY - newScrollY) > mScrollThreshold;
            if (isSignificantDelta) {
                if (mLastScrollY > newScrollY) {
                    onScrollUp();
                } else {
                    onScrollDown();
                }
            }
            mLastScrollY = newScrollY;
        } else {
            if (firstVisibleItem > mPreviousFirstVisibleItem) {
                onScrollUp();
            } else {
                onScrollDown();
            }

            mLastScrollY = getTopItemScrollY();
            mPreviousFirstVisibleItem = firstVisibleItem;
        }

        if (totalItemCount - zscnewsListController.getFooterViewsCount() - zscnewsListController.getHeaderViewsCount() == 0) {
            return;
        }

        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        boolean isLoading = zscnewsListController.isLoading();
        boolean isLastItem = lastVisibleItem == totalItemCount;
        OnLoadMoreListener onLoadMoreListener = zscnewsListController
                .getOnLoadMoreListener();

        //Log.d("onScroll", String.valueOf(isLoading));

        if (!isLoading) {

            // 位置A。

            if (zscnewsListController.isThereMaxItemsCount()
                    && totalItemCount >= zscnewsListController.getMaxItemsCount()) {
                zscnewsListController.setFooterLoadViewVisibility(false);
                return;

            } else if (totalItemCount == 0) {
                zscnewsListController.setFooterLoadViewVisibility(false);
                return;
            }

            // 本来在位置A。
            if (visibleItemCount == totalItemCount) {
                loadMore(onLoadMoreListener, totalItemCount);
                return;
            }

            if (isLastItem) {
                loadMore(onLoadMoreListener, totalItemCount);
            }
        }

        if (zscnewsListController.getOnScrollListener() != null) {
            zscnewsListController.getOnScrollListener().onScroll(view,
                    firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (zscnewsListController.getOnScrollListener() != null) {
            zscnewsListController.getOnScrollListener().onScrollStateChanged(view,
                    scrollState);
        }
    }

    private void loadMore(OnLoadMoreListener onLoadMoreListener,
                          int totalItemCount) {
        /*
        *
        * lastTotalItemCount的存在影响列表刷新。
        * */
        zscnewsListController.setLoading(true);
        zscnewsListController.setFooterLoadViewVisibility(true);
        if (onLoadMoreListener != null)
        {
            onLoadMoreListener.onLoadMore(totalItemCount);
        }
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
    }

    public void setListView(@NonNull AbsListView listView) {
        mListView = listView;
    }

    private boolean isSameRow(int firstVisibleItem) {
        return firstVisibleItem == mPreviousFirstVisibleItem;
    }

    private int getTopItemScrollY() {
        if (mListView == null || mListView.getChildAt(0) == null) return 0;
        View topChild = mListView.getChildAt(0);
        return topChild.getTop();
    }

}
