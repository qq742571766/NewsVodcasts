package com.example.videoplayer.list;

//用来管理列表视图上视频播放，共用一个Mediaplayer
//本类对外提供三对方法，给UI层调用
//onResume和onPause，初始化和释放MediaPlayer（生命周期保证）
//startPlayer和stopPlayer，开始和停止播放（提供给UI层来触发业务（逻辑））
//addPlayerBackListener和removeAllListeners，添加个移除监听（与视图交互的接口）


import android.content.Context;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

public class MediaPlayerManager {
    private static MediaPlayerManager sInstance;
    private Context context;
    private List<OnPlaybackListener> onPlaybackListeners;//接口集合
    private MediaPlayer mediaPlayer;
    private boolean needRelease = false;//是否需要释放（如果还没有设置数据源的话，release可能出现空指针的情况）
    private String videoId;//视频ID（用来区分当前操作谁）

    public static MediaPlayerManager getsInstance(Context context){
        if (sInstance == null){
            sInstance = new MediaPlayerManager(context);
        }
        return sInstance;
    }

    private MediaPlayerManager(Context context){
        this.context = context;
        Vitamio.isInitialized(context);
        onPlaybackListeners = new ArrayList<>();
    }

    //获取视频ID
    public String getVideoId(){
        return videoId;
    }

    //初始化Mediaplayer
    public void onResume(){
        mediaPlayer = new MediaPlayer(context);
        //准备监听，设置缓冲大小
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.setBufferSize(512* 1024);
                mediaPlayer.start();
            }
        });
        //设置播放完监听，停止播放，并且更新UI
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayer();//停止播放
            }
        });
        //监听视频大小的改变，更新UI
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                if (width == 0 || height == 0) return;
                changeVideoSize(width,height);//改变视图大小的方法
            }
        });
        //监听Info,获取缓冲状态，更新UI
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_FILE_OPEN_OK:
                        //Vitamio音频处理
                        mediaPlayer.audioInitedOk(mediaPlayer.audioTrackInit());
                        return true;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        startBuffering();//缓冲开始的方法
                        return true;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        endBuffering();//缓冲结束的方法
                        return true;
                }
                return false;
            }
        });
    }

    //释放Mediaplayer
    public void onPause(){
        stopPlayer();
        if (needRelease){
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    private long startTime;//用于避免用户频繁的开关视频

    //开始播放，更新UI（通过接口callBack）
    public void startPlayer(Surface surface,String path,String videoId){
        //避免用户视频的操作开关视频
        if (System.currentTimeMillis() - startTime < 300) return;
        startTime = System.currentTimeMillis();
        //当前是否有其他视频正在播放,有则停止播放
        if (this.videoId != null){
            stopPlayer();
        }
        //更新当前视频Id
        this.videoId = videoId;
        //通知UI更新
        for (OnPlaybackListener listener : onPlaybackListeners){
            listener.onStartPlay(videoId);
        }
        //准备播放
        try {
            mediaPlayer.setDataSource(path);
            needRelease = true;
            mediaPlayer.setSurface(surface);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //停止播放，更新UI（通过接口callBack）
    public void stopPlayer(){
        //判断当前是否有视频播放
        if (videoId == null) return;
        //通知UI更新
        for (OnPlaybackListener listener : onPlaybackListeners){
            listener.onStopPlay(videoId);
        }
        //停止播放，并且重置
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
    }

    //添加播放处理的监听（UI层的callback）
    public void addPlayerBackListener(OnPlaybackListener listener){
        onPlaybackListeners.add(listener);
    }
    //移除监听
    public void removeAllListeners(){
        onPlaybackListeners.clear();
    }

    //调整更改视频尺寸
    private void changeVideoSize(int width, int height) {
        //通知UI更新
        for (OnPlaybackListener listener : onPlaybackListeners){
            listener.onSizeMeasured(videoId,width,height);
        }
    }
    //缓冲开始,更新UI
    private void startBuffering() {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
        //通知UI更新
        for (OnPlaybackListener listener : onPlaybackListeners){
            listener.onStartBuffering(videoId);
        }
    }
    //缓冲结束的方法
    private void endBuffering() {
        mediaPlayer.start();
        //通知UI更新
        for (OnPlaybackListener listener : onPlaybackListeners){
            listener.onStopBuffering(videoId);
        }
    }

    //视图接口
    //在视频播放模块（videoplayer）完成播放处理，视图层(app模块)来实现此接口，完成视图层UI更新
    public interface OnPlaybackListener {

        void onStartBuffering(String videoId); // 视频缓冲开始

        void onStopBuffering(String videoId); // 视频缓冲结束

        void onStartPlay(String videoId); // 开始播放

        void onStopPlay(String videoId);// 停止播放

        void onSizeMeasured(String videoId, int width, int height);// 大小更改
    }
}
