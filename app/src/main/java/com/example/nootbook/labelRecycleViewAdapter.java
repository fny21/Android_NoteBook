package com.example.nootbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class labelRecycleViewAdapter extends RecyclerView.Adapter<labelRecycleViewAdapter.labelViewHolder> {

    private Context context;
    private List<String> mList;

    class labelViewHolder extends RecyclerView.ViewHolder {
        private TextView label_name;
        private List<TextView> Notes;

        public labelViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            label_name = itemView.findViewById(R.id.one_label_name);
        }
    }

    public labelRecycleViewAdapter(Context context, List<String> mList) {
        this.context=context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public labelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(this.context).inflate(R.layout.label_layout, parent, false);
        return new labelViewHolder(itemView, this.context);
    }

    @Override
    public void onBindViewHolder(@NonNull labelViewHolder holder, int position) {
        String one_label = this.mList.get(position);
        holder.label_name.setText(one_label);
    }

    @Override
    public int getItemCount() {
        return this.mList.size();
    }
}
