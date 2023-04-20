package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.agora.metachat.example.utils.MetaChatConstants;

public class RoleInfo {
    //名字
    private String name;
    //性别
    private int gender;
    //avatar
    private String avatarUrl;

    private String avatarType;

    private Map<Integer, Integer> dressResourceMap;

    private Map<String, Integer> faceParameterResourceMap;

    public RoleInfo() {
        name = "";
        gender = -1;
        avatarType = MetaChatConstants.AVATAR_TYPE_BOY;
        dressResourceMap = new HashMap<>();
        faceParameterResourceMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarType() {
        return avatarType;
    }

    public void setAvatarType(String avatarType) {
        this.avatarType = avatarType;
    }

    public void updateDressResource(int type, int resId) {
        dressResourceMap.put(type, resId);
    }

    public Map<Integer, Integer> getDressResourceMap() {
        return dressResourceMap;
    }

    public void updateFaceParameter(String key, int value) {
        faceParameterResourceMap.put(key, value);
    }

    public Map<String, Integer> getFaceParameterResourceMap() {
        return faceParameterResourceMap;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoleInfo{" +
                "name='" + name + '\'' +
                ", gender=" + gender +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", avatarType='" + avatarType + '\'' +
                ", dressResourceMap=" + dressResourceMap +
                ", faceParameterResourceMap=" + faceParameterResourceMap +
                '}';
    }
}
