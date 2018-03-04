package myapps.kz.betgames.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
 * Created by rauan on 28.07.17.
 */

public class FeedbackFragment extends Fragment {

    private EditText editSupport;
    private Button btnSendSupport;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String token;
    private ProgressDialog dialog;
    private String BASE_URL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        ((MainActivity) getActivity()).setToolbar("Служба поддержки", false);

        BASE_URL = getActivity().getResources().getString(R.string.base_url);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();
        token = preferences.getString("user_token", "");

        editSupport = (EditText) view.findViewById(R.id.editSupport);
        btnSendSupport = (Button) view.findViewById(R.id.btnSendSupport);
        btnSendSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editSupport.getText().toString().isEmpty()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Внимание")
                            .setMessage("Комментарий обязателен к заполнению")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                } else {
                    sendFeedback(BASE_URL + "api/game/send_feedback/", editSupport.getText().toString());
                }
            }
        });

        return view;
    }

    private void sendFeedback(String url, String comment) {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Отправка...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        btnSendSupport.setClickable(false);
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("message", comment);
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("auth-token", token)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSendSupport.setClickable(true);
                            dialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Проблема с сетью... Проверьте подключение к сети и повторите попытку.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSendSupport.setClickable(true);
                                dialog.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Неизвестная ошибка.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        throw new IOException("Unexpected code " + response);
                    }
                }
                final String responseData = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");

                    if (code == 0) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSendSupport.setClickable(true);
                                dialog.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Мы получили ваше сообщение.",
                                        Toast.LENGTH_LONG).show();

                                editSupport.setText("");
                                ((MainActivity)getActivity()).hideSoftKeyboard(getActivity());
                            }
                        });
                    } else if (code == 100) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSendSupport.setClickable(true);
                                dialog.dismiss();
                                resetUserDetails();
                                Toast.makeText(getActivity(), "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        });
                    } else {
                        final String message = jsonObject.getString("message");
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSendSupport.setClickable(true);
                                dialog.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Ошибка: " + message,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    final String message = "Неизвестная ошибка";
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSendSupport.setClickable(true);
                            dialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Ошибка: " + message,
                                    Toast.LENGTH_LONG).show();
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
