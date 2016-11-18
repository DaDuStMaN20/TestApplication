package com.example.dustin.testapplication;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.*;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.StringTokenizer;

/*
    Google now recognition notes:
        Saying "times" produces a "*", but saying multiplied by produces a "x"
*/


public class MainActivity extends AppCompatActivity {

    static final int check = 111;

    AudioManager manager;

    ArrayList<String> results;      //results of voice recognition (from the recognizer itself)
                                    // *contains likely matches [0] being most likely
    String [] resultAfterSplit;     //result
    Expression exp;                 //expression for the string to math expression
    BigDecimal result;              //the result received by expression

    //for numeric words to numbers
    public static final String[] DIGITS = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    public static final String[] TENS = {null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    public static final String[] TEENS = {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    public static final String[] MAGNITUDES = {"hundred", "thousand", "million", "point"};
    public static final String[] ZERO = {"zero", "oh"};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Starts Speech recognition processes. This will only be called from the application itself, not this code.
     * @param view Paramater passed into any method called from the application itself
     */
    public void startRecognition(View view){
        if(manager.isMusicActive()) {
            pause();

            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, "");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "How can I help?");
            startActivityForResult(i, check);

            play();
        }

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, "");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "How can I help?");
        startActivityForResult(i, check);

    }

    public void start(View view){
        startActivityForResult(new Intent(this, MainActivity.class), check);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){



        if(requestCode == check && resultCode == RESULT_OK){


            results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.println(Log.INFO,"result", results.get(0));


            String resultBeforeSplit = results.get(0);
            resultAfterSplit = resultBeforeSplit.split(" ", 25);

            EditText text = (EditText)findViewById(R.id.editText);

            resultBeforeSplit = text.getText().toString();
            resultAfterSplit = resultBeforeSplit.split(" ", 25);

            for(int i = 0; i < resultAfterSplit.length; i++){
                Log.println(Log.INFO, "result_"+ i, resultAfterSplit[i]);

            }



        }

        //CALL ALL MAJOR METHODS HERE (math(), conversion(), phone(), etc)
        math();
        music();

        super.onActivityResult(requestCode, resultCode, data);
    }

    //NOTE: be sure to check for "search the web for" type expressions before checking for anything else.

    /**
     * Checks what was said to see if(the user wants to complete a math problem, and completes it.
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

        //check to see if(there has been anything said
        if(resultAfterSplit != null){

            //loop looking for math terms
            for(int i = 0; i < resultAfterSplit.length; i++){

                //check to make sure i+1 isnt greater than or equal to the length and i-- is greater than 0
                if(i++ < resultAfterSplit.length && i-- > 0){

                    //search for "plus" "times" "divided by" "times"

                    //Addition
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("plus") || resultAfterSplit[i].trim().equalsIgnoreCase("+")){
                        //Checks to ensure that the things before and after the operand are numbers.
                        if(i-1 >= 0 && i+1 < resultAfterSplit.length &&
                                isNumeric(resultAfterSplit[i-1].trim()) &&
                                isNumeric(resultAfterSplit[i+1].trim())) {
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i - 1] + "+" + resultAfterSplit[i + 1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i-1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                resultAfterSplit[i-1].trim().equals("this") ||
                                        resultAfterSplit[i-1].trim().equals("that")){
                            //form the equation
                            equation = equation.concat(result + "+" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that")){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "+" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }



                    }

                    //subtraction
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("minus") || resultAfterSplit[i].trim().equalsIgnoreCase("-")){


                        //Using the last result as a parameter for i-1
                        if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                resultAfterSplit[i-1].trim().equals("this") ||
                                        resultAfterSplit[i-1].trim().equals("that")){
                            //form the equation
                            equation = equation.concat(result + "-" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that")){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "-" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Checks to ensure that the things before and after the operand are numbers.
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length &&
                                isNumeric(resultAfterSplit[i-1].trim()) && isNumeric(resultAfterSplit[i+1].trim())) {
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i - 1] + "-" + resultAfterSplit[i + 1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }



                    }

                    //multiplication
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("times") || resultAfterSplit[i].trim().equalsIgnoreCase("*")
                            || resultAfterSplit[i].trim().equalsIgnoreCase("multiplied") //multiplied by
                            || resultAfterSplit[i].trim().equalsIgnoreCase("x")){

                        //Checks to ensure that the things before and after the operand are numbers.
                        if(i-1 >= 0 && i+1 < resultAfterSplit.length &&
                                isNumeric(resultAfterSplit[i-1].trim()) &&
                                isNumeric(resultAfterSplit[i+1].trim())){
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i-1] + "*" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }
                        //Ensures that multiplied by will result in multiplication
                        else if(i-1 >= 0 && i+2 < resultAfterSplit.length && isNumeric(resultAfterSplit[i-1].trim()) &&
                                isNumeric(resultAfterSplit[i+2].trim())){
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i-1] + "*" +resultAfterSplit[i+2]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i-1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                resultAfterSplit[i-1].trim().equals("this") ||
                                        resultAfterSplit[i-1].trim().equals("that")){
                            //form the equation
                            equation = equation.concat(result + "*" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                (resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "*" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }




                    }

                    //division
                    if(resultAfterSplit[i].trim().equalsIgnoreCase("÷")
                            || resultAfterSplit[i].trim().equalsIgnoreCase("/")
                            || resultAfterSplit[i].trim().equalsIgnoreCase("divided") //divided by
                            || resultAfterSplit[i].trim().equalsIgnoreCase("over")
                            || resultAfterSplit[i].trim().equalsIgnoreCase("out")) //Out = out of
                    {

                        //Checks to ensure that the things before and after the operand are numbers.
                        if(i-1 >= 0 && i+1 < resultAfterSplit.length &&
                                isNumeric(resultAfterSplit[i-1].trim()) &&
                                isNumeric(resultAfterSplit[i+1].trim())){
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i-1] + "/" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }
                        //This checks to see if(it was 2 words that was said (EX: divided, by) and ensures that they are numbers on both sides
                        else if(i-1 >= 0 && i+2 < resultAfterSplit.length && isNumeric(resultAfterSplit[i-1].trim()) &&
                                isNumeric(resultAfterSplit[i+2].trim())){
                            //form the equation
                            equation = equation.concat(resultAfterSplit[i-1] + "/" +resultAfterSplit[i+2]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i-1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i-1]) &&
                                isNumeric(resultAfterSplit[i+1]) &&
                                resultAfterSplit[i-1].trim().equals("this") ||
                                resultAfterSplit[i-1].trim().equals("that")){
                            //form the equation
                            equation = equation.concat(result + "/" +resultAfterSplit[i+1]);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }

                        //Using the last result as a parameter for i+1
                        else if(i-1 >= 0 && i+1 < resultAfterSplit.length && result != null &&
                                !isNumeric(resultAfterSplit[i+1]) && isNumeric(resultAfterSplit[i-1]) &&
                                (resultAfterSplit[i+1].trim().equals("this") ||
                                        resultAfterSplit[i+1].trim().equals("that"))){
                            //form the equation
                            equation = equation.concat( resultAfterSplit[i-1]+ "/" + result);
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }




                    }
                    //to percent
                    if((resultAfterSplit[i].trim().equalsIgnoreCase("to") &&
                            resultAfterSplit[i+1].trim().equalsIgnoreCase("percent"))
                        || (resultAfterSplit[i].trim().equalsIgnoreCase("in") &&
                            resultAfterSplit[i+1].trim().equalsIgnoreCase("percent"))){

                        if(i-1 >= 0 && isNumeric(resultAfterSplit[i-1].trim())) {
                            equation = equation.concat(resultAfterSplit[i - 1] + "*100");
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }
                        else if(i-1 >= 0 && result != null && !isNumeric(resultAfterSplit[i-1]) &&
                                resultAfterSplit[i-1].trim().equalsIgnoreCase("this") ||
                                resultAfterSplit[i-1].trim().equalsIgnoreCase("that")){
                            equation = equation.concat(result + "*100");
                            Log.println(Log.INFO, "result", "Equation = " + equation);
                        }


                    }
                    //percent of

                    //squared

                    //cubed

                    //sqrt




                }
            }

            //if(the equation is not empty, evaluate it.
            if(!equation.trim().equalsIgnoreCase("")) {
                exp = new Expression(equation);
                result = exp.eval();

                Log.println(Log.INFO, "result", "" + result);
                TextView t = (TextView) findViewById(R.id.textView);
                t.append("\nThe result of " + equation + " is " + result);

            }

        }
    }

    public void pause(){
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    public void play(){
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    public void next(){
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    public void previous(){
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }

    public void beginning(){
        long eventtime = SystemClock.uptimeMillis();
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        sendOrderedBroadcast(upIntent, null);
    }



    public void music(){

        if(resultAfterSplit != null) {

            //loop looking for math terms
            for (int i = 0; i < resultAfterSplit.length; i++) {

                if(resultAfterSplit[i].equals("pause")){
                    if(manager.isMusicActive()) {

                        pause();
                    }
                }


                //shuffle
                if(i+3 < resultAfterSplit.length){
                    if((resultAfterSplit[i].equals("shuffle")) || (resultAfterSplit[i].equals("shuffle") && resultAfterSplit[i+1].equals("my") &&
                            resultAfterSplit[i+2].equals("music")) || (resultAfterSplit[i].equals("play") && resultAfterSplit[i+1].equals("my")) &&
                            resultAfterSplit[i+2].equals("music") && resultAfterSplit[i+3].equals("shuffled")){
                        if(manager.isMusicActive()) {
                            //simulate a shuffle by skipping 5 songs

                            next();
                            next();
                            next();
                            next();
                            next();


                        }
                        else {
                            //simulate a shuffle by skipping 5 songs

                            next();
                            next();
                            next();
                            next();
                            next();
                            play();
                        }
                    }
                }


                //play
                if(resultAfterSplit[i].equals("play")){
                    //check to see if(music is playing to begin with
                    if(manager.isMusicActive()) {

                        play();
                    }
                }

                //ADD RECOGNIZABLE WORDS!!!

                //previous
                if(resultAfterSplit[i].equals("previous")){
                    //check to see if(music is playing to begin with
                    if(manager.isMusicActive()) {

                        previous();
                    }
                }

                //next
                if(resultAfterSplit[i].equals("next")){
                    //check to see if(music is playing to begin with
                    if(manager.isMusicActive()) {

                        next();
                    }
                }


                //start over
                if(resultAfterSplit[i].equals("beginning")){
                    //check to see if(music is playing to begin with
                    if(manager.isMusicActive()) {

                        beginning();
                    }
                }


            }
        }

    }
    //isNumeric retrieved from http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java

    /**
     * This checks a string to see if(it is a number.
     * @param str The string to check
     * @return True if(the string is a string. False if(it is not.
     */
    public static boolean isNumeric(String str)
    {
        String str2 = replaceNumbers(str); //convert from word numbers to literal numbers
        return str2.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    //CONVERSIONS

    //temperature F to C C to F

    //PHONE FUNCTIONS

    //call, dial, (etc) contact



    //MAY THROW AN ERROR BECAUSE IT MAY NOT LIKE NON DIGIT NUMBERS
    //retrieved from http://stackoverflow.com/questions/4062022/how-to-convert-words-to-a-number
    public static String replaceNumbers (String input) {
        String result = "";
        String[] decimal = input.split(MAGNITUDES[3]);
        String[] millions = decimal[0].split(MAGNITUDES[2]);

        for (int i = 0; i < millions.length; i++) {
            String[] thousands = millions[i].split(MAGNITUDES[1]);

            for (int j = 0; j < thousands.length; j++) {
                int[] triplet = {0, 0, 0};
                StringTokenizer set = new StringTokenizer(thousands[j]);

                if((set.countTokens() == 1)) { //If there is only one token given in triplet
                    String uno = set.nextToken();
                    triplet[0] = 0;
                    for (int k = 0; k < DIGITS.length; k++) {
                        if((uno.equals(DIGITS[k]))) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                        }
                        if((uno.equals(TENS[k]))) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                        }
                    }
                }


                else if((set.countTokens() == 2)) {  //If there are two tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    if((dos.equals(MAGNITUDES[0]))) {  //If one of the two tokens is "hundred"
                        for (int k = 0; k < DIGITS.length; k++) {
                            if((uno.equals(DIGITS[k]))) {
                                triplet[0] = k + 1;
                                triplet[1] = 0;
                                triplet[2] = 0;
                            }
                        }
                    }
                    else {
                        triplet[0] = 0;
                        for (int k = 0; k < DIGITS.length; k++) {
                            if((uno.equals(TENS[k]))) {
                                triplet[1] = k + 1;
                            }
                            if((dos.equals(DIGITS[k]))) {
                                triplet[2] = k + 1;
                            }
                        }
                    }
                }

                else if((set.countTokens() == 3)) {  //If there are three tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if((uno.equals(DIGITS[k]))) {
                            triplet[0] = k + 1;
                        }
                        if((tres.equals(DIGITS[k]))) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                        }
                        if((tres.equals(TENS[k]))) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                        }
                    }
                }

                else if((set.countTokens() == 4)) {  //If there are four tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    String cuatro = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if((uno.equals(DIGITS[k]))) {
                            triplet[0] = k + 1;
                        }
                        if((cuatro.equals(DIGITS[k]))) {
                            triplet[2] = k + 1;
                        }
                        if((tres.equals(TENS[k]))) {
                            triplet[1] = k + 1;
                        }
                    }
                }
                else {
                    triplet[0] = 0;
                    triplet[1] = 0;
                    triplet[2] = 0;
                }

                result = result + Integer.toString(triplet[0]) + Integer.toString(triplet[1]) + Integer.toString(triplet[2]);
            }
        }

        if((decimal.length > 1)) {  //The number is a decimal
            StringTokenizer decimalDigits = new StringTokenizer(decimal[1]);
            result = result + ".";
            System.out.println(decimalDigits.countTokens() + " decimal digits");
            while (decimalDigits.hasMoreTokens()) {
                String w = decimalDigits.nextToken();
                System.out.println(w);

                if((w.equals(ZERO[0]) || w.equals(ZERO[1]))) {
                    result = result + "0";
                }
                for (int j = 0; j < DIGITS.length; j++) {
                    if((w.equals(DIGITS[j]))) {
                        result = result + Integer.toString(j + 1);
                    }
                }

            }
        }

        return result;
    }

}
