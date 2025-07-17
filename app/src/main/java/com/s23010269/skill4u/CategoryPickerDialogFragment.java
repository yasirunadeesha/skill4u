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
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_category, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_categories);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns

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

        CategoryAdapter adapter = new CategoryAdapter(categories, categoryName -> {
            listener.onCategorySelected(categoryName);
            dismiss();
        });

        rv.setAdapter(adapter);

        // Close button
        view.findViewById(R.id.btn_close_categories).setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // Data holder class for each category
    public static class CategoryItem {
        public final String name;
        public final int iconRes;

        public CategoryItem(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }
}
