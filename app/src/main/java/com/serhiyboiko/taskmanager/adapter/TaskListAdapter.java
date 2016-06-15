package com.serhiyboiko.taskmanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskListAdapter extends BaseAdapter {
    public static final String START_DATE = "%02d.%02d.%4d %02d:%02d";
    public static final String END_DATE = " - %02d.%02d.%4d %02d:%02d";
    public static final String TIME_SPEND = " %02d:%02d";
    public static final int BLACK_FONT_COLOUR = 0xCC404040;
    public static final int WHITE_FONT_COLOUR = 0xE5F1F1F1;
    private List<Task> mTaskListArray;
    private Context mContext;
    private int[] mBackgroundColors;

    private final static int IDLE_TASK = 0;
    private final static int STARTED_TASK = 1;
    private final static int ENDED_TASK = 2;

    public TaskListAdapter(List<Task> taskList, int[] backgroundColors, Context context){
        mTaskListArray = taskList;
        mContext = context;
        mBackgroundColors = backgroundColors;
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
        int currentBackgroundColor;
        StringBuilder date = new StringBuilder();
        if (convertView == null){
            //if convertView doesn't exist create viewholder and save view eferences in it, than attach it ti convertview
            viewHolder = new ViewHolder();
            convertView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.task_list_item, null);
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
            currentBackgroundColor = mBackgroundColors[IDLE_TASK];
            viewHolder.itemCard.setCardBackgroundColor(currentBackgroundColor);
        } else {
            viewHolder.time.setVisibility(View.VISIBLE);
            date.append(String.format(START_DATE, item.getTaskStart().get(GregorianCalendar.DAY_OF_MONTH),
                    item.getTaskStart().get(GregorianCalendar.MONTH), item.getTaskStart().get(GregorianCalendar.YEAR),
                    item.getTaskStart().get(GregorianCalendar.HOUR_OF_DAY), item.getTaskStart().get(GregorianCalendar.MINUTE)));
            currentBackgroundColor = mBackgroundColors[STARTED_TASK];
            viewHolder.itemCard.setCardBackgroundColor(currentBackgroundColor);
            if (item.getTaskEnd() != null){
                date.append(String.format(END_DATE, item.getTaskEnd().get(GregorianCalendar.DAY_OF_MONTH),
                        item.getTaskEnd().get(GregorianCalendar.MONTH), item.getTaskEnd().get(GregorianCalendar.YEAR),
                        item.getTaskEnd().get(GregorianCalendar.HOUR_OF_DAY), item.getTaskEnd().get(GregorianCalendar.MINUTE)));
                long elapsedMills = item.getTimeSpend();
                int elapsedHours = (int)TimeUnit.MILLISECONDS.toHours(elapsedMills);
                int elapsedMinutes = (int)(TimeUnit.MILLISECONDS.toMinutes(elapsedMills) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMills)));
                date.append(String.format(TIME_SPEND, elapsedHours, elapsedMinutes));
                currentBackgroundColor = mBackgroundColors[ENDED_TASK];
                viewHolder.itemCard.setCardBackgroundColor(currentBackgroundColor);
            }
            viewHolder.time.setText(date.toString());
        }

        if(isBrightColor(currentBackgroundColor)){
            viewHolder.time.setTextColor(BLACK_FONT_COLOUR);
            viewHolder.title.setTextColor(BLACK_FONT_COLOUR);
            viewHolder.commentary.setTextColor(BLACK_FONT_COLOUR);
        } else {
            viewHolder.time.setTextColor(WHITE_FONT_COLOUR);
            viewHolder.title.setTextColor(WHITE_FONT_COLOUR);
            viewHolder.commentary.setTextColor(WHITE_FONT_COLOUR);
        }

        return convertView;
    }

    public void setBackgroundColors(int[] backgroundColors) {
        mBackgroundColors = backgroundColors;
    }

    public static boolean isBrightColor(int color) {
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

    class ViewHolder {
        TextView title;
        TextView commentary;
        TextView time;
        CardView itemCard;

    }


}
