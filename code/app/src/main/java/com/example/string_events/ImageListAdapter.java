package com.example.string_events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

public class ImageListAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> imageResIds;
    private int selectedPosition = -1; // 记录选中项，供删除用

    public ImageListAdapter(Context context, List<Integer> imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @Override
    public int getCount() {
        return imageResIds.size();
    }

    @Override
    public Integer getItem(int position) {
        return imageResIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int pos) {
        selectedPosition = pos;
        notifyDataSetChanged();
    }

    public void removeSelected() {
        if (selectedPosition >= 0 && selectedPosition < imageResIds.size()) {
            imageResIds.remove(selectedPosition);
            selectedPosition = -1;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder h;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image_row, parent, false);
            h = new Holder(convertView);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }

        h.img.setImageResource(imageResIds.get(position));

        // 简单的选中态（可按需美化）
        convertView.setAlpha(position == selectedPosition ? 0.7f : 1f);

        return convertView;
    }

    static class Holder {
        final ImageView img;
        Holder(View v) { img = v.findViewById(R.id.img_event); }
    }
}
