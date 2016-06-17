package com.example.dustin.testapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.*;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static final int check = 111;
    ArrayList<String> results;
    String [] resultAfterSplit;
    Expression exp;
    BigDecimal result;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startRecognition(View view){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speek up son!");
        startActivityForResult(i, check);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == check && resultCode == RESULT_OK){


            results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.println(Log.INFO,"result", results.get(0));


            String resultBeforeSplit = results.get(0);
            resultAfterSplit = resultBeforeSplit.split(" ", 25);

            for(int i = 0; i < resultAfterSplit.length; i++){
                Log.println(Log.INFO, "result_"+ i, resultAfterSplit[i]);
                Log.println(Log.INFO, "result_"+ i, resultAfterSplit[i].trim());
            }

            math();

        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    //NOTE: be sure to check for "search the web for" type expressions before checking for anything else.


    public void math(){
        String equation = "";
        //check to see if there has been anything said
        if(resultAfterSplit != null){
            //loop looking for math terms
            for(int i = 0; i < resultAfterSplit.length; i++){
                //check to make sure i+1 isnt greater than or equal to the length and i-- is greater than 0
                if(i++ < resultAfterSplit.length && i-- > 0){
                    //search for "plus" "times" "divided by" "times"

                    //Addition
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("plus") || resultAfterSplit[i].trim().equalsIgnoreCase("+")){
                        //form the equation
                        equation = equation.concat(resultAfterSplit[i-1] + "+" +resultAfterSplit[i+1]);
                        Log.println(Log.INFO, "result", "Equation = " + equation);

                    }

                    //subtraction
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("minus") || resultAfterSplit[i].trim().equalsIgnoreCase("-")){
                        //form the equation
                        equation = equation.concat(resultAfterSplit[i-1] + "-" +resultAfterSplit[i+1]);
                        Log.println(Log.INFO, "result", "Equation = " + equation);

                    }


                }
            }

            //if the equation is not empty, evauluate it.
            if(!equation.trim().equalsIgnoreCase(""))
                exp = new Expression(equation);
                result = exp.eval();

                Log.println(Log.INFO, "result", "" + result);
                TextView t = (TextView) findViewById(R.id.textView);
                t.setText("The result of " + equation + " is " +result);


        }
    }



}
