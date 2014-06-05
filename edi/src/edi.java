import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import java.awt.ScrollPane;


public class edi {

	JFrame frame;
	private JLabel jlabel;
	private JPanel jp1 = new JPanel();
	private JPanel jp2 = new JPanel();
	private JFileChooser fileChooser = new JFileChooser();
	private JPanel jp3 = new JPanel();
	private JTextArea textArea = new JTextArea();
	private JButton btnNewButton_1 = new JButton("Lancer la vérification");
	private String result = "<html>";
	private File selectedFile;
	private ScrollPane scrollPane = new ScrollPane();

	/** 
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					edi window = new edi();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public edi() {
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 474);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(jp1, BorderLayout.CENTER);
        jp1.setLayout(null);
        jp2.setLayout(null);
        
        
        textArea.setBounds(new Rectangle(10, 51, 414, 200));
        jp1.add(textArea);
        
        JButton btnNewButton = new JButton("Ouvrir un fichier");

        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
                try {
                    // Open an input stream
                	fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                	int result = fileChooser.showOpenDialog(jp3);
                	if (result == JFileChooser.APPROVE_OPTION) {
                	    selectedFile = fileChooser.getSelectedFile();
                	    String text = new Scanner(selectedFile).useDelimiter("\\A").next();
                	    System.out.println(selectedFile.getAbsolutePath());
                	    textArea.setText(text);
                	}
                	
                	
                }catch (Exception e1) {
        			e1.printStackTrace();
        		}
        	}
        });
        btnNewButton.setBounds(247, 11, 177, 33);
        jp1.add(btnNewButton);
        btnNewButton_1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
        		System.out.println("Vérification en cours");
        		InputStreamReader inputReader = new InputStreamReader(System.in);
        		OutputStreamWriter outputWriter = new OutputStreamWriter(System.out);
				EDItoXML edixml = new EDItoXML(inputReader, outputWriter);
        		String[] s = {selectedFile.getAbsolutePath()};
        		try {
					edixml.main(s);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					result = "<html><span bgcolor='red'>"+e.getMessage()+"</span>";
			        JLabel lblNewLabe2 = new JLabel(result);        
			        scrollPane.add(lblNewLabe2);
				}
        		
        	}
        });
        btnNewButton_1.setBounds(10, 11, 167, 33);
        
        jp1.add(btnNewButton_1);
        
        scrollPane.setBounds(10, 257, 414, 169);
        jp1.add(scrollPane);
        
        JLabel lblNewLabe2 = new JLabel(result);        
        scrollPane.add(lblNewLabe2);
        
        
        
        
        
        
        
        
        
        
        
        
        
        /*
        jtp.addChangeListener(new ChangeListener() { //add the Listener

            public void stateChanged(ChangeEvent e) {

                System.out.println(""+jtp.getSelectedIndex());

                if(jtp.getSelectedIndex()==1) //Index starts at 0, so Index 2 = Tab3
                {
            		

                }
            }
        });
  */
        
	}
}
