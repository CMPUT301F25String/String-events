package com.example.string_events;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays details of a single event and lets the current user apply/cancel,
 * or accept/decline an invite. Event data is loaded from Firestore.
 */
public class EventDetailActivity extends AppCompatActivity {
    private String username;
    private FirebaseFirestore db;
    private DocumentReference eventDocumentRef;

    private final AtomicBoolean userInEventWaitlist = new AtomicBoolean(false);
    private final AtomicBoolean userInEventInvited = new AtomicBoolean(false);
    private final AtomicBoolean userInEventAttendees = new AtomicBoolean(false);
    private final LotteryHelper lotteryHelper = new LotteryHelper();

    private static final int REQ_COARSE_APPLY = 7000;
    private FusedLocationProviderClient fusedForSave;

    /**
     * Initializes UI, resolves intent extras, fetches the event document,
     * configures action buttons (apply / accept / decline), and sets up
     * membership state checks for attendees and waitlist.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_screen); // Updated to match your latest XML file name

        String eventId = getIntent().getStringExtra("event_id");
        assert eventId != null;
        db = FirebaseFirestore.getInstance();
        eventDocumentRef = db.collection("events").document(eventId);

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        username = sp.getString("user", null);

        fusedForSave = LocationServices.getFusedLocationProviderClient(this);

        ImageView back = findViewById(getId("back_button"));
        ImageButton applyButton = findViewById(R.id.apply_button);
        back.setOnClickListener(v -> finish());

        // change the visual elements of the event details to match the event details of the clicked event
        eventDocumentRef.get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> {
                    Log.d("FirestoreCheck", "document with eventId does not exist");
                });

        // set the variables userInEventWaitlist and userInEventAttendees using the database
        // also change appearance of the apply button to reflect the user's status in the event
        getUserStatusFromDatabase(applyButton, eventDocumentRef);

        applyButton.setOnClickListener(view -> {
            // user has not applied for this event yet
            if (!userInEventWaitlist.get() && !userInEventAttendees.get()) {
                eventDocumentRef.get().addOnSuccessListener(this, documentSnapshot -> {
                    boolean geolocationReq = Boolean.TRUE.equals(documentSnapshot.getBoolean("geolocationReq"));
                    if (geolocationReq) {
                        // request location permissions from the user and calls onRequestPermissionsResult
                        // if the user accepts the permissions, it will add them to the waitlist from within that method
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQ_COARSE_APPLY);
                    }
                    else {
                        addUserToEventWaitlist(applyButton);
                    }
                });
            }
            // user has applied for the event and is not an attendee yet (not been accepted yet)
            else if (userInEventWaitlist.get() && !userInEventAttendees.get()) {
                eventDocumentRef.update("waitlist", FieldValue.arrayRemove(username));
                Toast.makeText(EventDetailActivity.this, "Removed from waitlist!", Toast.LENGTH_SHORT).show();
                userInEventWaitlist.set(false);
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
            // user is already an attendee and wants to cancel their appearance
            else {
                eventDocumentRef.update("attendees", FieldValue.arrayRemove(username));
                eventDocumentRef.update("cancelled", FieldValue.arrayUnion(username));
                Toast.makeText(EventDetailActivity.this, "Removed from attendees!", Toast.LENGTH_SHORT).show();
                userInEventAttendees.set(false);
                // when an attendee for an event cancels, the lottery automatically selects a replacement user from the waitlist
                lotteryHelper.replaceCancelledUser(eventDocumentRef);
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
        });
    }

    private void addUserToEventWaitlist(ImageButton applyButton) {
        eventDocumentRef.update("waitlist", FieldValue.arrayUnion(username));
        Toast.makeText(EventDetailActivity.this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
        userInEventWaitlist.set(true);
        applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_COARSE_APPLY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DocumentReference userDocRef = db.collection("users").document(username);
                userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                    // store the user's location if it's not already saved
                    if (!documentSnapshot.contains("location")) {
                        saveApproxLocation();
                    }
                    // add the user to the event's waitlist
                    addUserToEventWaitlist(findViewById(R.id.apply_button));
                });
            } else {
                Toast.makeText(this, "Please enable location services to join this event.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void saveApproxLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedForSave.getLastLocation().addOnSuccessListener(this, (Location loc) -> {
            if (loc == null) {
                Toast.makeText(this, "Could not retrieve location. Please ensure location is enabled and try again.", Toast.LENGTH_LONG).show();
                return;
            }
            Map<String, Object> data = new HashMap<>();
            GeoPoint userLocation = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            data.put("location", userLocation);
            DocumentReference userDocRef = db.collection("users").document(username);
            userDocRef.update(data).addOnSuccessListener(v -> {
                Toast.makeText(EventDetailActivity.this, "Saved your approximate location!", Toast.LENGTH_SHORT).show();
                Log.d("Firestore", "user location uploaded to database");
            });
        });
    }


    /**
     * Populates UI fields with document values and formats date/time and waitlist
     * counters for display.
     *
     * @param s Firestore document snapshot of the event
     */
    private void bind(DocumentSnapshot s) {
        String title = s.getString("title");
        String description = s.getString("description");
        String location = s.getString("location");
        boolean geolocation = Boolean.TRUE.equals(s.getBoolean("geolocationReq"));
        String creator = s.getString("creator");
        Timestamp startAt = s.getTimestamp("startAt");
        Timestamp endAt = s.getTimestamp("endAt");

        int max = asInt(s.get("maxAttendees"));
        ArrayList<String> attendees = (ArrayList<String>) s.get("attendees");
        assert attendees != null;
        int taken = attendees.size();
        int waitLimit = asInt(s.get("waitlistLimit"));

        @SuppressWarnings("unchecked")
        List<String> waitlist = (List<String>) s.get("waitlist");
        int currentWaitCount = (waitlist != null) ? waitlist.size() : 0;

        // Updated Date/Time Logic: Show full Start and End Date/Time in the two TextViews
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        String startString = startAt != null ? df.format(startAt.toDate()) : "N/A";
        String endString = endAt != null ? df.format(endAt.toDate()) : "N/A";

        // Assigning Start Date+Time to tvDateLine and End Date+Time to tvTimeLine
        setText(getId("tvDateLine"), "Start:  " + startString);
        setText(getId("tvTimeLine"), "End:   " + endString);

        setText(getId("tvEventTitle"), title);
        setText(getId("tvAddress"), location);
        if (geolocation) {
            setText(getId("geolocation_text"), "Entrant geolocation is enabled");
        } else {
            setText(getId("geolocation_text"), "Entrant geolocation is disabled");
        }

        setText(getId("tvDescription"), description);

        TextView org = findViewById(R.id.tvOrganizer);
        if (org != null && creator != null) org.setText("Hosted by: " + creator);

        setText(getId("spots_taken"), "(" + taken + "/" + max + ") Spots");
        if (waitLimit > 0)
            setText(getId("waiting_list"), "(" + currentWaitCount + "/" + waitLimit + ") on Waitlist");
        else
            setText(getId("waiting_list"), currentWaitCount + " on Waitlist");

        ImageView eventImage = findViewById(R.id.ivEventImage);
        String imageUrl = s.getString("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    java.io.InputStream input = conn.getInputStream();
                    final android.graphics.Bitmap bmp =
                            android.graphics.BitmapFactory.decodeStream(input);

                    eventImage.post(() -> {
                        if (bmp != null) eventImage.setImageBitmap(bmp);
                        else eventImage.setImageResource(android.R.drawable.ic_menu_report_image);
                    });

                } catch (Exception e) {
                    eventImage.post(() ->
                            eventImage.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            eventImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    /**
     * Safely sets text on a {@link TextView} identified by ID.
     *
     * @param id    resource id of the target view
     * @param value text to display; empty string used if {@code null}
     */
    private void setText(int id, String value) {
        if (id == 0) return;
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value == null ? "" : value);
    }

    /**
     * Resolves a view ID by name using {@link android.content.res.Resources#getIdentifier}.
     *
     * @param name id name in the "id" resource type
     * @return resolved identifier or {@code 0} if not found
     */
    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    /**
     * Converts an arbitrary object to an {@code int} with safe fallbacks.
     *
     * @param o object to convert (may be {@link Number} or parsable string)
     * @return parsed integer value or {@code 0} on failure/null
     */
    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private void getUserStatusFromDatabase(ImageButton applyButton, DocumentReference eventDocumentRef) {
        // setting the value of userInEventWaitlist (checking if user is in the event's waitlist)
        // and the value of userInEventAttendees (checking if user is in the event's attendees)
        eventDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> waitlist = (ArrayList<String>) documentSnapshot.get("waitlist");
            ArrayList<String> invitedList = (ArrayList<String>) documentSnapshot.get("invited");
            ArrayList<String> attendeesList = (ArrayList<String>) documentSnapshot.get("attendees");

            if (waitlist != null && waitlist.contains(username)) {
                Log.d("FirestoreCheck", "already in waitlist");
                userInEventWaitlist.set(true);
                applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            } else if (invitedList != null && invitedList.contains(username)) {
                // user has been invited to the event and needs to either confirm or decline attendance
                Log.d("FirestoreCheck", "already in invited");
                userInEventInvited.set(true);
                eventPendingUserConfirmation(applyButton, eventDocumentRef);
            } else if (attendeesList != null && attendeesList.contains(username)) {
                Log.d("FirestoreCheck", "already in attendees");
                userInEventAttendees.set(true);
                applyButton.setBackgroundResource(R.drawable.leave_event_button);
            } else {
                // user is not on any of the lists
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
        }).addOnFailureListener(e -> Log.e("FirestoreCheck", "Error fetching document", e));
    }

    private void eventPendingUserConfirmation(ImageButton applyButton, DocumentReference eventDocumentRef) {
        // this instance of event details was opened from the notification screen
        ImageButton acceptInviteButton = findViewById(R.id.accept_invite_button);
        ImageButton declineInviteButton = findViewById(R.id.decline_invite_button);
        LinearLayout inviteButtonsLayout = findViewById(R.id.invite_buttons_container);

        applyButton.setVisibility(View.GONE);
        inviteButtonsLayout.setVisibility(View.VISIBLE);

        acceptInviteButton.setOnClickListener(view -> {
            eventDocumentRef.update("attendees", FieldValue.arrayUnion(username));
            eventDocumentRef.update("invited", FieldValue.arrayRemove(username));
            Toast.makeText(EventDetailActivity.this, "You've confirmed your attendance!", Toast.LENGTH_SHORT).show();
            userInEventAttendees.set(true);
            applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            applyButton.setVisibility(View.VISIBLE);
            inviteButtonsLayout.setVisibility(View.GONE);
        });

        declineInviteButton.setOnClickListener(view -> {
            eventDocumentRef.update("invited", FieldValue.arrayRemove(username));
            Toast.makeText(EventDetailActivity.this, "You've declined your attendance!", Toast.LENGTH_SHORT).show();
            // when an invited user for an event declines, the lottery automatically selects a replacement user from the waitlist
            lotteryHelper.replaceCancelledUser(eventDocumentRef);
            applyButton.setBackgroundResource(R.drawable.apply_button);
            applyButton.setVisibility(View.VISIBLE);
            inviteButtonsLayout.setVisibility(View.GONE);
        });
    }
}