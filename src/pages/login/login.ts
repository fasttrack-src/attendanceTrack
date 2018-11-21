import { Component, Inject, ViewChild } from '@angular/core';
import { CONFIG, ApplicationConfig, CONFIG_TOKEN } from '../../app/app-config';
import { Platform, NavController, AlertController, Content } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { BackendService } from '../../app/backend.service';
import { GroupsPage } from '../groups/groups';
import { HistoryPage } from '../history/history';

@Component({
    templateUrl : 'login.html',
    providers : [{provide: CONFIG_TOKEN, useValue: CONFIG }]
})
export class LoginPage{
  @ViewChild(Content) content: Content;
    guid: string;
    password: string;

    constructor(
        @Inject(CONFIG_TOKEN) private config: ApplicationConfig, 
        platform: Platform, 
        statusBar: StatusBar, 
        splashScreen: SplashScreen,
        private backendService: BackendService,
        private navCtrl: NavController,
        private alertCtrl: AlertController) {
    }

    //needed to properly display content when returning to this page from another
    ionViewDidEnter() {
      this.content.resize();
    }

    public login(){
        this.backendService.checkLogin(this.config.loginUrl, this.guid, this.password).subscribe(
            //If logged in, show list of tutorial groups
            response => {
              if(response.json().isTutor){
                this.navCtrl.push(GroupsPage, { login: this.guid });
              }else{
                this.navCtrl.push(HistoryPage, {login : this.guid, studentLogin: this.guid});
              }
            },
            err => {
              var message = err.json().message;
              if(!message){
                message = "Connection error";
              }
              //Client-side error
              if(err.status == 400){
                  this.showAlert("Invalid login details", message);
              //Server-side error - give user the option to try again
              }else{
                this.showPrompt(
                    message, 
                    "Try again?", 
                    'No', 
                    'Yes',
                    LoginPage.prototype.login);
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

  //Displays a basic alert
  showAlert(titleText, subtitle) {
    let alert = this.alertCtrl.create({
      title: titleText,
      subTitle: subtitle,
      buttons: ['OK']
    });
    alert.present();
  }
}