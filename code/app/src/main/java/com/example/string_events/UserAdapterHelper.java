package com.example.string_events;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserAdapterHelper {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final UserAdapter adapter;
    private final ArrayList<UserItem> userList;
    private final UserItem.Status mode;

    public UserAdapterHelper(UserAdapter adapter, ArrayList<UserItem> userList, UserItem.Status mode) {
        this.adapter = adapter;
        this.userList = userList;
        this.mode = mode;
    }

    @SuppressWarnings("unchecked")
    public void loadUsers(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        return;
                    }

                    String listName = "";
                    switch (mode) {
                        case WAITLIST:
                            listName = "waitlist";
                            break;
                        case INVITED:
                            listName = "invited";
                            break;
                        case PARTICIPATING:
                            listName = "attendees";
                            break;
                        case CANCELED:
                            listName = "cancelled";
                            break;
                    }

                    ArrayList<String> waitlist = (ArrayList<String>) doc.get(listName);
                    if (waitlist == null || waitlist.isEmpty()) {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    fetchUsers(waitlist);
                });
    }

    private void fetchUsers(List<String> usernames) {
        userList.clear();

        for (String username : usernames) {
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            addUserToList(query.getDocuments().get(0));
                        }
                    });
        }
    }

    private void addUserToList(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        String username = doc.getString("username");
        String name = doc.getString("name");
        String email = doc.getString("email");

        UserItem.Status status;
        switch (mode) {
            case WAITLIST:
                status = UserItem.Status.WAITLIST;
                break;
            case INVITED:
                status = UserItem.Status.INVITED;
                break;
            case PARTICIPATING:
                status = UserItem.Status.PARTICIPATING;
                break;
            case CANCELED:
                status = UserItem.Status.CANCELED;
                break;
            default:
                status = UserItem.Status.NONE;
                break;
        }

        userList.add(new UserItem(
                username != null ? username : "",
                name != null ? name : "",
                email != null ? email : "",
                status
        ));

        adapter.notifyDataSetChanged();
    }
}
