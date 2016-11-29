package com.what.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
public class Data implements Serializable {

    private String ssn;
    private double loanAmount;
    private int loanDuration;

    public Data() {
    }

    public Data( String ssn, double loanAmount, int loanDuration) {
        this.ssn = ssn;
        this.loanAmount = loanAmount;
        this.loanDuration = loanDuration;
    }

    public String getSsn() {
        return ssn;
    }


    public double getLoanAmount() {
        return loanAmount;
    }

    public int getLoanDuration() {
        return loanDuration;
    }



    public void setSsn( String ssn ) {
        this.ssn = ssn;
    }


    public void setLoanAmount( double loanAmount ) {
        this.loanAmount = loanAmount;
    }

    public void setLoanDuration( int loanDuration ) {
        this.loanDuration = loanDuration;
    }

  
   

}
