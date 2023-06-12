package io.agora.metachat.example.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import io.agora.mediaplayer.Constants;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
import io.agora.metachat.example.BuildConfig;
import io.agora.musiccontentcenter.IAgoraMusicContentCenter;
import io.agora.musiccontentcenter.IAgoraMusicPlayer;
import io.agora.musiccontentcenter.IMusicContentCenterEventHandler;
import io.agora.musiccontentcenter.Music;
import io.agora.musiccontentcenter.MusicChartInfo;
import io.agora.musiccontentcenter.MusicContentCenterConfiguration;
import io.agora.rtc2.RtcEngine;

public class AgoraMusicPlayer {
    private static final String TAG = AgoraMusicPlayer.class.getSimpleName();
    private volatile static AgoraMusicPlayer mInstance;
    private IAgoraMusicPlayer mAgoraMusicPlayer;
    private IAgoraMusicContentCenter mMcc;

    private MusicContentCenterConfiguration mConfig;

    private long mSongCode;

    private AgoraMusicPlayerCallBack mAgoraMusicPlayerCallBack;

    private static final int MUSIC_PAGE_SIZE = 10;
    private String mMusicChartsRequestId;
    private String mMusicCollectionRequestId;


    private final IMediaPlayerObserver mMediaPlayerObserver = new IMediaPlayerObserver() {
        @Override
        public void onPlayerStateChanged(Constants.MediaPlayerState state, io.agora.mediaplayer.Constants.MediaPlayerError error) {
            if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
                onMusicOpenCompleted();
            } else if (Constants.MediaPlayerState.PLAYER_STATE_PLAYING == state) {
                if (null != mAgoraMusicPlayerCallBack) {
                    mAgoraMusicPlayerCallBack.onPlayStateChange(mSongCode, Constants.MediaPlayerState.PLAYER_STATE_PLAYING);
                }
            } else if (Constants.MediaPlayerState.PLAYER_STATE_PAUSED == state) {
            } else if (Constants.MediaPlayerState.PLAYER_STATE_STOPPED == state) {
            } else if (Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED == state) {
            } else if (Constants.MediaPlayerState.PLAYER_STATE_FAILED == state) {
                if (null != mAgoraMusicPlayerCallBack) {
                    mAgoraMusicPlayerCallBack.onPlayStateChange(mSongCode, Constants.MediaPlayerState.PLAYER_STATE_FAILED);
                    mSongCode = -1;
                }
            } else if (Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_COMPLETED == state) {
                if (null != mAgoraMusicPlayerCallBack) {
                    mAgoraMusicPlayerCallBack.onPlayStateChange(mSongCode, Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_COMPLETED);
                }
            }
        }

        @Override
        public void onPositionChanged(long position_ms) {
        }

        @Override
        public void onPlayerEvent(io.agora.mediaplayer.Constants.MediaPlayerEvent eventCode, long elapsedTime, String message) {
        }

        @Override
        public void onMetaData(io.agora.mediaplayer.Constants.MediaPlayerMetadataType type, byte[] data) {

        }

        @Override
        public void onPlayBufferUpdated(long playCachedBuffer) {

        }

        @Override
        public void onPreloadEvent(String src, io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent event) {
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
    };

    private final IMusicContentCenterEventHandler mIMccEventHandler = new IMusicContentCenterEventHandler() {

        @Override
        public void onPreLoadEvent(long songCode, int percent, int status, String msg, String lyricUrl) {
            Log.i(TAG, "onPreLoadEvent songCode=" + songCode + ",status=" + status + ",msg=" + msg);
            if (status == 0) {
                if (percent == 100 && mSongCode == songCode) {
                    openMusic(songCode);
                }
            } else if (status == 1) {
                if (null != mAgoraMusicPlayerCallBack) {
                    mAgoraMusicPlayerCallBack.onPlayStateChange(mSongCode, Constants.MediaPlayerState.PLAYER_STATE_FAILED);
                    mSongCode = -1;
                }
            }

        }

        @Override
        public void onMusicCollectionResult(String requestId, int status, int page, int pageSize, int total, Music[] list) {
            if (!TextUtils.isEmpty(mMusicCollectionRequestId) && mMusicCollectionRequestId.equals(requestId)) {
                if (list.length > 0) {
                    onLoadMusics(Arrays.asList(list));
                }
            }
        }

        @Override
        public void onMusicChartsResult(String requestId, int status, MusicChartInfo[] list) {
            if (!TextUtils.isEmpty(mMusicChartsRequestId) && mMusicChartsRequestId.equals(requestId)) {
                //加载声网热歌榜
                if (list.length > 0) {
                    loadMusicsByChartId(list[1].type);
                }
            }
        }

        @Override
        public void onLyricResult(String requestId, String lyricUrl) {

        }
    };

    private AgoraMusicPlayer() {
        mSongCode = -1;
    }

    public static AgoraMusicPlayer getInstance() {
        if (null == mInstance) {
            synchronized (AgoraMediaPlayer.class) {
                if (null == mInstance) {
                    mInstance = new AgoraMusicPlayer();
                }
            }
        }
        return mInstance;
    }

