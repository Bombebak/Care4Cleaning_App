package com.example.den_f.mandatoryassignment_care4cleaning;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Den_F on 24-10-2016.
 */

public class OkCancelInputDialog {

    AlertDialog.Builder alert;
    String userInput = "";
    Resources resources;

    public String getUserInput()
    {
        return userInput;
    }

    public OkCancelInputDialog(Context context, String title, String message, String defaultInput) {
        alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        final EditText input = new EditText(context);
        input.setText(defaultInput);
        alert.setView(input);

        resources = context.getResources();

        alert.setPositiveButton(resources.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userInput = input.getText().toString();
                clickOk();

            }
        });


    }

    public OkCancelInputDialog(final Context context, String title, String message) {
        alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        final EditText input = new EditText(context);
        alert.setView(input);
        resources = context.getResources();
        alert.setCancelable(false);



        alert.setPositiveButton(resources.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userInput = input.getText().toString();
                if (userInput.length() <= 0 ) {

                    Toast toast = Toast.makeText(context,R.string.usernameEmpty,Toast.LENGTH_LONG);
                    toast.show();

                }
                else {

                    clickOk();

                }


            }
        });



    }

    public void clickOk()
    {

    }


    public void show()
    {
        alert.show();
    }


}
