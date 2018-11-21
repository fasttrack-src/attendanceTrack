import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { SplashScreen } from '@ionic-native/splash-screen';
import { StatusBar } from '@ionic-native/status-bar';

import { StudentsPage } from '../pages/students/students';
import { GroupsPage } from '../pages/groups/groups'
import { LoginPage } from '../pages/login/login';
import { HistoryPage } from '../pages/history/history';
import { BarcodeScanner } from '@ionic-native/barcode-scanner';
import { HttpModule } from '@angular/http';
import { MyApp } from './app.component';
import { CalendarModule } from "ion2-calendar";

import { BackendService } from './backend.service';

@NgModule({
  declarations: [
    StudentsPage,
    GroupsPage,
    LoginPage,
    HistoryPage,
    MyApp
  ],
  imports: [
    BrowserModule,
    HttpModule,
    CalendarModule,
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    GroupsPage,
    StudentsPage,
    LoginPage,
    HistoryPage,
    MyApp
  ],
  providers: [
    StatusBar,
    SplashScreen,
    BarcodeScanner,
    BackendService,
    {provide: ErrorHandler, useClass: IonicErrorHandler}
  ]
})
export class AppModule {}
