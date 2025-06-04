package com.only.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> posts;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://onlyfriends-1b1f9-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        posts = new ArrayList<>();
        adapter = new PostAdapter(posts, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadPosts);

        loadPosts();

        return view;
    }

    private void loadPosts() {
        databaseReference.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
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
    }    @Override
    public void onDelete(Post post) {
        if (mAuth.getCurrentUser() != null && post.getUserId().equals(mAuth.getCurrentUser().getUid())) {
            databaseReference.child("posts").child(post.getId()).removeValue();
        }
    }

    @Override
    public void onLike(Post post) {
        if (mAuth.getCurrentUser() != null) {
            // Increment like count
            int newLikeCount = post.getLikeCount() + 1;
            post.setLikeCount(newLikeCount);
            
            // Update in Firebase
            databaseReference.child("posts").child(post.getId()).child("likeCount").setValue(newLikeCount);
            
            // Notify adapter to update the UI
            adapter.notifyDataSetChanged();
        }
    }
}
