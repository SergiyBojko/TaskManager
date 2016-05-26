package com.serhiyboiko.taskmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.serhiyboiko.taskmanager.R;
import com.serhiyboiko.taskmanager.models.Task;

import java.util.List;

public class TaskListAdapter extends BaseAdapter {
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
        if (convertView == null){
            //if convertView doesn't exist create viewholder and save view eferences in it, than attach it ti convertview
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.task_list_item, null);
            viewHolder.title = (TextView)convertView.findViewById(R.id.list_item_title);
            viewHolder.commentary = (TextView)convertView.findViewById(R.id.list_item_commentary);
            convertView.setTag(viewHolder);
        } else {
            //if convertView exist retrieve viewholder  from it
            viewHolder = (ViewHolder)convertView.getTag();
        }

        item = (Task)getItem(position);
        viewHolder.title.setText(item.getTitle());
        viewHolder.commentary.setText(item.getCommentary());
        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView commentary;

    }
}
