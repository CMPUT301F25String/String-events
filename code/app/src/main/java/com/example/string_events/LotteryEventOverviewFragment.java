package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LotteryEventOverviewFragment extends Fragment {

    // Temporary hardcoded event id (event3 in Firestore)
    private static final String EVENT3_ID = "07d4dd53-3efe-4613-b852-0720a924be8b";

    // In the future this can be set from arguments or ViewModel
    private final String eventId = EVENT3_ID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_overview_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // QR Code button in the overview screen
        ImageButton btnQrCode = view.findViewById(R.id.btn_qr_code);

        btnQrCode.setOnClickListener(v -> {
            // Decide which event id to use (for now always event3)
            String targetEventId = eventId;
            if (targetEventId == null || targetEventId.isEmpty()) {
                targetEventId = EVENT3_ID;
            }

            // Launch QrCodeActivity and pass the event id
            Intent intent = new Intent(requireContext(), QrCodeActivity.class);
            intent.putExtra(QrCodeActivity.EXTRA_EVENT_ID, targetEventId);
            startActivity(intent);
        });
    }
}
