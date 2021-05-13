package com.ritwikscompany.treasurehunt.utils;

public class Race {
    public String title;
    public String startTime;
    public int creatorId;
    public String creatorName;
    public String groupName;
    public int raceID;

    public Race(String title, String startTime, int creatorId, String creatorName, String groupName, int raceID) {
        this.title = title;
        this.startTime = startTime;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.groupName = groupName;
        this.raceID = raceID;
    }
}