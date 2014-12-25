package org.waxberry.zscnews.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

/**
 * Created by GJH-08 on 2014/12/13.
 */
public class SharedPreferencesService {

    public static void WriteBoolean(Context mContext, String FileName, String Key, boolean Value)
    {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(FileName,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Key, Value);
        editor.apply();
    }

    public static boolean ReadBoolean(Context mContext, String FileName, String Key)
    {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(FileName,
                Activity.MODE_PRIVATE);

        return mSharedPreferences.getBoolean(Key, false);
    }

    public static void WriteInt(Context mContext, String FileName, String Key, int Value)
    {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(FileName,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(Key, Value);
        editor.apply();
    }

    public static int ReadInt(Context mContext, String FileName, String Key)
    {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(FileName,
                Activity.MODE_PRIVATE);

        return mSharedPreferences.getInt(Key, -1);
    }

    public static void WriteString(Context mContext, String FileName, String Key, String Value)
    {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(FileName,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Key, Value);
        editor.apply();
    }

    public static String ReadString(Context mContext, String FileName, String Key)
    {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(FileName,
                Activity.MODE_PRIVATE);

        return mSharedPreferences.getString(Key, "");
    }

}
