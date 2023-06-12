package io.agora.metachat.example.utils;

import android.net.Uri;
import android.util.Log;

import io.agora.base.VideoFrame;
import io.agora.mediaplayer.Constants;
import io.agora.mediaplayer.IMediaPlayer;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.IMediaPlayerVideoFrameObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
import io.agora.rtc2.RtcEngine;

public class AgoraMediaPlayer implements IMediaPlayerObserver, IMediaPlayerVideoFrameObserver {
    private static final String TAG = AgoraMediaPlayer.class.getSimpleName();
    private volatile static AgoraMediaPlayer mAgoraMediaPlayer;
    private IMediaPlayer mMediaPlayer;
    private OnMediaVideoFramePushListener mOnMediaVideoFramePushListener;

    private PlayerObserver mPlayerObserver;

    private AgoraMediaPlayer() {

    }

    public static AgoraMediaPlayer getInstance() {
        if (null == mAgoraMediaPlayer) {
            synchronized (AgoraMediaPlayer.class) {
                if (null == mAgoraMediaPlayer) {
                    mAgoraMediaPlayer = new AgoraMediaPlayer();
                }
            }
        }
        return mAgoraMediaPlayer;
    }

    public void initMediaPlayer(RtcEngine rtcEngine) {
        mMediaPlayer = rtcEngine.createMediaPlayer();
        mMediaPlayer.adjustPlayoutVolume(20);
        registerObserver();
    }

    public void adjustPlayVolume(int volume) {
        mMediaPlayer.adjustPlayoutVolume(volume);
    }

    public int getMediaPlayerId() {
        if (null != mMediaPlayer) {
            return mMediaPlayer.getMediaPlayerId();
        }
        return -1;
    }

    public void setOnMediaVideoFramePushListener(OnMediaVideoFramePushListener onMediaVideoFramePushListener) {
        this.mOnMediaVideoFramePushListener = onMediaVideoFramePushListener;
    }

    public void setPlayerObserver(PlayerObserver mPlayerObserver) {
        this.mPlayerObserver = mPlayerObserver;
    }

    public void play(String url, long startPos) {
        int ret = mMediaPlayer.open(url, startPos);
        if (ret == io.agora.rtc2.Constants.ERR_OK) {
            mMediaPlayer.setLoopCount(MetaChatConstants.PLAY_ADVERTISING_VIDEO_REPEAT);
        }
    }

    public void play(Uri uri, long startPos) {
        int ret = mMediaPlayer.open(uri, startPos);
        if (ret == io.agora.rtc2.Constants.ERR_OK) {
            mMediaPlayer.setLoopCount(MetaChatConstants.PLAY_ADVERTISING_VIDEO_REPEAT);
        }
    }

    public void stop() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
        }
    }

    public void destroy() {
        if (null != mMediaPlayer) {
            unregisterObserver();
            mMediaPlayer.destroy();
            mMediaPlayer = null;
        }
    }

    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            unregisterObserver();
        }
    }

    public void resume() {
        if (null != mMediaPlayer) {
            mMediaPlayer.resume();
            registerObserver();
        }
    }

    private void registerObserver() {
        if (null != mMediaPlayer) {
            mMediaPlayer.registerVideoFrameObserver(this);
            mMediaPlayer.registerPlayerObserver(this);
        }
    }

    private void unregisterObserver() {
        if (null != mMediaPlayer) {
            mMediaPlayer.registerVideoFrameObserver(null);
            mMediaPlayer.unRegisterPlayerObserver(this);
        }
    }


    @Override
    public void onPlayerStateChanged(Constants.MediaPlayerState state, Constants.MediaPlayerError error) {
        Log.i(TAG, "onPlayerStateChanged state=" + state + ",error=" + error);
        if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
            if (mMediaPlayer.play() != io.agora.rtc2.Constants.ERR_OK) {
                Log.i(TAG, "onPlayerStateChanged play success");
            }
        }
    }

    @Override
    public void onPositionChanged(long position_ms) {
        if (null != mPlayerObserver) {
            mPlayerObserver.onPositionChanged(position_ms);
        }
    }

    @Override
    public void onPlayerEvent(Constants.MediaPlayerEvent eventCode, long elapsedTime, String message) {

    }

    @Override
    public void onMetaData(Constants.MediaPlayerMetadataType type, byte[] data) {

    }

    @Override
    public void onPlayBufferUpdated(long playCachedBuffer) {

    }

    @Override
    public void onPreloadEvent(String src, Constants.MediaPlayerPreloadEvent event) {

    }

    @Override
    public void onAgoraCDNTokenWillExpire() {

    }

    @Override
    public void onPlayerSrcInfoChanged(SrcInfo from, SrcInfo to) {

    }

    @Override
    public void onPlayerInfoUpdated(PlayerUpdatedInfo info) {

    }

    @Override
    public void onAudioVolumeIndication(int volume) {

    }

    @Override
    public void onFrame(VideoFrame frame) {
        if (null != mOnMediaVideoFramePushListener) {
            mOnMediaVideoFramePushListener.onMediaVideoFramePushed(frame);
        }
    }


    public interface OnMediaVideoFramePushListener {
        void onMediaVideoFramePushed(VideoFrame frame);
    }

    public interface PlayerObserver {
        void onPositionChanged(long position_ms);
    }
}
