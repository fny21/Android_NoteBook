package com.example.nootbook;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class labelRecycleViewAdapter extends RecyclerView.Adapter<labelRecycleViewAdapter.labelViewHolder> {

    private Context context;
    private List<String> mList;
    private String label_prefix;
    private String label_hide_prefix;
    private String label_show_prefix;
    private String note_prefix;
    private float dp_to_px_ratio;

    // 定义回调接口
    public interface OnItemClickListener {
        //clicked_item: 0 for nothing(click the background), 1 for hide or show, 2 for label/text name, 3 for edit/add button, 4 for delete
        void onItemClick(int position, int clicked_item);
    }

    // 持有回调接口的引用
    private OnItemClickListener mListener;

    // 提供设置回调的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    class labelViewHolder extends RecyclerView.ViewHolder {
        private ImageButton label_show_or_hide;
        private TextView label_name;
        private ImageButton label_add_note;
        private ImageButton label_delete_all;

        public labelViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            label_name = itemView.findViewById(R.id.one_label_name);
            label_add_note = itemView.findViewById(R.id.one_label_add_button);
            label_delete_all = itemView.findViewById(R.id.one_label_delete_all);
            label_show_or_hide = itemView.findViewById(R.id.label_hide_or_show);
        }
    }

    public labelRecycleViewAdapter(Context context, List<String> mList, float dp_to_px_ratio_) {
        this.context=context;
        this.mList = mList;
        label_prefix = "Label";
        label_hide_prefix = "Label-hide";
        label_show_prefix = "Label-show";
        note_prefix = "Note";
        dp_to_px_ratio = dp_to_px_ratio_;
    }

    @NonNull
    @Override
    public labelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(this.context).inflate(R.layout.label_layout, parent, false);
        labelViewHolder new_label_view_holder = new labelViewHolder(itemView, this.context);

        // 如果回调接口已设置，可以为ViewHolder中的视图设置点击监听器
        if (mListener != null) {
            new_label_view_holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 0);
                    }
                }
            });

            new_label_view_holder.label_show_or_hide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 1);
                    }
                }
            });

            new_label_view_holder.label_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 2);
                    }
                }
            });

            new_label_view_holder.label_add_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 3);
                    }
                }
            });

            new_label_view_holder.label_delete_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 4);
                    }
                }
            });
        }
        return new_label_view_holder;
    }

    @Override
    public void onBindViewHolder(@NonNull labelViewHolder holder, int position) {
        String one_label = this.mList.get(position);
        if(one_label.startsWith(this.label_prefix)){
            holder.label_add_note.setBackgroundResource(R.drawable.blue_add);
            holder.label_delete_all.setBackgroundResource(R.drawable.rubbish);
            if(one_label.startsWith(this.label_hide_prefix)){
                holder.label_show_or_hide.setBackgroundResource(R.drawable.arrow_label_hide);
            }
            else{
                holder.label_show_or_hide.setBackgroundResource(R.drawable.arrow_label_show);
            }
        }
        else if (one_label.startsWith(this.note_prefix)) {
            ConstraintLayout.LayoutParams show_or_hide_button_layout = (ConstraintLayout.LayoutParams) holder.label_show_or_hide.getLayoutParams();
            show_or_hide_button_layout.width = Math.round((float)25 * dp_to_px_ratio);
            holder.label_show_or_hide.setLayoutParams(show_or_hide_button_layout);
            holder.label_show_or_hide.setVisibility(View.INVISIBLE);
            holder.label_add_note.setBackgroundResource(R.drawable.note_edit);
            holder.label_delete_all.setBackgroundResource(R.drawable.rubbish);
        }
        else{
            Log.e(String.valueOf(this), "unexpected string: string should begin with 'label' or 'note'");
        }
        holder.label_name.setText(one_label.substring(12));
        holder.label_name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.label_name.setMarqueeRepeatLimit(-1); // -1 表示无限重复
        holder.label_name.setFocusable(true);
        holder.label_name.setFocusableInTouchMode(true);
        holder.label_name.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return this.mList.size();
    }
}
