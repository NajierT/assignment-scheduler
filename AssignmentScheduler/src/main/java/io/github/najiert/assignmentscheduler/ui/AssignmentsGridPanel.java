package io.github.najiert.assignmentscheduler.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AssignmentsGridPanel implements ActionListener {
	
	private JPanel assignmentsGridPanel;
	private JPanel assignmentsGrid;
	private ArrayList<AssignmentPanel> assignmentPanels;
	private JButton addAssignment;
	private JButton removeAssignment;
    private int MAX_ASSIGNMENTS = 20;
	
    public AssignmentsGridPanel() {
    	createAssignmentsGridPanel();
    }
    
    /**
	 * @return the assignmentsGridPanel
	 */
	public JPanel getPanel() {
		return assignmentsGridPanel;
	}

	/**
	 * @return the assignmentPanels
	 */
	public ArrayList<AssignmentPanel> getAssignmentPanels() {
		return assignmentPanels;
	}

	private void createAssignmentsGridPanel() {
        assignmentsGridPanel = new JPanel();
        assignmentsGridPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        
        // create label
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.05;
        
        JLabel sectionLabel = new JLabel("Add assignments:");
        assignmentsGridPanel.add(sectionLabel, gbc);
        
        // Add buttons
        //Create add / minus buttons
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = .05;
        gbc.weighty = 0.05;
        
        addAssignment = new JButton("+");
        addAssignment.setFocusable(false);
        addAssignment.addActionListener(this);
        assignmentsGridPanel.add(addAssignment, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = .05;
        gbc.weighty = 0.05;
        
        removeAssignment = new JButton("-");
        removeAssignment.setFocusable(false);
        removeAssignment.addActionListener(this);
        assignmentsGridPanel.add(removeAssignment, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 0.95;
        
        createAssignmentsGrid();
        JScrollPane assignments = new JScrollPane(assignmentsGrid);
        assignmentsGridPanel.add(assignments, gbc);
        
        assignmentsGridPanel.setBorder(BorderFactory.createEtchedBorder());
    }
	
	private void createAssignmentsGrid() {
    	assignmentsGrid = new JPanel();
    	assignmentsGrid.setLayout(new GridLayout(MAX_ASSIGNMENTS, 1, 0, 5));
    	assignmentPanels = new ArrayList<AssignmentPanel>();
    	assignmentPanels.add(new AssignmentPanel());
    	
    	assignmentsGrid.add(assignmentPanels.get(0).getPanel());
    	
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addAssignment) {
			if (assignmentPanels.size() < MAX_ASSIGNMENTS) {
				handleAdd();
			}
			else {
				JOptionPane.showMessageDialog(assignmentsGridPanel, "Maximum number of assignments!");
			}
		}
		if (e.getSource() == removeAssignment) {
			if (assignmentPanels.size() > 1) {
				assignmentPanels.remove(assignmentPanels.size() - 1);
				assignmentsGrid.remove(assignmentPanels.size());
				assignmentsGrid.validate();
				assignmentsGrid.repaint();
			}
			else {
				JOptionPane.showMessageDialog(assignmentsGridPanel, "Must have at least one assignment");
			}
		}
	}
	
	public boolean isFilled() {
		for (int i = 0; i < assignmentPanels.size(); i++) {
			if (!assignmentPanels.get(i).isFilled()) {
				return false;
			}
		}
		return true;
	}
	private void handleAdd() {
		if (this.isFilled()) {
			assignmentPanels.add(new AssignmentPanel());
			JPanel newPanel = assignmentPanels.get(assignmentPanels.size() - 1).getPanel();
			assignmentsGrid.add(newPanel);
			assignmentsGrid.validate();
			assignmentsGrid.repaint();
		}
		else {
			JOptionPane.showMessageDialog(assignmentsGridPanel, "Must fill in all assignment fields"
															  + " before adding a new assignment");
		}
	}
}
