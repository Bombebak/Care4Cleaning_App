<h1>Android mandatory 1 autumn 2016</h1>
<h3>Process</h3>
<p>Here is a short presentation of the process of the app. 
As a requirement from the assignment the first time the app is run, the user will be displayed with a registration screen. 
The user input its username and if the registration is successful the user will be redirected to the “main” page. 
In case the registration is unsuccessful a validation message in form of a Toast will be shown, 
which for example can be that the user forgot to type anything in the username text field. 
The two pictures below are screenshots of the registration page with orientation horizontal and vertical. </p>
<p>
<img alt="Choose username page Port" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/Create-user_choose_username.png" width="300" height="500"/>

<img alt="Choose username page Landscape" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/Create-user_choose_username_landscape.png" width="500" height="400"/>
</p>
<p>
When the user clicks “OK” on the registration page a progress dialog will be shown which is displayed below. 
In case the registration is successful a dialog will be shown with a confirmation message. The dialog is also shown below.
</p>
<p>
<img alt="Wait user is being created dialog" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/wait-user-being-created.png" width="350" height="400"/>
<img alt="User has been created dialog" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/user-is-created.png" width="450" height="400"/>
</p>
<p>
From now on when the user opens the app it will skip the registration page and load the main the page. This is done by adding a token in the SharedPreferences. Whenever the user opens the phone the token is examined and validated and if the token exists then the user will skip the registration page. 
After the registration page the user will be redirected to the “main page”. Below is a couple of screenshots with both orientations.
</p>
<p>
<img alt="Home page Port" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/main-page-port.png" width="450" height="400"/>
<img alt="Home page Landscape" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/main-page-landscape.png" width="450" height="400"/>
</p>
<p>
This page contains the primary functionality which is uploading images with a case id and description. 
The user is able to either pick an existing or take a new image with the camera on the phone. 
If the user wants to take a picture with the image a dialog will be shown asking permission to use the camera the phone. 
The same happens if the user wants to pick an existing picture.
![Grant access permission](https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/grant-access-to-app.png)
Depending on the action of the user a Snackbar will be shown with the message that access has been granted or not. 
This is just a confirmation to the user that access has either been or not been granted. 
In case the user tries to upload but forgot to fill out the fields or submit an image a Toast will show containing the error message. 
As previously a progress dialog will be shown when all fields are completed. 
In case the upload fails which could be if the user inputs a case id that doesn’t exist a Toast will be shown with error message. 
Below is a couple of screenshots with the cases as described. 
</p>
<img alt="Upload image validation dialog" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/uploadImage-validation.png" width="450" height="400"/>
<img alt="Image being uploaded dialog" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/ImageUpload-being-uploaed.png" width="450" height="400"/>
<img alt="Image upload complete" src="https://github.com/Bombebak/Care4Cleaning_App/blob/master/app/src/main/res/drawable/uploadImage-upload-complete.png" width="450" height="400"/>
