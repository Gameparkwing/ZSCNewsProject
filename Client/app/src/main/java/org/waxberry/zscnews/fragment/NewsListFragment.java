package org.waxberry.zscnews.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;
import org.waxberry.zscnews.R;
import org.waxberry.zscnews.ZSCNewsApplication;
import org.waxberry.zscnews.activity.ListBaseActivity;
import org.waxberry.zscnews.activity.MainActivity;
import org.waxberry.zscnews.activity.NewsContentActivity;
import org.waxberry.zscnews.activity.SettingsActivity;
import org.waxberry.zscnews.data.DataRequester;
import org.waxberry.zscnews.data.FileService;
import org.waxberry.zscnews.data.NewsListAdapter;
import org.waxberry.zscnews.data.NewsListItem;
import org.waxberry.zscnews.events.OnLoadMoreListener;
import org.waxberry.zscnews.exceptions.NoEmptyViewException;
import org.waxberry.zscnews.exceptions.NoListViewException;
import org.waxberry.zscnews.view.FloatingActionButton;
import org.waxberry.zscnews.view.ZSCNewsList;
import org.waxberry.zscnews.view.ZSCNewsListHeader;

import java.io.File;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class NewsListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    private int categoryID;
    private int categoryItems;

    private SwipeRefreshLayout mSwipeLayout;
    private ZSCNewsListHeader mHeader;
    private ListView mListView;
    private NewsListAdapter mAdapter;
    private ZSCNewsList zscnewsList;
    public FloatingActionButton fab;

    private DataRequester mDataRequester;
    private FileService mFileService;
    private ZSCNewsApplication mZSCNewsApplication;
    private static Handler handler = new Handler();

    private OnFragmentInteractionListener mListener;

    private int tag;
    private int[] category = {1, 2, 3, 4, 5, 6, 7, 8, 9, 13};

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position Parameter 1.
     * @return A new instance of fragment NewsListFragment.
     */
    public static NewsListFragment newInstance(int position) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }
    public NewsListFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在配置变化的时候将这个fragment保存下来。
        setRetainInstance(true);

        if (getArguments() != null) {
            tag = getArguments().getInt(ARG_SECTION_NUMBER);
            categoryID = category[tag];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_news_list, container, false);

        mZSCNewsApplication = (ZSCNewsApplication) getActivity().getApplication();
        fab = (FloatingActionButton) mView.findViewById(R.id.fab);

        mListView = (ListView) mView.findViewById(R.id.news_list);

        mFileService = new FileService(getActivity().getApplicationContext());
        mDataRequester = new DataRequester(getActivity().getApplicationContext(), mFileService);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position >= 1 && position - 1 < mAdapter.getCount())
                {
                    //Log.d("List Item", String.valueOf(position));
                    Intent intent_data = new Intent();
                    intent_data.setClass(getActivity(), NewsContentActivity.class);
                    intent_data.putExtra(getString(R.string.key_NewsCategoryID), categoryID);
                    intent_data.putExtra(getString(R.string.key_NewsContentID),
                            mAdapter.mNewsListItem.get(position - 1).itemID);
                    intent_data.putExtra(getString(R.string.key_NewsTag), tag);
                    intent_data.putExtra(getString(R.string.key_NewsURL),
                            mAdapter.mNewsListItem.get(position - 1).item_href);
                    getActivity().startActivity(intent_data);
                }
            }
        });

        mHeader = new ZSCNewsListHeader(getActivity());
        mHeader.setTextViewVisibility(View.GONE);
        mListView.addHeaderView(mHeader);

        initList(mView, mListView);

        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListView.requestFocusFromTouch();
                mListView.setSelection(0);
            }
        });

        return mView;
    }

    private void initList(View rootView, ListView listView) {

        // DobList initializing
        zscnewsList = new ZSCNewsList();
        try
        {
            // Register ListView
            //
            // NoListViewException will be thrown when
            // there is no ListView
            zscnewsList.register(listView, fab);

            // Add ProgressBar to footers of ListView
            // to be shown in loading more
            zscnewsList.addDefaultLoadingFooterView();

            // Sets the view to show if the adapter is empty
            // see startCentralLoading() method
            View noItems = rootView.findViewById(R.id.noItems);
            zscnewsList.setEmptyView(noItems);

            // Callback called when reaching last item in ListView
            zscnewsList.setOnLoadMoreListener(new OnLoadMoreListener() {

                @Override
                public void onLoadMore(final int totalItemCount) {
                    Log.i("NewsListFragment", "onStart totalItemCount " + totalItemCount);

                    // Just inserting some dummy data after
                    // period of time to simulate waiting
                    // data from server
                    addNewsListItem(mAdapter.getPagesCount());
                }
            });

        }
        catch (NoListViewException e)
        {
            e.printStackTrace();
        }

        if(mAdapter != null)
        {
            listView.setAdapter(mAdapter);
        }
        else
        {
            zscnewsList.setMaxItemsCount(0);
            try
            {
                // Show ProgressBar at the center of ListView
                // this can be used while loading data from
                // server at the first time
                //
                // setEmptyView() must be called before
                //
                // NoEmptyViewException will be thrown when
                // there is no EmptyView
                zscnewsList.startCentralLoading();

            }
            catch (NoEmptyViewException e)
            {
                e.printStackTrace();
            }

            mAdapter = new NewsListAdapter(getActivity());
            mListView.setAdapter(mAdapter);
            // adding data at the first time
            addNewsListItem(1);
            //refreshNewsListItem();
        }
    }

    protected void addNewsListItem(final int pages) {

        Thread getNewsListThread = new Thread(new Runnable() {
            @Override
            public void run() {

                categoryItems = mDataRequester.GetNewsCategoryItems(categoryID);
                zscnewsList.setMaxItemsCount(categoryItems);

                //Log.d("Pages", String.valueOf(categoryItems));

                final List<NewsListItem> pList = mDataRequester.GetNewsList(
                        categoryID, pages, mZSCNewsApplication.isUseCache, !mZSCNewsApplication.isMiniList);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(pList.isEmpty())
                        {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.toast_error_get_news_list, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            mAdapter.addItems(pList);
                            mAdapter.notifyDataSetChanged();
                        }

                        // We must call finishLoading
                        // when finishing adding data
                        zscnewsList.finishLoading();
                    }
                });

            }
        });

        getNewsListThread.start();

    }

    protected void refreshNewsListItem() {

        Thread getNewsListThread = new Thread(new Runnable() {
            @Override
            public void run() {

                categoryItems = mDataRequester.GetNewsCategoryItems(categoryID);
                zscnewsList.setMaxItemsCount(categoryItems);

                //Log.d("Pages", String.valueOf(categoryItems));

                mFileService.deleteCache("NewsList_" + categoryID);
                mFileService.deleteCache("NewsListMini_" + categoryID);
                mFileService.deleteCache("NewsContent_" + categoryID);

                final List<NewsListItem> pList = mDataRequester.GetNewsList(
                        categoryID, 1, mZSCNewsApplication.isUseCache, !mZSCNewsApplication.isMiniList);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(pList.isEmpty())
                        {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.toast_error_get_news_list, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            mAdapter = new NewsListAdapter(getActivity());
                            //mAdapter.mNewsListItem.clear();
                            //mAdapter.setPagesCount(1);
                            mAdapter.addItems(pList);
                            //mAdapter.notifyDataSetChanged();
                            mListView.setAdapter(mAdapter);
                        }

                        mSwipeLayout.setRefreshing(false);
                        if(getActivity() != null)
                        {
                            mHeader.setText(getString(R.string.refresh_finish));
                        }
                    }
                });

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mHeader.setTextViewVisibility(View.GONE);
                    }
                }, 1000);
            }
        });

        getNewsListThread.start();

    }

    @Override
    public void onRefresh()
    {
        mHeader.setText(getString(R.string.refreshing));
        mHeader.setTextViewVisibility(View.VISIBLE);
        refreshNewsListItem();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            Intent intent_data = new Intent();
            intent_data.setClass(getActivity(), SettingsActivity.class);
            intent_data.putExtra(getString(R.string.key_NewsTag), tag);
            getActivity().startActivity(intent_data);
            return true;
        }
        else if(id == R.id.action_hot)
        {
            Intent intent_data = new Intent();
            intent_data.setClass(getActivity(), ListBaseActivity.class);
            intent_data.putExtra(getString(R.string.key_NewsTag), tag);
            intent_data.putExtra(getString(R.string.key_Type), 1);
            getActivity().startActivity(intent_data);
        }
        else if(id == R.id.action_search)
        {
            Intent intent_data = new Intent();
            intent_data.setClass(getActivity(), ListBaseActivity.class);
            intent_data.putExtra(getString(R.string.key_NewsTag), tag);
            intent_data.putExtra(getString(R.string.key_Type), 2);
            getActivity().startActivity(intent_data);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
     public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
    }

}
