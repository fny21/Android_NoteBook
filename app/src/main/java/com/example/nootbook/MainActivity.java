package com.example.nootbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> startLogInActivityForResult;
    private ActivityResultLauncher<Intent> startEditNoteActivityForResult;

    private List<note_list_item> label_names;  // TODO
    private List<note_list_item> list_for_adapter;
    private TreeMap<Integer, Integer> adapter_map_to_label;
    private TreeMap<Integer, Integer> label_map_to_adapter;
    private float dp_to_px_ratio;  // 将dp转为像素值时乘的比例因子
    private int delete_label_position;
    private int unlabeled_label_position;
    private TreeMap<Integer, Integer> deleted_note_to_label_map;  // 删除的note原本属于哪个label  // TODO
    private labelRecycleViewAdapter label_recycle_view_adapter;

    private ConstraintLayout user_info_layout;
    private ConstraintLayout change_user_info_background_layout;
    private RecyclerView recyclerView;

    private TextView shoot_head_button;
    private TextView upload_head_button;

    String header_image_path;
    Bitmap header_image_bitmap;  // TODO
    String new_header_image_path;
    Bitmap new_header_image_bitmap;

    ImageView user_head_imageview;
    ImageView new_user_head_imageview;

    TextView setting_add_label;
    EditText setting_search_content;
    ImageView setting_search_button;
    Spinner setting_sort_spinner;
    TextView hello_user;
    TextView user_sign_text_view;
    TextView change_user_info_username_text_view;
    TextView change_user_info_password_text_view;
    TextView change_user_info_password_again_text_view;

    int label_unique_index;  // TODO
    int note_unique_index;  // TODO
    String user_sign;  // TODO
    String user_name;  // TODO
    String pass_word;  // TODO

    int editing_position;

    private UserAuthHelper authHelper;
    private FirestoreHelper firestoreHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase initialize
        FirebaseApp.initializeApp(this);
        authHelper = new UserAuthHelper();
        firestoreHelper = new FirestoreHelper();

        label_unique_index = 0;
        note_unique_index = 0;

        // 注册ActivityResultLaunchernote
        startLogInActivityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // 从Intent中提取数据
                            Intent data = result.getData();
                            if (data != null) {
                                String returned_username = data.getStringExtra("username");
                                String returned_password = data.getStringExtra("password");
                                set_data_after_log_in(returned_username, returned_password);
                            }
                        }
                        else{
                            Log.e(String.valueOf(this), "login error"+result.getResultCode());
                            show_error("登录后才可使用笔记");
                            start_log_in_activity();
                        }
                    }
                }
        );

        startEditNoteActivityForResult  = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // 从Intent中提取数据
                            Intent data = result.getData();
                            if (data != null) {
                                // TODO: 编辑笔记返回的数据（如果需要）
                                String new_note_name = data.getStringExtra("new_name");
                                assert new_note_name != null;
                                if(new_note_name.length()==0){
                                    new_note_name = "illegal name";
                                }
                                note_list_item temp_item = list_for_adapter.get(editing_position);
                                temp_item.name = new_note_name;
                                String returned_save_time = data.getStringExtra("save_time");
                                show_error("save_time: "+returned_save_time);
                                temp_item.modify_time = returned_save_time;

                                int position_in_label = locate_in_label_from_position_in_adapter(editing_position);
                                change_in_label_and_adapter(editing_position, position_in_label, temp_item);
                            }
                        }
                        else{;
                            show_error("编辑笔记出错");
                        }
                    }
                }
        );

        start_log_in_activity();

        user_head_imageview = findViewById(R.id.user_info_head);
        new_user_head_imageview = findViewById(R.id.change_user_info_head);

        change_user_info_background_layout = findViewById(R.id.change_user_info_background);
        user_info_layout = findViewById(R.id.user_info);
        recyclerView = (RecyclerView) findViewById(R.id.label_list);
        change_user_info_background_layout.setVisibility(View.INVISIBLE);
        user_info_layout.setEnabled(true);

        setting_add_label = findViewById(R.id.add_label);
        setting_search_content = findViewById(R.id.search_content);
        setting_search_button = findViewById(R.id.search);

        setting_sort_spinner = findViewById(R.id.sort_mode_spinner);
        hello_user = findViewById(R.id.user_info_name);
        user_sign_text_view = findViewById(R.id.change_user_info_sign_text);
        change_user_info_username_text_view = findViewById(R.id.change_user_info_username_text);
        change_user_info_password_text_view = findViewById(R.id.change_user_info_password_text);
        change_user_info_password_again_text_view = findViewById(R.id.change_user_info_password_confirm_text);

        // 下拉框
        String[] options = {"标题", "创建时间", "修改时间"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setting_sort_spinner.setAdapter(adapter);



        setting_add_label.setOnClickListener(v -> {
            note_list_item new_note_item = new note_list_item(0, true, false, "new label", get_unique_label_index(), -1);
            add_in_label_and_adapter(0, 0, new_note_item);
            change_map(-1, 1, 1);
            put_new_adapter_label_map(0, 0);
        });

        setting_search_button.setOnClickListener(v -> {
            String search_string = setting_search_content.getText().toString();
            show_error("search: "+search_string);
            // TODO: finish search
        });

        user_info_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理点击事件
                show_error("修改个人信息");
                change_user_info_background_layout.setVisibility(View.VISIBLE);
                new_header_image_path = null;
                new_header_image_bitmap = null;
                user_info_layout.setEnabled(false);
            }
        });

        Button finish_change_user_info_button = findViewById(R.id.change_user_info_button);
        finish_change_user_info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_error("修改成功");
                change_user_info_background_layout.setVisibility(View.INVISIBLE);

                if (new_header_image_bitmap != null) {
                    header_image_path = new_header_image_path;
                    header_image_bitmap = new_header_image_bitmap;
                    BitmapDrawable temp_bitmapDrawable = new BitmapDrawable(getResources(), header_image_bitmap);
                    user_head_imageview.setBackground(temp_bitmapDrawable);
                }

                user_info_layout.setEnabled(true);
                String new_user_sign = user_sign_text_view.getText().toString();
                if (new_user_sign.length() != 0) {
                    user_sign = new_user_sign;
                    hello_user.setText("Hi! " + user_sign);
                }

                String new_user_name = change_user_info_username_text_view.getText().toString();
                String new_pass_word = change_user_info_password_text_view.getText().toString();
                String new_confirm_pass_word = change_user_info_password_again_text_view.getText().toString();

                if (!new_user_name.equals(user_name) && new_user_name.length() > 0) {
                    user_name = new_user_name;
                    change_user_info_username_text_view.setText(user_name);
                    show_error("用户名修改成功");
                }

                if (new_pass_word.equals(new_confirm_pass_word) && !new_pass_word.equals(pass_word)) {
                    pass_word = new_pass_word;
                    change_user_info_password_text_view.setText(pass_word);
                    change_user_info_password_again_text_view.setText(pass_word);
                    show_error("密码修改成功");
                }

                // 更新Firebase中的用户信息
                updateUserProfile(user_name, new_header_image_path);
                if (!new_pass_word.equals(pass_word)) {
                    updateUserPassword(new_pass_word);
                }
            }
        });

        shoot_head_button = findViewById(R.id.shoot_user_info_head);
        upload_head_button = findViewById(R.id.upload_user_info_head);

        shoot_head_button.setOnClickListener(v -> {
            applyPermission(1);
        });
        upload_head_button.setOnClickListener(v -> {
            applyPermission(2);
        });

        // 从数据库中获取数据
        startLogInActivityForResult.launch(new Intent(this, RegesOrLogIn.class));
    }

    void set_adapter_list_from_main_list(boolean notify_change){
        int previous_adapter_list_size = list_for_adapter.size();
        clear_all_adapter_label_map();
        list_for_adapter.clear();
        //传给Adapter的list
        boolean status = false;  // 0 for normal, 1 for add notes of one label
        for (int i=0; i<label_names.size(); i++){
            note_list_item one_item = label_names.get(i);
            if(one_item.type==0 && one_item.name.startsWith("Recently Deleted")){
                delete_label_position = i;
            }
            if(one_item.type==0 && one_item.name.startsWith("Unlabeled notes")){
                unlabeled_label_position = i;
            }
            if(!status){
                if(one_item.type==0 && one_item.is_hided){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                }
                else if(one_item.type==0 && !one_item.is_hided){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                    status=true;
                }
            }
            else{
                if(one_item.type==1){
                    list_for_adapter.add(one_item);
                }
                else if(one_item.type==0 && !one_item.is_hided){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                }
                else if(one_item.type==0 && one_item.is_hided){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                    status=false;
                }
            }
        }
        int current_adapter_list_size = list_for_adapter.size();
        if(notify_change) {
            if (current_adapter_list_size >= previous_adapter_list_size) {
                for (int i = 0; i < previous_adapter_list_size; i++) {
                    label_recycle_view_adapter.changeData(i);
                }
                for (int i = previous_adapter_list_size; i < current_adapter_list_size; i++) {
                    label_recycle_view_adapter.addData(i);
                }
            } else {
                for (int i = 0; i < current_adapter_list_size; i++) {
                    label_recycle_view_adapter.changeData(i);
                }
                for (int i = current_adapter_list_size; i < previous_adapter_list_size; i++) {
                    label_recycle_view_adapter.deleteData(i);
                }
            }
        }
    }

    private void updateUserProfile(String displayName, String photoUri) {
        authHelper.updateUserProfile(displayName, photoUri, new UserAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(MainActivity.this, "用户信息更新成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "用户信息更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserPassword(String newPassword) {
        authHelper.updateUserPassword(newPassword, new UserAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(MainActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "密码修改失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    void start_log_in_activity(){
        Intent intent = new Intent(this, RegesOrLogIn.class);
        startLogInActivityForResult.launch(intent);
    }

    void edit_note(int position_in_adapter, note_list_item note_message){
        // TODO: 给EditNote传数据（如果需要）
        Intent intent = new Intent(this, EditNote.class);
        intent.putExtra("note_id", note_message.note_id);
        intent.putExtra("note_message", note_message.name);
        editing_position = position_in_adapter;
        startEditNoteActivityForResult.launch(intent);
    }

    void set_data_after_log_in(String username, String password){
        label_names = new ArrayList<>();
        list_for_adapter = new ArrayList<>();
        adapter_map_to_label = new TreeMap<>();
        label_map_to_adapter = new TreeMap<>();
        deleted_note_to_label_map = new TreeMap<>();
        delete_label_position = 0;
        unlabeled_label_position = 0;

        // 从数据库加载数据
        loadUserNotes();

        user_sign = username;
        hello_user.setText("Hi! " + user_sign);
        change_user_info_username_text_view.setText(username);
        change_user_info_password_text_view.setText(password);
        change_user_info_password_again_text_view.setText(password);
        user_name = username;
        pass_word = password;



//        label_names.add(new note_list_item(0, true, false, "You have not created a label, this label is really really long, long long long long long...", get_unique_label_index(), -1));
//        label_names.add(new note_list_item(1, false, false, "1", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(1, false, false, "2", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(1, false, false, "3", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(0, false, false, "Again! You have not created a label, this label is really really long, long long long long long...", get_unique_label_index(), -1));
//        label_names.add(new note_list_item(1, false, false, "4", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(1, false, false, "5", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(1, false, false, "6", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(1, false, false, "7", -1, get_unique_note_index()));
//        label_names.add(new note_list_item(0, true, false, "Last! You have not created a label, this label is really really long, long long long long long...", get_unique_label_index(), -1));
        if(label_names.size()==0) {
            label_names.add(new note_list_item(0, true, false, "Unlabeled notes", get_unique_label_index(), -1));
            label_names.add(new note_list_item(0, true, false, "Recently Deleted", get_unique_label_index(), - 1));
        }

        dp_to_px_ratio = getResources().getDisplayMetrics().density;

        // 笔记列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        set_adapter_list_from_main_list(false);
        //设置Adapter
        label_recycle_view_adapter = new labelRecycleViewAdapter(this, list_for_adapter, this.dp_to_px_ratio);

        setting_sort_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                show_error("spinner position: "+position);
                for(int i=0; i<label_names.size(); i++){
                    note_list_item temp_note_item = label_names.get(i);
                    if(temp_note_item.type==0){
                        if(position==0){
                            sort_label_names(i, 0);
                        }
                        else if(position==1){
                            sort_label_names(i, 1);
                        }
                        else{
                            sort_label_names(i, 2);
                        }
                    }
                }
                set_adapter_list_from_main_list(true);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 当Spinner没有任何项被选中时触发（通常不会触发）
            }
        });

        label_recycle_view_adapter.setOnItemClickListener(new labelRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int clicked_item, String new_name) {
                String item_name;
                if (clicked_item == 0) {
                    item_name = "background";
                } else if (clicked_item == 1) {
                    item_name = "show_or_hide";
                } else if (clicked_item == 2) {
                    item_name = "name_or_title";
                } else if (clicked_item == 3) {
                    item_name = "add_or_edit";
                } else if (clicked_item == 4) {
                    item_name = "delete";
                } else {
                    item_name = "error-unknown" + clicked_item;
                }
                Log.e(String.valueOf(this), "position: " + position + ", item: " + item_name + " been clicked");

                if (clicked_item == 1) {  // "show_or_hide"
                    note_list_item temp_item = list_for_adapter.get(position);
                    int index_in_label_names = adapter_map_to_label.get(position);

                    if (temp_item.type == 0 && temp_item.is_hided) {
                        temp_item.is_hided = false;
                        change_in_label_and_adapter(position, index_in_label_names, temp_item);
                        int new_changed_item_number = adapter_hide_to_show(position + 1, index_in_label_names + 1);
                        change_map(position, new_changed_item_number);
                    } else if (temp_item.type == 0 && !temp_item.is_hided) {
                        temp_item.is_hided = true;
                        change_in_label_and_adapter(position, index_in_label_names, temp_item);
                        int hide_item_number = adapter_show_to_hide(position + 1);
                        change_map(position, -hide_item_number);
                    } else {
                        Log.e(String.valueOf(this), "unexpected prefix in show_or_hide: " + temp_item.name);
                    }
                } else if (clicked_item == 2) {  // change name
                    note_list_item temp_item = list_for_adapter.get(position);
                    temp_item.name = new_name;
                    if (temp_item.type == 0) {
                        int position_in_label = adapter_map_to_label.get(position);
                        label_names.set(position_in_label, temp_item);
                        list_for_adapter.set(position, temp_item);
                    } else {
                        int position_in_label = locate_in_label_from_position_in_adapter(position);
                        label_names.set(position_in_label, temp_item);
                        list_for_adapter.set(position, temp_item);
                    }
                } else if (clicked_item == 3) {  // "add_or_edit"
                    note_list_item temp_item = list_for_adapter.get(position);
                    if (temp_item.type == 1) {
                        if (temp_item.deleted) {  // 删除找回
                            recall_from_recent_deleted(position);
                        } else {  // 编辑此条笔记
                            edit_note(position, temp_item);
                        }
                    } else {
                        // change shown list
                        int index_in_label_names = adapter_map_to_label.get(position);
                        if (temp_item.type == 0 && temp_item.is_hided) {
                            temp_item.is_hided = false;
                            change_in_label_and_adapter(position, index_in_label_names, temp_item);
                            note_list_item new_note_item = new note_list_item(1, false, false, "added note", -1, get_unique_note_index());
                            add_in_label_and_adapter(position + 1, index_in_label_names + 1, new_note_item);
                            int new_shown_item_number = 1 + adapter_hide_to_show(position + 2, index_in_label_names + 2);
                            change_map(position, new_shown_item_number, 1);
                        } else if (temp_item.type == 0 && !temp_item.is_hided) {
                            note_list_item new_note_item = new note_list_item(1, false, false, "added note", -1, get_unique_note_index());
                            add_in_label_and_adapter(position + 1, index_in_label_names + 1, new_note_item);
                            change_map(position, 1, 1);
                        } else {
                            Log.e(String.valueOf(this), "unexpected prefix in add_or_edit: " + temp_item.name);
                        }
                    }
                } else if (clicked_item == 4) {  // "delete"
                    note_list_item temp_item = list_for_adapter.get(position);
                    if (temp_item.type == 1) {
                        if (temp_item.deleted) {  // 彻底删除
                            int position_in_label = locate_in_label_from_position_in_adapter(position);
                            delete_from_adapter_and_label(position, position_in_label);
                            change_map(position, -1, -1);
                        } else {  // 移动到最近删除
                            move_note_to_recent_delete(position, temp_item);
                        }
                    } else if (temp_item.type == 0) {
                        if (temp_item.name.startsWith("Recently Deleted")) {  // 清空最近删除
                            int index_in_label_names = adapter_map_to_label.get(position);
                            if (index_in_label_names != delete_label_position) {
                                Log.e(String.valueOf(this), "delete_label_position is not right, please check!");
                            }
                            if (temp_item.is_hided) {  // 如果隐藏，先展开
                                temp_item.is_hided = false;
                                change_in_label_and_adapter(position, index_in_label_names, temp_item);
                                int new_changed_item_number = adapter_hide_to_show(position + 1, index_in_label_names + 1);
                                change_map(position, new_changed_item_number);
                            }

                            // 删除所有笔记
                            for (int i = index_in_label_names + 1; i < label_names.size(); i++) {
                                delete_from_adapter_and_label(position + i - index_in_label_names, i);
                            }
                            change_map(position + 1, index_in_label_names + 1 - label_names.size());

                            // 最后再隐藏
                            temp_item.is_hided = true;
                            change_in_label_and_adapter(position, index_in_label_names, temp_item);
                            int hide_item_number = adapter_show_to_hide(position + 1);
                            if (hide_item_number != 0) {
                                Log.e(String.valueOf(this), "after delete add, there is still note in recent delete, please check!");
                            }
                        } else {  // 依次移动到最近删除
                            int index_in_label_names = adapter_map_to_label.get(position);
                            if (temp_item.type == 0 && temp_item.is_hided) {  // 如果隐藏，先展开
                                temp_item.is_hided = false;
                                change_in_label_and_adapter(position, index_in_label_names, temp_item);
                                int new_changed_item_number = adapter_hide_to_show(position + 1, index_in_label_names + 1);
                                change_map(position, new_changed_item_number);
                            }

                            delete_label_notes(position);

                            // delete label
                            if (!temp_item.name.startsWith("Unlabeled notes")) {
                                delete_from_adapter_and_label(position, index_in_label_names);
                                remove_label_adapter_map(position, index_in_label_names);
                                change_map(position, -1, -1);
                            }
                        }
                    } else {
                        Log.e(String.valueOf(this), "unexpected name in delete: " + temp_item.name);
                    }
                }
            }
        });
        recyclerView.setAdapter(label_recycle_view_adapter);
        //设置分隔线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void loadUserNotes() {
        String user_id = authHelper.getCurrentUser().getUid();
        firestoreHelper.getNotes(user_id, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                    note_list_item note_item = new note_list_item();
                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) result;
                    note_item.name = document.getString("title");
                    // TODO: edit_time
//                    note_item.init_time = document.getDate("timestamp").toString();
//                    note_item.modify_time = document.getDate("timestamp").toString();
//                    note_item.type = document.getData("type");


                }
                set_adapter_list_from_main_list(true);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "获取笔记失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void sort_label_names(int start_label_position, int sort_mode){
        int end_label_position = label_names.size();
        for(int i=start_label_position+1; i<label_names.size(); i++){
            note_list_item temp_note_item = label_names.get(i);
            if(temp_note_item.type==0){
                end_label_position = i;
                break;
            }
        }
        if(end_label_position<=start_label_position+2){
            return;
        }
        else{
            for(int i=start_label_position+1; i<end_label_position-1; i++){
                for(int j=start_label_position+1; j<end_label_position-i+start_label_position; j++){
                    note_list_item temp_item_1 = label_names.get(j);
                    note_list_item temp_item_2 = label_names.get(j+1);
                    boolean need_swap=false;
                    if(sort_mode==0){  // 标题
                        if(temp_item_1.name.compareTo(temp_item_2.name)>0){
                            need_swap=true;
                        }
                    }
                    else if(sort_mode==1){  // 创建
                        if(temp_item_1.init_time.compareTo(temp_item_2.init_time)<0){
                            need_swap=true;
                        }
                    }
                    else{  // 修改
                        if(temp_item_1.modify_time.compareTo(temp_item_2.modify_time)<0){
                            need_swap=true;
                        }
                    }
                    if(need_swap){
                        label_names.set(j, temp_item_2);
                        label_names.set(j+1, temp_item_1);
                    }
                }
            }
        }
    }

    void show_error(String error_message){
        Toast.makeText(this, error_message, Toast.LENGTH_SHORT).show();
    }

    void clear_all_adapter_label_map(){
        adapter_map_to_label.clear();
        label_map_to_adapter.clear();
    }
    void put_new_adapter_label_map(int adapter_position, int label_position){
        adapter_map_to_label.put(adapter_position, label_position);
        label_map_to_adapter.put(label_position, adapter_position);
    }
    void remove_label_adapter_map(int adapter_position, int label_position){
        adapter_map_to_label.remove(adapter_position);
        label_map_to_adapter.remove(label_position);
    }

    void change_map(int start_position, int add_or_delete_num){  // label_name未改变
        Set<Integer> keys = adapter_map_to_label.keySet();
        List<Integer> keys_to_change = new ArrayList<>();
        List<Integer> values_to_change = new ArrayList<>();
        for(int key : keys){
            if(key>start_position){
                keys_to_change.add(key);
            }
        }
        for(int i=0; i<keys_to_change.size(); i++){
            int previous_map_index = adapter_map_to_label.get(keys_to_change.get(i));
            values_to_change.add(previous_map_index);
            remove_label_adapter_map(keys_to_change.get(i), previous_map_index);
        }
        for(int i=0; i<keys_to_change.size(); i++){
            put_new_adapter_label_map(keys_to_change.get(i)+add_or_delete_num, values_to_change.get(i));
        }
    }

    void change_map(int start_position, int adapter_add_or_delete_num, int label_name_add_or_delete_num){
        Set<Integer> keys = adapter_map_to_label.keySet();
        List<Integer> keys_to_change = new ArrayList<>();
        List<Integer> values_to_change = new ArrayList<>();
        for(int key : keys){
            if(key>start_position){
                keys_to_change.add(key);
            }
        }
        for(int i=0; i<keys_to_change.size(); i++){
            int previous_map_index = adapter_map_to_label.get(keys_to_change.get(i));
            values_to_change.add(previous_map_index);
            remove_label_adapter_map(keys_to_change.get(i), previous_map_index);
        }
        for(int i=0; i<keys_to_change.size(); i++){
            put_new_adapter_label_map(keys_to_change.get(i)+adapter_add_or_delete_num, values_to_change.get(i)+label_name_add_or_delete_num);
        }
    }

    void add_in_adapter(int position, note_list_item item_name){
        list_for_adapter.add(position, item_name);
        label_recycle_view_adapter.addData(position);
    }

    void add_in_label_and_adapter(int position_in_adapter, int position_in_label, note_list_item item_name){
        label_names.add(position_in_label, item_name);
        if(position_in_label<delete_label_position){  // this should always be true
            delete_label_position+=1;
        }
        else{
            Log.e(String.valueOf(this), "current position is "+position_in_label+", which is less than delete_label_position "+delete_label_position);
        }
        if(position_in_label<unlabeled_label_position){
            unlabeled_label_position+=1;
        }
        list_for_adapter.add(position_in_adapter, item_name);
        label_recycle_view_adapter.addData(position_in_adapter);
    }

    void change_in_label_and_adapter(int position_in_adapter, int position_in_label, note_list_item note_item){
        label_names.set(position_in_label, note_item);
        list_for_adapter.set(position_in_adapter, note_item);
        label_recycle_view_adapter.changeData(position_in_adapter);
    }

    int adapter_hide_to_show(int first_note_position_in_adapter, int first_note_position_in_label){
        int new_shown_item_number=0;
        for (int i = first_note_position_in_label; i < label_names.size(); i++) {
            note_list_item one_note_item = label_names.get(i);
            if (one_note_item.type!=1) {
                break;
            }
            new_shown_item_number+=1;
            add_in_adapter(first_note_position_in_adapter + i - first_note_position_in_label, one_note_item);
        }
        return new_shown_item_number;
    }

    int adapter_show_to_hide(int first_note_position_in_adapter){
        int hide_item_number=0;
        int current_position = first_note_position_in_adapter;
        while(true){
            if(current_position>=list_for_adapter.size()){
                break;
            }
            note_list_item one_note_name = list_for_adapter.get(current_position);
            if(one_note_name.type!=1){
                break;
            }
            hide_item_number+=1;
            list_for_adapter.remove(current_position);
            label_recycle_view_adapter.deleteData(current_position);
        }
        return hide_item_number;
    }

    int locate_in_label_from_position_in_adapter(int position_in_adapter){
        Set<Integer> keys = adapter_map_to_label.keySet();
        int previous_key=0;
        for(int key : keys){  // 这里的key应当不会和position_in_adapter重合
            if(key>position_in_adapter){
                break;
            }
            else{
                previous_key = key;
            }
        }
        int aim_label_in_labels = adapter_map_to_label.get(previous_key);
        return aim_label_in_labels + (position_in_adapter-previous_key);
    }

    void delete_from_adapter_and_label(int position_in_adapter, int position_in_label){
        label_names.remove(position_in_label);
        list_for_adapter.remove(position_in_adapter);
        label_recycle_view_adapter.deleteData(position_in_adapter);

        if(position_in_label<delete_label_position){
            delete_label_position-=1;
        }
        if(position_in_label<unlabeled_label_position){
            unlabeled_label_position-=1;
        }
    }

    int find_label_id_of_a_note(int position_in_adapter){
        int aim_label_id = -1;
        for(int i=position_in_adapter-1; i>=0; i--){
            note_list_item temp_note_item = list_for_adapter.get(i);
            if(temp_note_item.type==0){
                aim_label_id = temp_note_item.label_id;
                break;
            }
        }
        return aim_label_id;
    }

    void move_note_to_recent_delete(int position_in_adapter, note_list_item note_item){
        int position_in_label = locate_in_label_from_position_in_adapter(position_in_adapter);
        delete_from_adapter_and_label(position_in_adapter, position_in_label);
        change_map(position_in_adapter, -1, -1);

        note_list_item new_deleted_note_item = new note_list_item(1, false, true, note_item.name, -1, get_unique_note_index());
        int original_label_id = find_label_id_of_a_note(position_in_adapter);
        deleted_note_to_label_map.put(new_deleted_note_item.note_id, original_label_id);

        int delete_position_in_adapter = label_map_to_adapter.get(delete_label_position);
        note_list_item temp_note_item = list_for_adapter.get(delete_position_in_adapter);
        if(temp_note_item.type==0 && !temp_note_item.is_hided) {
            add_in_label_and_adapter(delete_position_in_adapter + 1, delete_label_position + 1, new_deleted_note_item);
        }
        else{
            label_names.add(delete_label_position+1, new_deleted_note_item);
        }
    }

    void delete_label_notes(int position_in_adapter){
        int delete_start_index = position_in_adapter+1;
        int delete_end_index = -1;
        Set<Integer> keys = adapter_map_to_label.keySet();
        boolean find_key=false;
        for(int key : keys){
            if(find_key){
                delete_end_index=key;
                break;
            }
            if(key==position_in_adapter){
                find_key=true;
            }
        }
        if(delete_end_index==-1){
            Log.e(String.valueOf(this), "in delete_label_notes, can't find delete_end_index");
        }

        for(int i=delete_start_index; i<delete_end_index; i++){
            note_list_item temp_note_item = list_for_adapter.get(delete_start_index);  // 这里就是delete_start_index，因为每次删除后面的东西都会前移
            move_note_to_recent_delete(delete_start_index, temp_note_item);
        }
    }

    int find_adapter_position_from_label_id(int aim_label_id){
        for(int i=0; i<list_for_adapter.size(); i++){
            note_list_item temp_item = list_for_adapter.get(i);
            if(temp_item.type==0 && temp_item.label_id==aim_label_id){
                return i;
            }
        }
        return -1;
    }

    void recall_from_recent_deleted(int position_in_adapter){
        int position_in_label = locate_in_label_from_position_in_adapter(position_in_adapter);
        note_list_item recalled_item = list_for_adapter.get(position_in_adapter);
        delete_from_adapter_and_label(position_in_adapter, position_in_label);
        int aim_label_id = deleted_note_to_label_map.get(recalled_item.note_id);
        int aim_label_adapter_position = find_adapter_position_from_label_id(aim_label_id);

        recalled_item.deleted = false;
        Date currentDate = new Date();
        recalled_item.modify_time = currentDate.toString();
        if(aim_label_adapter_position==-1){  // label已不存在，放到未分类里
            recalled_item.label_id = label_names.get(unlabeled_label_position).label_id;
            label_map_to_adapter.get(unlabeled_label_position);
            int temp_position_in_adapter = label_map_to_adapter.get(unlabeled_label_position);
            add_in_label_and_adapter(temp_position_in_adapter+1, unlabeled_label_position+1, recalled_item);
            change_map(temp_position_in_adapter, 1, 1);
            sort_label_names(unlabeled_label_position, setting_sort_spinner.getSelectedItemPosition());
            set_adapter_list_from_main_list(true);
        }
        else{  // label存在，放到label里
            recalled_item.label_id = aim_label_id;
            int aim_label_label_position = adapter_map_to_label.get(aim_label_adapter_position);
            add_in_label_and_adapter(aim_label_adapter_position+1, aim_label_label_position+1, recalled_item);
            change_map(aim_label_adapter_position, 1, 1);
            sort_label_names(aim_label_label_position, setting_sort_spinner.getSelectedItemPosition());
            set_adapter_list_from_main_list(true);
        }
    }

    void applyPermission(int permission_type){
        if(permission_type==1){  // 相机拍摄头像
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // 权限尚未被授予，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, permission_type);
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
                    ActivityCompat.requestPermissions(MainActivity.this,
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
                    ActivityCompat.requestPermissions(MainActivity.this,
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
     * 用户选择是否开启权限操作后的回调
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
                new_header_image_bitmap = (Bitmap) extras.get("data");
                BitmapDrawable temp_bitmapDrawable = new BitmapDrawable(getResources(), new_header_image_bitmap);
                new_user_head_imageview.setBackground(temp_bitmapDrawable);
            }
        }
        else if (requestCode == 12) { // 打开相册，选新头像
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    new_header_image_path = Objects.requireNonNull(data.getData()).toString();
                    InputStream inputStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                    new_header_image_bitmap = BitmapFactory.decodeStream(inputStream);
                    BitmapDrawable temp_bitmapDrawable = new BitmapDrawable(getResources(), new_header_image_bitmap);
                    new_user_head_imageview.setBackground(temp_bitmapDrawable);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int get_unique_label_index(){
        label_unique_index++;
        return label_unique_index;
    }

    private int get_unique_note_index(){
        note_unique_index++;
        return note_unique_index;
    }
}

