/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package farkle;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author jterry
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private ImageView dice_one, dice_two, dice_three, dice_four, dice_five, dice_six;
    
    @FXML
    private Label main_label;
    
    @FXML
    private ListView<String> scoreboard;
    
    @FXML
    private Button main_button;
    
    private ArrayList<Integer> scores;
    ObservableList<String> scoreboardList;
    private boolean isNewGame;
    private boolean hasRolled;
    private boolean diceClickingEnabled;
    private Die[] dice;
    private EventHandler diceEventHandler;
    
    @FXML
    private void rollDice(ActionEvent event) {
        if(isNewGame)
            initializeScoreboard();
        if(getPointValueOfDice(true) != 0) {
            addToCurrentScore(getPointValueOfDice(true));
            saveAllDicePermanently();
        } else if(hasRolled) {
            return;
        }
        
        // Disable 'roll' button
        main_button.setText("Rolling...");
        main_button.setDisable(true);
        if(!hasRolled)
            scoreboardList.add(scoreboardList.size() - 2, "           0");
        hasRolled = true;
        
        // Check to see if all dice are saved
        if(allDiceAreSaved())
            resetAllDice();
        
        // 'Roll' the dice
        for(int i = 0; i < dice.length; ++i)
            if(!dice[i].isPermanentlySaved())
                dice[i].roll();
        
        if(getPointValueOfDice(false) == 0) {
            endTurn();
            main_label.setText("Farkle! Roll to continue");
        } else {
            main_label.setText("Nice roll! Click dice to save them");
            enableDiceClicking();
            main_button.setText("Roll again");
        }
        main_button.setDisable(false);
    }
    
    @FXML
    public void endTurn() {
        if(getPointValueOfDice(true) != 0) {
            addToCurrentScore(getPointValueOfDice(true));
            saveAllDicePermanently();
        }
        if(scores.get(scores.size() - 1) < 500) {
            scores.set(scores.size() - 1, 0);
            updateScoreboard();
            scores.add(0);
        } else {
            updateScoreboard();
            scores.add(0);
        }
        resetAllDice();
        disableDiceClicking();
        hasRolled = false;
        main_button.setText("Roll");
        main_label.setText("Roll to continue");
        int totalScore = scores.stream().mapToInt(x -> x).sum();
        if(totalScore >= 10000) {
            main_label.setText("You win! Roll to start a new game");
        }
    }
    
    @FXML
    public void clickAllDice() {
        if(diceClickingEnabled) {
            for(int i = 0; i < dice.length; ++i)
                if(!dice[i].isPermanentlySaved()) {
                    dice[i].click();
                }
            int tentativeScore = scores.get(scores.size() - 1) + getPointValueOfDice(true);
            updateScoreboard(tentativeScore);
        }
    }
    
    private void updateScoreboard(int latestScore) {
        scoreboardList.set(scoreboardList.size() - 3, padLeft("" + latestScore, 12));
        int totalScore = scores.subList(0, scores.size() - 1).stream().mapToInt(x -> x).sum() + latestScore;
        scoreboardList.set(scoreboardList.size() - 1, " Total: " + padLeft("" + totalScore, 4));
    }
    
    private void updateScoreboard() {
        updateScoreboard(scores.get(scores.size() - 1));
    }
    
    /*
     * I got this method from 'Realhowto' on StackOverflow
     */
    public static String padRight(String s, int n) {
     return String.format("%1$-" + n + "s", s);  
    }

    /*
     * I got this method from 'Realhowto' on StackOverflow
     */
    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);  
    }

    private void enableDiceClicking() {
        diceClickingEnabled = true;
        for(int i = 0; i < dice.length; ++i) {
            dice[i].getImageView().setCursor(Cursor.HAND);
            dice[i].getImageView().setOnMouseClicked(diceEventHandler);
        }
    }
    
    private void disableDiceClicking() {
        diceClickingEnabled = false;
        for(int i = 0; i < dice.length; ++i) {
            dice[i].getImageView().setCursor(Cursor.DEFAULT);
            dice[i].getImageView().setOnMouseClicked(null);
        }
    }
    
    private boolean allDiceAreSaved() {
        for(int i = 0; i < dice.length; ++i)
            if(!dice[i].isPermanentlySaved())
                return false;
        return true;
    }
    
    private void saveAllDicePermanently() {
        for(int i = 0; i < dice.length; ++i)
            if(dice[i].isTentativelySaved()) {
                dice[i].savePermanently();
                dice[i].resetTentativelySaved();
            }
    }
    
    private void resetAllDice() {
        for(int i = 0; i < dice.length; ++i) {
            dice[i].resetPermanentlySaved();
            dice[i].resetTentativelySaved();
            dice[i].resetClicked();
        }
    }
    
    private int[] getDiceNumbers(ArrayList<Die> diceToSave) {
        int[] result = new int[6];
        for(AtomicInteger i = new AtomicInteger(0); i.intValue() < result.length; i.incrementAndGet()) {
            result[i.intValue()] = (int) diceToSave
                    .stream()
                    .filter(x -> x.getValue() == i.intValue()).count();
        }
        
        return result;
    }
    
    /**
     * Determines whether or not the selected set of dice can be saved, according to the rules of Farkle.
     * @return 
     */
    private int getPointValueOfDice(boolean countOnlySelectedDice) {
        int pointValue = 0;
        
        ArrayList<Die> diceToSave = new ArrayList<>(6);
        for(int i = 0; i < dice.length; ++i) {
            if(dice[i].isTentativelySaved() || (!dice[i].isPermanentlySaved() && !countOnlySelectedDice))
                diceToSave.add(dice[i]);
        }
        
        if(diceToSave.isEmpty())
            return 0;
        
        int[] diceCounts = getDiceNumbers(diceToSave);
        
        // Straight
        int uniqueNumberCount = 0;
        for(int i = 0; i < diceCounts.length; ++i)
            if(diceCounts[i] > 0)
                ++uniqueNumberCount;
        if(uniqueNumberCount == 6)
            return 1500;
        
        // Two three-of-a-kinds
        int threeOfAKinds = 0;
        for(int i = 0; i < diceCounts.length; ++i)
            if(diceCounts[i] == 3)
                ++threeOfAKinds;
        if(threeOfAKinds == 2)
            return 2500;
        
        // Three pairs
        int pairs = 0;
        for(int i = 0; i < diceCounts.length; ++i)
            if(diceCounts[i] == 2)
                ++pairs;
        if(pairs == 3)
            return 1500;
        
        // Six of a kind
        boolean sixOfAKind = false;
        for(int i = 0; i < diceCounts.length; ++i)
            if(diceCounts[i] == 6) {
                sixOfAKind = true;
                break;
            }
        if(sixOfAKind)
            return 3000;
        
        // Five of a kind
        int fiveOfAKind = -1;
        for(int i = 0; i < diceCounts.length; ++i)
            if(diceCounts[i] == 5) {
                fiveOfAKind = i;
                break;
            }
        if(fiveOfAKind != -1) {
            pointValue += 2000;
            diceCounts[fiveOfAKind] = 0;
        }
        
        // Four of a kind
        int fourOfAKind = -1;
        boolean andAPair = false;
        for(int i = 0; i < diceCounts.length; ++i) {
            if(diceCounts[i] == 4)
                fourOfAKind = i;
            if(diceCounts[i] == 2)
                andAPair = true;
        }
        if(fourOfAKind != -1) {
            if(andAPair) {
                return 1500;
            } else {
                pointValue += 1000;
                diceCounts[fourOfAKind] = 0;
            }
        }
        
        // Three of a kind
        int threeOfAKind = -1;
        for(int i = 0; i < diceCounts.length; ++i)
            if(diceCounts[i] == 3) {
                threeOfAKind = i;
                break;
            }
        if(threeOfAKind != -1) {
            switch(threeOfAKind) {
            case 0:
                pointValue += 300;
                break;
            case 1:
                pointValue += 200;
                break;
            case 2:
                pointValue += 300;
                break;
            case 3:
                pointValue += 400;
                break;
            case 4:
                pointValue += 500;
                break;
            case 5:
                pointValue += 600;
                break;
            default:
                break;
            }
            diceCounts[threeOfAKind] = 0;
        }
        
        // Lonely 1s
        pointValue += diceCounts[0] * 100;
        diceCounts[0] = 0;
        
        // Lonely 5s
        pointValue += diceCounts[4] * 50;
        diceCounts[4] = 0;
        
        // Check for stragglers
        if(countOnlySelectedDice)
            for(int i = 0; i < diceCounts.length; ++i)
                if(diceCounts[i] > 0)
                    pointValue = 0;
        
        return pointValue;
    }
    
    private void addToCurrentScore(int score) {
        scores.set(scores.size() - 1, scores.get(scores.size() - 1) + score);
        updateScoreboard();
    }
    
    private void initializeDice() throws FileNotFoundException {
        ImageView[] diceImageViews = new ImageView[6];
        diceImageViews[0] = dice_one;
        diceImageViews[1] = dice_two;
        diceImageViews[2] = dice_three;
        diceImageViews[3] = dice_four;
        diceImageViews[4] = dice_five;
        diceImageViews[5] = dice_six;
        
        dice = new Die[6];
        for(int i = 0; i < dice.length; ++i)
            dice[i] = new Die(diceImageViews[i]);
    }
    
    private void initializeScoreboard() {
        scores = new ArrayList<>();
        scores.add(0);
        scoreboardList = FXCollections.observableArrayList(new ArrayList<String>());
        scoreboardList.add(" Scoreboard ");
        scoreboardList.add("-------------");
        scoreboardList.add("-------------");
        scoreboardList.add(" Total:    0");
        scoreboard.setItems(scoreboardList);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeDice();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        diceEventHandler = new DiceEventHandler();
        isNewGame = false;
        hasRolled = false;
        diceClickingEnabled = false;
        initializeScoreboard();
        main_button.setDefaultButton(true);
    }
    
    private class DiceEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            for(int i = 0; i < dice.length; ++i)
                if(event.getSource().equals(dice[i].getImageView())) {
                    dice[i].click();
                    int tentativeScore = scores.get(scores.size() - 1) + getPointValueOfDice(true);
                    updateScoreboard(tentativeScore);
                }
        }
    }
    
}
