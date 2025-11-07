package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.string_events.R;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AdminProfileAdapterItemTest {

    static class Row {
        final String name;
        final String email;
        final String password;
        Row(String name, String email, String password) {
            this.name = name; this.email = email; this.password = password;
        }
    }

    static class TestProfileAdapter extends RecyclerView.Adapter<TestProfileAdapter.VH> {
        private final List<Row> data;

        TestProfileAdapter(List<Row> data) { this.data = data; }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvName, tvEmail, tvPwd;
            VH(@NonNull View itemView) {
                super(itemView);
                tvName  = itemView.findViewById(R.id.profile_name);
                tvEmail = itemView.findViewById(R.id.profile_email);
                tvPwd   = itemView.findViewById(R.id.profile_password);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_profile, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Row r = data.get(position);
            h.tvName.setText("Name: " + r.name);
            h.tvEmail.setText("Email: " + r.email);
            h.tvPwd.setText("Password: " + r.password);
        }

        @Override public int getItemCount() { return data.size(); }
    }

    private static int launchWithRecyclerAndAdapter() {
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            rows.add(new Row("User " + i, "user" + i + "@demo.com", "pw" + i));
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.string_events", "com.example.string_events.UiHostActivity");
        intent.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, android.R.layout.list_content);

        ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(intent);
        sc.onActivity(act -> {
            RecyclerView rv = new RecyclerView(act);
            rv.setLayoutManager(new LinearLayoutManager(act));
            rv.setAdapter(new TestProfileAdapter(rows));
            act.setContentView(rv);
        });
        return rows.size();
    }

    @Test
    public void row0_bindsCoreFields_and_isVisible() {
        launchWithRecyclerAndAdapter();

        onView(isAssignableFrom(RecyclerView.class))
                .perform(scrollToPosition(0));

        onView(withText(Matchers.startsWith("Name: User 1")))
                .check(matches(isDisplayed()));
        onView(withText(Matchers.startsWith("Email: user1@demo.com")))
                .check(matches(isDisplayed()));
        onView(withText(Matchers.startsWith("Password: pw1")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void scroll_to_last_row_and_click_doesNotCrash() {
        int count = launchWithRecyclerAndAdapter();
        int last = count - 1;

        onView(isAssignableFrom(RecyclerView.class))
                .perform(scrollToPosition(last))
                .perform(actionOnItemAtPosition(last, click()));

        onView(withText(Matchers.startsWith("Name: User " + (last + 1))))
                .check(matches(isDisplayed()));
    }
}
