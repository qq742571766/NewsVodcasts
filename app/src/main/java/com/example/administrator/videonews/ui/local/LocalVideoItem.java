package com.example.administrator.videonews.ui.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.videonews.R;
import com.example.videoplayer.full.VideoViewActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LocalVideoItem extends FrameLayout{


    public LocalVideoItem(Context context) {
        this(context,null);
    }

    public LocalVideoItem(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LocalVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @BindView(R.id.ivPreview)
    ImageView ivPreview;
    @BindView(R.id.tvVideoName)
    TextView tvVideoName;
    private String filePath;//文件路径

    public String getFilePath(){
        return filePath;
    }

    public void setIvPreView(Bitmap bitmap){
        ivPreview.setImageBitmap(bitmap);
    }

    //设置预览图的方法，可以在后台线程执行
    public void setIvPreView(String filePath,final Bitmap bitmap){
        if (!filePath.equals(this.filePath)) return;
        post(new Runnable() {
            @Override
            public void run() {
                ivPreview.setImageBitmap(bitmap);
            }
        });
    }


    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_local_video,this,true);
        ButterKnife.bind(this);
    }

    public void bind(Cursor cursor){
        //取出视频名称
        int index = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
        String videoName = cursor.getString(index);
        tvVideoName.setText(videoName);
        //取出视频路径
        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

        //获取视频预览图，是一个很费事的操作
        //-------放到后台去执行

        //同时获取多张图片，可能会同时又多个线程执行
        //-------线程池来控制

        //获取过的图片，做缓存处理
        //-------LruCache(最近最少使用原则)

//        //获取预览图
//        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
//        //设置预览图
//        ivPreview.setImageBitmap(bitmap);
    }

    //点击item，全屏播放
    @OnClick
    public void click(){
        VideoViewActivity.open(getContext(),filePath);
    }
}
