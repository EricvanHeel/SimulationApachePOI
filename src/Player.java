import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Player {
    public int[] ab = new int[8];        //parameters
    public int[] stats = new int[9];
    public String name;
    public int[] gameStats = new int[15];	//stats
    public boolean earned = true;			//did this player get on by an error
    public int speed;

    public Player(XSSFSheet s, int row){
        XSSFRow r = s.getRow(row);
        name = r.getCell(1).getStringCellValue();
        for(int i = 0; i < 6; i++){
            stats[i] = (int)r.getCell(2+i).getNumericCellValue();
        }
        stats[6] = (int)(r.getCell(8).getNumericCellValue()/2);
        stats[7] = (int)(r.getCell(8).getNumericCellValue()-stats[6]);
        ab[0] = stats[0];
        for(int i = 1; i < 7; i++){
            ab[i] = ab[i-1]+stats[i];
        }
        speed = (int)r.getCell(9).getNumericCellValue();
    }

    public void printString(){
        System.out.println(name+"\t"+ab[0]+"\t"+ab[1]+"\t"+ab[2]+"\t"+ab[3]+"\t"+ab[4]+"\t"+ab[5]+"\t"+ab[6]+"\t"+ab[7]);
    }
    public void setEarned(boolean b){
        earned = b;
    }
    public boolean getEarned(){
        return earned;
    }
    public void addAB(){
        gameStats[0] = gameStats[0] + 1;
    }
    public void addH(){
        gameStats[1] = gameStats[1] + 1;
    }
    public void add2B(){
        gameStats[2] = gameStats[2] + 1;
    }
    public void add3B(){
        gameStats[3] = gameStats[3] + 1;
    }
    public void addHR(){
        gameStats[4] = gameStats[4] + 1;
    }
    public void addK(){
        gameStats[5] = gameStats[5] + 1;
    }
    public void addBB(){
        gameStats[6] = gameStats[6] + 1;
    }
    public void addRBI(){
        gameStats[7] = gameStats[7] + 1;
    }
    public void addR(){
        gameStats[8] = gameStats[8] + 1;
    }
    public void addSAC(){
        gameStats[9] = gameStats[9] + 1;
    }
}
