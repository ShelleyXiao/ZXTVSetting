package com.zx.zxtvsettings.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.FileUtil;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.claer.ClearInfo;

import java.util.List;


/**
 * User: ShaudXiao
 * Date: 2016-08-26
 * Time: 10:27
 * Company: zx
 * Description:
 * FIXME
 */

public class ClearDetailAdapter extends BaseAdapter {


    private List<ClearInfo> infoList;
    private Context mContext;
    private LayoutInflater mInflater;
    private itemCheckedChangedListener mChangedListener;

    public ClearDetailAdapter(Context _context, List<ClearInfo> infos, itemCheckedChangedListener listener) {
        this.mContext = _context;
        this.infoList = infos;
        mInflater = LayoutInflater.from(mContext);
        this.mChangedListener = listener;
    }

    @Override
    public int getCount() {
        if (infoList != null && infoList.size() > 0) {
            return infoList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (infoList != null && infoList.size() > 0) {
            return infoList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.clear_detail_list_item_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ClearInfo info = infoList.get(position);
        if (info == null) {
            return null;
        }
        if (holder.mDetialItemIcon != null) {
            if (info.getIcon() != null) {
                holder.mDetialItemIcon.setImageBitmap(info.getIcon());
            } else {
                holder.mDetialItemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.file_icon_default));
            }
        }
        if (holder.mDetialItemName != null) {
            if (!TextUtils.isEmpty(info.getName())) {
                holder.mDetialItemName.setText(info.getName());
            } else {
                holder.mDetialItemName.setText(mContext.getString(R.string.unknown));
            }
        }
        if (holder.mDetialItemState != null) {
            holder.mDetialItemState.setText(FileUtil.convertStorage(info.getSize()));
        }

        if (holder.mDetialItemSelected != null) {

            holder.mDetialItemSelected.setFocusable(false);

            holder.mDetialItemSelected.setChecked(info.isSelected());
//            holder.mDetialItemSelected.setOnClickListener(new CheckBoxClick(holder.mDetialItemSelected, position));
            holder.mDetialItemSelected.setOnCheckedChangeListener(new CheckBoxCheckedChange(holder.mDetialItemSelected, position));

//            if (info.getState() != Constant.CACHE_STATE) {
//                holder.mDetialItemSelected.setChecked(info.isSelected());
//                holder.mDetialItemSelected.setOnClickListener(new CheckBoxClick(holder.mDetialItemSelected, position));
//            } else {
//                holder.mDetialItemSelected.setVisibility(View.GONE);
//            }
        }

        if (holder.mDetialItemVersion != null) {
            if (!TextUtils.isEmpty(info.getVersion())) {
                holder.mDetialItemVersion.setVisibility(View.VISIBLE);
                holder.mDetialItemVersion.setText(info.getVersion());
            } else {
                holder.mDetialItemVersion.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private class CheckBoxCheckedChange implements CompoundButton.OnCheckedChangeListener {

        private CheckBox cbox;
        private int position = -1;

        public CheckBoxCheckedChange(CheckBox cb, int pos) {
            this.cbox = cb;
            this.position = pos;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            infoList.get(position).setSelected(isChecked);
            Logger.getLogger().d(" " + infoList.get(position).getName());
            if(null != mChangedListener) {
                mChangedListener.itemCheckedChanged(position);
            }
        }
    }

    private class CheckBoxClick implements View.OnClickListener {
        private CheckBox cbox;
        private int position = -1;

        public CheckBoxClick(CheckBox cb, int pos) {
            this.cbox = cb;
            this.position = pos;
        }

        @Override
        public void onClick(View v) {
            boolean isChecked = cbox.isChecked();
            infoList.get(position).setSelected(isChecked);
            if (isChecked) {
                boolean isAllChecked = true;
                int size = infoList.size();
                for (int i = 0; i < size; i++) {
                    isAllChecked &= infoList.get(i).isSelected();
                }
//                ((ClearDetialActivity) mContext).selectAll.setChecked(isAllChecked);
            } else {
//                ((ClearDetialActivity) mContext).selectAll.setChecked(false);
            }

        }
    }

    static class ViewHolder {
        ImageView mDetialItemIcon;
        TextView mDetialItemName;
        TextView mDetialItemState;
        TextView mDetialItemSize;
        TextView mDetialItemVersion;
        CheckBox mDetialItemSelected;
        LinearLayout mDetialItemLayout;

        public ViewHolder(View view) {
            mDetialItemIcon = (ImageView) view.findViewById(R.id.detial_item_icon);
            mDetialItemName = (TextView) view.findViewById(R.id.detial_item_name);
            mDetialItemState = (TextView) view.findViewById(R.id.detial_item_state);
            mDetialItemSize = (TextView) view.findViewById(R.id.detial_item_size);
            mDetialItemVersion = (TextView) view.findViewById(R.id.detial_item_version);
            mDetialItemSelected = (CheckBox) view.findViewById(R.id.detial_item_selected);
            mDetialItemLayout = (LinearLayout) view.findViewById(R.id.detial_item_layout);
        }
    }

    public interface itemCheckedChangedListener {

         void itemCheckedChanged(int position);
    }
}
