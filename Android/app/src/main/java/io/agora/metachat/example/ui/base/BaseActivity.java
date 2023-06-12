package io.agora.metachat.example.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.base.VideoFrame;
import io.agora.metachat.example.utils.AgoraMediaPlayer;
import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserPositionInfo;
import io.agora.metachat.example.inf.IRtcEventCallback;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.reactivex.disposables.CompositeDisposable;

public class BaseActivity extends Activity implements IMetachatSceneEventHandler, IMetachatEventHandler, IRtcEventCallback, AgoraMediaPlayer.OnMediaVideoFramePushListener, AgoraMediaPlayer.PlayerObserver {
    protected CompositeDisposable compositeDisposable;
    protected boolean mIsFront;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTransparent(getWindow());
        adaptAndroidP(getWindow());

        initContentView();

        initData();

        initView();

        initListener();

        initClickEvent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();

        initView();
    }

    protected void initClickEvent() {
    }

    protected void initContentView() {
    }

    protected void initView() {

    }

    protected void initData() {
        compositeDisposable = new CompositeDisposable();
        mIsFront = false;

        if (MetaChatContext.getInstance().isInitMetachat()) {
            AgoraMediaPlayer.getInstance().initMediaPlayer(MetaChatContext.getInstance().getRtcEngine());
            if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                AgoraMediaPlayer.getInstance().setOnMediaVideoFramePushListener(this);
            } else if (MetaChatConstants.SCENE_IDOL == MetaChatContext.getInstance().getCurrentScene()) {
                AgoraMediaPlayer.getInstance().setPlayerObserver(this);
            }
        }
    }

    protected void initListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsFront = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsFront = false;
    }


    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (MetaChatConstants.SCENE_IDOL == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != compositeDisposable) {
            compositeDisposable.dispose();
        }
    }

    protected void unregister() {
        MetaChatContext.getInstance().unregisterMetaChatEventHandler(this);
        MetaChatContext.getInstance().unregisterMetaChatSceneEventHandler(this);
    }

    protected void register() {
        MetaChatContext.getInstance().registerMetaChatSceneEventHandler(this);
        MetaChatContext.getInstance().registerMetaChatEventHandler(this);

        MetaChatContext.getInstance().setRtcEventCallback(this);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //重写Activity该方法，当窗口焦点变化时自动隐藏system bar，这样可以排除在弹出dialog和menu时，
    //system bar会重新显示的问题(弹出dialog时似乎还是可以重新显示的0.0)。
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY //(修改这个选项，可以设置不同模式)
                        //使用下面三个参数，可以使内容显示在system bar的下面，防止system bar显示或
                        //隐藏时，Activity的大小被resize。
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // 隐藏导航栏和状态栏
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * 1.使状态栏透明
     */
    private static void setTransparent(@NonNull Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    private static void adaptAndroidP(@NonNull Window window) {
        // 适配刘海屏,全屏去黑状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(layoutParams);
        }
    }


    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {

    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {

    }

    @Override
    public void onRequestToken() {

    }

    @Override
    public void onGetSceneInfosResult(MetachatSceneInfo[] sceneInfos, int errorCode) {

    }

    @Override
    public void onDownloadSceneProgress(long sceneId, int progress, int state) {

    }

    @Override
    public void onEnterSceneResult(int errorCode) {

    }

    @Override
    public void onLeaveSceneResult(int errorCode) {

    }

    @Override
    public void onRecvMessageFromScene(byte[] message) {

    }

    @Override
    public void onUserPositionChanged(String uid, MetachatUserPositionInfo posInfo) {

    }

    @Override
    public void onEnumerateVideoDisplaysResult(String[] displayIds) {

    }

    @Override
    public void onReleasedScene(int status) {
    }

    @Override
    public void onSceneVideoFrame(VideoFrame videoFrame) {

    }

    @Override
    public void onRecvMessageFromUser(String userId, byte[] message) {

    }

    @Override
    public void onMediaVideoFramePushed(VideoFrame frame) {

    }

    @Override
    public void onPositionChanged(long position_ms) {

    }

    public void pauseMediaPlayer() {
        AgoraMediaPlayer.getInstance().pause();
    }

    public void resumeMediaPlayer() {
        AgoraMediaPlayer.getInstance().resume();
    }

    public void stopMediaPlayer() {
        AgoraMediaPlayer.getInstance().stop();
    }

    public void destroyMediaPlayer() {
        AgoraMediaPlayer.getInstance().destroy();
    }
}
