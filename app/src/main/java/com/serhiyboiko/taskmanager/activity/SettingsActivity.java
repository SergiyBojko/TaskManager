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
import com.serhiyboiko.taskmanager.dialog.ConfirmationDialog;
import com.serhiyboiko.taskmanager.dialog.NumberInputDialog;

public class SettingsActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback,
        SettingsActivityClickListener, ConfirmationDialog.DialogListener, NumberInputDialog.DialogListener {

    final static String IDLE_TASK_BACKGROUND_COLOR = "idle_task_background_color";
    final static String STARTED_TASK_BACKGROUND_COLOR = "started_task_background_color";
    final static String FINISHED_TASK_BACKGROUND_COLOR = "finished_task_background_color";
    final static String RESTORE_DEFAULT_COLORS = "restore_defaults";
    final static String MAXIMUM_TASK_DURATION = "maximum_task_duration";

    private String mSelectedPreferenceKey;
    private SharedPreferences mSharedPreferences;

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener,
            SharedPreferences.OnSharedPreferenceChangeListener{
        private SettingsActivityClickListener mListener;
        private Preference mIdleTaskColor;
        private Preference mStartedTaskColor;
        private Preference mFinishedTaskColor;
        private Preference mRestoreDefaultColors;
        private Preference mMaxTaskDuration;
        private SharedPreferences mSharedPreferences;
        private AppCompatActivity mActivity;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
            mListener = (SettingsActivityClickListener)getActivity();
            mActivity = (AppCompatActivity) getActivity();

            mIdleTaskColor = findPreference(IDLE_TASK_BACKGROUND_COLOR);
            mIdleTaskColor.setOnPreferenceClickListener(this);

            mStartedTaskColor = findPreference(STARTED_TASK_BACKGROUND_COLOR);
            mStartedTaskColor.setOnPreferenceClickListener(this);

            mFinishedTaskColor = findPreference(FINISHED_TASK_BACKGROUND_COLOR);
            mFinishedTaskColor.setOnPreferenceClickListener(this);

            mRestoreDefaultColors = findPreference(RESTORE_DEFAULT_COLORS);
            mRestoreDefaultColors.setOnPreferenceClickListener(this);

            mMaxTaskDuration = findPreference(MAXIMUM_TASK_DURATION);
            mMaxTaskDuration.setOnPreferenceClickListener(this);
            int currentMaxDuration = mSharedPreferences.getInt(MAXIMUM_TASK_DURATION, 0);
            if (currentMaxDuration > 0){
                mMaxTaskDuration.setSummary(String.format(mActivity.getString(R.string.task_will_finish_at_sec), currentMaxDuration));
            } else {
                mMaxTaskDuration.setSummary(mActivity.getString(R.string.task_will_not_finish));
            }
        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            mListener.onPreferenceClick(preference);
            return true;
        }



        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key){
                case MAXIMUM_TASK_DURATION:
                    int currentMaxDuration = mSharedPreferences.getInt(MAXIMUM_TASK_DURATION, 0);
                    if (currentMaxDuration > 0){
                        mMaxTaskDuration.setSummary(String.format(mActivity.getString(R.string.task_will_finish_at_sec), currentMaxDuration));
                    } else {
                        mMaxTaskDuration.setSummary(mActivity.getString(R.string.task_will_not_finish));
                    }
                    break;
            }
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
                ConfirmationDialog.newInstance(R.string.dialog_restore_default_colors)
                        .show(getSupportFragmentManager(), getString(R.string.dialog_restore_default_colors));
                break;
            case MAXIMUM_TASK_DURATION:
                int currentMaxDuration = mSharedPreferences.getInt(MAXIMUM_TASK_DURATION, 0);
                NumberInputDialog.newInstance(R.string.dialog_set_default_task_duration,
                        R.string.dialog_set_max_task_duration_description, Integer.toString(currentMaxDuration))
                        .show(getSupportFragmentManager(), getString(R.string.dialog_set_default_task_duration));
                break;
        }

    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        mSharedPreferences.edit().putInt(mSelectedPreferenceKey, selectedColor).commit();
        setResult(RESULT_OK);
    }

    @Override
    public void onPositive(int titleId) {
        switch (titleId){
            case R.string.dialog_restore_default_colors:
                mSharedPreferences.edit()
                        .remove(IDLE_TASK_BACKGROUND_COLOR)
                        .remove(STARTED_TASK_BACKGROUND_COLOR)
                        .remove(FINISHED_TASK_BACKGROUND_COLOR)
                        .commit();
                setResult(RESULT_OK);
                break;
        }
    }

    @Override
    public void onInput(int titleId, CharSequence input) {
        switch (titleId){
            case R.string.dialog_set_default_task_duration:
                String inputString = input.toString();
                if (inputString.equals("")){
                    inputString = "0";
                }
                mSharedPreferences.edit().putInt(MAXIMUM_TASK_DURATION, Integer.parseInt(inputString)).commit();
                break;

        }
    }
}

interface SettingsActivityClickListener{
    void onPreferenceClick(Preference preference);
}