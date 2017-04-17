package com.zx.zxtvsettings.display;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zx.zxtvsettings.R;


//import com.android.ZXSettings.system.DisplayAreaActivity;

public class DisplayAreaFragment extends Fragment 
{
    private Button button;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_displayarea, container, false);
        button = (Button)view.findViewById(R.id.display_area_set_btn);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), DisplayAreaActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}

