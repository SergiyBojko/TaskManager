package com.serhiyboiko.taskmanager.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.activity.EditTaskActivity;
import com.serhiyboiko.taskmanager.activity.TaskListActivity;
import com.serhiyboiko.taskmanager.model.PauseInfo;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.model.TaskExecInfo;
import com.serhiyboiko.taskmanager.utils.file_io.FileIO;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import io.realm.RealmResults;

public class TaskListAdapter extends RecyclerSwipeAdapter<TaskListAdapter.ViewHolder>{
    public static final String START_DATE = "%02d.%02d.%4d %02d:%02d\n";
    public static final String END_DATE = "%02d.%02d.%4d %02d:%02d\n";
    public static final String TIME_SPEND = "%s %02d:%02d:%02d";
    private final static String HH_MM_SS = " %02d:%02d:%02d";
    public static final int BLACK_FONT_COLOUR = 0xCC404040;
    public static final int WHITE_FONT_COLOUR = 0xE5F1F1F1;
    private static final String REQUEST_CODE_EXTRA = "request_code";
    private static final String TAG = "TaskListAdapter";


    private RealmResults<Task> mTaskRealmResults;
    private int[] mBackgroundColors;
    private Context mContext;
    private RealmIO mRealmIO;

    private static final int EDIT_TASK_REQUEST = 2;

    private final static int IDLE_TASK = 0;
    private final static int STARTED_TASK = 1;
    private final static int ENDED_TASK = 2;

