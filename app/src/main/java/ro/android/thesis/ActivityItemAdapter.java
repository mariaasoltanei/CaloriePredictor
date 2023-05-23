package ro.android.thesis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ro.android.thesis.domain.ActivityData;

public class ActivityItemAdapter extends RecyclerView.Adapter<ActivityItemAdapter.ViewHolder>{
    private List<ActivityData> activityDataList;

    public ActivityItemAdapter(List<ActivityData> activityDataList) {
        this.activityDataList = activityDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item, parent, false);
        return new ViewHolder(view);
    }
    public void updateData(List<ActivityData> newData) {
        activityDataList = newData;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //String item = activityDataList.get(position);
        final int index = holder.getAdapterPosition();
        ActivityData item = activityDataList.get(position);
        holder.tvActivityType.setText(activityDataList.get(position).getActivityType());
        holder.tvNoCalories.setText(String.valueOf(activityDataList.get(position).getNoCalories()) + " kcal");
        if(item.getActivityType() == "WALKING"){
            holder.imgActivityType.setImageResource(R.drawable.walking_icon);
        }
        else if (item.getActivityType() == "WALKING_DOWNSTAIRS"){
            holder.imgActivityType.setImageResource(R.drawable.walking_downstairs);
        }
        else if (item.getActivityType() == "WALKING_UPSTAIRS"){
            holder.imgActivityType.setImageResource(R.drawable.walking_upstairs_icon);
        }
        else if (item.getActivityType() == "STANDING"){
            holder.imgActivityType.setImageResource(R.drawable.standing_icon);
        }
        else if (item.getActivityType() == "SITTING"){
            holder.imgActivityType.setImageResource(R.drawable.sitting_icon);
        }
        else {
            holder.imgActivityType.setImageResource(R.drawable.laying_icon);
        }

    }

    @Override
    public int getItemCount() {
        return activityDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgActivityType;
        TextView tvActivityType;
        TextView tvNoCalories;


        public ViewHolder(View itemView) {
            super(itemView);
            tvActivityType = itemView.findViewById(R.id.tvActivityType);
            tvNoCalories = itemView.findViewById(R.id.tvNoCalories);
            imgActivityType = itemView.findViewById(R.id.imgActivityType);
        }
    }
}
