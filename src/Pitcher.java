import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Pitcher {
    int kbb;
    int bat;
    String name;
    int[] gameStats = {0, 0, 0, 0, 0, 0, 0, 0, 0};  //ER, H, AB, R, HR, IP, K, BB

    public Pitcher(XSSFSheet s, int row){
        XSSFRow r = s.getRow(row);
        name = r.getCell(1).getStringCellValue();
        kbb = (int)r.getCell(2).getNumericCellValue();
        bat = (int)r.getCell(3).getNumericCellValue();
    }

    public String getName(){
        return name;
    }
    public int getKBB(){
        return kbb;
    }
    public int getBAT(){
        return bat;
    }
    public void subAB(){
        gameStats[3]--;
    }
    public void addER(){
        gameStats[6]++;
    }
    public void addH(){
        gameStats[4]++;
    }
    public void addAB(){
        gameStats[3]++;
    }
    public void addR(){
        gameStats[7]++;
    }
    public void addHR(){
        gameStats[5]++;
    }
    public void addIP(){
        gameStats[0]++;
    }
    public void addK(){
        gameStats[1]++;
    }
    public void addBB(){
        gameStats[2]++;
    }
}
