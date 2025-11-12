package com.example.map_demo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FavoritePlacesActivity extends AppCompatActivity {

    ListView listView;
    Button addPlaceBtn;
    ArrayList<String> favoritePlaces;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);

        listView = findViewById(R.id.listView);
        addPlaceBtn = findViewById(R.id.btnAddPlace);

        favoritePlaces = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favoritePlaces);
        listView.setAdapter(adapter);

        addPlaceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces();
    }

    private void loadPlaces() {
        SharedPreferences prefs = getSharedPreferences("places_prefs", MODE_PRIVATE);
        String json = prefs.getString("places_list", "[]");
        favoritePlaces.clear();

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                favoritePlaces.add(arr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }
}

