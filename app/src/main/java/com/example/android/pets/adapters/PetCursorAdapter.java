package com.example.android.pets.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.pets.R;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by Bassam on 7/19/2017.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_pets_list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        modifyName(view, cursor);
        modifySummary(view, cursor);
    }

    private void modifySummary(View view, Cursor cursor) {
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary_text_view);
        String summary = cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));
        if (summary.isEmpty()) {
            summary = "Unknown breed";
        }
        summaryTextView.setText(summary);
    }

    private void modifyName(View view, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        nameTextView.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
    }
}
