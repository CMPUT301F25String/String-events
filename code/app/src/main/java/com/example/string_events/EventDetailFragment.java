package com.example.string_events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Locale;

public class EventDetailFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";

    public static EventDetailFragment newInstance(String eventId) {
        EventDetailFragment f = new EventDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_detail_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        String id = getArguments() == null ? null : getArguments().getString(ARG_EVENT_ID);

        View back = v.findViewById(R.id.back_button);
        if (back != null) back.setOnClickListener(x -> requireActivity().getSupportFragmentManager().popBackStack());
        if (id == null) return;

        final TextView tvTitle = v.findViewById(R.id.tvEventTitle);
        final TextView tvLocation = v.findViewById(R.id.tvLocation);
        final TextView tvDateLine = v.findViewById(R.id.tvDateLine);
        final TextView tvTimeLine = v.findViewById(R.id.tvTimeLine);
        final TextView tvDesc = v.findViewById(R.id.tvDescription);
        final TextView tvSpotsTaken = v.findViewById(R.id.spots_taken);
        final TextView tvWaiting = v.findViewById(R.id.waiting_list);
        final TextView tvAddress = v.findViewById(R.id.tvAddress);

        DateFormat dfDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        db.collection("events").document(id).get().addOnSuccessListener((DocumentSnapshot d) -> {
            if (tvTitle != null) tvTitle.setText(d.getString("title"));
            if (tvLocation != null) tvLocation.setText(d.getString("location"));

            Timestamp startAt = d.getTimestamp("startAt");
            Timestamp endAt = d.getTimestamp("endAt");
            if (startAt != null && tvDateLine != null) tvDateLine.setText(dfDate.format(startAt.toDate()));
            if (startAt != null && endAt != null && tvTimeLine != null) {
                tvTimeLine.setText(dfTime.format(startAt.toDate()) + " - " + dfTime.format(endAt.toDate()));
            }

            Long maxAtt = d.getLong("maxAttendees");
            Long attCnt = d.getLong("attendeesCount");
            if (maxAtt != null && attCnt != null && tvSpotsTaken != null) {
                tvSpotsTaken.setText("(" + attCnt + "/" + maxAtt + ") Spots Taken");
            }

            Long waiting = d.getLong("waitingCount");
            if (waiting != null && tvWaiting != null) tvWaiting.setText(waiting + " Waiting List");

            if (tvDesc != null) tvDesc.setText(d.getString("description"));
            if (tvAddress != null) tvAddress.setText(d.getString("address"));
        });
    }
}
