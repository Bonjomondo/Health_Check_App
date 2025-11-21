package com.example.health_check_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.health_check_app.models.AlertRecord;
import java.util.List;

public class AlertLogAdapter extends RecyclerView.Adapter<AlertLogAdapter.ViewHolder> {
    
    private List<AlertRecord> alertRecords;
    
    public AlertLogAdapter(List<AlertRecord> alertRecords) {
        this.alertRecords = alertRecords;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_alert_log, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertRecord record = alertRecords.get(position);
        holder.timeText.setText("[" + record.getTime() + "]");
        holder.typeText.setText(record.getType());
        holder.messageText.setText(record.getMessage());
    }
    
    @Override
    public int getItemCount() {
        return alertRecords.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        TextView typeText;
        TextView messageText;
        
        ViewHolder(View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.alertTime);
            typeText = itemView.findViewById(R.id.alertType);
            messageText = itemView.findViewById(R.id.alertMessage);
        }
    }
}
