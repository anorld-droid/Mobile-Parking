package com.karanja.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.karanja.R;
import com.karanja.Register.LoginActivity;
import com.karanja.utils.SharePreference;
import com.karanja.views.homefragments.DefaultFragment;
import com.karanja.views.homefragments.MyVehicleFragment;
import com.karanja.views.homefragments.PaymentMethodsFragment;

import java.util.Objects;

public class HomeActivity extends BaseActivity {

    //widgets
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private boolean mToolBarNavigationListenerIsRegistered = false;
    private TextView nav_fullnames, nav_phonenumber, nav_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        loadViews();
        initViews();
        setUpDefaultFragment();
        navigationClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String name = getIntent().getStringExtra("name");
        if (name != null) {
            Fragment frag = new MyVehicleFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.home_frame, frag).commit();
            enableBackViews(true);
            Objects.requireNonNull(getSupportActionBar()).setTitle("My Vehicle");
        }
    }


    private void loadViews() {
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        View headerView = navigationView.getHeaderView(0);
        nav_fullnames = headerView.findViewById(R.id.nav_user_fullnames);
        nav_phonenumber = headerView.findViewById(R.id.nav_user_phone_number);
        nav_user = headerView.findViewById(R.id.user);
    }

    private void initViews() {
        nav_fullnames.setText(SharePreference.getINSTANCE(getApplicationContext()).getUser());
        nav_phonenumber.setText(SharePreference.getINSTANCE(getApplicationContext()).getPhoneNumber());
        nav_user.setText(SharePreference.getINSTANCE(getApplicationContext()).getUserType());
    }

    private void setUpDefaultFragment() {
        DefaultFragment defaultFragment = new DefaultFragment();
        setUpFragment(defaultFragment);
        toolbar.setTitle(null);
    }


    @SuppressLint("NonConstantResourceId")
    private void navigationClickListeners() {
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = "";
            switch (item.getItemId()) {

                case R.id.nav_notification:
                    title = "Notificatins";
                    fragment = new NotificationFragment();
                    break;


                case R.id.nav_pay:
                    title = "Payment Methods";
                    fragment = new PaymentMethodsFragment();
                    break;


                case R.id.nav_car:
                    title = "My Vehicle";
                    fragment = new MyVehicleFragment();
                    break;


                case R.id.nav_sign_out:
                    title = "Logout";
                    signout();
                    break;
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            navigationView.setCheckedItem(item);
            if (fragment != null) {
                setUpFragment(fragment);
                enableBackViews(true);
                toolbar.setTitle(title);

            }
            return true;
        });

    }

    public void setUpFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, fragment).commit();

    }


    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.home_frame);
        if (f instanceof DefaultFragment) {
            finish();
        } else {
            setUpDefaultFragment();
            enableBackViews(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment frag = new MyVehicleFragment();
        Fragment frag2 = new DefaultFragment();
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(frag2).attach(frag).commit();
        }
    }

    private void signout() {
        SharePreference.getINSTANCE(getApplicationContext()).setIsUserLoggedIn(false);
        SharePreference.getINSTANCE(getApplicationContext()).setAccesstoken("null");
        Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logout);
        finish();
    }

    private void enableBackViews(boolean enable) {
        if (enable) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);

            @SuppressLint("UseCompatLoadingForDrawables") final Drawable upArrow = getResources().getDrawable(R.drawable.ic_action_back);
            upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(upArrow);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setTitleTextColor(getResources().getColor(R.color.color_white));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            if (!mToolBarNavigationListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(v -> onBackPressed());

                mToolBarNavigationListenerIsRegistered = true;
            }

        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;

            toolbar.setBackgroundColor(getResources().getColor(R.color.color_white));
        }
    }

}
