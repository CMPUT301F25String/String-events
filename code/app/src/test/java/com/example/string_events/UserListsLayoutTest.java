package com.example.string_events;
// include activity_canceled_users、activity_participating_users、activity_waitlist_users
import static org.junit.Assert.*;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserListsLayoutTest {

    private View inflate(int layoutRes) {
        Context base = ApplicationProvider.getApplicationContext();
        Context themed = new ContextThemeWrapper(base, com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight);
        return LayoutInflater.from(themed).inflate(layoutRes, null, false);
    }

    @Test
    public void canceledUsers_inflates_and_has_core_ids() {
        View v = inflate(R.layout.activity_canceled_users);
        assertNotNull(v.findViewById(R.id.btnBack));
        assertNotNull(v.findViewById(R.id.tvTitle));
        assertNotNull(v.findViewById(R.id.listCanceled));
        assertNotNull(v.findViewById(R.id.btnSendCanceled));
    }

    @Test
    public void participatingUsers_inflates_and_has_core_ids() {
        View v = inflate(R.layout.activity_participating_users);
        assertNotNull(v.findViewById(R.id.btnBack));
        assertNotNull(v.findViewById(R.id.tvTitle));
        assertNotNull(v.findViewById(R.id.listParticipating));
        assertNotNull(v.findViewById(R.id.btnSendParticipating));
        assertNotNull(v.findViewById(R.id.btnExportParticipating));
    }

    @Test
    public void waitlistUsers_inflates_and_has_core_ids() {
        View v = inflate(R.layout.activity_waitlist_users);
        assertNotNull(v.findViewById(R.id.btnBack));
        assertNotNull(v.findViewById(R.id.tvTitle));
        assertNotNull(v.findViewById(R.id.listWaitlist));
        assertNotNull(v.findViewById(R.id.btnSendWaitlist));
    }
}