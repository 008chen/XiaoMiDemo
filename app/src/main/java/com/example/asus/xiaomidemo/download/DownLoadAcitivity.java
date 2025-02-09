package com.example.asus.xiaomidemo.download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
//import androidx.viewpager.widget.ViewPager;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.asus.xiaomidemo.R;
import com.example.asus.xiaomidemo.setting.SettingActivity;
import com.flyco.tablayout.SlidingTabLayout;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.task.XExecutor;

import java.util.ArrayList;
import java.util.List;

//// import butterknife.BindView;
//// import butterknife.ButterKnife;
//// import butterknife.OnClick;

/**
 * Created by asus on 2017/10/31.
 */

public class DownLoadAcitivity extends AppCompatActivity implements XExecutor.OnAllTaskEndListener {

//    // @BindView(R.id.donwload_sliding_tab_layout)
    SlidingTabLayout mSlidingTabLayout;
//    // @BindView(R.id.download_view_pager)
    ViewPager mViewPager;
//    // @BindView(R.id.search_toolbar)
    Toolbar mToolbar;

    private OkDownload mOkDownLoad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
//       //  // ButterKnife.bind(this);

        SlidingTabLayout mSlidingTabLayout = findViewById(R.id.donwload_sliding_tab_layout);

        ViewPager mViewPager = findViewById(R.id.download_view_pager);

         mToolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(mToolbar);
        initView();
        initDonwLoadConfig();
    }

    private void initDonwLoadConfig() {
        mOkDownLoad = OkDownload.getInstance();
        String path = Environment.getExternalStorageDirectory().getPath() + "/download/";

        mOkDownLoad.setFolder(path);//设置全局下载目录
        mOkDownLoad.getThreadPool().setCorePoolSize(3);//设置同时下载数量
        mOkDownLoad.addOnAllTaskEndListener(this);//设置所有任务监听

    }

    private void initView() {
        final List<Fragment> fragmengList = new ArrayList<>();
        fragmengList.add(new DownloadingFragment());
        fragmengList.add(new DownloadFinishFragment());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmengList.get(position);
            }

            @Override
            public int getCount() {
                return fragmengList.size();
            }
        };
        mViewPager.setAdapter(adapter);
        String titles[] = new String[]{"下载中", "已下载"};
        mSlidingTabLayout.setViewPager(mViewPager, titles);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOkDownLoad.removeOnAllTaskEndListener(this);//移除所有任务监听
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DownLoadAcitivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onAllTaskEnd() {
//        Toast.makeText(this, "所有任务下载完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_main_download:
                DownLoadAcitivity.startActivity(this);
                break;
            case R.id.menu_main_setting:
                SettingActivity.startActivity(this);
                break;
            case R.id.menu_main_login:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    //  @OnClick(R.id.search_iv_back)
    public void onClick() {
        finish();
    }
}
