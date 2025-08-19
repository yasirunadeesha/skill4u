package com.s23010269.skill4u;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class CategoryPickerDialogFragment extends DialogFragment {

    public interface OnCategorySelectedListener {
        void onCategorySelected(String categoryName);
    }

    private final OnCategorySelectedListener listener;

    public CategoryPickerDialogFragment(OnCategorySelectedListener listener) {
        this.listener = listener;
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert); // dialog style
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_category, container, false); // inflate layout

        RecyclerView rv = view.findViewById(R.id.rv_categories); // find RecyclerView
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 column grid layout

        // create category list
        List<CategoryItem> categories = Arrays.asList(
                new CategoryItem("Art", R.drawable.cat_art),
                new CategoryItem("Meditation", R.drawable.cat_meditation),
                new CategoryItem("Study", R.drawable.cat_study),
                new CategoryItem("Sports", R.drawable.cat_sport),
                new CategoryItem("Social", R.drawable.cat_social),
                new CategoryItem("Finance", R.drawable.cat_finance),
                new CategoryItem("Health", R.drawable.cat_health),
                new CategoryItem("Work", R.drawable.cat_work),
                new CategoryItem("Outdoors", R.drawable.cat_outdoor),
                new CategoryItem("Other", R.drawable.cat_other)
        );

        // set adapter with click callback
        CategoryAdapter adapter = new CategoryAdapter(categories, categoryName -> {
            listener.onCategorySelected(categoryName); // notify listener
            dismiss(); // close dialog
        });

        rv.setAdapter(adapter); // attach adapter

        // Close button listener
        view.findViewById(R.id.btn_close_categories).setOnClickListener(v -> dismiss());

        return view; // return inflated view
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog(); // get dialog
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT, // full width
                    WindowManager.LayoutParams.WRAP_CONTENT // wrap content height
            );
        }
    }

    // Class representing a category item
    public static class CategoryItem {
        public final String name; // category name
        public final int iconRes; // drawable resource for icon

        public CategoryItem(String name, int iconRes) {
            this.name = name; // set name
            this.iconRes = iconRes; // set icon
        }
    }
}
