package org.waxberry.zscnews.data;

/**
 * Created by GJH-08 on 2014/12/17.
 */
public class HotListItem {

    public int itemID;
    public String item_href;
    public String item_title;
    public String item_number;

    public HotListItem(int id, String href, String title, String number)
    {
        this.itemID = id;
        this.item_href = href;
        this.item_title = title;
        this.item_number = number;
    }

}
