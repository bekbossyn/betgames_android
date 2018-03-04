package myapps.kz.betgames;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnReceiveCode;
    private Spinner spinnerCountries;
    private TextView txtCountryCode;
    private RelativeLayout loadingPaneLogin;
    private EditText editCode, editPhone;

    private List<String> codesList;
    private String baseUrl;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        codesList = Arrays.asList(getResources().getStringArray(R.array.codes));
        baseUrl = getResources().getString(R.string.base_url);

        // Loading Pane Login
        loadingPaneLogin = (RelativeLayout) findViewById(R.id.loadingPanelLogin);

        // EditText Phone, Code
        editCode = (EditText) findViewById(R.id.editCode);
        editPhone = (EditText) findViewById(R.id.editPhone);

        // Receive Code Button
        btnReceiveCode = (Button) findViewById(R.id.btnReceiveCode);
        btnReceiveCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send
                String url = baseUrl + "api/phone_login/";
                String phone = txtCountryCode.getText().toString() + editPhone.getText().toString();
                makeLoginRequest(url, phone);
            }
        });

        // Login Button
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = baseUrl + "api/phone_login_complete/";
                String phone = txtCountryCode.getText().toString() + editPhone.getText().toString();
                String code = editCode.getText().toString();
                makeLoginCompleteRequest(url, phone, code);
            }
        });

        // Country Code TextView
        txtCountryCode = (TextView) findViewById(R.id.txtCountryCode);

        // Countries Spinner
        spinnerCountries = (Spinner) findViewById(R.id.spinnerCountries);
        spinnerCountries.getBackground().setColorFilter(getResources().getColor(R.color.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
        spinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtCountryCode.setText(codesList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String token = preferences.getString("user_token", "");

        if(!token.equalsIgnoreCase(""))
        {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }

    }

    private void makeLoginRequest(String url, String phone) {

        btnReceiveCode.setClickable(false);
        loadingPaneLogin.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("phone", phone);
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingPaneLogin.setVisibility(View.GONE);
                        btnReceiveCode.setClickable(true);
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Внимание")
                                .setMessage("Проблема с сетью... Проверьте подключение к сети и повторите попытку.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingPaneLogin.setVisibility(View.GONE);
                            btnReceiveCode.setClickable(true);
                            String message = "Неизвестная ошибка. Повторите попытку.";
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Внимание")
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }

                final String responseData = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = new JSONObject(responseData);

                            int code = jsonObject.getInt("code");
                            if (code == 0) {

                                btnReceiveCode.setVisibility(View.GONE);
                                btnLogin.setVisibility(View.VISIBLE);
                                editCode.setVisibility(View.VISIBLE);
                                editCode.requestFocus();

                            } else {
                                btnReceiveCode.setClickable(true);
                                String message = jsonObject.getString("message");
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("Внимание")
                                        .setMessage(message)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                            }
                            loadingPaneLogin.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            String message = "Ошибка конвертирования JSON объектов. Повторите попытку.";
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Внимание")
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                });

            }
        });

    }

    private void makeLoginCompleteRequest(String url, String phone, String code) {
        btnLogin.setClickable(false);
        loadingPaneLogin.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("phone", phone)
                .add("code", code);
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingPaneLogin.setVisibility(View.GONE);
                        btnLogin.setClickable(true);
                        String message = "Проблема с сетью... Проверьте подключение к сети и повторите попытку.";
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Внимание")
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingPaneLogin.setVisibility(View.GONE);
                            btnLogin.setClickable(true);
                            String message = "Неизвестная ошибка. Повторите попытку.";
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Внимание")
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }

                final String responseData = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = new JSONObject(responseData);

                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                String token = jsonObject.getString("token");
                                JSONObject userObject = jsonObject.getJSONObject("user");
                                String userphone = userObject.getString("phone");
                                String userid = userObject.getString("user_id");
                                String usertariff = userObject.getString("tariff");
                                int usertariffdate = userObject.getInt("tariff_date");
                                boolean userSound = userObject.getBoolean("sound");
                                String userPushSound = userObject.getString("sound_name");
                                editor.putString("user_token",token);
                                editor.putString("user_phone",userphone);
                                editor.putString("user_id",userid);
                                editor.putBoolean("user_sound", userSound);
                                editor.putString("user_tariff", usertariff);
                                editor.putInt("user_tariff_date", usertariffdate);
                                editor.putString("user_push_sound", userPushSound);
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                btnLogin.setClickable(true);
                                String message = jsonObject.getString("message");
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("Внимание")
                                        .setMessage(message)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                            }
                            loadingPaneLogin.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            String message = "Ошибка конвертирования JSON объектов. Повторите попытку.";
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Внимание")
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                });

            }
        });
    }
}
