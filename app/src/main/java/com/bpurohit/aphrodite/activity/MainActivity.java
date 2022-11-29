package com.bpurohit.aphrodite.activity;

import android.os.Bundle;
import android.view.Menu;

import com.bpurohit.aphrodite.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.bpurohit.aphrodite.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Drawer layout
        DrawerLayout drawer = binding.drawerLayout;

        // Navigation View
        NavigationView navigationView = binding.navView;

        // BottomNavigationView
        BottomNavigationView navBottomView = findViewById(R.id.bottom_nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // add fragments to configurations to navigate
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_profile, R.id.nav_ShopsNearby,
                R.id.nav_help, R.id.nav_info, R.id.nav_logout,
                R.id.nav_requests, R.id.nav_contacts, R.id.nav_chats)
                .setOpenableLayout(drawer)
                .build();

        // Create a controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // finally setup both the navigation’s
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(navBottomView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}