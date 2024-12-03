package com.example.regreen.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter<T> extends RecyclerView.Adapter<MyAdapter.MyViewHolder<T>> {

    private List<T> items;
    private int layoutId;
    private Binder<T> binder;

    public MyAdapter(List<T> items, int layoutId, Binder<T> binder) {
        this.items = items;
        this.layoutId = layoutId;
        this.binder = binder;
    }

    @NonNull
    @Override
    public MyViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MyViewHolder<>(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder<T> holder, int position) {
        binder.bind(holder.itemView, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MyViewHolder<T> extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface Binder<T> {
        void bind(View itemView, T item);
    }

    public void setData(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void updateData(List<T> newData) {
        this.items.clear();
        this.items.addAll(newData);
        notifyDataSetChanged();
    }
}
