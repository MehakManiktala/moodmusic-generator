/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicgenerator;

import java.util.Random;

import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Score;
import jm.music.tools.Mod;
/**
 *
 * @author user
 */
public class MusicGenerator {

    
    static double[][][] map = {MidiHandler.Maj1,MidiHandler.Min2,MidiHandler.Min3,MidiHandler.Maj4,MidiHandler.Maj5,MidiHandler.Min6,MidiHandler.Dim7};
     
    EmotionHandler Emotion = new EmotionHandler();
    Score scr = new Score();
     
    static double[] ValMap = {JMC.QUAVER, JMC.CROTCHET, JMC.MINIM ,JMC.SEMIBREVE};
    static int[] OctMap = {0,12};

	private Random randnum;

	private MarkovChain noteMC;

	private MarkovChain octMC;

	private MarkovChain valMC;

	private MarkovChain chordMC;

	private MidiHandler melody;

	private MidiHandler chordRoot;

	private MidiHandler chordMid;

	private MidiHandler chordDom;
    
	public boolean populated = false;
	
    
    public void Initialize() {
    	
    	populated = false;
    	
        randnum = new Random();
        noteMC = new MarkovChain();
        octMC = new MarkovChain();
        valMC = new MarkovChain();
        chordMC = new MarkovChain();
        
        
        noteMC.states = 7;
        noteMC.curState = 1;
        
        octMC.states = 2;
        octMC.curState = 1;
       
        valMC.states = 4;
        valMC.curState = 1;
        
        chordMC.states = 7;
        chordMC.curState = 1;
        
        melody = new MidiHandler();
        chordRoot = new MidiHandler();
        chordMid = new MidiHandler();
        chordDom = new MidiHandler();
    }
    
    public void SetTransitions() {
        noteMC.transition = map[randnum.nextInt(6)];
        octMC.transition = Emotion.OctMC;
        valMC.transition = Emotion.ValueMC;
        chordMC.transition = Emotion.ChordMC;
    }
    
