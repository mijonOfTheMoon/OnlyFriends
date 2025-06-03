package com.only.friends;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactVH> {
    private final List<Contact> contacts;
    private final LayoutInflater inflater;
    private final OnContactActionListener listener;

    public ContactAdapter(Context ctx, List<Contact> contacts, OnContactActionListener listener) {
        this.contacts = contacts;
        this.inflater = LayoutInflater.from(ctx);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_contact, parent, false);
        return new ContactVH(itemView, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ContactVH holder, int position) {
        Contact c = contacts.get(position);
        holder.bind(c);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateContacts(List<Contact> newContacts) {
        contacts.clear();
        contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    public interface OnContactActionListener {
        void onEdit(Contact contact);

        void onDelete(Contact contact);
    }

    class ContactVH extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView emailTextView;
        final TextView phoneTextView;
        final Button editButton;
        final Button deleteButton;

        ContactVH(@NonNull View itemView, Context context) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.item_name);
            emailTextView = itemView.findViewById(R.id.item_email);
            phoneTextView = itemView.findViewById(R.id.item_phone);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            editButton.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listener.onEdit(contacts.get(pos));
            });
            deleteButton.setOnClickListener(v -> new AlertDialog.Builder(context).setTitle("Konfirmasi").setMessage("Apakah kamu yakin ingin menghapus kontak ini?").setPositiveButton("Ya", (dialog, which) -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listener.onDelete(contacts.get(pos));
            }).setNegativeButton("Tidak", null).show());
        }

        void bind(Contact c) {
            nameTextView.setText(c.getName());
            emailTextView.setText(c.getEmail());
            phoneTextView.setText(c.getPhone());
        }
    }
}