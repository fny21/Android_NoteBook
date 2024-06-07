package com.example.nootbook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EditNote extends AppCompatActivity {
    private ImageView edit_back_button;
    private ImageView edit_save_button;
    private ImageView edit_image_button;
    private ImageView edit_audio_button;
    private ImageView edit_video_button;
    private EditText edit_note_title;

    private RecyclerView recyclerView;
    private EditNoteLabelRecycleViewAdapter edit_note_label_recycle_view_adapter;
    private float dp_to_px_ratio;  // 将dp转为像素值时乘的比例因子
    private String note_name;

    private List<edit_note_item> item_list_for_adapter;

    public String new_image_path_result;
    public Bitmap new_image_bitmap_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_one_note);

        Intent intent = getIntent();
        note_name = intent.getStringExtra("note_message");

        dp_to_px_ratio = getResources().getDisplayMetrics().density;

        edit_back_button = findViewById(R.id.edit_title_back);
        edit_save_button = findViewById(R.id.edit_title_save);
        edit_image_button = findViewById(R.id.edit_title_image);
        edit_audio_button = findViewById(R.id.edit_title_audio);
        edit_video_button = findViewById(R.id.edit_title_video);
        edit_note_title = findViewById(R.id.edit_one_note_title);
        edit_note_title.setText(note_name);

        edit_note_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                note_name = edit_note_title.getText().toString();
            }
        });

        edit_back_button.setOnClickListener(v -> {
            back_clicked();
        });
        edit_save_button.setOnClickListener(v -> {
            save_clicked();
        });
        edit_image_button.setOnClickListener(v -> {
            image_clicked();
        });
        edit_audio_button.setOnClickListener(v -> {
            audio_clicked();
        });
        edit_video_button.setOnClickListener(v -> {
            video_clicked();
        });

        recyclerView = (RecyclerView) findViewById(R.id.edit_one_note_item_list);
        // 笔记列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        item_list_for_adapter=new ArrayList<>();
        edit_note_item new_text_item = new edit_note_item(0);
        new_text_item.edit_text_string="";
        new_text_item.position = 0;
        item_list_for_adapter.add(new_text_item);

        //设置Adapter
        edit_note_label_recycle_view_adapter = new EditNoteLabelRecycleViewAdapter(this, item_list_for_adapter, this.dp_to_px_ratio);
        bind_on_item_click_listener();
        recyclerView.setAdapter(edit_note_label_recycle_view_adapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    void back_clicked(){
        show_message("edit title: back clicked");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("new_name", note_name);
        Date currentDate = new Date();
        String dateString = currentDate.toString();
        returnIntent.putExtra("save_time", dateString);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    void save_clicked(){
        show_message("edit title: save clicked");
        edit_note_label_recycle_view_adapter.delete_null_edit_text();
    }

    void image_clicked(){
        show_message("edit title: image clicked");
        edit_note_label_recycle_view_adapter.add_image_audio_video(1);
    }

    void audio_clicked(){
        show_message("edit title: audio clicked");
        edit_note_label_recycle_view_adapter.add_image_audio_video(2);
    }

    void video_clicked(){
        show_message("edit title: video clicked");
        edit_note_label_recycle_view_adapter.add_image_audio_video(3);
    }

    void bind_on_item_click_listener(){
        edit_note_label_recycle_view_adapter.setOnItemClickListener(new EditNoteLabelRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int clicked_item) {
                edit_note_item clicked_item_in_adapter = item_list_for_adapter.get(position);
                if(clicked_item_in_adapter.type==1){  // 图片
                    if(clicked_item == 1) {
                        applyPermission(1);
                    }
                    else if(clicked_item == 2) {
                        applyPermission(2);
                    }
                    else{
                        Log.e(String.valueOf(this), "in edit_note_edit_image, position: "+position+", item: "+clicked_item+" is unexpected");
                    }
                }
                else if(clicked_item_in_adapter.type==2){  // 音频
                    if(clicked_item == 1) {  // 录音
                        applyPermission(3);
                    }
                    else if(clicked_item == 2) {  // 上传
                        applyPermission(4);
                    }
                    else{
                        Log.e(String.valueOf(this), "in edit_note_edit_audio, position: "+position+", item: "+clicked_item+" is unexpected");
                    }
                }
                else if(clicked_item_in_adapter.type==3){  // 视频
                    if(clicked_item == 2) {  // 上传
                        applyPermission(5);
                    }
                    else{
                        Log.e(String.valueOf(this), "in edit_note_edit_video, position: "+position+", item: "+clicked_item+" is unexpected");
                    }
                }
                else{
                    Log.e(String.valueOf(this), "in edit_note_edit_something, position: "+position+", type: "+clicked_item_in_adapter.type+" is unexpected");
                }
            }
        });
    }

    void show_message(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void applyPermission(int permission_type){
        if(permission_type==1){  // 相机拍摄头像
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // 权限尚未被授予，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA}, permission_type);
            }
            else {
                // 权限已经被授予，继续你的任务
                openCamera();
            }
        }
        else if(permission_type==2||permission_type==5||permission_type==4){  // 本地上传头像
            if(Build.VERSION.SDK_INT<=32) {
                //检测权限
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有权限，则申请需要的权限
                    ActivityCompat.requestPermissions(EditNote.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, permission_type);
                }
                else {
                    // 已经申请了权限
                    if(permission_type==2) {
                        openGalleryForImage();
                    }
                    else if(permission_type==5){
                        openGalleryForVideo();
                    }
                    else{
                        get_audio();
                    }
                }
            }
            else{
                if(permission_type==2) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 如果没有权限，则申请需要的权限
                        ActivityCompat.requestPermissions(EditNote.this,
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES}, permission_type);
                    }
                    else {
                        // 已经申请了权限
                        openGalleryForImage();
                    }
                }
                else if(permission_type==5){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 如果没有权限，则申请需要的权限
                        ActivityCompat.requestPermissions(EditNote.this,
                                new String[]{Manifest.permission.READ_MEDIA_VIDEO}, permission_type);
                    }
                    else {
                        // 已经申请了权限
                        openGalleryForVideo();
                    }
                }
                else{
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 如果没有权限，则申请需要的权限
                        ActivityCompat.requestPermissions(EditNote.this,
                                new String[]{Manifest.permission.READ_MEDIA_AUDIO}, permission_type);
                    }
                    else {
                        // 已经申请了权限
                        get_audio();
                    }
                }
            }
        }
        else if(permission_type==3){  // 打开录音机
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                // 权限尚未授权，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, permission_type);
            } else {
                // 权限已经授予，可以进行录音操作
                start_recording_audio();
            }
        }
        else{
            Log.e(String.valueOf(this), "unexpected permission type");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {  // 相机拍摄
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权
                openCamera();
            }
            else {
                // 用户拒绝授权
                Toast.makeText(this, "相机权限被拒绝！", Toast.LENGTH_SHORT).show();
                Log.d("HL", "你拒绝使用存储权限！");
            }
        }
        else if(requestCode==2||requestCode==5||requestCode==4) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权
                if(requestCode==2) {
                    openGalleryForImage();
                }
                else if(requestCode==5){
                    openGalleryForVideo();
                }
                else{
                    get_audio();
                }
            }
            else {
                // 用户拒绝授权
                Toast.makeText(this, "存储权限被拒绝！", Toast.LENGTH_SHORT).show();
                Log.d("HL", "你拒绝使用存储权限！");
            }
        }
        else if(requestCode==3){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 录音权限被授予，可以进行录音操作
                start_recording_audio();
            }
            else {
                Toast.makeText(this, "录音权限被拒绝！", Toast.LENGTH_SHORT).show();
                Log.d("HL", "你拒绝使用录音权限！");
            }
        }
    }

    void openCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 111);
        }
    }

    void openGalleryForImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , "image/*");
        startActivityForResult(intent, 112);
    }

    void openGalleryForVideo(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("video/*");
        startActivityForResult(intent, 115);
    }

    void get_audio(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("audio/*");
        startActivityForResult(intent, 114);
    }

    void start_recording_audio(){
        edit_note_label_recycle_view_adapter.get_record_audio_permission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            Log.e(String.valueOf(this), "data is null");
        }
        if (requestCode == 111){
            if (resultCode == RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                assert extras != null;
                this.new_image_bitmap_result = (Bitmap) extras.get("data");
                edit_note_label_recycle_view_adapter.image_audio_video_result.image_bitmap = new_image_bitmap_result;
                edit_note_label_recycle_view_adapter.set_image_after_result();
            }
        }
        else if (requestCode == 112) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    this.new_image_path_result = Objects.requireNonNull(data.getData()).toString();
                    InputStream inputStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                    this.new_image_bitmap_result = BitmapFactory.decodeStream(inputStream);
                    edit_note_label_recycle_view_adapter.image_audio_video_result.image_path = new_image_path_result;
                    edit_note_label_recycle_view_adapter.image_audio_video_result.image_bitmap = new_image_bitmap_result;
                    edit_note_label_recycle_view_adapter.set_image_after_result();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == 114) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri result_audio_uri = data.getData();
                byte[] audioBytes = null;
                try {
                    assert result_audio_uri != null;
                    try (InputStream inputStream = getContentResolver().openInputStream(result_audio_uri)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        audioBytes = byteArrayOutputStream.toByteArray();
                        edit_note_label_recycle_view_adapter.image_audio_video_result.audio_byte = audioBytes;
                        edit_note_label_recycle_view_adapter.set_audio_after_result();
                    }
                }
                catch (Exception e) {
                    show_message("error when loading audio");
                }
            }
        }
        else if (requestCode == 115) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri result_video_uri = data.getData();
                edit_note_label_recycle_view_adapter.image_audio_video_result.video_uri = result_video_uri;
                edit_note_label_recycle_view_adapter.set_video_after_result();
            }
        }
    }
}


