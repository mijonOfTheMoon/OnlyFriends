package com.only.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private OnPostActionListener listener;
    private boolean isMyPostsView; // Flag to determine if we're showing user's own posts

    public interface OnPostActionListener {
        void onDelete(Post post);
        void onEdit(Post post);
    }

    public PostAdapter(List<Post> posts, OnPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
        this.isMyPostsView = false; // Default to showing others' posts (no buttons)
    }

    public PostAdapter(List<Post> posts, OnPostActionListener listener, boolean isMyPostsView) {
        this.posts = posts;
        this.listener = listener;
        this.isMyPostsView = isMyPostsView;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameText;
        private ImageView postImageView;
        private TextView captionText;
        private TextView timestampText;
        private ImageButton editButton;
        private ImageButton deleteButton;
        private LinearLayout editDeleteSection;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            postImageView = itemView.findViewById(R.id.postImageView);
            captionText = itemView.findViewById(R.id.captionText);
            timestampText = itemView.findViewById(R.id.timestampText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editDeleteSection = itemView.findViewById(R.id.editDeleteSection);
        }

        public void bind(Post post) {
            userNameText.setText(post.getUserName());
            captionText.setText(post.getCaption() != null ? post.getCaption() : "");
            
            // Load image from Firebase Storage
            if (post.getContent() != null && !post.getContent().isEmpty()) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("post_images/" + post.getContent());
                Glide.with(itemView.getContext())
                        .load(storageRef)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(postImageView);
            } else {
                postImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(post.getTimestamp()));
            timestampText.setText(formattedDate);

            // Show edit/delete buttons only for own posts
            if (isMyPostsView) {
                editDeleteSection.setVisibility(View.VISIBLE);
                
                editButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEdit(post);
                    }
                });
                
                deleteButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDelete(post);
                    }
                });
            } else {
                editDeleteSection.setVisibility(View.GONE);
            }
        }
    }
}
