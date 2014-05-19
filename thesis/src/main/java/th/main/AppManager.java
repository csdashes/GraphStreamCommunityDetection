package th.main;

import th.utils.Menu;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class AppManager {
    
    public void printUserMenu() {
        int selection;
        boolean flag = true;
        
        while(flag) {
            selection = Menu.printMenu();
            switch(selection) {
                case 1:
                    //Execute 1st function
                    break;
                case 2:
                    //Execute 2st function
                    break;
                case 3:
                    //Execute 3rd function
                case 0:
                    //Exit
                    return;
            }
        }
    }
}
