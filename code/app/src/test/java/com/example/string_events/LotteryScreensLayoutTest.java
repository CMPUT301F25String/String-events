package com.example.string_events;

// Lottery_before, Lottery_after, Lottery_information_screen

import static org.junit.Assert.*;

import android.view.View;
import com.google.android.material.button.MaterialButton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LotteryScreensLayoutTest {

    @Test
    public void beforeRoll_inflates_and_coreViews_exist_and_rollEnabled() {
        View root = TestViews.inflate(R.layout.lottery_before_roll);

        assertNotNull(root.findViewById(R.id.btnBack));
        assertNotNull(root.findViewById(R.id.tvTitle));
        assertNotNull(root.findViewById(R.id.cardBanner));
        assertNotNull(root.findViewById(R.id.imgBanner));
        assertNotNull(root.findViewById(R.id.tvEventName));
        assertNotNull(root.findViewById(R.id.ivClock));
        assertNotNull(root.findViewById(R.id.tvTime));
        assertNotNull(root.findViewById(R.id.ivLoc));
        assertNotNull(root.findViewById(R.id.tvLocation));
        assertNotNull(root.findViewById(R.id.cardStatus));
        assertNotNull(root.findViewById(R.id.tvStatusLine1));
        assertNotNull(root.findViewById(R.id.tvStatusLine2));

        MaterialButton roll = root.findViewById(R.id.btnRoll);
        assertNotNull(roll);
        assertTrue(roll.isEnabled());
    }

    @Test
    public void afterRoll_inflates_and_coreViews_exist_and_rollDisabled() {
        View root = TestViews.inflate(R.layout.lottery_after_roll);

        assertNotNull(root.findViewById(R.id.btnBack));
        assertNotNull(root.findViewById(R.id.tvTitle));
        assertNotNull(root.findViewById(R.id.cardBanner));
        assertNotNull(root.findViewById(R.id.imgBanner));
        assertNotNull(root.findViewById(R.id.tvEventName));
        assertNotNull(root.findViewById(R.id.ivClock));
        assertNotNull(root.findViewById(R.id.tvTime));
        assertNotNull(root.findViewById(R.id.ivLoc));
        assertNotNull(root.findViewById(R.id.tvLocation));
        assertNotNull(root.findViewById(R.id.cardStatus));
        assertNotNull(root.findViewById(R.id.tvSelectedCount));
        assertNotNull(root.findViewById(R.id.ivNotify));
        assertNotNull(root.findViewById(R.id.tvNotified));

        MaterialButton roll = root.findViewById(R.id.btnRoll);
        assertNotNull(roll);
        assertFalse(roll.isEnabled());
    }

    @Test
    public void infoScreen_inflates_and_textBlocks_exist() {
        View root = TestViews.inflate(R.layout.lottery_information_screen);

        assertNotNull(root.findViewById(R.id.top_bar));
        assertNotNull(root.findViewById(R.id.btn_back));
        assertNotNull(root.findViewById(R.id.tv_title));
        assertNotNull(root.findViewById(R.id.tv_guidelines_title));
        assertNotNull(root.findViewById(R.id.tv_guidelines_content));
    }
}
