package com.example.librarymanagement;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewBooks;
    private Button btnAddBook;
    private DatabaseHelper db;
    private ArrayList<Book> books;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        listViewBooks = findViewById(R.id.listViewBooks);
        btnAddBook = findViewById(R.id.btnAddBook);

        loadBooks();

        btnAddBook.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BookFormActivity.class));
        });

        listViewBooks.setOnItemClickListener((parent, view, position, id) -> {
            Book selectedBook = books.get(position);

            String[] options = {"Edit", "Delete"};
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(selectedBook.getTitle())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) { // Edit
                            Intent intent = new Intent(MainActivity.this, BookFormActivity.class);
                            intent.putExtra("BOOK_ID", selectedBook.getId());
                            startActivity(intent);
                        } else { // Delete
                            db.deleteBook(selectedBook.getId());
                            Toast.makeText(MainActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
                            loadBooks();
                        }
                    })
                    .show();
        });
    }

    private void loadBooks() {
        books = db.getAllBooks();
        ArrayList<String> titles = new ArrayList<>();
        for (Book b : books) {
            titles.add(b.getTitle() + " (" + b.getYear() + ")");
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listViewBooks.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
    }
}
