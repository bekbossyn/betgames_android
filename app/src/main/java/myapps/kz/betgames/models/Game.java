package myapps.kz.betgames.models;

import java.util.List;

/**
 * Created by rauan on 24.06.17.
 */

public class Game {

    private String name;
    private String last_game;
    private List<DataBean> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLast_game() {
        return last_game;
    }

    public void setLast_game(String last_game) {
        this.last_game = last_game;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {

        private String param_name;
        private String name;
        private int current_data;
        private int user_data;
        private boolean user_push;

        public String getParam_name() {
            return param_name;
        }

        public void setParam_name(String param_name) {
            this.param_name = param_name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCurrent_data() {
            return current_data;
        }

        public void setCurrent_data(int current_data) {
            this.current_data = current_data;
        }

        public int getUser_data() {
            return user_data;
        }

        public void setUser_data(int user_data) {
            this.user_data = user_data;
        }

        public boolean isUser_push() {
            return user_push;
        }

        public void setUser_push(boolean user_push) {
            this.user_push = user_push;
        }
    }
}
