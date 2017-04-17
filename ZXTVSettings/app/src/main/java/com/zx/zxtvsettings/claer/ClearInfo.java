package com.zx.zxtvsettings.claer;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: ShaudXiao
 * Date: 2016-08-24
 * Time: 14:22
 * Company: zx
 * Description:
 * FIXME
 */

public class ClearInfo implements Parcelable {

    private Bitmap icon;
    private String name;
    private long size;
    private String path;
    private boolean selected;
    private int state = -1;
    private String version;
    private String packageName;

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public static final Creator<ClearInfo> CREATOR = new Creator<ClearInfo>() {

        @Override
        public ClearInfo createFromParcel(Parcel source) {
            ClearInfo clearInfo = new ClearInfo();
            clearInfo.icon = source.readParcelable(Bitmap.class.getClassLoader());
            clearInfo.name = source.readString();
            clearInfo.packageName = source.readString();

            clearInfo.path = source.readString();
            clearInfo.selected = (Boolean) source.readValue(Boolean.class.getClassLoader());
            clearInfo.size = source.readLong();
            clearInfo.state = source.readInt();
            clearInfo.version = source.readString();

            return clearInfo;
        }

        @Override
        public ClearInfo[] newArray(int var1){
            return  null;
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeParcelable(icon, flag);
        parcel.writeString(name);
        parcel.writeString(packageName);
        parcel.writeString(path);
        parcel.writeValue(selected);
        parcel.writeLong(size);
        parcel.writeInt(state);
        parcel.writeString(version);
    }
}
