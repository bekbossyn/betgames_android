package myapps.kz.betgames.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import myapps.kz.betgames.LoginActivity;
import myapps.kz.betgames.MainActivity;
import myapps.kz.betgames.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rauan on 25.07.17.
 */

public class SettingsFragment extends Fragment {

    private String BASE_URL;
    private TextView editProfitSettings, editCoefSettings,
            editProfitIncSettings, editCoefIncSettings;
    private Spinner spinnerRefreshTimes;
    private Button btnSaveSettings;
    private Button btnTestPush;

    private RadioGroup radioGroup;
    private RadioButton radio0, radio1, radio2, radio3, radio4;
    private ArrayList<RadioButton> radios;

    private MediaPlayer mp;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    // Setting values
    private long val_profit_increment, val_profit_initial;
    private double val_coef_increment, val_coef_initial;
    private int sel_time_position;

    private String token;

    private String soundName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ((MainActivity) getActivity()).setToolbar("Настройки", false);

        BASE_URL = getActivity().getResources().getString(R.string.base_url);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();
        token = preferences.getString("user_token", "");
        soundName = preferences.getString("user_push_sound", "");

        getSettingValues();
        initialiseViews(view);

        return view;
    }

    private void getSettingValues() {
        val_profit_increment = preferences.getLong("val_profit_increment", -1);
        val_coef_increment = Double.longBitsToDouble(preferences.getLong("val_coef_increment", Double.doubleToLongBits(-1)));
        val_profit_initial = preferences.getLong("val_profit_initial", -1);
        val_coef_initial = Double.longBitsToDouble(preferences.getLong("val_coef_initial", Double.doubleToLongBits(-1)));
        sel_time_position = preferences.getInt("sel_time_position", -1);
    }

    private void initialiseViews(View view) {
        editProfitSettings = (EditText) view.findViewById(R.id.editProfitSettings);
        editCoefSettings = (EditText) view.findViewById(R.id.editCoefSettings);
        editProfitIncSettings = (EditText) view.findViewById(R.id.editProfitIncSettings);
        editCoefIncSettings = (EditText) view.findViewById(R.id.editCoefIncSettings);

        editProfitSettings.setText(String.valueOf(val_profit_initial));
        editCoefSettings.setText(String.valueOf(val_coef_initial));
        editProfitIncSettings.setText(String.valueOf(val_profit_increment));
        editCoefIncSettings.setText(String.valueOf(val_coef_increment));

        spinnerRefreshTimes = (Spinner) view.findViewById(R.id.spinnerRefreshTimes);
        spinnerRefreshTimes.getBackground().setColorFilter(getResources().getColor(R.color.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
        spinnerRefreshTimes.setSelection(sel_time_position);

        btnSaveSettings = (Button) view.findViewById(R.id.btnSaveSettings);
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = BASE_URL + "api/game/update_sound_name/";
                changeNotifySound(url, soundName, v);
            }
        });
        btnTestPush = (Button) view.findViewById(R.id.btnTestPush);
        btnTestPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = BASE_URL + "api/game/test_push/";
                testPush(url);
            }
        });

        // Radio sounds
        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        radio0 = (RadioButton) view.findViewById(R.id.radio_sound0);
        radio1 = (RadioButton) view.findViewById(R.id.radio_sound1);
        radio2 = (RadioButton) view.findViewById(R.id.radio_sound2);
        radio3 = (RadioButton) view.findViewById(R.id.radio_sound3);
        radio4 = (RadioButton) view.findViewById(R.id.radio_sound4);
        radios = new ArrayList<>();
        radios.add(radio0);
        radios.add(radio1);
        radios.add(radio2);
        radios.add(radio3);
        radios.add(radio4);
        int position = 0;
        try {
            position = Integer.parseInt(soundName.substring(soundName.length() - 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        radios.get(position).setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (radios.get(0).isChecked()) {
                    soundName = "onesignal0";
                    stopPlaying();
                    mp = MediaPlayer.create(getActivity(), R.raw.onesignal0);
                    mp.start();
                } else if (radios.get(1).isChecked()) {
                    soundName = "onesignal1";
                    stopPlaying();
                    mp = MediaPlayer.create(getActivity(), R.raw.onesignal1);
                    mp.start();
                } else if (radios.get(2).isChecked()) {
                    soundName = "onesignal2";
                    stopPlaying();
                    mp = MediaPlayer.create(getActivity(), R.raw.onesignal2);
                    mp.start();
                } else if (radios.get(3).isChecked()) {
                    soundName = "onesignal3";
                    stopPlaying();
                    mp = MediaPlayer.create(getActivity(), R.raw.onesignal3);
                    mp.start();
                } else if (radios.get(4).isChecked()) {
                    soundName = "onesignal4";
                    stopPlaying();
                    mp = MediaPlayer.create(getActivity(), R.raw.onesignal4);
                    mp.start();
                }
            }
        });
    }

    private void changeNotifySound(String url, final String soundName, final View v) {
        btnSaveSettings.setClickable(false);
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("sound_name", soundName);
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("auth-token", token)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSaveSettings.setClickable(true);
                        Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSaveSettings.setClickable(true);
                            Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_LONG).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSaveSettings.setClickable(true);
                                editor.putString("user_push_sound", soundName);
                                editor.apply();

                                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                stopPlaying();

                                long profit;
                                if (editProfitSettings.getText().toString().isEmpty()) {
                                    profit = 0;
                                } else {
                                    profit = Long.parseLong(editProfitSettings.getText().toString());
                                }
                                long profitInc;
                                if (editProfitIncSettings.getText().toString().isEmpty()) {
                                    profitInc = 0;
                                } else {
                                    profitInc = Long.parseLong(editProfitIncSettings.getText().toString());
                                }
                                double coef;
                                if (editCoefSettings.getText().toString().isEmpty()) {
                                    coef = 0;
                                } else {
                                    coef = Double.parseDouble(editCoefSettings.getText().toString());
                                }
                                double coefInc;
                                if (editCoefIncSettings.getText().toString().isEmpty()) {
                                    coefInc = 0;
                                } else {
                                    coefInc = Double.parseDouble(editCoefIncSettings.getText().toString());
                                }

                                if (profit <= 0) {
                                    showDialog("Выгода не может быть пустым и должна быть больше 0!");
                                } else if (coef <= 1) {
                                    showDialog("Коэффициент не может быть пустым и должен быть больше 1!");
                                } else if (profitInc <= 0) {
                                    showDialog("Разница выгоды не может быть пустым и должна быть больше 0!");
                                } else if (coefInc <= 0) {
                                    showDialog("Разница коэффициента не может быть пустым и должна быть больше 0!");
                                } else {
                                    val_profit_initial = profit;
                                    val_profit_increment = profitInc;
                                    val_coef_initial = coef;
                                    val_coef_increment = coefInc;

                                    editor.putLong("val_profit_initial", val_profit_initial);
                                    editor.putLong("val_profit_increment", val_profit_increment);
                                    editor.putLong("val_coef_initial", Double.doubleToRawLongBits(val_coef_initial));
                                    editor.putLong("val_coef_increment", Double.doubleToRawLongBits(val_coef_increment));
                                    editor.putInt("sel_time_position", spinnerRefreshTimes.getSelectedItemPosition());

                                    String jsonBets = preferences.getString("json_bets", "");
                                    if (jsonBets.isEmpty()) {
                                        editor.putLong("val_last_profit", val_profit_initial);
                                        editor.putLong("val_last_coef", Double.doubleToRawLongBits(val_coef_initial));
                                    }
                                    String jsonBets2 = preferences.getString("json_bets2", "");
                                    if (jsonBets2.isEmpty()) {
                                        editor.putLong("val_last_profit2", val_profit_initial);
                                        editor.putLong("val_last_coef2", Double.doubleToRawLongBits(val_coef_initial));
                                    }
                                    editor.apply();
                                    Toast.makeText(getActivity(), "Данные сохранены", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else if (code == 100) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSaveSettings.setClickable(true);
                                resetUserDetails();
                                Toast.makeText(getActivity(), "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        });
                    }else {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSaveSettings.setClickable(true);
                                Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSaveSettings.setClickable(true);
                            Toast.makeText(getActivity(), "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void testPush(String url) {
        btnTestPush.setClickable(false);
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnTestPush.setClickable(true);
                        Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnTestPush.setClickable(true);
                            Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_LONG).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTestPush.setClickable(true);
                                Toast.makeText(getActivity(), "Уведомление отправлено", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (code == 100) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTestPush.setClickable(true);
                                resetUserDetails();
                                Toast.makeText(getActivity(), "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        });
                    }else {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTestPush.setClickable(true);
                                Toast.makeText(getActivity(), "Ошибка сети", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnTestPush.setClickable(true);
                            Toast.makeText(getActivity(), "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void resetUserDetails() {
        editor.putString("user_token","");
        editor.putString("user_phone","");
        editor.putString("user_id","");
        editor.putString("user_tariff","");
        editor.putInt("user_tariff_date", -1);
        editor.apply();
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Внимание")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void stopPlaying() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPlaying();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_silent).setVisible(false);
        menu.findItem(R.id.action_reset).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}
