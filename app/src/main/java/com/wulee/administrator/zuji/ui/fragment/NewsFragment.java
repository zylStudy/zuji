package com.wulee.administrator.zuji.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.entity.NewsUrl;
import com.wulee.administrator.zuji.widget.NoScroViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by wulee on 2017/9/6 09:52
 */
public class NewsFragment extends MainBaseFrag implements ViewPager.OnPageChangeListener{


    @InjectView(R.id.rl_title)
    RelativeLayout rlTitle;
    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.viewpager)
    NoScroViewPager viewpager;
    private View mRootView;
    private Context mContext;

    private NavigAdapter adapter;
    private MyFragmentPageAdapter pagerAdapter;

    private HashMap<Integer,String> urlsMap = new HashMap<>();

    private List<Fragment> mFragmentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.news_fragment, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        ButterKnife.inject(this, mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int spanCount = 1; // 只显示一行
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NavigAdapter();
        recyclerView.setAdapter(adapter);

        setOnTitleItemClickListener(new OnTitleItemClickListener() {
            @Override
            public void onTitleItemClick(int index) {
                adapter.setSelectIndex(index);
                switch (index) {
                    case 0://社会新闻
                        viewpager.setCurrentItem(0);
                        break;
                    case 1://娱乐新闻
                        viewpager.setCurrentItem(1);
                        break;
                    case 2://体育新闻
                        viewpager.setCurrentItem(2);
                        break;
                    case 3://科技新闻
                        viewpager.setCurrentItem(3);
                        break;
                    case 4://旅游资讯
                        viewpager.setCurrentItem(4);
                        break;
                    case 5://健康知识
                        viewpager.setCurrentItem(5);
                        break;
                }
            }
        });
        pagerAdapter =  new MyFragmentPageAdapter(getChildFragmentManager(),mFragmentList);
        viewpager.setScroll(false);
        viewpager.addOnPageChangeListener(this);
        viewpager.setAdapter(pagerAdapter);
    }

    private void initNewsPagerData() {
        for (int i = 0; i < 6; i++) {
            if(urlsMap.size()>0 && urlsMap.containsKey(i)){
                String url = urlsMap.get(i);
                NewsChildFragment newsFrag = NewsChildFragment.newInstance(url);
                mFragmentList.add(newsFrag);
            }
        }
        pagerAdapter.notifyDataSetChanged();
    }


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_GET_NEWS_URL_OK:
                    initNewsPagerData();
                    break;
            }
        }
    };

    private final int MSG_GET_NEWS_URL_OK = 100;

    private void getNesUrls() {
        BmobQuery<NewsUrl> query = new BmobQuery<>();
        query.order("index");
        query.findObjects(new FindListener<NewsUrl>() {
            @Override
            public void done(List<NewsUrl> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size() ; i++) {
                            String url = list.get(i).getUrl();
                            urlsMap.put(i,url);
                        }
                        mHandler.sendEmptyMessage(MSG_GET_NEWS_URL_OK);
                    }
                }
            }
        });
    }



    @Override
    public void onFragmentFirstSelected() {
        getNesUrls();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        adapter.setSelectIndex(position);
        recyclerView.smoothScrollToPosition(position);//标题滚动到相应的postion
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    class NavigAdapter extends RecyclerView.Adapter<NavigAdapter.ViewHolder> {
        String[] titleArray = new String[]{"社会新闻", "娱乐新闻", "体育新闻", "科技新闻", "旅游资讯", "健康知识"};
        int mIndex = 0;

        public void setSelectIndex(int index) {
            this.mIndex = index;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_title_recyview_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTitle.setText(titleArray[position]);
            if (mIndex == position) {
                holder.mTitle.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            } else {
                holder.mTitle.setTextColor(ContextCompat.getColor(mContext, R.color.ctv_black_2));
            }
            holder.rlRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onTitleItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return titleArray.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private RelativeLayout rlRoot;
            private TextView mTitle;

            public ViewHolder(View itemView) {
                super(itemView);
                rlRoot = (RelativeLayout) itemView.findViewById(R.id.rl_root);
                mTitle = (TextView) itemView.findViewById(R.id.tv_title);
            }
        }
    }

    public OnTitleItemClickListener mListener;

    public void setOnTitleItemClickListener(OnTitleItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnTitleItemClickListener {
        void onTitleItemClick(int index);
    }

    class MyFragmentPageAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList = new ArrayList<>();

        public MyFragmentPageAdapter(FragmentManager fm,List<Fragment> list) {
            super(fm);
            this.fragmentList = list;
        }
        @Override
        public int getCount() {
            return fragmentList.size();
        }
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }

}