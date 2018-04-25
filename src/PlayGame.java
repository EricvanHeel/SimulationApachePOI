import org.apache.poi.xssf.usermodel.XSSFRow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class PlayGame {
    Gamelog g = new Gamelog();
    Team away, home;
    Pitcher aP, hP;
    Random r = new Random();
    int outs;
    int apitch, hpitch;

    public PlayGame(Team a, Team h, int apitcher, int hpitcher) {
        away = a;
        home = h;
        aP = a.getPitcher(apitch);
        hP = h.getPitcher(hpitch);
        apitch = apitcher;
        hpitch = hpitcher;
    }

    public void playGame() {
        //Initialize batters, pitchers and inning
        int aBatInd = 0;    //batter index
        int hBatInd = 0;
        int inning = 1;
        int limit = 10; //determines when to go to extra innings
        System.out.println(aP.getName() + " vs " + hP.getName());
        System.out.println();

        //Begin simulation
        while (inning < limit) {
            g.addEvent(away.name + " now batting, " + home.name + " int the field. Top " + inning);
            hP.addIP();
            aBatInd = halfInning(aBatInd, away, home, hP, false);    //halfInning will sim beginning with aBat and will return the next aBat when inning is over
            if (inning > 8)  //handles when this is the potentially last inning
                if (home.score > away.score) //home is already winning, no need to play bot last
                    System.out.println(inning + "\t" + away.score + "\tX");
                else {
                    g.addEvent(home.name + " now batting, " + away.name + " in the field. Bot " + inning);
                    aP.addIP();
                    hBatInd = halfInning(hBatInd, home, away, aP, true);
                    System.out.println(inning + "\t" + away.score + "\t" + home.score);
                }
            else {
                g.addEvent(home.name + " now batting, " + away.name + " in the field. Bot " + inning);
                aP.addIP();
                hBatInd = halfInning(hBatInd, home, away, aP, false);
                System.out.println(inning + "\t" + away.score + "\t" + home.score);
            }
            inning++;
            if (inning > 9 && away.score == home.score)
                limit++;    //increase limit to allow for an extra inning
        }

        //Game is finished
        String finalScore;
        if (away.score > home.score)
            finalScore = away.name + " win " + away.score + " to " + home.score + " over the " + home.name + ".";
        else
            finalScore = home.name + " win " + home.score + " to " + away.score + " over the " + away.name + ".";
        g.addEvent(finalScore);

        //Printing player stats
        //							Team	Errors
        //Print Stats in the form:	Name	AB	H	2B	3B	HR	K	BB	RBI	R   SAC
        //							Pitcher	K	BB	AB	H	HR	ER	R
        Team[] t = {away, home};
        Pitcher[] pi = {aP, hP};
        for (int k = 0; k < 2; k++) {
            System.out.println();
            System.out.println(t[k].name + "\tErrors: " + t[k].getError());
            System.out.println("AB\tH\t2B\t3B\tHR\tK\tBB\tRBI\tR\tSAC");
            for (int i = 0; i < 9; i++) { //For each player
                Player p = t[k].roster[i];
                String line = p.gameStats[0] + "";  //First stat (AB)
                for (int n = 1; n < 10; n++) {   //For all other stats
                    line = line + "\t" + p.gameStats[n];
                }
                System.out.println(line);
            }


            //Printing Pitcher's stats
            System.out.println("IP\tK\tBB\tAB\tH\tHR\tER\tR");
            String line = pi[k].gameStats[0] + "";
            for (int n = 1; n < 8; n++) {
                line = line + "\t" + pi[k].gameStats[n];
            }
            System.out.println(line);
        }

        //Finally, print game log
        System.out.println();
        for (int k = 0; g.events[k] != null; k++) {
            System.out.println(g.events[k]);
        }

        //Export stats to spreadsheet
        try{
            this.exportStats(away, apitch);
            this.exportStats(home, hpitch);}
        catch(Exception e){}
    }

    public Player[] single(Player p, Team atBat, Team inField, Pitcher pitcher, Player[] bases, boolean botlast) {
        String event = new String();
        if (botlast && bases[2] != null && bases[1] != null && atBat.score == inField.score) ;
        else
            botlast = false;
        if (!botlast) {
            event = p.name + " singles";
/*3rd*/
            if (bases[2] != null) {
                event = event + ", " + bases[2].name + " scores";
                atBat.score++;
                if (bases[2].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[2].addR();
                bases[2] = null;
            }
/*2nd*/     if (bases[1] != null) {
                int j = 0;
                if (bases[1].speed == inField.getDefense())
                    j = r.nextInt(2);
    /*run*/     if (bases[1].speed > inField.getDefense() || j == 1) {
                    event = event + ", " + bases[1].name + " scores";
                    if (bases[1].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    p.addRBI();
                    bases[1].addR();
                    atBat.score++;
                    bases[1] = null;
                }
    /*stay*/    else if (bases[1].speed < inField.getDefense()) {
                    event = event + ", " + bases[1].name + " to third";
                    bases[2] = bases[1];
                    bases[1] = null;
                }
    /*out*/     else {
                    event = event + ", " + bases[1].name + " out at the plate";
                    outs++;
                }
            }
/*1st*/     if (bases[0] != null) {
                event = event + ", " + bases[0].name + " to second";
                bases[1] = bases[0];
            }
            bases[0] = p;
            p.addH();
            p.addAB();
            pitcher.addAB();
            pitcher.addH();
        }
        else {
            event = p.name + " singles, " + bases[2].name + " scores";
            if (bases[0] != null) {
                event = event + ", " + bases[1].name + " to third, " + bases[0].name + " to second";
            }
            atBat.score++;
            if (bases[2].getEarned())
                pitcher.addER();
            pitcher.addR();
            pitcher.addH();
            pitcher.addAB();
            p.addRBI();
            p.addH();
            p.addAB();
            bases[2].addR();
        }
        g.addEvent(event);
        return bases;
    }

    public Player[] doub(Player p, Team atBat, Team inField, Pitcher pitcher, Player[] bases, boolean botlast) {
        String event = new String();

        if (!botlast) {
            event = p.name + " doubles";
/*3rd*/     if (bases[2] != null) {
                event = event + ", " + bases[2].name + " scores";
                atBat.score++;
                if (bases[2].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[2].addR();
                bases[2] = null;
            }
/*2nd*/     if (bases[1] != null) {
                event = event + ", " + bases[1].name + " scores";
                atBat.score++;
                if (bases[1].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[1].addR();
                bases[1] = null;
            }
/*1st*/     if (bases[0] != null) {
                int j = 0;
                if (bases[0].speed == inField.getDefense())
                    j = r.nextInt(2);
    /*run*/     if (bases[0].speed > inField.getDefense() || j == 1) {
                    event = event + ", " + bases[0].name + " scores";
                    atBat.score++;
                    if (bases[0].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    p.addRBI();
                    bases[0].addR();
                    bases[0] = null;
                }
    /*stay*/    else if (bases[0].speed < inField.getDefense()) {
                    event = event + ", " + bases[0].name + " to third";
                    bases[2] = bases[0];
                    bases[0] = null;
                }
    /*out*/     else {
                    event = event + ", " + bases[0].name + " out at the plate";
                    outs++;
                    bases[0] = null;
                }
            }
            bases[1] = p;
            p.addH();
            p.addAB();
            p.add2B();
            pitcher.addAB();
            pitcher.addH();
        }
        else {
            if (bases[2] != null && atBat.score == inField.score) {
                event = p.name + " singles, " + bases[2].name + " scores";
                if (bases[1] != null && bases[0] != null)
                    event = event + ", " + bases[1].name + " to third, " + bases[0].name + " to second";
                else if (bases[0] != null)
                    event = event + ", " + bases[0].name + " to second";
                atBat.score++;
                if (bases[2].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[2].addR();
                bases[2] = bases[1];
                bases[1] = bases[0];
                bases[0] = p;
            }
            else if (bases[1] != null && atBat.score == inField.score) {
                event = p.name + " doubles, " + bases[1].name + " scores";
                if (bases[0] != null)
                    event = event + ", " + bases[0].name + " to third";
                atBat.score++;
                if (bases[1].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                p.add2B();
                bases[1].addR();
                bases[2] = bases[0];
                bases[1] = p;
            }
            else if (bases[0] != null && atBat.score == inField.score) {
                event = p.name + " doubles";
                int j = 0;
                if (bases[0].speed == inField.getDefense())
                    j = r.nextInt(2);
    /*run*/
                if (bases[0].speed > inField.getDefense() || j == 1) {
                    event = event + ", " + bases[0].name + " scores";
                    atBat.score++;
                    if (bases[0].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    p.addRBI();
                    bases[0].addR();
                    bases[0] = null;
                    bases[1] = p;
                }
    /*stay*/    else if (bases[0].speed < inField.getDefense()) {
                    event = event + ", " + bases[0].name + " to third";
                    bases[2] = bases[0];
                    bases[0] = null;
                    bases[1] = p;
                }
    /*out*/     else {
                    event = event + ", " + bases[0].name + " out at the plate";
                    outs++;
                    bases[0] = null;
                    bases[1] = p;
                }
                p.add2B();
            }
            else {
                event = p.name + " doubles";
                p.add2B();
                if (bases[2] != null) {
                    event = event + ", " + bases[2].name + " scores";
                    atBat.score++;
                    if (bases[2].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    p.addRBI();
                    bases[2].addR();
                    bases[2] = null;
                }
                if (bases[1] != null) {
                    event = event + ", " + bases[1].name + " scores";
                    atBat.score++;
                    if (bases[1].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    p.addRBI();
                    bases[1].addR();
                    bases[1] = null;
                }
                if (bases[0] != null) {
                    if (atBat.score > inField.score) {
                        event = event + ", " + bases[0].name + " to third";
                        bases[2] = bases[0];
                        bases[0] = null;
                    }
                    else {
                        int j = 0;
                        if (bases[0].speed == inField.getDefense())
                            j = r.nextInt(2);
    /*run*/             if (bases[0].speed > inField.getDefense() || j == 1) {
                            event = event + ", " + bases[0].name + " scores";
                            atBat.score++;
                            if (bases[0].getEarned())
                                pitcher.addER();
                            pitcher.addR();
                            p.addRBI();
                            bases[0].addR();
                            bases[0] = null;
                        }
    /*stay*/            else if (bases[0].speed < inField.getDefense()) {
                            event = event + ", " + bases[0].name + " to third";
                            bases[2] = bases[0];
                            bases[0] = null;
                        }
    /*out*/             else {
                            event = event + ", " + bases[0].name + " out at the plate";
                            outs++;
                            bases[0] = null;
                        }
                    }
                }
                bases[1] = p;
            }
            p.addAB();
            p.addH();
            pitcher.addAB();
            pitcher.addH();
        }
        g.addEvent(event);
        return bases;
    }

    public Player[] triple(Player p, Team atBat, Team inField, Pitcher pitcher, Player[] bases, boolean botlast) {
        String event = new String();
        if (!botlast) {
            event = p.name + " triples";
    /*3rd*/ if (bases[2] != null) {
                event = event + ", " + bases[2].name + " scores";
                atBat.score++;
                if (bases[2].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[2].addR();
                bases[2] = null;
            }
    /*2nd*/ if (bases[1] != null) {
                event = event + ", " + bases[1].name + " scores";
                atBat.score++;
                if (bases[1].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[1].addR();
                bases[1] = null;
            }
    /*1st*/ if (bases[0] != null) {
                event = event + ", " + bases[0].name + " scores";
                atBat.score++;
                if (bases[0].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[0].addR();
                bases[0] = null;
            }
            bases[2] = p;
            p.addH();
            p.addAB();
            p.add3B();
            pitcher.addAB();
            pitcher.addH();
        }
        else {
            if (bases[2] != null && atBat.score == inField.score) {
                event = p.name + " singles , " + bases[2].name + " scores";
                if (bases[1] != null)
                    event = event + ", " + bases[1].name + " to third";
                if (bases[0] != null)
                    event = event + ", " + bases[0].name + " to second";
                atBat.score++;
                if (bases[2].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                bases[2].addR();
                bases[2] = bases[1];
                bases[1] = bases[0];
                bases[0] = p;
            }
            else if (bases[1] != null && atBat.score == inField.score) {
                event = p.name + " doubles , " + bases[1].name + " scores";
                if (bases[0] != null)
                    event = event + ", " + bases[0].name + " to third";
                atBat.score++;
                if (bases[1].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                p.add2B();
                bases[1].addR();
                bases[2] = bases[0];
                bases[1] = p;
            }
            else if (bases[0] != null && atBat.score == inField.score) {
                event = p.name + " triples , " + bases[0].name + " scores";
                atBat.score++;
                if (bases[1].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                p.add3B();
                bases[0] = null;
                bases[2] = p;
            }
            else {
                if (atBat.score == inField.score - 1 && bases[2] != null && bases[1] != null && bases[0] != null) {
                    event = p.name + " doubles, " + bases[2].name + " scores, " + bases[1].name + " scores, " + bases[0].name + " to third";
                    atBat.score = atBat.score + 2;
                    if (bases[2].getEarned())
                        pitcher.addER();
                    if (bases[1].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    pitcher.addR();
                    p.addRBI();
                    p.addRBI();
                    p.add2B();
                    bases[2].addR();
                    bases[1].addR();
                    bases[2] = bases[0];
                    bases[1] = p;
                    bases[0] = null;
                }
                else {
    /*3rd*/         event = p.name+" triples";
                    if (bases[2] != null) {
                        event = event + ", " + bases[2].name + " scores";
                        atBat.score++;
                        if (bases[2].getEarned())
                            pitcher.addER();
                        pitcher.addR();
                        p.addRBI();
                        bases[2].addR();
                        bases[2] = null;
                    }
    /*2nd*/         if (bases[1] != null) {
                        event = event + ", " + bases[1] + " scores";
                        atBat.score++;
                        if (bases[1].getEarned())
                            pitcher.addER();
                        pitcher.addR();
                        p.addRBI();
                        bases[1].addR();
                        bases[1] = null;
                    }
    /*1st*/         if (bases[0] != null) {
                        event = event + ", " + bases[0] + " scores";
                        atBat.score++;
                        if (bases[0].getEarned())
                            pitcher.addER();
                        pitcher.addR();
                        p.addRBI();
                        bases[0].addR();
                        bases[0] = null;
                    }
                    bases[2] = p;
                    p.add3B();
                }
            }
            p.addH();
            p.addAB();
            pitcher.addH();
            pitcher.addAB();
        }
        g.addEvent(event);
        return bases;
    }

    public Player[] homeRun(Player p, Team atBat, Pitcher pitcher, Player[] bases) {
        String event = new String();
        event = p.name + " homers";
        /*3rd*/
        if (bases[2] != null) {
            event = event + ", " + bases[2].name + " scores";
            atBat.score++;
            if (bases[2].getEarned())
                pitcher.addER();
            pitcher.addR();
            p.addRBI();
            bases[2].addR();
            bases[2] = null;
        }
        /*2nd*/
        if (bases[1] != null) {
            event = event + ", " + bases[1].name + " scores";
            atBat.score++;
            if (bases[1].getEarned())
                pitcher.addER();
            pitcher.addR();
            p.addRBI();
            bases[1].addR();
            bases[1] = null;
        }
        /*1st*/
        if (bases[0] != null) {
            event = event + ", " + bases[0].name + " scores";
            atBat.score++;
            if (bases[0].getEarned())
                pitcher.addER();
            pitcher.addR();
            p.addRBI();
            bases[0].addR();
            bases[0] = null;
        }
        atBat.score++;
        p.addRBI();
        p.addH();
        p.addAB();
        p.addHR();
        p.addR();
        pitcher.addAB();
        pitcher.addH();
        pitcher.addHR();
        pitcher.addER();
        pitcher.addR();
        g.addEvent(event);
        return bases;
    }

    public Player[] strikeOut(Player p, Pitcher pitcher, Player[] bases) {
        String event = new String();
        event = p.name + " strikes out";
        p.addAB();
        p.addK();
        pitcher.addK();
        pitcher.addAB();
        outs++;
        g.addEvent(event);
        return bases;
    }

    public Player[] walk(Player p, Team atBat, Pitcher pitcher, Player[] bases) {
        String event = new String();
        event = p.name + " walks";
        if (bases[2] != null && bases[1] != null && bases[0] != null) {
            event = event + ", " + bases[2].name + " scores, " + bases[1].name + " to third, " + bases[0].name + " to second";
            atBat.score++;
            if (bases[2].getEarned())
                pitcher.addER();
            pitcher.addR();
            p.addRBI();
            bases[2].addR();
            bases[2] = bases[1];
            bases[1] = bases[0];
        }
        else if (bases[1] != null && bases[0] != null) {
            event = event + ", " + bases[1].name + " to third, " + bases[0].name + " to second";
            bases[2] = bases[1];
            bases[1] = bases[0];
        }
        else if (bases[0] != null) {
            event = event + ", " + bases[0].name + " to second";
            bases[1] = bases[0];
        }
        bases[0] = p;
        p.addBB();
        pitcher.addBB();
        g.addEvent(event);
        return bases;
    }

    public Player[] ground(Player p, Team inField, Pitcher pitcher, Player[] bases) {
        String event;
        if (outs < 2) {
            if (bases[2] == null && bases[1] != null && bases[0] == null) {
                event = p.name + " grounds out";
                if (bases[1].speed > inField.getDefense()) {
                    event = event + ", " + bases[1].name + " moves to third";
                    bases[2] = bases[1];
                    bases[1] = null;
                }
                outs++;
            }
            else if (bases[2] != null && bases[0] != null) {
                outs++;
                if (bases[1] == null) {
                    event = p.name + " grounds out, " + bases[0].name + " moves to second";
                    bases[1] = bases[0];
                    bases[0] = null;
                } else {
                    event = p.name + " grounds into fielder's choice, " + bases[2].name + " out at the plate";
                    bases[2] = bases[1];
                    bases[1] = bases[0];
                    bases[0] = p;
                }
            }
            else if (bases[2] == null && bases[0] != null) {
                if (p.speed > inField.getDefense()) {
                    bases[0] = p;
                    bases[2] = bases[1];
                    bases[1] = null;
                    outs++;
                    event = p.name + " grounds into fielder's choice";
                } else {
                    bases[2] = bases[1];
                    bases[1] = null;
                    bases[0] = null;
                    outs = outs + 2;
                    event = p.name + " grounds into double play";
                }
            }
            else {
                event = p.name + " grounds out";
                outs++;
            }
        }
        else {
            event = p.name + " grounds out";
            outs++;
        }
        p.addAB();
        pitcher.addAB();
        g.addEvent(event);
        return bases;
    }

    public Player[] flyOut(Player p, Team atBat, Team inField, Pitcher pitcher, Player[] bases) {
        String event = new String();
        int deep = r.nextInt(3);    //0 is shallow, 1-2 is deep
        if (outs == 2 || deep == 0) {
            event = p.name + " pops out";
            p.addAB();
            pitcher.addAB();
            outs++;
        }
        else {
            if (bases[2] != null && bases[2].speed > inField.getDefense()) {
                event = p.name + " hits sacrifice fly, " + bases[2].name + " scores";
                atBat.score++;
                outs++;
                if (bases[2].getEarned())
                    pitcher.addER();
                pitcher.addR();
                p.addRBI();
                p.addSAC();
                bases[2].addR();
                bases[2] = null;
                if (bases[1] != null && bases[1].speed > inField.getDefense() + 2) {
                    event = event + ", " + bases[1].name + " moves to third";
                    bases[2] = bases[1];
                    bases[1] = null;
                }
            }
            else if (bases[2] != null) {
                int run = r.nextInt(3); //if 0, out at plate
                if (run == 0) {
                    event = p.name + " flies out, " + bases[2].name + " out at the plate";
                    outs = outs + 2;
                    p.addAB();
                    pitcher.addAB();
                }
                else {
                    event = p.name + " hits sacrifice fly, " + bases[2].name + " scores";
                    atBat.score++;
                    outs++;
                    if (bases[2].getEarned())
                        pitcher.addER();
                    pitcher.addR();
                    p.addRBI();
                    p.addSAC();
                    bases[2].addR();
                    bases[2] = null;
                    if (bases[1] != null && bases[1].speed > inField.getDefense() + 2) {
                        event = event + ", " + bases[1].name + " moves to third";
                        bases[2] = bases[1];
                        bases[1] = null;
                    }
                }
            }
            else if (bases[1] != null && bases[1].speed > inField.getDefense() + 2) {
                event = p.name + " flies out, " + bases[1].name + " moves to third";
                outs++;
                bases[2] = bases[1];
                bases[1] = null;
                p.addAB();
                pitcher.addAB();
            }
            else {
                event = p.name + " flies out";
                outs++;
                p.addAB();
                pitcher.addAB();
            }
        }
        g.addEvent(event);
        return bases;
    }

    public Player[] error(Player p, Team atBat, Team inField, Pitcher pitcher, Player[] bases) {
        String event = new String();
        event = p.name + " reaches on error";
        p.addAB();
        p.setEarned(false);
        pitcher.addAB();
        inField.addError();
        if (outs == 2) {
            if (bases[2] != null) {
                event = event + ", " + bases[2].name + " scores";
                atBat.score++;
                pitcher.addR();
                bases[2].addR();
                bases[2] = null;
            }
            if (bases[1] != null) {
                event = event + ", " + bases[1].name + " to third";
                bases[2] = bases[1];
                bases[1] = null;
            }
            if (bases[0] != null) {
                event = event + ", " + bases[0].name + " to second";
                bases[1] = bases[0];
            }
        }
        else {
            if (bases[2] != null && bases[1] != null && bases[0] != null) {
                event = event + ", " + bases[2].name + " scores, " + bases[1].name + " to third, " + bases[0].name + " to second";
                atBat.score++;
                pitcher.addR();
                bases[2].addR();
                bases[2] = bases[1];
                bases[1] = bases[0];
            }
            else if (bases[1] != null && bases[0] != null) {
                event = event + ", " + bases[1].name + " to third, " + bases[0].name + " to second";
                bases[2] = bases[1];
                bases[1] = bases[0];
            }
            else if (bases[0] != null) {
                event = event + ", " + bases[0].name + " to second";
                bases[1] = bases[0];
            }
        }
        bases[0] = p;
        g.addEvent(event);
        return bases;
    }

    public int halfInning(int batInd, Team atBat, Team inField, Pitcher pitcher, Boolean botlast) {     //Returns next batter index
        outs = 0;
        Player[] bases = {null, null, null};
        //g.addRand(-1); For debugging purposes
        g.addBases(0);
        while ((!botlast && outs < 3) || (botlast && atBat.score <= inField.score && outs < 3)) {
            Player p = atBat.roster[batInd];
            p.setEarned(true);
            int x = r.nextInt(1020 - inField.getDefense());
            g.addRand(x);
            if (x < p.ab[0] + hP.getBAT() - 20)                //single
                bases = this.single(p, atBat, inField, pitcher, bases, botlast);
            else if (x < p.ab[1] + pitcher.getBAT() - 20)      //double
                bases = this.doub(p, atBat, inField, pitcher, bases, botlast);
            else if (x < p.ab[2] + pitcher.getBAT() - 20)      //triple
                bases = this.triple(p, atBat, inField, pitcher, bases, botlast);
            else if (x < p.ab[3] + pitcher.getBAT() - 20)      //home run
                bases = this.homeRun(p, atBat, pitcher, bases);
            else if (x < p.ab[4] + pitcher.getKBB() - 20)      //K
                bases = this.strikeOut(p, pitcher, bases);
            else if (x < p.ab[5] - 20)       //BB
                bases = this.walk(p, atBat, pitcher, bases);
            else if (x < p.ab[6] - 10)       //ground
                bases = this.ground(p, inField, pitcher, bases);
            else if (x < 1000)             //fly
                bases = this.flyOut(p, atBat, inField, pitcher, bases);
            else                        //error
                bases = this.error(p, atBat, inField, pitcher, bases);
            batInd++;
            if (batInd > 8)
                batInd = 0;

            //Add bases situation to gamelog
            int b;
            if (outs == 3 || (botlast && atBat.score > inField.score))
                b = 0;
            else {
                if (bases[2] != null)
                    if (bases[1] != null)
                        if (bases[0] != null)
                            b = 7;
                        else
                            b = 6;
                    else if (bases[0] != null)
                        b = 5;
                    else
                        b = 3;
                else if (bases[1] != null)
                    if (bases[0] != null)
                        b = 4;
                    else
                        b = 2;
                else if (bases[0] != null)
                    b = 1;
                else
                    b = 0;
            }
            g.addBases(b);
        }
        return batInd;
    }

    public void exportStats(Team t, int pitch) throws IOException{
        //Export batting stats
        for(int i = 0; i < 9; i++){
            XSSFRow r = t.statSheet.getRow(i+4);
            for(int k = 0; k < 10; k++) {
                int j = (int)r.getCell(k+2).getNumericCellValue();
                j = j + t.roster[i].gameStats[k];
                r.getCell(k+2).setCellValue(j);
            }
        }
        //Export pitchers stats
        XSSFRow r = t.statSheet.getRow(16+pitch);
        for(int i = 0; i < 8; i++){
            int j = (int)r.getCell(i+2).getNumericCellValue();
            j = j + t.getPitcher(pitch).gameStats[i];
            r.getCell(i+2).setCellValue(j);
        }
        if(t.getPitcher(pitch).gameStats[7] == 0){
            int j = (int)r.getCell(11).getNumericCellValue();
            j++;
            r.getCell(11).setCellValue(j);
        }
        t.wb.write(new FileOutputStream("XSSF Test WB.xlsx"));
    }
}
