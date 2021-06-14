package com.example.dbdemo.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridLayout;

import androidx.annotation.RequiresApi;

import com.example.dbdemo.R;
import com.example.dbdemo.app.Config;
import com.example.dbdemo.app.ConfigManager;
import com.example.dbdemo.app.Constant;
import com.example.dbdemo.db.CellEntity;
import com.example.dbdemo.db.GameDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameView extends GridLayout {
    public static final String KEY_SCORE = "KEY_SCORE";
    public static final String KEY_RESULT = "KEY_RESULT";
    public static final String ACTION_RECORD_SCORE = "ACTION_RECORD_SCORE";
    public static final String ACTION_WIN = "ACTION_WIN";
    public static final String ACTION_LOSE = "ACTION_LOSE";

    public static final int MIN_DIS=64;

    private float setX;
    private float setY;

    private float offsetX;
    private float offsetY;

    private Cell[][] cells;

    private final List<Integer> dataAfterSwipe=new ArrayList<>();

    private final List<Point> emptyCellPoint=new ArrayList<>();

    private int recordPreviousDigital=-1;//记录上一个位置的数字

    private final ArrayList<Integer>  someData=new ArrayList<>();

    private SoundPool mSoundPool;

    private int soundID;

    private boolean canSwipe;//是否可以滑动
    private int gridColumnCount;//每列方格数

    private int gameMode;

    private  GameDatabaseHelper gameDatabaseHelper;

    public GameView(Context context) {
        super(context);
        System.out.println("get here1");
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSoundPool();
        setOrientation(GridLayout.HORIZONTAL);
        System.out.println("get here");
        gameDatabaseHelper=new GameDatabaseHelper(context, Constant.DB_NAME,null,1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initView(int mode){
        gameMode=mode;
        canSwipe=true;
        removeAllViews();
        if(mode==Constant.MODE_CLASSIC){
            gridColumnCount= Config.GRIDColumnCount;
        }else if(mode==Constant.MODE_INFINITE){
            gridColumnCount=6;
        }
        cells=new Cell[gridColumnCount][gridColumnCount];
        setColumnCount(gridColumnCount);//设置界面的烈面
        int cellWidth=getCellSize();
        int cellHeight=getCellSize();
        addCell(cellWidth,cellHeight);
        startGame();
        setOnTouchListener((v,event)-> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (canSwipe) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setX = event.getX();
                        setY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        offsetX = event.getX() - setX;
                        offsetY = event.getY() - setY;
                        int orientaion = getOrientation(offsetX, offsetY);
                        switch (orientaion) {
                            case 0:
                                swipeRight();
                                break;
                            case 1:
                                swipeLeft();
                                break;
                            case 2:
                                swipeDown();
                                break;
                            case 3:
                                swipeUp();
                                break;
                            default:
                                break;
                        }
                    default:
                        break;
                }
            }
            return true;
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSoundPool() {
        mSoundPool = new SoundPool.Builder().setMaxStreams(2).build();
        // 加载音效资源
        soundID = mSoundPool.load(getContext(), R.raw.game_2048_volume, 1);
    }
    private int getCellSize() {
        //  获取屏幕的宽度
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int cardWidth = metrics.widthPixels - dp2px();
        return (cardWidth - 12) / gridColumnCount;
    }

    /**
     * dp转换成px
     */
    private int dp2px() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((float) 16 * scale + 0.5f);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addCell(int cellWidth, int cellHeight) {
        Cell cell;
        for (int i = 0; i < gridColumnCount; i++) {
            for (int j = 0; j < gridColumnCount; j++) {
                cell = new Cell(getContext());

                cell.setDigital(0);
                addView(cell, cellWidth, cellHeight);
                cells[i][j] = cell;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startGame(){
        reset();
        ArrayList<CellEntity> data=new ArrayList<>();
        SQLiteDatabase db=gameDatabaseHelper.getWritableDatabase();
        Cursor cursor=db.query(Config.getTableName(),null,null,null,null
        ,null,null,null);
        if(null!=cursor){
            if(cursor.moveToFirst()){
                do{
                    int x=cursor.getInt(cursor.getColumnIndex("x"));
                    int y=cursor.getInt(cursor.getColumnIndex("y"));
                    int num=cursor.getInt(cursor.getColumnIndex("num"));
                    data.add(new CellEntity(x,y,num));
                }while (cursor.moveToNext());
            }
            cursor.close();
            if(data.size()<=2){
                initGame();
            }
            else
                resumeGame(data);//恢复游戏画面
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void resumeGame(ArrayList<CellEntity> data){
        for(CellEntity cellEntity:data){
            cells[cellEntity.getX()][cellEntity.getY()].setDigital(cellEntity.getNum());
            setAppearAnim(cells[cellEntity.getX()][cellEntity.getY()]);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void resetGame(){
        reset();
        addDigital(false);
        addDigital(false);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void reset() {
        for (int i = 0; i < gridColumnCount; i++) {
            for (int j = 0; j < gridColumnCount; j++) {
                cells[i][j].setDigital(0);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initGame() {
        addDigital(false);
        addDigital(false);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void addDigital(boolean isCheat){
        getEmptyCell();
        if(emptyCellPoint.size()>0){
            Point point=emptyCellPoint.get((int)Math.random()*emptyCellPoint.size());
            if(isCheat)
                cells[point.x][point.y].setDigital(1024);
            else
                cells[point.x][point.y].setDigital(Math.random()>0.4?2:4);
            setAppearAnim(cells[point.x][point.y]);
        }
    }

    private void getEmptyCell(){
        emptyCellPoint.clear();
        for(int i=0;i<gridColumnCount;i++){
            for(int j=0;j<gridColumnCount;j++){
                if(cells[i][j].getDigital()<=0)
                    emptyCellPoint.add(new Point(i,j));
            }
        }
    }
    private void playSound() {
        if (Config.VolumeState) {
            mSoundPool.play(soundID, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
    public ArrayList<CellEntity> getCurrentProcess() {
        ArrayList<CellEntity> data = new ArrayList<>();
        for (int i = 0; i < gridColumnCount; i++) {
            for (int j = 0; j < gridColumnCount; j++) {
                int digital = cells[i][j].getDigital();
                if (digital > 0) {
                    data.add(new CellEntity(i, j, digital));
                }
            }
        }
        return data;
    }
    private void setAppearAnim(Cell cell) {
        // 设置缩放动画（以自身中心为缩放点，从10%缩放到原始大小）
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.1f, 1, 0.1f, 1,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(120);
        cell.setAnimation(null);
        cell.getCellShowText().startAnimation(scaleAnimation);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void swipeUp() {
        // 判断是否需要添加数字
        boolean needAddDigital = false;
        for (int i = 0; i < gridColumnCount; i++) {
            for (int j = 0; j < gridColumnCount; j++) {
                // 获取当前位置数字
                int currentDigital = cells[j][i].getDigital();
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    // 记录数字
                    if (recordPreviousDigital == -1) {
                        recordPreviousDigital = currentDigital;
                    } else {
                        // 记录的之前的数字和当前数字不同
                        if (recordPreviousDigital != currentDigital) {
                            // 加入记录的数字
                            dataAfterSwipe.add(recordPreviousDigital);
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            // 加入*2
                            dataAfterSwipe.add(recordPreviousDigital * 2);
                            // 记录得分
                            recordScore(recordPreviousDigital * 2);
                            // 重置记录数字
                            recordPreviousDigital = -1;
                        }
                    }
                }
            }

            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }

            // 补0
            for (int p = dataAfterSwipe.size(); p < gridColumnCount; p++) {
                dataAfterSwipe.add(0);
            }
            // 若原始数据和移动后的数据不同，视为界面发生改变
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();

            // 重新设置格子数据
            for (int k = 0; k < dataAfterSwipe.size(); k++) {
                cells[k][i].setDigital(dataAfterSwipe.get(k));
            }
            // 重置数据
            recordPreviousDigital = -1;
            dataAfterSwipe.clear();
        }
        if (needAddDigital) {
            // 添加一个随机数字（2或4）
            addDigital(false);
            playSound();
        }
        judgeOverOrAccomplish();
    }

    /**
     * 下滑
     */
    private void swipeDown() {
        // 判断是否需要添加数字
        boolean needAddDigital = false;
        for (int i = gridColumnCount - 1; i >= 0; i--) {
            for (int j = gridColumnCount - 1; j >= 0; j--) {
                // 获取当前位置数字
                int currentDigital = cells[j][i].getDigital();
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    // 记录数字
                    if (recordPreviousDigital == -1) {
                        recordPreviousDigital = currentDigital;
                    } else {
                        // 记录的之前的数字和当前数字不同
                        if (recordPreviousDigital != currentDigital) {
                            // 加入记录的数字
                            dataAfterSwipe.add(recordPreviousDigital);
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            // 记录得分
                            dataAfterSwipe.add(recordPreviousDigital * 2);
                            recordScore(recordPreviousDigital * 2);
                            // 重置记录数字
                            recordPreviousDigital = -1;
                        }
                    }
                }
            }

            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }

            // 补0
            int temp = gridColumnCount - dataAfterSwipe.size();
            for (int k = 0; k < temp; k++) {
                dataAfterSwipe.add(0);
            }
            Collections.reverse(dataAfterSwipe);
            // 若原始数据和移动后的数据不同，视为界面发生改变
            Collections.reverse(someData);
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();

            // 重新设置格子数据
            int index = 0;
            for (int p = 0; p < gridColumnCount; p++) {
                cells[p][i].setDigital(dataAfterSwipe.get(index++));
            }
            // 重置数据
            recordPreviousDigital = -1;
            dataAfterSwipe.clear();
        }
        if (needAddDigital) {
            // 添加一个随机数字（2或4）
            addDigital(false);
            playSound();
        }
        judgeOverOrAccomplish();
    }

    /**
     * 左滑
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void swipeLeft() {
        // 判断是否需要添加数字
        boolean needAddDigital = false;
        for (int i = 0; i < gridColumnCount; i++) {
            for (int j = 0; j < gridColumnCount; j++) {
                // 获取当前位置数字
                int currentDigital = cells[i][j].getDigital();
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    // 记录数字
                    if (recordPreviousDigital == -1) {
                        recordPreviousDigital = currentDigital;
                    } else {
                        // 记录的之前的数字和当前数字不同
                        if (recordPreviousDigital != currentDigital) {
                            // 加入记录的数字
                            dataAfterSwipe.add(recordPreviousDigital);
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            // 加入*2
                            dataAfterSwipe.add(recordPreviousDigital * 2);
                            // 记录得分
                            recordScore(recordPreviousDigital * 2);
                            // 重置记录数字
                            recordPreviousDigital = -1;
                        }
                    }
                }
            }

            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }

            // 补0
            for (int p = dataAfterSwipe.size(); p < gridColumnCount; p++) {
                dataAfterSwipe.add(0);
            }
            // 若原始数据和移动后的数据不同，视为界面发生改变
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();

            // 重新设置格子数据
            for (int k = 0; k < gridColumnCount; k++) {
                cells[i][k].setDigital(dataAfterSwipe.get(k));
            }
            // 每一行结束重置list
            dataAfterSwipe.clear();
            // 每一行结束重置记录数字
            recordPreviousDigital = -1;
        }
        if (needAddDigital) {
            // 添加一个随机数字（2或4）
            addDigital(false);
            playSound();
        }
        judgeOverOrAccomplish();
    }

    /**
     * 右滑
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void swipeRight() {
        // 判断是否需要添加数字
        boolean needAddDigital = false;
        for (int i = gridColumnCount - 1; i >= 0; i--) {
            for (int j = gridColumnCount - 1; j >= 0; j--) {
                // 获取当前位置数字
                int currentDigital = cells[i][j].getDigital();
                someData.add(currentDigital);
                if (currentDigital != 0) {
                    // 记录数字
                    if (recordPreviousDigital == -1) {
                        recordPreviousDigital = currentDigital;
                    } else {
                        // 记录的之前的数字和当前数字不同
                        if (recordPreviousDigital != currentDigital) {
                            // 加入记录的数字
                            dataAfterSwipe.add(recordPreviousDigital);
                            recordPreviousDigital = currentDigital;
                        } else {// 记录的之前的数字和当前的数字相同
                            // 加入*2
                            dataAfterSwipe.add(recordPreviousDigital * 2);
                            // 记录得分
                            recordScore(recordPreviousDigital * 2);
                            // 重置记录数字
                            recordPreviousDigital = -1;
                        }
                    }
                }
            }

            if (recordPreviousDigital != -1) {
                dataAfterSwipe.add(recordPreviousDigital);
            }

            // 补0
            int temp = gridColumnCount - dataAfterSwipe.size();
            for (int k = 0; k < temp; k++) {
                dataAfterSwipe.add(0);
            }
            Collections.reverse(dataAfterSwipe);
            // 若原始数据和移动后的数据不同，视为界面发生改变
            Collections.reverse(someData);
            if (!someData.equals(dataAfterSwipe)) {
                needAddDigital = true;
            }
            someData.clear();

            // 重新设置格子数据
            int index = 0;
            for (int p = 0; p < gridColumnCount; p++) {
                cells[i][p].setDigital(dataAfterSwipe.get(index++));
            }
            // 重置数据
            recordPreviousDigital = -1;
            dataAfterSwipe.clear();
        }
        if (needAddDigital) {
            // 添加一个随机数字（2或4）
            addDigital(false);
            playSound();
        }
        judgeOverOrAccomplish();
    }

    /**
     * 记录得分
     */
    private void recordScore(int score) {
        Intent intent = new Intent(ACTION_RECORD_SCORE);
        intent.putExtra(KEY_SCORE, score);
        getContext().sendBroadcast(intent);
    }

    /**
     * 检查游戏是否结束或达成游戏目标
     */
    private void judgeOverOrAccomplish() {
        // 判断游戏结束的标识
        boolean isOver = true;

        // 判断游戏是否结束
        // 格子都不为空且相邻的格子数字不同
        over:
        for (int i = 0; i < gridColumnCount; i++) {
            for (int j = 0; j < gridColumnCount; j++) {
                // 有空格子，游戏还可以继续
                if (cells[i][j].getDigital() == 0) {
                    isOver = false;
                    break over;
                }
                // 判断左右上下有没有相同的
                if (j < gridColumnCount - 1) {
                    if (cells[i][j].getDigital() == cells[i][j + 1].getDigital()) {
                        isOver = false;
                        break over;
                    }
                }
                if (i < gridColumnCount - 1) {
                    if (cells[i][j].getDigital() == cells[i + 1][j].getDigital()) {
                        isOver = false;
                        break over;
                    }
                }
            }
        }

        // 游戏结束，弹出提示框
        if (isOver) {
            canSwipe = false;
            sendGameOverMsg(ACTION_LOSE);
        }

        // 经典模式下才判赢
        if (gameMode == 0) {
            // 判断是否达成游戏目标
            for (int i = 0; i < gridColumnCount; i++) {
                for (int j = 0; j < gridColumnCount; j++) {
                    // 有一个格子数字到达2048则视为达成目标
                    if (cells[i][j].getDigital() == 2048) {
                        canSwipe = false;
                        int currentTime = ConfigManager.getGoalTime(getContext()) + 1;
                        ConfigManager.putGoalTime(getContext(), currentTime);
                        Config.GetGoalTime = currentTime;
                        sendGameOverMsg(ACTION_WIN);
                    }
                }
            }
        }
    }
    private void sendGameOverMsg(String action) {
        Intent intent = new Intent(action);
        if (action.equals(ACTION_WIN)) {
            intent.putExtra(KEY_RESULT, "You Win!");
        } else {
            intent.putExtra(KEY_RESULT, "You Lose!");
        }
        getContext().sendBroadcast(intent);
    }
    private int getOrientation(float offsetX, float offsetY) {
        // X轴移动
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            if (offsetX > MIN_DIS) {
                return 0;
            } else if (offsetX < -MIN_DIS) {
                return 1;
            } else {
                return -1;
            }
        } else {// Y轴移动
            if (offsetY > MIN_DIS) {
                return 2;
            } else if (offsetY < -MIN_DIS) {
                return 3;
            } else {
                return -1;
            }
        }
    }
}
