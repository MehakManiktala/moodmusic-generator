/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicgenerator;
import java.lang.Math;
import java.util.*;
/**
 *
 * @author user
 */
public class MarkovChain {
    
    double[][] transition;
    int states;
    int curState;
    boolean done = false;
    
    public void nextState(){
        System.out.println(curState);
        double r = Math.random();
        double sum = 0.0;
        for(int j = 0; j < states ; j++){
            sum+= transition[curState][j];
            if (sum >= r){
                curState = j + 1;
                break;
            }    
        }
        System.out.println(curState);
    }
    

}