    public void init(RtcEngine rtcEngine) {
        if (null == rtcEngine) {
            Log.e(TAG, "please init rtc engine first!");
            return;
        }
        try {
            rtcEngine.loadExtensionProvider("agora_drm_loader_extension");
            mMcc = IAgoraMusicContentCenter.create(rtcEngine);

            mConfig = new MusicContentCenterConfiguration();
            mConfig.appId = BuildConfig.APP_ID;
            mConfig.mccUid = KeyCenter.RTC_UID;
            mConfig.rtmToken = KeyCenter.RTM_TOKEN;
            mConfig.eventHandler = mIMccEventHandler;
            int ret = mMcc.initialize(mConfig);

            mAgoraMusicPlayer = mMcc.createMusicPlayer();

            registerObserver();
            Log.i(TAG, "mcc init finish ret=" + ret);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "init exception =" + e);
        }
    }

    public void destroyMcc() {
        unregisterPlayerObserver();
        mMcc.unregisterEventHandler();
        IAgoraMusicContentCenter.destroy();
        mAgoraMusicPlayer.destroy();
        mAgoraMusicPlayer = null;
        mMcc = null;
        mConfig.eventHandler = null;
        mConfig = null;
    }

    private void unregisterPlayerObserver() {
        mAgoraMusicPlayer.unRegisterPlayerObserver(mMediaPlayerObserver);
    }

    private void registerObserver() {
        mAgoraMusicPlayer.registerPlayerObserver(mMediaPlayerObserver);
    }

    public void setPlayStateCallBack(AgoraMusicPlayerCallBack agoraMusicPlayerCallBack) {
        this.mAgoraMusicPlayerCallBack = agoraMusicPlayerCallBack;
    }

    private void openMusic(long songCode) {
        if (null == mAgoraMusicPlayer) {
            Log.e(TAG, "please init rtc mcc first!");
            return;
        }
        if (songCode != mSongCode) {
            Log.e(TAG, "play: not same song, abort playing");
            return;
        }


        int ret = mAgoraMusicPlayer.open(songCode, 0);
        //int ret = mAgoraMusicPlayer.open("http://agora.fronted.love/yyl.mov",0);
        Log.i(TAG, "open() called ret=" + ret);

        mAgoraMusicPlayer.setLoopCount(MetaChatConstants.PLAY_ADVERTISING_VIDEO_ONCE);
    }

    public void pause() {
        Log.i(TAG, "pause() called");
        if (null == mAgoraMusicPlayer) {
            Log.e(TAG, "please init rtc mcc first!");
            return;
        }

        mAgoraMusicPlayer.pause();
    }

    public void resume() {
        Log.i(TAG, "resume() called");
        if (null == mAgoraMusicPlayer) {
            Log.e(TAG, "please init rtc mcc first!");
            return;
        }

        mAgoraMusicPlayer.resume();
    }

    public void stop() {
        Log.i(TAG, "stop()  called");
        if (null == mAgoraMusicPlayer) {
            Log.e(TAG, "please init rtc mcc first!");
            return;
        }
        mSongCode = -1;
        mAgoraMusicPlayer.stop();
    }

    private void onMusicOpenCompleted() {
        Log.i(TAG, "onMusicOpenCompleted() called");
        if (null == mAgoraMusicPlayer) {
            Log.e(TAG, "please init rtc mcc first!");
            return;
        }
        mAgoraMusicPlayer.play();
    }

    public void preloadMusic(long songCode) {
        Log.i(TAG, "preloadMusic call with song code =" + songCode);
        if (null == mMcc) {
            Log.e(TAG, "please init rtc mcc first!");
            return;
        }
        try {
            stop();
            mSongCode = songCode;
            if (null != mAgoraMusicPlayerCallBack) {
                mAgoraMusicPlayerCallBack.onPlayStateChange(mSongCode, Constants.MediaPlayerState.PLAYER_STATE_OPENING);
            }
            if (0 == mMcc.isPreloaded(mSongCode)) {
                openMusic(mSongCode);
            } else {
                mMcc.preload(mSongCode, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMusics() {
        mMusicChartsRequestId = mMcc.getMusicCharts();
    }

    private void loadMusicsByChartId(int musicChartId) {
        if (-1 == musicChartId) {
            return;
        }
        //默认加载十首歌曲
        mMusicCollectionRequestId = mMcc.getMusicCollectionByMusicChartId(musicChartId, 0, MUSIC_PAGE_SIZE);
    }

    private void onLoadMusics(List<Music> list) {
        if (null != mAgoraMusicPlayerCallBack) {
            mAgoraMusicPlayerCallBack.onLoadMusicResult(list);
        }
    }

    public int getMusicPlayerId() {
        if (null != mAgoraMusicPlayer) {
            return mAgoraMusicPlayer.getMediaPlayerId();
        }
        return -1;
    }

    public long getCurrentPlaySongCode() {
        return mSongCode;
    }

    public interface AgoraMusicPlayerCallBack {
        void onPlayStateChange(long songCode, Constants.MediaPlayerState state);

        void onLoadMusicResult(List<Music> list);
    }

}
