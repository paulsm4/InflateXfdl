package com.rgb;

import java.io.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;
import org.w3c.dom.*; // XML DOM
import javax.xml.parsers.*; // DocumentBuilder etc
import javax.xml.transform.*;  // Document manipulation
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FrmMain {

	private JFrame frmInflateIbmForm;
	private JTextField edtXfdlFile;
	private JEditorPane editorPane = null;
	private JButton btnWriteXml = null;
	private Document document = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrmMain window = new FrmMain();
					window.frmInflateIbmForm.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FrmMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmInflateIbmForm = new JFrame();
		frmInflateIbmForm.setTitle("Inflate IBM Form");
		frmInflateIbmForm.setBounds(100, 100, 800, 600);
		frmInflateIbmForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmInflateIbmForm.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setAlignmentX (Component.LEFT_ALIGNMENT);
		panel.setAlignmentY (0.0f);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		frmInflateIbmForm.getContentPane().add(panel, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("XFDL File:");
		panel.add(lblNewLabel);
		
		JPanel panel3 = new JPanel ();
		panel.add(panel3);
		panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
		
		edtXfdlFile = new JTextField();
		panel3.add(edtXfdlFile);
		edtXfdlFile.setColumns(30);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectXfdlFile ();
			}
		});
		panel3.add(btnBrowse);
		
		JPanel panel4 = new JPanel();
		panel.add(panel4);
		
		JButton btnInflate = new JButton("Inflate");
		panel4.add(btnInflate);
		btnInflate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				inflateFile (edtXfdlFile.getText());
			}
		});
		
		btnWriteXml = new JButton("Write XML");
		btnWriteXml.setEnabled(false);
		btnWriteXml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeXmlFile (edtXfdlFile.getText());
			}
		});
		panel4.add(btnWriteXml);
		
		editorPane = new JEditorPane ();
		editorPane.setFont(new Font("Courier New", Font.PLAIN, 11));
		editorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.setViewportBorder(new TitledBorder(null, "XFDL File:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setMinimumSize (new Dimension(500, 800));
		frmInflateIbmForm.getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	private void inflateFile (String fname) {
		try {
			// Open specified Xfdl file
			XfdlFile xfdlFile = new XfdlFile (fname);
			InputStream is = xfdlFile.open ();

			DocumentBuilder db = 
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = db.parse (is);
			Transformer transformer = 
				TransformerFactory.newInstance().newTransformer ();
			DOMSource source = new DOMSource (document);

			// Display the XML to editor pane
			StreamResult result = new StreamResult (new StringWriter());
			transformer.transform(source, result);
			editorPane.setText(result.getWriter().toString());

			// Close file
			xfdlFile.close ();			

			// Update the rest of the UI
			editorPane.setCaretPosition(0);
			btnWriteXml.setEnabled (true);

		}
		catch (Exception e) {
			showMessage ("Read error: " + e.getMessage (), 0);
		}
	}
	

	private String mkXmlName (String xfdlName) {
	  String s = null;
	  int i = xfdlName.lastIndexOf('.');
	  if (i > 0)
	  	s = xfdlName.substring(0, i) + ".xml";
	  else
		s = xfdlName + ".xml";
	  return s;
	}
	
	private void selectXfdlFile () {
		JFileChooser fc = new JFileChooser ();
		fc.setFileFilter (new XfdlFilter());
		try {
			java.io.File file = new File (edtXfdlFile.getText ());
			if (file.exists ())
			    fc.setSelectedFile (file);
		}
		catch (Exception e)
		{
			; // No-op: just use default folder instead of edtXfdlFile text
		}

		String sPath = null;
	    try {
			// Select folder
	    	fc.setDialogTitle ("Select new IBM Form file");
	    	fc.setMultiSelectionEnabled (false); 
	    	int iret = fc.showOpenDialog (null);
	    	if (iret != JFileChooser.APPROVE_OPTION)
	    		return;
	    	// Update UI
	    	sPath = fc.getSelectedFile ().getCanonicalPath();
	    	edtXfdlFile.setText(sPath);
	    	// Erase previous XML 
	    	editorPane.setText("");
	    }
	   	catch (IOException e) {
	   		return; // No-op: simply don't update UI
	    }
	   	finally {
	   		fc = null;
	   	}
	}		

	private void showMessage (String msg, int messageType) throws java.awt.HeadlessException {
		switch (messageType) {
		case JOptionPane.INFORMATION_MESSAGE :
			JOptionPane.showMessageDialog (frmInflateIbmForm, msg,	"Information", messageType);
			break;
		case JOptionPane.WARNING_MESSAGE :
			JOptionPane.showMessageDialog (frmInflateIbmForm, msg,	"Warning", messageType);
			break;
		default :
			JOptionPane.showMessageDialog (frmInflateIbmForm, msg,	"Error", JOptionPane.ERROR_MESSAGE);
			break;
		}
	}
	
	private void writeXmlFile (String fname) {
		FileOutputStream os = null;
		String xmlFile = mkXmlName (fname);
		try {
			os = new FileOutputStream (xmlFile);
			Transformer transformer = 
					TransformerFactory.newInstance().newTransformer ();
			DOMSource source = new DOMSource (document);
			StreamResult result = new StreamResult (os);
			transformer.transform (source, result);
			os.close ();
			showMessage ("Wrote file " + xmlFile, 1);
		}
		catch (Exception e) {
			showMessage ("XML write error: " + e.getMessage (), 0);
		}
	}
	
}

class XfdlFilter extends FileFilter {

	public boolean accept (File f) {
		return (
			f.isDirectory() || 
			f.getName().toLowerCase().endsWith(".xfdd") ||
			f.getName().toLowerCase().endsWith(".xfdl"));
	}
	
	public String getDescription() {
		return "Xfdl Files (*.xfdd;*.xfdl)";
	}
}
