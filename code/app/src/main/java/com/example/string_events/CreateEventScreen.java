package com.example.string_events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateEventScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event_screen);

        ImageButton backButton = findViewById(R.id.back_button);
        EditText eventTitleEditText = findViewById(R.id.event_title_editText);
        ImageButton addEventPhotoButton = findViewById(R.id.add_photo_button);
        ImageView eventPhotoImageView = findViewById(R.id.event_photo);
        EditText eventDescriptionEditText = findViewById(R.id.event_description_editText);
        ImageButton addEventTagsButton = findViewById(R.id.add_tags_button);
        EditText eventStartDateEditText = findViewById(R.id.event_start_date_editText);
        EditText eventStartTimeEditText = findViewById(R.id.event_start_time_editText);
        EditText eventEndDateEditText = findViewById(R.id.event_end_date_editText);
        EditText eventEndTimeEditText = findViewById(R.id.event_end_time_editText);
        EditText eventLocationEditText = findViewById(R.id.event_location_editText);
        EditText registrationStartDateEditText = findViewById(R.id.registration_start_date_editText);
        EditText registrationStartTimeEditText = findViewById(R.id.registration_start_time_editText);
        EditText registrationEndDateEditText = findViewById(R.id.registration_end_date_editText);
        EditText registrationEndTimeEditText = findViewById(R.id.registration_end_time_editText);

        EditText eventAttendantsEditText = findViewById(R.id.event_attendants_editText);
        EditText eventWaitlistEdittext = findViewById(R.id.event_waitlist_editText);
        SwitchCompat eventGeolocationSwitch = findViewById(R.id.geolocation_switch);
        ImageButton eventVisibilityPublic = findViewById(R.id.event_public_button);
        ImageButton eventVisibilityPrivate = findViewById(R.id.event_private_button);
        // needs to be atomic boolean to avoid an error
        AtomicBoolean visibility = new AtomicBoolean(false);

        // putting a bunch of similar editTexts into arrays so we can simplify repeated code
        EditText[] dateEditTexts = {
                eventStartDateEditText, eventEndDateEditText,
                registrationStartDateEditText, registrationEndDateEditText};
        EditText[] timeEditTexts = {
                eventStartTimeEditText, eventEndTimeEditText,
                registrationStartTimeEditText, registrationEndTimeEditText};

        ImageButton doneButton = findViewById(R.id.done_button);

        for (EditText editText : dateEditTexts) {
            editText.setOnClickListener(view ->
                    showDateDialog(CreateEventScreen.this, editText));
        }
        for (EditText editText : timeEditTexts) {
            editText.setOnClickListener(view ->
                    showTimeDialog(CreateEventScreen.this, editText));
        }

        backButton.setOnClickListener(view -> finish());

        // create an ActivityResultLauncher for picking an image
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // get the intent of the result
                Intent intent = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && intent != null) {
                    // get the actual uri stored in the intent
                    Uri selectedImageUri = intent.getData();
                    eventPhotoImageView.setImageURI(selectedImageUri);
                    eventPhotoImageView.setTag(selectedImageUri);
                }
            }
        );

        addEventPhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            // call the ActivityResultLauncher with the intent to pick an image
            imagePickerLauncher.launch(intent);
        });

        eventVisibilityPublic.setOnClickListener(view -> visibility.set(true));
        eventVisibilityPrivate.setOnClickListener(view -> visibility.set(false));

        doneButton.setOnClickListener(view -> {
            String title = String.valueOf(eventTitleEditText.getText());
            Uri photo = (Uri)eventPhotoImageView.getTag();
            String description = String.valueOf(eventDescriptionEditText);
            ArrayList<String> tags = null; // TODO

            LocalDateTime startDateTime = LocalDate.parse(
                    String.valueOf(eventStartDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(eventStartTimeEditText.getText())));

            LocalDateTime endDateTime = LocalDate.parse(
                    String.valueOf(eventEndDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(eventEndTimeEditText.getText())));

            String location = String.valueOf(eventLocationEditText.getText());

            LocalDateTime registrationStartDateTime = LocalDate.parse(
                    String.valueOf(registrationStartDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(registrationStartTimeEditText.getText())));

            LocalDateTime registrationEndDateTime = LocalDate.parse(
                    String.valueOf(registrationEndDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(registrationEndTimeEditText.getText())));

            int numOfAttendants = Integer.parseInt(eventAttendantsEditText.getText().toString());
            int waitlistLimit = Integer.parseInt(eventWaitlistEdittext.getText().toString());
            boolean geolocationRequirement = eventGeolocationSwitch.isChecked();

            Event newEvent = new Event(title, photo, description, tags,
                    startDateTime, endDateTime, location,
                    registrationStartDateTime, registrationEndDateTime,
                    numOfAttendants, waitlistLimit, geolocationRequirement, visibility.get());

            // TODO link the newly created event to database and other screens
        });
    }

    private void showDateDialog(Context context, EditText dateEditText) {
        // get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // create a new date picker popup dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                // pass context
                context,
                // create a DatePicker
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // set the EditText in the screen to the selected date padding with 0s if needed
                    dateEditText.setText(String.format(Locale.CANADA,"%02d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay));
                },
                // pass the current date to the datePicker so the default date is the current date
                year, month, day);
        datePickerDialog.show();
    }

    private void showTimeDialog(Context context, EditText timeEditText) {
        // get the current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // create a new time picker popup dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                // pass context
                context,
                // create a TimePicker
                (timePicker, selectedHour, selectedMinute) -> {
                    // set the EditText in the screen to the selected time padding with 0s if needed
                    timeEditText.setText(String.format(Locale.CANADA, "%02d:%02d", selectedHour, selectedMinute));
                },
                // pass the current time to the timePicker so the default time is the current time
                hour, minute, false);
        timePickerDialog.show();
    }
}