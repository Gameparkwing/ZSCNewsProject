package org.waxberry.zscnews.data;

/**
 * Created by Waxberry on 2014/12/11.
 */
public class NewsListItem
{
    public int itemID;
    public String item_href;
    public String item_title;
    public String item_summary;
    public String item_date;
    public int item_category;

    public NewsListItem(int id, String href, String title, String date, int category)
    {
        this.itemID = id;
        this.item_href = href;
        this.item_title = title;
        this.item_date = date;
        this.item_summary = null;
        this.item_category = category;
    }

    public void setSummary(String summary)
    {
        this.item_summary = summary;
    }

}