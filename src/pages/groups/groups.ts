import { Component, Inject, ViewChild } from '@angular/core';
import { CONFIG, ApplicationConfig, CONFIG_TOKEN } from '../../app/app-config';
import { AlertController, NavController, NavParams, Content } from 'ionic-angular';
import { BackendService } from '../../app/backend.service';
import { StudentsPage } from '../students/students';
import { LoginPage } from '../login/login';

@Component({
    templateUrl: 'groups.html',
    providers : [{provide: CONFIG_TOKEN, useValue: CONFIG }]
  })
export class GroupsPage{
  @ViewChild(Content) content: Content;
  public groups;
  public login;
  keys;

  /**
   * @constructor
   * @param platform 
   * @param statusBar 
   * @param splashScreen 
   * @param backendService -used to send requests to Java service
   * @param config - application configuration
   */
  constructor(
    @Inject(CONFIG_TOKEN) private config: ApplicationConfig, 
    private backendService: BackendService,
    public alertCtrl: AlertController,
    private navCtrl: NavController,
    private navParams: NavParams,) {
      this.login = navParams.get('login');
      this.displayGroups();
  }

  //needed to properly display content when returning to this page from another
  ionViewDidEnter() {
    this.content.resize();
  }

  /**
   * Navigates to page showing students in selected tutorial group
   * @param course - Used when saving attendance
   * @param groupId - Selected Tutorial group ID
   * @param courseId - Used when saving attendance
   */
  public goToGroupPage(course, groupId, courseId, groupName){
    this.navCtrl.push(StudentsPage, 
      { course: course, groupId: groupId, courseId : courseId, login: this.login, groupName : groupName });
  }

  /**
   * Queries and displays a list of tutorial groups for tutor
   * URL to request list from is specified in config
   */
  public displayGroups(){
    this.backendService.requestGroupsPerTutor(this.config.groupsPerTutorUrl, this.login).subscribe(
      response => {
        this.groups = this.divideGroups(response.json());
        if(this.groups.length == 0){
          this.showAlert("No tutorial groups registered for this tutor", "");
        }
      },
      err => {
        var message = err.json().message;
        if(!message){
          message = "Connection error";
        }
        //Client-side error
        if(err.status == 403){
          this.showAlert("Error", message);
          this.navCtrl.setRoot(LoginPage);
        } else if(err.status == 400){
          this.showAlert("Invalid login", message);
        //Server-side error - give user the option to try again
        }else{
          this.showPrompt(
          message, 
          "Try again?", 
          'No', 
          'Yes',
          GroupsPage.prototype.displayGroups);
        }
      });
  }

  /**
   * Queries and displays a list of all active tutorial groups
   * To be used when a tutor is filling in for someone else and not tutoring their own tutorial groups
   * URL to request list from is specified in config
   */
  public displayAllGroups(){
    this.backendService.requestAllGroups(this.config.allGroupsUrl, this.login).subscribe(
      response => {
        this.groups = this.divideGroups(response.json());
        if(this.groups.length == 0){
          this.showAlert("No tutorial groups are currently active", "");
        }
      },
      err => {
        var message = err.json().message;
        if(!message){
          message = "Connection error";
        }
        this.showPrompt(
        message, 
        "Try again?", 
        'No', 
        'Yes',
        GroupsPage.prototype.displayAllGroups);
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

  /**
   * Divides all tutorial group into separate lists by course
   * @param groups - all tutorial groups
   */
  public divideGroups(groups){
    let dividedGroups = {}
    for(let group of groups){
      if (group.course in dividedGroups){
        dividedGroups[group.course].push(group);
      }
      else{
        dividedGroups[group.course] = [group];
      }
    }

    //Store all course names for easy access in template
    this.keys = Object.keys(dividedGroups);
    return dividedGroups;
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