package org.waxberry.zscnews.data;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.waxberry.zscnews.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GJH-08 on 2014/12/18.
 */
public class HotListAdapter extends BaseAdapter {

    private Context mContext;
    public List<HotListItem> mHotListItem;

    public HotListAdapter(Context pContext, List<HotListItem> pList)
    {
        this.mContext = pContext;
        this.mHotListItem = pList;
    }

    @Override
    public int getCount()
    {
        return mHotListItem.size() < 10 ? mHotListItem.size() : 10;
    }

    @Override
    public Object getItem(int Index)
    {
        return mHotListItem.get(Index);
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
        if(mView == null)
        {
            mHolder = new ViewHolder();
            mView= LayoutInflater.from(mContext).inflate(R.layout.hot_list_item, null);
            mHolder.Item_Number = (TextView)mView.findViewById(R.id.number);
            mHolder.Item_Title = (TextView)mView.findViewById(R.id.title);
            // 设置使用ViewHolder。
            mView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) mView.getTag();
        }
        mHolder.Item_Number.setText(mHotListItem.get(Index).item_number);
        mHolder.Item_Title.setText(mHotListItem.get(Index).item_title);

        return mView;
    }

    private static class ViewHolder
    {
        public TextView Item_Number;
        public TextView Item_Title;
    }

}
