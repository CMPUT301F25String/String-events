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

    private static final String ARG_EVENT_ID = "arg_event_id";

    private String eventId;

    public static LotteryEventOverviewFragment newInstance(String eventId) {
        LotteryEventOverviewFragment fragment = new LotteryEventOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.event_overview_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnQrCode = view.findViewById(R.id.btn_qr_code);

        btnQrCode.setOnClickListener(v -> {
            String targetEventId = eventId;
            if (targetEventId == null || targetEventId.isEmpty()) {
                targetEventId = "demo-event-id";
            }

            Intent intent = new Intent(requireContext(), QrCodeActivity.class);
            intent.putExtra(QrCodeActivity.EXTRA_EVENT_ID, targetEventId);
            startActivity(intent);
        });
    }
}
