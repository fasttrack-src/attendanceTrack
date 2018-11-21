import { Component, Inject, ViewChild } from '@angular/core';
import { AlertController, NavParams, NavController, Content } from 'ionic-angular';

import { BackendService } from '../../app/backend.service';
import { CONFIG, ApplicationConfig, CONFIG_TOKEN } from '../../app/app-config';
import { LoginPage } from '../login/login';
import { CalendarComponentOptions } from 'ion2-calendar';

@Component({
  templateUrl: 'history.html',
  providers : [{provide: CONFIG_TOKEN, useValue: CONFIG }]
})
export class HistoryPage {
  @ViewChild(Content) content: Content;
  public attendanceHistory;
  public login;
  public studentLogin;
  public keys;
  public dates = {};
  public studentName;
  type: 'string'; 
  optionsMulti: CalendarComponentOptions= {
    pickMode: 'multi',
    from: new Date(1),
    weekStart: 1
  };

  /**
   * @constructor
   * @param platform 
   * @param statusBar 
   * @param splashScreen 
   * @param backendService -used to send requests to Java service
   * @param config - application configuration
   * @param navParams - used to retrieve parameters passed in by previous page
   */
  constructor(
    @Inject(CONFIG_TOKEN) private config: ApplicationConfig, 
    private backendService: BackendService,
    public alertCtrl: AlertController,
    private navParams: NavParams,
    private navCtrl: NavController) {
    this.login = navParams.get('login');
    this.studentLogin = navParams.get('studentLogin');
    this.studentName = navParams.get('studentName');
    this.displayHistory();
  }

  //needed to properly display content when returning to this page from another
  ionViewDidEnter() {
    this.content.resize();
  }

  //gets student attendance history
  public displayHistory(){
    this.backendService.requestHistory(
      this.config.historyUrl, 
      this.studentLogin, 
      this.login)
    .subscribe(
      response => {
        var reply = response.json();
        if(reply.length == 0){
          this.showAlert("No attendance history for this student", "");
        }
        this.attendanceHistory = this.divideItems(reply);
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
          HistoryPage.prototype.displayHistory);
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
   * Divides all attendance items into separate lists by course
   * @param items - all attendance items
   */
  public divideItems(items){
    let dividedItems = {}
    for(let item of items){
        var date = this.buildDate(item);
      if (item.course in dividedItems){
        dividedItems[item.course].push(item);
        this.dates[item.course].push(date);
      }
      else{
        dividedItems[item.course] = [item];
        this.dates[item.course] = [date];
      }
    }

    //Store all course names for easy access in template
    this.keys = Object.keys(dividedItems);
    return dividedItems;
  }

    private buildDate(item){
        var date = item.year + "-";
        if(item.month < 10){
            date = date + "0";
        }
        date = date + item.month + "-";
        if(item.date < 10){
            date = date + "0";
        }
        date = date + item.date;
        return date;
    }
}