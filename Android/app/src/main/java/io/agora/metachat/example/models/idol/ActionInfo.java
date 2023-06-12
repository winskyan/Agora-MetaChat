package io.agora.metachat.example.models.idol;

import androidx.annotation.NonNull;

public class ActionInfo {
    private String actionName;
    private String actionData;
    private int actionResId;

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public int getActionResId() {
        return actionResId;
    }

    public void setActionResId(int actionResId) {
        this.actionResId = actionResId;
    }

    @NonNull
    @Override
    public String toString() {
        return "IdolActionInfo{" +
                "actionName='" + actionName + '\'' +
                ", actionData='" + actionData + '\'' +
                ", actionResId=" + actionResId +
                '}';
    }
}