    public TaskListAdapter(Context context, RealmResults<Task> taskList,
                           int[] backgroundColors, RealmIO realmIO){
        mTaskRealmResults = taskList;
        mBackgroundColors = backgroundColors;
        mContext = context;
        mRealmIO = realmIO;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_item, parent, false);
        SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(R.id.swipe_layout);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, v.findViewById(R.id.bottom_layout));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder (ViewHolder holder, int position){
        mItemManger.bindView(holder.itemView, position);
        Task item = mTaskRealmResults.get(position);
        onBindViewHolder(holder, item);
    }

    //потрібен для вимірювання розмірів в'юшки при автогенерації бо RealmResults не обновляєтсья в циклах
    public void onBindViewHolder(ViewHolder holder, Task item) {
        int currentBackgroundColor;
        StringBuilder date = new StringBuilder();
        setText(holder, item);
        setDateText(holder, date, item);
        currentBackgroundColor = setTaskBackgroundColor(holder, item);
        setItemTextColor(holder, currentBackgroundColor);
        setStartStopTaskButtonImage(holder, item);
        setPauseContinueButtonState(holder, item);
        setSwipeMenuButtonsVisibility(holder, item);
        setAvatar(holder, item);
        setPeriodicTaskIcon(holder, item, currentBackgroundColor);
        setLocationIndicator(holder, item);
    }

    private void setLocationIndicator(ViewHolder holder, Task item) {
        if (item.isAssignedToLocation()){
            holder.locationIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.locationIndicator.setVisibility(View.GONE);
        }
    }

    private void setPeriodicTaskIcon(ViewHolder holder, Task item, int currentBackgroundColor) {
        if(item.getPeriod() != 0 && item.getTaskStart() != null){
            holder.periodicTaskIcon.setVisibility(View.VISIBLE);
            if(isBrightColor(currentBackgroundColor)){
                holder.periodicTaskIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_autorenew_black_24dp));
            } else {
                holder.periodicTaskIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_autorenew_white_24dp));
            }
        } else {
            holder.periodicTaskIcon.setVisibility(View.GONE);
        }

    }

    private void setText(ViewHolder holder, Task item) {
        holder.title.setText(item.getTitle());
        String commentary = item.getCommentary();
        if (commentary.isEmpty()){
            holder.commentary.setVisibility(View.GONE);
        } else {
            holder.commentary.setVisibility(View.VISIBLE);
            holder.commentary.setText(commentary);
        }
    }

    private void setPauseContinueButtonState(ViewHolder holder, Task item) {
        if(item.getTaskEnd() != null || item.getTaskStart() == null){
            holder.pauseContinueTask.setVisibility(View.INVISIBLE);
        } else {
            holder.pauseContinueTask.setVisibility(View.VISIBLE);
            if (!item.isPaused()){
                holder.pauseContinueTask.setBackgroundResource(R.drawable.button_pause_selector);
            } else {
                holder.pauseContinueTask.setBackgroundResource(R.drawable.button_start_selector);
            }
        }
    }

    private void setAvatar(ViewHolder holder, Task item) {
        final String avatarLocation = item.getAvatarLocation();
        final ViewHolder fHolder = holder;
        if ((avatarLocation != null) && (!avatarLocation.equals(""))){
            Glide.with(mContext)
                    .load(avatarLocation)
                    .signature(new StringSignature(Long.toString(item.getLastAvatarEditTime())))
                    .placeholder(R.mipmap.avatar_placeholder)
                    .crossFade()
                    .into(holder.taskAvatar);
        } else {
            Glide.with(mContext)
                    .load(R.mipmap.default_avatar)
                    .placeholder(R.mipmap.avatar_placeholder)
                    .crossFade()
                    .into(holder.taskAvatar);
        }
    }

    private void setSwipeMenuButtonsVisibility(ViewHolder holder, Task item) {
        if(item.getTaskEnd() != null){
            holder.cancelTaskExecution.setVisibility(View.VISIBLE);
            holder.oneStepBackInTaskExecution.setVisibility(View.VISIBLE);
        } else {
            if (item.getTaskStart() != null){
                holder.cancelTaskExecution.setVisibility(View.GONE);
                holder.oneStepBackInTaskExecution.setVisibility(View.VISIBLE);
            } else {
                holder.cancelTaskExecution.setVisibility(View.GONE);
                holder.oneStepBackInTaskExecution.setVisibility(View.GONE);
            }
        }
    }

    private void setStartStopTaskButtonImage(ViewHolder holder, Task item) {
        if(item.getTaskEnd() != null){
            holder.startStopTask.setVisibility(View.INVISIBLE);
        } else {
            holder.startStopTask.setVisibility(View.VISIBLE);
            if (item.getTaskStart() != null){
                holder.startStopTask.setBackgroundResource(R.drawable.button_stop_selector);
            } else {
                holder.startStopTask.setBackgroundResource(R.drawable.button_start_selector);
            }
        }
    }

    private void setItemTextColor(ViewHolder holder, int currentBackgroundColor) {
        if(isBrightColor(currentBackgroundColor)){
            holder.time.setTextColor(BLACK_FONT_COLOUR);
            holder.title.setTextColor(BLACK_FONT_COLOUR);
            holder.commentary.setTextColor(BLACK_FONT_COLOUR);
        } else {
            holder.time.setTextColor(WHITE_FONT_COLOUR);
            holder.title.setTextColor(WHITE_FONT_COLOUR);
            holder.commentary.setTextColor(WHITE_FONT_COLOUR);
        }
    }

    private int setTaskBackgroundColor(ViewHolder holder, Task item) {
        int currentBackgroundColor;
        if(item.getTaskEnd() != null){
            currentBackgroundColor = mBackgroundColors[ENDED_TASK];
        } else {
            if (item.getTaskStart() != null){
                currentBackgroundColor = mBackgroundColors[STARTED_TASK];
            } else {
                currentBackgroundColor = mBackgroundColors[IDLE_TASK];
            }
        }
        holder.itemCard.setCardBackgroundColor(currentBackgroundColor);
        return currentBackgroundColor;
    }

    private void setDateText(ViewHolder holder, StringBuilder date, Task item) {

        if(item.getTaskStart() == null){
            holder.time.setVisibility(View.INVISIBLE);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            date.append(String.format(START_DATE, item.getTaskStart().get(GregorianCalendar.DAY_OF_MONTH),
                    item.getTaskStart().get(GregorianCalendar.MONTH), item.getTaskStart().get(GregorianCalendar.YEAR),
                    item.getTaskStart().get(GregorianCalendar.HOUR_OF_DAY), item.getTaskStart().get(GregorianCalendar.MINUTE)));
            if (item.getTaskEnd() != null){
                date.append(String.format(END_DATE, item.getTaskEnd().get(GregorianCalendar.DAY_OF_MONTH),
                        item.getTaskEnd().get(GregorianCalendar.MONTH), item.getTaskEnd().get(GregorianCalendar.YEAR),
                        item.getTaskEnd().get(GregorianCalendar.HOUR_OF_DAY), item.getTaskEnd().get(GregorianCalendar.MINUTE)));
                long elapsedMills = item.getTimeSpend();
                int elapsedHours = (int) TimeUnit.MILLISECONDS.toHours(elapsedMills);
                int elapsedMinutes = (int)(TimeUnit.MILLISECONDS.toMinutes(elapsedMills) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMills)));
                int elapsedSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(elapsedMills) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMills)));
                date.append(String.format(TIME_SPEND, mContext.getString(R.string.task_finished_in), elapsedHours, elapsedMinutes, elapsedSeconds));
            }
            holder.time.setText(date.toString());
        }
    }

    public void setBackgroundColors(int[] backgroundColors){
        mBackgroundColors = backgroundColors;
    }

    public void setTaskRealmResults (RealmResults<Task> realmResults){
        mTaskRealmResults = realmResults;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTaskRealmResults.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    private boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return true;

        boolean rtnValue = false;

        int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };

        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

        // color is light
        if (brightness >= 200) {
            rtnValue = true;
        }

        return rtnValue;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title;
        TextView commentary;
        TextView time;
        CardView itemCard;
        SwipeLayout swipeLayout;
        ImageButton startStopTask;
        ImageButton pauseContinueTask;
        ImageView taskAvatar;
        ImageView periodicTaskIcon;
        ImageView locationIndicator;


        //swipe menu buttons
        ImageButton editTask;
        ImageButton deleteTask;
        ImageButton cancelTaskExecution;
        ImageButton oneStepBackInTaskExecution;



        ViewHolder(View v) {
            super(v);
            title = (TextView)v.findViewById(R.id.list_item_title);
            commentary = (TextView)v.findViewById(R.id.list_item_commentary);
            time = (TextView)v.findViewById(R.id.list_item_time);
            itemCard = (CardView)v.findViewById(R.id.item_card);
            swipeLayout = (SwipeLayout)v.findViewById(R.id.swipe_layout);
            startStopTask = (ImageButton)v.findViewById(R.id.start_stop_task_image_button);
            pauseContinueTask = (ImageButton)v.findViewById(R.id.pause_continue_task_image_button);
            periodicTaskIcon = (ImageView)v.findViewById(R.id.list_item_periodicity_indicator);
            editTask = (ImageButton)v.findViewById(R.id.edit_task_image_button);
            deleteTask = (ImageButton)v.findViewById(R.id.delete_task_image_button);
            cancelTaskExecution = (ImageButton)v.findViewById(R.id.cancel_task_execution_image_button);
            oneStepBackInTaskExecution = (ImageButton)v.findViewById(R.id.one_step_back_image_button);
            taskAvatar = (ImageView)v.findViewById(R.id.list_item_avatar);
            locationIndicator = (ImageView)v.findViewById(R.id.location_indicator);

            startStopTask.setOnClickListener(this);
            pauseContinueTask.setOnClickListener(this);
            editTask.setOnClickListener(this);
            deleteTask.setOnClickListener(this);
            cancelTaskExecution.setOnClickListener(this);
            oneStepBackInTaskExecution.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            int position = getAdapterPosition();
            Task item = mTaskRealmResults.get(position);

            switch (viewId){
                case R.id.start_stop_task_image_button:
                    if (item.getTaskStart() == null) {
                        mRealmIO.getRealm().beginTransaction();
                        item.setTaskStart(new GregorianCalendar());
                        mRealmIO.getRealm().commitTransaction();
                    } else {
                        if (item.getTaskEnd() == null) {
                            mRealmIO.getRealm().beginTransaction();
                            item.setTaskEnd(new GregorianCalendar());
                            TaskExecInfo.createTaskExecInfo(mRealmIO, item);
                            mRealmIO.getRealm().commitTransaction();
                            long elapsedTimeInMills = item.getTimeSpend();
                            int hours = (int) TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills);
                            int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills)));
                            int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(elapsedTimeInMills) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills)));
                            Snackbar.make(((Activity)mContext).findViewById(R.id.task_list_activity_container),
                                    mContext.getString(R.string.task_finished_in) + String.format(HH_MM_SS, hours, minutes, seconds), Snackbar.LENGTH_SHORT).show();
                            Log.i(TAG, "task info size : " + item.getTaskExecInfoList().size());
                            for (TaskExecInfo t:item.getTaskExecInfoList()){
                                Log.i(TAG, "task duration : " + t.getDuration());
                            }

                        }
                    }
                    notifyDataSetChanged();
                    break;
                case R.id.pause_continue_task_image_button:
                    if(!item.isPaused()){
                        mRealmIO.getRealm().beginTransaction();
                        PauseInfo newPause = new PauseInfo();
                        newPause.start();
                        PauseInfo managedPause = mRealmIO.putPauseInfo(newPause);

                        item.setPaused(true);
                        item.getPauseInfoList().add(managedPause);
                        mRealmIO.getRealm().commitTransaction();
                    } else {
                        mRealmIO.getRealm().beginTransaction();
                        item.setPaused(false);

                        int pausesAmount = item.getPauseInfoList().size();
                        PauseInfo pauseToFinish = item.getPauseInfoList().get(pausesAmount - 1);
                        pauseToFinish.finish();
                        mRealmIO.getRealm().commitTransaction();
                    }
                    break;
                case R.id.delete_task_image_button:
                    swipeLayout.close(false);
                    FileIO.removeFile(item.getAvatarLocation());
                    mRealmIO.hideTask(item);
                    notifyDataSetChanged();
                    break;
                case R.id.edit_task_image_button:
                    swipeLayout.close(false);
                    Task taskToEdit = mTaskRealmResults.get(position);
                    Intent intent = new Intent(mContext, EditTaskActivity.class);
                    intent.putExtra(TaskListActivity.ITEM_POSITION_EXTRA, position);
                    intent.putExtra(TaskListActivity.TASK_ID_EXTRA, taskToEdit.getId());
                    intent.putExtra(TaskListActivity.TITLE_EXTRA, taskToEdit.getTitle());
                    intent.putExtra(TaskListActivity.COMMENTARY_EXTRA, taskToEdit.getCommentary());
                    intent.putExtra(TaskListActivity.MAX_DURATION_EXTRA, taskToEdit.getTaskMaxDuration());
                    intent.putExtra(TaskListActivity.TASK_FREQUENCY_EXTRA, taskToEdit.getPeriod());
                    intent.putExtra(TaskListActivity.AVATAR_PATH_EXTRA, taskToEdit.getAvatarLocation());
                    intent.putExtra(TaskListActivity.AVATAR_EDIT_TIME_EXTRA, taskToEdit.getLastAvatarEditTime());
                    intent.putExtra(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, taskToEdit.isAssignedToLocation());
                    intent.putExtra(TaskListActivity.LATITUDE_EXTRA, taskToEdit.getLatitude());
                    intent.putExtra(TaskListActivity.LONGITUDE_EXTRA, taskToEdit.getLongitude());
                    intent.putExtra(REQUEST_CODE_EXTRA, EDIT_TASK_REQUEST);
                    ((Activity)mContext).startActivityForResult(intent, EDIT_TASK_REQUEST);
                    break;
                case R.id.one_step_back_image_button:
                    if(item.getTaskEnd() != null){
                        mRealmIO.getRealm().beginTransaction();
                        item.setTaskRestart(new GregorianCalendar());
                        item.setTaskEnd(null);
                        item.getPauseInfoList().deleteAllFromRealm();
                        mRealmIO.getRealm().commitTransaction();
                    } else {
                        mRealmIO.getRealm().beginTransaction();
                        item.setTaskStart(null);
                        item.setTaskRestart(null);
                        item.getPauseInfoList().deleteAllFromRealm();
                        mRealmIO.getRealm().commitTransaction();
                    }
                    notifyDataSetChanged();
                    swipeLayout.close(true);
                    break;
                case R.id.cancel_task_execution_image_button:
                    mRealmIO.getRealm().beginTransaction();
                    item.setTaskStart(null);
                    item.setTaskRestart(null);
                    item.setTaskEnd(null);
                    item.getPauseInfoList().deleteAllFromRealm();
                    mRealmIO.getRealm().commitTransaction();
                    notifyDataSetChanged();
                    swipeLayout.close(true);
                    break;
            }

        }
    }
}
