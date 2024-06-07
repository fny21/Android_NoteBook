package com.example.nootbook;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

class note_list_item{
    int label_id;
    int note_id;
    int type;  // 0 for label, 1 for note
    boolean is_hided;  // only for label, hide or show notes of this label.
    boolean deleted;  // only for notes, whether it has been deleted
    String name;  // label name or note title
    String init_time;  // 创建时间
    String modify_time;  // 最近修改时间

    note_list_item(int type_, boolean is_hided_, boolean deleted_, String name_, int label_id_, int note_id_){
        type = type_;
        is_hided = is_hided_;
        deleted = deleted_;
        name = name_;
        label_id = label_id_;
        note_id = note_id_;

        Date currentDate = new Date();
        init_time = currentDate.toString();
        modify_time = currentDate.toString();
    }
}

public class labelRecycleViewAdapter extends RecyclerView.Adapter<labelRecycleViewAdapter.labelViewHolder> {
    private Context context;
    private List<note_list_item> mList;
    private float dp_to_px_ratio;

    // 定义回调接口
    public interface OnItemClickListener {
        //clicked_item: 0 for nothing(click the background), 1 for hide or show, 2 for label/text name, 3 for edit/add button, 4 for delete
        void onItemClick(int position, int clicked_item, String new_name);
    }

    // 持有回调接口的引用
    private OnItemClickListener mListener;

    // 提供设置回调的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    class labelViewHolder extends RecyclerView.ViewHolder {
        private ImageButton label_show_or_hide;
        private EditText label_name;
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

    public labelRecycleViewAdapter(Context context, List<note_list_item> mList, float dp_to_px_ratio_) {
        this.context=context;
        this.mList = mList;
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
                        mListener.onItemClick(position, 0, null);
                    }
                }
            });

            new_label_view_holder.label_show_or_hide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 1, null);
                    }
                }
            });

            new_label_view_holder.label_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String new_name = new_label_view_holder.label_name.getText().toString();
                        if(new_name.length()==0){
                            new_name = "illegal name";
                        }
                        mListener.onItemClick(position, 2, new_name);
                    }
                }
            });

            new_label_view_holder.label_add_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 3, null);
                    }
                }
            });

            new_label_view_holder.label_delete_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position, 4, null);
                    }
                }
            });
        }
        return new_label_view_holder;
    }

    @Override
    public void onBindViewHolder(@NonNull labelViewHolder holder, int position) {
        note_list_item one_item = this.mList.get(position);
        if(one_item.type==0){
            ConstraintLayout.LayoutParams show_or_hide_button_layout = (ConstraintLayout.LayoutParams) holder.label_show_or_hide.getLayoutParams();
            show_or_hide_button_layout.width = Math.round((float) 15 * dp_to_px_ratio);
            holder.label_show_or_hide.setLayoutParams(show_or_hide_button_layout);
            holder.label_show_or_hide.setVisibility(View.VISIBLE);
            if(!one_item.name.startsWith("Recently Deleted")){
                holder.label_add_note.setVisibility(View.VISIBLE);
                holder.label_add_note.setBackgroundResource(R.drawable.blue_add);
            }
            else{
                holder.label_add_note.setVisibility(View.INVISIBLE);
            }
            holder.label_delete_all.setBackgroundResource(R.drawable.rubbish);
            if(one_item.is_hided){
                holder.label_show_or_hide.setBackgroundResource(R.drawable.arrow_label_hide);
            }
            else{
                holder.label_show_or_hide.setBackgroundResource(R.drawable.arrow_label_show);
            }
        }
        else if (one_item.type==1) {
            ConstraintLayout.LayoutParams show_or_hide_button_layout = (ConstraintLayout.LayoutParams) holder.label_show_or_hide.getLayoutParams();
            show_or_hide_button_layout.width = Math.round((float) 25 * dp_to_px_ratio);
            holder.label_show_or_hide.setLayoutParams(show_or_hide_button_layout);
            holder.label_show_or_hide.setVisibility(View.INVISIBLE);
            holder.label_delete_all.setBackgroundResource(R.drawable.rubbish);
            holder.label_add_note.setVisibility(View.VISIBLE);
            if(one_item.deleted){
                holder.label_add_note.setBackgroundResource(R.drawable.call_back);
            }
            else {
                holder.label_add_note.setBackgroundResource(R.drawable.note_edit);
            }
        }
        else{
            Log.e(String.valueOf(this), "unexpected string: string should begin with 'label' or 'note'");
        }
        holder.label_name.setText(one_item.name);
        /*
        holder.label_name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.label_name.setMarqueeRepeatLimit(-1); // -1 表示无限重复
        holder.label_name.setFocusable(true);
        holder.label_name.setFocusableInTouchMode(true);
        holder.label_name.setSelected(true);
        */
    }

    public void addData(int position){  //label_or_note: 0 for label, 1 for note
        notifyItemInserted(position);
    }

    public void deleteData(int position){
        notifyItemRemoved(position);
    }

    public void changeData(int position){
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return this.mList.size();
    }
}
