
public class Gamelog {
    public String[] events = new String[500];
    public int[] bases = new int[500];
    int count=0;
    int bcount=0;
    int rcount=0;
    int[] rand = new int[4000];

    public Gamelog(){}

    public void addEvent(String s){
        events[count]=s;
        count++;
    }
    public void addBases(int b){
        bases[bcount]=b;
        bcount++;
    }
    public void addRand(int r){
        rand[rcount]=r;
        rcount++;
    }
    public String getEvent(int i){
        return events[i];
    }
    public String[] getEvents(){
        return events;
    }
    public int[] getBases(){
        return bases;
    }
    public int[] getRand(){
        return rand;
    }
}
