import { async, TestBed } from '@angular/core/testing';
import { IonicModule, Platform } from 'ionic-angular';

import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { BarcodeScanner } from '@ionic-native/barcode-scanner';
import { BackendService } from '../../app/backend.service';
import {} from 'jasmine';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { StudentsPage } from './students';
import {
    PlatformMock,
    StatusBarMock,
    SplashScreenMock,
    BarcodeScannerMock,
    BackendServiceMock,
    AlertControllerMock,
    NavParamsMock,
    ErrorMock,
    NavControllerMock
} from '../../../test-config/mocks-ionic';
import { AlertController } from 'ionic-angular/components/alert/alert-controller';
import { NavParams } from 'ionic-angular/navigation/nav-params';
import { NavController } from 'ionic-angular/navigation/nav-controller';

describe('MyApp Component', () => {
    let fixture;
    let component;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [StudentsPage],
            imports: [
                IonicModule.forRoot(StudentsPage)
            ],
            providers: [
                { provide: StatusBar, useClass: StatusBarMock },
                { provide: SplashScreen, useClass: SplashScreenMock },
                { provide: Platform, useClass: PlatformMock },
                { provide: BarcodeScanner, useClass: BarcodeScannerMock },
                { provide: BackendService, useClass: BackendServiceMock },
                { provide: AlertController, useClass: AlertControllerMock },
                { provide: NavParams, useClass: NavParamsMock },
                { provide: NavController, useClass: NavControllerMock }
            ]
        })
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(StudentsPage);
        component = fixture.componentInstance;
    });

    it('should be created', () => {
        expect(component instanceof StudentsPage).toBe(true);
    });

    it('should not try to save attendance when cancelled scan', () =>{
        spyOn(component, "sendAttendance");
        component.scan();
        expect(component.sendAttendance).toHaveBeenCalledTimes(0);
    })

    it('should update attendedToday when saving attendance', () =>{
        var student = {"login":"1234567a", "name" : "Test", "attendedToday" : false, "barcode" : "123456789101"};
        component.students = [student];
        component.sendAttendance(student);
        expect(component.students[0].attendedToday).toBe(true);
    })

    it('should not update attendedToday when failed to save attendance', ()=>{
        var student = {"login":"1234567a", "name" : "Test", "attendedToday" : false, "barcode" : "123456789101"};
        component.students = [student];
        spyOn(component.backendService, 'sendAttendance').and.callFake(function(){
            return Observable.create(observer =>{
                observer.error(new ErrorMock());
                observer.complete();
            })
        });
        component.sendAttendance(student);
        expect(component.students[0].attendedToday).toBe(false);
    })

    it('should find corresponding student by barcode when saving attendance', ()=>{
        var student = {"login":"1234567a", "name" : "Test", "attendedToday" : false, "barcode" : "123456789101"};
        component.students = [student];
        component.sendAttendance({"barcode" : "123456789101"});
        expect(component.students[0].attendedToday).toBe(true);
    })

    it('should not update attendedToday when failed to delete attendance', ()=>{
        var student = {"login":"1234567a", "name" : "Test", "attendedToday" : true, "barcode" : "123456789101"};
        component.students = [student];
        spyOn(component.backendService, 'deleteAttendance').and.callFake(function(){
            return Observable.create(observer =>{
                observer.error(new ErrorMock());
                observer.complete();
            })
        });
        component.deleteAttendance(student);
        expect(component.students[0].attendedToday).toBe(true);
    })
});