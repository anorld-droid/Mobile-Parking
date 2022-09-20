package com.karanja.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.karanja.R;
import com.karanja.adapter.SectionsAdapter;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
//import com.google.android.gms.location.places.ui.PlaceSelectionListener;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;

import com.google.android.material.tabs.TabLayout;

public class MapsActivity extends AppCompatActivity {


    private SectionsAdapter mSectionsAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewpager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map_list);

        getSupportActionBar().setTitle("Available Spaces");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsAdapter = new SectionsAdapter(getSupportFragmentManager());
        mViewpager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.map_list);
        mViewpager.setAdapter(mSectionsAdapter);
        mTabLayout.setupWithViewPager(mViewpager);
        mViewpager.setCurrentItem(0);
    }
}


