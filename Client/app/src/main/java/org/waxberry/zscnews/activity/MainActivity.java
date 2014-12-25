package org.waxberry.zscnews.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.waxberry.zscnews.R;
import org.waxberry.zscnews.fragment.NewsListFragment;
import org.waxberry.zscnews.utils.PushServiceUtils;
import org.waxberry.zscnews.view.AccordionTransformer;
import org.waxberry.zscnews.view.BackgroundToForegroundTransformer;
import org.waxberry.zscnews.view.CubeInTransformer;
import org.waxberry.zscnews.view.CubeOutTransformer;
import org.waxberry.zscnews.view.DefaultTransformer;
import org.waxberry.zscnews.view.DepthPageTransformer;
import org.waxberry.zscnews.view.FlipHorizontalTransformer;
import org.waxberry.zscnews.view.FlipVerticalTransformer;
import org.waxberry.zscnews.view.ForegroundToBackgroundTransformer;
import org.waxberry.zscnews.view.PagerSlidingTabStrip;
import org.waxberry.zscnews.view.RotateDownTransformer;
import org.waxberry.zscnews.view.RotateUpTransformer;
import org.waxberry.zscnews.view.StackTransformer;
import org.waxberry.zscnews.view.TabletTransformer;
import org.waxberry.zscnews.view.ZoomInTransformer;
import org.waxberry.zscnews.view.ZoomOutSlideTransformer;
import org.waxberry.zscnews.view.ZoomOutTransformer;

public class MainActivity extends Activity
        implements NewsListFragment.OnFragmentInteractionListener,
        PagerSlidingTabStrip.OnInteractionListener {

    private final Handler handler = new Handler();

    private boolean isMessagePush;

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;

    private Drawable oldBackground = null;
    private int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentColor = getResources().getColor(R.color.theme_color_1);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setListener(this);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getFragmentManager());

        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        int mListTransforms = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getResources().getString(R.string.settings_key_list_transforms),
                        getResources().getStringArray(R.array.entryvalues_viewpager_transforms)[0]));
        pager.setPageTransformer(true, SelectTransformer(mListTransforms));

        tabs.setViewPager(pager);

        changeColor(currentColor);

        Thread PushServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {

                isMessagePush = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getBoolean(getResources().getString(R.string.settings_key_message_push), true);

                //初始化推送服务。
                initPushService();

                //Log.d("Push Boolean", String.valueOf(isMessagePush));

                if(isMessagePush)
                {
                    resumePushService();
                }
                else
                {
                    stopPushService();
                }

                //Log.d("Push Enable", String.valueOf(PushManager.isPushEnabled(getApplicationContext())));
                //Log.d("Push Connect", String.valueOf(PushManager.isConnected(getApplicationContext())));

            }
        });

        PushServiceThread.start();

    }

    protected PageTransformer SelectTransformer(int id)
    {
        PageTransformer transformer;
        switch (id)
        {
            case 1:
                transformer = new DefaultTransformer();
                break;
            case 2:
                transformer = new AccordionTransformer();
                break;
            case 3:
                transformer = new BackgroundToForegroundTransformer();
                break;
            case 4:
                transformer = new ForegroundToBackgroundTransformer();
                break;
            case 5:
                transformer = new CubeInTransformer();
                break;
            case 6:
                transformer = new CubeOutTransformer();
                break;
            case 7:
                transformer = new DepthPageTransformer();
                break;
            case 8:
                transformer = new FlipHorizontalTransformer();
                break;
            case 9:
                transformer = new FlipVerticalTransformer();
                break;
            case 10:
                transformer = new RotateDownTransformer();
                break;
            case 11:
                transformer = new RotateUpTransformer();
                break;
            case 12:
                transformer = new StackTransformer();
                break;
            case 13:
                transformer = new TabletTransformer();
                break;
            case 14:
                transformer = new ZoomOutSlideTransformer();
                break;
            case 15:
                transformer = new ZoomInTransformer();
                break;
            case 16:
                transformer = new ZoomOutTransformer();
                break;
            default:
                transformer = new DefaultTransformer();
        }

        return transformer;
    }

    /**
     * 初始化推送服务。
     */
    protected void initPushService()
    {
        // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        // 通过share preference实现的绑定标志开关，如果已经成功绑定，就取消这次绑定
        //将AndroidManifest.xml的api_key字段值修改为自己的 api_key。
        if (!PushServiceUtils.hasBind(getApplicationContext()))
        {
            PushManager.startWork(getApplicationContext(),
                    PushConstants.LOGIN_TYPE_API_KEY,
                    PushServiceUtils.getMetaValue(MainActivity.this, "api_key"));
            // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
            // PushManager.enableLbs(getApplicationContext());
        }
    }

    /**
     * 恢复推送服务。
     */
    protected void resumePushService()
    {
        if (!PushServiceUtils.hasBind(getApplicationContext()))
        {
            PushManager.resumeWork(getApplicationContext());
            // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
            // PushManager.enableLbs(getApplicationContext());
        }
    }

    /**
     * 关闭推送服务。
     */
    protected void stopPushService()
    {
        if (PushServiceUtils.hasBind(getApplicationContext()))
        {
            PushManager.stopWork(getApplicationContext());
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void changeColor(int newColor) {

        tabs.setIndicatorColor(newColor);

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
        public void invalidateDrawable(Drawable who)
        {
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

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = getResources().getStringArray(R.array.news_category);

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            return NewsListFragment.newInstance(position);
        }

    }

}
