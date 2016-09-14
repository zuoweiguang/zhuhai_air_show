package com.navinfo.mapspotter.foundation.io;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navinfo.mapspotter.foundation.io.util.MongoDateFormatter;

import java.util.Date;

/**
 * Created by SongHuiXing on 2016/1/5.
 */
@JsonIgnoreProperties("_id")
public class TestPOJO {
    public String Name;

    public boolean Gender;

    public short Age;

    private Work work = null;

    @JsonProperty("Job")
    public Work getWork(){
        return work;
    }
    public void setWork(Work work){
        this.work = work;
    }

    public static class Work{
        public String Address;

        public float Salary;

        @JsonProperty("From")
        @JsonSerialize(using = MongoDateFormatter.MongoDateSerializer.class)
        @JsonDeserialize(using = MongoDateFormatter.MongoDateDeserializer.class)
        public Date StartDay;
    }
}
