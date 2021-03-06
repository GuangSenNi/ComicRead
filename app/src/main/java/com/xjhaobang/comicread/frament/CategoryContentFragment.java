package com.xjhaobang.comicread.frament;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.xjhaobang.comicread.R;
import com.xjhaobang.comicread.adapter.CategoryComicRvAdapter;
import com.xjhaobang.comicread.adapter.LoadMoreAdapterWrapper;
import com.xjhaobang.comicread.base.BaseFragment;
import com.xjhaobang.comicread.been.ComicBeen;
import com.xjhaobang.comicread.constract.GetCategoryComicConstract;
import com.xjhaobang.comicread.listener.OnClickRecyclerViewListener;
import com.xjhaobang.comicread.listener.OnLoadMoreDataRv;
import com.xjhaobang.comicread.presenter.GetCategoryComicPresenterImpl;
import com.xjhaobang.comicread.ui.ComicItemActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by PC on 2017/9/29.
 */

public class CategoryContentFragment extends BaseFragment implements GetCategoryComicConstract.View,OnLoadMoreDataRv {
    @BindView(R.id.rv_category)
    RecyclerView mRvCategory;
    @BindView(R.id.srl_fresh)
    SwipeRefreshLayout mSrlFresh;

    private int mType = 0;
    private int mPageNum = 0;
    private List<ComicBeen> mList;
    private LoadMoreAdapterWrapper mAdapterWrapper;
    private CategoryComicRvAdapter mAdapter;
    private GetCategoryComicConstract.Presenter mPresenter;

    public static CategoryContentFragment newInstance(int index) {
        CategoryContentFragment fragment = new CategoryContentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("fragmentID", index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.fragment_category_content;
    }

    @Override
    protected void initData(Bundle bundle) {
        mType = bundle.getInt("fragmentID");
        mList = new ArrayList<>();
        mAdapter = new CategoryComicRvAdapter();
        mAdapterWrapper = new LoadMoreAdapterWrapper(mAdapter,this);
        mAdapterWrapper.updateData(mList);
        mPresenter = new GetCategoryComicPresenterImpl(this);
    }

    @Override
    protected void initView() {
        mRvCategory.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        mRvCategory.setItemAnimator(new DefaultItemAnimator());
        mRvCategory.setAdapter(mAdapterWrapper);
    }

    @Override
    protected void setListener() {
        mSrlFresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSrlFresh.setRefreshing(true);
                mPageNum = 1;
                mPresenter.getCategoryComic(mType,mPageNum);
            }
        });
        mAdapter.setOnRecyclerViewListener(new OnClickRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(mBaseActivity, ComicItemActivity.class);
                intent.putExtra("comicItemUrl",mList.get(position).getUrl());
                intent.putExtra("comicItemTitle",mList.get(position).getTitle());
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }
        });
    }

    @Override
    public void loadMoreData() {
        mPageNum++;
        mPresenter.getCategoryComic(mType,mPageNum);
    }

    @Override
    public void getCategoryComicSuccess(List<ComicBeen> list) {
        if (mSrlFresh != null && mSrlFresh.isRefreshing()){
            mSrlFresh.setRefreshing(false);
        }
        if (list == null || list.size() == 0){
            mAdapterWrapper.setHasMoreData(false);
        }else {
            if (mPageNum == 1){
                mList = list;
                mAdapter.updateData(mList);
            }else {
                mList.addAll(list);
                mAdapter.appendData(list);
            }
        }
        mAdapterWrapper.notifyDataSetChanged();
    }

    @Override
    public void getError(String msg) {
        showToast(msg);
    }

}
