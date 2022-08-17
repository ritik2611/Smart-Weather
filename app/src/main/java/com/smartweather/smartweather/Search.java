package com.smartweather.smartweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.zip.Inflater;

public class Search extends AppCompatActivity {
EditText search_field;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //Removing status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Finding view through id
        getId();
        //Action on click keyboard search
        lisentEdit();
    }

    //Sending search value to MainActivity and listening search from keyboard
    private void lisentEdit() {
        search_field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String search = search_field.getText().toString();
                if (search.isEmpty()){
                    search_field.setError("Please enter city name");
                }else{
                    search_field.setError(null);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("rj",search);
                    startActivity(intent);
                    finish();
                }

                return false;
            }
        });
    }

    //Finding view through id
    private void getId() {
        search_field = findViewById(R.id.edit_search_field_id);
    }
}