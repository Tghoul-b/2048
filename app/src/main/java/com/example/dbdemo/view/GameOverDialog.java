package com.example.dbdemo.view;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dbdemo.R;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;


public class GameOverDialog extends Dialog {

    private String finalScore;
    private String title;

    private View.OnClickListener onShareClickListener;
    private View.OnClickListener onGoOnClickListener;

    public GameOverDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_game_over);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Objects.requireNonNull(getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView title = findViewById(R.id.tv_custom_title);
        TextView finalScore = findViewById(R.id.tv_final_score);
        MaterialButton share = findViewById(R.id.tv_share);
        MaterialButton goOn = findViewById(R.id.tv_go_on);
        if (onShareClickListener != null) {
            share.setOnClickListener(onShareClickListener);
        }
        if (onGoOnClickListener != null) {
            goOn.setOnClickListener(onGoOnClickListener);
        }
        if (!TextUtils.isEmpty(this.finalScore)) {
            finalScore.setText(this.finalScore);
        }
        if (!TextUtils.isEmpty(this.title)) {
            title.setText(this.title);
        }
    }

    public GameOverDialog setOnShareClickListener(
            View.OnClickListener onShareClickListener) {
        this.onShareClickListener = onShareClickListener;
        return this;
    }

    public GameOverDialog setOnGoOnClickListener(
            View.OnClickListener onGoOnClickListener) {
        this.onGoOnClickListener = onGoOnClickListener;
        return this;
    }

    /**
     * ??????????????????
     */
    public GameOverDialog setFinalScore(String finalScore) {
        this.finalScore = finalScore;
        return this;
    }

    /**
     * ????????????
     */
    public GameOverDialog setTitle(String title) {
        this.title = title;
        return this;
    }
}
