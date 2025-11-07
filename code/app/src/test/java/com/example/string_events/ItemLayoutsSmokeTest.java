package com.example.string_events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.material.imageview.ShapeableImageView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ItemLayoutsSmokeTest {

    private View inflate(int layoutRes) {
        Context base = ApplicationProvider.getApplicationContext();
        Context themed = new ContextThemeWrapper(base, com.google.android.material.R.style.Theme_Material3_DayNight);
        return LayoutInflater.from(themed).inflate(layoutRes, null, false);
    }

    @Test
    public void item_event_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_event);
        assertNotNull(v.findViewById(R.id.img_cover));
        assertNotNull(v.findViewById(R.id.tv_title));
        assertNotNull(v.findViewById(R.id.tv_time));
        assertNotNull(v.findViewById(R.id.tv_spots));
        assertNotNull(v.findViewById(R.id.tv_place));
        assertNotNull(v.findViewById(R.id.btn_status));
    }

    @Test
    public void item_admin_event_card_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_admin_event_card);
        assertNotNull(v.findViewById(R.id.imgCover));
        assertNotNull(v.findViewById(R.id.tvTitle));
        assertNotNull(v.findViewById(R.id.tvTime));
        assertNotNull(v.findViewById(R.id.imgLocationLogo));
        assertNotNull(v.findViewById(R.id.tvLocation));
        assertNotNull(v.findViewById(R.id.tvOrganizer));
        assertNotNull(v.findViewById(R.id.chipStatus));
    }

    @Test
    public void item_org_event_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_org_event);
        assertNotNull(v.findViewById(R.id.img_cover));
        assertNotNull(v.findViewById(R.id.tv_title));
        assertNotNull(v.findViewById(R.id.tv_time));
        assertNotNull(v.findViewById(R.id.tv_spots));
        assertNotNull(v.findViewById(R.id.tv_place));
    }

    @Test
    public void item_admin_event_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_admin_event);
        assertNotNull(v.findViewById(R.id.tv_event_title));
        assertNotNull(v.findViewById(R.id.tv_event_subtitle));
    }

    @Test
    public void item_notification_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_notification);
        View status = v.findViewById(R.id.imgStatus);
        View thumb = v.findViewById(R.id.imgThumb);
        View open = v.findViewById(R.id.imgOpen);
        assertNotNull(status);
        assertNotNull(thumb);
        assertNotNull(open);
        assertTrue(status instanceof ImageView);
        assertTrue(thumb instanceof ShapeableImageView);
        assertTrue(open instanceof ImageButton);
        assertNotNull(v.findViewById(R.id.tvMessage));
        assertNotNull(v.findViewById(R.id.tvEventName));
    }

    @Test
    public void item_user_row_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_user_row);
        assertNotNull(v.findViewById(R.id.textBlock));
        assertNotNull(v.findViewById(R.id.tvName));
        assertNotNull(v.findViewById(R.id.tvEmail));
        assertNotNull(v.findViewById(R.id.tvBadge));
    }

    @Test
    public void item_image_row_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_image_row);
        assertNotNull(v.findViewById(R.id.img_event));
    }

    @Test
    public void item_profile_event_inflates_and_has_core_ids() {
        View v = inflate(R.layout.item_profile_event);
        assertNotNull(v.findViewById(R.id.profile_background));
        assertNotNull(v.findViewById(R.id.event_image));
        assertNotNull(v.findViewById(R.id.event_name));
        assertNotNull(v.findViewById(R.id.date_image));
        assertNotNull(v.findViewById(R.id.event_date));
        assertNotNull(v.findViewById(R.id.event_time));
        assertNotNull(v.findViewById(R.id.community_centre_image));
        assertNotNull(v.findViewById(R.id.event_location));
    }
}
