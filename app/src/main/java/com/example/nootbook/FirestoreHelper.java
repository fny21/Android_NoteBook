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
    public TreeMap<Integer, Integer> deleted_note_to_label_map;  // 删除的note原本属于哪个label
    public Bitmap header_image_bitmap;

    public int label_unique_index;
    public int note_unique_index;
    public String user_sign;
    public String user_name;
    public String pass_word;

    public FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    public FirestoreHelper(List<note_list_item> label_names, TreeMap<Integer, Integer> deleted_note_to_label_map, Bitmap header_image_bitmap,  int label_unique_index, int note_unique_index, String user_sign, String user_name, String pass_word) {
        this.db = FirebaseFirestore.getInstance();
        this.label_names = label_names;
        this.deleted_note_to_label_map = deleted_note_to_label_map;
        this.label_unique_index = label_unique_index;
        this.note_unique_index = note_unique_index;
        this.user_sign = user_sign;
        this.user_name = user_name;
        this.pass_word = pass_word;
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

        // 将 label_names 转换为 List<Map<String, Object>>
        List<Map<String, Object>> labelNamesList = new ArrayList<>();
        for (note_list_item item : label_names) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("type", item.type);
            itemMap.put("is_hided", item.is_hided);
            itemMap.put("deleted", item.deleted);
            itemMap.put("name", item.name);
            itemMap.put("label_id", item.label_id);
            itemMap.put("note_id", item.note_id);
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
                            if (header_image_bitmap != null) {
                                uploadHeaderImage(userId, header_image_bitmap, callback);
                            } else {
                                callback.onSuccess(null);
                            }
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
                                label_unique_index = document.getLong("label_unique_index").intValue();
                                note_unique_index = document.getLong("note_unique_index").intValue();
                                user_sign = document.getString("user_sign");
                                user_name = document.getString("user_name");
                                pass_word = document.getString("pass_word");

                                // 从 List<Map<String, Object>> 转换回 label_names
                                List<Map<String, Object>> labelNamesList = (List<Map<String, Object>>) document.get("label_names");
                                label_names = new ArrayList<>();
                                for (Map<String, Object> itemMap : labelNamesList) {
                                    note_list_item item = new note_list_item(
                                            (int) itemMap.get("type"),
                                            (boolean) itemMap.get("is_hided"),
                                            (boolean) itemMap.get("deleted"),
                                            (String) itemMap.get("name"),
                                            (int) itemMap.get("label_id"),
                                            (int) itemMap.get("note_id")
                                    );
                                    label_names.add(item);
                                }

                                deleted_note_to_label_map = (TreeMap<Integer, Integer>) document.get("deleted_note_to_label_map");
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


    public void uploadHeaderImage(String userId, Bitmap headerImageBitmap, final FirestoreCallback callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        headerImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("avatars/" + userId + ".jpg");

        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    public void addUser(String userId, String username, String signature, String avatarUri, final FirestoreCallback callback) {
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

    // 添加标签
    public void addLabel(String userId, String labelId, Map<String, Object> label, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("labels")
                .document(labelId)
                .set(label)
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

    // 获取所有笔记
    public void getNotes(String userId, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    // 删除笔记（标记删除）
    public void deleteNote(String userId, String noteId, final FirestoreCallback callback) {
        DocumentReference noteRef = db.collection("users").document(userId).collection("notes").document(noteId);
        noteRef.update("deleted", true)
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

    // 恢复已删除的笔记
    public void undeleteNote(String userId, String noteId, final FirestoreCallback callback) {
        DocumentReference noteRef = db.collection("users").document(userId).collection("notes").document(noteId);
        noteRef.update("deleted", false)
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

    // 彻底删除笔记
    public void completeDeleteNote(String userId, int noteId, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("deleted_note_to_label").document(String.valueOf(noteId))
                .delete()
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

    // 获取所有标签
    public void getLabels(String userId, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("labels")
                .orderBy("init_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
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

    // 获取删除的笔记与标签的映射
    public void getDeletedNoteToLabel(String userId, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("deleted_note_to_label")
                .orderBy("note_id", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
