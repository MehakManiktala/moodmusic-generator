package pipeline;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import javax.swing.JButton;
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
	private JTextArea textArea = new JTextArea(10, 32);;
	private JTextArea currentMood = new JTextArea(1,10);
	private JCheckBox showMoodCB;
	private JButton clear;
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
		Angry(1.3, new Color(209, 148, 148)),//dark red
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
		
		public MoodSentence(String st, Mood mood, int start, int end, int moodStart, int moodEnd) {

			this.sentence = st;
			this.mood = mood;
			this.startIndex = start;
			this.moodStartIndex = moodStart;
			this.moodEndIndex = moodEnd;
			this.endIndex = end;
		}

		public String sentence = "";
		public Mood mood = Mood.Calm;
		public int startIndex = -1;
		public int endIndex = -1;
		public int moodStartIndex =-1;
		public int moodEndIndex =-1;
	}
	
	private void displaySentences() {
		this.textArea.setText("");
		for (MoodSentence s : this.sentenceBuffer) {
			if (this.showMoodCB.isSelected()) {
				this.textArea.append("["+s.mood.name()+"] ");
			}
			this.textArea.append(s.sentence);
		}
		
	}

	private void initComponents(String sentenceDir) {


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
					WatchKey key = dir.register(watcher, ENTRY_CREATE);//, ENTRY_MODIFY);
					System.out.println("Watching directory "+dir.toAbsolutePath().toString());
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
								
								int previous_index = 0;//keep track of where sentences start and stop in the collective text
								int previous_moodIndex = 0;
								player.sentenceBuffer = new LinkedList<MoodSentence>();
								
								while ((st = br.readLine()) != null) {
									if (st.equals(TERMINATION)) {
										player.terminationSwitch = true;
										break;
									}
									System.out.println(st);
									String[] split = st.split("#");
									String sentence = split[0];
									String moodString = split[1];
									System.out.println(sentence);
									System.out.println(moodString);
									Mood mood = Mood.Quiescent;
									try {
									mood = Mood.valueOf(moodString);
									}catch(Exception ex) {
										ex.printStackTrace();
									}
									System.out.println(mood.color);
									String moodPiece = "["+mood.name()+"] ";
									player.sentenceBuffer.add(new MoodSentence(sentence, mood, previous_index, previous_index+sentence.length()-1, previous_moodIndex, previous_moodIndex + sentence.length()+ moodPiece.length() ));
									previous_index += sentence.length();
									previous_moodIndex += sentence.length() + moodPiece.length();

									player.textArea.append(sentence);


								}
								player.displaySentences();


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
				JPanel controlPanel = new JPanel(new GridBagLayout());
				controlPanel.setLayout(new GridBagLayout());
				mainFrame.add(controlPanel);
				player.textArea.setEditable(false);
				player.textArea.setFont(new Font("Serif", Font.ITALIC, 32));
				player.textArea.setLineWrap(true);
				player.textArea.setWrapStyleWord(true);
				{
				Font font = player.textArea.getFont();
				float size = font.getSize() + 20.0f;
				player.textArea.setFont( font.deriveFont(size) );
				}
				JScrollPane scrollPane = new JScrollPane(player.textArea); 
				player.showMoodCB = new JCheckBox("Show Mood");
				
				GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 0;
                c.gridy = 1;
                c.gridwidth = 3;
				controlPanel.add(scrollPane, c);
				
				c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 0;
                c.gridy = 0;
				controlPanel.add(player.showMoodCB, c);
				
				c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 2;
                c.gridy = 0;
				player.currentMood.setFont(new Font("Serif", Font.BOLD, 32));
				player.currentMood.setEditable(false);
				{
				Font font = player.currentMood.getFont();
				float size = font.getSize() + 20.0f;
				player.currentMood.setFont( font.deriveFont(size) );
				}
				controlPanel.add(player.currentMood,c);
				
				player.showMoodCB.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                System.out.println("Checked:" + player.showMoodCB.isSelected());
		                player.displaySentences();
		            }
		        });
				player.clear = new JButton("Clear");
				player.clear.setEnabled(false);
				c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 1;
                c.gridy = 0;
				controlPanel.add(player.clear, c);
				player.clear.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		            	player.clear();
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
				List<MoodSentence> sentenceBuffer = player.sentenceBuffer;
				
				//default starting mood
				player.musicGen.Emotion.setPleasantness();
				
				while (player.terminationSwitch==false) {
					
					//if set of sentences currently play has been replaced, switch to playing from that new set
					if (sentenceBuffer!=player.sentenceBuffer) {
						next_index = 0;
						sentenceBuffer = player.sentenceBuffer;
					}
					
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
						if (player.showMoodCB.isSelected()) {
							player.currentMood.setText(next.mood.name());
						}
						else {
							player.currentMood.setText("");
						}
						player.displaySentences();
						player.highlighter.removeAllHighlights();
						if (player.showMoodCB.isSelected()) {
							HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(next.mood.color);
							player.highlighter.addHighlight(next.moodStartIndex, next.moodEndIndex, p );
						}
						else {
							HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(null);
							player.highlighter.addHighlight(next.startIndex, next.endIndex, p );
						}
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
					case Angry:
						emotion.setAngryMusic();
					default:
						System.out.println("Unrecognized mood "+next.mood.name());
						break;
					}
				}
				

				
				return null;
			}
		}.execute();



	}
	protected void clear() {
		this.sentenceBuffer.clear();
		this.displaySentences();
		
	}

	static int determineDuration(MoodSentence sentence){
		
		int duration = sentence.sentence.length();
		duration*= sentence.mood.multiplier;
		return duration;
	}
}
