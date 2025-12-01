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

public class LotteryHelper {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @SuppressWarnings("unchecked")
    public void replaceCancelledUser(DocumentReference eventRef) {
        eventRef.get().addOnSuccessListener(eventSnap -> {
            // get the waitlist array of the event and count how many people are on it;
            ArrayList<String> waitlist = (ArrayList<String>) eventSnap.get("waitlist");
            if (waitlist != null && !waitlist.isEmpty()) {
                // shuffle the event's current waitlist and select the first user as the winner
                Collections.shuffle(waitlist);
                eventRef.update("invited", FieldValue.arrayUnion(waitlist.get(0)));
                eventRef.update("waitlist", FieldValue.arrayRemove(waitlist.get(0)));
                ArrayList<String> inviteList = new ArrayList<>();
                inviteList.add(waitlist.get(0));
                // call sendLotteryNotifications with an inviteList of size 1 and no waitlist
                // this is because we only need to send 1 notification to the winner
                sendLotteryNotifications(eventRef, inviteList, null);
            }
        });
    }

    public void sendLotteryNotifications(DocumentReference eventRef, ArrayList<String> inviteList, ArrayList<String> waitList) {
        eventRef.get().addOnSuccessListener(eventSnap -> {
            String eventName = eventSnap.getString("title");
            String url = eventSnap.getString("imageUrl");
            String eventPhoto = url == null || url.isEmpty() ? null : url; // make sure photo is either a url or null

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

            for (Notification notification : notificationUploadList) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("username", notification.getUsername());
                doc.put("selectedStatus", notification.getSelectedStatus());
                doc.put("eventId", notification.getEventId());
                doc.put("createdAt", notification.getTimeStamp());
                // only add the image url if it's not null
                if (notification.getEventPhoto() != null) {
                    doc.put("imageUrl", notification.getEventPhoto());
                }
                doc.put("eventName", notification.getEventName());

                // creating new notification in database under collection "notifications"
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
