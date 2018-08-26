package com.dmu.sash.planechase;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageButton dieButton, undoButton;
    private int position;
    private ArrayList<Integer> planesArray;
    private Resources resources;
    private ConstraintLayout constraintLayout;
    private int lastPlane;
    private TextView manaToPay;
    private int mana = 0;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        layout = findViewById(R.id.layout);

        Field[] fields = R.drawable.class.getFields();
        planesArray = new ArrayList<>();
        position = -1;
        resources = getBaseContext().getResources();
        for (Field field : fields) {
            try {
                if (field.getName().contains("plane")) {
                    planesArray.add(field.getInt(null));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        constraintLayout = findViewById(R.id.layout);

        imageView = findViewById(R.id.plane);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                planeswalk();
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                position = -1;
                planeswalk();
                mana = 0;
                manaToPay.setText("" + mana);
                return true;
            }
        });

        dieButton = findViewById(R.id.planechase_die);
        dieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollDie();
            }
        });

        undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLastPlane();
            }
        });

        manaToPay = findViewById(R.id.manaToPay);
        manaToPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mana = 0;
                manaToPay.setText("" + mana);
            }
        });

        //Call initial plane to be shown.
        planeswalk();

    }

    private void goToLastPlane() {
        if (position > 0){
            position--;
            goToPlane(position);
        }
    }

    private void goToPlane(int i) {
        imageView.setImageResource(planesArray.get(i));
        Bitmap bmp = BitmapFactory.decodeResource(resources, planesArray.get(i));
        int color = getAverageColor(bmp);
        constraintLayout.setBackgroundColor(color);
        dieButton.setColorFilter(color);
        showHideUndo();
    }

    private void showHideUndo() {
        if (position > 0){
            undoButton.setVisibility(View.VISIBLE);
        } else {
            undoButton.setVisibility(View.GONE);
        }
    }

    private void planeswalk() {
        if (position == -1) {
            Collections.shuffle(planesArray);
            if (planesArray.get(0) == lastPlane){
                Collections.shuffle(planesArray);
                planeswalk();
            } else {
                position++;
                goToPlane(position);
            }
        } else if (position < planesArray.size() - 1) {
            position++;
            goToPlane(position);
        } else {
            lastPlane = planesArray.get(position);
            position = -1;
            planeswalk();
        }
    }

    private void rollDie() {
        int roll = new Random().nextInt(6) + 1;
        if (roll == 1) {
            chaos();
        } else if (roll == 6) {
            planeswalkRolled();
        } else {
            dieButton.setImageResource(android.R.color.transparent);
            increaseMana();
        }
    }

    private void planeswalkRolled() {
        dieButton.setImageResource(R.drawable.walk);
        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(this))
                .setTitle("You rolled planeswalk!")
                .setMessage("Planeswalk to the next plane.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        layout.setClickable(true);
                        planeswalk();
                    }
                })
                .create();
        alertDialog.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        layout.setClickable(false);
        alertDialog.show();
        increaseMana();
    }

    private void increaseMana() {
        mana++;
        manaToPay.setText("" + mana);
    }

    private void chaos() {
        dieButton.setImageResource(R.drawable.chaos);

        AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(this))
                .setTitle("You rolled chaos!")
                .setMessage("Trigger the chaos ability of the plane.")
                .setPositiveButton("OK", listener)
                .create();
        alertDialog.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        layout.setClickable(false);
        increaseMana();
    }
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            layout.setClickable(true);
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    public int getAverageColor(Bitmap bmp) {
        Palette p = Palette.from(bmp).generate();
        return p.getDominantColor(0);
    }
}

