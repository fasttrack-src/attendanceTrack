import { InjectionToken } from '@angular/core';

//Used to hold config values for application
export interface ApplicationConfig{
    allowedUrls : string,
    submitAttendanceUrl : string,
    studentsInGroupUrl: string,
    deleteAttendanceUrl: string,
    groupsPerTutorUrl: string,
    allGroupsUrl: string,
    loginUrl: string,
    logoutUrl: string,
    historyUrl: string
}

export const CONFIG: ApplicationConfig = {
    //URls app can send requests to
    allowedUrls: "http://www.dcs.gla.ac.uk",
    //URL to send barcodes to
    submitAttendanceUrl: "http://www.dcs.gla.ac.uk/attendance/rest/submitAttendance",
    //URL to return list of students in tutorial group
    studentsInGroupUrl: "http://www.dcs.gla.ac.uk/attendance/rest/getStudentsInGroup",
    //URL to delete an item of attendance
    deleteAttendanceUrl: "http://www.dcs.gla.ac.uk/attendance/rest/deleteAttendance",
    //URL to return list of tutorial groups for tutor
    groupsPerTutorUrl: "http://www.dcs.gla.ac.uk/attendance/rest/getGroups",
    //URL to return list of all active tutorial groups
    allGroupsUrl: "http://www.dcs.gla.ac.uk/attendance/rest/getAllGroups",
    //URL to check user credentials
    loginUrl: "http://www.dcs.gla.ac.uk/attendance/rest/login",
    //URL to logout user
    logoutUrl : "http://www.dcs.gla.ac.uk/attendance/rest/logout",
    //URL to get student attendance history
    historyUrl: "http://www.dcs.gla.ac.uk/attendance/rest/getHistory"
}

//Used to prevent naming conflict when inserting the object
export const CONFIG_TOKEN = new InjectionToken<ApplicationConfig>('config');