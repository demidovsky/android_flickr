package com.example.student.flickr;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by student on 2016.
 */
public class PhotoAdapter extends CursorAdapter {
    public PhotoAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        Holder holder = new Holder();
        holder.picture = (ImageView) view.findViewById(R.id.image);
        populateView(holder, cursor, context);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();
        populateView(holder, cursor, context);
    }

    private void populateView(Holder holder, Cursor cursor, Context context) {
        Picasso
                .with(context)
                .load(cursor.getString(cursor.getColumnIndex(PhotosTable.COLUMN_URL)))
                .fit()
                .centerCrop()
                .into(holder.picture);
    }
}
