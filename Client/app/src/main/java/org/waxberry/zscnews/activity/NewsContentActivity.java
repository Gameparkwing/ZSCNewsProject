package org.waxberry.zscnews.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.waxberry.zscnews.R;
import org.waxberry.zscnews.ZSCNewsApplication;
import org.waxberry.zscnews.data.DataRequester;
import org.waxberry.zscnews.data.FileService;
import org.waxberry.zscnews.data.NewsContentItem;

public class NewsContentActivity extends Activity {

    public LinearLayout layout_title;
    public TextView News_Title;
    public TextView News_SubTitle;
    public WebView News_Text;
    public String News_URL;

    private DataRequester mDataRequester;
    public NewsContentItem mNewsContentItem;
    private static Handler handler = new Handler();

    private Intent intent_data;

    private int[] MyColor = {
            R.color.theme_color_1,
            R.color.theme_color_2,
            R.color.theme_color_3,
            R.color.theme_color_4,
            R.color.theme_color_5,
            R.color.theme_color_6,
            R.color.theme_color_7,
            R.color.theme_color_8,
            R.color.theme_color_9,
            R.color.theme_color_10,
            R.color.theme_color_11,
            R.color.theme_color_12,
            R.color.theme_color_7
    };

    private Drawable oldBackground = null;
    private int currentColor;

    private ZSCNewsApplication mZSCNewsApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mZSCNewsApplication = (ZSCNewsApplication) getApplication();

        if(mZSCNewsApplication.isBrightness)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        intent_data = getIntent();
        int tag = intent_data.getIntExtra(getString(R.string.key_NewsTag), 0);
        currentColor = getResources().getColor(MyColor[tag]);
        changeColor(currentColor);

        News_URL = intent_data.getStringExtra(getString(R.string.key_NewsURL));

        layout_title = (LinearLayout) findViewById(R.id.layout_title);
        News_Title = (TextView) findViewById(R.id.news_title);
        News_SubTitle = (TextView) findViewById(R.id.news_subtitle);
        News_Text = (WebView) findViewById(R.id.news_text);

        layout_title.setBackgroundColor(getResources().getColor(MyColor[tag]));

        mDataRequester = new DataRequester(this, new FileService(getApplicationContext()));
        mNewsContentItem = null;
        setNewsContent();
    }

    public void setNewsContent()
    {
        if(intent_data != null)
        {
            //final int catid = 3;
            //final int contentid = 16810;

            final int catid = intent_data.getIntExtra(getString(R.string.key_NewsCategoryID), -1);
            final int contentid = intent_data.getIntExtra(getString(R.string.key_NewsContentID), -1);
            final boolean isUseCache = mZSCNewsApplication.isUseCache;

            getActionBar().setSubtitle(getResources().getStringArray(R.array.news_category_subtitle)[catid]);

            if(catid == -1 || contentid == -1)
            {
                Toast.makeText(NewsContentActivity.this,
                        R.string.toast_error_get_news_content, Toast.LENGTH_SHORT).show();
                return;
            }

            Thread getNewsContentThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    mNewsContentItem = mDataRequester.GetNewsContent(catid, contentid, isUseCache);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if(mNewsContentItem != null)
                            {
                                News_Title.setText(mNewsContentItem.item_title);
                                News_SubTitle.setText(mNewsContentItem.item_subtitle);
                                WebSettings mWebSettings = News_Text.getSettings();
                                mWebSettings.setDefaultTextEncodingName("UTF-8");
                                mWebSettings.setTextZoom(mZSCNewsApplication.getWebViewZoom());
                                //Log.d("Size", String.valueOf(mZSCNewsApplication.getWebViewZoom()));
                                News_Text.loadDataWithBaseURL(null, mNewsContentItem.item_text, "text/html", "UTF-8", null);
                            }
                            else
                            {
                                Toast.makeText(NewsContentActivity.this,
                                        R.string.toast_error_get_news_content, Toast.LENGTH_SHORT).show();
                                NewsContentActivity.this.finish();
                            }

                        }
                    });
                }
            });

            getNewsContentThread.start();
        }
    }

    public void changeColor(int newColor) {

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);
            Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
            LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });

            if (oldBackground == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ld.setCallback(drawableCallback);
                } else {
                    getActionBar().setBackgroundDrawable(ld);
                }

            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(drawableCallback);
                } else {
                    getActionBar().setBackgroundDrawable(td);
                }

                td.startTransition(200);

            }

            oldBackground = ld;

            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);

        }

        currentColor = newColor;

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            this.finish();
            return true;
        }
        else if(id == R.id.action_open_in_browser)
        {
            if(News_URL != null)
            {
                Uri uri = Uri.parse(getString(R.string.base) + News_URL);
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
