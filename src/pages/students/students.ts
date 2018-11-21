import { Component, Inject, ViewChild } from '@angular/core';
import { AlertController, NavParams, NavController, ActionSheetController, Content } from 'ionic-angular';
import { BarcodeScanner } from '@ionic-native/barcode-scanner';

import { BackendService } from '../../app/backend.service';
import { CONFIG, ApplicationConfig, CONFIG_TOKEN } from '../../app/app-config';
import { ToastController } from 'ionic-angular';
import { LoginPage } from '../login/login';
import { HistoryPage } from '../history/history';

@Component({
  templateUrl: 'students.html',
  providers : [{provide: CONFIG_TOKEN, useValue: CONFIG }]
})
export class StudentsPage {
  @ViewChild(Content) content: Content;
  public students;
  public responseMsg;
  public course;
  public courseId;
  public groupId;
  public login;
  public groupName;

  /**
   * @constructor
   * @param platform 
   * @param statusBar 
   * @param splashScreen 
   * @param barcodeScanner 
   * @param backendService -used to send requests to Java service
   * @param config - application configuration
   * @param navParams - used to retrieve parameters passed in by previous page
   */
  constructor(
    @Inject(CONFIG_TOKEN) private config: ApplicationConfig,
    private barcodeScanner: BarcodeScanner, 
    private backendService: BackendService,
    public alertCtrl: AlertController,
    private navParams: NavParams,
    private toastCtrl: ToastController,
    private navCtrl: NavController,
    public actionSheetCtrl: ActionSheetController) {
    this.course = navParams.get('course');
    this.groupId = navParams.get('groupId');
    this.courseId = navParams.get('courseId');
    this.login = navParams.get('login');
    this.groupName = navParams.get('groupName');
    this.displayStudents();
  }

  //needed to properly display content when returning to this page from another
  ionViewDidEnter() {
    this.content.resize();
  }

  presentActionSheet(item) {
    let actionSheet = this.actionSheetCtrl.create({
      title: '',
      buttons: [
        {
          text: 'View History',
          handler: () => {
            this.navCtrl.push(HistoryPage, 
              {login : this.login, studentLogin: item.login, studentName: item.name});
          }
        },{
          text: 'Record Attendance',
          handler: () => {
            this.changeAttendance(item);
          }
        }
      ]
    });
    actionSheet.present();
  }

  //gets list of students in this tutorial group
  public displayStudents(){
    this.backendService.requestStudentsInGroup(
      this.config.studentsInGroupUrl, 
      this.groupId, 
      this.course, 
      this.login)
    .subscribe(
      response => {
        this.students = response.json();
        if(this.students.length == 0){
          this.showAlert("No students registered for this tutorial group", "");
        }
      },
      err => {
        var message = err.json().message;
        if(!message){
          message = "Connection error";
        }
        if(err.status == 403){
          this.showAlert("Error", message);
          this.navCtrl.setRoot(LoginPage);
        }else if(err.status == 400){
          this.showAlert("Error", message);
        //Server-side error - give user the option to try again
        }else{
          this.showPrompt(
          message, 
          "Try again?", 
          'No', 
          'Yes',
          StudentsPage.prototype.displayStudents);
        }
      });
  }

  //Shows a prompt alert
  //Choosing option 1 closes the prompt alert
  //Final argument is function to be called if user chooses option 2
  public showPrompt(titleText, messageText, option1Text, option2Text, option2Function){
    let prompt = this.alertCtrl.create({
      title:titleText,
      message: messageText,
      buttons: [
        {
          text: option1Text
        },
        {
          text: option2Text,
          handler: data => {
            option2Function.call(this);
          }
        }
      ]
    });
    prompt.present();
  }

  //Manually mark student from the list as attended/not attended
  public changeAttendance(item){
    if(!item.attendedToday){
      this.showPromptSaveAttendance(item);
    }else{
      this.showPromptDeleteAttendance('Are you sure you want to delete attendance for this student?', item);
    }
  }

  //Shows a prompt alert when deleting a students' attendance
  //Choosing option 1 closes the prompt alert
  public showPromptDeleteAttendance(titleText, student){
    let prompt = this.alertCtrl.create({
      title:titleText,
      message: student.name,
      buttons: [
        {
          text: 'No'
        },
        {
          text: 'Yes',
          handler: data => this.deleteAttendance(student)
        }
      ]
    });
    prompt.present();
  }

