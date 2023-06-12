package io.agora.metachat.example.utils;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.models.idol.ActionMenu;

public class MenuActionUtils {
    private volatile static MenuActionUtils instance = null;

    private final List<ActionMenu> broadcasterActionMenus;
    private final List<ActionMenu> audienceActionMenus;

    private final Map<Integer, ActionMenu> actionMenusMaps;

    private final Map<String, List<ActionMenu>> broadcasterKeyActionsMap;
    private final Map<String, List<ActionMenu>> audienceKeyActionsMap;

    private int buttonIdIndex;

    public static MenuActionUtils getInstance() {
        if (instance == null) {
            synchronized (MetaChatContext.class) {
                if (instance == null) {
                    instance = new MenuActionUtils();
                }
            }
        }
        return instance;
    }

    private MenuActionUtils() {
        broadcasterActionMenus = new ArrayList<>();
        audienceActionMenus = new ArrayList<>();

        actionMenusMaps = new HashMap<>();

        broadcasterKeyActionsMap = new HashMap<>();
        audienceKeyActionsMap = new HashMap<>();

        buttonIdIndex = 1;
    }

    public void initMenus(Context context) {
        if (MetaChatContext.getInstance().isBroadcaster()) {
            if (broadcasterActionMenus.size() > 0 || broadcasterKeyActionsMap.size() > 0) {
                return;
            }
        } else {
            if (audienceActionMenus.size() > 0 || audienceKeyActionsMap.size() > 0) {
                return;
            }
        }

        JSONObject jsonObject = JSON.parseObject(Utils.getFromAssets(context, "menus.json"));
        JSONArray actionsArray;
        if (MetaChatContext.getInstance().isBroadcaster()) {
            actionsArray = jsonObject.getJSONArray("broadcasterMenu");
        } else {
            actionsArray = jsonObject.getJSONArray("audienceMenu");
        }
        List<ActionMenu> menus = parseSubMenu(actionsArray);
        if (MetaChatContext.getInstance().isBroadcaster()) {
            broadcasterActionMenus.addAll(menus);
        } else {
            audienceActionMenus.addAll(menus);
        }
    }

    private List<ActionMenu> parseSubMenu(JSONArray actionsArray) {
        JSONObject jsonItem;
        ActionMenu actionMenu;
        JSONObject valueMenu;
        List<ActionMenu> actionMenus = new ArrayList<>();
        for (int i = 0; i < actionsArray.size(); i++) {
            jsonItem = actionsArray.getJSONObject(i);
            actionMenu = new ActionMenu();
            if (jsonItem.containsKey("subMenus")) {
                actionMenu.setEndMenu(false);
                actionMenu.setMenuName(jsonItem.getString("menuName"));
                actionMenu.setSubMenus(parseSubMenu(jsonItem.getJSONArray("subMenus")));
                actionMenu.setMenuData(null);
            } else if (jsonItem.containsKey("menuData")) {
                String menuName = jsonItem.getString("menuName");
                actionMenu.setEndMenu(true);
                actionMenu.setMenuName(menuName);
                actionMenu.setSubMenus(null);
                valueMenu = JSON.parseObject(jsonItem.getJSONObject("menuData").getString("value"));
                if (null != valueMenu) {
                    if (!MetaChatContext.getInstance().isBroadcaster()) {
                        valueMenu.put("userId", KeyCenter.RTM_UID);
                    }
                    jsonItem.getJSONObject("menuData").put("value", valueMenu.toJSONString());

                    Map<String, List<ActionMenu>> keyActionsMap;
                    if (MetaChatContext.getInstance().isBroadcaster()) {
                        keyActionsMap = broadcasterKeyActionsMap;
                    } else {
                        keyActionsMap = audienceKeyActionsMap;
                    }
                    String key = jsonItem.getJSONObject("menuData").getString("key");
                    if (!TextUtils.isEmpty(key)) {
                        List<ActionMenu> actionMenusList = keyActionsMap.get(key);
                        if (actionMenusList == null) {
                            actionMenusList = new ArrayList<>();
                            keyActionsMap.put(key, actionMenusList);
                        }
                        actionMenusList.add(actionMenu);
                    }
                }
                actionMenu.setMenuData(jsonItem.getJSONObject("menuData").toJSONString());


            }
            actionMenu.setId(buttonIdIndex);
            actionMenusMaps.put(buttonIdIndex, actionMenu);
            buttonIdIndex++;
            actionMenus.add(actionMenu);

        }
        return actionMenus;
    }


    public List<ActionMenu> getBroadcasterActionMenus() {
        return broadcasterActionMenus;
    }

    public List<ActionMenu> getAudienceActionMenus() {
        return audienceActionMenus;
    }

    public Map<Integer, ActionMenu> getActionMenusMaps() {
        return actionMenusMaps;
    }


    public List<ActionMenu> getBroadcasterChangeViews() {
        return broadcasterKeyActionsMap.get("changeView");
    }

    public List<ActionMenu> getBroadcasterAnchorMotion() {
        return broadcasterKeyActionsMap.get("anchorMotion");
    }

    public List<ActionMenu> getAudienceUserActions() {
        return audienceKeyActionsMap.get("userAction");
    }
}
