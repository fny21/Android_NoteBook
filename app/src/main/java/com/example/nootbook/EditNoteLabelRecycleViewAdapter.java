package com.example.nootbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditNoteLabelRecycleViewAdapter extends RecyclerView.Adapter<EditNoteLabelRecycleViewAdapter.labelViewHolder> {

    public class edit_note_item{
        int position;
        int type;

        String edit_text_string;
        int edit_text_line_num;

        String image_path;
        Bitmap image_bitmap;
        boolean image_show;

        boolean image_edit_show;

        edit_note_item(int type_){  // 0 text, 1 image, 2 audio, 3 video
            type = type_;
        }

        edit_note_item(int position_, String edit_text_string_, String image_path_){
            position = position_;
            edit_text_string = edit_text_string_;
            image_path = image_path_;
        }
    }

    public static class on_item_click_listener_result{
        String image_path;
        Bitmap image_bitmap;

        public on_item_click_listener_result(){
            image_path = null;
            image_bitmap = null;
        }
    }

    private Context context;

    private List<edit_note_item> item_list;
    private float dp_to_px_ratio;

    private int selected_position;
    private int cursor_position_in_text;

    // 定义回调接口
    public interface OnItemClickListener {
        on_item_click_listener_result onItemClick(int position, int clicked_item);
    }

    // 持有回调接口的引用
    private EditNoteLabelRecycleViewAdapter.OnItemClickListener mListener;

    // 提供设置回调的方法
    public void setOnItemClickListener(EditNoteLabelRecycleViewAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    class labelViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        EditText edit_note_edit_text;

        ImageView edit_note_image_view;
        ConstraintLayout edit_note_image_edit_layout;
        Button edit_note_image_shoot_button;
        Button edit_note_image_upload_button;
        Button edit_note_image_delete_button;

        ConstraintLayout edit_note_audio_layout;
        ImageView edit_note_audio_play;
        ConstraintLayout edit_note_edit_audio_box;
        Button edit_note_get_audio_button;
        Button edit_note_upload_audio_button;
        Button edit_note_delete_audio_button;

        VideoView edit_note_video_view;
        ConstraintLayout edit_note_edit_video_layout;
        Button edit_note_upload_video_button;
        Button edit_note_delete_video_button;


        public labelViewHolder(@NonNull View itemView_, Context context) {
            super(itemView_);
            itemView = itemView_;
        }
        public void init_edit_text(){
            edit_note_edit_text = itemView.findViewById(R.id.edit_one_note_edit_text);
        }

        public void init_image(){
            edit_note_image_view = itemView.findViewById(R.id.edit_one_note_image_view);
            edit_note_image_edit_layout = itemView.findViewById(R.id.edit_one_note_edit_image);
            edit_note_image_shoot_button = itemView.findViewById(R.id.edit_one_note_shoot_image);
            edit_note_image_upload_button = itemView.findViewById(R.id.edit_one_note_upload_image);
            edit_note_image_delete_button = itemView.findViewById(R.id.edit_one_note_delete_image);
        }

        public void init_audio(){
            edit_note_audio_layout = itemView.findViewById(R.id.edit_one_note_audio_layout);
            edit_note_audio_play = itemView.findViewById(R.id.edit_one_note_audio_play);
            edit_note_edit_audio_box = itemView.findViewById(R.id.edit_one_note_edit_audio);
            edit_note_get_audio_button = itemView.findViewById(R.id.edit_one_note_get_audio);
            edit_note_upload_audio_button = itemView.findViewById(R.id.edit_one_note_upload_audio);
            edit_note_delete_audio_button = itemView.findViewById(R.id.edit_one_note_delete_audio);
        }

        public void init_video(){
            edit_note_video_view = itemView.findViewById(R.id.edit_one_note_video_view);
            edit_note_edit_video_layout = itemView.findViewById(R.id.edit_one_note_edit_video);
            edit_note_upload_video_button = itemView.findViewById(R.id.edit_one_note_upload_video);
            edit_note_delete_video_button = itemView.findViewById(R.id.edit_one_note_delete_video);
        }
    }

    public EditNoteLabelRecycleViewAdapter(Context context, List<edit_note_item> item_list_, float dp_to_px_ratio_) {
        this.context = context;
        this.item_list = item_list_;
        this.dp_to_px_ratio = dp_to_px_ratio_;
        selected_position = -1;
        cursor_position_in_text = -1;
    }

    @Override
    public int getItemViewType(int position) {
        // 0表示textEdit，1表示图片，2表示音频，3表示视频
        return item_list.get(position).type;
    }

    @NonNull
    @Override
    public labelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==1){
            View itemView = LayoutInflater.from(this.context).inflate(R.layout.edit_note_picture, parent, false);
            labelViewHolder new_label_view_holder = new labelViewHolder(itemView, this.context);

            new_label_view_holder.init_image();

            if (mListener != null) {
                new_label_view_holder.edit_note_image_shoot_button.setOnClickListener(v -> {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        on_item_click_listener_result temp_item_click_result = mListener.onItemClick(position, 1);
                        edit_note_item aim_edit_item = item_list.get(position);
                        aim_edit_item.image_path = temp_item_click_result.image_path;
                        aim_edit_item.image_bitmap = temp_item_click_result.image_bitmap;
                        notifyItemChanged(position);
                    }
                });
                new_label_view_holder.edit_note_image_upload_button.setOnClickListener(v -> {
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        on_item_click_listener_result temp_item_click_result = mListener.onItemClick(position, 2);
                        edit_note_item aim_edit_item = item_list.get(position);
                        aim_edit_item.image_path = temp_item_click_result.image_path;
                        aim_edit_item.image_bitmap = temp_item_click_result.image_bitmap;
                        notifyItemChanged(position);
                    }
                });
            }

            return new_label_view_holder;
        }
        else if(viewType==2){
            View itemView = LayoutInflater.from(this.context).inflate(R.layout.edit_note_audio, parent, false);
            labelViewHolder new_label_view_holder = new labelViewHolder(itemView, this.context);

            new_label_view_holder.init_audio();

            if (mListener != null) {

            }

            return new_label_view_holder;
        }
        else if(viewType==3){
            View itemView = LayoutInflater.from(this.context).inflate(R.layout.edit_note_video, parent, false);
            labelViewHolder new_label_view_holder = new labelViewHolder(itemView, this.context);

            new_label_view_holder.init_video();

            if (mListener != null) {

            }

            return new_label_view_holder;
        }
        else {  // ViewType=0，EditText
            View itemView = LayoutInflater.from(this.context).inflate(R.layout.edit_note_text, parent, false);
            labelViewHolder new_label_view_holder = new labelViewHolder(itemView, this.context);

            new_label_view_holder.init_edit_text();

            return new_label_view_holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull labelViewHolder holder, int position) {
        edit_note_item aim_edit_note_item = item_list.get(position);
        int viewType=aim_edit_note_item.type;
        if(viewType==1){  // 图片
            if(aim_edit_note_item.image_show){
                holder.edit_note_image_view.setVisibility(View.VISIBLE);
                int image_width=aim_edit_note_item.image_bitmap.getWidth();
                int image_height=aim_edit_note_item.image_bitmap.getHeight();
                ConstraintLayout.LayoutParams edit_note_image_layout_param = (ConstraintLayout.LayoutParams) holder.edit_note_image_view.getLayoutParams();
                edit_note_image_layout_param.height = Math.round((float) (340*image_height/image_width) * dp_to_px_ratio);
                holder.edit_note_edit_text.setLayoutParams(edit_note_image_layout_param);
                holder.edit_note_image_view.setImageBitmap(aim_edit_note_item.image_bitmap);
            }
            else{
                holder.edit_note_image_view.setVisibility(View.GONE);
            }
            if(aim_edit_note_item.image_edit_show){
                holder.edit_note_image_edit_layout.setVisibility(View.VISIBLE);
            }
            else{
                holder.edit_note_image_edit_layout.setVisibility(View.GONE);
            }

            holder.edit_note_image_view.setOnClickListener(v -> {
                if(aim_edit_note_item.image_show) {
                    aim_edit_note_item.image_show=false;
                    item_list.set(position, aim_edit_note_item);
                    notifyItemChanged(holder.getAdapterPosition());
                }
                else{
                    aim_edit_note_item.image_show=true;
                    item_list.set(position, aim_edit_note_item);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });

            holder.edit_note_image_delete_button.setOnClickListener(v -> {
                item_list.remove(position);
                notifyItemRemoved(position);
            });
        }
        else if(viewType==2){  // 音频

        }
        else if(viewType==3){  // 视频

        }
        else {  // ViewType=0，EditText
            holder.edit_note_edit_text.setText(aim_edit_note_item.edit_text_string);
            if(position == this.item_list.size()-1){  // 这是最后一个
                ConstraintLayout.LayoutParams edit_note_edit_text_layout = (ConstraintLayout.LayoutParams) holder.edit_note_edit_text.getLayoutParams();
                edit_note_edit_text_layout.height = Math.round((float) 450 * dp_to_px_ratio);
                holder.edit_note_edit_text.setLayoutParams(edit_note_edit_text_layout);
            }
            else{  // 不是最后一个
                ConstraintLayout.LayoutParams edit_note_edit_text_layout = (ConstraintLayout.LayoutParams) holder.edit_note_edit_text.getLayoutParams();
                int edit_text_lines = holder.edit_note_edit_text.getLineCount();
                edit_note_edit_text_layout.height = Math.round((float) edit_text_lines * 20 * dp_to_px_ratio);
                holder.edit_note_edit_text.setLayoutParams(edit_note_edit_text_layout);
            }

            holder.edit_note_edit_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    cursor_position_in_text = holder.edit_note_edit_text.getSelectionStart();
                    int edit_text_lines = holder.edit_note_edit_text.getLineCount();
                    if(edit_text_lines!=aim_edit_note_item.edit_text_line_num){
                        aim_edit_note_item.edit_text_line_num = edit_text_lines;
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }
            });

            holder.edit_note_edit_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        // EditText获得了焦点
                        selected_position = holder.getAdapterPosition();
                        cursor_position_in_text = holder.edit_note_edit_text.getSelectionStart();
                    }
                    else {
                        // EditText失去了焦点
                    }
                }
            });
        }
    }

    public void addData(int position, String new_name){  //label_or_note: 0 for label, 1 for note
        // mList.add(position, new_name);
        notifyItemInserted(position);
    }

    public void deleteData(int position){
        // mList.remove(position);
        notifyItemRemoved(position);
    }

    public void changeData(int position, String new_name){
        // mList.set(position, new_name);
        notifyItemChanged(position);
    }

    boolean text_edit_cursor_at_tail(){
        if(selected_position==-1||cursor_position_in_text==-1){
            return true;
        }
        else{
            String edit_text_string = item_list.get(selected_position).edit_text_string;
            if(edit_text_string.length()==cursor_position_in_text){
                return true;
            }
            else{
                return false;
            }
        }
    }

    boolean text_edit_cursor_at_head(){
        if(selected_position!=-1||cursor_position_in_text==0){
            return true;
        }
        else{
            return false;
        }
    }

    public void add_image(){
        edit_note_item new_image_item = new edit_note_item(1);
        new_image_item.image_show=false;
        new_image_item.image_edit_show=true;
        if(selected_position==-1||selected_position==item_list.size()-1){  // 在最后加
            int current_item_num = item_list.size();
            new_image_item.position = current_item_num;
            item_list.add(new_image_item);

            edit_note_item new_text_item = new edit_note_item(0);
            new_text_item.edit_text_string="";
            new_text_item.edit_text_line_num=1;
            new_text_item.position = current_item_num+1;
            item_list.add(new_text_item);

            notifyItemChanged(current_item_num-1);
            notifyItemInserted(current_item_num);
            notifyItemInserted(current_item_num+1);
        }
        else{  // 在中间插入
            if(text_edit_cursor_at_tail()){  // 光标位于尾巴，在它后面加
                new_image_item.position = selected_position+1;
                item_list.add(selected_position+1, new_image_item);
                notifyItemInserted(selected_position+1);
            }
            else if(text_edit_cursor_at_head()){  // 光标位于头部，在它前面加
                new_image_item.position = selected_position;
                item_list.add(selected_position, new_image_item);
                notifyItemInserted(selected_position);
            }
            else{  // 光标位于中间，将此EditText一分为二
                edit_note_item current_edit_text_item = item_list.get(selected_position);

                new_image_item.position = selected_position+1;
                item_list.add(selected_position+1, new_image_item);

                edit_note_item new_text_item = new edit_note_item(0);
                new_text_item.edit_text_string="";
                new_text_item.edit_text_line_num=1;
                new_text_item.position = selected_position+2;
                item_list.add(selected_position+2, new_text_item);

                notifyItemChanged(selected_position);
                notifyItemInserted(selected_position+1);
                notifyItemInserted(selected_position+2);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.item_list.size();
    }
}
