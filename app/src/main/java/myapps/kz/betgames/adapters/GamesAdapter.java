package myapps.kz.betgames.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import myapps.kz.betgames.LoginActivity;
import myapps.kz.betgames.MainActivity;
import myapps.kz.betgames.R;
import myapps.kz.betgames.fragments.GamesFragment;
import myapps.kz.betgames.models.Game;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rauan on 25.06.17.
 */

public class GamesAdapter extends BaseExpandableListAdapter {

    private Handler handler = new Handler();

    private Context context;
    private ArrayList<Game> games;
    private String token;
    private GamesFragment fragment;
    private String baseUrl;

    public GamesAdapter(Context context, ArrayList<Game> games, String token, GamesFragment fragment, String baseUrl) {
        this.context = context;
        this.games = games;
        this.token = token;
        this.fragment = fragment;
        this.baseUrl = baseUrl;
    }

    @Override
    public int getGroupCount() {
        return games.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Game.DataBean> childList = (ArrayList<Game.DataBean>) games.get(groupPosition).getData();
        return childList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return games.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<Game.DataBean> childList = (ArrayList<Game.DataBean>) games.get(groupPosition).getData();
        return childList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Game game = (Game) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_group, null);
        }

        TextView txtGameName = (TextView) convertView.findViewById(R.id.txtGameName);
        txtGameName.setText(game.getName().trim());

        TextView txtLastGame = (TextView) convertView.findViewById(R.id.txtLastGame);
        txtLastGame.setText(game.getLast_game().substring(game.getLast_game().length() - 4));

        final CheckBox checkBoxGroup = (CheckBox) convertView.findViewById(R.id.checkBoxGroup);
        int counter = 0;
        final ArrayList<Game.DataBean> dataBean = (ArrayList<Game.DataBean>) game.getData();
        for (int i = 0; i < dataBean.size(); i++) {
            if (dataBean.get(i).isUser_push()) {
                counter++;
            }
        }
        if (counter == dataBean.size()) {
            checkBoxGroup.setChecked(true);
        } else {
            checkBoxGroup.setChecked(false);
        }

        checkBoxGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isChecked = ((CheckBox) v).isChecked();
                String url = baseUrl + "api/game/update_push/";
                OkHttpClient client = new OkHttpClient.Builder()
                        .build();
                FormBody.Builder formBuilder = new FormBody.Builder()
                        .add("value", String.valueOf(isChecked));

                for (int i = 0; i < dataBean.size(); i++) {
                    formBuilder.add("param_names", dataBean.get(i).getParam_name());
                }


                RequestBody formBody = formBuilder.build();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("auth-token", token)
                        .post(formBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (context != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkBoxGroup.toggle();
                                    Toast.makeText(context, "Ошибка сети", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(responseData);
                                int code = jsonObject.getInt("code");

                                if (code == 0) {
                                    for (int i = 0; i < dataBean.size(); i++) {
                                        dataBean.get(i).setUser_push(isChecked);
                                    }
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                } else if (code == 100) {
                                    fragment.stopRepeatingTask();
                                    fragment.closeLoading();
                                    fragment.resetUserDetails();
                                    Toast.makeText(context, "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    context.startActivity(intent);
                                    ((Activity) context).finish();
                                } else {
                                    if (context != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                checkBoxGroup.toggle();
                                                Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {
                                if (context != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            checkBoxGroup.toggle();
                                            Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        ImageView imgGroupIndicator = (ImageView) convertView.findViewById(R.id.imgGroupIndicator);
        if (isExpanded) {
            imgGroupIndicator.setImageResource(R.drawable.arrow_down);
            editor.putBoolean(game.getName(), true);
            editor.apply();
        } else {
            imgGroupIndicator.setImageResource(R.drawable.arrow_right);
            editor.putBoolean(game.getName(), false);
            editor.apply();
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.item_child, null);
        final Game.DataBean data = (Game.DataBean) getChild(groupPosition, childPosition);

        RelativeLayout layoutChild = (RelativeLayout) convertView.findViewById(R.id.layoutChild);
        layoutChild.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((MainActivity)context).hideSoftKeyboard((MainActivity)context);
                return true;
            }
        });

        TextView txtDataName = (TextView) convertView.findViewById(R.id.txtDataName);
        txtDataName.setText(data.getName());

        TextView txtDataCurrent = (TextView) convertView.findViewById(R.id.txtDataCurrent);
        txtDataCurrent.setText(String.valueOf(data.getCurrent_data()));
        if ((double)data.getCurrent_data() >= data.getUser_data() * 0.8
                && (double)data.getCurrent_data() < data.getUser_data() * 0.9) {
            txtDataCurrent.setTextColor(context.getResources().getColor(R.color.textColorLightGreen));
        } else if (data.getCurrent_data() >= Math.round(data.getUser_data() * 0.9)) {
            txtDataCurrent.setTextColor(context.getResources().getColor(R.color.textColorGreen));
        } else {
            txtDataCurrent.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
        }


        final CheckBox checkBoxChild = (CheckBox) convertView.findViewById(R.id.checkBoxChild);
        if (data.isUser_push()) {
            checkBoxChild.setChecked(true);
        } else {
            checkBoxChild.setChecked(false);
        }

        checkBoxChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isChecked = ((CheckBox) v).isChecked();
                String url = baseUrl + "api/game/update_push/";
                OkHttpClient client = new OkHttpClient.Builder()
                        .build();
                FormBody.Builder formBuilder = new FormBody.Builder()
                        .add("param_names", data.getParam_name())
                        .add("value", String.valueOf(isChecked));
                RequestBody formBody = formBuilder.build();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("auth-token", token)
                        .post(formBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (context != null) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkBoxChild.toggle();
                                    Toast.makeText(context, "Ошибка сети", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(responseData);
                                int code = jsonObject.getInt("code");

                                if (code == 0) {
                                    data.setUser_push(isChecked);
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                } else if (code == 100) {
                                    fragment.stopRepeatingTask();
                                    fragment.closeLoading();
                                    fragment.resetUserDetails();
                                    Toast.makeText(context, "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    context.startActivity(intent);
                                    ((Activity) context).finish();
                                } else {
                                    if (context != null) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                checkBoxChild.toggle();
                                                Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {
                                if (context != null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            checkBoxChild.toggle();
                                            Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });

        final EditText editDataUser = (EditText) convertView.findViewById(R.id.editDataUser);
        editDataUser.setText(String.valueOf(data.getUser_data()));

        Pair<Integer, Integer> pair = fragment.getLastFocused();
        final int lastFocusedGroup = pair.first;
        final int lastFocusedChild = pair.second;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastFocusedGroup == groupPosition && lastFocusedChild == childPosition) {
                    editDataUser.requestFocus();
                    int pos = editDataUser.getText().length();
                    editDataUser.setSelection(pos);
                }
            }
        }, 50);

        editDataUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    fragment.setLastFocused(groupPosition, childPosition);
                    int pos = ((EditText) v).getText().length();
                    ((EditText) v).setSelection(pos);
                }
            }
        });

        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start;i < end;i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        editDataUser.setFilters(new InputFilter[] { filter });


        editDataUser.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String url = baseUrl + "api/game/update/";

                    InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    fragment.setLastFocused(-1, -1);

                    String editVal = editDataUser.getText().toString();
                    int newUserData;
                    if (editVal.isEmpty()) {
                        newUserData = 0;
                        games.get(groupPosition).getData().get(childPosition).
                                setUser_data(newUserData);
                    } else {
                        newUserData = Integer.parseInt(editVal);
                        games.get(groupPosition).getData().get(childPosition).
                                setUser_data(newUserData);
                    }
                    //notifyDataSetChanged();

                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();
                    FormBody.Builder formBuilder = new FormBody.Builder()
                            .add("param_name", data.getParam_name())
                            .add("param_val", String.valueOf(newUserData));
                    RequestBody formBody = formBuilder.build();
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("auth-token", token)
                            .post(formBody)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (context != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Ошибка сети", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();
                                try {
                                    JSONObject jsonObject = new JSONObject(responseData);
                                    int code = jsonObject.getInt("code");

                                    if (code == 0) {
                                        fragment.setLastFocused(-1, -1);
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyDataSetChanged();
                                            }
                                        });
                                    } else if (code == 100) {
                                        fragment.stopRepeatingTask();
                                        fragment.closeLoading();
                                        fragment.resetUserDetails();
                                        Toast.makeText(context, "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(context, LoginActivity.class);
                                        context.startActivity(intent);
                                        ((Activity) context).finish();
                                    } else {
                                        final String message = jsonObject.getString("message");
                                        final int paramVal = jsonObject.getInt("param_val");
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                                games.get(groupPosition).getData().get(childPosition).
                                                        setUser_data(paramVal);
                                                notifyDataSetChanged();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });

                    return true;
                }
                return false;
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void refill(ArrayList<Game> list) {
        games.clear();
        games.addAll(list);
        notifyDataSetChanged();
    }
}
