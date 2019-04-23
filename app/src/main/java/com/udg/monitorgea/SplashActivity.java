package com.udg.monitorgea;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity
{
    private ImageView appIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        appIcon = findViewById(R.id.app_icon);
    }

    private final Runnable goToMain = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();

        //Definir animacion
        AnimationSet set = new AnimationSet(true);
        Animation fadeIn = FadeIn();
        fadeIn.setStartOffset(0);
        set.addAnimation(fadeIn);

        //Aplicar animación a icono
        appIcon.startAnimation(set);

        //Dar 3 segundos para cambiar de actividad
        Handler handler = new Handler();
        handler.postDelayed(goToMain, 3000);
    }

    private Animation FadeIn()
    {
        Animation fade;
        fade = new AlphaAnimation(0.0f, 1.0f);
        fade.setDuration(2000);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }
}
