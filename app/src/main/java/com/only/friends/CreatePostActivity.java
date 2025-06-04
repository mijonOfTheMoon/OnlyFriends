package com.only.friends;

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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview;
    private TextView selectImageText;
    private EditText captionField;
    private Button selectImageButton;
    private Button createPostButton;
    private Button cancelButton;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeFirebase();
        setupClickListeners();
    }

    private void initializeViews() {
        imagePreview = findViewById(R.id.imagePreview);
        selectImageText = findViewById(R.id.selectImageText);
        captionField = findViewById(R.id.captionField);
        selectImageButton = findViewById(R.id.selectImageButton);
        createPostButton = findViewById(R.id.createPostButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("post_images");
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
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

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
            selectImageText.setVisibility(View.GONE);
        }
    }

    private void createPost() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String caption = captionField.getText().toString().trim();
        if (TextUtils.isEmpty(caption)) {
            Toast.makeText(this, "Please add a caption", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndCreatePost(caption);
    }

    private void uploadImageAndCreatePost(String caption) {
        showLoading(true);

        // Generate unique filename
        String fileName = "post_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully, now create post
                        createPostInDatabase(fileName, caption);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Toast.makeText(CreatePostActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createPostInDatabase(String fileName, String caption) {
        String postId = databaseReference.push().getKey();
        
        if (postId == null) {
            showLoading(false);
            Toast.makeText(this, "Failed to generate post ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Post object with filename as content
        Post post = new Post(
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous",
                currentUser.getEmail(),
                fileName, // This is the filename in Firebase Storage
                caption,
                System.currentTimeMillis()
        );
        post.setId(postId);

        databaseReference.child(postId).setValue(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showLoading(false);
                        Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Toast.makeText(CreatePostActivity.this, "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createPostButton.setEnabled(!show);
        selectImageButton.setEnabled(!show);
        cancelButton.setEnabled(!show);
    }
}
