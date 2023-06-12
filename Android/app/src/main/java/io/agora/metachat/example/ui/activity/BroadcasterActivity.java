package io.agora.metachat.example.ui.activity;


import static io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.agora.base.VideoFrame;
import io.agora.metachat.example.adapter.idol.ActionRecyclerAdapter;
import io.agora.metachat.example.adapter.idol.BgmRecyclerAdapter;
import io.agora.metachat.example.adapter.idol.ChangeViewRecyclerAdapter;
import io.agora.metachat.example.adapter.idol.UnitySceneRecyclerAdapter;
import io.agora.metachat.example.models.idol.ActionMenu;
import io.agora.metachat.example.models.idol.BgmMusic;
import io.agora.metachat.example.models.idol.ActionInfo;
import io.agora.metachat.example.models.idol.UnitySceneInfo;
import io.agora.metachat.example.ui.base.BaseActivity;
import io.agora.metachat.example.ui.view.BottomDialog;
import io.agora.metachat.example.utils.AgoraMusicPlayer;
import io.agora.metachat.example.utils.KeyCenter;
import io.agora.mediaplayer.Constants;
import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.example.R;
import io.agora.metachat.example.ui.main.MainActivity;
import io.agora.metachat.example.databinding.BroadcasterActivityBinding;
import io.agora.metachat.example.inf.IRtcEventCallback;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.utils.MenuActionUtils;
import io.agora.metachat.example.utils.Utils;
import io.agora.musiccontentcenter.Music;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.reactivex.disposables.Disposable;
import io.agora.metachat.example.BuildConfig;

public class BroadcasterActivity extends BaseActivity implements IMetachatSceneEventHandler, IMetachatEventHandler, IRtcEventCallback, View.OnClickListener {

    private final String TAG = BroadcasterActivity.class.getSimpleName();
    private BroadcasterActivityBinding binding;
    private TextureView mTextureView = null;

    private boolean mReCreateScene;
    private int mFrameWidth;
    private int mFrameHeight;

    private int mCurrentViewLevel;

    // level ==> layout
    private Map<Integer, View> mLevelLayoutMap;

    //view id ===> level
    private Map<Integer, Integer> mViewIdLevelMap;

    //view id ===> view
    private Map<Integer, View> mViewIdViewMap;

    private int mViewGroupId;

    private List<BgmMusic> mBgmMusicList;
    private BottomDialog mBgmSelectDialog;
    private BgmRecyclerAdapter mBgmAdapter;


    private List<UnitySceneInfo> mSceneInfoList;
    private BottomDialog mSceneSelectDialog;
    private UnitySceneRecyclerAdapter mSceneAdapter;

