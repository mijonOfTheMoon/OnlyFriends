package com.only.friends;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyPostsFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private static final int CREATE_POST_REQUEST = 1;
    private static final int EDIT_POST_REQUEST = 2;

    private PostAdapter adapter;
    private List<Post> posts;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://onlyfriends-1b1f9-default-rtdb.asia-southeast1.firebasedatabase.app/");
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        databaseReference = firebaseDatabase.getReference();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        FloatingActionButton fabAddPost = view.findViewById(R.id.fabAddPost);

        posts = new ArrayList<>();
        adapter = new PostAdapter(posts, this, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadMyPosts);
        
        fabAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FormActivity.class);
            startActivityForResult(intent, CREATE_POST_REQUEST);
        });

        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        if (mAuth.getCurrentUser() == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        
        databaseReference.child("posts").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && Objects.equals(post.getUserId(), currentUserId)) {
                        post.setId(postSnapshot.getKey());
                        posts.add(post);
                    }
                }
                posts.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_POST_REQUEST || requestCode == EDIT_POST_REQUEST)) {
            getActivity();
            if (resultCode == Activity.RESULT_OK) {
                loadMyPosts();
            }
        }
    }

    @Override
    public void onDelete(Post post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Post")
                .setMessage("Apakah Anda yakin ingin menghapus post ini?")
                .setPositiveButton("Hapus", (dialog, which) -> deletePost(post))
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void deletePost(Post post) {
        storageReference.child(post.getContent()).delete();
        databaseReference.child("posts").child(post.getId()).removeValue()
                .addOnSuccessListener(aVoid ->
                    Toast.makeText(getContext(), "Post berhasil dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Gagal menghapus Post", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onEdit(Post post) {
        Intent intent = new Intent(getActivity(), FormActivity.class);
        intent.putExtra("post_id", post.getId());
        intent.putExtra("post_caption", post.getCaption());
        intent.putExtra("post_content", post.getContent());
        intent.putExtra("is_editing", true);
        startActivityForResult(intent, EDIT_POST_REQUEST);
    }
}
