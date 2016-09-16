package com.example.student.flickr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.regex.Pattern;

public class Detail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

     /*   requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );*/




        ImageView imageView = (ImageView) findViewById(R.id.detail);

        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.PHOTO_URL);

//        url.replaceFirst(new Pattern("_q\."), "_b.");

        url = url.replace("_q.", "_b.");

        Picasso
                .with(this)
                .load(url)
                .fit()
                .centerCrop()
                .into(imageView);

    }
}
