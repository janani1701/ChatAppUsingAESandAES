package com.app.chat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;


public class DBUtils {

    static {
        Connection connect = connect();
        Statement statement = null;
        try {
            statement = connect.createStatement();
            String sql = "create table if not exists User(user_name varchar(250), password varchar(250)," +
                    " first_name varchar(250), last_name varchar(250), email varchar(250), public_key longtext, private_key longtext)";
            boolean execute = statement.execute(sql);
            System.out.println("User Table Created " + execute);

            sql = "create table if not exists Message(user1 varchar(250), user2 varchar(250), message longtext, sender int, order_ int)";

            execute = statement.execute(sql);
            System.out.println("Message Table Created " + execute);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
     /**
     * Connect to a sample database
      * @return
      */
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");

            // db parameters
            String url = "jdbc:sqlite:test.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static boolean loginUser(LoginRequest loginRequest) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select user_name from User where user_name = ? and password = ?");
            statement.setString(1, loginRequest.getUser_name());
            statement.setString(2, generateEncryptedPassword(loginRequest.getPassword()));

            ResultSet resultSet = statement.executeQuery();
            String user_name = null;
            if (resultSet.next()) {
                user_name = resultSet.getString("user_name");
            }

            if (user_name != null) {
                return true;
            } else {
                return false;
            }

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return true;
    }

    public static Integer getMaxOrder(String user1, String user2) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select max(order_) from Message where user1 = ? and user2 = ?");
            statement.setString(1, user1);
            statement.setString(2, user2);

            ResultSet resultSet = statement.executeQuery();
            Integer maxOder = null;
            if (resultSet.next()) {
                maxOder = resultSet.getInt(1);
            }

            if (maxOder != null) {
                return maxOder;
            } else {
                return 0;
            }

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return 0;
    }


    public static String getPrivateKey(String user) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select private_key from User where user_name = ?");
            statement.setString(1, user);

            ResultSet resultSet = statement.executeQuery();
            String privateKey = null;
            if (resultSet.next()) {
                privateKey = resultSet.getString(1);
            }
            return privateKey;

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return null;
    }

    public static String getPublicKey(String user) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select public_key from User where user_name = ?");
            statement.setString(1, user);

            ResultSet resultSet = statement.executeQuery();
            String privateKey = null;
            if (resultSet.next()) {
                privateKey = resultSet.getString(1);
            }
            return privateKey;

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return null;
    }



    public static List<MessageRequest> getMessages(String senderName, String receiverName) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select * from Message where user1 = ? and user2 = ? order by order_");
            int senderHash = senderName.hashCode();
            int receiverHash = receiverName.hashCode();
            String user1 = senderHash > receiverHash ? receiverName : senderName;
            String user2 = senderHash > receiverHash ? senderName : receiverName;

            statement.setString(1, user1);
            statement.setString(2, user2);

            List<MessageRequest> messageRequests = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                MessageRequest messageRequest = new MessageRequest();
                String u1 = resultSet.getString(1);
                String u2 = resultSet.getString(2);
                String message = resultSet.getString(3);
                int sender = resultSet.getInt(4);
                if (sender == 1 ) {
                    messageRequest.setSender_name(u1);
                    messageRequest.setReceiver_name(u2);
                } else {
                    messageRequest.setSender_name(u2);
                    messageRequest.setReceiver_name(u1);
                }
                messageRequest.setMessage(ChatUtils.decryptPasswordBased(message));
                messageRequests.add(messageRequest);
            }

            return messageRequests;

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public static void insertMessage(MessageRequest messageRequest) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("insert into Message values(?, ?, ?, ?, ?)");
            int senderHash = messageRequest.getSender_name().hashCode();
            int receiverHash = messageRequest.getReceiver_name().hashCode();
            String user1 = senderHash > receiverHash ? messageRequest.getReceiver_name() : messageRequest.getSender_name();
            String user2 = senderHash > receiverHash ? messageRequest.getSender_name() : messageRequest.getReceiver_name();

            statement.setString(1, user1);
            statement.setString(2, user2);
            statement.setString(3, generateEncryptedPassword(messageRequest.getMessage()));
            statement.setInt(4, messageRequest.getSender_name().equals(user1) ? 1 : 2);

            int order = 0;
            Integer maxOrder = getMaxOrder(user1, user2);
            if (maxOrder == 0L) {
                order = 1;
            } else {
                order = maxOrder + 1;
            }

            statement.setInt(5, order);

            statement.executeUpdate();

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    public static void updatePassword(String userName, String encryptedPassword) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("update User set password = ? where user_name = ?");

            statement.setString(1, generateEncryptedPassword(encryptedPassword));
            statement.setString(2, userName);

            int i = statement.executeUpdate();
            System.out.println(i);

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static List<String> getUsers() {

        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select user_name from User");
            ResultSet resultSet = statement.executeQuery();
            List<String> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(resultSet.getString(1));
            }

            return users;
        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return Collections.emptyList();

    }

    public static class RegistrationResponse {
        boolean userExists;
        String privateKey;

        public RegistrationResponse(boolean userExists, String privateKey) {
            this.userExists = userExists;
            this.privateKey = privateKey;
        }
    }

    public static RegistrationResponse createUserIfNotExists(RegistrationRequest request) {
        Connection connect = connect();
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement("select user_name from User where user_name = ?");
            statement.setString(1, request.getUser_name());

            ResultSet resultSet = statement.executeQuery();
            String user_name = null;
            if (resultSet.next()) {
                user_name = resultSet.getString("user_name");
            }

            if (user_name != null) {
                return new RegistrationResponse(true, null);
            }


            ChatUtils.PublicPrivateKeyPair publicPrivateKeys = ChatUtils.getPublicPrivateKeys();
            String publicKey = generateEncryptedPassword(publicPrivateKeys.getPublicKey());
            String privateKey = generateEncryptedPassword(publicPrivateKeys.getPrivateKey());


            statement = connect.prepareStatement("insert into User(user_name, first_name, last_name, email, public_key, private_key) values(?, ?, ?, ?, ?, ?)");
            statement.setString(1, request.getUser_name());
            statement.setString(2, request.getFirst_name());
            statement.setString(3, request.getLast_name());
            statement.setString(4, request.getEmail());
            statement.setString(5, publicKey);
            statement.setString(6, privateKey);

            int i = statement.executeUpdate();

            System.out.println("Response from mysql " + i);

            return new RegistrationResponse(false,privateKey);

        } catch (Exception throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connect.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return null;

    }

    private static String generateEncryptedPassword(String password) {
        try {
            return ChatUtils.encryptPasswordBased(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RegistrationResponse registrationResponse = createUserIfNotExists(new RegistrationRequest("sxdffffdsdfgddfffsdfsdfdfdfdfdxvxvdfggdfgvsaDF", "fdff", "hgvf", "hagdfv"));


        try
        {

            PrivateKey privateKey = ChatClient.getPrivateKey(Base64.getDecoder().decode(ChatUtils.decryptPasswordBased(registrationResponse.privateKey)));
            String encryptedPassword = ChatUtils.encryptMessage("sdfdf", privateKey);
        } catch (Exception exception) {
            exception.printStackTrace();
            //TODO
        }
    }
}