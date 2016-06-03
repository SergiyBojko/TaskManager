package com.serhiyboiko.taskmanager.adapter;

import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.model.Task;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskListAdapter extends BaseAdapter {
    public static final String START_DATE = "%02d.%02d.%4d %02d:%02d";
    public static final String END_DATE = " - %02d.%02d.%4d %02d:%02d";
    public static final String TIME_SPEND = " %02d:%02d";
    private final static int RED_CARD_COLOR = 0xffffdddd;
    private final static int YELLOW_CARD_COLOR = 0xffffffcc;
    private final static int GREEN_CARD_COLOR = 0xffddffdd;
    private List<Task> mTaskListArray;
    private LayoutInflater mLayoutInflater;

    public TaskListAdapter(List<Task> taskList, LayoutInflater layoutInflater){
        mTaskListArray = taskList;
        mLayoutInflater = layoutInflater;
    }
    @Override
    public int getCount() {
        return mTaskListArray.size();
    }

    @Override
    public Object getItem(int position) {
        return mTaskListArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        Task item = null;
        StringBuilder date = new StringBuilder();
        if (convertView == null){
            //if convertView doesn't exist create viewholder and save view eferences in it, than attach it ti convertview
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.task_list_item, null);
            viewHolder.title = (TextView)convertView.findViewById(R.id.list_item_title);
            viewHolder.commentary = (TextView)convertView.findViewById(R.id.list_item_commentary);
            viewHolder.time = (TextView)convertView.findViewById(R.id.list_item_time);
            viewHolder.itemCard = (CardView)convertView.findViewById(R.id.item_card);
            convertView.setTag(viewHolder);
        } else {
            //if convertView exist retrieve viewholder  from it
            viewHolder = (ViewHolder)convertView.getTag();
        }

        item = (Task)getItem(position);
        viewHolder.title.setText(item.getTitle());
        viewHolder.commentary.setText(item.getCommentary());
        if(item.getTaskStart() == null){
            viewHolder.time.setVisibility(View.INVISIBLE);
            viewHolder.itemCard.setCardBackgroundColor(RED_CARD_COLOR);
        } else {
            viewHolder.time.setVisibility(View.VISIBLE);
            date.append(String.format(START_DATE, item.getTaskStart().get(GregorianCalendar.DAY_OF_MONTH),
                    item.getTaskStart().get(GregorianCalendar.MONTH), item.getTaskStart().get(GregorianCalendar.YEAR),
                    item.getTaskStart().get(GregorianCalendar.HOUR_OF_DAY), item.getTaskStart().get(GregorianCalendar.MINUTE)));
            viewHolder.itemCard.setCardBackgroundColor(YELLOW_CARD_COLOR);
            if (item.getTaskEnd() != null){
                date.append(String.format(END_DATE, item.getTaskEnd().get(GregorianCalendar.DAY_OF_MONTH),
                        item.getTaskEnd().get(GregorianCalendar.MONTH), item.getTaskEnd().get(GregorianCalendar.YEAR),
                        item.getTaskEnd().get(GregorianCalendar.HOUR_OF_DAY), item.getTaskEnd().get(GregorianCalendar.MINUTE)));
                long elapsedMills = item.getTimeSpend();
                int elapsedHours = (int)TimeUnit.MILLISECONDS.toHours(elapsedMills);
                int elapsedMinutes = (int)(TimeUnit.MILLISECONDS.toMinutes(elapsedMills) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMills)));
                date.append(String.format(TIME_SPEND, elapsedHours, elapsedMinutes));
                viewHolder.itemCard.setCardBackgroundColor(GREEN_CARD_COLOR);
            }
            viewHolder.time.setText(date.toString());
        }

        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView commentary;
        TextView time;
        CardView itemCard;

    }
}
