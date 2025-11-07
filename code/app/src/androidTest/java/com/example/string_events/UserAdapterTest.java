package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class UserAdapterTest {

    @Rule
    public ActivityScenarioRule<UiHostActivity> rule =
            new ActivityScenarioRule<>(new Intent(
                    ApplicationProvider.getApplicationContext(),
                    UiHostActivity.class)
                    .putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, android.R.layout.list_content)
            );

    static class Row {
        final String name;
        final String email;
        final boolean showBadge;
        final String badgeText;

        Row(String name, String email, boolean showBadge, String badgeText) {
            this.name = name;
            this.email = email;
            this.showBadge = showBadge;
            this.badgeText = badgeText;
        }
    }

    static class TestAdapter extends RecyclerView.Adapter<TestAdapter.VH> {
        final List<Row> data;

        TestAdapter(List<Row> data) { this.data = data; }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvName, tvEmail, tvBadge;
            VH(@NonNull View itemView) {
                super(itemView);
                tvName  = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvBadge = itemView.findViewById(R.id.tvBadge);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_row, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Row r = data.get(pos);
            h.tvName.setText("Name: " + r.name);
            h.tvEmail.setText("Email: " + r.email);
            if (r.showBadge) {
                h.tvBadge.setText(r.badgeText);
                h.tvBadge.setVisibility(View.VISIBLE);
            } else {
                h.tvBadge.setVisibility(View.GONE);
            }
        }

        @Override public int getItemCount() { return data.size(); }
    }

    private void launchWithData(List<Row> rows) {
        rule.getScenario().onActivity(host -> {
            RecyclerView rv = new RecyclerView(host);
            rv.setId(android.R.id.list);
            rv.setLayoutManager(new LinearLayoutManager(host));
            rv.setAdapter(new TestAdapter(rows));
            host.setContentView(rv);
        });
    }

    @Test
    public void firstRow_bindsNameAndEmail_badgeHidden() {
        List<Row> rows = Arrays.asList(
                new Row("Alice", "alice@mail.com", false, ""),
                new Row("Bob",   "bob@mail.com",   true,  "invited")
        );
        launchWithData(rows);

        onView(withId(android.R.id.list))
                .perform(actionOnItemAtPosition(0, click()));
        onView(recyclerItemAt(0)).check(matches(hasDescendant(withText("Name: Alice"))));
        onView(recyclerItemAt(0)).check(matches(hasDescendant(withText("Email: alice@mail.com"))));
    }

    @Test
    public void secondRow_showsBadgeText_invited() {
        List<Row> rows = Arrays.asList(
                new Row("Alice", "alice@mail.com", false, ""),
                new Row("Bob",   "bob@mail.com",   true,  "invited")
        );
        launchWithData(rows);

        onView(withId(android.R.id.list))
                .perform(actionOnItemAtPosition(1, click()));
        onView(recyclerItemAt(1))
                .check(matches(AllOf.allOf(
                        hasDescendant(withText("Name: Bob")),
                        hasDescendant(withText("Email: bob@mail.com")),
                        hasDescendant(AllOf.allOf(withId(R.id.tvBadge), withText("invited")))
                )));
    }

    private static Matcher<View> recyclerItemAt(int position) {
        return new RecyclerViewChildMatcher(android.R.id.list).atPosition(position);
    }
}

