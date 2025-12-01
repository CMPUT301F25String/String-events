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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

import com.google.zxing.WriterException;
import com.google.firebase.storage.UploadTask;

/**
 * Screen for creating a new event.
 * <p>
 * Handles user input for event details, image selection and upload to Firebase Storage,
 * and persisting the event document to Firestore.
 */
public class CreateEventScreen extends AppCompatActivity {
    /** Firestore entry point used to persist event data. */
    private FirebaseFirestore db;
    private final HashSet<String> selectedTags = new HashSet<>();

    ImageButton backButton;
    EditText eventTitleEditText;
    ImageButton addEventPhotoButton;
    ImageView eventPhotoImageView;
    EditText eventDescriptionEditText;
    ImageButton addEventTagsButton;
    TextView tagsDisplayText;
    EditText eventStartDateEditText;
    EditText eventStartTimeEditText;
    EditText eventEndDateEditText;
    EditText eventEndTimeEditText;
    EditText eventLocationEditText;
    EditText registrationStartDateEditText;
    EditText registrationStartTimeEditText;
    EditText registrationEndDateEditText;
    EditText registrationEndTimeEditText;

    EditText eventAttendantsEditText;
    EditText eventWaitlistEdittext;
    SwitchCompat eventGeolocationSwitch;
    ImageButton eventVisibilityPublic;
    ImageButton eventVisibilityPrivate;

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

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null && !eventId.isEmpty()) {
            // if an eventId is passed in, we should use the id to populate the screen
            populateEventDetails(eventId);
        }

        backButton = findViewById(R.id.back_button);
        eventTitleEditText = findViewById(R.id.event_title_editText);
        addEventPhotoButton = findViewById(R.id.add_photo_button);
        eventPhotoImageView = findViewById(R.id.event_photo);
        eventDescriptionEditText = findViewById(R.id.event_description_editText);
        addEventTagsButton = findViewById(R.id.add_tags_button);
        tagsDisplayText = findViewById(R.id.display_tags_text);
        eventStartDateEditText = findViewById(R.id.event_start_date_editText);
        eventStartTimeEditText = findViewById(R.id.event_start_time_editText);
        eventEndDateEditText = findViewById(R.id.event_end_date_editText);
        eventEndTimeEditText = findViewById(R.id.event_end_time_editText);
        eventLocationEditText = findViewById(R.id.event_location_editText);
        registrationStartDateEditText = findViewById(R.id.registration_start_date_editText);
        registrationStartTimeEditText = findViewById(R.id.registration_start_time_editText);
        registrationEndDateEditText = findViewById(R.id.registration_end_date_editText);
        registrationEndTimeEditText = findViewById(R.id.registration_end_time_editText);

        eventAttendantsEditText = findViewById(R.id.event_attendants_editText);
        eventWaitlistEdittext = findViewById(R.id.event_waitlist_editText);
        eventGeolocationSwitch = findViewById(R.id.geolocation_switch);
        eventVisibilityPublic = findViewById(R.id.event_public_button);
        eventVisibilityPrivate = findViewById(R.id.event_private_button);
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

        // create an ActivityResultLauncher for selecting event tags
        ActivityResultLauncher<Intent> tagsLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedTags.clear();
                        String[] arr = data.getStringArrayExtra(EventFilterActivity.EXTRA_TAGS);
                        assert arr != null;
                        java.util.Collections.addAll(selectedTags, arr);

                        // build the display string and add the tags to it
                        StringBuilder displayText = new StringBuilder();
                        if (!selectedTags.isEmpty()) {
                            displayText.append(android.text.TextUtils.join(", ", selectedTags));
                            tagsDisplayText.setText(displayText.toString());
                            tagsDisplayText.setVisibility(View.VISIBLE);
                        }
                        else {
                            tagsDisplayText.setVisibility(View.GONE);
                        }
                    }
                });

        addEventTagsButton.setOnClickListener(view -> {
            Intent it = new Intent(this, EventFilterActivity.class);
            it.putExtra(EventFilterActivity.EXTRA_TAGS, selectedTags.toArray(new String[0]));
            it.putExtra("action", "tags");
            tagsLauncher.launch(it);
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
            // validate user inputs first before continuing
            if (!validateInputs()) {
                return;
            }

            String title = String.valueOf(eventTitleEditText.getText());
            Uri photo;
            if (eventPhotoImageView.getTag() instanceof Uri) {
                photo = (Uri) eventPhotoImageView.getTag();
            } else {
                photo = Uri.parse((String) eventPhotoImageView.getTag());
            }
            String description = String.valueOf(eventDescriptionEditText.getText());

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

            ArrayList<String> tags = new ArrayList<>(selectedTags);
            Event newEvent = new Event(eventId, username, title, photo, description, tags,
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
        Object photoData = eventPhotoImageView.getTag(); // this can be a Uri (new image) or a String (existing URL)

        if (photoData instanceof Uri) {
            // new image was selected so we must upload it
            Toast.makeText(CreateEventScreen.this, "Please wait...", Toast.LENGTH_SHORT).show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("event_images/" + event.getEventId() + ".png");

            imageRef.putFile(event.getPhotoUri())
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get its public download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            Log.d("Upload", "New image uploaded successfully. URL: " + downloadUrl);
                            // Now, save the event details WITH THE NEW image URL.
                            saveEventDetailsToDatabase(event, downloadUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("Upload", "Failed to get download URL: " + e.getMessage());
                            Toast.makeText(this, "Error getting image URL", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Upload", "Image upload failed: " + e.getMessage());
                        Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    });

        } else if (photoData instanceof String) {
            // image was not changed so the photoData is just the existing URL string
            String existingImageUrl = (String) photoData;
            Log.d("Upload", "Image not changed. Reusing existing URL: " + existingImageUrl);
            // skip the upload and save the event details directly, passing the existing URL
            saveEventDetailsToDatabase(event, existingImageUrl);

        } else {
            Log.w("Upload", "No valid image provided. Saving event without an image.");
            saveEventDetailsToDatabase(event, null);
        }
    }

    /**
     * Builds a Firestore document from the provided {@link Event} and stores it under
     * the {@code events} collection using the event's ID. Shows a toast on success/failure
     * and triggers a lightweight image retrieval test via.
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
        doc.put("categories", event.getTags());
        // need to convert the ZonedDateTime type into a Date type so it can be stored properly as a timestamp
        doc.put("startAt", Date.from(event.getStartDateTime().toInstant()));
        doc.put("endAt", Date.from(event.getEndDateTime().toInstant()));
        doc.put("location", event.getLocation());
        doc.put("regStartAt", Date.from(event.getRegistrationStartDateTime().toInstant()));
        doc.put("regEndAt", Date.from(event.getRegistrationEndDateTime().toInstant()));
        doc.put("attendeesCount", 0);
        doc.put("maxAttendees", event.getMaxAttendees());
        doc.put("attendees", event.getAttendees()); // when creating a new event, this attendees list is empty
        doc.put("invited", event.getInvited()); // when creating a new event, this invited list is empty
        doc.put("waitlist", event.getWaitlist()); // when creating a new event, this waitlist is empty
        doc.put("cancelled", event.getCancelled()); // when creating a new event, this cancelled list is empty
        doc.put("lotteryRolled", event.isLotteryRolled()); // when creating a new event, lotteryRolled is set to false
        doc.put("waitlistLimit", event.getWaitlistLimit());
        doc.put("geolocationReq", event.getGeolocationRequirement());
        doc.put("visibility", event.getEventVisibility());

        // creating new event in database under collection "events"
        db.collection("events").document(event.getEventId()).set(doc)
                .addOnSuccessListener(v -> {
                    Toast.makeText(CreateEventScreen.this, "Event Uploaded!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "event uploaded to database");
                    // testing function that replaces the image button on the screen with the database uploaded image
                    // this is just to test that we can get the image back from the database and use it in the app
                    generateAndUploadQrCode(event);
                    Intent intent = new Intent(CreateEventScreen.this, OrganizerEventDetailScreen.class);
                    intent.putExtra("eventId", event.getEventId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventScreen.this, "Error creating event in database", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "couldn't upload event to database", e);
                });
    }

    private void populateEventDetails(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // populate text and number fields
                        eventTitleEditText.setText(documentSnapshot.getString("title"));
                        eventDescriptionEditText.setText(documentSnapshot.getString("description"));
                        eventLocationEditText.setText(documentSnapshot.getString("location"));
                        eventAttendantsEditText.setText(String.valueOf(documentSnapshot.getLong("maxAttendees")));
                        eventWaitlistEdittext.setText(String.valueOf(documentSnapshot.getLong("waitlistLimit")));

                        // populate image
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null) {
                            // use Glide to load the image from the URL
                            Glide.with(this).load(imageUrl).into(eventPhotoImageView);
                            // store the original URL in the tag
                            eventPhotoImageView.setTag(imageUrl);
                        }

                        // populate tags
                        @SuppressWarnings("unchecked")
                        ArrayList<String> tags = (ArrayList<String>) documentSnapshot.get("categories");
                        if (tags != null) {
                            selectedTags.clear();
                            selectedTags.addAll(tags);
                            tagsDisplayText.setText(TextUtils.join(", ", selectedTags));
                            tagsDisplayText.setVisibility(View.VISIBLE);
                        }

                        // populate dates and times
                        Timestamp startTimestamp = documentSnapshot.getTimestamp("startAt");
                        if (startTimestamp != null) {
                            ZonedDateTime startDateTime = startTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault());
                            eventStartDateEditText.setText(startDateTime.toLocalDate().toString());
                            eventStartTimeEditText.setText(startDateTime.toLocalTime().toString());
                        }
                        Timestamp endTimestamp = documentSnapshot.getTimestamp("endAt");
                        if (endTimestamp != null) {
                            ZonedDateTime endDateTime = endTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault());
                            eventEndDateEditText.setText(endDateTime.toLocalDate().toString());
                            eventEndTimeEditText.setText(endDateTime.toLocalTime().toString());
                        }
                        Timestamp regStartTimestamp = documentSnapshot.getTimestamp("regStartAt");
                        if (regStartTimestamp != null) {
                            ZonedDateTime regStartDateTime = regStartTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault());
                            registrationStartDateEditText.setText(regStartDateTime.toLocalDate().toString());
                            registrationStartTimeEditText.setText(regStartDateTime.toLocalTime().toString());
                        }
                        Timestamp regEndTimestamp = documentSnapshot.getTimestamp("regEndAt");
                        if (regEndTimestamp != null) {
                            ZonedDateTime regEndDateTime = regEndTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault());
                            registrationEndDateEditText.setText(regEndDateTime.toLocalDate().toString());
                            registrationEndTimeEditText.setText(regEndDateTime.toLocalTime().toString());
                        }

                        // populate switches
                        Boolean geoReq = documentSnapshot.getBoolean("geolocationReq");
                        eventGeolocationSwitch.setChecked(geoReq != null && geoReq);
//                        Boolean autoRoll = documentSnapshot.getBoolean("autoRoll");
//                        eventAutoRollingSwitch.setChecked(autoRoll != null && autoRoll);

                        // populate visibility
                        Boolean isPublic = documentSnapshot.getBoolean("visibility");
                        if (isPublic != null && isPublic) {
                            eventVisibilityPublic.performClick();
                        } else {
                            eventVisibilityPrivate.performClick();
                        }

                    } else {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs() {
        // check for empty text fields
        if (isEditTextEmpty(eventTitleEditText, "Title") ||
                isEditTextEmpty(eventStartDateEditText, "Start date") ||
                isEditTextEmpty(eventStartTimeEditText, "Start time") ||
                isEditTextEmpty(eventEndDateEditText, "End date") ||
                isEditTextEmpty(eventEndTimeEditText, "End time") ||
                isEditTextEmpty(eventLocationEditText, "Location") ||
                isEditTextEmpty(registrationStartDateEditText, "Registration start date") ||
                isEditTextEmpty(registrationStartTimeEditText, "Registration start time") ||
                isEditTextEmpty(registrationEndDateEditText, "Registration end date") ||
                isEditTextEmpty(registrationEndTimeEditText, "Registration end time") ||
                isEditTextEmpty(eventAttendantsEditText, "Max attendants") ||
                isEditTextEmpty(eventWaitlistEdittext, "Waitlist limit")) {
            return false; // Stop validation if any field is empty
        }

        // check if an image has been selected
        if (eventPhotoImageView.getTag() == null) {
            Toast.makeText(this, "Please select an event photo", Toast.LENGTH_LONG).show();
            return false;
        }

        // validate event start and end date times
        ZonedDateTime startDateTime = LocalDate.parse(eventStartDateEditText.getText().toString())
                .atTime(LocalTime.parse(eventStartTimeEditText.getText().toString()))
                .atZone(ZoneId.systemDefault());
        ZonedDateTime endDateTime = LocalDate.parse(eventEndDateEditText.getText().toString())
                .atTime(LocalTime.parse(eventEndTimeEditText.getText().toString()))
                .atZone(ZoneId.systemDefault());

        ZonedDateTime regStartDateTime = LocalDate.parse(registrationStartDateEditText.getText().toString())
                .atTime(LocalTime.parse(registrationStartTimeEditText.getText().toString()))
                .atZone(ZoneId.systemDefault());
        ZonedDateTime regEndDateTime = LocalDate.parse(registrationEndDateEditText.getText().toString())
                .atTime(LocalTime.parse(registrationEndTimeEditText.getText().toString()))
                .atZone(ZoneId.systemDefault());

        // check if end datetime is after start datetime
        if (endDateTime.isBefore(startDateTime)) {
            eventEndDateEditText.setError("End date must be after start date");
            eventEndTimeEditText.setError("End time must be after start time");
            Toast.makeText(this, "Event cannot end before it starts", Toast.LENGTH_LONG).show();
            return false;
        }
        if (regEndDateTime.isBefore(regStartDateTime)) {
            registrationEndDateEditText.setError("Registration End date must be after start date");
            registrationEndTimeEditText.setError("Registration End time must be after start time");
            Toast.makeText(this, "Event cannot end before it starts", Toast.LENGTH_LONG).show();
            return false;
        }

        // validate max attendants
        int maxAttendees = Integer.parseInt(eventAttendantsEditText.getText().toString());
        if (maxAttendees <= 0) {
            eventAttendantsEditText.setError("Max attendants must be greater than 0");
            return false;
        }

        // if all checks pass
        return true;
    }

    private boolean isEditTextEmpty(EditText editText, String fieldName) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError(fieldName + " cannot be empty");
            return true;
        }
        return false;
    }

    private void generateAndUploadQrCode(Event event) {
        String qrContent = "stringevents://event/" + event.getEventId();

        Bitmap qrBitmap;
        try {
            qrBitmap = QRUtils.generateQrCode(qrContent, 800, 800);
        } catch (WriterException e) {
            Log.e("UploadQR", "Failed to generate QR bitmap", e);
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();
        StorageReference qrRef = rootRef.child("qr_code/" + event.getEventId() + ".png");

        UploadTask uploadTask = qrRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot ->
                qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String qrUrl = uri.toString();
                    Log.d("UploadQR", "QR uploaded successfully, url: " + qrUrl);

                    db.collection("events")
                            .document(event.getEventId())
                            .update("qrCodeUrl", qrUrl)
                            .addOnSuccessListener(v ->
                                    Log.d("UploadQR", "qrCodeUrl saved to event document"))
                            .addOnFailureListener(e ->
                                    Log.e("UploadQR", "Failed to save qrCodeUrl", e));
                }).addOnFailureListener(e ->
                        Log.e("UploadQR", "Failed to get QR download url", e))
        ).addOnFailureListener(e ->
                Log.e("UploadQR", "Failed to upload QR image", e));
    }
}