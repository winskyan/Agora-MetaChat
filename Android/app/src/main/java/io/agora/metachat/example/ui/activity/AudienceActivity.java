package io.agora.metachat.example.ui.activity;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSONObject;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.agora.metachat.example.BuildConfig;
import io.agora.metachat.example.R;
import io.agora.metachat.example.adapter.idol.ActionRecyclerAdapter;
import io.agora.metachat.example.models.idol.ActionInfo;
import io.agora.metachat.example.models.idol.ActionMenu;
import io.agora.metachat.example.ui.view.BottomDialog;
import io.agora.metachat.example.utils.KeyCenter;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.MetachatUserInfo;
import io.agora.metachat.example.ui.main.MainActivity;
import io.agora.metachat.example.databinding.AudienceActivityBinding;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.ui.base.BaseActivity;
import io.agora.metachat.example.utils.MenuActionUtils;
import io.agora.metachat.example.utils.Utils;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.video.VideoCanvas;
import io.reactivex.disposables.Disposable;

public class AudienceActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = AudienceActivity.class.getSimpleName();
    private AudienceActivityBinding binding;

    private boolean mEnterSceneSuccess;

    private int mCurrentViewLevel;

    // level ==> layout
    private Map<Integer, View> mLevelLayoutMap;

    //view id ===> level
    private Map<Integer, Integer> mViewIdLevelMap;

    //view id ===> view
    private Map<Integer, View> mViewIdViewMap;

    private int mViewGroupId;

    private List<ActionInfo> mActionInfoList;
    private BottomDialog mActionSelectDialog;
    private ActionRecyclerAdapter mActionAdapter;

    private int mLayoutMaxHeight;
    private int mSoftKeyboardHeight;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
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
        initMetachatAndRtc();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        binding = AudienceActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initView() {
        if (BuildConfig.DEBUG_MENUS) {
            mCurrentViewLevel = 1;
            addMenuList(MenuActionUtils.getInstance().getAudienceActionMenus(), binding.menuViews);

            mLevelLayoutMap.put(mCurrentViewLevel, binding.menuViews);
            mViewIdLevelMap.put(binding.menuViews.getId(), mCurrentViewLevel);
        } else {
            initChatLayout();
        }
    }

    private void initChatLayout() {
        binding.layout.post(new Runnable() {
            @Override
            public void run() {
                mLayoutMaxHeight = binding.layout.getHeight();
                binding.layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        AudienceActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);

                        if (r.bottom > mLayoutMaxHeight) {
                            mLayoutMaxHeight = r.bottom;
                        }
                        mSoftKeyboardHeight = mLayoutMaxHeight - r.bottom;

                        //30应该大于基本的底部导航栏高度，可以认为是软键盘弹出状态
                        boolean isKeyboardShowing = mSoftKeyboardHeight > 100;
                        if (isKeyboardShowing) {
                            if (View.VISIBLE != binding.chatMessageLayout.getVisibility()) {
                                binding.chatMessageLayout.setVisibility(View.VISIBLE);
                                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.chatMessageLayout.getLayoutParams();
                                layoutParams.bottomMargin = mSoftKeyboardHeight;
                                binding.chatMessageLayout.setLayoutParams(layoutParams);
                                binding.chatMessageEt.requestFocus();
                                binding.chatMessageTip.clearFocus();
                            }

                        } else {
                            if (View.GONE != binding.chatMessageLayout.getVisibility()) {
                                binding.chatMessageLayout.setVisibility(View.GONE);
                                binding.chatMessageEt.clearFocus();
                            }
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void initData() {
        super.initData();
        mEnterSceneSuccess = false;
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

        if (mActionInfoList == null) {
            mActionInfoList = new ArrayList<>();
        } else {
            mActionInfoList.clear();
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
                resetView();
            });
            compositeDisposable.add(disposable);
        } else {
            disposable = RxView.clicks(binding.menuLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                Utils.hideSoftInput(binding.chatMessageTip);
                // binding.chatMessageLayout.setVisibility(View.GONE);
            });
            compositeDisposable.add(disposable);
            disposable = RxView.clicks(binding.btnExit).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                removeRemoteVideo();
                MetaChatContext.getInstance().resetRoleInfo();
                resetView();
                if (mEnterSceneSuccess) {
                    if (!MetaChatContext.getInstance().leaveScene()) {
                        Log.e(TAG, "leave scene fail");
                    }
                } else {
                    removeInitView();
                    MetaChatContext.getInstance().leaveRtcChannel();
                }
            });
            compositeDisposable.add(disposable);

            disposable = RxView.clicks(binding.btnAction).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (null == mActionSelectDialog) {
                    mActionAdapter = new ActionRecyclerAdapter(getApplicationContext(), mActionInfoList);
                    mActionAdapter.setInitPosition(-1);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mActionSelectDialog = new BottomDialog(this)
                            .initRecyclerView(layoutManager, mActionAdapter)
                            .setTitle(this.getResources().getString(R.string.action_title));
                    mActionAdapter.setOnItemClick(new ActionRecyclerAdapter.OnItemClick() {
                        @Override
                        public void onItemClick(ActionInfo actionInfo) {
                            if (!TextUtils.isEmpty(actionInfo.getActionData())) {
                                MetaChatContext.getInstance().sendMessageToUser(MetaChatContext.getInstance().getBroadcasterUserId(), actionInfo.getActionData());
                            }
                        }
                    });
                }
                mActionSelectDialog.show();
            });
            compositeDisposable.add(disposable);

            disposable = RxView.clicks(binding.chatMessageTip).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {

            });
            compositeDisposable.add(disposable);

            binding.chatMessageEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (EditorInfo.IME_ACTION_SEND == actionId || EditorInfo.IME_ACTION_DONE == actionId) {
                        sendChatMessage();
                        binding.chatMessageEt.setText("");
                        return true;
                    }
                    return false;
                }
            });
        }


    }

    private void sendChatMessage() {
        JSONObject messageJson = new JSONObject();
        messageJson.put("key", "userAction");
        JSONObject valueJson = new JSONObject();
        valueJson.put("userId", KeyCenter.RTM_UID);
        valueJson.put("actionId", "chat");
        valueJson.put("param", binding.chatMessageEt.getText().toString());
        messageJson.put("value", valueJson.toJSONString());
        MetaChatContext.getInstance().sendMessageToUser(MetaChatContext.getInstance().getBroadcasterUserId(), messageJson.toJSONString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        initMetachatAndRtc();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isEnterScene.removeOnPropertyChangedCallback(callback);
    }


    private void resetViewVisibility() {
        binding.menuLayout.setVisibility(View.GONE);
        binding.viewGroup.setVisibility(View.GONE);
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isEnterScene.set(true);
            }
        });
        mEnterSceneSuccess = true;
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
        runOnUiThread(() -> {
            isEnterScene.set(false);
            resetViewVisibility();
        });
        mEnterSceneSuccess = false;
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
                }
            });
            unregister();

            Intent intent = new Intent(AudienceActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
        if (errorCode == 0) {
            MetaChatContext.getInstance().enterScene();
        }
    }

    private void initMetachatAndRtc() {
        if (MetaChatContext.getInstance().initialize(this.getApplicationContext())) {
            register();
            MetaChatContext.getInstance().joinChannel(Constants.CLIENT_ROLE_AUDIENCE);
        } else {
            Log.i(TAG, "initMetachatAndRtc fail");
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        MetaChatContext.getInstance().prepareScene(null, null, new MetachatUserInfo() {{
            mUserId = KeyCenter.RTM_UID;
            mUserName = MetaChatContext.getInstance().getRoleInfo().getName() == null ? mUserId : MetaChatContext.getInstance().getRoleInfo().getName();
            mUserIconUrl = MetaChatContext.getInstance().getRoleInfo().getAvatar() == null ? "https://accpic.sd-rtn.com/pic/test/png/2.png" : MetaChatContext.getInstance().getRoleInfo().getAvatar();
        }});
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        if (!mEnterSceneSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaChatContext.getInstance().destroy();

                    unregister();
                    Intent intent = new Intent(AudienceActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }
    }

    public void removeRemoteVideo() {
        try {
            MetaChatContext.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, Integer.parseInt(MetaChatContext.getInstance().getBroadcasterUserId())));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "removeRemoteVideo fail");
        }
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaChatContext.getInstance().setBroadcasterUserId(String.valueOf(uid));
                SurfaceView surfaceView = new SurfaceView(getApplicationContext());
                surfaceView.setZOrderMediaOverlay(true);

                if (binding.unity.getChildCount() > 0) {
                    binding.unity.removeAllViews();
                }

                binding.unity.addView(surfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                int ret = -1;
                try {
                    ret = MetaChatContext.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, Integer.parseInt(MetaChatContext.getInstance().getBroadcasterUserId())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "onUserJoined setupRemoteVideo ret=" + ret);

                if (!MetaChatContext.getInstance().createScene(AudienceActivity.this, KeyCenter.CHANNEL_ID, null)) {
                    Log.e(TAG, "create scene fail");
                } else {
                    initActions();
                }
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
                    MetaChatContext.getInstance().sendMessageToUser(MetaChatContext.getInstance().getBroadcasterUserId(), actionMenu.getMenuData());
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
                    binding.menuViews.removeView(mViewIdViewMap.get(integerViewEntry.getKey()));
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

    private void resetView() {
        removeViewByViewId(-1);
        Utils.hideSoftInput(binding.chatMessageEt);
    }


    private void initActions() {
        List<ActionMenu> actionList = MenuActionUtils.getInstance().getAudienceUserActions();
        if (null != actionList && actionList.size() > 0) {
            ActionInfo actionInfo;
            int index = 0;
            for (ActionMenu menu : actionList) {
                actionInfo = new ActionInfo();
                actionInfo.setActionName(menu.getMenuName());
                actionInfo.setActionData(menu.getMenuData());
                actionInfo.setActionResId(Utils.getResId("user_action" + index++, getApplicationContext()));
                mActionInfoList.add(actionInfo);
            }
        }
    }

}
