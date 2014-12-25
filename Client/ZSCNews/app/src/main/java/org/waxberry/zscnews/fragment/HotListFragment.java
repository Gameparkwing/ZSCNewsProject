package org.waxberry.zscnews.fragment;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.waxberry.zscnews.R;
import org.waxberry.zscnews.ZSCNewsApplication;
import org.waxberry.zscnews.activity.NewsContentActivity;
import org.waxberry.zscnews.data.DataRequester;
import org.waxberry.zscnews.data.FileService;
import org.waxberry.zscnews.data.HotListAdapter;
import org.waxberry.zscnews.data.HotListItem;
import org.waxberry.zscnews.events.OnLoadMoreListener;
import org.waxberry.zscnews.exceptions.NoEmptyViewException;
import org.waxberry.zscnews.exceptions.NoListViewException;
import org.waxberry.zscnews.view.ZSCNewsList;
import org.waxberry.zscnews.view.ZSCNewsListHeader;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HotListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class HotListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    private int tag;

    private SwipeRefreshLayout mSwipeLayout;
    private ZSCNewsListHeader mHeader;
    private ZSCNewsListHeader mFooter;
    private ListView mListView;
    private HotListAdapter mAdapter;
    private List<HotListItem> mList;

    private DataRequester mDataRequester;
    private FileService mFileService;
    private ZSCNewsApplication mZSCNewsApplication;
    private static Handler handler = new Handler();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position Parameter 1.
     * @return A new instance of fragment HotListFragment.
     */
    public static HotListFragment newInstance(int position) {
        HotListFragment fragment = new HotListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }
    public HotListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在配置变化的时候将这个fragment保存下来。
        setRetainInstance(true);

        if (getArguments() != null) {
            tag = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_hot_list, container, false);

        mListener.setTitle(getString(R.string.title_fragment_hot_list));

        mZSCNewsApplication = (ZSCNewsApplication) getActivity().getApplication();
        mListView = (ListView) mView.findViewById(R.id.hot_list);

        mFileService = new FileService(getActivity().getApplicationContext());
        mDataRequester = new DataRequester(getActivity(), mFileService);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position >= 1)
                {
                    //Log.d("List Item", String.valueOf(position));
                    Intent intent_data = new Intent();
                    intent_data.setClass(getActivity(), NewsContentActivity.class);
                    intent_data.putExtra(getString(R.string.key_NewsCategoryID), 14);
                    intent_data.putExtra(getString(R.string.key_NewsContentID),
                            mAdapter.mHotListItem.get(position - 1).itemID);
                    intent_data.putExtra(getString(R.string.key_NewsTag), tag);
                    intent_data.putExtra(getString(R.string.key_NewsURL),
                            mAdapter.mHotListItem.get(position - 1).item_href);
                    getActivity().startActivity(intent_data);
                }
            }
        });

        mHeader = new ZSCNewsListHeader(getActivity());
        mHeader.setTextViewVisibility(View.GONE);
        mListView.addHeaderView(mHeader);
        mFooter = new ZSCNewsListHeader(getActivity());
        mFooter.setText("");
        mFooter.setTextViewVisibility(View.GONE);
        mListView.addFooterView(mFooter);

        if(mAdapter == null)
        {
            initList();
        }
        else
        {
            mListView.setAdapter(mAdapter);
        }

        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return mView;
    }

    private void initList()
    {
        Thread getHotListThread = new Thread(new Runnable() {
            @Override
            public void run() {

                mList = mDataRequester.GetHotList(mZSCNewsApplication.isUseCache);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(mList.isEmpty())
                        {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.toast_error_get_news_list, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            mAdapter = new HotListAdapter(getActivity(), mList);
                            mListView.setAdapter(mAdapter);
                        }
                    }
                });

            }
        });

        getHotListThread.start();
    }

    private void refreshList()
    {
        Thread getHotListThread = new Thread(new Runnable() {
            @Override
            public void run() {

                mFileService.deleteCache("HotList");
                mFileService.deleteCache("NewsContent_14");

                mList = mDataRequester.GetHotList(mZSCNewsApplication.isUseCache);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(mList.isEmpty())
                        {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.toast_error_get_news_list, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            mAdapter = new HotListAdapter(getActivity(), mList);
                            mListView.setAdapter(mAdapter);
                        }

                        mSwipeLayout.setRefreshing(false);
                        mHeader.setText(getString(R.string.refresh_finish));
                    }
                });


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mHeader.setTextViewVisibility(View.GONE);
                        mHeader.setText(getString(R.string.refreshing));
                    }
                }, 1000);

            }
        });

        getHotListThread.start();
    }

    @Override
    public void onRefresh()
    {
        mHeader.setText(getString(R.string.refreshing));
        mHeader.setTextViewVisibility(View.VISIBLE);
        refreshList();
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
        public void setTitle(String title);
    }

}