  //Shows a prompt alert when saving a students' attendance
  //Choosing option 1 closes the prompt alert
  public showPromptSaveAttendance(student){
    let prompt = this.alertCtrl.create({
      title:'Are you sure you want to manually record attendance for this student?',
      message: student.name,
      buttons: [
        {
          text: 'No'
        },
        {
          text: 'Yes',
          handler: data => this.sendAttendance(student, false)
        }
      ]
    });
    prompt.present();
  }

  /**
   * Uses the scanning service to send an item of attendance to Java service
   * URL of Java service specified in app-config
   * @param student 
   */
  public sendAttendance(student, scanned){
    this.backendService.sendAttendance(
      student.barcode, 
      this.config.submitAttendanceUrl, 
      this.course, 
      this.courseId,
      this.login)
    .subscribe(
      response => {
        //If the student object doesn't have a login, then the barcode has been scanned
        //Find the corresponding student in the list if they are there
        if(student.login == null){
          for (let s of this.students){
            if (s.barcode == student.barcode){
              student = s;
            }
          }
        }
        student.attendedToday = true;
        //If student hasn't been found in list of students for this group
        //Notify user that they're not part of this tutorial group
        //But their attendance has been recorded because they are taking the course(checked by backend)
        if(student.login == null){
          if(scanned){
            this.showPrompt(
              'Attendance succesfully recorded. Student is not part of this group.', 
              "Scan another barcode?", 
              'No',
              'Yes', 
              StudentsPage.prototype.scan);
          }else{
            this.showAlert('Attendance succesfully recorded. Student is not part of this group.', "");
          }
        }else{
          if(scanned){
            this.scan();
          }
        }
      },
      err => {
        var message = err.json().message;
        if(!message){
          message = "Connection error";
        }
        //If client-side error
        if(err.status == 403){
          this.showAlert("Error", message);
          this.navCtrl.setRoot(LoginPage);
        } else if(err.status == 400){
          this.showAlert("Error", message);
        //Server-side error - give user the option to try again
        }else{
          if(scanned){
            this.showPrompt(
              "Could not save barcode. " + message, 
              "Scan again?", 
              'No', 
              'Yes', 
              StudentsPage.prototype.scan);
          }else{
            this.showAlert("Could not save attendance. " + message, "");
          }
        }
      });
  }

  public explainIcon(){
    let toast = this.toastCtrl.create({
      message: 'Students marked with this icon have not attended for the past two weeks',
      position: 'top',
      showCloseButton: true
    });
  
    toast.present();
  }

  /**
   * Uses the scanning service to request that item of attendance be deleted through Java service
   * URL of Java service specified in app-config
   * @param student 
   */
  public deleteAttendance(student){
    this.backendService.deleteAttendance(
      student.barcode, 
      this.config.deleteAttendanceUrl, 
      this.course, 
      this.courseId,
      this.login)
    .subscribe(
      response => {
        student.attendedToday = false;
        this.showAlert('Attendance succesfully deleted', student.name);
      },
      err => {
        var message = err.json().message;
        if(!message){
          message = "Connection error";
        }
        this.showPromptDeleteAttendance(
          message + ". Try again?", 
          student);
      });
  }

  /**
   * Requests that backend service log out the user
   * And returns to root(login) page
   */
  public logOut(){
    this.login = null;
    this.backendService.logOut(this.config.logoutUrl).subscribe(
      response => this.navCtrl.setRoot(LoginPage)
    )
  }

  //Displays a basic alert
  showAlert(titleText, subtitle) {
    let alert = this.alertCtrl.create({
      title: titleText,
      subTitle: subtitle,
      buttons: ['OK']
    });
    alert.present();
  }

  /**
   * Uses barcode scanner plugin to scan a barcode
   */
  public scan(){
    this.barcodeScanner.scan().then((barcodeData) => {
      //Check to only send request if user has scanned a barcode, not returned using the back button
      if(barcodeData.text.length > 0){
        //sendAttendance expects a barcode inside a student object
        this.sendAttendance({"barcode" : barcodeData.text}, true)
      }
    }, (err) => 
    this.showPrompt("Could not scan barcode", "Try again?", 'No', 'Yes', StudentsPage.prototype.scan));
  }
}