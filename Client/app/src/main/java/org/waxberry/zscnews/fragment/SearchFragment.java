package org.waxberry.zscnews.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.waxberry.zscnews.R;
import org.waxberry.zscnews.ZSCNewsApplication;
import org.waxberry.zscnews.activity.NewsContentActivity;
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

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    private int tag;
    private int resultCount;

    private View rView;
    private EditText EditText_Search;
    private Thread SearchThread;
    private int catid;

    private DataRequester mDataRequester;
    private FileService mFileService;
    private ZSCNewsApplication mZSCNewsApplication;
    private static Handler handler = new Handler();

    private ZSCNewsListHeader mHeader;
    private ListView mListView;
    private NewsListAdapter mAdapter;
    private ZSCNewsList zscnewsList;
    public FloatingActionButton fab;

    private OnFragmentInteractionListener mListener;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position Parameter 1.
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance(int position) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }
    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在配置变化的时候将这个fragment保存下来。
        setRetainInstance(true);

        if (getArguments() != null) {
            tag = getArguments().getInt(ARG_SECTION_NUMBER);
            catid = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_search, container, false);

        rView = mView;
        mListener.setTitle(getString(R.string.title_fragment_search));

        SpinnerAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.news_category_spinner, R.layout.actionbar_spinner_item);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setSubtitle(R.string.subtitle_fragment_search);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                catid = itemPosition;
                return false;
            }
        });

        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.layout_editText);
        layout.setBackgroundColor(getResources().getColor(MyColor[tag]));

        mZSCNewsApplication = (ZSCNewsApplication) getActivity().getApplication();
        fab = (FloatingActionButton) mView.findViewById(R.id.fab);

        mListView = (ListView) mView.findViewById(R.id.result_list);
        EditText_Search = (EditText) mView.findViewById(R.id.editText_search);

        mFileService = new FileService(getActivity().getApplicationContext());
        mDataRequester = new DataRequester(getActivity(), mFileService);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("List Item", String.valueOf(position));
                if(position >= 1 && position - 1 < mAdapter.getCount())
                {
                    Intent intent_data = new Intent();
                    intent_data.setClass(getActivity(), NewsContentActivity.class);
                    intent_data.putExtra(getString(R.string.key_NewsCategoryID),
                            mAdapter.mNewsListItem.get(position - 1).item_category);
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

        initResultList(mView, mListView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListView.requestFocusFromTouch();
                mListView.setSelection(0);
            }
        });

        Button btn_search = (Button) mView.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = EditText_Search.getText().toString();
                if(!str.isEmpty())
                {
                    Search(catid, str);
                }
            }
        });

        return mView;
    }

    private void Search(final int catid, final String key_string)
    {
        SearchThread = new Thread(new Runnable() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        DisplayResultList(rView);
                    }
                });

                final int count = mDataRequester.Search(catid, key_string);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(count > 0)
                        {
                            zscnewsList.setMaxItemsCount(count);
                            // adding data at the first time
                            addNewsListItem(1);

                            String str = String.format(getString(R.string.search_result), count);
                            mHeader.setText(str);
                            mHeader.setTextViewVisibility(View.VISIBLE);
                        }
                        else
                        {
                            // We must call finishLoading
                            // when finishing adding data
                            zscnewsList.finishLoading();
                            mHeader.setText("");
                            mHeader.setTextViewVisibility(View.GONE);
                        }
                        resultCount = count;
                    }
                });
            }
        });

        SearchThread.start();
    }

    private void initResultList(View rootView, ListView listView) {

        // ResultList initializing
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

            // Callback called when reaching last item in ListView
            zscnewsList.setOnLoadMoreListener(new OnLoadMoreListener() {

                @Override
                public void onLoadMore(final int totalItemCount) {
                    Log.i("NewsListFragment", "onStart totalItemCount "
                            + (totalItemCount - mListView.getHeaderViewsCount() - mListView.getFooterViewsCount()));

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
            mListView.setAdapter(mAdapter);
            zscnewsList.setMaxItemsCount(resultCount);
            if(resultCount > 0)
            {
                String str = String.format(getString(R.string.search_result), resultCount);
                mHeader.setText(str);
                mHeader.setTextViewVisibility(View.VISIBLE);
            }
            else
            {
                mHeader.setText("");
                mHeader.setTextViewVisibility(View.VISIBLE);
            }
        }
    }

    protected void DisplayResultList(View rootView)
    {
        if(zscnewsList == null)
        {
            initResultList(rootView, mListView);
        }

        mAdapter = new NewsListAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        zscnewsList.setMaxItemsCount(0);

        try
        {
            // Sets the view to show if the adapter is empty
            // see startCentralLoading() method
            View noItems = rootView.findViewById(R.id.noItems);
            zscnewsList.setEmptyView(noItems);

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
    }

    protected void addNewsListItem(final int pages) {

        Thread getNewsListThread = new Thread(new Runnable() {
            @Override
            public void run() {

                final List<NewsListItem> pList = mDataRequester.GetSearchResultList(
                        pages, !mZSCNewsApplication.isMiniList);

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

        Thread SearchStopThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mDataRequester.SearchStop();
            }
        });
        SearchStopThread.start();
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
        public void setTitle(String title);
    }

}
