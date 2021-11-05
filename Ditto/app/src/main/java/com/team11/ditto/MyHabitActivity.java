package com.team11.ditto;
/*
Role: To display the listview of Habits for a user in the "My Habits" tab
Allow a user to add a habit, swipe left to delete a habit
Goals:
    -Implement occurrence tracking
    -Visually make it better
 */

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.team11.ditto.habit.AddHabitFragment;
import com.team11.ditto.habit.Habit;
import com.team11.ditto.habit.HabitRecyclerAdapter;
import com.team11.ditto.habit.ViewHabitActivity;
import com.team11.ditto.interfaces.Firebase;
import com.team11.ditto.interfaces.SwitchTabs;
import com.team11.ditto.login.ActiveUser;

import java.util.ArrayList;

/**To display the listview of Habits for a user in the "My Habits" tab
 *Allow a user to add a habit, swipe left to delete a habit
 * TODO:
 *     -A user can add a habit to the database, but cannot delete a habit from the db yet
 *     -Add the days of week to db
 *     -Allow user to edit an existing habit
 *     -Visually make it better
 *     -Get the happy faces for the level of completion for each habit
 *     -WHEN YOU DELETE A HABIT, ALSO DELETE THE HABIT EVENT ITS ASSOCIATED WITH
 * @author Kelly Shih, Aidan Horemans
 */

public class MyHabitActivity extends AppCompatActivity implements
        AddHabitFragment.OnFragmentInteractionListener, SwitchTabs,
        HabitRecyclerAdapter.HabitClickListener, Firebase {

    public static String EXTRA_HABIT = "EXTRA_HABIT";
    private TabLayout tabLayout;

    //Declare variables for the list of habits
    private RecyclerView habitListView;

    private HabitRecyclerAdapter habitRecyclerAdapter;
    private ArrayList<Habit> habitDataList;

    private FirebaseFirestore db;

    private ActiveUser currentUser;

    /**
     * Create the Activity instance for the "My Habits" screen, control flow of actions
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_habit);
        tabLayout = findViewById(R.id.tabs);
        db = FirebaseFirestore.getInstance();

        setTitle("My Habits");

        habitDataList = new ArrayList<Habit>();
        habitListView = findViewById(R.id.list);
        tabLayout = findViewById(R.id.tabs);

        habitRecyclerAdapter = new HabitRecyclerAdapter(habitDataList, this, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        habitListView.setLayoutManager(manager);
        habitListView.setAdapter(habitRecyclerAdapter);

        // Load habits
        currentUser = new ActiveUser();
        db.collection("Habit")
            .whereEqualTo("uid", currentUser.getUID())
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    habitDataList.clear();
                    for (QueryDocumentSnapshot document: value) {
                        String id = document.getId();
                        String title = (String) document.getData().get("title");
                        String reason = (String) document.getData().get("reason");
                        ArrayList<Integer> days = (ArrayList<Integer>) document.getData().get("days_of_week");
                        Habit habit = new Habit(id, title, reason, days);
                        habitDataList.add(habit);
                    }
                    habitRecyclerAdapter.notifyDataSetChanged();
                }
            });

        currentTab(tabLayout, MY_HABITS_TAB);
        switchTabs(this, tabLayout, MY_HABITS_TAB);

        //add habit button action
        final FloatingActionButton addHabitButton = findViewById(R.id.add_habit);
        addHabitButton.setOnClickListener(new View.OnClickListener() {
            /**
             * call the add habit fragment
             * @param view
             */
            @Override
            public void onClick(View view) {
                new AddHabitFragment().show(getSupportFragmentManager(), "ADD_HABIT");
            }
        });

        //Notifies if cloud data changes (from Firebase Interface)
        autoSnapshotListener(db, habitRecyclerAdapter, HABIT_KEY);

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(habitListView);
    }

    /**
     * Adding a habit to the database and listview as the response to the user clicking the "Add" button from the fragment
     *
     * @param newHabit
     */
    @Override
    public void onOkPressed(Habit newHabit) {
        //when the user clicks the add button, we want to add to the db and display the new entry
        if (newHabit.getTitle().length() > 0) {
            pushHabitData(db, newHabit);
        }
    }

    /**
     * To transfer the control to the Main activity/ homepage when the back button is pressed
     */
    public void onBackPressed() {
        Intent intent = new Intent(MyHabitActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        /**
         * To delete an item from the listview and database when a Habit is swiped to the left
         * @param recyclerView
         * @param viewHolder
         * @param target
         * @return
         */
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        /**
         * When an item is swiped left, delete from database and recyclerview
         * @param viewHolder
         * @param direction
         */
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Habit oldEntry = (Habit) habitDataList.get(viewHolder.getAbsoluteAdapterPosition());
            habitDataList.remove(viewHolder.getAbsoluteAdapterPosition());
            habitRecyclerAdapter.notifyDataSetChanged();

            deleteDataMyHabit(db, oldEntry);

        }

        /**
         * To set the background color and background icon for a swipe to delete item in the list.
         * @param c
         * @param recyclerView
         * @param viewHolder
         * @param dX
         * @param dY
         * @param actionState
         * @param isCurrentlyActive
         */
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            //Get recyclerView item from viewholder
            View itemView = viewHolder.itemView;
            ColorDrawable background = new ColorDrawable();
            background.setColor(Color.rgb(0xC9, 0xC9,
                    0xCE));
            background.setBounds((int) (itemView.getRight() + dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.draw(c);

            Drawable deleteIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_round_delete_24);
            int itemTop = itemView.getTop() + ((itemView.getBottom() - itemView.getTop()) - deleteIcon.getIntrinsicHeight()) / 2;
            int itemMargin = ((itemView.getBottom() - itemView.getTop()) - deleteIcon.getIntrinsicHeight()) / 2;
            int itemLeft = itemView.getRight() - itemMargin - deleteIcon.getIntrinsicWidth();
            int itemRight = itemView.getRight() - itemMargin;
            int itemBottom = itemTop + deleteIcon.getIntrinsicHeight();
            deleteIcon.setBounds(itemLeft, itemTop, itemRight, itemBottom);
            deleteIcon.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    /**
     * Opens ViewHabitActivity to view and potentially update the clicked object
     *
     * @param position
     */
    @Override
    public void onHabitClick(int position) {
        habitDataList.get(position);
        Intent intent = new Intent(this, ViewHabitActivity.class);
        intent.putExtra(EXTRA_HABIT, habitDataList.get(position));
        startActivity(intent);
    }


}