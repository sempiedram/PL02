package com.sempiedram.pl02app;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

class PhotosRecyclerViewAdapter extends RecyclerView.Adapter<PhotosRecyclerViewAdapter.ViewHolder> {

    private List<Bitmap> photosList;
    private ItemSelectedListener listener;

    PhotosRecyclerViewAdapter(List<Bitmap> items, ItemSelectedListener listener) {
        this.photosList = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.photo_item, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.bitmap = photosList.get(i);
        viewHolder.imageView.setImageBitmap(photosList.get(i));
        viewHolder.positionText.setText(Integer.toString(i));

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.itemSelected(viewHolder.bitmap, viewHolder.getAdapterPosition());
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int i) {
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.itemSelected(holder.bitmap, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public TextView positionText;
        public final ImageView imageView;
        public Bitmap bitmap;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imageView = view.findViewById(R.id.photo_view);
            positionText = view.findViewById(R.id.item_position);
        }
    }

    public interface ItemSelectedListener {
        public void itemSelected(Bitmap photo, int index);
    }
}