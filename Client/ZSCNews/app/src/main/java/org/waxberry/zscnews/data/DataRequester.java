package org.waxberry.zscnews.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.waxberry.zscnews.R;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by GJH-08 on 2014/12/10.
 */
public class DataRequester {

    private Context mContext;
    private FileService mFileService;
    private String strURL;
    private String userID;

    public DataRequester(Context pContext, FileService pFileService)
    {
        this.mContext = pContext;
        this.mFileService = pFileService;
        this.userID = getUUID(pContext);
    }

    private String getUUID(Context pContext)
    {
        String androidId = "" + Settings.Secure.getString(pContext.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
        //Log.d("UUID", androidId);
        return androidId;
    }

    private String requestDataFromServer(String strURL)
    {
        // HttpGet对象
        HttpGet httpRequest = new HttpGet(strURL);
        String strResult = "";

        /*ConnectivityManager mConnectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 检查移动数据和WLAN的链接状态。
        State mobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        if(mobile != State.CONNECTED && mobile != State.CONNECTING && wifi != State.CONNECTED && wifi != State.CONNECTING)
        {
            return strResult;
        }*/

        try
        {
            // HttpClient对象
            HttpClient httpClient = new DefaultHttpClient();
            // 获得HttpResponse对象
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                // 取得返回的数据
                strResult = EntityUtils.toString(httpResponse.getEntity());
            }
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return strResult;
    }

    public int GetNewsCategoryPages(int catid)
    {
        int pages = -1;
        String strResult;
        strURL = "http://" + mContext.getString(R.string.host)
                + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                + "&operation=get_category_pages";
        strURL = strURL + "&catid=" + catid;
        //Log.d("URL", strURL);

        strResult = requestDataFromServer(strURL);

        if(strResult.equals(""))
        {
            return pages;
        }

        try
        {
            JSONObject jsonObject;

            //Log.d("Str", strResult);
            JSONObject mJSONObject = new JSONObject(strResult).getJSONObject(String.valueOf(catid));
            pages = mJSONObject.getInt("category_pages");
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return pages;
    }

    public int GetNewsCategoryItems(int catid)
    {
        int items = -1;
        String strResult;
        strURL = "http://" + mContext.getString(R.string.host)
                + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                + "&operation=get_category_items";
        strURL = strURL + "&catid=" + catid;
        //Log.d("URL", strURL);

        strResult = requestDataFromServer(strURL);

        if(strResult.equals(""))
        {
            return items;
        }

        try
        {
            JSONObject jsonObject;

            //Log.d("Str", strResult);
            JSONObject mJSONObject = new JSONObject(strResult).getJSONObject(String.valueOf(catid));
            items = mJSONObject.getInt("category_items");
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return items;
    }

    public List<NewsListItem> GetNewsList(int catid, int pages, boolean isUseCache, final boolean hasSummary)
    {
        String strResult;
        String cachePathName = (hasSummary ? ("NewsList_" + catid) : ("NewsListMini_" + catid));
        String cacheFileName = String.valueOf(pages);

        if(isUseCache)
        {
            // 检查缓存，如果有缓存文件，就从缓存文件中读取，否则从服务器获取。
            if(mFileService.isFileExists(cachePathName, cacheFileName))
            {
                strResult = mFileService.loadCacheFileFromExternalStorage(cachePathName, cacheFileName);
            }
            else
            {
                if(hasSummary)
                {
                    strURL = "http://" + mContext.getString(R.string.host)
                            + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                            + "&operation=get_list_summary";
                    strURL = strURL + "&catid=" + catid + "&pages=" + pages;
                }
                else
                {
                    strURL = "http://" + mContext.getString(R.string.host)
                            + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                            + "&operation=get_list";
                    strURL = strURL + "&catid=" + catid + "&pages=" + pages;
                }
                //Log.d("URL", strURL);
                strResult = requestDataFromServer(strURL);

                if(!strResult.equals(""))
                {
                    mFileService.saveCacheFileToExternalStorage(cachePathName, cacheFileName, strResult);
                }
            }
        }
        else
        {
            if(hasSummary)
            {
                strURL = "http://" + mContext.getString(R.string.host)
                        + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                        + "&operation=get_list_summary";
                strURL = strURL + "&catid=" + catid + "&pages=" + pages;
            }
            else
            {
                strURL = "http://" + mContext.getString(R.string.host)
                        + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                        + "&operation=get_list";
                strURL = strURL + "&catid=" + catid + "&pages=" + pages;
            }
            //Log.d("URL", strURL);
            strResult = requestDataFromServer(strURL);
        }

        List<NewsListItem> mList = new ArrayList<NewsListItem>();

        if(strResult.equals(""))
        {
            return mList;
        }

        try
        {
            int id, count;
            String href, title, summary, date;
            JSONObject jsonObject;

            //Log.d("Str", strResult);
            JSONObject mJSONObject = new JSONObject(strResult);
            count = mJSONObject.length();

            for(int i = 1; i <= count; i++)
            {
                jsonObject = mJSONObject.getJSONObject(String.valueOf(i));

                id = jsonObject.getInt("item_flag");
                href = jsonObject.getString("item_href");
                title = jsonObject.getString("item_description");
                date = jsonObject.getString("item_date");
                NewsListItem mNewsListItem = new NewsListItem(id, href, title, date, catid);
                if(hasSummary)
                {
                    summary = jsonObject.getString("item_summary");
                    summary = summary + "...";
                    mNewsListItem.setSummary(summary);
                }
                mList.add(mNewsListItem);
            }
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return mList;
    }

    public NewsContentItem GetNewsContent(int catid, int contentid, boolean isUseCache)
    {
        String strResult;
        String cachePathName = "NewsContent_" + catid;
        String cacheFileName = String.valueOf(contentid);

        // 使用本地缓存。
        if(isUseCache)
        {
            // 检查缓存，如果有缓存文件，就从缓存文件中读取，否则从服务器获取。
            if(mFileService.isFileExists(cachePathName, cacheFileName))
            {
                strResult = mFileService.loadCacheFileFromExternalStorage(cachePathName, cacheFileName);
            }
            else
            {
                strURL = "http://" + mContext.getString(R.string.host)
                        + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                        + "&operation=get_content";
                strURL = strURL + "&catid=" + catid + "&contentid=" + contentid;

                //Log.d("URL", strURL);

                strResult = requestDataFromServer(strURL);

                if(!strResult.equals(""))
                {
                    mFileService.saveCacheFileToExternalStorage(cachePathName, cacheFileName, strResult);
                }

            }
        }
        else
        {
            strURL = "http://" + mContext.getString(R.string.host)
                    + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                    + "&operation=get_content";
            strURL = strURL + "&catid=" + catid + "&contentid=" + contentid;

            //Log.d("URL", strURL);

            strResult = requestDataFromServer(strURL);
        }

        NewsContentItem mNewsContentItem = null;

        try
        {
            int id, category;
            String title, subtitle, text;
            JSONObject jsonObject;

            //Log.d("Str", strResult);
            JSONObject mJSONObject = new JSONObject(strResult);

            jsonObject = mJSONObject.getJSONObject(String.valueOf(contentid));

            id = jsonObject.getInt("itemID");
            title = jsonObject.getString("item_title").trim();
            subtitle = jsonObject.getString("item_subtitle").trim();
            text = Html.fromHtml(jsonObject.getString("item_text")).toString();
            category = jsonObject.getInt("item_category");
            mNewsContentItem = new NewsContentItem(id, title, subtitle, text, category);
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return mNewsContentItem;
    }

    public List<HotListItem> GetHotList(boolean isUseCache)
    {
        String strResult;
        String cachePathName = "";
        String cacheFileName = "HotList";

        if(isUseCache)
        {
            // 检查缓存，如果有缓存文件，就从缓存文件中读取，否则从服务器获取。
            if(mFileService.isFileExists(cachePathName, cacheFileName))
            {
                strResult = mFileService.loadCacheFileFromExternalStorage(cachePathName, cacheFileName);
            }
            else
            {
                strURL = "http://" + mContext.getString(R.string.host)
                        + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                        + "&operation=get_hotlist";
                //Log.d("URL", strURL);
                strResult = requestDataFromServer(strURL);

                if(!strResult.equals(""))
                {
                    mFileService.saveCacheFileToExternalStorage(cachePathName, cacheFileName, strResult);
                }
            }
        }
        else
        {
            strURL = "http://" + mContext.getString(R.string.host)
                    + "/news/news_api.php?" + mContext.getString(R.string.server_key)
                    + "&operation=get_hotlist";
            //Log.d("URL", strURL);
            strResult = requestDataFromServer(strURL);
        }

        List<HotListItem> mList = new ArrayList<HotListItem>();

        if(strResult.equals(""))
        {
            return mList;
        }

        try
        {
            int id, count;
            String href, title;
            String[] number = mContext.getResources().getStringArray(R.array.number);
            JSONObject jsonObject;

            //Log.d("Str", strResult);
            JSONObject mJSONObject = new JSONObject(strResult);
            count = mJSONObject.length();
            if(count > 10)
            {
                count = 10;
            }

            for(int i = 1; i <= count; i++)
            {
                jsonObject = mJSONObject.getJSONObject(String.valueOf(i));

                id = jsonObject.getInt("item_flag");
                href = jsonObject.getString("item_href");
                title = jsonObject.getString("item_title");
                HotListItem mHotListItem = new HotListItem(id, href, title, number[i - 1]);
                mList.add(mHotListItem);
            }
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return mList;
    }

    public int Search(int catid, String search_key)
    {
        int res = 0;
        String strResult;

        try
        {
            search_key = URLEncoder.encode(search_key, "utf-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(catid == 0)
        {
            strURL = "http://" + mContext.getString(R.string.host)
                    + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                    + "&operation=search_all";
            strURL = strURL + "&userID=" + this.userID;
            strURL = strURL + "&search_key=" + search_key;
        }
        else
        {
            strURL = "http://" + mContext.getString(R.string.host)
                    + "/news/news_api.php?" + mContext.getString(R.string.server_key)
                    + "&operation=search";
            strURL = strURL + "&userID=" + this.userID;
            strURL = strURL + "&catid=" + catid + "&search_key=" + search_key;
        }
        //Log.d("URL", strURL);
        strResult = requestDataFromServer(strURL);

        if(strResult.equals(""))
        {
            return res;
        }

        try
        {
            res = new JSONObject(strResult).getInt("count");
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return res;
    }

    public List<NewsListItem> GetSearchResultList(int pages, final boolean hasSummary)
    {
        String strResult;

        if(hasSummary)
        {
            strURL = "http://" + mContext.getString(R.string.host)
                    + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                    + "&operation=get_search_list_summary";
            strURL = strURL + "&userID=" + this.userID;
            strURL = strURL + "&pages=" + pages;
        }
        else
        {
            strURL = "http://" + mContext.getString(R.string.host)
                    + "/news/news_api.php?" + mContext.getString(R.string.server_key)
                    + "&operation=get_search_list";
            strURL = strURL + "&userID=" + this.userID;
            strURL = strURL + "&pages=" + pages;
        }
        //Log.d("URL", strURL);
        strResult = requestDataFromServer(strURL);
        //Log.d("strResult", strResult);

        List<NewsListItem> mList = new ArrayList<NewsListItem>();

        if(strResult.equals(""))
        {
            return mList;
        }

        try
        {
            int id, count, category;
            String href, title, summary, date;
            JSONObject jsonObject;

            //Log.d("Str", strResult);
            JSONObject mJSONObject = new JSONObject(strResult);
            count = mJSONObject.length();

            for(int i = 1; i <= count; i++)
            {
                jsonObject = mJSONObject.getJSONObject(String.valueOf(i));

                id = jsonObject.getInt("item_flag");
                href = jsonObject.getString("item_href");
                title = jsonObject.getString("item_description");
                date = jsonObject.getString("item_date");
                category = jsonObject.getInt("item_category");
                NewsListItem mNewsListItem = new NewsListItem(id, href, title, date, category);
                if(hasSummary)
                {
                    summary = jsonObject.getString("item_summary");
                    summary = summary + "...";
                    mNewsListItem.setSummary(summary);
                }
                mList.add(mNewsListItem);
            }
        }
        catch (JSONException e)
        {
            System.out.println("Json parse error");
            e.printStackTrace();
        }

        return mList;
    }

    public void SearchStop()
    {
        strURL = "http://" + mContext.getString(R.string.host)
                + "/news/news_api.php?token=" + mContext.getString(R.string.server_key)
                + "&operation=search_finish" + "&userID=" + this.userID;

        //Log.d("URL", strURL);
        requestDataFromServer(strURL);
    }

}
