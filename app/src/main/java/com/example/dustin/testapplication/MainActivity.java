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

/*
    Google now recongition notes:
        Saying "times" produces a "*", but saying multiplied by produces a "x"
*/


public class MainActivity extends AppCompatActivity {

    static final int check = 111;


    ArrayList<String> results;      //results of voice recognition (from the recognizer itself)
                                    // *contains likely matches [0] being most likely
    String [] resultAfterSplit;     //result
    Expression exp;                 //expression for the string to math expression
    BigDecimal result;              //the result received by expression




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Starts Speech recognition processes. This will only be called from the application itself, not this code.
     * @param view Paramater passed into any method called from the application itself
     */
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

            }

            //CALL ALL MAJOR METHODS HERE (math(), conversion(), phone(), etc)
            math();

        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    //NOTE: be sure to check for "search the web for" type expressions before checking for anything else.

    /**
     * Checks what was said to see if the user wants to complete a math problem, and completes it.
     * It can currently detect the following: <br/>
     * <table>
     *     <caption>Current Recognizable Operations</caption>
     *     <tr>
     *         <th>Operand</th>
     *         <th>Operation Performed</th>
     *         <th>Phrases Recognized</th>
     *     </tr>
     *     <tr>
     *         <td>+</td>
     *         <td>Addition</td>
     *         <td>"plus"</td>
     *     </tr>
     *     <tr>
     *         <td>-</td>
     *         <td>Subtraction</td>
     *         <td>"minus"</td>
     *     </tr>
     *     <tr>
     *         <td>*</td>
     *         <td>Multiplication</td>
     *         <td>"times", "multiplied by"</td>
     *     </tr>
     *
     * </table>
     *
     *
     */
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
                        //Checks to ensure that the things before and after the operand are numbers.
                        if(isNumeric(resultAfterSplit[i-1].trim()) && isNumeric(resultAfterSplit[i+1].trim())) {
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i - 1] + "+" + resultAfterSplit[i + 1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i-1
                        else if(result != null && !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                (resultAfterSplit[i-1].trim().equals("this") ||
                                        resultAfterSplit[i-1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat(result + "+" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(result != null && !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                (resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "+" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }



                    }

                    //subtraction
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("minus") || resultAfterSplit[i].trim().equalsIgnoreCase("-")){

                        //Checks to ensure that the things before and after the operand are numbers.
                        if(isNumeric(resultAfterSplit[i-1].trim()) && isNumeric(resultAfterSplit[i+1].trim())) {
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i - 1] + "-" + resultAfterSplit[i + 1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i-1
                        else if(result != null && !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                (resultAfterSplit[i-1].trim().equals("this") ||
                                        resultAfterSplit[i-1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat(result + "-" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(result != null && !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                (resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "-" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }


                    }

                    //multiplication
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("times") || resultAfterSplit[i].trim().equalsIgnoreCase("*")
                            || resultAfterSplit[i].trim().equalsIgnoreCase("multiplied by")
                            || resultAfterSplit[i].trim().equalsIgnoreCase("x")){

                        //Checks to ensure that the things before and after the operand are numbers.
                        if(isNumeric(resultAfterSplit[i-1].trim()) && isNumeric(resultAfterSplit[i+1].trim())){
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i-1] + "*" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i-1
                        else if(result != null && !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                (resultAfterSplit[i-1].trim().equals("this") ||
                                        resultAfterSplit[i-1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat(result + "*" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(result != null && !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                (resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "*" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }




                    }

                    //division

                    //to percent

                    //percent of

                    //squared

                    //cubed

                    //sqrt




                }
            }

            //if the equation is not empty, evauluate it.
            if(!equation.trim().equalsIgnoreCase("")) {
                exp = new Expression(equation);
                result = exp.eval();

                Log.println(Log.INFO, "result", "" + result);
                TextView t = (TextView) findViewById(R.id.textView);
                t.append("\nThe result of " + equation + " is " + result);

            }

        }
    }


    //isNumeric retreived from http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java

    /**
     * This checks a string to see if it is a number.
     * @param str The string to check
     * @return True if the string is a string. False if it is not.
     */
    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    //CONVERSIONS

    //temperature F to C C to F

    //PHONE FUNCTIONS

    //call, dial, (etc) contact



}
