package com.example.dbdemo.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.dbdemo.R;
import com.example.dbdemo.app.Config;
import com.example.dbdemo.app.Constant;

public class Cell extends FrameLayout {
    private  TextView cellShowText;//显示数字的TextView
    private int digital;//显示的数字
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Cell(@NonNull Context context) {
        super(context);
        cellShowText=new TextView(context);
        if(Config.CurrentGameMode== Constant.MODE_INFINITE){
            cellShowText.setTextSize(20);
        }
        else{
            switch (Config.GRIDColumnCount){
                case 5:
                    cellShowText.setTextSize(28);
                    break;
                case 6:
                    cellShowText.setTextSize(20);
                default:
                    cellShowText.setTextSize(36);
                    break;
            }
        }
        cellShowText.setGravity(Gravity.CENTER);
        cellShowText.getPaint().setAntiAlias(true);
        cellShowText.getPaint().setFakeBoldText(true);
        cellShowText.setTextColor(getResources().getColor(R.color.colorWhiteDim));
        cellShowText.setBackgroundResource(R.drawable.bg_cell);
        LayoutParams params=new LayoutParams(-1,-1);
        params.setMargins(dp2px(),dp2px(),0,0);
        addView(cellShowText,params);//动态添加组件到界面上
        setDigital(0);
    }

    public Cell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Cell(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Cell(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setDigital(int digital) {
        this.digital = digital;
        cellShowText.setBackgroundTintList(ColorStateList.valueOf(getBackgroundColor(digital)));
        if (digital <= 0) {
            cellShowText.setText("");
        } else {
            cellShowText.setText(String.valueOf(digital));
        }
    }

    public TextView getCellShowText() {
        return cellShowText;
    }

    public void setCellShowText(TextView cellShowText) {
        this.cellShowText = cellShowText;
    }

    public int getDigital() {
        return digital;
    }

    private int getBackgroundColor(int number) {
        switch (number) {
            case 0:
                return ContextCompat.getColor(getContext(), R.color.cell_0);
            case 2:
                return ContextCompat.getColor(getContext(), R.color.cell_2);
            case 4:
                return ContextCompat.getColor(getContext(), R.color.cell_4);
            case 8:
                return ContextCompat.getColor(getContext(), R.color.cell_8);
            case 16:
                return ContextCompat.getColor(getContext(), R.color.cell_16);
            case 32:
                return ContextCompat.getColor(getContext(), R.color.cell_32);
            case 64:
                return ContextCompat.getColor(getContext(), R.color.cell_64);
            case 128:
                return ContextCompat.getColor(getContext(), R.color.cell_128);
            case 256:
                return ContextCompat.getColor(getContext(), R.color.cell_256);
            case 512:
                return ContextCompat.getColor(getContext(), R.color.cell_512);
            case 1024:
                return ContextCompat.getColor(getContext(), R.color.cell_1024);
            case 2048:
                return ContextCompat.getColor(getContext(), R.color.cell_2048);
            default:
                return ContextCompat.getColor(getContext(), R.color.cell_default);
        }
    }
    private int dp2px() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (12 * scale + 0.5f);
    }
}
