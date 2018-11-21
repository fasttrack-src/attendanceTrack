import { Component, Inject } from '@angular/core';
import { Platform } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { LoginPage } from '../pages/login/login';
import { SplashScreen } from '@ionic-native/splash-screen';
import { CONFIG, ApplicationConfig, CONFIG_TOKEN } from './app-config';
 
@Component({
    templateUrl: 'app.html',
    providers : [{provide: CONFIG_TOKEN, useValue: CONFIG }]
})
export class MyApp {
  rootPage: any = LoginPage;
 
  constructor(
    public platform: Platform,
    public splashScreen: SplashScreen,
    public statusBar: StatusBar,
    @Inject(CONFIG_TOKEN) private config: ApplicationConfig, 
  ) {
    this.initializeApp();
  }
 
  initializeApp() {
    this.platform.ready().then(() => {
        // the platform is ready and our plugins are available.
        this.statusBar.styleDefault();
        this.splashScreen.hide();
      });
  }
}