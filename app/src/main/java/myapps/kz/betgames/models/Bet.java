package myapps.kz.betgames.models;

/**
 * Created by rauan on 23.07.17.
 */

public class Bet {

    private int number;
    private long amount;
    private double coef;
    private long profit;

    public Bet(int number, long profit, double coef, long amount) {
        this.number = number;
        this.amount = amount;
        this.coef = coef;
        this.profit = profit;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getCoef() {
        return coef;
    }

    public void setCoef(double coef) {
        this.coef = coef;
    }

    public long getProfit() {
        return profit;
    }

    public void setProfit(long profit) {
        this.profit = profit;
    }
}
