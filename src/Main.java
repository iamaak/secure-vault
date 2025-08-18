import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        DataBaseHelper dh = new DataBaseHelper();
        //new GUI(dh);
        SwingUtilities.invokeLater(() -> new GUI(dh).setVisible(true));

    }
}
