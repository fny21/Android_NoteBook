package com.example.nootbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class edit_note_item{
    int position;
    int type;

    String edit_text_string;
    int edit_text_line_num;

    String image_path;
    Bitmap image_bitmap;
    boolean image_show;
    boolean image_edit_show;
    int image_width;

    boolean audio_show;
    boolean audio_edit_show;
    byte[] audio_byte;
    // 使用MediaPlayer播放临时文件
    MediaPlayer mediaPlayer;
    boolean audio_playing;
    boolean getting_audio;
    MediaRecorder mediaRecorder;
    String audio_output_file_path;

    boolean video_show;
    boolean video_edit_show;
    Uri video_uri;
    int video_width;
    MediaController video_controler;

    edit_note_item(int type_){  // 0 text, 1 image, 2 audio, 3 video
        type = type_;
    }

    edit_note_item(int position_, String edit_text_string_, String image_path_){
        position = position_;
        edit_text_string = edit_text_string_;
        image_path = image_path_;
    }
}

class on_item_click_listener_result{
    String image_path;
    Bitmap image_bitmap;

    Uri video_uri;
    MediaController video_controler;

    byte[] audio_byte;

    public on_item_click_listener_result(){
        image_path = null;
        image_bitmap = null;
        video_uri = null;
        audio_byte = null;
    }
}

public class EditNoteLabelRecycleViewAdapter extends RecyclerView.Adapter<EditNoteLabelRecycleViewAdapter.labelViewHolder> {

    private Context context;

    private List<edit_note_item> item_list;  // TODO
    private float dp_to_px_ratio;

    private int selected_position;
    private int cursor_position_in_text;

    private int waiting_image_position;
    private int waiting_audio_position;
    private int waiting_video_position;
    public on_item_click_listener_result image_audio_video_result;

    // 定义回调接口
    public interface OnItemClickListener {
        void onItemClick(int position, int clicked_item);
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

        ConstraintLayout edit_note_image_total_box;
        ImageView edit_note_image_view;
        ConstraintLayout edit_note_image_edit_layout;
        Button edit_note_image_shoot_button;
        Button edit_note_image_upload_button;
        Button edit_note_image_delete_button;
        ImageView edit_note_image_bigger_image_view;
        ImageView edit_note_image_smaller_image_view;

        ConstraintLayout edit_note_audio_total_box;
        ConstraintLayout edit_note_audio_layout;
        ImageView edit_note_audio_play;
        ImageView edit_note_audio_radio;
        TextView edit_note_audio_length;
        ConstraintLayout edit_note_edit_audio_box;
        Button edit_note_get_audio_button;
        Button edit_note_upload_audio_button;
        Button edit_note_delete_audio_button;

        ConstraintLayout edit_note_video_total_box;
        VideoView edit_note_video_view;
        ConstraintLayout edit_note_edit_video_layout;
        Button edit_note_upload_video_button;
        Button edit_note_delete_video_button;
        ImageView edit_note_video_bigger_image_view;
        ImageView edit_note_video_smaller_image_view;


        public labelViewHolder(@NonNull View itemView_, Context context) {
            super(itemView_);
            itemView = itemView_;
        }
        public void init_edit_text(){
            edit_note_edit_text = itemView.findViewById(R.id.edit_one_note_edit_text);
        }

        public void init_image(){
            edit_note_image_total_box = itemView.findViewById(R.id.edit_one_note_image_total_box);
            edit_note_image_view = itemView.findViewById(R.id.edit_one_note_image_view);
            edit_note_image_edit_layout = itemView.findViewById(R.id.edit_one_note_edit_image);
            edit_note_image_shoot_button = itemView.findViewById(R.id.edit_one_note_shoot_image);
            edit_note_image_upload_button = itemView.findViewById(R.id.edit_one_note_upload_image);
            edit_note_image_delete_button = itemView.findViewById(R.id.edit_one_note_delete_image);
            edit_note_image_bigger_image_view = itemView.findViewById(R.id.edit_one_note_image_bigger);
            edit_note_image_smaller_image_view = itemView.findViewById(R.id.edit_one_note_image_smaller);
        }