    public void Generate() {
        
    	scr.setTitle(randnum.nextLong()+"");
        //initialize MIDI
        //ask for mood input
        //read mood input
        //initialize Markov Chains according to mood
        //fill constraints
        //feed values of MC + Constraint to MIDI
        //Play
        
        

        
        int crCounter = 0;
        int cmCounter = 0;
        int cdCounter = 0;
        int mCounter = 0;
        
            for(int i=0;i<4;i++){
                if (i % 8 == 0){
                    chordMC.nextState();
                //System.out.println(map[ChordMC.curState - 1]);
                noteMC.transition = map[chordMC.curState - 1];
                System.out.println("Chord State = " + chordMC.curState);
                octMC.nextState();
                }
                //initialize chord notes
                Note cRoot = new Note();
                Note cMid = new Note();
                Note cDom = new Note();
                //setting the note pitch for each chord note
                if(crCounter == 0){
                    cRoot.setPitch(Emotion.emoMap[chordMC.curState - 1] - 12);
                    System.out.println("cRoot pitch: " + cRoot.getPitch());
                    valMC.nextState();
                    cRoot.setLength(ValMap[valMC.curState - 1]);
                    switch (valMC.curState) {
                        case 1 : crCounter = 1;
                                 break;
                        case 2 : crCounter = 2;
                                 break;
                        case 3 : crCounter = 4;
                                 break;
                        case 4 : crCounter = 8;
                                 break;
                    }
                    //System.out.println(cRoot.getPitch());
                    chordRoot.phrase.addNote(cRoot);
                }
                if (cmCounter == 0){
                    if(chordMC.curState == 1 || chordMC.curState == 4 || chordMC.curState == 5){                
                        cMid.setPitch(Emotion.emoMap[chordMC.curState - 1] - 8);
                        System.out.println("cMid pitch: " + cMid.getPitch());
                        valMC.nextState();
                        cMid.setLength(ValMap[valMC.curState - 1]);
                        switch (valMC.curState) {
                            case 1 : cmCounter = 1;
                                     break;
                            case 2 : cmCounter = 2;
                                     break;
                            case 3 : cmCounter = 4;
                                     break;
                            case 4 : cmCounter = 8;
                                     break;
                        }
                    }
                    else if(chordMC.curState == 2 || chordMC.curState == 3 || chordMC.curState == 6 || chordMC.curState == 7){                
                        cMid.setPitch(Emotion.emoMap[chordMC.curState - 1] - 9);
                        System.out.println("cMid pitch: " + cMid.getPitch());
                        valMC.nextState();
                        cMid.setLength(ValMap[valMC.curState - 1]);
                        switch (valMC.curState) {
                            case 1 : cmCounter = 1;
                                     break;
                            case 2 : cmCounter = 2;
                                     break;
                            case 3 : cmCounter = 4;
                                     break;
                            case 4 : cmCounter = 8;
                                     break;
                        }
                    }
                   chordMid.phrase.addNote(cMid);
                }
                if (cdCounter == 0){
                    if(chordMC.curState == 7){
                        cDom.setPitch(Emotion.emoMap[chordMC.curState - 1] - 6);
                        System.out.println("cDom pitch: " + cDom.getPitch());
                        valMC.nextState();
                        cDom.setLength(ValMap[valMC.curState - 1]);
                        switch (valMC.curState) {
                            case 1 : cdCounter = 1;
                                     break;
                            case 2 : cdCounter = 2;
                                     break;
                            case 3 : cdCounter = 4;
                                     break;
                            case 4 : cdCounter = 8;
                                     break;
                        }
                        
                    }
                    else{
                        cDom.setPitch(Emotion.emoMap[chordMC.curState - 1] - 5);
                        System.out.println("cDom pitch: " + cDom.getPitch());
                        valMC.nextState();
                        cDom.setLength(ValMap[valMC.curState - 1]);
                        switch (valMC.curState) {
                            case 1 : cdCounter = 1;
                                     break;
                            case 2 : cdCounter = 2;
                                     break;
                            case 3 : cdCounter = 4;
                                     break;
                            case 4 : cdCounter = 8;
                                     break;
                        }
                    }
                    chordDom.phrase.addNote(cDom);
                }
                //putting the chord notes in a note array
                //Note[] noteArray = {cRoot, cMid, cDom};
                //using cphrase to contain the chord
                //CPhrase ChordN = new CPhrase();
                //ChordN.addChord(noteArray);
                //adding the chord to the part
               // Chord.part.addCPhrase(ChordN);
               if (mCounter == 0){
                    noteMC.nextState();
                    //System.out.println("Note state = " + NoteMC.curState);
                    valMC.nextState();
                    Note n = new Note();
                   
                    n.setPitch(Emotion.emoMap[noteMC.curState - 1] + OctMap[octMC.curState - 1]);
                    System.out.println("melod pitch: " + n.getPitch());
                    n.setLength(ValMap[valMC.curState - 1]);
                    if (((cRoot.getPitch() == (n.getPitch()) + 2) || (cRoot.getPitch() == (n.getPitch()) - 2))){
                        n.setPitch(n.getPitch() + 2);
                        System.out.println("dissonance detected");
                    }
                    if (((cMid.getPitch() == (n.getPitch()) + 2) || (cMid.getPitch() == (n.getPitch()) - 2))){
                        n.setPitch(n.getPitch() + 2);
                        System.out.println("dissonance detected");
                    }
                    if (((cDom.getPitch() == (n.getPitch()) + 2) || (cDom.getPitch() == (n.getPitch()) - 2))){
                        n.setPitch(n.getPitch() + 2);
                        System.out.println("dissonance detected");
                    }
                    switch (valMC.curState) {
                        case 1 : mCounter = 1;
                                 break;
                        case 2 : mCounter = 2;
                                 break;
                        case 3 : mCounter = 4;
                                 break;
                        case 4 : mCounter = 8;
                                 break;
                    }
                    //System.out.println(n.getPitch());
                    melody.phrase.addNote(n);
                    }
                crCounter--;
                cmCounter--;
                cdCounter--;
                mCounter--;
            }
            
            if (!populated) {
	            melody.part.addPhrase(melody.phrase);
	            chordRoot.part.addPhrase(chordRoot.phrase);
	            chordMid.part.addPhrase(chordMid.phrase);
	            chordDom.part.addPhrase (chordDom.phrase);
	            scr.addPart(melody.part);
	            scr.addPart(chordRoot.part);
	            scr.addPart(chordMid.part);
	            scr.addPart(chordDom.part);
	            scr.setTempo(Emotion.emoTempo);
	            populated = true;
            }
            int transposer = randnum.nextInt(12) + 1;
            Mod.transpose(scr, transposer);
            
       }
}
