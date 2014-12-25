package org.waxberry.zscnews.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.waxberry.zscnews.R;

/**
 * Created by GJH-08 on 2014/12/16.
 */
public class ZSCNewsListHeader extends FrameLayout {

    private TextView textView;

    public ZSCNewsListHeader(Context context) {
        super(context);
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.zscnews_list_header, this);
        textView = (TextView) findViewById(R.id.text);
    }

    public ZSCNewsListHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.zscnews_list_header, this);
        textView = (TextView) findViewById(R.id.text);
    }

    public void setTextViewVisibility(int vis)
    {
        textView.setVisibility(vis);
    }

    public void setText(String text)
    {
        this.textView.setText(text);
    }

}
