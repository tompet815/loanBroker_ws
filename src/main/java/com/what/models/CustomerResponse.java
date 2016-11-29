package com.what.models;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CustomerResponse")
public class CustomerResponse implements Serializable {

    private String ssn;
    private double interestRate;
    private String bankName;

    public CustomerResponse() {
    }

    public CustomerResponse(String ssn, double interestRate, String bankName) {
        this.ssn = ssn;
        this.interestRate = interestRate;
        this.bankName = bankName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

   

}
