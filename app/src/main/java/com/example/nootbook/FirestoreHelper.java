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

import java.util.HashMap;
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

    public void addNote(String userId, String title, String content, String tag, final FirestoreCallback callback) {
        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("content", content);
        note.put("tag", tag);
        note.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(userId).collection("notes")
                .add(note)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    public void updateNote(String userId, String noteId, String title, String content, String tag, final FirestoreCallback callback) {
        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("content", content);
        note.put("tag", tag);
        note.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(userId).collection("notes").document(noteId)
                .update(note)
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

    public interface FirestoreCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }
}
