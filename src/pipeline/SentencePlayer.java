package pipeline;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import jm.util.Play;
import musicgenerator.EmotionHandler;
import musicgenerator.MusicGenerator;

@SuppressWarnings("serial")
public class SentencePlayer extends javax.swing.JFrame  {

	private static final String TERMINATION = "$T3RMN@+N$";

	private static WatchService watcher;

	
	MusicGenerator musicGen = new MusicGenerator();

	//UI Components
	private JTextArea textArea;
	private JCheckBox showMoodCB;
	private Highlighter highlighter;
	HighlightPainter painter;
	private List<MoodSentence> sentenceBuffer = new LinkedList<MoodSentence>();
	private boolean terminationSwitch = false;

	public static enum Mood{
		Sad(1.5, new Color(220, 231, 242)),//light grey
		Happy(.7, new Color(255, 204, 233)),//pink
		Surprised(.4, new Color(255, 255, 35)),//yellow
		Drowsy(2, new Color(219, 219, 219)),//gray
		Quiescent(1.2, new Color(171, 210, 216)),//blue
		Active(.5, new Color(200,200,150)),//orange
		Calm(1.3, new Color(200,255,150)),//green
		Angry(1.3, new Color(165, 70, 58)),//dark red
		Fear(0.6, new Color(194, 179, 214));//purple
		
	   double multiplier;
	   Color color;
	   Mood(double m, Color color) {
	      multiplier = m;
	      this.color = color;
	   }
	}
	private static class MoodSentence{

		@SuppressWarnings("unused")
		public MoodSentence(String st, String mood, int start, int end) {

			this.sentence = st;

			mood = mood.toLowerCase();
			for (Mood m : Mood.values()) {
				if (mood.equals(m.name().toLowerCase())) {
					this.mood = m;
					break;
				}
			}
		}
		
		public MoodSentence(String st, Mood mood, int start, int end) {

			this.sentence = st;
			this.mood = mood;
			this.startIndex = start;
			this.endIndex = end;
		}

		public String sentence = "";
		public Mood mood = Mood.Calm;
		public int startIndex = -1;
		public int endIndex = -1;
	}

	private void initComponents(String sentenceDir) {

		textArea = new JTextArea(20, 64);

		String text = "Waiting for sentences in "+sentenceDir;

		textArea.setText(text);

		highlighter = textArea.getHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

	}


	void SetEmotion(String mood) {
		//TODO
	}

	public static void main(String[]args) {
		System.out.println("Running");

		SentencePlayer player = new SentencePlayer();
		String path = null;
		if (args.length > 0) {
			if (args[0]!=null)
				path = args[0];
		}
		final String fpath = path!=null? path: System.getProperty("user.dir")+"\\inputsentences";
		
		player.initComponents(fpath);

		
		//File-change-watcher thread
		new SwingWorker<Void, String>(){
			@Override
			protected Void doInBackground() throws Exception {
				System.out.println("Attempting to watch directory "+fpath);
				boolean has_found_files = false;
				try {
					watcher = FileSystems.getDefault().newWatchService();
					Path dir = Paths.get(fpath);
					WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);//, ENTRY_MODIFY);
					System.out.println("Watching directory "+dir.toAbsolutePath().toString());
					int previous_index = 0;//keep track of where sentences start and stop in the collective text
					//put poller in new thread called mood-change reader
					while(player.terminationSwitch==false){

						for (WatchEvent<?> event: key.pollEvents()) {
							WatchEvent.Kind<?> kind = event.kind();
							if (kind == OVERFLOW) {
								continue;
							}
							@SuppressWarnings("unchecked")
							WatchEvent<Path> ev = (WatchEvent<Path>)event;
							Path filename = ev.context();
							String pathString = fpath+File.separator+filename;
							File file = new File(pathString);

							BufferedReader br;
							try {
								br = new BufferedReader(new FileReader(file));
								String st; 
								
								//remove "Watching dir" text from textarea
								if (!has_found_files) {
									has_found_files = true;
									player.textArea.setText("");
								}
								
								while ((st = br.readLine()) != null) {
									if (st.equals(TERMINATION)) {
										player.terminationSwitch = true;
										break;
									}
									String[] split = st.split("#");
									String sentence = split[0];
									String moodString = split[1];
									Mood mood = Mood.valueOf(moodString);
									player.sentenceBuffer.add(new MoodSentence(sentence, mood, previous_index, previous_index+sentence.length()-1));
									previous_index += sentence.length();

									player.textArea.append(sentence);


								}


							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 

						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
		
		//UI thread
		new SwingWorker<Void, String>(){
			@Override
			protected Void doInBackground() throws Exception {
				JFrame mainFrame = new JFrame("Java SWING Examples");
				mainFrame.setSize(800,640);
				JPanel controlPanel= new JPanel();
				mainFrame.add(controlPanel);
				player.textArea.setEditable(false);
				player.textArea.setFont(new Font("Serif", Font.ITALIC, 16));
				player.textArea.setLineWrap(true);
				player.textArea.setWrapStyleWord(true);
				Font font = player.textArea.getFont();
				float size = font.getSize() + 20.0f;
				player.textArea.setFont( font.deriveFont(size) );
				JScrollPane scrollPane = new JScrollPane(player.textArea); 
				player.showMoodCB = new JCheckBox("Show Mood");
				controlPanel.add(scrollPane);
				controlPanel.add(player.showMoodCB);
				player.showMoodCB.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                System.out.println("Checked:" + player.showMoodCB.isSelected());
		            }
		        });
				mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
				mainFrame.setUndecorated(true);
				mainFrame.setVisible(true);
				return null;
			}
		}.execute();
		
		//Music player thread
		int pace_duration = 5;
		new SwingWorker<Void, String>(){
			@Override
			protected Void doInBackground() throws Exception {
				int next_index = 0;
				
				//default starting mood
				player.musicGen.Emotion.setPleasantness();
				
				while (player.terminationSwitch==false) {
					
	            	player.musicGen.scr.empty();
	            	player.musicGen.Generate(24);
	                Play.midi(player.musicGen.scr);
	            	while(Play.cycleIsPlaying()) {
				        Play.waitCycle(player.musicGen.scr,
				                0);
	            	}
	                
					while (next_index >= player.sentenceBuffer.size()) {
						Thread.sleep(pace_duration);
					}
					MoodSentence next = player.sentenceBuffer.get(next_index);
					System.out.print(next.sentence);
					System.out.print(next.mood.name());
					System.out.println(next.startIndex +", "+next.endIndex);
					try {
						player.highlighter.removeAllHighlights();
						HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Mood.Sad.color);
						player.highlighter.addHighlight(next.startIndex, next.endIndex, p );
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					next_index++;
					EmotionHandler emotion = player.musicGen.Emotion;
					switch(next.mood) {
					case Happy:
						emotion.setPleasantness();
						break;
					case Calm:
						emotion.setLowNegativeAffect();
						break;
					case Active:
						emotion.setHighPositiveAffect();
						break;
					case Quiescent:
						emotion.setDisengagement();
						break;
					case Surprised:
						emotion.setStrongEngagement();
						break;
					case Drowsy:
				        emotion.setLowPositiveAffect();
				        break;
					case Sad:
						emotion.setUnpleasantness();
						break;
					case Fear:
				        emotion.setHighNegativeAffect();
				        break;
					default:
						System.out.println("Unrecognized mood "+next.mood.name());
						break;
					}
				}
				

				
				return null;
			}
		}.execute();



	}
	static int determineDuration(MoodSentence sentence){
		
		int duration = sentence.sentence.length();
		duration*= sentence.mood.multiplier;
		return duration;
	}
}
