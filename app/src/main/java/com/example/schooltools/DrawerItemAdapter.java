package com.example.schooltools;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.schooltools.Objetos.DrawerItemViewModel;
import com.example.schooltools.Perfil.Perfil_Usuario;
import com.google.firebase.auth.FirebaseUser;

public class DrawerItemAdapter extends ArrayAdapter<DrawerItemViewModel> {
    FirebaseUser user;

    Context mContext;
    int layoutResourceId;
    DrawerItemViewModel data[] = null;

    public DrawerItemAdapter(Context mContext, int layoutResourceId, DrawerItemViewModel[] data, FirebaseUser user) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
        this.user = user;
    }

    @Override
    public View getView(int position, View listItem, ViewGroup parent) {
        DrawerItemViewModel folder = data[position];

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = listItem.findViewById(R.id.imageViewIcon);
        CardView cardViewImage = listItem.findViewById(R.id.profile_pic_card);
        ImageView imageViewPic = listItem.findViewById(R.id.profile_pic_view);
        TextView textViewName = listItem.findViewById(R.id.textViewName);
        TextView textViewMail = listItem.findViewById(R.id.mail_drawer);

        if (position > 0 && position < 5) {
            imageViewIcon.getLayoutParams().width = 200;
            imageViewIcon.getLayoutParams().height = 230;
            imageViewIcon.setImageResource(folder.icon);
            textViewName.setText(folder.name);
            cardViewImage.setVisibility(View.GONE);
            listItem.findViewById(R.id.nombre_drawer).setVisibility(View.GONE);
            textViewMail.setVisibility(View.GONE);
            imageViewIcon.setPadding(16, 16, 24, 16);
        } else if (position == 5) {
            imageViewIcon.getLayoutParams().width = 600;
            imageViewIcon.getLayoutParams().height = 280;
            textViewName.setVisibility(View.GONE);
            cardViewImage.setVisibility(View.GONE);
            listItem.findViewById(R.id.nombre_drawer).setVisibility(View.GONE);
            textViewMail.setVisibility(View.GONE);
        } else {
            imageViewIcon.getLayoutParams().width = 600;
            imageViewIcon.getLayoutParams().height = 800;
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.profile_gradient);
            imageViewIcon.setBackground(drawable);
            textViewName.setVisibility(View.GONE);
            Perfil_Usuario.getUserImageInto(imageViewPic, mContext);
        }

        return listItem;
    }
}