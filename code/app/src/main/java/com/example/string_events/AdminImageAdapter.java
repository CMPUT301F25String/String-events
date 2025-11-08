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

/**
 * RecyclerView adapter for displaying selectable event images in the admin UI.
 * <p>Supports single-selection highlighting and reports the selected image via
 * {@link OnImageSelectedListener}.</p>
 *
 * @since 1.0
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {
    /** Host context used for inflating item views. */
    private final Context context;
    /** Backing list of images to render. */
    private final ArrayList<EventImage> imageList;
    /** Callback invoked when an item becomes selected. */
    private final OnImageSelectedListener listener;
    /** Currently selected item position, or -1 when none. */
    private int selectedPosition = -1;

    /**
     * Listener for image selection events.
     */
    public interface OnImageSelectedListener {
        /**
         * Called when an image item is selected.
         *
         * @param image the selected {@link EventImage}
         */
        void onImageSelected(EventImage image);
    }

    /**
     * Lightweight model for an image displayed in the grid/list.
     */
    public static class EventImage {
        /** Firestore/storage identifier (if applicable). */
        String id;
        /** Human-readable title. */
        String title;
        /** Public image URL (e.g., Firebase Storage download URL). */
        String imageUrl;

        /**
         * Creates a new {@code EventImage}.
         *
         * @param id       unique id (may be a document id)
         * @param title    display title
         * @param imageUrl public URL for loading the bitmap
         */
        public EventImage(String id, String title, String imageUrl) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
        }
    }

    /**
     * Constructs the adapter.
     *
     * @param context   host context
     * @param imageList data set to display
     * @param listener  selection callback
     */
    public AdminImageAdapter(Context context, ArrayList<EventImage> imageList, OnImageSelectedListener listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    /** {@inheritDoc} */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_event_image, parent, false);
        return new ImageViewHolder(v);
    }

    /** {@inheritDoc} */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        EventImage img = imageList.get(position);
        holder.title.setText(img.title);

        // Load image from URL on a background thread; fall back to a system icon on failure.
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

        // Show overlay for the selected position.
        holder.overlay.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(position);
            listener.onImageSelected(img);
        });
    }

    /** {@inheritDoc} */
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    /**
     * View holder for an image item.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        /** Thumbnail view. */
        ImageView imageView;
        /** Title text. */
        TextView title;
        /** Selection overlay view. */
        View overlay;

        /**
         * Binds child views from the item layout.
         *
         * @param itemView inflated row view
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_event);
            title = itemView.findViewById(R.id.tv_event_title);
            overlay = itemView.findViewById(R.id.overlay_selected);
        }
    }
}