        public void init_audio(){
            edit_note_audio_radio = itemView.findViewById(R.id.edit_one_note_audio_radio);
            edit_note_audio_length = itemView.findViewById(R.id.edit_one_note_audio_length);
            edit_note_audio_total_box = itemView.findViewById(R.id.edit_note_audio_total_box);
            edit_note_audio_layout = itemView.findViewById(R.id.edit_one_note_audio_layout);
            edit_note_audio_play = itemView.findViewById(R.id.edit_one_note_audio_play);
            edit_note_edit_audio_box = itemView.findViewById(R.id.edit_one_note_edit_audio);
            edit_note_get_audio_button = itemView.findViewById(R.id.edit_one_note_get_audio);
            edit_note_upload_audio_button = itemView.findViewById(R.id.edit_one_note_upload_audio);
            edit_note_delete_audio_button = itemView.findViewById(R.id.edit_one_note_delete_audio);
        }

        public void init_video(){
            edit_note_video_total_box = itemView.findViewById(R.id.edit_note_video_total_box);
            edit_note_video_view = itemView.findViewById(R.id.edit_one_note_video_view);
            edit_note_edit_video_layout = itemView.findViewById(R.id.edit_one_note_edit_video);
            edit_note_upload_video_button = itemView.findViewById(R.id.edit_one_note_upload_video);
            edit_note_delete_video_button = itemView.findViewById(R.id.edit_one_note_delete_video);
            edit_note_video_bigger_image_view = itemView.findViewById(R.id.edit_one_note_video_bigger);
            edit_note_video_smaller_image_view = itemView.findViewById(R.id.edit_one_note_video_smaller);
        }
    }

