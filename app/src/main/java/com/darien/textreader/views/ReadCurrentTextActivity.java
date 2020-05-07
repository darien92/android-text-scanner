package com.darien.textreader.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.darien.textreader.R;
import com.darien.textreader.repositories.DBHandler;

public class ReadCurrentTextActivity extends AppCompatActivity {
    Button backBtn, saveCurrText;
    TextView textView;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_current_text);
        initVars();
    }

    private void initVars(){
        backBtn = findViewById(R.id.btn_back_curr_text_activity);
        textView = findViewById(R.id.tv_current_text);
        saveCurrText = findViewById(R.id.btn_save_curr_text);
        dbHandler = new DBHandler(this);
        backBtn.setOnClickListener(view -> {
            finish();
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            textView.setText(extras.getString("text"));
            boolean canSave = extras.getBoolean("canSave");
            if (!canSave){
                saveCurrText.setVisibility(View.INVISIBLE);
                saveCurrText.setClickable(false);
            }
        }

        saveCurrText.setOnClickListener(view -> {
            //save current Text in database
            dbHandler.insertText(textView.getText().toString());
            finish();
        });
    }
}