    private List<ActionInfo> mActionInfoList;
    private BottomDialog mActionSelectDialog;
    private ActionRecyclerAdapter mActionAdapter;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);

    private final List<Music> mLocalMusicList = new ArrayList<Music>() {
        {
            add(new Music(6625526731807940L, "Mi Gente", "J Balvin；Willy William", "", "", 0, 0, 0, null, null, null));
            add(new Music(6625526619592330L, "Beautiful Now", "Jon Bellion；Zedd", "", "", 0, 0, 0, null, null, null));
            add(new Music(6387858737215170L, "booyah", "showtek", "", "", 0, 0, 0, null, null, null));
            add(new Music(6315145703083760L, "Can't Feel My Face", "The Weeknd", "", "", 0, 0, 0, null, null, null));
            add(new Music(6625526733989090L, "We Are Legends", "Hardwell", "", "", 0, 0, 0, null, null, null));
        }
    };

    private int mCurrentBmgIndex;
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        binding.menuLayout.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        binding.viewGroup.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUnityView();
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        binding = BroadcasterActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        super.initData();
        mFrameWidth = -1;
        mFrameHeight = -1;
        resetSceneState();
        if (mLevelLayoutMap == null) {
            mLevelLayoutMap = new HashMap<>();
        } else {
            mLevelLayoutMap.clear();
        }
        if (null == mViewIdLevelMap) {
            mViewIdLevelMap = new HashMap<>();
        } else {
            mViewIdLevelMap.clear();
        }
        if (null == mViewIdViewMap) {
            mViewIdViewMap = new HashMap<>();
        } else {
            mViewIdViewMap.clear();
        }
        mViewGroupId = 1000;

        if (mBgmMusicList == null) {
            mBgmMusicList = new ArrayList<>();
        } else {
            mBgmMusicList.clear();
        }

        if (mSceneInfoList == null) {
            mSceneInfoList = new ArrayList<>();
        } else {
            mSceneInfoList.clear();
        }

        if (mActionInfoList == null) {
            mActionInfoList = new ArrayList<>();
        } else {
            mActionInfoList.clear();
        }

        mCurrentBmgIndex = 0;
    }

    @Override
    protected void initView() {
        if (BuildConfig.DEBUG_MENUS) {
            mCurrentViewLevel = 1;
            addMenuList(MenuActionUtils.getInstance().getBroadcasterActionMenus(), binding.menuRootViews);

            mLevelLayoutMap.put(mCurrentViewLevel, binding.menuRootViews);
            mViewIdLevelMap.put(binding.menuRootViews.getId(), mCurrentViewLevel);
        } else {
            binding.menuLayout.removeAllViews();

            ChangeViewRecyclerAdapter adapter = new ChangeViewRecyclerAdapter(getApplicationContext(), MenuActionUtils.getInstance().getBroadcasterChangeViews());
            binding.changeViewList.setAdapter(adapter);
            binding.changeViewList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            binding.changeViewList.addItemDecoration(new ChangeViewRecyclerAdapter.SpacesItemDecoration(5));

            adapter.setOnItemClick(new ChangeViewRecyclerAdapter.OnItemClick() {
                @Override
                public void onItemClick(ActionMenu actionMenu) {
                    if (!TextUtils.isEmpty(actionMenu.getMenuData())) {
                        MetaChatContext.getInstance().sendSceneMessage(actionMenu.getMenuData());
                    }
                }
            });

            binding.changeViewList.setVisibility(View.GONE);
            binding.changeViewLayout.setBackground(null);
        }
    }


    @Override
    protected void initListener() {
        super.initListener();
        isEnterScene.addOnPropertyChangedCallback(callback);
    }

    @Override
    protected void initClickEvent() {
        super.initClickEvent();
        Disposable disposable;
        if (BuildConfig.DEBUG_MENUS) {
            disposable = RxView.clicks(binding.menuLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                removeViewByViewId(-1);
            });
            compositeDisposable.add(disposable);
        } else {
            disposable = RxView.clicks(binding.btnExit).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                resetViewVisibility();
                MetaChatContext.getInstance().resetRoleInfo();
                MetaChatContext.getInstance().leaveScene();
            });
            compositeDisposable.add(disposable);

            disposable = RxView.clicks(binding.btnChangeView).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (View.VISIBLE == binding.changeViewList.getVisibility()) {
                    binding.changeViewList.setVisibility(View.GONE);
                    binding.changeViewLayout.setBackground(null);
                } else {
                    binding.changeViewList.setVisibility(View.VISIBLE);
                    binding.changeViewLayout.setBackgroundResource(R.drawable.idol_change_view_bg);
                }
            });
            compositeDisposable.add(disposable);


            disposable = RxView.clicks(binding.menuLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                binding.changeViewList.setVisibility(View.GONE);
                binding.changeViewLayout.setBackground(null);
            });
            compositeDisposable.add(disposable);

            disposable = RxView.clicks(binding.btnSwitchBgm).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (null == mBgmSelectDialog) {
                    mBgmAdapter = new BgmRecyclerAdapter(getApplicationContext(), mBgmMusicList);
                    mBgmSelectDialog = new BottomDialog(this)
                            .initRecyclerView(new LinearLayoutManager(getApplicationContext()), mBgmAdapter)
                            .setTitle(this.getResources().getString(R.string.bgm_title));
                    mBgmAdapter.setOnItemClick(new BgmRecyclerAdapter.OnItemClick() {
                        @Override
                        public void onItemClick(BgmMusic bgmMusic) {
                            AgoraMusicPlayer.getInstance().preloadMusic(bgmMusic.getSongCode());
                            mCurrentBmgIndex = mBgmMusicList.indexOf(bgmMusic);
                        }
                    });
                    mBgmAdapter.setCurrentPosition(mCurrentBmgIndex);
                }

                mBgmSelectDialog.show();
            });
            compositeDisposable.add(disposable);


            disposable = RxView.clicks(binding.btnSwitchScene).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (null == mSceneSelectDialog) {
                    mSceneAdapter = new UnitySceneRecyclerAdapter(getApplicationContext(), mSceneInfoList);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mSceneSelectDialog = new BottomDialog(this)
                            .initRecyclerView(layoutManager, mSceneAdapter)
                            .setTitle(this.getResources().getString(R.string.scene_title));
                    mSceneAdapter.setOnItemClick(new UnitySceneRecyclerAdapter.OnItemClick() {
                        @Override
                        public void onItemClick(UnitySceneInfo sceneInfo) {

                        }
                    });
                }
                mSceneSelectDialog.show();
            });
            compositeDisposable.add(disposable);


            disposable = RxView.clicks(binding.btnAction).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (null == mActionSelectDialog) {
                    mActionAdapter = new ActionRecyclerAdapter(getApplicationContext(), mActionInfoList);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mActionSelectDialog = new BottomDialog(this)
                            .initRecyclerView(layoutManager, mActionAdapter)
                            .setTitle(this.getResources().getString(R.string.action_title));
                    mActionAdapter.setOnItemClick(new ActionRecyclerAdapter.OnItemClick() {
                        @Override
                        public void onItemClick(ActionInfo actionInfo) {
                            if (!TextUtils.isEmpty(actionInfo.getActionData())) {
                                MetaChatContext.getInstance().sendSceneMessage(actionInfo.getActionData());
                            }
                        }
                    });
                }
                mActionSelectDialog.show();
            });
            compositeDisposable.add(disposable);
        }

    }


    private void initUnityView() {
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                mReCreateScene = true;
                maybeCreateScene();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Log.i(TAG, "onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        binding.unity.addView(mTextureView, 0, layoutParams);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mReCreateScene = true;
        maybeCreateScene();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isEnterScene.removeOnPropertyChangedCallback(callback);
    }

    private void createScene(TextureView tv) {
        Log.i(TAG, "createScene");
        resetSceneState();
        resetViewVisibility();
        if (!MetaChatContext.getInstance().createScene(this, KeyCenter.CHANNEL_ID, tv)) {
            Log.e(TAG, "create scene fail");
        } else {
            initAgoraMusicPlayer();
            initUnityScene();
            initActions();
        }
    }

    private void resetViewVisibility() {
        binding.menuLayout.setVisibility(View.GONE);
        binding.viewGroup.setVisibility(View.GONE);
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        runOnUiThread(() -> {
            if (errorCode != 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "EnterSceneFailed %d", errorCode), Toast.LENGTH_LONG).show();
                return;
            }

            isEnterScene.set(true);
        });
        resetSceneState();
        MetaChatContext.getInstance().updatePublishMediaOptions(true, AgoraMusicPlayer.getInstance().getMusicPlayerId());

        if (mBgmMusicList.size() >= 1) {
            AgoraMusicPlayer.getInstance().preloadMusic(mBgmMusicList.get(mCurrentBmgIndex).getSongCode());
        }
    }

    private void initAgoraMusicPlayer() {
        AgoraMusicPlayer.getInstance().init(MetaChatContext.getInstance().getRtcEngine());
        AgoraMusicPlayer.getInstance().setPlayStateCallBack(new AgoraMusicPlayer.AgoraMusicPlayerCallBack() {
            @Override
            public void onPlayStateChange(long songCode, Constants.MediaPlayerState state) {
                Log.i(TAG, "onPlayStateChange songCode=" + songCode + ", state=" + state + ",mCurrentBmgIndex=" + mCurrentBmgIndex);
                for (BgmMusic music : mBgmMusicList) {
                    if (music.getSongCode() == songCode) {
                        music.setState(state);
                    } else {
                        music.setState(Constants.MediaPlayerState.PLAYER_STATE_IDLE);
                    }
                }
                if (null != mBgmSelectDialog) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBgmAdapter.notifyDataSetChanged();
                        }
                    });
                }
                if (Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_COMPLETED == state) {
                    if (mCurrentBmgIndex >= mBgmMusicList.size() - 1) {
                        mCurrentBmgIndex = 0;
                    } else {
                        mCurrentBmgIndex++;
                    }
                    if (null != mBgmAdapter) {
                        mBgmAdapter.setCurrentPosition(mCurrentBmgIndex);
                    }
                    AgoraMusicPlayer.getInstance().preloadMusic(mBgmMusicList.get(mCurrentBmgIndex).getSongCode());
                }
            }

            @Override
            public void onLoadMusicResult(List<Music> list) {
                mBgmMusicList.clear();
                loadMusics(list);
            }
        });
        if (mLocalMusicList.size() > 0) {
            loadMusics(mLocalMusicList);
        } else {
            AgoraMusicPlayer.getInstance().loadMusics();
        }
    }

    private void loadMusics(List<Music> list) {
        BgmMusic bgmMusic;
        long currentPlaySongCode = AgoraMusicPlayer.getInstance().getCurrentPlaySongCode();
        for (Music music : list) {
            bgmMusic = new BgmMusic();
            bgmMusic.setName(music.getName());
            bgmMusic.setSinger(music.getSinger());
            bgmMusic.setSongCode(music.getSongCode());
            if (music.songCode == currentPlaySongCode) {
                bgmMusic.setState(Constants.MediaPlayerState.PLAYER_STATE_PLAYING);
            } else {
                bgmMusic.setState(Constants.MediaPlayerState.PLAYER_STATE_IDLE);
            }
            mBgmMusicList.add(bgmMusic);
        }
    }

    private void initUnityScene() {
        UnitySceneInfo sceneInfo = new UnitySceneInfo();
        sceneInfo.setSceneId(1);
        sceneInfo.setSceneResId(R.drawable.night_club_scene);
        mSceneInfoList.add(sceneInfo);
    }

    private void initActions() {
        List<ActionMenu> actionList = MenuActionUtils.getInstance().getBroadcasterAnchorMotion();
        int index = 0;
        if (null != actionList && actionList.size() > 0) {
            ActionInfo actionInfo;
            for (ActionMenu menu : actionList) {
                actionInfo = new ActionInfo();
                actionInfo.setActionName(menu.getMenuName());
                actionInfo.setActionData(menu.getMenuData());
                actionInfo.setActionResId(Utils.getResId("anchor_motion" + index++, getApplicationContext()));
                mActionInfoList.add(actionInfo);
            }
        }
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
        runOnUiThread(() -> {
            isEnterScene.set(false);
        });
        AgoraMusicPlayer.getInstance().stop();
    }

    @Override
    public void onReleasedScene(int status) {
        if (status == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaChatContext.getInstance().destroy();
                    removeInitView();
                    initData();

                    unregister();

                    AgoraMusicPlayer.getInstance().destroyMcc();
                    Intent intent = new Intent(BroadcasterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
        //异步线程回调需在主线程处理
        runOnUiThread(() -> MetaChatContext.getInstance().enterScene());
    }


    @Override
    public void onSceneVideoFrame(VideoFrame videoFrame) {
        if (null == videoFrame) {
            return;
        }
        if (mFrameWidth == -1 && mFrameHeight == -1) {
            mFrameWidth = videoFrame.getBuffer().getWidth();
            mFrameHeight = videoFrame.getBuffer().getHeight();
            // update set video configuration
            MetaChatContext.getInstance().getRtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                    new VideoEncoderConfiguration.VideoDimensions(mFrameWidth, mFrameHeight),
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                    STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE, VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED));
        }
        if (!MetaChatContext.getInstance().pushExternalVideoFrame(videoFrame)) {
            Log.e(TAG, "pushExternalVideoFrame fail");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (MetaChatContext.getInstance().isInScene()) {
            AgoraMusicPlayer.getInstance().resume();
        }
        maybeCreateScene();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (MetaChatContext.getInstance().isInScene()) {
            AgoraMusicPlayer.getInstance().pause();
        }
    }

    private void maybeCreateScene() {
        Log.i(TAG, "maybeCreateScene,mReCreateScene=" + mReCreateScene + ",mIsFront=" + mIsFront);
        if (mReCreateScene && mIsFront) {
            register();
            createScene(mTextureView);
        }
    }

    private void resetSceneState() {
        mReCreateScene = false;
    }

    @Override
    public void onRecvMessageFromUser(String userId, byte[] message) {
        Log.i(TAG, "onRecvMessageFromUser message:" + new String(message));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaChatContext.getInstance().sendSceneMessage(new String(message));
            }
        });
    }

    @Override
    public void onClick(View v) {
        ActionMenu actionMenu = MenuActionUtils.getInstance().getActionMenusMaps().get(v.getId());
        if (null != actionMenu) {
            if (!actionMenu.isEndMenu()) {
                View view = mLevelLayoutMap.get(mCurrentViewLevel);
                if (null != view) {
                    removeViewByViewId(v.getId());
                }
                mCurrentViewLevel++;
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setId(mViewGroupId++);
                RelativeLayout.LayoutParams linearLayoutLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, Objects.requireNonNull(mLevelLayoutMap.get(mViewIdLevelMap.get(v.getId()))).getId());
                linearLayout.setLayoutParams(linearLayoutLayoutParams);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                addMenuList(actionMenu.getSubMenus(), linearLayout);
                binding.menuLayout.addView(linearLayout);
                mLevelLayoutMap.put(mCurrentViewLevel, linearLayout);
            } else {
                removeViewByViewId(v.getId());
                if (!TextUtils.isEmpty(actionMenu.getMenuData())) {
                    MetaChatContext.getInstance().sendSceneMessage(actionMenu.getMenuData());
                }
            }
        }
    }

    private void removeViewByViewId(int viewId) {
        int viewLevel = 1;
        if (-1 != viewId) {
            viewLevel = Objects.isNull(mViewIdLevelMap.get(viewId)) ? 1 : mViewIdLevelMap.get(viewId);
        }
        for (Map.Entry<Integer, View> integerViewEntry : mLevelLayoutMap.entrySet()) {
            if (integerViewEntry.getKey() > viewLevel) {
                binding.menuLayout.removeView(integerViewEntry.getValue());
            }
        }
    }

    private void removeInitView() {
        if (null != mViewIdLevelMap) {
            for (Map.Entry<Integer, Integer> integerViewEntry : mViewIdLevelMap.entrySet()) {
                if (integerViewEntry.getValue() == 1) {
                    binding.menuRootViews.removeView(mViewIdViewMap.get(integerViewEntry.getKey()));
                }
            }
        }

    }

    private void addMenuList(List<ActionMenu> menuList, ViewGroup parentView) {
        Button button;
        int index = 0;
        for (ActionMenu menu : menuList) {
            index++;
            button = new Button(this);
            button.setId(menu.getId());
            button.setText(menu.getMenuName());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (1 == index) {
                if (1 == mCurrentViewLevel) {
                    layoutParams.setMargins(0, Utils.dip2px(getApplicationContext(), 20), 0, 0);
                }
            } else {
                layoutParams.setMargins(0, Utils.dip2px(getApplicationContext(), 20), 0, 0);
            }
            button.setLayoutParams(layoutParams);
            button.setOnClickListener(this);
            parentView.addView(button);
            mViewIdViewMap.put(menu.getId(), button);
            mViewIdLevelMap.put(menu.getId(), mCurrentViewLevel);
        }
    }
}
