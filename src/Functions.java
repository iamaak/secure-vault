import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;


class Functions{
    private int choice;

    private  void login(){
        System.out.println("login");
    }
    private void register(){
        System.out.println("Register");
    }
    private void about(){
        System.out.println("==================================");
        System.out.println("           About Program          ");
        System.out.println("==================================");
        System.out.println("Author      : Aamir A. Khan");
        System.out.println("Designation : Assistant Sub-Inspector");
        System.out.println("Department  : Uttar Pradesh Police Technical Services");
        System.out.println("Speciality  : Cyber Cell, Police Station Gazipur, Lucknow");
        System.out.println("Version     : 1.0");
        System.out.println("==================================");
    }
    private void quit(){
        System.out.println("Quit");
    }

    public void clearScreen() throws IOException, InterruptedException {
        if (System.getProperty("os.name").contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        }
    }

    private void menu(){
        System.out.println("MENU : ");
        System.out.println("______ ");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. About");
        System.out.println("4. Exit");
    }

    protected void prepareCLI(){
        try (Scanner sc = new Scanner(System.in)){
            do{
                menu();
                System.out.print("Your Choice : ");
                choice = sc.nextInt();

                if (choice < 1 || choice > 4){
                    System.out.println("Invalid Choice ! Only Select (1-4)");
                    //Thread.sleep(1000);
                }

                switch (choice){
                    case 1: login(); break;
                    case 2: register(); break;
                    case 3: about(); break;
                    case 4: quit(); 
                }
                /*try{
                    
                   // clearScreen();
                }
                catch (IOException e){
                }
                catch (InterruptedException i){

                }*/
            }
            while (choice != 4);
        }
        catch (InputMismatchException e) {
            System.out.println("Invalid input type!");
            //Thread.sleep(1000);
        }
    }
}