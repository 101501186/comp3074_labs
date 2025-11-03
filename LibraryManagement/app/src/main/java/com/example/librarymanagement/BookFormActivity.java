package com.example.librarymanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BookFormActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor, etGenre, etYear;
    private Button btnSave;
    private DatabaseHelper db;
    private int bookId = -1; // this for editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_form);

        db = new DatabaseHelper(this);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etGenre = findViewById(R.id.etGenre);
        etYear = findViewById(R.id.etYear);
        btnSave = findViewById(R.id.btnSave);

        if (getIntent().hasExtra("BOOK_ID")) {
            bookId = getIntent().getIntExtra("BOOK_ID", -1);
            loadBookDetails();
        }

        btnSave.setOnClickListener(v -> saveBook());
    }

    private void loadBookDetails() {
        ArrayList<Book> books = db.getAllBooks();
        for (Book b : books) {
            if (b.getId() == bookId) {
                etTitle.setText(b.getTitle());
                etAuthor.setText(b.getAuthor());
                etGenre.setText(b.getGenre());
                etYear.setText(String.valueOf(b.getYear()));
                break;
            }
        }
    }

    private void saveBook() {
        String title = etTitle.getText().toString();
        String author = etAuthor.getText().toString();
        String genre = etGenre.getText().toString();
        int year = Integer.parseInt(etYear.getText().toString());

        if (bookId == -1) {
            db.addBook(new Book(title, author, genre, year));
            Toast.makeText(this, "Book added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateBook(new Book(bookId, title, author, genre, year));
            Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}