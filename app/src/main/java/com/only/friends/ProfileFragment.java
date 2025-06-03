package com.only.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private TextView emailText;
    private EditText nameField;
    private EditText bioField;
    private EditText postContentField;
    private Button updateProfileButton;
    private Button createPostButton;
    private Button logoutButton;
    
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://fir-demo-3ba13-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        emailText = view.findViewById(R.id.emailText);
        nameField = view.findViewById(R.id.nameField);
        bioField = view.findViewById(R.id.bioField);
        postContentField = view.findViewById(R.id.postContentField);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        createPostButton = view.findViewById(R.id.createPostButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailText.setText(currentUser.getEmail());
            loadUserProfile();
        }

        updateProfileButton.setOnClickListener(v -> updateProfile());
        createPostButton.setOnClickListener(v -> createPost());
        logoutButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signOut();
            }
        });

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        databaseReference.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    nameField.setText(user.getName());
                    bioField.setText(user.getBio());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String name = nameField.getText().toString().trim();
        String bio = bioField.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(name, currentUser.getEmail(), bio);
        databaseReference.child("users").child(currentUser.getUid()).setValue(user)
                .addOnSuccessListener(unused -> 
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void createPost() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String content = postContentField.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Post content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameField.getText().toString().trim();
        if (name.isEmpty()) {
            name = "Anonymous";
        }

        Post post = new Post(
            currentUser.getUid(),
            name,
            currentUser.getEmail(),
            content,
            System.currentTimeMillis()
        );

        databaseReference.child("posts").push().setValue(post)
                .addOnSuccessListener(unused -> {
                    postContentField.setText("");
                    Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show());
    }
}
