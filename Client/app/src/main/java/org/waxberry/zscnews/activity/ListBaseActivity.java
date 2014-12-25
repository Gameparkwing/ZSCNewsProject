package org.waxberry.zscnews.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import org.waxberry.zscnews.R;
import org.waxberry.zscnews.fragment.HotListFragment;
import org.waxberry.zscnews.fragment.SearchFragment;

public class ListBaseActivity extends Activity
        implements HotListFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener {

    private FragmentManager mFragmentManager;
    private HotListFragment mHotListFragment;
    private SearchFragment mSearchFragment;
    private static Handler handler = new Handler();

    private Intent intent_data;
    private int type;

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

    private final static int HOT_LIST = 1;
    private final static int SEARCH_LIST = 2;

    private final static String TAG_HOT_LIST_FRAGMENT = "hot_list_fragment";
    private final static String TAG_SEARCH_LIST_FRAGMENT = "search_list_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_base);

        if(getActionBar() != null)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        intent_data = getIntent();
        int tag = intent_data.getIntExtra(getString(R.string.key_NewsTag), 0);
        currentColor = getResources().getColor(MyColor[tag]);
        changeColor(currentColor);

        type = intent_data.getIntExtra(getString(R.string.key_Type), HOT_LIST);

        mFragmentManager = getFragmentManager();
        if(type == HOT_LIST)
        {
            mHotListFragment = (HotListFragment)mFragmentManager.findFragmentByTag(TAG_HOT_LIST_FRAGMENT);
            if(mHotListFragment == null)
            {
                mHotListFragment = HotListFragment.newInstance(tag);
                mFragmentManager.beginTransaction()
                        .replace(R.id.container, mHotListFragment, TAG_HOT_LIST_FRAGMENT)
                        .commit();
            }
        }
        else if(type == SEARCH_LIST)
        {
            mSearchFragment = (SearchFragment)mFragmentManager.findFragmentByTag(TAG_SEARCH_LIST_FRAGMENT);
            if(mSearchFragment == null)
            {
                mSearchFragment = SearchFragment.newInstance(tag);
                mFragmentManager.beginTransaction()
                        .replace(R.id.container, mSearchFragment, TAG_SEARCH_LIST_FRAGMENT)
                        .commit();
            }
        }
        else
        {
            mHotListFragment = (HotListFragment)mFragmentManager.findFragmentByTag(TAG_HOT_LIST_FRAGMENT);
            if(mHotListFragment == null)
            {
                mHotListFragment = HotListFragment.newInstance(tag);
                mFragmentManager.beginTransaction()
                        .replace(R.id.container, mHotListFragment, TAG_HOT_LIST_FRAGMENT)
                        .commit();
            }
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
        if(type == SEARCH_LIST)
        {
            outState.putInt("spinnerCurrentItem", getActionBar().getSelectedNavigationIndex());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
        if(type == SEARCH_LIST)
        {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt("spinnerCurrentItem"));
        }
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
        getMenuInflater().inflate(R.menu.list_base, menu);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(String title)
    {
        getActionBar().setTitle(title);
    }
}
