package com.example.nootbook;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {

    private FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
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

    public void updateUserDetails(String userId, String userSign, String userName, String password, int labelUniqueIndex, int noteUniqueIndex, Bitmap headerImageBitmap, List<note_list_item> labelNames, final FirestoreCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", userName);
        user.put("signature", userSign);
        user.put("labelUniqueIndex", labelUniqueIndex);
        user.put("noteUniqueIndex", noteUniqueIndex);

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // 更新头像
                            if (headerImageBitmap != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                headerImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                // 上传头像
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
                            } else {
                                callback.onSuccess(null);
                            }
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });

        // 更新标签名称
        CollectionReference labelRef = db.collection("users").document(userId).collection("labels");
        for (int i = 0; i < labelNames.size(); i++) {
            Map<String, Object> label = new HashMap<>();
            label.put("name", labelNames.get(i).name);
            label.put("is_hided", labelNames.get(i).is_hided);
            label.put("deleted", labelNames.get(i).deleted);
            label.put("init_time", labelNames.get(i).init_time);
            label.put("modify_time", labelNames.get(i).modify_time);
            label.put("label_id", labelNames.get(i).label_id);
            labelRef.document(String.valueOf(labelNames.get(i).label_id)).set(label);
        }

        // 更新笔记名称
        CollectionReference noteRef = db.collection("users").document(userId).collection("notes");
        for (int i = 0; i < labelNames.size(); i++) {
            if (labelNames.get(i).type == 1) {
                Map<String, Object> note = new HashMap<>();
                note.put("name", labelNames.get(i).name);
                note.put("is_hided", labelNames.get(i).is_hided);
                note.put("deleted", labelNames.get(i).deleted);
                note.put("init_time", labelNames.get(i).init_time);
                note.put("modify_time", labelNames.get(i).modify_time);
                note.put("note_id", labelNames.get(i).note_id);
                noteRef.document(String.valueOf(labelNames.get(i).note_id)).set(note);
            }
        }
    }

    public void addNote(String userId, String noteId, Map<String, Object> note, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("notes")
                .document(noteId) // 使用noteId作为文档ID
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

    public void addLabel(String userId, String labelId, Map<String, Object> label, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("labels")
                .document(labelId) // 使用labelId作为文档ID
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

    public void updateNote(String userId, String noteId, Map<String, Object> note, final FirestoreCallback callback) {
        DocumentReference noteRef = db.collection("users").document(userId).collection("notes").document(noteId);

        noteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update it
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
                        // Document does not exist, create it
                        addNote(userId, noteId, note, callback);
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

//    public void deleteNote(String userId, String noteId, final FirestoreCallback callback) {
//        db.collection("users").document(userId).collection("notes").document(noteId)
//                .delete()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            callback.onSuccess(null);
//                        } else {
//                            callback.onFailure(task.getException());
//                        }
//                    }
//                });
//    }

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
