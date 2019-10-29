package com.example.todolist.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class DateConverter extends AppCompatActivity {

    public long dateToMilliseconds(String dateTimeInput)
    {
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm aa");
            long dateInMilliseconds;
            Date date = df.parse(dateTimeInput);
            dateInMilliseconds = date.getTime();
            return dateInMilliseconds;
        } catch(ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String millisecondsToDate(long millisecondInput) {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm aa");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millisecondInput);
        return df.format(cal.getTime());
    }
}
