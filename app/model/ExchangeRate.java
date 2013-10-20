package model;

public class ExchangeRate implements Comparable<ExchangeRate>{

    private String currency;
    private String date;
    private double rate;

    public ExchangeRate() {
    }

    public ExchangeRate(String date, String currency, double rate) {
        this.date = date;
        this.currency = currency;
        this.rate = rate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int compareTo(ExchangeRate r) {
        return this.getDate().compareTo(r.getDate());
    }
}
