package io.agora.metachat.example.models.idol;

import androidx.annotation.NonNull;

public class UnitySceneInfo {
    private int sceneId;

    private int sceneResId;

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public int getSceneResId() {
        return sceneResId;
    }

    public void setSceneResId(int sceneResId) {
        this.sceneResId = sceneResId;
    }

    @NonNull
    @Override
    public String toString() {
        return "SceneInfo{" +
                "sceneId=" + sceneId +
                ", sceneResId=" + sceneResId +
                '}';
    }
}
