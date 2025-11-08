package com.example.string_events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment that displays event details.
 * <p>
 * When launched with {@code fromTest=true}, the UI is populated with placeholder
 * values and the apply button toggles its background without backend calls.
 * The target event can be provided via the {@code event_id} argument.
 */
public class EventDetailFragment extends Fragment {

    private boolean fromTest = false;
    private String eventId = null;

    /**
     * Inflates the event detail layout for this fragment.
     *
     * @param inflater  layout inflater
     * @param container parent view group
     * @param savedInstanceState previously saved instance state, or {@code null}
     * @return the inflated event detail view
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_detail_screen, container, false);
    }

    /**
     * Binds views, reads fragment arguments ({@code fromTest}, {@code event_id}),
     * wires back navigation and apply button behavior, and optionally populates
     * placeholder data when running in test mode.
     *
     * @param v the fragment root view
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            fromTest = args.getBoolean("fromTest", false);
            eventId  = args.getString("event_id", null);
        }

        ImageView back = v.findViewById(R.id.back_button);
        if (back != null) {
            back.setOnClickListener(btn -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        TextView tvTitle = v.findViewById(R.id.tvEventTitle);
        TextView tvLoc   = v.findViewById(R.id.tvLocation);
        ImageView ivImg  = v.findViewById(R.id.ivEventImage);
        TextView tvSpots = v.findViewById(R.id.spots_taken);
        TextView tvWait  = v.findViewById(R.id.waiting_list);
        TextView tvDate  = v.findViewById(R.id.tvDateLine);
        TextView tvTime  = v.findViewById(R.id.tvTimeLine);
        TextView tvAddr  = v.findViewById(R.id.tvAddress);
        TextView tvDesc  = v.findViewById(R.id.tvDescription);
        ImageButton btnApply = v.findViewById(R.id.apply_button);

        if (fromTest) {
            if (tvTitle != null) tvTitle.setText("UI Test Event");
            if (tvLoc   != null) tvLoc.setText("Main Hall");
            if (tvSpots != null) tvSpots.setText("(5/20) Spots Taken");
            if (tvWait  != null) tvWait.setText("3 on Waitlist");
            if (tvDate  != null) tvDate.setText("Wed, Oct 8, 2025");
            if (tvTime  != null) tvTime.setText("11:00 AM - 1:00 PM");
            if (tvAddr  != null) tvAddr.setText("123 Test Ave");
            if (tvDesc  != null) tvDesc.setText("This is a dummy description for UI test.");
        } else {
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(b -> {
                if (fromTest) {
                    Object tag = b.getTag();
                    boolean on = tag instanceof Boolean && (Boolean) tag;
                    if (on) {
                        b.setBackgroundResource(R.drawable.apply_button);
                    } else {
                        b.setBackgroundResource(R.drawable.cancel_apply_button);
                    }
                    b.setTag(!on);
                    return;
                }
            });
        }
    }
}
