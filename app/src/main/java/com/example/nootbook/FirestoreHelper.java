package com.example.nootbook;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {      

    private FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
        addNote("1", "1", new HashMap<String, Object>() {{
            put("title", "test");
            put("content", "test");
            put("timestamp", System.currentTimeMillis());
        }}, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("Note added successfully");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to add note");
            }
        }
        );
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


    public void deleteNote(String userId, String noteId, final FirestoreCallback callback) {
        db.collection("users").document(userId).collection("notes").document(noteId)
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


    public void getNotes(String userId, final FirestoreCallback callback) {
        db.collectionGroup("notes")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Map<String, Object>> notes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                notes.add(document.getData());
                            }
                            callback.onSuccess(notes);
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