    public EditNoteLabelRecycleViewAdapter(Context context, List<edit_note_item> item_list_, float dp_to_px_ratio_) {
        this.context = context;
        this.item_list = item_list_;
        this.dp_to_px_ratio = dp_to_px_ratio_;
        selected_position = -1;
        cursor_position_in_text = -1;
        waiting_image_position = -1;
        image_audio_video_result = new on_item_click_listener_result();
        waiting_audio_position = -1;
        waiting_video_position = -1;
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
                    selected_position = -1;
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        waiting_image_position = position;
                        mListener.onItemClick(position, 1);
                    }
                });
                new_label_view_holder.edit_note_image_upload_button.setOnClickListener(v -> {
                    selected_position = -1;
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        waiting_image_position = position;
                        mListener.onItemClick(position, 2);
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
                new_label_view_holder.edit_note_get_audio_button.setOnClickListener(v -> {
                    selected_position = -1;
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        edit_note_item item_in_listener = item_list.get(position);
                        if(!item_in_listener.getting_audio) {
                            if(waiting_audio_position==-1) {
                                waiting_audio_position = position;
                                mListener.onItemClick(position, 1);
                            }
                        }
                        else{
                            item_in_listener.mediaRecorder.stop();
                            item_in_listener.mediaRecorder.release();
                            item_in_listener.mediaRecorder = null;
                            item_in_listener.getting_audio = false;
                            File file = new File(item_in_listener.audio_output_file_path);
                            if (!file.exists()) {
                                try {
                                    throw new FileNotFoundException("File not found: " + item_in_listener.audio_output_file_path);
                                }
                                catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // 设置缓冲区大小
                            byte[] buffer = new byte[1024];
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();

                            try (FileInputStream fis = new FileInputStream(file)) {
                                int bytesRead;
                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    bos.write(buffer, 0, bytesRead);
                                }
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            // 转换为byte数组
                            byte[] bytes = bos.toByteArray();
                            // 关闭ByteArrayOutputStream
                            try {
                                bos.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            item_in_listener.audio_byte = bytes;
                            item_in_listener.audio_show = true;

                            waiting_audio_position = -1;
                            notifyItemChanged(position);
                        }
                    }
                });
                new_label_view_holder.edit_note_upload_audio_button.setOnClickListener(v -> {
                    selected_position = -1;
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        waiting_audio_position = position;
                        mListener.onItemClick(position, 2);
                    }
                });
            }

            return new_label_view_holder;
        }
        else if(viewType==3){
            View itemView = LayoutInflater.from(this.context).inflate(R.layout.edit_note_video, parent, false);
            labelViewHolder new_label_view_holder = new labelViewHolder(itemView, this.context);

            new_label_view_holder.init_video();

            if (mListener != null) {
                new_label_view_holder.edit_note_upload_video_button.setOnClickListener(v -> {
                    selected_position = -1;
                    int position = new_label_view_holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        waiting_video_position = position;
                        mListener.onItemClick(position, 2);
                    }
                });
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

    void set_image_after_result(){
        edit_note_item aim_edit_item = item_list.get(waiting_image_position);
        aim_edit_item.image_path = image_audio_video_result.image_path;
        aim_edit_item.image_bitmap = image_audio_video_result.image_bitmap;
        aim_edit_item.image_show = true;
        aim_edit_item.image_width = 340;
        item_list.set(waiting_image_position, aim_edit_item);
        notifyItemChanged(waiting_image_position);
    }

    void set_video_after_result(){
        edit_note_item aim_edit_item = item_list.get(waiting_video_position);
        aim_edit_item.video_uri = image_audio_video_result.video_uri;
        aim_edit_item.video_controler = image_audio_video_result.video_controler;
        aim_edit_item.video_show = true;
        aim_edit_item.video_width = 340;
        item_list.set(waiting_video_position, aim_edit_item);
        notifyItemChanged(waiting_video_position);
        waiting_video_position = -1;
    }

    void set_audio_after_result(){
        edit_note_item aim_edit_item = item_list.get(waiting_audio_position);
        aim_edit_item.audio_byte = image_audio_video_result.audio_byte;
        aim_edit_item.audio_show = true;
        item_list.set(waiting_audio_position, aim_edit_item);
        notifyItemChanged(waiting_audio_position);
        waiting_audio_position = -1;
    }

    void get_record_audio_permission(){
        edit_note_item aim_edit_item = item_list.get(waiting_audio_position);
        aim_edit_item.getting_audio = true;
        aim_edit_item.mediaRecorder = new MediaRecorder();
        aim_edit_item.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置音频源为麦克风
        aim_edit_item.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 设置输出格式为3GPP
        aim_edit_item.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 设置音频编码格式为AMR_NB
        File dir = context.getFilesDir();
        File audioOutputFile = new File(dir, "output.mp4");
        String audioOutputPath = audioOutputFile.getAbsolutePath();
        aim_edit_item.mediaRecorder.setOutputFile(audioOutputPath);
        aim_edit_item.audio_output_file_path = audioOutputPath;
        try {
            aim_edit_item.mediaRecorder.prepare();
            aim_edit_item.mediaRecorder.start(); // 开始录音
        } catch (IOException e) {
            e.printStackTrace();
        }
        item_list.set(waiting_audio_position, aim_edit_item);
        notifyItemChanged(waiting_audio_position);
    }

    @Override
    public void onBindViewHolder(@NonNull labelViewHolder holder, int position) {
        edit_note_item aim_edit_note_item = item_list.get(position);
        int viewType=aim_edit_note_item.type;
        if(viewType==1){  // 图片
            RecyclerView.LayoutParams edit_note_image_total_box_layout_param = (RecyclerView.LayoutParams) holder.edit_note_image_total_box.getLayoutParams();
            edit_note_image_total_box_layout_param.height = 0;
            if(aim_edit_note_item.image_show){
                holder.edit_note_image_view.setVisibility(View.VISIBLE);
                int image_width=aim_edit_note_item.image_bitmap.getWidth();
                int image_height=aim_edit_note_item.image_bitmap.getHeight();
                ConstraintLayout.LayoutParams edit_note_image_layout_param = (ConstraintLayout.LayoutParams) holder.edit_note_image_view.getLayoutParams();
                int image_height_in_px = Math.round((float) (aim_edit_note_item.image_width*image_height/image_width) * dp_to_px_ratio);
                edit_note_image_layout_param.height = image_height_in_px;
                edit_note_image_layout_param.width = Math.round(aim_edit_note_item.image_width * dp_to_px_ratio);
                holder.edit_note_image_view.setLayoutParams(edit_note_image_layout_param);
                holder.edit_note_image_view.setImageBitmap(aim_edit_note_item.image_bitmap);
                edit_note_image_total_box_layout_param.height += image_height_in_px;
            }
            else{
                holder.edit_note_image_view.setVisibility(View.GONE);
            }
            if(aim_edit_note_item.image_edit_show){
                holder.edit_note_image_edit_layout.setVisibility(View.VISIBLE);
                edit_note_image_total_box_layout_param.height += Math.round((float) 90 * dp_to_px_ratio);
            }
            else{
                holder.edit_note_image_edit_layout.setVisibility(View.GONE);
            }
            holder.edit_note_image_total_box.setLayoutParams(edit_note_image_total_box_layout_param);

            holder.edit_note_image_view.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.image_edit_show) {
                    aim_edit_note_item_in_listener.image_edit_show=false;
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
                else{
                    aim_edit_note_item_in_listener.image_edit_show=true;
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
            });

            holder.edit_note_image_edit_layout.setOnLongClickListener(v -> {
                selected_position = -1;
                insert_edit_text(holder.getAdapterPosition()+1);
                // 返回true则不会再触发onClickListener
                return true;
            });

            holder.edit_note_image_delete_button.setOnClickListener(v -> {
                selected_position = -1;
                delete_image_audio_video(holder.getAdapterPosition());
            });

            holder.edit_note_image_bigger_image_view.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.image_show) {
                    aim_edit_note_item_in_listener.image_width += 10;
                    if(aim_edit_note_item_in_listener.image_width>340){
                        aim_edit_note_item_in_listener.image_width = 340;
                    }
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
            });

            holder.edit_note_image_smaller_image_view.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.image_show) {
                    aim_edit_note_item_in_listener.image_width -= 10;
                    if(aim_edit_note_item_in_listener.image_width<20){
                        aim_edit_note_item_in_listener.image_width = 20;
                    }
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
            });
        }
        else if(viewType==2){  // 音频
            RecyclerView.LayoutParams edit_note_audio_total_box_layout_param = (RecyclerView.LayoutParams) holder.edit_note_audio_total_box.getLayoutParams();
            edit_note_audio_total_box_layout_param.height = 0;
            if(aim_edit_note_item.audio_show){
                holder.edit_note_audio_layout.setVisibility(View.VISIBLE);
                if(aim_edit_note_item.audio_playing){
                    holder.edit_note_audio_radio.setVisibility(View.VISIBLE);
                }
                else{
                    holder.edit_note_audio_radio.setVisibility(View.INVISIBLE);
                }
                edit_note_audio_total_box_layout_param.height += Math.round((float) 50 * dp_to_px_ratio);
                // 将byte[]写入临时文件
                File tempFile = new File(context.getCacheDir(), "temp_audio.mp3");
                int temp_temp_file_index=0;
                while(tempFile.exists()){
                    tempFile = new File(context.getCacheDir(), "temp_audio"+temp_temp_file_index+".mp3");
                    temp_temp_file_index++;
                }
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(aim_edit_note_item.audio_byte);

                    // 使用MediaPlayer播放临时文件
                    aim_edit_note_item.mediaPlayer = new MediaPlayer();
                    try {
                        aim_edit_note_item.mediaPlayer.setDataSource(tempFile.getAbsolutePath());
                        aim_edit_note_item.mediaPlayer.prepare(); // 异步或同步准备

                        aim_edit_note_item.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                // 在这里，MediaPlayer已经准备好播放，我们可以安全地获取时长
                                int duration = aim_edit_note_item.mediaPlayer.getDuration(); // 获取时长，单位是毫秒
                                int seconds = Math.round(duration / 1000);
                                int minutes = (int) Math.floor((double) seconds /60);
                                seconds = seconds % 60;
                                String seconds_str = Integer.toString(seconds);
                                String minutes_str = Integer.toString(minutes);
                                if(seconds_str.length()<2){
                                    seconds_str = "0" + seconds_str;
                                }
                                if(minutes_str.length()<2){
                                    minutes_str = "0" + minutes_str;
                                }
                                holder.edit_note_audio_length.setText(minutes_str+":"+seconds_str);
                            }
                        });

                        aim_edit_note_item.audio_playing=false;
                        holder.edit_note_audio_radio.setVisibility(View.INVISIBLE);
                        aim_edit_note_item.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                // 播放结束后的处理逻辑
                                // 例如，释放MediaPlayer资源
                                int position_in_listener = holder.getAdapterPosition();
                                edit_note_item item_in_listener = item_list.get(position_in_listener);
                                item_in_listener.audio_playing = false;
                                item_list.set(position_in_listener, item_in_listener);
                                notifyItemChanged(position_in_listener);
                            }
                        });
                    }
                    catch (IOException ignored1) {

                    }
                }
                catch (IOException ignored) {

                }
            }
            else{
                holder.edit_note_audio_layout.setVisibility(View.GONE);
            }
            if(aim_edit_note_item.audio_edit_show){
                holder.edit_note_edit_audio_box.setVisibility(View.VISIBLE);
                edit_note_audio_total_box_layout_param.height += Math.round((float) 90 * dp_to_px_ratio);
                if(aim_edit_note_item.getting_audio){
                    holder.edit_note_get_audio_button.setText("停止");
                    holder.edit_note_get_audio_button.setTextColor(0xFFFF0000);
                }
                else{
                    holder.edit_note_get_audio_button.setText("录音");
                    holder.edit_note_get_audio_button.setTextColor(Color.WHITE);
                }
            }
            else{
                holder.edit_note_edit_audio_box.setVisibility(View.GONE);
            }
            holder.edit_note_audio_total_box.setLayoutParams(edit_note_audio_total_box_layout_param);

            holder.edit_note_audio_layout.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.audio_edit_show) {
                    aim_edit_note_item_in_listener.audio_edit_show=false;
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
                else{
                    aim_edit_note_item_in_listener.audio_edit_show=true;
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
            });

            holder.edit_note_audio_play.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item edit_item_in_listener = item_list.get(position_in_listener);
                if(edit_item_in_listener.audio_playing){
                    edit_item_in_listener.audio_playing=false;
                    holder.edit_note_audio_radio.setVisibility(View.INVISIBLE);
                    edit_item_in_listener.mediaPlayer.pause();
                }
                else{
                    edit_item_in_listener.audio_playing=true;
                    holder.edit_note_audio_radio.setVisibility(View.VISIBLE);
                    edit_item_in_listener.mediaPlayer.start();
                }
            });

            holder.edit_note_edit_audio_box.setOnLongClickListener(v -> {
                selected_position = -1;
                insert_edit_text(holder.getAdapterPosition()+1);
                // 返回true则不会再触发onClickListener
                return true;
            });

            holder.edit_note_delete_audio_button.setOnClickListener(v -> {
                selected_position = -1;
                delete_image_audio_video(holder.getAdapterPosition());
            });
        }
        else if(viewType==3){  // 视频
            RecyclerView.LayoutParams edit_note_video_total_box_layout_param = (RecyclerView.LayoutParams) holder.edit_note_video_total_box.getLayoutParams();
            edit_note_video_total_box_layout_param.height = 0;
            if(aim_edit_note_item.video_show){
                ConstraintLayout.LayoutParams edit_note_video_layout_param = (ConstraintLayout.LayoutParams) holder.edit_note_video_view.getLayoutParams();
                edit_note_video_layout_param.height = 0;
                holder.edit_note_video_view.setVisibility(View.VISIBLE);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(context, aim_edit_note_item.video_uri);
                    String widthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    int video_width = Integer.parseInt(widthString);
                    String heightString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    int video_height = Integer.parseInt(heightString);
                    int video_height_in_px = Math.round((float) (aim_edit_note_item.video_width*video_height/video_width) * dp_to_px_ratio);
                    edit_note_video_layout_param.height = video_height_in_px;
                    edit_note_video_layout_param.width = Math.round(aim_edit_note_item.video_width * dp_to_px_ratio);
                    edit_note_video_total_box_layout_param.height += video_height_in_px;
                    holder.edit_note_video_view.setVideoURI(aim_edit_note_item.video_uri);
                    holder.edit_note_video_view.setMediaController(aim_edit_note_item.video_controler);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    // 处理异常，比如文件不存在或无法访问等
                }
                finally {
                    // 释放资源
                    try {
                        retriever.release();
                    }
                    catch (IOException ignored) {

                    }
                }
                holder.edit_note_video_view.setLayoutParams(edit_note_video_layout_param);
            }
            else{
                holder.edit_note_video_view.setVisibility(View.GONE);
            }
            if(aim_edit_note_item.video_edit_show){
                holder.edit_note_edit_video_layout.setVisibility(View.VISIBLE);
                edit_note_video_total_box_layout_param.height += Math.round((float) 90 * dp_to_px_ratio);
            }
            else{
                holder.edit_note_edit_video_layout.setVisibility(View.GONE);
            }
            holder.edit_note_video_total_box.setLayoutParams(edit_note_video_total_box_layout_param);

            holder.edit_note_video_view.setOnLongClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.video_edit_show) {
                    aim_edit_note_item_in_listener.video_edit_show=false;
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
                else{
                    aim_edit_note_item_in_listener.video_edit_show=true;
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
                return true;
            });

            holder.edit_note_video_total_box.setOnLongClickListener(v -> {
                selected_position = -1;
                insert_edit_text(holder.getAdapterPosition()+1);
                // 返回true则不会再触发onClickListener
                return true;
            });

            holder.edit_note_delete_video_button.setOnClickListener(v -> {
                selected_position = -1;
                delete_image_audio_video(holder.getAdapterPosition());
            });

            holder.edit_note_video_bigger_image_view.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.video_show) {
                    aim_edit_note_item_in_listener.video_width += 10;
                    if(aim_edit_note_item_in_listener.video_width>340){
                        aim_edit_note_item_in_listener.video_width = 340;
                    }
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
            });

            holder.edit_note_video_smaller_image_view.setOnClickListener(v -> {
                selected_position = -1;
                int position_in_listener = holder.getAdapterPosition();
                edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                if(aim_edit_note_item_in_listener.video_show) {
                    aim_edit_note_item_in_listener.video_width -= 10;
                    if(aim_edit_note_item_in_listener.video_width<20){
                        aim_edit_note_item_in_listener.video_width = 20;
                    }
                    item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                    notifyItemChanged(position_in_listener);
                }
            });
        }
        else {  // ViewType=0，EditText
            holder.edit_note_edit_text.setText(aim_edit_note_item.edit_text_string);
            int edit_text_lines = holder.edit_note_edit_text.getLineCount();
            if(edit_text_lines<1){
                edit_text_lines=1;
            }
            aim_edit_note_item.edit_text_line_num = edit_text_lines;
            if(position == this.item_list.size()-1){  // 这是最后一个
                ConstraintLayout.LayoutParams edit_note_edit_text_layout = (ConstraintLayout.LayoutParams) holder.edit_note_edit_text.getLayoutParams();
                edit_note_edit_text_layout.height = Math.round((float) 350 * dp_to_px_ratio);
                holder.edit_note_edit_text.setLayoutParams(edit_note_edit_text_layout);
            }
            else{  // 不是最后一个
                ConstraintLayout.LayoutParams edit_note_edit_text_layout = (ConstraintLayout.LayoutParams) holder.edit_note_edit_text.getLayoutParams();
                edit_note_edit_text_layout.height = Math.round((float) edit_text_lines * 30 * dp_to_px_ratio);
                holder.edit_note_edit_text.setLayoutParams(edit_note_edit_text_layout);
            }
            if(selected_position==position){
                holder.edit_note_edit_text.setSelection(cursor_position_in_text);
            }

            holder.edit_note_edit_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(!holder.edit_note_edit_text.hasFocus()){
                        return;
                    }
                    int position_in_listener = holder.getAdapterPosition();
                    edit_note_item aim_edit_note_item_in_listener = item_list.get(position_in_listener);
                    aim_edit_note_item_in_listener.edit_text_string = holder.edit_note_edit_text.getText().toString();
                    cursor_position_in_text = holder.edit_note_edit_text.getSelectionStart();
                    int edit_text_lines = holder.edit_note_edit_text.getLineCount();
                    if(edit_text_lines!=aim_edit_note_item_in_listener.edit_text_line_num){
                        aim_edit_note_item_in_listener.edit_text_line_num = edit_text_lines;
                        item_list.set(position_in_listener, aim_edit_note_item_in_listener);
                        notifyItemChanged(position_in_listener);
                    }
                    else{
                        aim_edit_note_item_in_listener.edit_text_string = holder.edit_note_edit_text.getText().toString();
                        item_list.set(position_in_listener, aim_edit_note_item_in_listener);
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
                }
            });

            holder.edit_note_edit_text.setOnClickListener(v -> {
                cursor_position_in_text = holder.edit_note_edit_text.getSelectionStart();
            });

            holder.edit_note_edit_text.setOnLongClickListener(v -> {
                insert_edit_text(holder.getAdapterPosition()+1);
                // 返回true则不会再触发onClickListener
                return true;
            });
        }
        item_list.set(position, aim_edit_note_item);
    }

    void insert_edit_text(int position){
        edit_note_item new_text_item = new edit_note_item(0);
        new_text_item.edit_text_string="";
        new_text_item.position = position;
        item_list.add(position, new_text_item);
        notifyItemInserted(position);
    }

    void delete_image_audio_video(int position_in_listener){
        edit_note_item item_to_delete = item_list.get(position_in_listener);
        if(item_to_delete.type == 2){
            if(item_to_delete.getting_audio){
                item_to_delete.mediaRecorder.stop();
                item_to_delete.mediaRecorder.release();
                item_to_delete.mediaRecorder = null;
                item_to_delete.getting_audio = false;
            }
            if(item_to_delete.mediaPlayer!=null){
                item_to_delete.mediaPlayer.stop();
                item_to_delete.mediaPlayer.release();
                item_to_delete.mediaPlayer = null;
            }
        }
        item_list.remove(position_in_listener);
        notifyItemRemoved(position_in_listener);
        if(position_in_listener>0){
            edit_note_item before_image_item = item_list.get(position_in_listener-1);
            edit_note_item after_image_item = item_list.get(position_in_listener);
            if(before_image_item.type==0&&after_image_item.type==0){  // 两个EditText需要合并
                before_image_item.edit_text_string = before_image_item.edit_text_string + after_image_item.edit_text_string;
                item_list.set(position_in_listener-1, before_image_item);
                item_list.remove(position_in_listener);
                notifyItemChanged(position_in_listener-1);
                notifyItemRemoved(position_in_listener);
            }
        }
    }

    public void delete_null_edit_text(){
        List<Integer> item_to_delete_list = new ArrayList<>();
        for(int i=item_list.size()-2; i>=0; i--){
            edit_note_item edit_item_to_check = item_list.get(i);
            if(edit_item_to_check.type==0){
                if(edit_item_to_check.edit_text_string.length()==0){
                    item_to_delete_list.add(i);
                }
            }
        }

        for(int item_to_delete : item_to_delete_list){
            item_list.remove(item_to_delete);
            notifyItemRemoved(item_to_delete);
        }
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
        if(selected_position!=-1&&cursor_position_in_text==0){
            return true;
        }
        else{
            return false;
        }
    }

    public void add_image_audio_video(int type){  // 1 image, 2 audio, 3 video
        edit_note_item new_image_audio_video_item;
        if(type==1){
            new_image_audio_video_item = new edit_note_item(1);
            new_image_audio_video_item.image_show=false;
            new_image_audio_video_item.image_edit_show=true;
        }
        else if(type==2){
            new_image_audio_video_item = new edit_note_item(2);
            new_image_audio_video_item.audio_show=false;
            new_image_audio_video_item.audio_edit_show=true;
            new_image_audio_video_item.audio_playing=false;
            new_image_audio_video_item.getting_audio=false;
        }
        else{
            new_image_audio_video_item = new edit_note_item(3);
            new_image_audio_video_item.video_show=false;
            new_image_audio_video_item.video_edit_show=true;
        }

        if(selected_position==-1||(selected_position==item_list.size()-1&&text_edit_cursor_at_tail())){  // 在最后加
            int current_item_num = item_list.size();
            new_image_audio_video_item.position = current_item_num;
            item_list.add(new_image_audio_video_item);

            edit_note_item new_text_item = new edit_note_item(0);
            new_text_item.edit_text_string="";
            new_text_item.position = current_item_num+1;
            item_list.add(new_text_item);

            notifyItemChanged(current_item_num-1);
            notifyItemInserted(current_item_num);
            notifyItemInserted(current_item_num+1);
        }
        else{  // 在中间插入
            if(text_edit_cursor_at_tail()){  // 光标位于尾巴，在它后面加
                new_image_audio_video_item.position = selected_position+1;
                item_list.add(selected_position+1, new_image_audio_video_item);
                notifyItemInserted(selected_position+1);
            }
            else if(text_edit_cursor_at_head()){  // 光标位于头部，在它前面加
                new_image_audio_video_item.position = selected_position;
                item_list.add(selected_position, new_image_audio_video_item);
                notifyItemInserted(selected_position);
            }
            else{  // 光标位于中间，将此EditText一分为二
                edit_note_item current_edit_text_item = item_list.get(selected_position);
                String current_edit_text_string = current_edit_text_item.edit_text_string;
                current_edit_text_item.edit_text_string = current_edit_text_string.substring(0, cursor_position_in_text);
                item_list.set(selected_position, current_edit_text_item);

                new_image_audio_video_item.position = selected_position+1;
                item_list.add(selected_position+1, new_image_audio_video_item);

                edit_note_item new_text_item = new edit_note_item(0);
                new_text_item.edit_text_string=current_edit_text_string.substring(cursor_position_in_text);
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
