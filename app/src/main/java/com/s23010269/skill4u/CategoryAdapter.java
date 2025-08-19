package com.s23010269.skill4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<CategoryPickerDialogFragment.CategoryItem> categories; // list of categories
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(String categoryName);
    }

    public CategoryAdapter(List<CategoryPickerDialogFragment.CategoryItem> categories,
                           OnItemClickListener listener) {
        this.categories = categories; // initialize categories
        this.listener = listener; // initialize listener
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon; // category icon
        TextView label; // category name

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.iv_category_icon); // find icon view
            label = v.findViewById(R.id.tv_category_name); // find label view
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false); // inflate item layout
        return new ViewHolder(view); // create ViewHolder
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryPickerDialogFragment.CategoryItem item = categories.get(position); // get item
        holder.label.setText(item.name); // set category name
        holder.icon.setImageResource(item.iconRes); // set category icon
        holder.itemView.setOnClickListener(v -> listener.onClick(item.name)); // set click
    }

    @Override
    public int getItemCount() {
        return categories.size(); // return number of items
    }
}
