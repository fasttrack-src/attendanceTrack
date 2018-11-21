# Attendance monitoring project

## System architecture
<img src="/documentation/Design/ArchitectureDiagram.png"/>

### Components
<ul>
  <li>Ionic app</li>
  <li>Java RESTful service</li>
  <li>University database</li>
</ul>

## Java service design
<img src="/documentation/Design/JavaServiceDiagram.png"/>

### End points
<ul>
  <li>
    login - Used to log tutor in to the application</br>
    </br> Expects a JSON POST request of the following format: 
    </br> {"guid" : "{Glasgow University ID}", "password" : "{SOCS password}"}
  </li>
  </br>
  <li>logout - Used to log tutor out of the application if they are logged in</li>
  </br>
  <li>
    getGroups - Used to return a list of lab groups for a particular tutor </br></br>
    Expects a JSON POST request of the following format: </br>
    {"guid": "{Glasgow University ID}"}
  </li>
  </br>
  <li>
    getAllGroups - Used to return a list of all currently active lab groups regardless of who their tutor is</br></br>
    Expects a JSON POST request of the following format: </br>
    {"guid": "{Glasgow University ID}"}
  </li>
  </br>
  <li>
    getStudentsInGroup - Used to return a list of students in a lab group</br></br>
    Expects a JSON POST request of the following format: </br>
    {"guid": "{Glasgow University ID}", "groupId" : "{Tutorial group ID}", "course" : "{Course name}"}
  </li>
  </br>
  <li>
    submitAttendance - Used to submit items of attendance</br></br>
    Expects a JSON POST request of the following format: </br>
    {"guid": "{Glasgow University ID}", "course" : "{Course name}", "courseId" : "{Course ID}", "barcode" : "{Student card barcode}", "year" : "{Year}", "month" : "{Month}", "date" : "{Day of month}", "hour" : "{Hour}", "minute" : "{minute}"}
  </li>
  </br>
  <li>
    deleteAttendance - Used to delete an item of attendance (in case it has been submitted in error)</br></br>
    Expects a JSON POST request of the following format: </br>
    {"guid": "{Glasgow University ID}", "course" : "{Course name}", "courseId" : "{Course ID}", "barcode" : "{Student card barcode}", "year" : "{Year}", "month" : "{Month}", "date" : "{Day of month}"}
  </li>
</ul>

### Components
<ul>
  <li>AttendanceService - The AttendanceService class contains all REST endpoints. Its purpose is to service requests from the Ionic app by creating instances of DbConnection and query classes as needed and calling their methods. The AttendanceService class is responsible for loading the service configuration and passing relevant values to the other classes. It also sends an HTTP Response to the client with a status code that indicates whether the request was successfully serviced.</li>
  <li>DbConnection - The DbConnection class is responsible for establishing a connection to the database, using the URL and credentials passed in its constructor. It then can supply the Connection object to any class that calls the getConnection() method.</li>
  <li>SubmitBarcodeQuery - The SubmitBarcodeQuery class is responsible for creating a PreparedStatement to insert an entry into the lectureattendance table and executing it with passed-in values. More similar query classes will be created as needed.</li>
</ul>

## Wireframes
### Android
<img src="/documentation/AndroidWireframes/Screen 1.png"/>
<img src="/documentation/AndroidWireframes/Groups list.png"/>
<img src="/documentation/AndroidWireframes/Students list.png"/>
<img src="/documentation/AndroidWireframes/Scanning.png"/>
<img src="/documentation/AndroidWireframes/Successful scan.png"/>
<img src="/documentation/AndroidWireframes/Manual input.png"/>

### iOS
<img src="/documentation/iPhoneWireframes/Screen 1.png"/>
<img src="/documentation/iPhoneWireframes/Groups list.png"/>
<img src="/documentation/iPhoneWireframes/Students list.png"/>
<img src="/documentation/iPhoneWireframes/Scanning.png"/>
<img src="/documentation/iPhoneWireframes/Successful scan.png"/>
<img src="/documentation/iPhoneWireframes/Manual input.png"/>

## Actors
The following actors have been identified:
<ul>
  <li>Tutor - uses the system to record the attendance of their students</li>
  <li>Administrator - uses the system to view attendance data, send reminders to students who are not attending, notify other staff of students who are not attending</li>
  <li>Student - uses the system to view their attendance</li>
</ul>

## Use case diagrams
<img src="/documentation/UseCases/TutorUseCase2.png"/>
<img src="/documentation/UseCases/AdministratorUseCase.png"/>
<img src="/documentation/UseCases/StudentUseCase.png"/>
<img src="/documentation/UseCases/StudentTutorUseCase.png"/>

