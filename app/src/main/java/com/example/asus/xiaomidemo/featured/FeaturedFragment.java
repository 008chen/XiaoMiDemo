package com.example.asus.xiaomidemo.featured;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.View;

import com.example.asus.xiaomidemo.R;
import com.example.asus.xiaomidemo.RefreshManager;
import com.example.asus.xiaomidemo.base.LazyLoadFragment;
import com.example.asus.xiaomidemo.manage.LocalAppManager;
import com.example.asus.xiaomidemo.setting.SettingManager;
import com.example.asus.xiaomidemo.xiaomi.AppInfo;
import com.example.asus.xiaomidemo.xiaomi.JsoupUtil;
import com.example.asus.xiaomidemo.xiaomi.RetrofitManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

//// import butterknife.BindView;
//// import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by asus on 2017/10/23.
 * 精品的页面
 */

public class FeaturedFragment extends LazyLoadFragment implements RefreshManager.RefreshInterface {
//    // @BindView(R.id.featured_rv_result)
    RecyclerView mRvResult;
//    // @BindView(R.id.featured_refresh_layout)
    SmartRefreshLayout mRefreshLayout;

    public List<AppInfo> appInfoList;
    private AppAdapter adapter;

    private boolean isNotLoadBitmap ;


    @Override
    public int getLayoutId() {
        return R.layout.fragment_featured;
    }

    @Override
    public void initViews(View view) {

         mRvResult = view.findViewById(R.id.featured_rv_result);

         mRefreshLayout= view.findViewById(R.id.featured_refresh_layout);
//        // ButterKnife.bind(this, view);
        RefreshManager.getInstance().register(this);

        isNotLoadBitmap = SettingManager.getInstance(getActivity()).isSaveTraffic();

        appInfoList = new ArrayList<>();
        adapter = new AppAdapter(appInfoList, false, isNotLoadBitmap);
        mRvResult.setAdapter(adapter);
        mRvResult.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRefreshLayout.setEnableLoadmore(false);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initData2();
            }
        });
        mRefreshLayout.autoRefresh();
    }

    @Override
    public void loadData(Bundle savedInstanceState) {
        initData2();
    }
    private void initData2() {
        RetrofitManager.getInstance(getActivity()).getAllFeaturedList2(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("testx", "onResponse!!");
                adapter.mAppInfoList= JsoupUtil.getAPPInfoList(response.body());
                adapter.notifyDataSetChanged();
                mRefreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("testx", "onFailure!!",t);

            }
        });

    }
    private void initData() {
        Log.d("testx", "initData");
        RetrofitManager.getInstance(getActivity()).getAllFeaturedList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<AppInfo>>() {
                    @Override
                    public void onCompleted() {
                        for (int i = 0; i < appInfoList.size(); i++) {
                            final int position = i;
                            RetrofitManager.getInstance(getActivity()).getDetailInfo(appInfoList.get(i))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<AppInfo>() {
                                        @Override
                                        public void onCompleted() {
                                            Log.d("testx", "onCompleted");
                                            adapter.notifyItemChanged(position);
                                            if (appInfoList.size() - 1 == position) {
                                                adapter.notifyDataSetChanged();
                                                mRefreshLayout.finishRefresh();
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.d("testx", "onError:" + e.toString(),e.fillInStackTrace());
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onNext(AppInfo appInfo) {
                                            Log.d("testx", "onNext");
                                            boolean isInstall = LocalAppManager.getInstance(getActivity()).isInstall(appInfo);
                                            appInfo.setInstall(isInstall);
                                            //判断是否需要升级
                                            if (isInstall) {
                                                boolean isUpdate = LocalAppManager.getInstance(getActivity()).isNeedUpdate(appInfo);
                                                appInfo.setUpdate(isUpdate);
                                            }
                                            appInfoList.set(position, appInfo);
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("testx", "onError:" + e.toString(),e.fillInStackTrace());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<AppInfo> appInfos) {
                        Log.d("testx", "onNext");
                        appInfoList.clear();
                        appInfoList.addAll(appInfos);
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    public void onRefresh() {
        mRefreshLayout.autoRefresh();
    }

    @Override
    public void onLoadBitMap(boolean isLoad) {
        adapter.setNotLoadBitmap(isLoad);
    }
}
