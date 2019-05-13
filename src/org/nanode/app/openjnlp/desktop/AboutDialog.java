/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is openjnlp.nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.app.openjnlp.desktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.nanode.app.OpenJNLP;


public class AboutDialog extends JDialog {
    static final ImageIcon aboutImage = new ImageIcon(AboutDialog.class.getResource("images/about.jpg"));


    public AboutDialog() {
        super();
        setModal(true);
        setResizable(false);

        JPanel content = (JPanel) getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        Font labelFont = new Font("SansSerif", Font.PLAIN, 12);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        content.add(new JLabel(aboutImage), gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 0, 10);
        JLabel jl = new JLabel("http://openjnlp.nanode.org/");
        jl.setForeground(Color.black);
        jl.setFont(labelFont);
        content.add(jl, gbc);
        
        content.add(Box.createVerticalStrut(15), gbc);

        jl = new JLabel("OpenJNLP " + OpenJNLP.getVersion() + " \u00a9 2001-2002, Nanode LLC.  All rights reserved.");
        jl.setForeground(Color.black);
        jl.setFont(labelFont);
        content.add(jl, gbc);
        
        content.add(Box.createVerticalStrut(15), gbc);

        JPanel glass = (JPanel) getGlassPane();
//        glass.setLayout(new BorderLayout());
        glass.setVisible(true);
        glass.addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent e) { setVisible(false); }
                                 });

        pack();
    }
    
    public void show() {
        // make sure it's in the right location on the screen
        Dimension d0 = getToolkit().getScreenSize();
        Dimension d1 = getSize();
        
        setLocation((d0.width - d1.width) / 2, (d0.height - d1.height) / 4);
        super.show();
    }
}
