package com.darien.textreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.darien.textreader.R;
import com.darien.textreader.models.TextSaved;

import java.util.ArrayList;

/**
 * Adapter para manejar la lista de numeros guardados
 */

public class SearchTextAdapter extends RecyclerView.Adapter<SearchTextAdapter.MyViewHolder> {
    private ArrayList<TextSaved> texts;
    private Context context;
    private TextsListListener textsListListener;

    public void setTexts(ArrayList<TextSaved> texts) {
        this.texts = texts;
    }

    public SearchTextAdapter(Context context, TextsListListener textsListListener) {
        this.context = context;
        this.textsListListener = textsListListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.search_result_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.text.setText(texts.get(position).getText());
        holder.tvTimeStamp.setText(texts.get(position).getTimestamp());
        holder.btnRead.setOnClickListener((view -> {
            textsListListener.onTextClicked(texts.get(position).getText());
        }));
        holder.btnDelete.setOnClickListener(view -> {
            textsListListener.onDeleteClicked(texts.get(position).getText());
        });
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView text, tvTimeStamp;
        Button btnDelete, btnRead;
        private MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_text_results);
            btnDelete = itemView.findViewById(R.id.delete_text_btn);
            tvTimeStamp = itemView.findViewById(R.id.tv_timestamp);
            btnRead = itemView.findViewById(R.id.btn_read);
        }
    }

    public interface TextsListListener {
        void onTextClicked(String text);
        void onDeleteClicked(String text);
    }
}
