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

    private final List<CategoryPickerDialogFragment.CategoryItem> categories;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(String categoryName);
    }

    public CategoryAdapter(List<CategoryPickerDialogFragment.CategoryItem> categories,
                           OnItemClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.iv_category_icon);
            label = v.findViewById(R.id.tv_category_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryPickerDialogFragment.CategoryItem item = categories.get(position);
        holder.label.setText(item.name);
        holder.icon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> listener.onClick(item.name));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
