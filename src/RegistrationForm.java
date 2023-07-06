import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class RegistrationForm extends JDialog {
    private JTextField tfIC;
    private JTextField tfPostcode;
    private JComboBox comboGender;
    private JTextField tfTown;
    private JTextField tfDOB;
    private JButton btnSave;
    private JPanel registerPanel;

    public RegistrationForm(JFrame parent) {
        super(parent); //call parent constructor
        setTitle("Register your account");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(550, 474));
        setModal(true);
        setLocationRelativeTo(parent); //display dialog in the middle
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        setVisible(true);
    }

    private void registerUser() {

        String IC = tfIC.getText();
        String gender = getGenderFromIC(IC);
        String postcode = tfPostcode.getText();
        String DOB = getDOBFromIC(IC);
        String town = getTownByPostcode(postcode);
        getGenderFromIC(IC);

        tfDOB.setText(DOB);
        tfTown.setText(town);

        addUserToDatabase(IC, gender, DOB, postcode, town);

    }

    public User user;

    private String getGenderFromIC(String IC){

        String gender = "";

        if(IC.length()>=11){
            String genderSubstring = IC.substring(11);
            int tempGender = Integer.parseInt(genderSubstring);
            if (tempGender % 2 == 0){
                comboGender.setSelectedIndex(1);

            }
            else{
                comboGender.setSelectedIndex(0);
            }
            gender = (String) comboGender.getSelectedItem();
        }
        return gender;
    }

    private String getDOBFromIC(String IC) {
        String dob = "";

        if (IC.length() >= 6) {
            String dobSubstring = IC.substring(0, 6);

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyMMdd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

                java.util.Date utilDate = inputFormat.parse(dobSubstring);
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                dob = outputFormat.format(sqlDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return dob;
    }

    private String getTownByPostcode(String postcode) {
        String town = "";
        final String DB_URL ="jdbc:mysql://localhost:3306/postcode";
        final String USERNAME = "root";
        final String PASSWORD = "Sh@ne222";

        try {

            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT post_office FROM postcode WHERE postcode = ?");
            stmt.setString(1, postcode);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                town = rs.getString("post_office");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return town;
    }

    private User addUserToDatabase(String ic, String gender, String dob, String postcode, String town) {
        User user = null;
        final String DB_URL ="jdbc:mysql://localhost:3306/users";
        final String USERNAME = "root";
        final String PASSWORD = "Sh@ne222";

        try{
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            //connected successfully

            //sql statements to register user
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO details (IC, gender, DOB, postcode, town)"
                    + "VALUES (?,?,?,?,?)";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, ic);
            preparedStatement.setString(2, gender);
            preparedStatement.setString(4, postcode);

            // Convert dob string to java.util.Date
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = inputFormat.parse(dob);
            String formattedDOB = outputFormat.format(utilDate);
            preparedStatement.setString(3, formattedDOB);

            preparedStatement.setString(5, town);

            //add row into the table. (enables to execute the sql query)
            int addedRows = preparedStatement.executeUpdate();
            if(addedRows>0){
                user = new User();
                user.IC = ic;
                user.gender = gender;
                user.DOB = dob;
                user.postcode = postcode;
                user.town = town;
            }

            //close connections
            stmt.close();
            conn.close();

        }catch(Exception e){
            e.printStackTrace();
        }

        return user;
    }

    public static void main(String[] args) {

        RegistrationForm myForm = new RegistrationForm(null);
        User user = myForm.user;
        if (user != null) {
            System.out.println("Successful registration of: " + user.IC);
        }
        else {
            System.out.println("Registration canceled");
        }
    }

}
