import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions } from '@angular/http';
import "rxjs/add/operator/map";

/**
 * Used to send requests to Java service
 * @author Maria-Luiza Koleva
 */
@Injectable()
export class BackendService{

    constructor(private http:Http){
    }

    /**
     * Sends a POST request with a barcode
     * Used to save an item of attendance
     * @param barcode - String of digits that represents a scanned barcode
     * @param url - Specifies URL of service to receive barcode
     * @param course - Course to save attendance for
     * @param courseId - ID of course to save attendance for
     */
    sendAttendance(barcode, url, course, courseId, guid){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var date = new Date();
        
        //getMonth() returns number representing month starting from 0(January)
        let postParams = {
            "barcode": barcode,
            "year": date.getFullYear(),
            "month": date.getMonth() + 1,
            "date": date.getDate(),
            "hour": date.getHours(),
            "minute": date.getMinutes(),
            "course": course,
            "courseId": courseId,
            "guid" : guid
        }
        var response = this.http.post(url, postParams, options);
        return response;
    }

    /**
     * Sends a POST request with a barcode
     * Used to delete an item of attendance
     * @param barcode - String of digits that represents a scanned barcode
     * @param url - Specifies URL of service to receive barcode
     * @param course - Course to delete attendance for
     * @param courseId - ID of course to delete attendance for
     */
    deleteAttendance(barcode, url, course, courseId, guid){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var date = new Date();
        
        //getMonth() returns number representing month starting from 0(January)
        let postParams = {
            "barcode": barcode,
            "year": date.getFullYear(),
            "month": date.getMonth() + 1,
            "date": date.getDate(),
            "course": course,
            "courseId": courseId,
            "guid" : guid
        }
        var response = this.http.post(url, postParams, options);
        return response;
    }

    /**
     * Requests list of students in a tutorial group
     * @param url - Specifies URL of service to receive request
     * @param groupId - Tutorial group ID
     * @param course - Used to check student attendance for the day
     */
    requestStudentsInGroup(url, groupId, course, guid){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var response = this.http.post(url, { "groupId" : groupId , "course": course, "guid" : guid }, options);
        return response;
    }

    /**
     * Requests list of students in a tutorial group
     * @param url - Specifies URL of service to receive request
     * @param studentLogin - Student Login
     */
    requestHistory(url, studentLogin, guid){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var response = this.http.post(url, { "studentLogin" : studentLogin , "guid" : guid }, options);
        return response;
    }

    /**
     * Requests list of tutorial groups for tutor
     * Login currently hard-coded, will change it when tutor authentication is implemented
     * @param url - Specifies URL of service to receive request
     */
    requestGroupsPerTutor(url, login){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var response = this.http.post(url, { "guid" : login }, options);
        return response;
    }

    /**
     * Requests list of all active tutorial groups
     * @param url - Specifies URL of service to receive request
     */
    requestAllGroups(url, guid){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var response = this.http.post(url, { "guid" : guid }, options);
        return response;
    }

    /**
     * Checks that login and password are valid
     * @param url - Specifies URL of service to validate login and password
     * @param guid - Tutor username(Glasgow University ID)
     * @param password - Tutor password
     */
    checkLogin(url, guid, password){
        var headers = new Headers();
        headers.append('Content-Type', 'application/json');

        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({headers: headers, withCredentials: true});
        var response = this.http.post(url, { "guid" : guid, "password": password }, options);
        return response;
    }

    /**
     * Logs out user by removing their cookie
     * @param url - Specifies URL of service to log out user
     */
    logOut(url){
        //setting withCredentials to true takes care of cookies on client-side
        let options = new RequestOptions({ withCredentials: true });
        var response = this.http.get(url, options);
        return response;
    }
}