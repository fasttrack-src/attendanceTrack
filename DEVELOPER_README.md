Building AttendanceLTC with Travis CI
=====================================

iOS
---

Ionic/Cordova can automatically prepare an XCode project file for building the app for iOS. This can be done both on a
Mac after installing Node, Ionic, Cordova and XCode, as well as through Travis where Node and XCode are installed by
default. In addition, Travis has been confiugred to build the ``.ipa`` file needed to submit changes to the App store
without any local modifications whatsoever.

However, in order to build the app for anything other than testing in iOS Simulator, we need additional
certificates. These are in ``build-assets/ios`` right now. However, these certificates will expire every year. Here is
the process of how to get AttendanceLTC to build for iOS using Travis.

### Certificates and profiles needed

#### The Apple Worldwide Developer Relations Certification Authority.

* This should not need to be updated, but if so, it
can be downloaded [here](http://developer.apple.com/certificationauthority/AppleWWDRCA.cer). Add it to ``build-assets/ios``
under its own name.

#### A valid Distribution Certificate

* For this, log into the UoG Apple Developer Account and create a new **production** distribution certificate
(``Certificates`` > ``Production`` > ``Add`` > ``App Store and Ad Hoc``), or use a valid one that has already been created.
* Follow the steps given by the website, and name the certificate something that marks it as belonging to AttendanceLTC for
easy access later.
* After you get the certificate downloaded, click on it to install it. Add this certificate under ``dist.cer`` in the same folder. 
* Open up ``Keychain Access``, find the certificate you installed under the login keychain and export the private key corresponding
to the certificate. The name of the key should match the name you gave previously. Right click and hit ``Export...``. The private key
will be exported after providing a passphrase. Note this phrase carefully. Add this exported private key under ``dist.p12``
in the same folder.
* Go to the Travis page for your build and hit ``More options`` > ``Settings``. Find the ``Environment Variables`` heading. There
should be a variable called ``ENCRYPTION_SECRET``. If not, create one, if so, delete it. The value for this variable should be the
passphrase you set previously.

#### A valid Provisioning Profile

* For this, in the same account, create a new **App Store** provisioning profile (``Provisioning Profiles`` > ``Distribution`` >
``Add`` > ``App Store``). The default permissions should be fine.
* Make sure you add the distribution certificate you would like to use with this build to the profile when prompted. Also add
any devices that you may want to use for testing if they are added to the Developer Network already.
* Download the profile and save it under its own name in ``build-assets/ios``. Open ``.travis.yml`` and change the value after
PROFILE_NAME to read that name.

After these are set up, you should be able to use the provided Travis build fixture to build the iOS version.

### Configuration needed

Ionic/Cordova stores all its build information in ``build.json``. You will need to edit this file after updating these
certificates. Under ``ios`` and ``build``, you will need to change the following information for the app to build correctly.

* ``codeSignIdentity`` is the name of the certificate as it appears in the Keystore. The default setting
(``iPhone Distribution: University of Glasgow (B5JS8NF3ZN)``) should be correct.
* ``developmentTeam`` is the ID of the Developer Network account. Unless the University creates a new account, this
should be the same.
* ``provisioningProfile`` is the UUID of the Provisioning Profile you download from the Developer Network. Open the provisioning
profile you downloaded from the Network in a text editor and find the key corresponding to UUID. The value to that key in the
XML should be the value you need to insert here.

### Travis environment ariables

In addition to the variable ``ENCRYPTION_SECRET`` which we already declared, we also need the username and password for credentials to an Apple Developer Account with Admin privileges that is linked to the University of Glasgow team. This is needed to automatically upload new versions of the app to the App Store for review. The username and password for this acconut needs to be stored in the same way (on the Travis page for the project in ``Settings > Environment Variables``), under ``APPSTORECONNECT_USERNAME`` and ``APPSTORECONNECT_PASSWORD``.

Android/AttendanceService
-------------------------

The Travis fixture should be able to build the signed ``.apk`` and the ``.war`` for AttendanceService without any user input.

### Signing the Android app

The Android app needs to be signed not only so it can be uploaded to the Play Store, but also in order to get it installed on a local machine for testing. Currently, the repository has a ``my-release-key.jks`` file in it which contains the release keys used to sign the first version of this app. The same release key must be used for all subsequent versions of the app or updates cannot be pushed.

Travis can automatically creaate the Android Studio project, build it and sign it for submission to the Play store and/or local testing. However, a Travis environment variable needs to be added  (on the Travis page for the project in ``Settings > Environment Variables``) under ``ANDROID_KEYSTORE_PASS``, which is the password to the keystore and the private key in the keystore for the original app developer that signed the first build of AttendanceLTC. This information can currently be found on the Trello board for the project.
