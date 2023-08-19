package com.app.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import javax.swing.*;

public class ChatClient extends javax.swing.JFrame {

    private boolean signedIn = false;
    private String username = "";
    private PrivateKey privateKey;

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message="";
    private String serverIP;
    private Socket connection;
    private int port = 6789;
    private JTextField jUserTxtFld;
    private JPasswordField jPassTxtFld;
    private JButton jLoginButton;
    private JButton jRegisterButton;
    private JLabel jUserLabel1;
    private JLabel jPassLabel1;

    private javax.swing.JPanel jPanelRegister;
    private JLabel jUserLabelR;
    private JLabel jPassLabelR;
    private JLabel jfnLabel;
    private JLabel jlnLabel;
    private JLabel jEmailLabel;
    private JTextField jUserTf;
    private JPasswordField jPassTf;
    private JTextField jfnTf;
    private JTextField jlnTf;
    private JTextField jEmailTf;
    private JButton jRegisterButtonR;
    private JTextArea chatArea;
    private JButton jButton1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanelChat;
    private JScrollPane jScrollPane;
    private JTextField jTextField1;
    private JPanel jPanelSignIn;
    private JPanel jChatPanel;
    private JTextField jTextFieldText;
    public ChatClient(String s) {

        initComponents();

        this.setTitle("Client");
        this.setVisible(true);
        serverIP = s;
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        if (!signedIn) {
            jPanelSignIn = new javax.swing.JPanel();
            jUserLabel1 = new javax.swing.JLabel("Username");
            jUserTxtFld = new javax.swing.JTextField(10);
            jPassLabel1 = new javax.swing.JLabel("Password");
            jPassTxtFld = new javax.swing.JPasswordField(10);

            jLoginButton = new javax.swing.JButton("Login");
            jRegisterButton = new javax.swing.JButton("Register");

            jPanelSignIn.add(jUserLabel1);
            jPanelSignIn.add(jUserTxtFld);
            jPanelSignIn.add(jPassLabel1);
            jPanelSignIn.add(jPassTxtFld);
            jPanelSignIn.add(jLoginButton);
            jPanelSignIn.add(jRegisterButton);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelSignIn, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelSignIn, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
            );

            setSize(new java.awt.Dimension(508, 441));
            setLocationRelativeTo(null);
            jLoginButton.addActionListener(e -> {
                loginAction();
            });

            jRegisterButton.addActionListener(e -> {
                registerAction();
            });
        }
    }// </editor-fold>//GEN-END:initComponents

    private void registerAction() {
        getContentPane().removeAll();

        jPanelRegister = new javax.swing.JPanel();
        jUserLabelR = new javax.swing.JLabel("Username");
        jUserTf = new javax.swing.JTextField(10);
        jPassLabelR = new javax.swing.JLabel("Password");
        jPassTf = new javax.swing.JPasswordField(10);
        jfnLabel = new javax.swing.JLabel("First Name");
        jfnTf = new javax.swing.JTextField();
        jlnLabel = new javax.swing.JLabel("Last Name");
        jlnTf = new javax.swing.JTextField();
        jEmailLabel = new javax.swing.JLabel("Email");
        jEmailTf = new javax.swing.JTextField();

        jRegisterButtonR = new javax.swing.JButton("Submit");

        jPanelRegister.add(jUserLabelR);
        jPanelRegister.add(jUserTf);
        jPanelRegister.add(jPassLabelR);
        jPanelRegister.add(jPassTf);
        jPanelRegister.add(jfnLabel);
        jPanelRegister.add(jfnTf);
        jPanelRegister.add(jlnLabel);
        jPanelRegister.add(jlnTf);
        jPanelRegister.add(jEmailLabel);
        jPanelRegister.add(jEmailTf);
        jPanelRegister.add(jRegisterButtonR);

        jRegisterButtonR.addActionListener(e -> {
            String password = jPassTf.getText().strip();
            String userName = jUserTf.getText().strip();
            System.out.println("PASSWORRD " + password);
            DBUtils.RegistrationResponse registrationResponse = ChatServer.register(new RegistrationRequest(
                    userName,
                    jfnTf.getText(),
                    jlnTf.getText(),
                    jEmailTf.getText()
            ));

            if (registrationResponse.userExists) {
                JOptionPane.showMessageDialog(jPanelSignIn,
                        "User name already exists");
            }
            try {
                PrivateKey privateKey = getPrivateKey(decrypt(registrationResponse.privateKey));
                String encryptedPassword = ChatUtils.encryptMessage(password, privateKey);
                System.out.println("REGISTERED " + encryptedPassword);
                ChatServer.updatePassword(userName, encryptedPassword);
            } catch (Exception exception) {
                exception.printStackTrace();
                //TODO
            }
            getContentPane().removeAll();
            initComponents();

        });


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }

    private static byte[] decrypt(String privateKey) throws Exception {
        return Base64.getDecoder().decode(ChatUtils.decryptPasswordBased(privateKey));
    }

    private void loginAction() {
        String privateKeyStr = ChatServer.getPrivateKeyForUser(jUserTxtFld.getText().strip());
        if (privateKeyStr == null) {
            JOptionPane.showMessageDialog(jPanelSignIn,
                    "Register first");
        }



        try {
            this.privateKey = getPrivateKey(decrypt(privateKeyStr));
            System.out.println("VERIFYING " + ChatUtils.encryptMessage(jPassTxtFld.getText().strip(), privateKey));
            boolean login = ChatServer.login(new LoginRequest(
                    jUserTxtFld.getText().strip(),
                    ChatUtils.encryptMessage(jPassTxtFld.getText().strip(), privateKey)));
            System.out.println("login " + login);
            if (login) {
                this.signedIn = true;
                this.username = jUserTxtFld.getText().strip();
                getContentPane().removeAll();
                chatWindow();
            }
        } catch (Exception exception) {
            //TODO
exception.printStackTrace();
        }
    }

    public void chatWindow() {

        jPanelChat = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        JPanel jPanelChat1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanelChat.setBackground(new java.awt.Color(100, 1, 51));
        jPanelChat.setForeground(new java.awt.Color(204, 204, 204));
        jPanelChat.setLayout(null);

        jLabel2.setFont(new java.awt.Font("Myriad Pro", 1, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Secure Messenger");
        jPanelChat.add(jLabel2);
        jLabel2.setBounds(140, 20, 180, 40);


        jPanelChat.add(jLabel1);
        jLabel1.setBounds(0, 0, 400, 400);
        List<String> users = ChatServer.getUsers();
        System.out.println(users);

        jScrollPane.add(jPanelChat1);
        jScrollPane.setViewportView(jPanelChat1);


        for (String user : users) {
            if (user.equals(this.username))
                continue;
            JButton jButton = new JButton(user);
            jButton.setActionCommand(user);
            jButton.setBackground(new java.awt.Color(204, 204, 255));
            jButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    painChartArea(e.getActionCommand());
                }
            });
            jButton.setBounds(420, 370, 80, 40);
            jPanelChat1.add(jButton);
            jPanelChat1.revalidate();
            jPanelChat1.repaint();
        }

        jPanelChat.add(jScrollPane);
        jScrollPane.setBounds(10, 80, 490, 280);
        Timer timer = new Timer(20000, new UserRefreshActionListener(jPanelChat1, this.username));
        timer.start();

        javax.swing.GroupLayout layout1 = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout1);

        layout1.setHorizontalGroup(
                layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelChat, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout1.setVerticalGroup(
                layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelChat, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        setSize(new java.awt.Dimension(508, 441));
        setLocationRelativeTo(null);

    }

    public class UserRefreshActionListener implements ActionListener {
        JPanel jPanelChat1;
        String userName;
        public UserRefreshActionListener(JPanel jPanelChat1, String userName) {
            this.jPanelChat1 = jPanelChat1;
            this.userName = userName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
                List<String> users = ChatServer.getUsers();

                jPanelChat1.removeAll();
                System.out.println(users);
                for (String user : users) {
                    if (user.equals(this.userName))
                        continue;
                    JButton jButton = new JButton(user);
                    jButton.setBackground(new java.awt.Color(204, 204, 255));
                    jButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
                    jButton.setActionCommand(user);
                    jButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            painChartArea(e.getActionCommand());
                        }
                    });
                    jButton.setBounds(420, 370, 80, 40);

                    jPanelChat1.add(jButton);
                    jPanelChat1.revalidate();
                    jPanelChat1.repaint();

                }

                //Refresh the panel
                jPanelChat1.revalidate();
            }

    }

    private void painChartArea(String receiver) {
        JFrame jFrame = new JFrame(this.username);

        System.out.println(receiver);
        JPanel jPanelChat = new javax.swing.JPanel();
        jTextFieldText = new javax.swing.JTextField();
        JButton jButton1 = new javax.swing.JButton();
        JScrollPane jScrollPane = new javax.swing.JScrollPane();
         chatArea = new javax.swing.JTextArea();
        JLabel jLabel2 = new javax.swing.JLabel();
        JLabel jLabel1 = new javax.swing.JLabel();

        jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jFrame.setResizable(false);

        jPanelChat.setBackground(new java.awt.Color(100, 1, 51));
        jPanelChat.setForeground(new java.awt.Color(204, 204, 204));
        jPanelChat.setLayout(null);

        jLabel2.setFont(new java.awt.Font("Myriad Pro", 1, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Secure Messenger");
        jPanelChat.add(jLabel2);
        jLabel2.setBounds(140, 20, 180, 40);


        jPanelChat.add(jLabel1);
        jLabel1.setBounds(0, 0, 400, 400);



        javax.swing.GroupLayout layout1 = new javax.swing.GroupLayout(jFrame.getContentPane());
        jFrame.getContentPane().setLayout(layout1);

        layout1.setHorizontalGroup(
                layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelChat, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout1.setVerticalGroup(
                layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelChat, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jFrame.setSize(new java.awt.Dimension(508, 441));
        jFrame.setLocationRelativeTo(null);
        jTextFieldText.setToolTipText("text\tType your message here...");
        jTextFieldText.setActionCommand(receiver);
        jTextFieldText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanelChat.add(jTextFieldText);
        jTextFieldText.setBounds(10, 370, 410, 40);

        jButton1.setBackground(new java.awt.Color(204, 204, 255));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton1.setText("Send");
        jButton1.setActionCommand(receiver);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performSendAction(evt);
            }
        });
        jPanelChat.add(jButton1);
        jButton1.setBounds(420, 370, 80, 40);

        chatArea.setColumns(20);
        chatArea.setRows(5);

        List<MessageRequest> messageRequests = ChatServer.pollMessages(this.username, receiver);
        for (MessageRequest messageRequest : messageRequests) {
            if (messageRequest.getSender_name().equals(this.username)) {
                String publicKeyForUser = ChatServer.getPublicKeyForUser(this.username);
                try {
                    PublicKey publicKey = getPublicKey(decrypt(publicKeyForUser));
                    String message = ChatUtils.decryptMessage(messageRequest.getMessage(), publicKey);
                    chatArea.append("\nME("+messageRequest.getSender_name()+") - " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                String publicKeyForUser = ChatServer.getPublicKeyForUser(receiver);
                try {
                    PublicKey publicKey = getPublicKey(decrypt(publicKeyForUser));
                    String message = ChatUtils.decryptMessage(messageRequest.getMessage(), publicKey);
                    chatArea.append("\nTHEM("+messageRequest.getSender_name()+") - " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Timer timer = new Timer(1000, new MessageRefreshAL(this.username, receiver, chatArea));
        timer.start();

        jScrollPane.setViewportView(chatArea);

        jPanelChat.add(jScrollPane);
        jScrollPane.setBounds(10, 80, 490, 280);
        jFrame.setVisible(true);
    }

    public static PrivateKey getPrivateKey(byte[] privateKey) throws Exception {
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pvt = kf.generatePrivate(ks);
        return pvt;
    }

    public static PublicKey getPublicKey(byte[] privateKey) throws Exception {


        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);

    }

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

        sendMessage(evt.getActionCommand(), jTextFieldText.getText());
        jTextFieldText.setText("");
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void performSendAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        sendMessage(evt.getActionCommand(), jTextFieldText.getText());
        jTextFieldText.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    public static class MessageRefreshAL implements ActionListener {

        JTextArea chatArea;
        String sender;
        String receiver;
        public MessageRefreshAL(String sender, String receiver, JTextArea chatArea) {
            this.sender = sender;
            this.receiver = receiver;
            this.chatArea = chatArea;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            chatArea.selectAll();
            chatArea.replaceSelection("");
            List<MessageRequest> messageRequests = ChatServer.pollMessages(sender, receiver);
            for (MessageRequest messageRequest : messageRequests) {
                if (messageRequest.getSender_name().equals(sender)) {
                    String publicKeyForUser = ChatServer.getPublicKeyForUser(sender);
                    try {
                        PublicKey publicKey = getPublicKey(decrypt(publicKeyForUser));
                        String message = ChatUtils.decryptMessage(messageRequest.getMessage(), publicKey);
                        chatArea.append("\nME("+messageRequest.getSender_name()+") - " + message);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                } else {
                    String publicKeyForUser = ChatServer.getPublicKeyForUser(receiver);
                    try {
                        PublicKey publicKey = getPublicKey(decrypt(publicKeyForUser));
                        String message = ChatUtils.decryptMessage(messageRequest.getMessage(), publicKey);
                        chatArea.append("\nTHEM("+messageRequest.getSender_name()+") - " + message);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
            chatArea.repaint();

        }
    }
    public void startRunning()
    {
        try
        {

            whileChatting();
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    private void whileChatting() throws IOException
    {
        jTextField1.setEditable(true);
        do{
            try
            {
                message = (String) input.readObject();
                chatArea.append("\n"+message);
            }
            catch(ClassNotFoundException classNotFoundException)
            {
            }
        }while(!message.equals("Client - END"));
    }


    private void sendMessage(String receiver, String message)
    {
        try
        {

            String encryptMessage = ChatUtils.encryptMessage(message, privateKey);

            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setSender_name(this.username);
            messageRequest.setReceiver_name(receiver);
            messageRequest.setMessage(encryptMessage);

            ChatServer.sendMessage(messageRequest);
            chatArea.append("\nME("+this.username+") - "+message);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        /*catch(IOException ioException)
        {
            chatArea.append("\n Unable to Send Message");
        }*/
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables


    // End of variables declaration//GEN-END:variables

    public static void main(String[] args)
    {
        ChatClient client=new ChatClient("127.0.0.1");
        client.startRunning();
    }
}