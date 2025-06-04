package com.only.friends;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class FormActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview;
    private TextView titleText;
    private EditText captionField;
    private Button selectImageButton, createPostButton, cancelButton;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private boolean isEditMode = false;
    private String editPostId, existingImageFileName;
    private boolean imageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeFirebase();
        checkEditMode();
        setupClickListeners();
    }

    private void initializeViews() {
        imagePreview = findViewById(R.id.imagePreview);
        titleText = findViewById(R.id.createPostTitle);
        captionField = findViewById(R.id.captionField);
        selectImageButton = findViewById(R.id.selectImageButton);
        createPostButton = findViewById(R.id.createPostButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    @SuppressLint("SetTextI18n")
    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("is_editing", false);
        
        if (isEditMode) {
            editPostId = intent.getStringExtra("post_id");
            String caption = intent.getStringExtra("post_caption");
            existingImageFileName = intent.getStringExtra("post_content");

            titleText.setText("Edit Post");
            createPostButton.setText("Update Post");

            if (caption != null) {
                captionField.setText(caption);
            }

            if (existingImageFileName != null && !existingImageFileName.isEmpty()) {
                loadExistingImage(existingImageFileName);
            }
        }
    }
    
    private void loadExistingImage(String fileName) {
        storageReference.child(fileName).getDownloadUrl().addOnSuccessListener( uri -> Glide.with(this)
                .load(uri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imagePreview));
    }

    private void initializeFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        databaseReference = FirebaseDatabase.getInstance("https://onlyfriends-1b1f9-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    private void setupClickListeners() {
        findViewById(R.id.imagePreviewCard).setOnClickListener(v -> selectImage());
        selectImageButton.setOnClickListener(v -> selectImage());
        
        createPostButton.setOnClickListener(v -> createPost());
        
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    @SuppressLint("IntentReset")
    private void selectImage() {
        @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
            imageChanged = true;
        }
    }

    private void createPost() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String caption = captionField.getText().toString().trim();
        if (TextUtils.isEmpty(caption)) {
            Toast.makeText(this, "Please add a caption", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            updatePost(caption);
        } else {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadImageAndCreatePost(caption);
        }
    }
    
    private void updatePost(String caption) {
        showLoading(true);
        
        if (imageChanged && selectedImageUri != null) {
            uploadImageAndUpdatePost(caption);
        } else {
            updatePostInDatabase(existingImageFileName, caption);
        }
    }
    
    private void uploadImageAndUpdatePost(String caption) {
        String fileName = "post_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        storageReference.child(existingImageFileName).delete();
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    if (existingImageFileName != null && !existingImageFileName.isEmpty()) {
                        storageReference.child(existingImageFileName).delete();
                    }
                    updatePostInDatabase(fileName, caption);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(FormActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updatePostInDatabase(String fileName, String caption) {
        databaseReference.child("posts").child(editPostId).child("content").setValue(fileName);
        databaseReference.child("posts").child(editPostId).child("caption").setValue(caption)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(FormActivity.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(FormActivity.this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImageAndCreatePost(String caption) {
        showLoading(true);

        String fileName = "post_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> createPostInDatabase(fileName, caption))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(FormActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createPostInDatabase(String fileName, String caption) {
        String postId = databaseReference.child("posts").push().getKey();
        AtomicReference<String> username = new AtomicReference<>();

        if (postId == null) {
            showLoading(false);
            Toast.makeText(this, "Failed to generate post ID", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("users").child(currentUser.getUid()).child("name").get().addOnSuccessListener(dataSnapshot -> {
            username.set(dataSnapshot.getValue(String.class));

            if (username.get() == null || username.get().isBlank()) {
                username.set(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
            }

            Post post = new Post(
                    currentUser.getUid(),
                    username.get(),
                    fileName,
                    caption,
                    System.currentTimeMillis()
            );
            post.setId(postId);

            databaseReference.child("posts").child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        Toast.makeText(FormActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(FormActivity.this, "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createPostButton.setEnabled(!show);
        selectImageButton.setEnabled(!show);
        cancelButton.setEnabled(!show);
    }
}
