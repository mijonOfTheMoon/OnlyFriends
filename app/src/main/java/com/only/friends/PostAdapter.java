package com.only.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private OnPostActionListener listener;    public interface OnPostActionListener {
        void onDelete(Post post);
        void onLike(Post post);
    }

    public PostAdapter(List<Post> posts, OnPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
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
    }    public class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameText;
        private TextView contentText;
        private TextView timestampText;
        private TextView likeCountText;
        private ImageButton likeButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            contentText = itemView.findViewById(R.id.contentText);
            timestampText = itemView.findViewById(R.id.timestampText);
            likeCountText = itemView.findViewById(R.id.likeCountText);
            likeButton = itemView.findViewById(R.id.likeButton);
        }

        public void bind(Post post) {
            userNameText.setText(post.getUserName());
            contentText.setText(post.getContent());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(post.getTimestamp()));
            timestampText.setText(formattedDate);

            likeCountText.setText(String.valueOf(post.getLikeCount()));

            likeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLike(post);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(post);
                }
                return true;
            });
        }
    }
}
