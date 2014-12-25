package org.waxberry.zscnews.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.android.pushservice.PushManager;

import org.waxberry.zscnews.R;
import org.waxberry.zscnews.ZSCNewsApplication;
import org.waxberry.zscnews.data.FileService;
import org.waxberry.zscnews.utils.PushServiceUtils;

import java.math.BigDecimal;

public class SettingsActivity extends Activity {

    private static Handler handler = new Handler();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(getActionBar() != null)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent_data = getIntent();
        int tag = intent_data.getIntExtra(getString(R.string.key_NewsTag), 0);
        currentColor = getResources().getColor(MyColor[tag]);
        changeColor(currentColor);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
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

    public static class SettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceClickListener,
            Preference.OnPreferenceChangeListener {

        public String mKey_news_text_size;
        public String mKey_brightness;
        public String mKey_mini_list;
        public String mKey_list_transforms;
        public String mKey_message_push;
        public String mKey_use_cache;
        public String mKey_clear_cache;

        public ListPreference mListView_news_text_size;
        public CheckBoxPreference mCheckBox_brightness;
        public CheckBoxPreference mCheckBox_mini_list;
        public ListPreference mListView_list_transforms;
        public SwitchPreference mSwitch_message_push;
        public CheckBoxPreference mCheckBox_use_cache;
        public PreferenceScreen mPreferenceScreen_clear_cache;

        private ZSCNewsApplication mZSCNewsApplication;
        private FileService mFileService;

        private String[] summary_news_text_size;
        private String[] summary_viewpager_transforms;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            mZSCNewsApplication = (ZSCNewsApplication)getActivity().getApplication();
            mFileService = new FileService(getActivity().getApplicationContext());

            mKey_news_text_size = getResources().getString(R.string.settings_key_news_text_size);
            mKey_brightness = getResources().getString(R.string.settings_key_brightness);
            mKey_mini_list = getResources().getString(R.string.settings_key_mini_list);
            mKey_list_transforms = getResources().getString(R.string.settings_key_list_transforms);
            mKey_message_push = getResources().getString(R.string.settings_key_message_push);
            mKey_use_cache = getResources().getString(R.string.settings_key_use_cache);
            mKey_clear_cache = getResources().getString(R.string.settings_key_clear_cache);

            mListView_news_text_size = (ListPreference) findPreference(mKey_news_text_size);
            mCheckBox_brightness = (CheckBoxPreference) findPreference(mKey_brightness);
            mCheckBox_mini_list = (CheckBoxPreference) findPreference(mKey_mini_list);
            mListView_list_transforms = (ListPreference) findPreference(mKey_list_transforms);
            mSwitch_message_push = (SwitchPreference) findPreference(mKey_message_push);
            mCheckBox_use_cache = (CheckBoxPreference) findPreference(mKey_use_cache);
            mPreferenceScreen_clear_cache = (PreferenceScreen) findPreference(mKey_clear_cache);

            mListView_news_text_size.setOnPreferenceClickListener(this);
            mListView_news_text_size.setOnPreferenceChangeListener(this);
            mCheckBox_brightness.setOnPreferenceClickListener(this);
            mCheckBox_brightness.setOnPreferenceChangeListener(this);
            mCheckBox_mini_list.setOnPreferenceClickListener(this);
            mCheckBox_mini_list.setOnPreferenceChangeListener(this);
            mListView_list_transforms.setOnPreferenceClickListener(this);
            mListView_list_transforms.setOnPreferenceChangeListener(this);
            mSwitch_message_push.setOnPreferenceClickListener(this);
            mSwitch_message_push.setOnPreferenceChangeListener(this);
            mCheckBox_use_cache.setOnPreferenceClickListener(this);
            mCheckBox_use_cache.setOnPreferenceChangeListener(this);
            mPreferenceScreen_clear_cache.setOnPreferenceClickListener(this);
            mPreferenceScreen_clear_cache.setOnPreferenceChangeListener(this);

            setSummaryOfmPreferenceScreen_clear_cache();

            summary_news_text_size = getResources().getStringArray(R.array.entries_news_text_size);
            summary_viewpager_transforms = getResources().getStringArray(R.array.entries_viewpager_transforms);

            mListView_news_text_size.setSummary(summary_news_text_size[mZSCNewsApplication.getNewsTextSize() - 1]);
            mListView_list_transforms.setSummary(summary_viewpager_transforms[mZSCNewsApplication.getListTransforms() - 1]);
        }

        // 点击事件触发。
        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            if(preference == mPreferenceScreen_clear_cache)
            {
                //Log.d("Click", "清理缓存");
                Thread DeleteCacheThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mFileService.deleteCache();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setSummaryOfmPreferenceScreen_clear_cache();
                            }
                        });
                    }
                });

                DeleteCacheThread.start();
            }

            return true;
        }

        // 当Preference的值发生改变时触发该事件，true则以新值更新控件的状态，false则do noting。
        @Override
        public boolean onPreferenceChange(Preference preference, Object objValue)
        {
            //Log.d("Change", preference.getTitle().toString());
            if(preference == mListView_news_text_size)
            {
                int value = Integer.valueOf((String)objValue);
                mListView_news_text_size.setSummary(summary_news_text_size[value - 1]);
                mZSCNewsApplication.setNewsTextSize(value);
            }
            else if(preference == mCheckBox_brightness)
            {
                mZSCNewsApplication.isBrightness = (Boolean)objValue;
            }
            else if(preference == mCheckBox_mini_list)
            {
                mZSCNewsApplication.isMiniList = (Boolean)objValue;
            }
            else if(preference == mListView_list_transforms)
            {
                int value = Integer.valueOf((String)objValue);
                mListView_list_transforms.setSummary(summary_viewpager_transforms[value - 1]);
                mZSCNewsApplication.setListTransforms(value);
            }
            else if(preference == mSwitch_message_push)
            {
                mZSCNewsApplication.isMessagePush = (Boolean)objValue;

                Thread PushServiceThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(mZSCNewsApplication.isMessagePush)
                        {
                            resumePushService();
                        }
                        else
                        {
                            stopPushService();
                        }

                    }
                });

                PushServiceThread.start();
            }
            else if(preference == mCheckBox_use_cache)
            {
                mZSCNewsApplication.isUseCache = (Boolean)objValue;
            }
            //Log.d("Value", String.valueOf(objValue));
            return true;  //保存更新后的值。
        }

        public void setSummaryOfmPreferenceScreen_clear_cache()
        {
            long cache = mFileService.getCacheSize();
            //Log.d("cache", String.valueOf(cache));
            double size;
            if(cache >= 1048576)
            {
                size = cache/1048576.0;
                BigDecimal result = new BigDecimal(Double.toString(size));
                mPreferenceScreen_clear_cache.setSummary(
                        result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB");
            }
            else
            {
                size = cache/1024.0;
                BigDecimal result = new BigDecimal(Double.toString(size));
                mPreferenceScreen_clear_cache.setSummary(
                        result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB");
            }
        }

        /**
         * 恢复推送服务。
         */
        protected void resumePushService()
        {
            if (!PushServiceUtils.hasBind(getActivity().getApplicationContext()))
            {
                PushManager.resumeWork(getActivity().getApplicationContext());
                // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
                // PushManager.enableLbs(getApplicationContext());
            }
        }

        /**
         * 关闭推送服务。
         */
        protected void stopPushService()
        {
            if (PushServiceUtils.hasBind(getActivity().getApplicationContext()))
            {
                PushManager.stopWork(getActivity().getApplicationContext());
            }
        }

    }
}
