import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException{
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream("XSSF Test WB.xlsx"));
        Team away = new Team(5, "Away Team", wb, 0);
        Team home = new Team(6, "Home Team", wb, 1);

        PlayGame pg = new PlayGame(away, home, 0, 0);
        pg.playGame();
    }
}
