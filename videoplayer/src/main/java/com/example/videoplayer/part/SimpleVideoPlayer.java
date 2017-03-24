package com.example.videoplayer.part;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.videoplayer.R;
import com.example.videoplayer.full.VideoViewActivity;

import java.io.IOException;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * 一个自定义的VideoPlayer，使用Mediaplayer + SurfaceView来实现视频的播放
 * Mediaplayer来做视频播放的控制，SurfaceView用来显示视频
 * 视图方面，一个播放、暂停按钮，一个进度条，一个全屏按钮，一个SurfaceView
 * <p>
 * 结构：
 * 提供setVideoPath方法：设置数据源
 * 提供OnResume方法（在Activity的onResume调用）：初始化Mediaplayer，准备Mediaplayer
 * 提供OnPause方法（在Activity的OnPause调用）:释放Mediaplayer，停止播放
 */

public class SimpleVideoPlayer extends FrameLayout {

    //进度条控制（长度）
    private static final int PROGRESS_MAX = 1000;

    private String videoPath;//视频路径
    private MediaPlayer mediaPlayer;
    private boolean isPrepared;//是否准备好
    private boolean isPlaying;//是否正在播放

    //视图相关
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView ivPreView;//预览图
    private ImageButton btnToggle;//播放，暂停
    private ProgressBar progressBar;//进度条
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isPlaying) {
                int pragress = (int) (mediaPlayer.getCurrentPosition() * PROGRESS_MAX / mediaPlayer.getDuration());
                progressBar.setProgress(pragress);
                handler.sendEmptyMessageDelayed(0, 200);
            }
        }
    };

    public SimpleVideoPlayer(Context context) {
        this(context, null);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();//视图初始化相关
    }

    //视图初始化相关
    private void init() {
        //Vitamio初始化
        Vitamio.isInitialized(getContext());
        //填充布局
        LayoutInflater.from(getContext()).inflate(R.layout.view_simple_video_player, this, true);
        //初始化SurfaceView
        initSurfaceView();
        //初始化视频播放控制视图
        initControllerViews();
    }

    //设置数据源
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    //初始化状态(在Activity的onResume调用)
    public void onResume() {
        //初始化MediaPlayer
        initMediaPlayer();
        //准备MediaPlayer
        prepareMediaPlayer();
    }

    //释放状态(在Activity的onPause调用)
    public void onPause() {
        //暂停MediaPlayer
        pauseMediaplayer();
        //释放MediaPlayer
        releaseMediaPlayer();
    }

    //初始化SurfaceView
    private void initSurfaceView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        //注意，使用Vitamio要设置像素格式，否则会花屏
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    //初始化视频播放控制视图
    private void initControllerViews() {
        //预览图
        ivPreView = (ImageView) findViewById(R.id.ivPreview);
        //播放，暂停
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否正在播放
                if (mediaPlayer.isPlaying()) {
                    //暂停播放
                    pauseMediaplayer();
                } else if (isPrepared) {
                    //开始播放
                    startMediaplayer();
                } else {
                    Toast.makeText(getContext(), "Can't play now！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //设置进度条
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(PROGRESS_MAX);
        //全屏播放按钮
        findViewById(R.id.btnFullScreen).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到全屏播放
                VideoViewActivity.open(getContext(),videoPath);
            }
        });
    }

    //初始化MediaPlayer
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer(getContext());
        mediaPlayer.setDisplay(surfaceHolder);
        //准备监听
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                startMediaplayer();
            }
        });
        //audio处理
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_FILE_OPEN_OK) {
                    mediaPlayer.audioInitedOk(mediaPlayer.audioTrackInit());
                    return true;
                }
                return false;
            }
        });
        //视频大小改变监听
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                //参数的宽和高，是指视频的宽和高，我们可以通过参数去设置SurfaceView宽高
                int layoutWidth = surfaceView.getWidth();
                int layoutHeight = layoutWidth * height / width;
                //更新SurfaceView的宽高
                ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
                params.width = layoutWidth;
                params.height = layoutHeight;
                surfaceView.setLayoutParams(params);
            }
        });

    }

    //准备MediaPlayer
    private void prepareMediaPlayer() {
        try {
            //重置Mediaplayer
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoPath);
            //设置循环播放
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            ivPreView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Log.e("SimpleVideoPlayer", "prepare Mediaplayer" + e.getMessage());
        }
    }

    //开始播放
    private void startMediaplayer() {
        ivPreView.setVisibility(View.INVISIBLE);
        btnToggle.setImageResource(R.drawable.ic_pause);
        mediaPlayer.start();
        isPlaying = true;
        //进度条操作
        handler.sendEmptyMessage(0);

    }

    //暂停播放
    private void pauseMediaplayer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        isPlaying = false;
        btnToggle.setImageResource(R.drawable.ic_play_arrow);
        //进度条操作
        handler.removeMessages(0);
    }

    //释放MediaPlayer
    private void releaseMediaPlayer() {
        mediaPlayer.release();
        mediaPlayer = null;
        isPrepared = false;
        progressBar.setProgress(0);
    }
}
