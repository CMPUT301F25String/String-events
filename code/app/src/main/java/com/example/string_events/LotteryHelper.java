package com.example.string_events;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for handling lottery-related logic for events.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Promoting a user from the waitlist to the invited list when a spot opens up.</li>
 *     <li>Creating and uploading lottery notification documents to Firestore.</li>
 * </ul>
 */
public class LotteryHelper {

    /**
     * Firestore instance used to read and update event and notification documents.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Replaces a canceled user by promoting a random user from the waitlist
     * into the invited list, and sends a lottery notification to that user.
     * <p>
     * Implementation details:
     * <ul>
     *     <li>Fetches the current waitlist array from the event document.</li>
     *     <li>If the waitlist is not empty, shuffles it and selects the first user.</li>
     *     <li>Adds the selected user to the {@code invited} array and removes them from the {@code waitlist} array.</li>
     *     <li>Calls {@link #sendLotteryNotifications(DocumentReference, ArrayList, ArrayList)} with a single invited user
     *         and no waitlisted users, to send only one notification.</li>
     * </ul>
     *
     * @param eventRef Firestore {@link DocumentReference} for the event document
     */
    @SuppressWarnings("unchecked")
    public void replaceCancelledUser(DocumentReference eventRef) {
        eventRef.get().addOnSuccessListener(eventSnap -> {
            // Get the waitlist array of the event and check if it has any users
            ArrayList<String> waitlist = (ArrayList<String>) eventSnap.get("waitlist");
            if (waitlist != null && !waitlist.isEmpty()) {
                // Shuffle the current waitlist and select the first user as the new invitee
                Collections.shuffle(waitlist);
                eventRef.update("invited", FieldValue.arrayUnion(waitlist.get(0)));
                eventRef.update("waitlist", FieldValue.arrayRemove(waitlist.get(0)));

                ArrayList<String> inviteList = new ArrayList<>();
                inviteList.add(waitlist.get(0));

                // Call sendLotteryNotifications with a single invitee and no waitlist
                // because we only need to notify the one promoted user
                sendLotteryNotifications(eventRef, inviteList, null);
            }
        });
    }

    /**
     * Sends lottery notifications for invited and (optionally) non-selected users.
     * <p>
     * This method:
     * <ul>
     *     <li>Fetches event metadata (title, image URL) from {@code eventRef}.</li>
     *     <li>Builds {@link Notification} objects for all usernames in {@code inviteList}
     *         (selectedStatus = {@code true}).</li>
     *     <li>Optionally builds {@link Notification} objects for all usernames in {@code waitList}
     *         (selectedStatus = {@code false}), if {@code waitList} is not {@code null}.</li>
     *     <li>Writes a notification document to the {@code notifications} collection for each entry.</li>
     * </ul>
     *
     * @param eventRef   Firestore {@link DocumentReference} for the event whose users are notified
     * @param inviteList list of usernames selected in the lottery (winners); must not be {@code null}
     * @param waitList   optional list of usernames not selected in the lottery; may be {@code null}
     */
    public void sendLotteryNotifications(DocumentReference eventRef, ArrayList<String> inviteList, ArrayList<String> waitList) {
        eventRef.get().addOnSuccessListener(eventSnap -> {
            String eventName = eventSnap.getString("title");
            String url = eventSnap.getString("imageUrl");
            // Ensure the event photo is either a non-empty URL or null
            String eventPhoto = url == null || url.isEmpty() ? null : url;

            ArrayList<Notification> notificationUploadList = new ArrayList<>();
            for (String username : inviteList) {
                Notification newNotification = new Notification(
                        username, true, eventRef.getId(), eventPhoto, eventName);
                notificationUploadList.add(newNotification);
            }

            if (waitList != null) {
                for (String username : waitList) {
                    Notification newNotification = new Notification(username, false, eventRef.getId(), eventPhoto, eventName);
                    notificationUploadList.add(newNotification);
                }
            }

            // Upload each notification as a separate document in the "notifications" collection
            for (Notification notification : notificationUploadList) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("username", notification.getUsername());
                doc.put("selectedStatus", notification.getSelectedStatus());
                doc.put("eventId", notification.getEventId());
                // Only add the image URL if it's not null
                doc.put("createdAt", notification.getTimeStamp());
                if (notification.getEventPhoto() != null) {
                    doc.put("imageUrl", notification.getEventPhoto());
                }
                doc.put("eventName", notification.getEventName());

                // Create new notification in the "notifications" collection
                db.collection("notifications").add(doc)
                        .addOnSuccessListener(v -> {
                            Log.d("FirestoreCheck", "notifications uploaded to database");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreCheck", "couldn't upload notifications to database", e);
                        });
            }
        });
    }
}
