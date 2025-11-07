package com.example.string_events;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/** 匹配 RecyclerView 某一行的根视图。 */
public class RecyclerViewChildMatcher {
    private final int recyclerId;

    public RecyclerViewChildMatcher(@IdRes int recyclerId) {
        this.recyclerId = recyclerId;
    }

    public Matcher<View> atPosition(int position) {
        return new TypeSafeMatcher<View>() {
            @Override protected boolean matchesSafely(View view) {
                View rv = view.getRootView().findViewById(recyclerId);
                if (!(rv instanceof RecyclerView)) return false;
                RecyclerView recyclerView = (RecyclerView) rv;
                RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
                if (vh == null) return false; // 尚未绑定/不可见
                return view == vh.itemView;
            }

            @Override public void describeTo(Description description) {
                description.appendText("RecyclerView item at position " + position);
            }
        };
    }
}
