package com.only.friends;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FormActivity extends AppCompatActivity {
    private EditText nameField;
    private EditText emailField;
    private EditText phoneField;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

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
        Objects.requireNonNull(getSupportActionBar()).hide();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://fir-demo-3ba13-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();

        nameField = findViewById(R.id.nameField);
        phoneField = findViewById(R.id.phoneField);
        emailField = findViewById(R.id.emailField);
        Button confirmButton = findViewById(R.id.confirmButton);
        Button cancelButton = findViewById(R.id.batalButton);

        String nama = getIntent().getStringExtra("name");
        String surel = getIntent().getStringExtra("email");
        String nomor = getIntent().getStringExtra("phone");
        String contactId = getIntent().getStringExtra("id");

        if (nama != null) nameField.setText(nama);
        if (surel != null) emailField.setText(surel);
        if (nomor != null) phoneField.setText(nomor);

        confirmButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            Contact contact = new Contact(name, email, phone);
            if (contactId == null) {
                databaseReference.child("contacts").child(Objects.requireNonNull(mAuth.getUid())).push().setValue(contact).addOnSuccessListener(this, unused -> Toast.makeText(FormActivity.this, "Kontak baru berhasil tersimpan", Toast.LENGTH_SHORT).show()).addOnFailureListener(this, e -> Toast.makeText(FormActivity.this, "Kontak gagal tersimpan", Toast.LENGTH_SHORT).show());
            } else {
                databaseReference.child("contacts").child(Objects.requireNonNull(mAuth.getUid())).child(contactId).setValue(contact).addOnSuccessListener(this, unused -> Toast.makeText(FormActivity.this, "Kontak berhasil diperbarui", Toast.LENGTH_SHORT).show()).addOnFailureListener(this, e -> Toast.makeText(FormActivity.this, "Kontak gagal diperbarui", Toast.LENGTH_SHORT).show());
            }
            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
    }
}