package com.example.string_events;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {
    private final Context context;
    private final ArrayList<EventImage> imageList;
    private final OnImageSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnImageSelectedListener {
        void onImageSelected(EventImage image);
    }

    public static class EventImage {
        String id;
        String title;
        String imageUrl;

        public EventImage(String id, String title, String imageUrl) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
        }
    }

    public AdminImageAdapter(Context context, ArrayList<EventImage> imageList, OnImageSelectedListener listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_event_image, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        EventImage img = imageList.get(position);
        holder.title.setText(img.title);

        // load image from Firebase Storage URL manually
        if (img.imageUrl != null && !img.imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(img.imageUrl);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    java.io.InputStream input = connection.getInputStream();
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);

                    holder.imageView.post(() -> holder.imageView.setImageBitmap(bitmap));
                } catch (Exception e) {
                    holder.imageView.post(() ->
                            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // show overlay when selected
        holder.overlay.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(position);
            listener.onImageSelected(img);
        });
    }



    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        View overlay;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_event);
            title = itemView.findViewById(R.id.tv_event_title);
            overlay = itemView.findViewById(R.id.overlay_selected);
        }
    }
}
