package com.example.administrator.videonews.ui.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/3/23 0023.
 */

//看类的继承关系，，，ctrl + h

public class LocalVideoAdapter extends CursorAdapter{

    // 用来加载视频预览图的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    // 用来缓存已经加载过的预览图像
    private LruCache<String,Bitmap> lruCache = new LruCache<String, Bitmap>(5 * 1024 * 1024){
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    //构造方法（上下文，游标，是否要自动获取数据）
    public LocalVideoAdapter(Context context) {
        super(context, null, true);
    }

    //创建子视图
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new LocalVideoItem(context);
    }
    //绑定数据
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final LocalVideoItem item = (LocalVideoItem) view;
        item.bind(cursor);

        //文件路径
        final String filePath = item.getFilePath();
        //去缓存中获取预览图
        Bitmap bitmap = lruCache.get(filePath);
        if (bitmap != null){
            //设置预览图
            item.setIvPreView(bitmap);
            return;
        }

        //后台线程获取视频预览图
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                //缓存当期的预览图,文件路径作为key
                lruCache.put(filePath,bitmap);
                //将预览图设置到控件上
                //注意，当前是在后台线程
                item.setIvPreView(filePath,bitmap);
            }
        });
    }

    //关闭线程池
    public void release(){
        executorService.shutdown();
    }
}
