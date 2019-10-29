package com.example.todolist.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.todolist.receivers.AlertReceiver;
import com.example.todolist.model.DatabaseClient;
import com.example.todolist.R;
import com.example.todolist.model.Task;
import com.example.todolist.tools.ConnectionChecker;
import com.example.todolist.tools.DateConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;


public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTask, editTextDesc, editTextDate, editTextTime;
    private TimePickerDialog timePickerDialog;
    private final Calendar myCalendar = Calendar.getInstance();
    private AlertReceiver alertReceiver;
    private DateConverter dateConverter;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initializations
        alertReceiver = new AlertReceiver();
        dateConverter = new DateConverter();

        isConnected = ((ConnectionChecker)this.getApplication()).getConnected();

        editTextTask = findViewById(R.id.editTextTask);
        editTextDesc = findViewById(R.id.editTextDesc);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);

        // Open time picker when user taps on time field
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog = new TimePickerDialog(AddTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        boolean amPm;
                        if (hourOfDay >= 12) {
                            amPm = true;
                        } else {
                            amPm = false;
                        }
                        editTextTime.setText(String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minutes, amPm ? "PM" : "AM"));
                    }
                }, 0, 0, false);
                timePickerDialog.show();
            }
        });

        // Open date picker when user taps on date field
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
                editTextDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        // Set date text
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddTaskActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Save button logic
        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected == true) {
                    saveTask();
                } else {
                    saveTaskOffline();
                }

            }
        });
    }

    // Online save function
    private void saveTask() {

        final String sTask = editTextTask.getText().toString().trim();
        final String sDesc = editTextDesc.getText().toString().trim();
        final String sDate = editTextDate.getText().toString().trim();
        final String sTime = editTextTime.getText().toString().trim();
        final String sFinishBy = sDate + " " + sTime;

        if (sTask.isEmpty()) {
            editTextTask.setError("Task required");
            editTextTask.requestFocus();
            return;
        }

        if (sDesc.isEmpty()) {
            editTextDesc.setError("Desc required");
            editTextDesc.requestFocus();
            return;
        }

        if (sDate.isEmpty()) {
            editTextDate.setError("Date required");
            editTextDate.requestFocus();
            return;
        }

        if (sTime.isEmpty()) {
            editTextTime.setError("Time required");
            editTextTime.requestFocus();
            return;
        }


        class SaveTask extends AsyncTask<Void, Void, Void> {
            private long addedTaskID;

            @Override
            protected Void doInBackground(Void... voids) {

                // Creating a new task
                Task task = new Task();
                task.setTask(sTask);
                task.setDesc(sDesc);
                task.setFinishBy(dateConverter.dateToMilliseconds(sFinishBy));
                task.setFinished(false);

                // Adding to database
                addedTaskID = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .taskDao()
                        .insert(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                // Set alarm on post
                alertReceiver.startToDoAlarm(AddTaskActivity.this, dateConverter.dateToMilliseconds(sFinishBy), addedTaskID, sTask, sDesc);
                finishAffinity();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }

    // Offline save function(not enough time to fully implement)
    private void saveTaskOffline() {
        final String osTask = editTextTask.getText().toString().trim();
        final String osDesc = editTextDesc.getText().toString().trim();
        final String osDate = editTextDate.getText().toString().trim();
        final String osTime = editTextTime.getText().toString().trim();
        final String osFinishBy = osDate + " " + osTime;
        final String FILE_NAME = "offline_tasks.txt";
        File file = new File(this.getFilesDir(), FILE_NAME);
        FileWriter fileWriter;
        FileReader fileReader;
        BufferedWriter bufferedWriter;
        BufferedReader bufferedReader;
        String response;

        // Check if file exists
        if(!file.exists()) {
            try {
                file.createNewFile();
                fileWriter = new FileWriter(file.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("{}");
                bufferedWriter.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // Create a new JSON Object and write to the file
        JSONObject task = new JSONObject();
        try {
            task.put("task", osTask);
            task.put("desc", osDesc);
            task.put("finishBy", dateConverter.dateToMilliseconds(osFinishBy));
        } catch(JSONException e) {
            e.printStackTrace();
        }
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(task.toString());
            bw.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        Toast.makeText(getApplicationContext(), "Saved Offline", Toast.LENGTH_LONG).show();
    }

}