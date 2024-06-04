package com.example.nootbook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EditNote extends AppCompatActivity {
    private ImageView edit_back_button;
    private ImageView edit_save_button;
    private ImageView edit_image_button;
    private ImageView edit_audio_button;
    private ImageView edit_video_button;

    private RecyclerView recyclerView;
    private EditNoteLabelRecycleViewAdapter edit_note_label_recycle_view_adapter;
    private float dp_to_px_ratio;  // 将dp转为像素值时乘的比例因子

    private List<EditNoteLabelRecycleViewAdapter.edit_note_item> item_list_for_adapter;

    private String new_image_path_result;
    private Bitmap new_image_bitmap_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_one_note);

        Intent intent = getIntent();
        String note_message = intent.getStringExtra("note_message");
        show_message(note_message);

        dp_to_px_ratio = getResources().getDisplayMetrics().density;

        edit_back_button = findViewById(R.id.edit_title_back);
        edit_save_button = findViewById(R.id.edit_title_save);
        edit_image_button = findViewById(R.id.edit_title_image);
        edit_audio_button = findViewById(R.id.edit_title_audio);
        edit_video_button = findViewById(R.id.edit_title_video);

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

        //设置Adapter
        edit_note_label_recycle_view_adapter = new EditNoteLabelRecycleViewAdapter(this, item_list_for_adapter, this.dp_to_px_ratio);
        bind_on_item_click_listener();
    }

    void back_clicked(){
        show_message("edit title: back clicked");
        Intent returnIntent = new Intent();
        Date currentDate = new Date();
        String dateString = currentDate.toString();
        returnIntent.putExtra("save_time", dateString);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    void save_clicked(){
        show_message("edit title: save clicked");
    }

    void image_clicked(){
        show_message("edit title: image clicked");
        edit_note_label_recycle_view_adapter.add_image();
    }

    void audio_clicked(){
        show_message("edit title: audio clicked");
    }

    void video_clicked(){
        show_message("edit title: video clicked");
    }

    void bind_on_item_click_listener(){
        edit_note_label_recycle_view_adapter.setOnItemClickListener(new EditNoteLabelRecycleViewAdapter.OnItemClickListener() {
            @Override
            public EditNoteLabelRecycleViewAdapter.on_item_click_listener_result onItemClick(int position, int clicked_item) {
                EditNoteLabelRecycleViewAdapter.edit_note_item clicked_item_in_adapter = item_list_for_adapter.get(position);
                EditNoteLabelRecycleViewAdapter.on_item_click_listener_result result_to_return = new EditNoteLabelRecycleViewAdapter.on_item_click_listener_result();
                if(clicked_item_in_adapter.type==1){  // 图片
                    if(clicked_item == 1) {
                        applyPermission(1);
                        result_to_return.image_bitmap = new_image_bitmap_result;
                    }
                    else if(clicked_item == 2) {
                        applyPermission(2);
                        result_to_return.image_path = new_image_path_result;
                        result_to_return.image_bitmap = new_image_bitmap_result;
                    }
                    else{
                        Log.e(String.valueOf(this), "in edit_note_edit_image, position: "+position+", item: "+clicked_item+" is unexpected");
                    }
                }
                else if(clicked_item_in_adapter.type==2){

                }
                return result_to_return;
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
            } else {
                // 权限已经被授予，继续你的任务
                openCamera();
            }
        }
        else if(permission_type==2){  // 本地上传头像
            if(Build.VERSION.SDK_INT<=32) {
                //检测权限
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有权限，则申请需要的权限
                    ActivityCompat.requestPermissions(EditNote.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, permission_type);
                } else {
                    // 已经申请了权限
                    openGallery();
                }
            }
            else{
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有权限，则申请需要的权限
                    ActivityCompat.requestPermissions(EditNote.this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES}, permission_type);
                } else {
                    // 已经申请了权限
                    openGallery();
                }
            }
        }
        else{
            Log.e(String.valueOf(this), "unexpected permission type");
        }
    }

    /**
     * 用户选择是否开启权限操作后的回调；TODO 同意/拒绝
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {  // 相机拍摄
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权
                openCamera();
            }else {
                // 用户拒绝授权
                Toast.makeText(this, "相机权限被拒绝！", Toast.LENGTH_SHORT).show();
                Log.d("HL", "你拒绝使用存储权限！");
            }
        }
        else if(requestCode==2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权
                openGallery();
            }else {
                // 用户拒绝授权
                Toast.makeText(this, "存储权限被拒绝！", Toast.LENGTH_SHORT).show();
                Log.d("HL", "你拒绝使用存储权限！");
            }
        }
    }

    void openCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 11);
        }
    }

    void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , "image/*");
        startActivityForResult(intent, 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            Log.e(String.valueOf(this), "data is null");
        }
        if (requestCode == 11) {  // 打开相机，拍照新头像
            if (resultCode == RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                assert extras != null;
                new_image_bitmap_result = (Bitmap) extras.get("data");
            }
        }
        if (requestCode == 12) { // 打开相册，选新头像
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    new_image_path_result = Objects.requireNonNull(data.getData()).toString();
                    InputStream inputStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                    new_image_bitmap_result = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


