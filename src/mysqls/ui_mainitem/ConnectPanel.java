package mysqls.ui_mainitem;
/**
 * 实体关系图和sql生产的实现
 */

import mysqls.contanst.ConnectINFO;
import mysqls.contanst.UIconstant;
import mysqls.sql.util.MyIOutil;
import mysqls.ui_frame.MainleftPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author 长宏 链接服务器的主mune。
 *
 */
public class ConnectPanel {
    private static JPanel panel = null;

    public static JPanel getui() {
        if (panel != null) {
            return panel;
        }


        if (panel == null) {
            // TODO Auto-generated method stub
            panel = new JPanel();
            JLabel jLabel1 = new JLabel("选择数据库连接:");
            JRadioButton mysql = new JRadioButton("mysql");
            JRadioButton oracle = new JRadioButton("oracle");
            JRadioButton sqlsever = new JRadioButton("SQL Sever");
            ButtonGroup dbsevergroup = new ButtonGroup();
            JLabel uname = new JLabel("用户名");
            JLabel pwd = new JLabel("密    码");
            JLabel ipLabel = new JLabel("ip地址");
            JLabel portLabel = new JLabel("端   口");
            JTextField ipField = new JTextField("127.0.0.1", 10);// ip地址的输入
            JTextField dbportField = new JTextField("3306", 10);// 端口号的输入
            JTextField usernameField = new JTextField(10);
            JPasswordField passwordField = new JPasswordField(10);
            JLabel recentLink = new JLabel("最近连接");
            JTextArea linkRecord = new JTextArea(7, 7);
            linkRecord.setBounds(200, 50, 7, 0);
            linkRecord.setEditable(false);
            ipField.setEditable(true);
            dbportField.setEditable(true);
            usernameField.setEditable(true);
            passwordField.setEditable(true);
            JButton link = new JButton("连接");
            JButton cancel = new JButton("重新输入");

            // url|user|password 一行
            // 读取最近的链接
            File rectFile = new File(ConnectINFO.historyFilename);
            List<String> allrecord = new ArrayList<>();
            if (rectFile.exists()) {
                try {
                    Scanner reader = new Scanner(new BufferedInputStream(new FileInputStream(rectFile)));
                    while (reader.hasNextLine()) {
                        allrecord.add(reader.nextLine());
                    }
                    reader.close();
                } catch (FileNotFoundException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
            }
            for (String string : allrecord) {
                linkRecord.append(mysqls.sql.databaseserver2.MainUI.getrect2(string));
                linkRecord.append("\n");
            }
            linkRecord.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    // TODO Auto-generated method stub
                    int clickTimes = e.getClickCount();
                    if (clickTimes == 2) {
                        Scanner scanner = null;
                        try {

                            String select = linkRecord.getSelectedText();

                            String str = null;
                            str = mysqls.sql.databaseserver2.MainUI.geturl(select, allrecord);
                            if (str == null) {
                                return;
                            }
                            String[] urls = str.split("\\|");
                            ConnectINFO.getInstance().setConnection(DriverManager.getConnection(urls[0], urls[1], urls[2]));

                            ConnectINFO.getInstance().setUrl(urls[0]);
                            ConnectINFO.getInstance().setUrl(urls[1]);
                            ConnectINFO.getInstance().setPassworld(urls[2]);
                            JOptionPane.showMessageDialog(null, "链接成功\n");
                            MainleftPanel.getInstance().onconnect();
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(null, "链接失败\n" + e1.getMessage());
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } finally {
                            if (scanner != null)
                                scanner.close();
                        }
                    }
                }
            });

            // 监听选择的数据库服务器
            ActionListener radioButtonListener = e -> {
                // TODO Auto-generated method stub
                if (mysql.isSelected()) {
                    ConnectINFO.getInstance().setDatabaseType("mysql");
                } else if (oracle.isSelected()) {
                    // 后续加入oracle的驱动语句
                } else if (sqlsever.isSelected()) {
                    // 后续加入sqlsever驱动语句
                }
            };

