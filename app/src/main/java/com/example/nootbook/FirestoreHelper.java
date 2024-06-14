package com.example.nootbook;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.example.nootbook.note_list_item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FirestoreHelper {
    public List<note_list_item> label_names;
    public HashMap<String, String> deleted_note_to_label_map;  // 删除的note原本属于哪个label

    public int label_unique_index;
    public int note_unique_index;
    public String user_sign;
    public String user_name;
    public String pass_word;
    public String true_user_sign;

    public String user_head_path;

    public FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    public FirestoreHelper(List<note_list_item> label_names, HashMap<String, String> deleted_note_to_label_map, String header_image_path,  int label_unique_index, int note_unique_index, String user_sign, String user_name, String pass_word, String true_user_sign) {
        this.db = FirebaseFirestore.getInstance();
        this.label_names = label_names;
        this.deleted_note_to_label_map = deleted_note_to_label_map;
        this.label_unique_index = label_unique_index;
        this.note_unique_index = note_unique_index;
        this.user_sign = user_sign;
        this.user_name = user_name;
        this.pass_word = pass_word;
        this.user_head_path = header_image_path;
        this.true_user_sign = true_user_sign;
    }

    public List<note_list_item> getLabel_names() {
        return label_names;
    }

    // 存储所有本地变量到数据库
    public void saveLocalVariablesToDatabase(String userId, final FirestoreCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("label_unique_index", label_unique_index);
        data.put("note_unique_index", note_unique_index);
        data.put("user_sign", user_sign);
        data.put("user_name", user_name);
        data.put("pass_word", pass_word);
        data.put("user_head_path", user_head_path);
        data.put("true_user_sign", true_user_sign);

        // 将 label_names 转换为 List<Map<String, Object>>
        List<Map<String, Object>> labelNamesList = new ArrayList<>();
        for (note_list_item item : label_names) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("type", item.type);
            String temp_string;
            if(item.is_hided){
                temp_string = "true";
            }
            else{
                temp_string = "false";
            }
            itemMap.put("is_hided", temp_string);
            if(item.deleted){
                temp_string = "true";
            }
            else{
                temp_string = "false";
            }
            itemMap.put("deleted", temp_string);
            itemMap.put("name", item.name);
            itemMap.put("label_id", item.label_id);
            itemMap.put("note_id", item.note_id);
            if(item.search_aim){
                temp_string = "true";
            }
            else{
                temp_string = "false";
            }
            itemMap.put("search_aim", temp_string);
            itemMap.put("search_string", item.search_string);
            itemMap.put("init_time", item.init_time);
            itemMap.put("modify_time", item.modify_time);
            labelNamesList.add(itemMap);
        }
        data.put("label_names", labelNamesList);
        data.put("deleted_note_to_label_map", deleted_note_to_label_map);

        db.collection("users").document(userId)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    // 从数据库加载变量到本地
    public void loadVariablesFromDatabase(String userId, final FirestoreCallback callback) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Long temp_long;
                                temp_long = document.getLong("label_unique_index");
                                if(temp_long!=null){
                                    label_unique_index = temp_long.intValue();
                                }
                                else{
                                    label_unique_index = 0;
                                }
                                temp_long = document.getLong("note_unique_index");
                                if(temp_long!=null){
                                    note_unique_index = temp_long.intValue();
                                }
                                else{
                                    note_unique_index = 0;
                                }
                                String temp_string = document.getString("user_name");
                                if(temp_string!=null){
                                    user_name = temp_string;
                                }
                                temp_string = document.getString("pass_word");
                                if(temp_string!=null){
                                    pass_word = temp_string;
                                }
                                temp_string = document.getString("true_user_sign");
                                if(temp_string!=null){
                                    true_user_sign = temp_string;
                                }
                                user_sign = document.getString("user_sign");
                                if(user_sign==null){
                                    user_sign = user_name;
                                }
                                else if(user_sign.length()==0){
                                    user_sign = user_name;
                                }
                                user_head_path = document.getString("user_head_path");

                                // 从 List<Map<String, Object>> 转换回 label_names
                                List<Map<String, Object>> labelNamesList = (List<Map<String, Object>>) document.get("label_names");
                                label_names = new ArrayList<>();
                                if(labelNamesList!=null) {
                                    for (Map<String, Object> itemMap : labelNamesList) {
                                        boolean is_hided;
                                        boolean deleted;
                                        boolean search_aim;
                                        String temp_temp_string = (String) itemMap.get("is_hided");
                                        if (temp_temp_string.startsWith("tru")) {
                                            is_hided = true;
                                        } else {
                                            is_hided = false;
                                        }
                                        temp_temp_string = (String) itemMap.get("deleted");
                                        if (temp_temp_string.startsWith("tru")) {
                                            deleted = true;
                                        } else {
                                            deleted = false;
                                        }

                                        temp_temp_string = (String) itemMap.get("search_aim") != null ? (String) itemMap.get("search_aim") : "false";
                                        if (temp_temp_string.startsWith("tru")) {
                                            search_aim = true;
                                        } else {
                                            search_aim = false;
                                        }
                                        String init_time = (String) itemMap.get("init_time");
                                        String modify_time = (String) itemMap.get("modify_time");

                                        note_list_item item;
                                        if(init_time==null || modify_time==null) {
                                            item = new note_list_item(
                                                    ((Long) itemMap.get("type")).intValue(),
                                                    is_hided,
                                                    deleted,
                                                    (String) itemMap.get("name"),
                                                    ((Long) itemMap.get("label_id")).intValue(),
                                                    ((Long) itemMap.get("note_id")).intValue(),
                                                    search_aim,
                                                    (String) itemMap.get("name")
                                            );
                                        }
                                        else{
                                            item = new note_list_item(
                                                    ((Long) itemMap.get("type")).intValue(),
                                                    is_hided,
                                                    deleted,
                                                    (String) itemMap.get("name"),
                                                    ((Long) itemMap.get("label_id")).intValue(),
                                                    ((Long) itemMap.get("note_id")).intValue(),
                                                    search_aim,
                                                    (String) itemMap.get("name"),
                                                    init_time,
                                                    modify_time
                                            );
                                        }
                                        label_names.add(item);
                                    }
                                }
                                Object temp_object = document.get("deleted_note_to_label_map");
                                if(temp_object!=null){
                                    deleted_note_to_label_map = (HashMap<String, String>) temp_object;
                                }
                                else{
                                    deleted_note_to_label_map = new HashMap<>();
                                }
                                callback.onSuccess(null);
                            } else {
                                callback.onFailure(new Exception("Document does not exist"));
                            }
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    public void addUser(String userId, String username, String signature, String avatarUri,final FirestoreCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("signature", signature);
        user.put("avatarUri", avatarUri);


        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }


    // 添加笔记
    public void addNote(String userId, String noteId, Map<String, Object> note, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("notes")
                .document(noteId)
                .set(note)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    // 更新笔记
    public void updateNote(String userId, String noteId, Map<String, Object> note, final FirestoreCallback callback) {
        DocumentReference noteRef = db.collection("users").document(userId).collection("notes").document(noteId);
        noteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        noteRef.update(note)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            callback.onSuccess(null);
                                        } else {
                                            callback.onFailure(task.getException());
                                        }
                                    }
                                });
                    } else {
                        addNote(userId, noteId, note, callback);
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    // 获取笔记详情
    public void getNoteDetail(String userId, String noteId, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("notes").document(noteId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    public interface FirestoreCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }
}