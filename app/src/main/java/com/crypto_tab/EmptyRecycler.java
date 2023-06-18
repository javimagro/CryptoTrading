package com.crypto_tab;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// EmptyRecycler.java
public class EmptyRecycler extends RecyclerView.Adapter<EmptyHolder> {
    @NonNull
    @Override
    public EmptyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}