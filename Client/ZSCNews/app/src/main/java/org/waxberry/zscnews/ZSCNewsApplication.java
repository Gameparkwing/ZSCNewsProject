package org.waxberry.zscnews;

import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baidu.frontia.FrontiaApplication;

import org.waxberry.zscnews.data.SharedPreferencesService;

/**
 * Created by GJH-08 on 2014/10/3.
 */
public class ZSCNewsApplication extends Application {

    private int NewsTextSize;
    public boolean isBrightness;
    public boolean isMiniList;
    private int ListTransforms;
    public boolean isMessagePush;
    public boolean isUseCache;
    private int[] webView_zoom;

    @Override
    public void onCreate() {
        super.onCreate();

        this.NewsTextSize = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getResources().getString(R.string.settings_key_news_text_size),
                        getResources().getStringArray(R.array.entryvalues_news_text_size)[1]));

        this.isBrightness = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getResources().getString(R.string.settings_key_brightness), false);

        this.isMiniList = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getResources().getString(R.string.settings_key_mini_list), false);

        this.ListTransforms = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getResources().getString(R.string.settings_key_list_transforms),
                        getResources().getStringArray(R.array.entryvalues_viewpager_transforms)[0]));

        this.isMessagePush = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getResources().getString(R.string.settings_key_message_push), true);

        this.isUseCache = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getResources().getString(R.string.settings_key_use_cache), true);

        this.webView_zoom = getResources().getIntArray(R.array.entryvalues_webView_zoom);

        FrontiaApplication.initFrontiaApplication(this);

    }

    public void setNewsTextSize(int pSize)
    {
        this.NewsTextSize = pSize;
    }

    public int getNewsTextSize()
    {
        return this.NewsTextSize;
    }

    public void setListTransforms(int pTransform)
    {
        this.ListTransforms = pTransform;
    }

    public int getListTransforms()
    {
        return this.ListTransforms;
    }

    public int getWebViewZoom()
    {
        return webView_zoom[NewsTextSize - 1];
    }

}
