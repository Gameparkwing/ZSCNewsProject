package org.waxberry.zscnews.data;

/**
 * Created by GJH-08 on 2014/12/11.
 */
public class NewsContentItem {

    public int itemID;
    public String item_title;
    public String item_subtitle;
    public String item_text;
    public int item_category;

    public NewsContentItem(int id, String title, String subtitle, String text, int category)
    {
        this.itemID = id;
        this.item_title = title;
        this.item_subtitle = subtitle;
        this.item_text = text;
        this.item_category = category;
    }

}
