package com.serhiyboiko.taskmanager.utils.tutorial;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsDeserializer;
import com.serhiyboiko.taskmanager.utils.sharedprefs.SharedPrefsSerializer;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import io.realm.RealmResults;

/**
 * Created by user on 25.07.2016.
 */
public class Tutorial {

    private static final String NEW_TASK_TUTORIAL_ID = "0";
    private static final String SORTING_TUTORIAL_ID = "1";
    private static final String SETTINGS_TUTORIAL_ID = "2";
    private static final String LOCATION_TUTORIAL_ID = "3";

    public static void taskListActivityTutorial (final Activity activity, Menu menu){
        RealmIO realmIO = new RealmIO(activity);
        RealmResults<Task> tasks = realmIO.getAllTasks();
        int taskCount = tasks.size();
        View targetView;
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.action_bar);
        View menuLayout = toolbar.getChildAt(1);
        final MenuItem settings = menu.findItem(R.id.menu_settings);
        final MenuItem sorting = menu.findItem(R.id.menu_sort);

        boolean isNewTaskTutorialCompleted = SharedPrefsDeserializer.isNewTaskTutorialCompleted(activity);
        boolean isSortingTutorialCompleted = SharedPrefsDeserializer.isSortingTutorialCompleted(activity);
        boolean isSettingsTutorialCompleted = SharedPrefsDeserializer.isSettingsTutorialCompleted(activity);

        targetView = activity.findViewById(R.id.new_task_button);

        if (!isNewTaskTutorialCompleted){
            final MaterialIntroView newTaskTutorial = new MaterialIntroView.Builder(activity)
                    .enableDotAnimation(false)
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.NORMAL)
                    .enableFadeAnimation(true)
                    .performClick(true)
                    .setInfoText(activity.getString(R.string.new_task_button_tutorial_title) + "\n"
                            + activity.getString(R.string.new_task_button_tutorial_text))
                    .setTarget(targetView)
                    .setUsageId(NEW_TASK_TUTORIAL_ID)
                    .setListener(new MaterialIntroListener() {
                        @Override
                        public void onUserClicked(String s) {
                            SharedPrefsSerializer.newTaskTutorialCompleted(activity, true);
                        }
                    })
                    .show();
        }


        if (taskCount > 0 && !isSettingsTutorialCompleted && isNewTaskTutorialCompleted){
            settings.setActionView(R.layout.settings_action_view);
            final MaterialIntroView settingsTutorial = new MaterialIntroView.Builder(activity)
                    .enableDotAnimation(false)
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.NORMAL)
                    .setDelayMillis(500)
                    .enableFadeAnimation(true)
                    .performClick(false)
                    .setInfoText(activity.getString(R.string.settings_tutorial_title)
                            + "\n"
                            + activity.getString(R.string.settings_tutorial_text))
                    .setTarget(settings.getActionView())
                    .setUsageId(SETTINGS_TUTORIAL_ID)
                    .setListener(new MaterialIntroListener() {
                        @Override
                        public void onUserClicked(String s) {
                            settings.setActionView(null);
                            SharedPrefsSerializer.settingsTutorialCompleted(activity, true);
                        }
                    }).show();
        }

        if (taskCount > 2 && !isSortingTutorialCompleted && isSettingsTutorialCompleted){
            sorting.setActionView(R.layout.sorting_action_view);
            final MaterialIntroView sortingTutorial = new MaterialIntroView.Builder(activity)
                    .enableDotAnimation(false)
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.NORMAL)
                    .setDelayMillis(500)
                    .enableFadeAnimation(true)
                    .performClick(false)
                    .setInfoText(activity.getString(R.string.sorting_tutorial_title)
                            + "\n"
                            + activity.getString(R.string.sorting_tutorial_text))
                    .setTarget(sorting.getActionView())
                    .setUsageId(SORTING_TUTORIAL_ID)
                    .setListener(new MaterialIntroListener() {
                        @Override
                        public void onUserClicked(String s) {
                            sorting.setActionView(null);
                            SharedPrefsSerializer.sortingTutorialCompleted(activity, true);
                        }
                    }).show();
        }
    }

    public static void taskEditActivityTutorial (final Activity activity){
        View targetView;
        MaterialIntroView materialIntroView;

        boolean isTaskLocationTutorialCompleted = SharedPrefsDeserializer.isTaskLocationTutorialCompleted(activity);

        if (!isTaskLocationTutorialCompleted){
            targetView = activity.findViewById(R.id.map_placeholder);
            materialIntroView = new MaterialIntroView.Builder(activity)
                    .enableDotAnimation(false)
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.NORMAL)
                    .setDelayMillis(500)
                    .enableFadeAnimation(true)
                    .performClick(false)
                    .setInfoText(activity.getString(R.string.task_location_tutorial_title)
                            + "\n"
                            + activity.getString(R.string.task_location_tutorial_text))
                    .setTarget(targetView)
                    .setUsageId(LOCATION_TUTORIAL_ID)
                    .setListener(new MaterialIntroListener() {
                        @Override
                        public void onUserClicked(String s) {
                            SharedPrefsSerializer.taskLocationTutorialCompleted(activity, true);
                        }
                    })
                    .show();
        }
    }
}
