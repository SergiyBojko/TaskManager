package com.serhiyboiko.taskmanager.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.dialog.MaterialDialogFragment;

/**
 * Created by Amegar on 11.06.2016.
 */
public class SettingsActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback,
        SettingsActivityClickListener, MaterialDialogFragment.DialogListener {

    final static String IDLE_TASK_BACKGROUND_COLOR = "idle_task_background_color";
    final static String STARTED_TASK_BACKGROUND_COLOR = "started_task_background_color";
    final static String FINISHED_TASK_BACKGROUND_COLOR = "finished_task_background_color";
    final static String RESTORE_DEFAULT_COLORS = "restore_defaults";

    private String mSelectedPreferenceKey;
    private SharedPreferences mSharedPreferences;


    public static class SettingsFragment extends PreferenceFragment{
        private SettingsActivityClickListener mListener;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            mListener = (SettingsActivityClickListener)getActivity();

            Preference idleTaskColor = findPreference(IDLE_TASK_BACKGROUND_COLOR);
            idleTaskColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mListener.onPreferenceClick(preference);
                    return true;
                }
            });

            Preference startedTaskColor = findPreference(STARTED_TASK_BACKGROUND_COLOR);
            startedTaskColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mListener.onPreferenceClick(preference);
                    return true;
                }
            });

            Preference finishedTaskColor = findPreference(FINISHED_TASK_BACKGROUND_COLOR);
            finishedTaskColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mListener.onPreferenceClick(preference);
                    return true;
                }
            });

            Preference restoreDefaultColors = findPreference(RESTORE_DEFAULT_COLORS);
            restoreDefaultColors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mListener.onPreferenceClick(preference);
                    return true;
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportActionBar().setTitle(R.string.settings_activity_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (getFragmentManager().findFragmentById(R.id.settings_activity_container) == null) {
            getFragmentManager().beginTransaction().replace(R.id.settings_activity_container, new SettingsFragment()).commit();
        }
    }

    @Override
    public void onPreferenceClick(Preference preference) {
        mSelectedPreferenceKey = preference.getKey();
        switch (mSelectedPreferenceKey){
            case IDLE_TASK_BACKGROUND_COLOR:
                new ColorChooserDialog.Builder(this, R.string.choose_idle_task_color)
                        .doneButton(R.string.md_done_label)
                        .cancelButton(R.string.md_cancel_label)
                        .backButton(R.string.md_back_label)
                        .allowUserColorInputAlpha(false)
                        .preselect(mSharedPreferences.getInt(mSelectedPreferenceKey,
                                getResources().getColor(R.color.idle_task_background_color_default)))
                        .dynamicButtonColor(false)
                        .show();
                break;
            case STARTED_TASK_BACKGROUND_COLOR:
                new ColorChooserDialog.Builder(this, R.string.choose_started_task_color)
                        .doneButton(R.string.md_done_label)
                        .cancelButton(R.string.md_cancel_label)
                        .backButton(R.string.md_back_label)
                        .allowUserColorInputAlpha(false)
                        .preselect(mSharedPreferences.getInt(mSelectedPreferenceKey,
                                getResources().getColor(R.color.started_task_background_color_default)))
                        .dynamicButtonColor(false)
                        .show();
                break;
            case FINISHED_TASK_BACKGROUND_COLOR:
                new ColorChooserDialog.Builder(this, R.string.choose_finished_task_color)
                        .doneButton(R.string.md_done_label)
                        .cancelButton(R.string.md_cancel_label)
                        .backButton(R.string.md_back_label)
                        .allowUserColorInputAlpha(false)
                        .preselect(mSharedPreferences.getInt(mSelectedPreferenceKey,
                                getResources().getColor(R.color.finished_task_background_color_default)))
                        .dynamicButtonColor(false)
                        .show();
                break;
            case RESTORE_DEFAULT_COLORS:
                MaterialDialogFragment.newInstance(getString(R.string.dialog_restore_default_colors))
                        .show(getSupportFragmentManager(), getString(R.string.dialog_restore_default_colors));
        }

    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        mSharedPreferences.edit().putInt(mSelectedPreferenceKey, selectedColor).commit();
        setResult(RESULT_OK);
    }

    @Override
    public void onPositive() {
        mSharedPreferences.edit()
                .remove(IDLE_TASK_BACKGROUND_COLOR)
                .remove(STARTED_TASK_BACKGROUND_COLOR)
                .remove(FINISHED_TASK_BACKGROUND_COLOR)
                .commit();
        setResult(RESULT_OK);
    }
}

interface SettingsActivityClickListener{
    void onPreferenceClick(Preference preference);
}