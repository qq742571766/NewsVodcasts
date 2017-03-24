package com.example.administrator.videonews.ui.local;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.administrator.videonews.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class LocalVideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.gridView)
    GridView gridView;
    private Unbinder unbinder;
    private LocalVideoAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化当前页面的Loader（加载器，去loader视频数据）
        getLoaderManager().initLoader(0, null, this);
        //初始化适配器
        adapter = new LocalVideoAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        //设置适配器
        gridView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.release();//关闭线程池
    }

    //  ###########################   loader回调   ##################
    //创建所需loader对象
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                //视频ID
                MediaStore.Video.Media._ID,
                //视频文件的路径
                MediaStore.Video.Media.DATA,
                //视频名称
                MediaStore.Video.Media.DISPLAY_NAME
        };

        return new CursorLoader(
                getContext(),
                //视频 uri
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                //需要获取的内容
                projection,
                null, null, null
        );
    }

    //数据加载完成
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        Log.e("aaa", "本地视频的数量=" + data.getCount());
//
//        if (data.moveToFirst()) {
//            do {
//                int index = data.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
//                String name = data.getString(index);
//                Log.e("aaa", "视频名 = " + name);
//            } while (data.moveToNext());
//        }

        adapter.swapCursor(data);
    }

    //数据加载重置
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
