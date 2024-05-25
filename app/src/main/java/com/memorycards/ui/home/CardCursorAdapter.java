package com.memorycards.ui.home;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.memorycards.R;

public class CardCursorAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public CardCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_card, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final TextView textView = view.findViewById(R.id.text_card_front);
        String frontText = cursor.getString(cursor.getColumnIndexOrThrow("front"));
        String backText = cursor.getString(cursor.getColumnIndexOrThrow("back"));


        textView.setText(frontText);


        view.setOnClickListener(new View.OnClickListener() {
            boolean isFront = true;

            @Override
            public void onClick(View v) {
                if (isFront) {
                    textView.setText(backText);
                    textView.setBackgroundColor(Color.GREEN);
                } else {

                    textView.setText(frontText);
                    textView.setBackgroundColor(Color.TRANSPARENT);
                }
                isFront = !isFront;
            }
        });
    }

}
