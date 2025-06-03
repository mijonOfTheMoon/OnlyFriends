package com.only.friends;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class ContactActivity extends AppCompatActivity implements ContactAdapter.OnContactActionListener {

    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private TextView welcome_text;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Objects.requireNonNull(getSupportActionBar()).hide();

        mAdapter = new ContactAdapter(this, new ArrayList<>(), this);
        RecyclerView rView = findViewById(R.id.RecyclerView);
        rView.setAdapter(mAdapter);
        rView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormActivity.class);
            startActivity(intent);
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://fir-demo-3ba13-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(getBaseContext());

        FloatingActionButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> signOut());

        welcome_text = findViewById(R.id.welcome_text);

    }

    @Override
    public void onEdit(Contact contact) {
        Intent form = new Intent(this, FormActivity.class);
        form.putExtra("id", contact.getId());
        form.putExtra("name", contact.getName());
        form.putExtra("phone", contact.getPhone());
        form.putExtra("email", contact.getEmail());
        startActivity(form);
    }

    @Override
    public void onDelete(Contact contact) {
        databaseReference.child("contacts").child(Objects.requireNonNull(mAuth.getUid())).child(contact.getId()).removeValue().addOnSuccessListener(this, unused -> Toast.makeText(ContactActivity.this, "Kontak berhasil dihapus", Toast.LENGTH_SHORT).show()).addOnFailureListener(this, e -> Toast.makeText(ContactActivity.this, "Failed to delete contact", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            welcome_text.setText("Welcome,\n" + currentUser.getEmail());
        }
        refresh();
    }

    private void signOut() {
        mAuth.signOut();

        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(clearRequest, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {
            @Override
            public void onResult(@NonNull Void result) {
                updateUI(true);
                Intent intent = new Intent(ContactActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(@NonNull ClearCredentialException e) {
                updateUI(false);
            }
        });
    }

    private void updateUI(final boolean state) {
        runOnUiThread(() -> {
            try {
                if (state) {
                    Toast.makeText(ContactActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ContactActivity.this, "Error logging out", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void refresh() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.child("contacts").child(Objects.requireNonNull(mAuth.getUid())).getChildren()) {
                    Contact contact = snapshot.getValue(Contact.class);
                    if (contact != null) {
                        contact.setId(snapshot.getKey());
                        contacts.add(contact);
                    }
                }
                mAdapter.updateContacts(contacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactActivity.this, "Failed to read contacts.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}