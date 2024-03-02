package com.example.goeat.auth;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.goeat.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.UUID;

public class Auth {
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;
    private User currentUser;

    private Auth() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private static class Loader {
        static final Auth INSTANCE = new Auth();
    }

    public static class UserUIDSetter {
        private UserUIDSetter(){}
    }
    private static UserUIDSetter userUIDSetter = new UserUIDSetter();

    public static Auth getInstance() {
        return Loader.INSTANCE;
    }

    public User getCurrentUser() {
        return currentUser == null ? null : currentUser.copy();
    }

    public void signOut() {
        currentUser = null;
        firebaseAuth.signOut();
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    private static <T> void addSuccessListener(final TaskCompletionSource<T> taskCompletionSource, Task<T> task) {
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                taskCompletionSource.setException(e);
            }
        })
                .addOnSuccessListener(new OnSuccessListener<T>() {
                    @Override
                    public void onSuccess(T result) {
                        taskCompletionSource.setResult(result);
                    }
                });
    }

    private static <T> OnFailureListener taskcsFailureListener(final TaskCompletionSource<T> taskCompletionSource) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                taskCompletionSource.setException(e);
            }
        };
    }

    public Task<User> signInWithEmailAndPassword(final String email, final String password) {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener(taskcsFailureListener(taskCompletionSource))
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        addSuccessListener(taskCompletionSource, loadCurrentUser());
                    }
                });
        return taskCompletionSource.getTask();
    }

    public Task<User> createUserWithEmailAndPassword(final User user, String password) {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnFailureListener(taskcsFailureListener(taskCompletionSource))
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        final FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            currentUser = user;
                            currentUser.setUid(firebaseUser.getUid(), userUIDSetter);
                            user.setUid(firebaseUser.getUid(), userUIDSetter);
                            databaseReference.child("users").child(firebaseUser.getUid()).setValue(user)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        deleteCurrentUser();
                                        taskCompletionSource.setException(e);
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        taskCompletionSource.setResult(user);
                                    }
                                });
                        } else {
                            taskCompletionSource.setException(new NullPointerException());
                        }
                    }
                });

        return taskCompletionSource.getTask();
    }

    private void deleteCurrentUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.delete();
        }
        signOut();
    }


    public Task<Void> updateCurrentUser(final User user) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            taskCompletionSource.setException(new NullPointerException());
        } else {
            databaseReference.child("users").child(firebaseUser.getUid()).setValue(user)
                    .addOnFailureListener(taskcsFailureListener(taskCompletionSource))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (!user.getEmail().equals(firebaseUser.getEmail())) {
                                addSuccessListener(taskCompletionSource, updateCurrentUserEmail(user.getEmail()));
                            } else {
                                taskCompletionSource.setResult(aVoid);
                            }
                        }
                    });
        }

        return taskCompletionSource.getTask();
    }

    private Task<Void> updateCurrentUserEmail(String email) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            taskCompletionSource.setException(new NullPointerException());
        } else {
            addSuccessListener(taskCompletionSource, firebaseUser.updateEmail(email));
        }
        return taskCompletionSource.getTask();
    }

    public Task<Void> updatePassword(String oldPassword, final String newPassword) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            taskCompletionSource.setException(new NullPointerException());
        } else {
            final AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
            firebaseUser.reauthenticate(credential)
                    .addOnFailureListener(taskcsFailureListener(taskCompletionSource))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            addSuccessListener(taskCompletionSource, firebaseUser.updatePassword(newPassword));
                        }
                    });
        }
        return taskCompletionSource.getTask();
    }

    public Task<User> loadCurrentUser() {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            taskCompletionSource.setException(new NullPointerException());
        } else {
            databaseReference.child("users").child(firebaseUser.getUid()).getRef()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                currentUser = snapshot.getValue(User.class);
                                if (currentUser != null) {
                                    currentUser.setUid(firebaseUser.getUid(), userUIDSetter);
                                }
                                taskCompletionSource.setResult(currentUser);
                            } catch (Exception e) {
                                taskCompletionSource.setException(e);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            taskCompletionSource.setException(error.toException());
                        }
                    });
        }
        return taskCompletionSource.getTask();
    }

    public Task<Uri> uploadCurrentUserAvatar(Bitmap bitmap) {
        final TaskCompletionSource<Uri> taskCompletionSource = new TaskCompletionSource<>();
        final String filename = currentUser == null ? UUID.randomUUID().toString() : currentUser.getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        storageReference.child("images").child(filename).putBytes(data)
                .addOnFailureListener(taskcsFailureListener(taskCompletionSource))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnFailureListener(taskcsFailureListener(taskCompletionSource))
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        taskCompletionSource.setResult(uri);
                                    }
                                });
                    }
                });
        return taskCompletionSource.getTask();
    }

    public void updateHistory(long placeID, String district){
        final long id = placeID;
        final String d = district;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getAndPost(id, d);
            }
        });
        thread.start();
    }

    private void getAndPost(final long placeID, final String district){
        if (currentUser == null) {
            return;
        }
        databaseReference.child("history").child(currentUser.getUid()).child(String.valueOf(placeID)).child("timestamp").getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PlaceID pi = new PlaceID(district, new Date().getTime());
                        post(pi, placeID);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("History", "Error: " + error.getMessage());
                    }
                });
    }

    private void post(PlaceID pi, long placeID){
        databaseReference.child("history").child(currentUser.getUid()).child(String.valueOf(placeID)).setValue(pi);
    }

    @IgnoreExtraProperties
    public static class PlaceID{
        public String district;
        public long timestamp;

        public PlaceID(String district, long timestamp) {
            this.district = district;
            this.timestamp = timestamp;
        }

        public PlaceID(){
        }
    }
}



