package com.e.androidtalk.views;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.e.androidtalk.R;
import com.google.firebase.database.ValueEventListener;

import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_chat);
        ButterKnife.bind(this);
    }

}
