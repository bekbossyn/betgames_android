package myapps.kz.betgames;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import myapps.kz.betgames.fragments.AboutFragment;
import myapps.kz.betgames.fragments.CalcEmptyFragment;
import myapps.kz.betgames.fragments.CalculatorsFragment;
import myapps.kz.betgames.fragments.FeedbackFragment;
import myapps.kz.betgames.fragments.GamesFragment;
import myapps.kz.betgames.fragments.SettingsFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String pushToken;
    private String token;
    private String userPhone;
    private boolean userSound;
    private String userTariff;
    private int userTariffDate;
    private String BASE_URL;

    private RelativeLayout contentMain;

    private TextView txtUserPhone;
    private TextView txtTariff, txtTariffDate;
    private Button btnExtendTariff;
    private LinearLayout layoutHeaderTariff;
    private TextView toolbarTitle;
    private LinearLayout layoutCalcButtons;
    private Button btnBet, btnReset;

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    private NavigationView navigationView;

    // Double click back button to exit
    private boolean doubleBackToExitPressedOnce = false;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BASE_URL = getResources().getString(R.string.base_url);

        mHandler = new Handler();
        contentMain = (RelativeLayout) findViewById(R.id.content_main);
        contentMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        layoutCalcButtons = (LinearLayout) findViewById(R.id.layoutCalcButtons);
        btnBet = (Button) findViewById(R.id.btnBet);
        btnReset = (Button) findViewById(R.id.btnReset);

        btnBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CalculatorsFragment) fragment).makeBet();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment != null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Вы уверены что хотите сбросить все?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((CalculatorsFragment) fragment).makeReset();
                                }
                            })
                            .setNegativeButton("Нет", null)
                            .show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        token = preferences.getString("user_token", "");
        userSound = preferences.getBoolean("user_sound", true);
        userPhone = preferences.getString("user_phone", "");

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                pushToken = userId;
                sendOneSignToken(BASE_URL + "api/game/update_token/");
            }
        });

        View navHeader = navigationView.getHeaderView(0);
        txtUserPhone = (TextView) navHeader.findViewById(R.id.txtUserPhone);
        txtUserPhone.setText(userPhone);

        layoutHeaderTariff = (LinearLayout) navHeader.findViewById(R.id.layoutHeaderTariff);
        if (userPhone.equals("+77001112233")) {
            layoutHeaderTariff.setVisibility(View.GONE);
        }
        txtTariff = (TextView) navHeader.findViewById(R.id.txtTariff);
        txtTariffDate = (TextView) navHeader.findViewById(R.id.txtTariffDate);
        btnExtendTariff = (Button) navHeader.findViewById(R.id.btnExtendTariff);
        btnExtendTariff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                        startActivity(intent);
                    }
                }, 350);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        setTariffInfo();

        // initialise settings values
        initialiseSettingValues();

        fragmentManager = getSupportFragmentManager();
        fragment = new GamesFragment();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.containerMain, fragment).commit();
    }

    public void setTariffInfo() {
        userTariff = preferences.getString("user_tariff", "");
        userTariffDate = preferences.getInt("user_tariff_date", -1);
        txtTariff.setText(userTariff);
        if (userTariffDate % 10 == 1) {
            txtTariffDate.setText(userTariffDate + " день");
        } else if (userTariffDate % 10 > 1 && userTariffDate % 10 < 5) {
            txtTariffDate.setText(userTariffDate + " дня");
        } else {
            txtTariffDate.setText(userTariffDate + " дней");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fragment = new GamesFragment();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.containerMain, fragment).commit();
        navigationView.getMenu().getItem(0).setChecked(true);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Чтобы выйти, нажмите НАЗАД еще раз", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (userSound) {
            menu.findItem(R.id.action_silent).setIcon(getResources().getDrawable(R.drawable.ic_silent_sound));
        } else {
            menu.findItem(R.id.action_silent).setIcon(getResources().getDrawable(R.drawable.ic_silent));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (fragment != null) {
                ((GamesFragment) fragment).stopRepeatingTask();
                ((GamesFragment) fragment).startRepeatingTask();
            }
        } else if (id == R.id.action_silent) {
            String url = BASE_URL + "api/game/update_sound/";
            changeSound(item, url);
        } else if (id == R.id.action_reset) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Восстановить значения по умолчанию?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String url = BASE_URL + "api/game/reset/";
                            resetValues(item, url);
                        }
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_games) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragment = new GamesFragment();
                    transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.containerMain, fragment).commit();
                }
            }, 350);
        } else if (id == R.id.nav_calculator) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (userTariffDate == 0) {
                        fragment = new CalcEmptyFragment();
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.containerMain, fragment).commit();
                    } else {
                        fragment = new CalculatorsFragment();
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.containerMain, fragment).commit();
                    }
                }
            }, 350);
        } else if (id == R.id.nav_settings) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragment = new SettingsFragment();
                    transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.containerMain, fragment).commit();
                }
            }, 350);
        } else if (id == R.id.nav_feedback) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragment = new FeedbackFragment();
                    transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.containerMain, fragment).commit();
                }
            }, 350);
        }else if (id == R.id.nav_about) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragment = new AboutFragment();
                    transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.containerMain, fragment).commit();
                }
            }, 350);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void resetValues(final MenuItem item, String url) {
        item.setEnabled(false);
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody.Builder formBuilder = new FormBody.Builder();
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("auth-token", token)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Ошибка сети", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            item.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Ошибка сети", Toast.LENGTH_LONG).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                item.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "Значения восстановлены по умолчанию", Toast.LENGTH_LONG).show();
                                if (fragment != null) {
                                    ((GamesFragment) fragment).stopRepeatingTask();
                                    ((GamesFragment) fragment).startRepeatingTask();
                                }
                            }
                        });
                    } else {
                        item.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Ошибка. Повторите попытку.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    item.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Ошибка сети", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void changeSound(final MenuItem item, String url) {
        if (userSound) {
            item.setIcon(getResources().getDrawable(R.drawable.ic_silent));
        } else {
            item.setIcon(getResources().getDrawable(R.drawable.ic_silent_sound));
        }
        userSound = !userSound;
        item.setEnabled(false);
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("value", String.valueOf(userSound));
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("auth-token", token)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(true);
                        userSound = !userSound;
                        if (userSound) {
                            item.setIcon(getResources().getDrawable(R.drawable.ic_silent_sound));
                        } else {
                            item.setIcon(getResources().getDrawable(R.drawable.ic_silent));
                        }
                        Toast.makeText(getApplicationContext(), "Ошибка сети", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            item.setEnabled(true);
                            userSound = !userSound;
                            if (userSound) {
                                item.setIcon(getResources().getDrawable(R.drawable.ic_silent_sound));
                            } else {
                                item.setIcon(getResources().getDrawable(R.drawable.ic_silent));
                            }
                            Toast.makeText(getApplicationContext(), "Ошибка сети", Toast.LENGTH_LONG).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                item.setEnabled(true);
                                if (userSound) {
                                    Toast.makeText(getApplicationContext(), "Звук уведомления включен", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Звук уведомления выключен", Toast.LENGTH_LONG).show();
                                }
                                editor.putBoolean("user_sound", userSound);
                                editor.apply();
                                if (fragment != null) {
                                    ((GamesFragment) fragment).stopRepeatingTask();
                                    ((GamesFragment) fragment).startRepeatingTask();
                                }
                            }
                        });
                    } else {
                        item.setEnabled(true);
                        userSound = !userSound;
                        if (userSound) {
                            item.setIcon(getResources().getDrawable(R.drawable.ic_silent_sound));
                        } else {
                            item.setIcon(getResources().getDrawable(R.drawable.ic_silent));
                        }
                        Toast.makeText(getApplicationContext(), "Ошибка. Повторите попытку.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    item.setEnabled(true);
                    userSound = !userSound;
                    if (userSound) {
                        item.setIcon(getResources().getDrawable(R.drawable.ic_silent_sound));
                    } else {
                        item.setIcon(getResources().getDrawable(R.drawable.ic_silent));
                    }
                    Toast.makeText(getApplicationContext(), "Ошибка сети", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendOneSignToken(String url) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("push_token", pushToken);

        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("auth-token", token)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    public void setToolbar(String title, boolean showButtons) {
        toolbarTitle.setText(title);
        if (showButtons) {
            layoutCalcButtons.setVisibility(View.VISIBLE);
        } else {
            layoutCalcButtons.setVisibility(View.GONE);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void initialiseSettingValues() {
        // Calculator 1 values
        long val_last_profit = preferences.getLong("val_last_profit", -1);
        double val_last_coef = Double.longBitsToDouble(preferences.getLong("val_last_coef", Double.doubleToLongBits(-1)));
        long val_total_spent = preferences.getLong("val_total_spent", -1);

        // Calculator 2 values
        long val_last_profit2 = preferences.getLong("val_last_profit2", -1);
        double val_last_coef2 = Double.longBitsToDouble(preferences.getLong("val_last_coef2", Double.doubleToLongBits(-1)));
        long val_total_spent2 = preferences.getLong("val_total_spent2", -1);

        long val_profit_initial = preferences.getLong("val_profit_initial", -1);
        double val_coef_initial = Double.longBitsToDouble(preferences.getLong("val_coef_initial", Double.doubleToLongBits(-1)));
        long val_profit_increment = preferences.getLong("val_profit_increment", -1);
        double val_coef_increment = Double.longBitsToDouble(preferences.getLong("val_coef_increment", Double.doubleToLongBits(-1)));
        int sel_time_position = preferences.getInt("sel_time_position", -1);

        if (sel_time_position == -1) {
            int val = 0;
            editor.putInt("sel_time_position", val);
            editor.apply();
        }

        if (val_profit_increment == -1) {
            long val = 500;
            editor.putLong("val_profit_increment", val);
            editor.apply();
        }
        if (val_coef_increment == -1) {
            double val = 0.5;
            editor.putLong("val_coef_increment", Double.doubleToRawLongBits(val));
            editor.apply();
        }
        if (val_profit_initial == -1) {
            long val = 5000;
            editor.putLong("val_profit_initial", val);
            editor.apply();
        }
        if (val_coef_initial == -1) {
            double val = 2.5;
            editor.putLong("val_coef_initial", Double.doubleToRawLongBits(val));
            editor.apply();
        }

        if (val_last_profit == -1) {
            long val = 5000;
            editor.putLong("val_last_profit", val);
            editor.apply();
        }
        if (val_last_coef == -1) {
            double val = 2.5;
            editor.putLong("val_last_coef", Double.doubleToRawLongBits(val));
            editor.apply();
        }
        if (val_total_spent == -1) {
            long val = 0;
            editor.putLong("val_total_spent", val);
            editor.apply();
        }
        if (val_last_profit2 == -1) {
            long val = 5000;
            editor.putLong("val_last_profit2", val);
            editor.apply();
        }
        if (val_last_coef2 == -1) {
            double val = 2.5;
            editor.putLong("val_last_coef2", Double.doubleToRawLongBits(val));
            editor.apply();
        }
        if (val_total_spent2 == -1) {
            long val = 0;
            editor.putLong("val_total_spent2", val);
            editor.apply();
        }


    }
}
