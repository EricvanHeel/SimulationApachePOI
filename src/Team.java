import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;

public class Team {
    public Player[] roster = new Player[9];
    public int defense = 7;		// 3 - 10 is good range
    public Pitcher[] pitchingStaff = new Pitcher[4];
    public int errors, score = 0;
    public String name;
    XSSFSheet sheet;
    XSSFSheet statSheet;
    XSSFWorkbook wb;

    public Team(int d, String n, XSSFWorkbook w, int s){
        defense = d;
        name = n;
        wb = w;
        sheet = wb.getSheetAt(s);
        statSheet = wb.getSheetAt(s+2);
        for(int i = 0; i < 9;i++) {
            roster[i] = new Player(sheet, i+4);
        }
        for(int i = 0; i < 4; i++){
            pitchingStaff[i] = new Pitcher(sheet,i+15);
        }
    }

    public void addRoster(Player p, int i){
        roster[i] = p;
    }

    public void addPitcher(Pitcher p, int i){
        pitchingStaff[i] = p;
    }

    public Pitcher getPitcher(int i){
        return pitchingStaff[i];
    }

    public int getError(){
        return errors;
    }

    public void addError(){
        errors++;
    }

    public int getDefense(){
        return defense;
    }
}
