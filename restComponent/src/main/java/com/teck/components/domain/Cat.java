package com.teck.components.domain;

import java.time.LocalTime;
import javax.xml.bind.annotation.*;

/*
 * a simple domain entity doubling as a DTO
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Cat {


    private String name;

    private int numPaws;

    private LocalTime lastUpdated;

    public Cat() {
    }

    public Cat(String name, int numPaws) {
        this.name = name;
        this.numPaws = numPaws;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumPaws() {
        return numPaws;
    }

    public void setNumPaws(int numPaws) {
        this.numPaws = numPaws;
    }
    
    public LocalTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Cat {" +
                "name=" + name +
                ", lastUpdated=" + lastUpdated +
                ", numPaws=" + numPaws +
                '}';
    }
}
