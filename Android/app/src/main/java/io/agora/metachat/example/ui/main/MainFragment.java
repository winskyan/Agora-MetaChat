package io.agora.metachat.example.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import coil.ImageLoaders;
import coil.request.ImageRequest;
import io.agora.metachat.example.ui.view.CustomDialog;
import io.agora.metachat.example.ui.base.BaseFragment;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.MainFragmentBinding;
import io.agora.metachat.example.ui.activity.AudienceActivity;
import io.agora.metachat.example.ui.activity.BroadcasterActivity;
import io.agora.metachat.example.ui.activity.GameActivity;
import io.agora.metachat.example.utils.MenuActionUtils;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.rtc2.Constants;
import io.reactivex.disposables.Disposable;

public class MainFragment extends BaseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private MainViewModel mViewModel;
    private MainFragmentBinding binding;
    private int downloadProgress;

    private MaterialDialog progressDialog;
    private MaterialDialog DownloadingChooserDialog;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return binding.getRoot();
    }

    @Override
    protected void initContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, boolean attachToParent) {
        super.initContentView(inflater, container, attachToParent);
        binding = MainFragmentBinding.inflate(inflater, container, attachToParent);
    }

    @Override
    protected void initView() {
        super.initView();

        binding.tvGender.setVisibility(View.INVISIBLE);
        binding.linearFemale.setVisibility(View.INVISIBLE);
        binding.linearMale.setVisibility(View.INVISIBLE);

        binding.etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setNickname(s.toString());
            }
        });
        binding.linearMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.linearMale.setBackgroundResource(R.drawable.main_select_button_bg_checked);
                binding.linearFemale.setBackgroundResource(R.drawable.main_select_button_bg);
                mViewModel.setSex(MetaChatConstants.GENDER_MAN);
            }
        });

        binding.linearFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.linearMale.setBackgroundResource(R.drawable.main_select_button_bg);
                binding.linearFemale.setBackgroundResource(R.drawable.main_select_button_bg_checked);
                mViewModel.setSex(MetaChatConstants.GENDER_WOMEN);
            }
        });

        binding.linearRoleHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.linearRoleHost.setBackgroundResource(R.drawable.main_select_button_bg_checked);
                binding.linearRoleAudience.setBackgroundResource(R.drawable.main_select_button_bg);
                MetaChatContext.getInstance().setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
            }
        });

        binding.linearRoleAudience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.linearRoleHost.setBackgroundResource(R.drawable.main_select_button_bg);
                binding.linearRoleAudience.setBackgroundResource(R.drawable.main_select_button_bg_checked);
                MetaChatContext.getInstance().setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
            }
        });
    }

    @Override
    protected void initClickEvent() {
        super.initClickEvent();
        //防止多次频繁点击异常处理
        Disposable disposable = RxView.clicks(binding.enter).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (TextUtils.isEmpty(Objects.isNull(binding.etNickname.getText()) ? "" : binding.etNickname.getText().toString())) {
                Toast.makeText(requireActivity(), "please input nickname!", Toast.LENGTH_LONG).show();
            } else if (-1 == MetaChatContext.getInstance().getClientRoleType()) {
                Toast.makeText(requireActivity(), "please select role type!", Toast.LENGTH_LONG).show();
            } else {
                MenuActionUtils.getInstance().initMenus(requireContext());
                MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_IDOL);
                MetaChatContext.getInstance().initRoleInfo(binding.etNickname.getText().toString(),
                        mViewModel.getSex().getValue() == null ? MetaChatConstants.GENDER_MAN : mViewModel.getSex().getValue());
                MetaChatContext.getInstance().getRoleInfo().setAvatar(mViewModel.getAvatar().getValue());
                mViewModel.getScenes();
            }
        });
        compositeDisposable.add(disposable);

        disposable = RxView.clicks(binding.avatar).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            CustomDialog.showAvatarPicker(requireContext(), charSequence -> {
                mViewModel.setAvatar(charSequence.toString());
                return null;
            }, null, null);
        });

        compositeDisposable.add(disposable);
    }

    @Override
    protected void initData() {
        super.initData();
        downloadProgress = -1;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
    }


    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        LifecycleOwner owner = getViewLifecycleOwner();
        Context context = requireContext();
        mViewModel.getAvatar().observe(owner, charSequence -> {
            ImageRequest request = new ImageRequest.Builder(context)
                    .data(charSequence)
                    .target(binding.avatar)
                    .build();
            ImageLoaders.create(context)
                    .enqueue(request);
        });
        mViewModel.getNickname().observe(owner, charSequence -> {
            if (charSequence.length() < 2 || charSequence.length() > 12) {
                binding.tvNicknameIllegal.setVisibility(View.VISIBLE);
            } else {
                binding.tvNicknameIllegal.setVisibility(View.GONE);
            }
        });

        mViewModel.getSceneList().observe(owner, metachatSceneInfos -> {
            // TODO choose one
            if (metachatSceneInfos.size() > 0) {
                for (int a = 0; a < metachatSceneInfos.size(); a++) {
                    if (metachatSceneInfos.get(a).getSceneId() == MetaChatContext.getInstance().getSceneId()) {
                        mViewModel.prepareScene(metachatSceneInfos.get(a));
                        break;
                    }
                }
            }
        });
        mViewModel.getSelectScene().observe(owner, sceneInfo -> {
            if (!MetaChatContext.getInstance().isInitMetachat()) {
                return;
            }

            if (-1 != downloadProgress) {
                downloadProgress = -1;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            Intent intent = new Intent(context, GameActivity.class);
            if (MetaChatConstants.SCENE_IDOL == MetaChatContext.getInstance().getCurrentScene()) {
                if (MetaChatContext.getInstance().isBroadcaster()) {
                    intent = new Intent(context, BroadcasterActivity.class);
                } else {
                    intent = new Intent(context, AudienceActivity.class);
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        mViewModel.getRequestDownloading().observe(owner, aBoolean -> {
            if (!MetaChatContext.getInstance().isInitMetachat()) {
                return;
            }
            if (aBoolean) {
                DownloadingChooserDialog = CustomDialog.showDownloadingChooser(context, materialDialog -> {
                    mViewModel.downloadScene(MetaChatContext.getInstance().getSceneInfo());

                    return null;
                }, null);
            }
        });
        mViewModel.getDownloadingProgress().observe(owner, integer -> {
            if (!MetaChatContext.getInstance().isInitMetachat()) {
                return;
            }
            if (integer >= 0) {
                downloadProgress = integer;
            }
            if (progressDialog == null) {
                progressDialog = CustomDialog.showDownloadingProgress(context, materialDialog -> {
                    downloadProgress = -1;
                    mViewModel.cancelDownloadScene(MetaChatContext.getInstance().getSceneInfo());
                    return null;
                });
            } else if (integer < 0) {
                if (mIsFront) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                return;
            }

            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }

            ConstraintLayout constraintLayout = CustomDialog.getCustomView(progressDialog);
            ProgressBar progressBar = constraintLayout.findViewById(R.id.progressBar);
            TextView textView = constraintLayout.findViewById(R.id.textView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(integer, true);
            } else {
                progressBar.setProgress(integer);
            }
            textView.setText(String.format(Locale.getDefault(), "%d%%", integer));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!MetaChatContext.getInstance().isInitMetachat() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (!MetaChatContext.getInstance().isInitMetachat() && DownloadingChooserDialog != null && DownloadingChooserDialog.isShowing()) {
            DownloadingChooserDialog.dismiss();
            DownloadingChooserDialog = null;
        }


        if (MetaChatConstants.SCENE_NONE != MetaChatContext.getInstance().getNextScene()) {
            enableUI(false);
            MetaChatContext.getInstance().setCurrentScene(MetaChatContext.getInstance().getNextScene());
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_NONE);
            mViewModel.getScenes();
        } else {
            enableUI(true);
        }
    }

    private void enableUI(boolean enable) {
        binding.linearRoleHost.setEnabled(enable);
        binding.linearRoleAudience.setEnabled(enable);
        binding.etNickname.setEnabled(enable);
        binding.linearMale.setEnabled(enable);
        binding.linearFemale.setEnabled(enable);
        binding.enter.setEnabled(enable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (downloadProgress >= 0) {
            if (!MetaChatContext.getInstance().downloadScene(MetaChatContext.getInstance().getSceneInfo())) {
                Log.e(TAG, "onResume continue download fail");
            }
        }

        if (-1 == MetaChatContext.getInstance().getClientRoleType()) {
            binding.linearRoleHost.setBackgroundResource(R.drawable.main_select_button_bg);
            binding.linearRoleAudience.setBackgroundResource(R.drawable.main_select_button_bg);
            binding.linearRoleHost.setBackgroundResource(R.drawable.main_select_button_bg);
            binding.linearRoleAudience.setBackgroundResource(R.drawable.main_select_button_bg);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (downloadProgress >= 0) {
            if (!MetaChatContext.getInstance().cancelDownloadScene(MetaChatContext.getInstance().getSceneInfo())) {
                Log.e(TAG, "onPause cancel download fail");
            }
        }
    }
}