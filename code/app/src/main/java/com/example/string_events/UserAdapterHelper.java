package com.example.string_events;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that loads event-related users from Firestore and populates a {@link UserAdapter}.
 * <p>
 * Depending on the {@link UserItem.Status} mode (WAITLIST, INVITED, PARTICIPATING, CANCELED),
 * this helper:
 * <ul>
 *     <li>Reads the corresponding username list field from an event document
 *         (e.g. {@code waitlist}, {@code invited}, {@code attendees}, {@code cancelled}).</li>
 *     <li>Fetches each user's profile from the {@code users} collection.</li>
 *     <li>Builds {@link UserItem} objects and updates the backing list for the adapter.</li>
 * </ul>
 */
public class UserAdapterHelper {

    /**
     * Firestore instance used to read event and user documents.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Adapter that will be notified when the user list changes.
     */
    private final UserAdapter adapter;

    /**
     * Backing list of {@link UserItem} displayed by the adapter.
     */
    private final ArrayList<UserItem> userList;

    /**
     * Current display mode that determines which group of users to load
     * (waitlist, invited, participating, or canceled).
     */
    private final UserItem.Status mode;

    /**
     * Creates a helper that manages loading users into a {@link UserAdapter}.
     *
     * @param adapter  adapter that displays user items
     * @param userList underlying list used by the adapter
     * @param mode     which event-participation group to load (WAITLIST, INVITED, etc.)
     */
    public UserAdapterHelper(UserAdapter adapter, ArrayList<UserItem> userList, UserItem.Status mode) {
        this.adapter = adapter;
        this.userList = userList;
        this.mode = mode;
    }

    /**
     * Loads event users for the given event ID based on the configured {@link #mode}.
     * <p>
     * Steps:
     * <ol>
     *     <li>Fetch the event document from {@code events/{eventId}}.</li>
     *     <li>Select the correct field name (e.g. {@code waitlist}, {@code invited}).</li>
     *     <li>Read that field as a list of usernames.</li>
     *     <li>If the list is not empty, fetch user profiles for each username.</li>
     * </ol>
     *
     * @param eventId Firestore document ID for the event
     */
    @SuppressWarnings("unchecked")
    public void loadUsers(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        // If the event document does not exist, there are no users to load.
                        return;
                    }

                    // Determine which array field to read based on the current mode.
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

                    // Read the list of usernames (may be null or empty).
                    ArrayList<String> waitlist = (ArrayList<String>) doc.get(listName);
                    if (waitlist == null || waitlist.isEmpty()) {
                        // Clear the adapter list if there are no users in this group.
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // Fetch profile details for each username in the list.
                    fetchUsers(waitlist);
                });
    }

    /**
     * Fetches user documents for the given list of usernames and adds them to {@link #userList}.
     * <p>
     * For each username:
     * <ul>
     *     <li>Queries {@code users} collection where {@code username} matches.</li>
     *     <li>If a user is found, converts it to a {@link UserItem} and adds it to the list.</li>
     * </ul>
     *
     * @param usernames list of usernames to resolve into full user profiles
     */
    private void fetchUsers(List<String> usernames) {
        // Start with an empty list before adding fresh results.
        userList.clear();

        for (String username : usernames) {
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            // Use the first matching document as this user's profile.
                            addUserToList(query.getDocuments().get(0));
                        }
                    });
        }
    }

    /**
     * Converts a user document into a {@link UserItem} and updates the adapter list.
     * <p>
     * The {@link UserItem.Status} assigned to the new item mirrors the helper's current mode.
     *
     * @param doc Firestore document from the {@code users} collection
     */
    private void addUserToList(DocumentSnapshot doc) {
        if (!doc.exists()) {
            return;
        }

        String username = doc.getString("username");
        String name = doc.getString("name");
        String email = doc.getString("email");

        // Map the helper mode to the correct UserItem.Status for this entry.
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

        // Safely create and add a new UserItem, falling back to empty strings if any field is null.
        userList.add(new UserItem(
                username != null ? username : "",
                name != null ? name : "",
                email != null ? email : "",
                status
        ));

        // Notify the adapter that the underlying data set has changed so the UI can refresh.
        adapter.notifyDataSetChanged();
    }
}
