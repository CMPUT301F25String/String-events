package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ImageManagementActivity UI 测试（最小稳定版）
 * 覆盖：
 *  1) 页面能启动（带必要 extras）
 *  2) 关键控件可见：返回按钮、标题、图片列表、删除按钮
 *  3) 点击返回/删除不崩溃（先不做业务断言，保证稳定）
 *
 * 如 Activity 依赖其它 extras，请在 makeIntent() 里继续补 .putExtra(...)
 */
@RunWith(AndroidJUnit4.class)
public class ImageManagementActivityTest {

    /** 统一构造一个包含默认 extras 的 Intent，避免因缺少参数导致启动崩溃 */
    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, ImageManagementActivity.class)
                .putExtra("fromTest", true)          // 测试标记：Activity 可据此走本地假数据
                .putExtra("adminId", "test-admin")   // 常见必需参数（若代码不用可忽略）
                .putExtra("eventId", "test-event")
                .putExtra("imageFolder", "test-folder");
    }

    /** 如果页面里使用了 Firebase，这里先确保初始化 */
    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<ImageManagementActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
            onView(withId(R.id.image_list_view)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_delete)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void back_and_delete_doNotCrash() {
        // 单测“返回”，不和“删除”混在同一场景，避免生命周期导致的 NPE
        try (ActivityScenario<ImageManagementActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_back)).perform(click());
        }

        // 单测“删除”点击不崩（若业务需要先选中列表项，这里先做 smoke，后续再补完整流程）
        try (ActivityScenario<ImageManagementActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_delete)).perform(click());
        }
    }
}
