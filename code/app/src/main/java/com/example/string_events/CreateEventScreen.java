package com.example.string_events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Screen for creating a new event.
 * <p>
 * Handles user input for event details, image selection and upload to Firebase Storage,
 * and persisting the event document to Firestore.
 */
public class CreateEventScreen extends AppCompatActivity {
    /** Firestore entry point used to persist event data. */
    private FirebaseFirestore db;

    /**
     * Initializes view bindings, date/time pickers, image picker, visibility toggles,
     * and submits the form to create and upload a new {@link Event}.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event_screen);
        db = FirebaseFirestore.getInstance();

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
        SwitchCompat eventAutoRollingSwitch = findViewById(R.id.auto_rolling_switch);
        ImageButton eventVisibilityPublic = findViewById(R.id.event_public_button);
        ImageButton eventVisibilityPrivate = findViewById(R.id.event_private_button);
        // needs to be atomic boolean to avoid an error
        AtomicBoolean visibility = new AtomicBoolean(false);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

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
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        // get the actual uri stored in the intent
                        Uri selectedImageUri = null;
                        if (intent != null) {
                            // sometimes the image is handled with Data and sometimes with ClipData
                            if (intent.getData() != null) {
                                selectedImageUri = intent.getData();
                            }
                            else if (intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
                                ClipData clipData = intent.getClipData();
                                selectedImageUri = clipData.getItemAt(0).getUri();
                            }
                            if (selectedImageUri != null) {
                                eventPhotoImageView.setImageURI(selectedImageUri);
                                eventPhotoImageView.setTag(selectedImageUri);
                            }
                        }
                    }
                });

        addEventPhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            // call the ActivityResultLauncher with the intent to pick an image
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        eventVisibilityPublic.setOnClickListener(view -> {
            eventVisibilityPublic.setBackgroundResource(R.drawable.public_button_clicked);
            eventVisibilityPrivate.setBackgroundResource(R.drawable.private_button);
            visibility.set(true);
        });
        eventVisibilityPrivate.setOnClickListener(view -> {
            eventVisibilityPrivate.setBackgroundResource(R.drawable.private_button_clicked);
            eventVisibilityPublic.setBackgroundResource(R.drawable.public_button);
            visibility.set(false);
        });

        doneButton.setOnClickListener(view -> {
            String title = String.valueOf(eventTitleEditText.getText());
            Uri photo = (Uri)eventPhotoImageView.getTag();

            String description = String.valueOf(eventDescriptionEditText.getText());
            ArrayList<String> tags = null; // TODO

            // converts the text in the editText into a LocalDateTime and then into a ZonedDateTime (has timezone)
            ZonedDateTime startDateTime = LocalDate.parse(
                            String.valueOf(eventStartDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(eventStartTimeEditText.getText())))
                    .atZone(ZoneId.systemDefault());

            ZonedDateTime endDateTime = LocalDate.parse(
                            String.valueOf(eventEndDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(eventEndTimeEditText.getText())))
                    .atZone(ZoneId.systemDefault());

            String location = String.valueOf(eventLocationEditText.getText());

            ZonedDateTime registrationStartDateTime = LocalDate.parse(
                            String.valueOf(registrationStartDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(registrationStartTimeEditText.getText())))
                    .atZone(ZoneId.systemDefault());

            ZonedDateTime registrationEndDateTime = LocalDate.parse(
                            String.valueOf(registrationEndDateEditText.getText()))
                    .atTime(LocalTime.parse(String.valueOf(registrationEndTimeEditText.getText())))
                    .atZone(ZoneId.systemDefault());

            int maxAttendees = Integer.parseInt(eventAttendantsEditText.getText().toString());
            int waitlistLimit = Integer.parseInt(eventWaitlistEdittext.getText().toString());
            boolean geolocationRequirement = eventGeolocationSwitch.isChecked();
            boolean autoRoll = eventAutoRollingSwitch.isChecked();

            Event newEvent = new Event(username, title, photo, description, tags,
                    startDateTime, endDateTime, location,
                    registrationStartDateTime, registrationEndDateTime,
                    maxAttendees, waitlistLimit, geolocationRequirement, visibility.get());

            uploadNewEventToDatabase(newEvent);
        });
    }

    /**
     * Opens a {@link DatePickerDialog} and writes the selected date into the given {@link EditText}
     * using {@code yyyy-MM-dd} formatting.
     *
     * @param context     dialog context
     * @param dateEditText target input to receive the formatted date
     */
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

    /**
     * Opens a {@link TimePickerDialog} and writes the selected time into the given {@link EditText}
     * using {@code HH:mm} formatting (24-hour).
     *
     * @param context      dialog context
     * @param timeEditText target input to receive the formatted time
     */
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

