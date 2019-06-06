package com.sempiedram.pl02app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.BreakIterator;
import java.util.List;

class TextRecyclerViewAdapter extends RecyclerView.Adapter<TextRecyclerViewAdapter.ViewHolder> {

    private List<String> itemsList;
    private ItemSelectedListener listener;

    TextRecyclerViewAdapter(List<String> items, ItemSelectedListener listener) {
        this.itemsList = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int i) {
        String itemText = itemsList.get(i);

        holder.itemText.setText(itemText);
        holder.positionText.setText(Integer.toString(i));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.itemSelected(holder.itemText.getText().toString(), i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView itemText;
        public TextView positionText;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            itemText = view.findViewById(R.id.text_item);
            positionText = view.findViewById(R.id.item_position);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + itemText.getText() + "'";
        }
    }

    public interface ItemSelectedListener {
        public void itemSelected(String itemText, int index);
    }
}