package com.example.joans.timetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
    ALL CODED BY TEAM
**/

public class NewActivity extends AppCompatActivity {

    private Button snack;

    private final String tag = this.getClass().getSimpleName();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.getMenu().clear();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.create_activity);
            //Previous view
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Log.i(tag, "NewActivity!");
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        final RadioButton taskRadioButton =(RadioButton) findViewById(R.id.taskButton);
        final RadioButton projectRadioButton =(RadioButton) findViewById(R.id.projectButton);
        Button acceptButton = (Button) findViewById(R.id.button);
        final LinearLayout taskConfiguration = (LinearLayout) findViewById(R.id.task_configuration);
        final EditText duration = (EditText) findViewById(R.id.editText2);
        final EditText startIn = (EditText) findViewById(R.id.editText);
        final CheckBox limitedTaskCheckBox = (CheckBox) findViewById(R.id.limitedTask);
        final CheckBox progTaskCheckbox = (CheckBox) findViewById(R.id.progTask);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (projectRadioButton.isChecked()){
                    EditText projectName = (EditText) findViewById(R.id.nameInput);
                    EditText projectDescription = (EditText) findViewById(R.id.descInput);
                    Intent dataRecieved = new Intent("crear_projecte");
                    Log.i(tag, "1");
                    dataRecieved.putExtra("projectName",projectName.getText().toString());
                    Log.i(tag, "2");
                    dataRecieved.putExtra("projectDescription",projectDescription.getText().toString());
                    Log.i(tag, "3");
                    sendBroadcast(dataRecieved);
                    Log.i(tag, "4");
                    startActivity(new Intent(NewActivity.this, LlistaActivitatsActivity.class));
                }else if (taskRadioButton.isChecked()){

                    EditText projectName = (EditText) findViewById(R.id.nameInput);
                    EditText projectDescription = (EditText) findViewById(R.id.descInput);
                    Intent dataRecieved = new Intent("crear_tasca");
                    dataRecieved.putExtra("taskName",projectName.getText().toString());
                    dataRecieved.putExtra("taskDescription",projectDescription.getText().toString());
                    sendBroadcast(dataRecieved);
                    startActivity(new Intent(NewActivity.this, LlistaActivitatsActivity.class));


                }
            }
        });
        projectRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert taskConfiguration != null;
                taskConfiguration.setVisibility(View.INVISIBLE);
            }
        });
        taskRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert taskConfiguration != null;
                taskConfiguration.setVisibility(View.VISIBLE);
                duration.setVisibility(View.GONE);
                startIn.setVisibility(View.GONE);
            }
        });



        limitedTaskCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {

                    duration.setVisibility(View.VISIBLE);

                }
                else
                {
                    duration.setVisibility(View.GONE);


                }
            }
        });

        progTaskCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    startIn.setVisibility(View.VISIBLE);

                }
                else
                {
                    startIn.setVisibility(View.GONE);

                }
            }
        });

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