    /**
     * Uploads the event image to Firebase Storage; upon success, obtains the download URL
     * and delegates persistence of the event document to {@link #saveEventDetailsToDatabase(Event, String)}.
     *
     * @param event event to be uploaded and saved
     */
    private void uploadNewEventToDatabase(Event event) {
        Toast.makeText(CreateEventScreen.this, "Please wait...", Toast.LENGTH_SHORT).show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("event_images/" + UUID.randomUUID().toString() + ".png");

        // start file upload
        imageRef.putFile(event.getPhotoUri()).addOnSuccessListener(taskSnapshot -> {
                    // wait for the image to upload first before getting the download url
                    imageRef.getDownloadUrl().addOnSuccessListener(photoUri -> {
                        // get the download url of the photo
                        String downloadUrl = photoUri.toString();
                        Log.d("UploadImage", "image uploaded successfully url: " + downloadUrl);

                        // call helper function to fill in the event details and upload event to database
                        saveEventDetailsToDatabase(event, downloadUrl);
                    }).addOnFailureListener(e -> Log.e("UploadImage", "couldn't get image download url: " + e.getMessage()));
                }).addOnFailureListener(e -> Log.e("UploadImage", "couldn't upload image: " + e.getMessage()));
    }

    /**
     * Builds a Firestore document from the provided {@link Event} and stores it under
     * the {@code events} collection using the event's ID. Shows a toast on success/failure
     * and triggers a lightweight image retrieval test via {@link #testingImageGet(Event)}.
     *
     * @param event    the event whose fields populate the document
     * @param imageUrl optional image download URL to persist (ignored if {@code null})
     */
    // Helper function to handle saving the document to Firestore
    private void saveEventDetailsToDatabase(Event event, String imageUrl) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("creator", event.getEventCreator());
        doc.put("title", event.getTitle());
        // only add the image url if it's not null
        if (imageUrl != null) {
            doc.put("imageUrl", imageUrl);
        }
        doc.put("description", event.getDescription());
        doc.put("categories", "TODO");
        // need to convert the ZonedDateTime type into a Date type so it can be stored properly as a timestamp
        doc.put("startAt", Date.from(event.getStartDateTime().toInstant()));
        doc.put("endAt", Date.from(event.getEndDateTime().toInstant()));
        doc.put("location", event.getLocation());
        doc.put("regStartAt", Date.from(event.getRegistrationStartDateTime().toInstant()));
        doc.put("regEndAt", Date.from(event.getRegistrationEndDateTime().toInstant()));
        doc.put("attendeesCount", -1);
        doc.put("maxAttendees", event.getMaxAttendees());
        doc.put("attendees", event.getAttendees()); // when creating a new event, this attendees list is empty
        doc.put("invited", event.getInvited()); // when creating a new event, this invited list is empty
        doc.put("waitlist", event.getWaitlist()); // when creating a new event, this waitlist is empty
        doc.put("lotteryRolled", event.isLotteryRolled()); // when creating a new event, lotteryRolled is set to false
        doc.put("waitlistLimit", event.getWaitlistLimit());
        doc.put("geolocationReq", event.getGeolocationRequirement());
        doc.put("visibility", event.getEventVisibility());

        // creating new event in database under collection "events"
        db.collection("events").document(event.getEventId()).set(doc)
                .addOnSuccessListener(v -> {
                    Toast.makeText(CreateEventScreen.this, "Event Created!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "event uploaded to database");
                    // testing function that replaces the image button on the screen with the database uploaded image
                    // this is just to test that we can get the image back from the database and use it in the app
                    testingImageGet(event);
                    Intent intent = new Intent(CreateEventScreen.this, OrganizerEventDetails.class);
                    intent.putExtra("eventId", event.getEventId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventScreen.this, "Error creating event in database", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "couldn't upload event to database", e);
                });
    }

    /**
     * Debug helper that fetches the event document and, if an image URL exists,
     * loads it into the "add photo" button using Glide to confirm retrieval works.
     *
     * @param event the event whose document is retrieved
     */
    public void testingImageGet(Event event) {
        // get the specified event from the database
        DocumentReference documentReference = db.collection("events").document(event.getEventId());
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // getting the imageUrl from the specified event's field
                String imageUrl = documentSnapshot.getString("imageUrl");
                if (imageUrl != null) {
                    // load the image from the imageUrl into an ImageButton (can also be an ImageView)
                    ImageButton addPhotoButton = findViewById(R.id.add_photo_button);
                    Glide.with(CreateEventScreen.this)
                            .load(imageUrl)
                            .into(addPhotoButton);
                }
            } else {
                Log.d("Firestore", "document doesn't exist");
            }
        });
    }
}
