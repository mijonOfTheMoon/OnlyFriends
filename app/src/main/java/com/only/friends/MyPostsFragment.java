package com.only.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MyPostsFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private static final int CREATE_POST_REQUEST = 1;
    private static final int EDIT_POST_REQUEST = 2;
    
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> posts;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://onlyfriends-1b1f9-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabAddPost = view.findViewById(R.id.fabAddPost);

        posts = new ArrayList<>();
        // Pass true to indicate this is for user's own posts
        adapter = new PostAdapter(posts, this, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadMyPosts);
        
        fabAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePostActivity.class);
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
                Collections.sort(posts, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
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
        if ((requestCode == CREATE_POST_REQUEST || requestCode == EDIT_POST_REQUEST) 
            && resultCode == getActivity().RESULT_OK) {
            // Refresh the posts when a post is created or edited
            loadMyPosts();
        }
    }    @Override
    public void onDelete(Post post) {
        databaseReference.child("posts").child(post.getId()).removeValue()
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onEdit(Post post) {
        Intent intent = new Intent(getActivity(), CreatePostActivity.class);
        intent.putExtra("post_id", post.getId());
        intent.putExtra("post_caption", post.getCaption());
        intent.putExtra("post_content", post.getContent());
        intent.putExtra("is_editing", true);
        startActivityForResult(intent, EDIT_POST_REQUEST);
    }
}
