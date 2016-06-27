package com.serhiyboiko.taskmanager.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.activity.EditTaskActivity;
import com.serhiyboiko.taskmanager.model.Task;
import com.serhiyboiko.taskmanager.utils.alarm_manager.TaskAutoFinishManager;
import com.serhiyboiko.taskmanager.utils.realm_io.RealmIO;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import io.realm.RealmResults;

public class TaskListAdapter extends RecyclerSwipeAdapter<TaskListAdapter.ViewHolder>{
    public static final String START_DATE = "%02d.%02d.%4d %02d:%02d";
    public static final String END_DATE = " - %02d.%02d.%4d %02d:%02d";
    public static final String TIME_SPEND = " %02d:%02d";
    private final static String HH_MM_SS = " %02d:%02d:%02d";
    public static final int BLACK_FONT_COLOUR = 0xCC404040;
    public static final int WHITE_FONT_COLOUR = 0xE5F1F1F1;
    private static final String REQUEST_CODE_EXTRA = "request_code";


    private RealmResults<Task> mTaskRealmResults;
    private int[] mBackgroundColors;
    private Context mContext;
    private TaskAutoFinishManager mTaskAutoFinishManager;
    private RealmIO mRealmIO;

    final static String ITEM_ID_EXTRA = "item_id";
    final static String TITLE_EXTRA = "title";
    final static String COMMENTARY_EXTRA = "commentary";

    private static final int EDIT_TASK_REQUEST = 2;

    private final static int IDLE_TASK = 0;
    private final static int STARTED_TASK = 1;
    private final static int ENDED_TASK = 2;

    public TaskListAdapter(Context context, RealmResults<Task> taskList,
                           int[] backgroundColors,
                           TaskAutoFinishManager taskAutoFinishManager, RealmIO realmIO){
        mTaskRealmResults = taskList;
        mBackgroundColors = backgroundColors;
        mContext = context;
        mTaskAutoFinishManager = taskAutoFinishManager;
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
        holder.title.setText(item.getTitle());
        holder.commentary.setText(item.getCommentary());
        setDateText(holder, date, item);
        currentBackgroundColor = setTaskBackgroundColor(holder, item);
        setItemTextColor(holder, currentBackgroundColor);
        setStartStopTaskButtonImage(holder, item);
        setSwipeMenuButtonsVisibility(holder, item);
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
                date.append(String.format(TIME_SPEND, elapsedHours, elapsedMinutes));
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
            editTask = (ImageButton)v.findViewById(R.id.edit_task_image_button);
            deleteTask = (ImageButton)v.findViewById(R.id.delete_task_image_button);
            cancelTaskExecution = (ImageButton)v.findViewById(R.id.cancel_task_execution_image_button);
            oneStepBackInTaskExecution = (ImageButton)v.findViewById(R.id.one_step_back_image_button);

            startStopTask.setOnClickListener(this);
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
                        mTaskAutoFinishManager.registerTaskForAutoFinish(item);
                    } else {
                        if (item.getTaskEnd() == null) {
                            mRealmIO.getRealm().beginTransaction();
                            item.setTaskEnd(new GregorianCalendar());
                            mRealmIO.getRealm().commitTransaction();
                            long elapsedTimeInMills = item.getTimeSpend();
                            int hours = (int) TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills);
                            int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeInMills)));
                            int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(elapsedTimeInMills) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMills)));
                            Snackbar.make(((Activity)mContext).findViewById(R.id.task_list_activity_container),
                                    mContext.getString(R.string.task_finished_in) + String.format(HH_MM_SS, hours, minutes, seconds), Snackbar.LENGTH_SHORT).show();
                            mTaskAutoFinishManager.unregisterTaskAutoFinish(item);
                        }
                    }
                    notifyDataSetChanged();
                    break;
                case R.id.delete_task_image_button:
                    swipeLayout.close(false);
                    Task taskToDelete = mTaskRealmResults.get(position);
                    mTaskAutoFinishManager.unregisterTaskAutoFinish(item);
                    mRealmIO.getRealm().beginTransaction();
                    mRealmIO.removeTask(taskToDelete);
                    mRealmIO.getRealm().commitTransaction();
                    notifyDataSetChanged();
                    break;
                case R.id.edit_task_image_button:
                    swipeLayout.close(false);
                    Intent intent = new Intent(mContext, EditTaskActivity.class);
                    intent.putExtra(ITEM_ID_EXTRA, position);
                    intent.putExtra(TITLE_EXTRA, mTaskRealmResults.get(position).getTitle());
                    intent.putExtra(COMMENTARY_EXTRA, mTaskRealmResults.get(position).getCommentary());
                    intent.putExtra(REQUEST_CODE_EXTRA, EDIT_TASK_REQUEST);
                    ((Activity)mContext).startActivityForResult(intent, EDIT_TASK_REQUEST);
                    break;
                case R.id.one_step_back_image_button:
                    if(item.getTaskEnd() != null){
                        mRealmIO.getRealm().beginTransaction();
                        item.setTaskEnd(null);
                        item.setTaskRestart(new GregorianCalendar());
                        mRealmIO.getRealm().commitTransaction();
                        mTaskAutoFinishManager.registerTaskForAutoFinish(item);
                    } else {
                        mTaskAutoFinishManager.unregisterTaskAutoFinish(item);
                        mRealmIO.getRealm().beginTransaction();
                        item.setTaskStart(null);
                        item.setTaskRestart(null);
                        mRealmIO.getRealm().commitTransaction();
                    }
                    notifyDataSetChanged();
                    swipeLayout.close(true);
                    break;
                case R.id.cancel_task_execution_image_button:
                    mTaskAutoFinishManager.unregisterTaskAutoFinish(item);
                    mRealmIO.getRealm().beginTransaction();
                    item.setTaskEnd(null);
                    item.setTaskStart(null);
                    item.setTaskRestart(null);
                    mRealmIO.getRealm().commitTransaction();
                    notifyDataSetChanged();
                    swipeLayout.close(true);
                    break;
            }

        }
    }
}
