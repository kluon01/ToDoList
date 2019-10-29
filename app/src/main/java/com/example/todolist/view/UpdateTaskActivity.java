package com.example.todolist.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.todolist.receivers.AlertReceiver;
import com.example.todolist.model.DatabaseClient;
import com.example.todolist.R;
import com.example.todolist.model.Task;
import com.example.todolist.tools.DateConverter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class UpdateTaskActivity extends AppCompatActivity {

    private EditText editTextTask, editTextDesc, editTextDate, editTextTime;
    private CheckBox checkBoxFinished;
    private AlertReceiver alertReceiver;
    private DateConverter dateConverter;
    private TimePickerDialog timePickerDialog;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_task);

        // Initializations
        alertReceiver = new AlertReceiver();
        dateConverter = new DateConverter();

        editTextTask = findViewById(R.id.editTextTask);
        editTextDesc = findViewById(R.id.editTextDesc);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);

        checkBoxFinished = findViewById(R.id.checkBoxFinished);

        // Set Time Picker
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog = new TimePickerDialog(UpdateTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        // Set Date Picker
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

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(UpdateTaskActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        final Task task = (Task) getIntent().getSerializableExtra("task");

        loadTask(task);

        findViewById(R.id.button_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask(task);
            }
        });

        findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateTaskActivity.this);
                builder.setTitle("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTask(task);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog ad = builder.create();
                ad.show();
            }
        });
    }

    // Load task values into text fields
    private void loadTask(Task task) {
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat time = new SimpleDateFormat("hh:mm aa");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getFinishBy());
        editTextTask.setText(task.getTask());
        editTextDesc.setText(task.getDesc());
        editTextDate.setText(date.format(calendar.getTime()));
        editTextTime.setText(time.format(calendar.getTime()));
        checkBoxFinished.setChecked(task.isFinished());
    }

    // Update the task
    private void updateTask(final Task task) {
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

        class UpdateTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                // Cancel and reset alarm if finished box is not checked
                if(!checkBoxFinished.isChecked()) {
                    alertReceiver.cancelToDoAlarm(UpdateTaskActivity.this,task.getId());
                    alertReceiver.startToDoAlarm(UpdateTaskActivity.this, dateConverter.dateToMilliseconds(sFinishBy), task.getId(), sTask, sDesc);
                }
                task.setTask(sTask);
                task.setDesc(sDesc);
                task.setFinishBy(dateConverter.dateToMilliseconds(sFinishBy));
                task.setFinished(checkBoxFinished.isChecked());
                // Update database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .taskDao()
                        .update(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
                finishAffinity();
                startActivity(new Intent(UpdateTaskActivity.this, MainActivity.class));
            }
        }

        UpdateTask ut = new UpdateTask();
        ut.execute();
    }

    // Delete the task
    private void deleteTask(final Task task) {

        class DeleteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                // Cancel the alarm
                alertReceiver.cancelToDoAlarm(UpdateTaskActivity.this,task.getId());
                // Delete task from database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .taskDao()
                        .delete(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                finishAffinity();
                startActivity(new Intent(UpdateTaskActivity.this, MainActivity.class));
            }
        }

        DeleteTask dt = new DeleteTask();
        dt.execute();
    }
}