## Requirements
### Functional
#### Must have
<ul>
  <li>Tutors can log in to mobile application</li>
  <li>Tutors can choose the course and lab group to record attendance for</li>
  <li>If a tutor is covering for someone else they can choose a lab group they don't usually tutor</li>
  <li>Be able to scan student cards with mobile application</li>
  <li>Scanned barcodes should be saved to database</li>
  <li>Tutors should be able to save recorded attendance to database at a later time in case there is no WiFi</li>
  <li>Attendance data should be saved in database only once regardless of how many times Save/Send button is pressed</li>
  <li>Tutors should be able to scan students who are not expected to show up for their group</li>
  <li>Using DL it should be possible to input attendance for student who hasn't been scanned yet</li>
</ul>

#### Should have
<ul>
  <li>Tutor can see list of students in lab group</li>
  <li>Tutor can see indication of students who haven't been attending</li>
  <li>Tutors should be able to input attendance for student if they've forgotten their card</li>
  <li>Tutor should be able to input student attendance at a later time if they have forgotten their smartphone</li>
</ul>

#### Could have
<ul>
  <li>DL should display student attendance by course and group</li>
  <li>Students could be able to check their attendance</li>
  <li>API to the systems so tutors can get a CSV dump of a groups' attendance, and can upload their attendance as a CSV file</li>
</ul>

#### Would be nice to have
<ul>
  <li>Tutor can also input mark(ticks, stars) as well as attendance</li>
  <li>Tutors can scan barcodes while not authenticated in case their credentials haven't been set up yet. They can then login at a later time and only then save scanned barcodes to database.</li>
</ul>

### Non-functional
<ul>
  <li>Cross-platform</li>
  <li>Reliability</li>
  <li>Performance</li>
  <li>Security</li>
  <li>Data persistence</li>
  <li>Usability</li>
  <li>Responsiveness</li>
  <li>Good documentation</li>
  <li>App is aesthetically pleasing</li>
  <li>Scalability</li>
  <li>Extendability</li>
</ul>

## User stories
As a tutor<br/>
I want to use my mobile phone to record attendance<br/>
So that I don't have to carry a scanner

As a tutor<br/>
I want to automatically save attendance data<br/>
So that I don't have to use one particular machine in Boyd Orr to do it

As an administrator<br/>
I want to have reliable data on the attendance of Tier 4 students<br/>
So that I can comply with government regulations

As a tutor/lecturer<br/>
I want to know when a student is not attending<br/>
So that I can talk to them and make sure they are not falling behind

As a member of the school support team<br/>
I want to have the attendance data properly formatted<br/>
So that I don't have to fix mistakes manually

As a tutor<br/>
I want to automatically save attendance data<br/>
So that I don't have to install software to do it

As  a member of the school support team<br/>
I want all attendance data to have the correct date<br/>
So that I don't have to manually fix it

As an administrator<br/>
I want all attendance data files to be labelled correctly<br/>
So that it all appears under the same course name on DL

As a student<br/>
I want to check my attendance<br/>
So that I am not incorrectly marked as not attending

As an administrator<br/>
I want to input attendance data for multiple students at a time<br/>
So that I can be more efficient at my job

As an administrator<br/>
I want alerts for students to be cleared when they start attending again<br/>
So that I can focus on reminders for students who are not attending

As an administrator<br/>
I want to see students grouped by course and group<br/>
So that I can find them more quickly

As an administrator<br/>
I want an attendance recording system which is easy to use<br/>
So that I don't have to teach tutors how to use it

As an administrator<br/>
I want DC to be updated from MyCampus<br/>
So that I don't have to manually update it

As an administrator<br/>
I want DC not to break down<br/>
So that I can do my job

As an administrator<br/>
I want to add students who haven't had their card scanned<br/>
So that I can record their attendance

As an administrator<br/>
I want to delete an item of attendance<br/>
So that I can fix any mistakes

As a tutor<br/>
I want to record attendance and marks on the same application<br/>
So that it is more convenient and quicker for me to mark students

As a tutor<br/>
I want to be able to save attendance data after the lab<br/>
So that I can still save it in case I don't have access to WiFi during the lab

As a tutor<br/>
I want to save attendance data to database only once<br/>
So that I have the attendance of my students recorded correctly

As a tutor<br/>
I want to record attendance of students not in my usual lab group<br/>
So that I can fill in for a colleague

As a tutor<br/>
I want to record attendance of students not in my usual lab group<br/>
So that I can record attendance for students who show up to a different lab group than they should

As a tutor<br/>
I want to see a list of the students in my lab group<br/>
So that I know who is supposed to show up for the lab

As a tutor<br/>
I want to record the attendance of students without scanning their student cards<br/>
So that I can show that students who forgot their student cards still attended
