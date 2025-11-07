package com.example.string_events;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertNotNull;

public final class TestViews {
    private TestViews() {}

    public static View inflate(@LayoutRes int layout) {
        Context base = ApplicationProvider.getApplicationContext();
        Context themed = new ContextThemeWrapper(
                base, com.google.android.material.R.style.Theme_Material3_Light_NoActionBar);
        return LayoutInflater.from(themed).inflate(layout, null, false);
    }

    public static void assertHas(View root, @IdRes int... ids) {
        for (int id : ids) assertNotNull(root.findViewById(id));
    }
}
