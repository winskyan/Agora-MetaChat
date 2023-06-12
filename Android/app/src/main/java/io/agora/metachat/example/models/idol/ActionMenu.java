package io.agora.metachat.example.models.idol;

import androidx.annotation.NonNull;

import java.util.List;

public class ActionMenu {
    private int id;
    private String menuName;
    private List<ActionMenu> subMenus;
    private boolean isEndMenu;

    private String menuData;

    public int getId() {
        return id;
    }

    public String getMenuName() {
        return menuName;
    }

    public List<ActionMenu> getSubMenus() {
        return subMenus;
    }

    public boolean isEndMenu() {
        return isEndMenu;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public void setSubMenus(List<ActionMenu> subMenus) {
        this.subMenus = subMenus;
    }

    public void setEndMenu(boolean endMenu) {
        isEndMenu = endMenu;
    }


    public void setMenuData(String menuData) {
        this.menuData = menuData;
    }

    public String getMenuData() {
        return menuData;
    }

    @NonNull
    @Override
    public String toString() {
        return "ActionMenu{" +
                "id=" + id +
                ", menuName='" + menuName + '\'' +
                ", subMenus=" + subMenus +
                ", isEndMenu=" + isEndMenu +
                ", menuData='" + menuData + '\'' +
                '}';
    }
}
