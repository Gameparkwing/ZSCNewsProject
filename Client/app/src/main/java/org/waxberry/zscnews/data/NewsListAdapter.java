package org.waxberry.zscnews.data;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.waxberry.zscnews.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Waxberry on 2014/12/11.
 */
public class NewsListAdapter extends BaseAdapter {

    private Context mContext;
    private int PagesCount;
    public List<NewsListItem> mNewsListItem;

    public NewsListAdapter(Context pContext)
    {
        this.mContext = pContext;
        this.mNewsListItem = new ArrayList<NewsListItem>();
        this.PagesCount = 1;
    }

    public void addItems(List<NewsListItem> pNewsListItem)
    {
        if(!pNewsListItem.isEmpty())
        {
            mNewsListItem.addAll(pNewsListItem);
            PagesCount++;
        }
    }

    public void setPagesCount(int pPagesCount)
    {
        this.PagesCount = pPagesCount;
    }

    public int getPagesCount()
    {
        return this.PagesCount;
    }

    @Override
    public int getCount()
    {
        return mNewsListItem.size();
    }

    @Override
    public Object getItem(int Index)
    {
        return mNewsListItem.get(Index);
    }

    @Override
    public long getItemId(int Index)
    {
        return Index;
    }

    @Override
    public View getView(int Index, View mView, ViewGroup mParent)
    {
        ViewHolder mHolder = null;
        NewsListItem pNewsListItem = mNewsListItem.get(Index);
        if(mView == null)
        {
            mHolder = new ViewHolder();
            mView= LayoutInflater.from(mContext).inflate(R.layout.news_list_item, null);
            mHolder.Item_Title = (TextView)mView.findViewById(R.id.title);
            mHolder.Item_Summary = (TextView)mView.findViewById(R.id.summary);
            if(pNewsListItem.item_summary == null)
            {
                mHolder.Item_Summary.setVisibility(View.GONE);
            }
            else
            {
                mHolder.Item_Summary.setVisibility(View.VISIBLE);
            }
            mHolder.Item_Date = (TextView)mView.findViewById(R.id.date);
            // 设置使用ViewHolder。
            mView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) mView.getTag();
        }
        //mHolder.Item_Icon.setImageResource(mTileInnerListViewItems.get(Index).getCardInnerListTypeI().getIcon());
        mHolder.Item_Title.setText(pNewsListItem.item_title);
        mHolder.Item_Summary.setText(pNewsListItem.item_summary);
        mHolder.Item_Date.setText(pNewsListItem.item_date);

        return mView;
    }

    private static class ViewHolder
    {
        public TextView Item_Title;
        public TextView Item_Summary;
        public TextView Item_Date;
    }

}
