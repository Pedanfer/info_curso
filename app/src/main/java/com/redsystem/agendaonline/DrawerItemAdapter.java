package com.redsystem.agendaonline;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.redsystem.agendaonline.Objetos.DrawerItemViewModel;

public class DrawerItemAdapter extends ArrayAdapter<DrawerItemViewModel> {

    Context mContext;
    int layoutResourceId;
    DrawerItemViewModel data[] = null;

    public DrawerItemAdapter(Context mContext, int layoutResourceId, DrawerItemViewModel[] data) {

        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View listItem, ViewGroup parent) {

        DrawerItemViewModel folder = data[position];

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
        imageViewIcon.getLayoutParams().width = 200;
        imageViewIcon.getLayoutParams().height = 200;

        imageViewIcon.setImageResource(folder.icon);

        TextView textViewName = (TextView) listItem.findViewById(R.id.textViewName);

        textViewName.setText(folder.name);

        return listItem;
    }
}