            // 监听选择的是连接还是重新输入
            ActionListener actionListener = e -> {
                // TODO Auto-generated method stub

                String choice = ((JButton) e.getSource()).getText();
                if (choice.equals("连接")) {
                    try {

                        try {

                            ConnectINFO.getInstance().setUrl(usernameField.getText());
                            ConnectINFO.getInstance().setPassworld(passwordField.getText());
                            ConnectINFO.getInstance().setUrl("jdbc:" + ConnectINFO.getInstance().getDatabaseType() + "://" + ipField.getText() + ":"
                                    + dbportField.getText() + "/?characterEncoding=utf8&useSSL=true");
                            ConnectINFO.getInstance().setConnection(DriverManager.getConnection(ConnectINFO.getInstance().getUrl(),
                                    usernameField.getText(), passwordField.getText()));
                            if (ConnectINFO.getInstance().getConnection() != null) {
                                JOptionPane.showMessageDialog(null,"链接成功");
                                MainleftPanel.getInstance().onconnect();
                                mysqls.sql.databaseserver2.MainUI.savainfo(ConnectINFO.getInstance().getUrl(), ConnectINFO.getInstance().getUser(), ConnectINFO.getInstance().getPassworld());

                            }

                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            JOptionPane.showMessageDialog(null,"链接失败");
                            e1.printStackTrace();
                        }
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                } else if (choice.equals("重新输入")) {
                    usernameField.setText("");
                    passwordField.setText("");
                }
            };

            link.addActionListener(actionListener);
            cancel.addActionListener(actionListener);
            mysql.addActionListener(radioButtonListener);
            oracle.addActionListener(radioButtonListener);
            sqlsever.addActionListener(radioButtonListener);

            JPanel jPanel = new JPanel(), jPanelNorth = new JPanel(new FlowLayout()),
                    jPanelCenter = new JPanel(new GridLayout(4, 2, -25, 1)),
                    jpanelEast = new JPanel(new BorderLayout()), jPanelSouth = new JPanel(new FlowLayout());

            jPanel.setLayout(new BorderLayout());
            jPanel.add(jPanelNorth, BorderLayout.NORTH);
            jPanel.add(jPanelCenter, BorderLayout.CENTER);
            jPanel.add(jpanelEast, BorderLayout.EAST);
            jPanel.add(jPanelSouth, BorderLayout.SOUTH);

            jPanelNorth.add(jLabel1);
            jPanelNorth.add(mysql);
            jPanelNorth.add(oracle);
            jPanelNorth.add(sqlsever);

            dbsevergroup.add(mysql);
            dbsevergroup.add(oracle);
            dbsevergroup.add(sqlsever);

            jPanelCenter.add(ipLabel);
            jPanelCenter.add(ipField);
            jPanelCenter.add(portLabel);
            jPanelCenter.add(dbportField);
            jPanelCenter.add(uname);
            jPanelCenter.add(usernameField);
            jPanelCenter.add(pwd);
            jPanelCenter.add(passwordField);

            jpanelEast.add(recentLink, BorderLayout.NORTH);
            jpanelEast.add(linkRecord, BorderLayout.CENTER);

            jPanelSouth.setLayout(new GridLayout(1, 0));
            jPanelSouth.add(link);
            jPanelSouth.add(cancel);

            panel.add(jPanel);
            return panel;
        }

        return panel;

    }

    private static void savainfo(String url, String user, String passworld) {
        // TODO Auto-generated method stub

        String rec = url + "|" + user + "|" + passworld + "\n";
        File rectFile = new File(ConnectINFO.historyFilename);

        List<String> allrecord = new ArrayList<>();
        if (rectFile.exists()) {
            try {
                Scanner reader = new Scanner(new BufferedInputStream(new FileInputStream(rectFile)));
                while (reader.hasNextLine()) {
                    allrecord.add(reader.nextLine());
                }
                reader.close();
            } catch (FileNotFoundException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
        }
        if (allrecord.contains(rec)) {
            return;

        }
        if (!rectFile.exists()) {
            try {
                rectFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (rectFile.exists()) {
            try {

                MyIOutil.copy(rec, rectFile);
            } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
        }

    }

    /**
     * @param url
     * @param user
     *            把mysql和user链接起来 格式；mysql2changhong
     * @param passworld
     */
    private static String getrect(String url, String user) {
        // TODO Auto-generated method stub
        String[] strings = url.split(":");
        return strings[1] + "2" + user;

    }

    private static String getrect2(String urString) {
        // TODO Auto-generated method stub
        String[] strings = urString.split("\\|");
        return mysqls.sql.databaseserver2.MainUI.getrect(strings[0], strings[1]);

    }

    /**
     * @param rec
     *            mysql2changhong
     * @return url，user，pass
     */
    private static String geturl(String rec, List<String> strings) {

        String[] all2 = rec.split("2");
        for (String string : strings) {
            if (string.contains(all2[0]) && string.contains(all2[1])) {
                return string;
            }
        }

        return null;
    }

    private static void opendbUI() {
        JFrame jFrame = TreeFrame.getui();
        jFrame.setVisible(true);

    }

    public static void onclickOpen() {
        String why = UIconstant.WHY_Main;
        if (why.equals("1")) {

            GraphFrame.me.loaddatabasealltables();
        }
        if (why.equals("2")) {

            GraphFrame.me.graph2dbmenu();

        }
        if (why.equals("4")) {
            GraphFrame.me.mysqlvariablemenu();
        } else {
            GraphFrame.me.loaddatabasealltables();

        }

    }

}

