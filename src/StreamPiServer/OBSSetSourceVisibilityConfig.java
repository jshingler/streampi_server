package StreamPiServer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import net.twasi.obsremotejava.Callback;
import net.twasi.obsremotejava.objects.Scene;
import net.twasi.obsremotejava.objects.Source;
import net.twasi.obsremotejava.requests.GetSceneList.GetSceneListResponse;
import net.twasi.obsremotejava.requests.ResponseBase;

public class OBSSetSourceVisibilityConfig extends Application implements Initializable{
    @FXML
    private JFXTextField actionCasualNameField;

    @FXML
    private JFXListView sceneOptionsListView;

    @FXML
    private JFXListView sourceOptionsListView;

    @FXML
    private JFXTextField iconPathField;

    @FXML
    private Label sourceSelectedNotPresentLabel;

    @FXML
    private ImageView iconPreviewImg;

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton deleteButton;

    @FXML
    private Label unableToConnectToOBSLabel;

    @FXML
    private Label headingLabel;

    @FXML
    private JFXToggleButton setVisibleToggle;


    Image previewImageDefault = new Image(getClass().getResourceAsStream("icons/icon_preview.png"));
    String txt;

    @Override
    public void start(Stage primaryStage) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //First get the list of actions from obsStudio

        sceneOptionsListView.setOnMouseClicked(event -> {
            getSourcesForSelectedScene("");
        });

        try
        {
            dashboardController.obsController.getScenes(new Callback() {
                @Override
                public void run(ResponseBase responseBase) {
                    GetSceneListResponse res = (GetSceneListResponse) responseBase;
                    List<Scene> scenes = res.getScenes();
                    for(Scene eachScene : scenes)
                    {
                        sceneOptionsListView.getItems().add(eachScene.getName());
                    }

                    if(dashboardController.actionConfigType == 2)
                    {
                        System.out.println("gadha");
                        for(int i = 0;i<dashboardController.actions.length;i++)
                        {
                            String eachAction[] = dashboardController.actions[i];
                            System.out.println(eachAction[0]+", "+dashboardController.selectedActionUniqueID);
                            if(eachAction[0].equals(dashboardController.selectedActionUniqueID))
                            {
                                actionCasualNameField.setText(eachAction[1]);
                                String[] xx = eachAction[3].split("<>");
                                String sceneName = xx[0];
                                String sourceName = xx[1];
                                String visibility = xx[2];

                                try
                                {
                                    System.out.println("asdxz");
                                    System.out.println(sceneOptionsListView.getItems().size());
                                    boolean isFound = false;
                                    for(int x = 0;x<sceneOptionsListView.getItems().size();x++)
                                    {
                                        if(sceneName.equals(sceneOptionsListView.getItems().get(x).toString()))
                                        {
                                            sceneOptionsListView.getSelectionModel().select(x);
                                            sceneOptionsListView.getFocusModel().focus(x);
                                            sceneOptionsListView.scrollTo(x);
                                            getSourcesForSelectedScene(sourceName);
                                            System.out.println(sourceName+", Xxacasdas"+sceneName+", "+visibility);
                                            isFound = true;
                                            break;
                                        }
                                    }
                                    if(!isFound)
                                    {
                                        Label red = new Label(sceneName);
                                        red.setTextFill(Paint.valueOf("#FF0000"));
                                        sceneOptionsListView.getItems().add(red);

                                        Label red2 = new Label(sourceName);
                                        red2.setTextFill(Paint.valueOf("#FF0000"));
                                        sourceOptionsListView.getItems().add(red2);

                                        addButton.setDisable(true);
                                        sourceSelectedNotPresentLabel.setVisible(true);
                                    }

                                    if(visibility.equals("1"))
                                        setVisibleToggle.setSelected(true);
                                    else if(visibility.equals("0"))
                                        setVisibleToggle.setSelected(false);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                                Image icon = dashboardController.icons.get(eachAction[4]);
                                iconPreviewImg.setImage(icon);
                                break;
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e)
        {
            unableToConnectToOBSLabel.setVisible(true);
            addButton.setDisable(true);
            e.printStackTrace();
            for(int i = 0;i<dashboardController.actions.length;i++)
            {
                String eachAction[] = dashboardController.actions[i];
                System.out.println(eachAction[0]+", "+dashboardController.selectedActionUniqueID);
                if(eachAction[0].equals(dashboardController.selectedActionUniqueID))
                {
                    actionCasualNameField.setText(eachAction[1]);
                    String selectedScene = eachAction[3];
                    sceneOptionsListView.getItems().add(selectedScene);
                    sceneOptionsListView.setDisable(true);
                    Image icon = dashboardController.icons.get(eachAction[4]);
                    iconPreviewImg.setImage(icon);
                    break;
                }
            }
        }

        if(dashboardController.actionConfigType == 2)
        {
            deleteButton.setDisable(false);
            deleteButton.setVisible(true);
            isImageFileOK = true;
            addButton.setText("Apply Changes");
            headingLabel.setText("Modify OBS Studio (Set Source Visibility) Action");
        }
    }

    int x;
    private void getSourcesForSelectedScene(String s)
    {
        sourceOptionsListView.getItems().clear();
        String currentSelectedScene = sceneOptionsListView.getSelectionModel().getSelectedItem().toString();

        dashboardController.obsController.getScenes(responseBase -> {
            GetSceneListResponse scenes = (GetSceneListResponse) responseBase;
            for(Scene eachScene : scenes.getScenes())
            {
                if(eachScene.getName().equals(currentSelectedScene))
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            for(Source eachSource : eachScene.getSources())
                            {
                                sourceOptionsListView.getItems().add(eachSource.getName());
                            }

                            System.out.println("aa ");

                            for(x = 0;x<sourceOptionsListView.getItems().size();x++)
                            {
                                System.out.println(s);
                                System.out.println(sourceOptionsListView.getItems().get(x).toString()+"asxz");
                                if(s.equals(sourceOptionsListView.getItems().get(x).toString()))
                                {
                                    System.out.println("axzxc");
                                    sourceOptionsListView.getSelectionModel().select(x);
                                    sourceOptionsListView.getFocusModel().focus(x);
                                    sourceOptionsListView.scrollTo(x);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    Random r = new Random();

    @FXML
    public void deleteButtonClicked()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Main.dc.showProgress("Removing requested Action ");
                        }
                    });

                    for(String[] eachAction : dashboardController.actions)
                    {
                        if(eachAction[0].equals(dashboardController.selectedActionUniqueID))
                        {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    HBox row = (HBox) Main.dc.controlVBox.getChildren().get(Integer.parseInt(eachAction[5]));
                                    Pane ac = (Pane) row.getChildren().get(Integer.parseInt(eachAction[6]));
                                    ac.getChildren().clear();
                                    ac.setId("freeAction_"+eachAction[5]+"_"+eachAction[6]);
                                    ac.getStyleClass().add("action_box");
                                }
                            });
                            System.out.println("Removed from Server...");
                            Main.dc.writeToOS("delete_action::"+dashboardController.selectedActionUniqueID+"::"+eachAction[4]);
                            System.out.println("Removed from Client!");
                            break;
                        }
                    }

                    int x2 = 0;
                    String[][] newActions = new String[dashboardController.actions.length-1][8];
                    for(int x1 = 0;x1<dashboardController.actions.length;x1++)
                    {
                        String[] eachAction = dashboardController.actions[x1];
                        if(eachAction[0].equals(dashboardController.selectedActionUniqueID))
                            continue;
                        else
                        {
                            newActions[x2][0] = eachAction[0];
                            newActions[x2][1] = eachAction[1];
                            newActions[x2][2] = eachAction[2];
                            newActions[x2][3] = eachAction[3];
                            newActions[x2][4] = eachAction[4];
                            newActions[x2][5] = eachAction[5];
                            newActions[x2][6] = eachAction[6];
                            newActions[x2][7] = eachAction[7];
                            x2++;
                        }
                    }

                    dashboardController.actions = newActions;


                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Main.dc.hideProgress();
                            Main.dc.hideNewActionConfigDialog();
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }

    @FXML
    public void cancelButtonClicked()
    {
        Main.dc.newActionConfigDialog.close();
    }

    int i;
    String selectedSceneName;
    String selectedSourceName;
    String selectedVisibilityChoice;
    @FXML
    public void addButtonClicked()
    {
        String actionCasualName = actionCasualNameField.getText();

        try
        {
            selectedSceneName = sceneOptionsListView.getSelectionModel().getSelectedItem().toString();
        }
        catch (NullPointerException e)
        {
            selectedSceneName = null;
        }

        try
        {
            selectedSourceName = sourceOptionsListView.getSelectionModel().getSelectedItem().toString();
        }
        catch (NullPointerException e)
        {
            selectedSourceName = null;
        }

        System.out.println(selectedSceneName);
        String iconPath = iconPathField.getText();

        StringBuilder errors = new StringBuilder("Please correct and resolve the following errors :\n");
        boolean isError = false;

        if(actionCasualName.length() == 0)
        {
            errors.append("Invalid Action Name Entered\n");
            isError = true;
        }

        System.out.println(selectedSceneName);
        System.out.println(selectedSourceName);

        if(selectedSceneName == null)
        {
            errors.append("Please select any Scene\n");
            isError = true;
        }

        if(selectedSourceName == null)
        {
            errors.append("Please select any Source\n");
            isError = true;
        }

        if(!isImageFileOK)
        {
            errors.append("Invalid Action Icon\n");
            isError = true;
        }

        if(isError)
        {
            Main.dc.showErrorAlert("Error!",errors.toString());
            return;
        }

        if(setVisibleToggle.isSelected())
            selectedVisibilityChoice = "1";
        else
            selectedVisibilityChoice = "0";

        //Good to go!

        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Main.dc.showProgress("Updating actions... ");
                        }
                    });

                    if(dashboardController.actionConfigType == 1)
                    {
                        String newFileName = "icon_"+r.nextInt(30000);
                        File newFile = new File(selectedIconFile.getPath().replace(selectedIconFile.getName(),"")+"/"+newFileName);
                        Files.copy(selectedIconFile.toPath(), newFile.toPath());
                        //send icon to client ...
                        FileInputStream fs = new FileInputStream(newFile.getAbsolutePath());
                        byte[] imageB = fs.readAllBytes();
                        fs.close();
                        String base64EncryptedIcon = Base64.getEncoder().encodeToString(imageB);

                        String iconName = newFile.getName();

                        Main.dc.writeToOS("update_icon::"+iconName+"::"+base64EncryptedIcon+"::");
                        newFile.delete();

                        //first update local actions....
                        String[][] oldActions = new String[Main.dc.actions.length+1][8];

                        for(i = 0;i<Main.dc.actions.length;i++)
                        {
                            oldActions[i] = Main.dc.actions[i];
                        }

                        oldActions[i][0] = generateRandomID();
                        oldActions[i][1] = actionCasualName;
                        oldActions[i][2] = "obs_set_source_visibility";
                        String toWrite = selectedSceneName+"<>"+selectedSourceName+"<>"+selectedVisibilityChoice+"<>";
                        oldActions[i][3] = toWrite;
                        //oldActions[i][4] = selectedIconFile.getName();
                        oldActions[i][4] = newFileName;
                        oldActions[i][5] = Main.dc.selectedRow+"";
                        oldActions[i][6] = Main.dc.selectedCol+"";
                        oldActions[i][7] = dashboardController.currentLayer+"";

                        Main.dc.actions = oldActions;


                        Main.dc.icons.put(oldActions[i][4],previewIcon);


                        ImageView icon = new ImageView();
                        icon.setImage(Main.dc.icons.get(Main.dc.actions[i][4]));
                        icon.setFitHeight(100);
                        icon.setFitWidth(100);


                        HBox row = (HBox) Main.dc.controlVBox.getChildren().get(Main.dc.selectedRow);

                        Pane ac = (Pane) row.getChildren().get(Main.dc.selectedCol);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ac.getStyleClass().remove("action_box_highlight");
                                ac.getChildren().add(icon);
                                //actionPane.setStyle("-fx-effect: dropshadow(three-pass-box, "+actions[i][4]+", 5, 0, 0, 0);-fx-background-color:#212121");
                                ac.setId("allocatedaction_"+oldActions[i][5]+"_"+oldActions[i][6]+"_"+oldActions[i][0]);
                            }
                        });

                    }
                    else if(dashboardController.actionConfigType == 2)
                    {
                        String newFileName = "icon_"+r.nextInt(30000);
                        if(iconPathField.getText().length()>0)
                        {
                            File newFile = new File(selectedIconFile.getPath().replace(selectedIconFile.getName(),"")+"/"+newFileName);
                            Files.copy(selectedIconFile.toPath(), newFile.toPath());
                            //send icon to client ...
                            FileInputStream fs = new FileInputStream(newFile.getAbsolutePath());
                            byte[] imageB = fs.readAllBytes();
                            String base64EncryptedIcon = Base64.getEncoder().encodeToString(imageB);

                            String iconName = newFile.getName();

                            Main.dc.writeToOS("update_icon::"+iconName+"::"+base64EncryptedIcon+"::");
                        }

                        //first update local actions....
                        String[][] oldActions = dashboardController.actions;

                        for(i = 0;i<dashboardController.actions.length;i++)
                        {
                            if(dashboardController.actions[i][0].equals(dashboardController.selectedActionUniqueID))
                            {
                                oldActions[i][0] = generateRandomID();
                                oldActions[i][1] = actionCasualName;
                                oldActions[i][2] = "obs_set_source_visibility";
                                String toWrite = selectedSceneName+"<>"+selectedSourceName+"<>"+selectedVisibilityChoice+"<>";;
                                oldActions[i][3] = toWrite;
                                //oldActions[i][4] = selectedIconFile.getName();
                                if(iconPathField.getText().length()>0)
                                {
                                    oldActions[i][4] = newFileName;
                                }
                                oldActions[i][5] = dashboardController.selectedRow+"";
                                oldActions[i][6] = dashboardController.selectedCol+"";
                                oldActions[i][7] = dashboardController.currentLayer+"";
                                System.out.println("YAAY");
                                break;
                            }
                        }

                        dashboardController.actions = oldActions;


                        if(iconPathField.getText().length()>0)
                        {
                            dashboardController.icons.put(oldActions[i][4],previewIcon);
                        }

                        ImageView icon = new ImageView();
                        icon.setImage(dashboardController.icons.get(dashboardController.actions[i][4]));
                        icon.setFitHeight(100);
                        icon.setFitWidth(100);


                        HBox row = (HBox) Main.dc.controlVBox.getChildren().get(dashboardController.selectedRow);

                        Pane ac = (Pane) row.getChildren().get(dashboardController.selectedCol);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ac.getStyleClass().remove("action_box_highlight");
                                ac.getChildren().clear();
                                ac.getChildren().add(icon);
                                //actionPane.setStyle("-fx-effect: dropshadow(three-pass-box, "+actions[i][4]+", 5, 0, 0, 0);-fx-background-color:#212121");
                                ac.setId("allocatedaction_"+oldActions[i][5]+"_"+oldActions[i][6]+"_"+oldActions[i][0]);
                            }
                        });


                    }

                    Thread.sleep(1000);

                    String towrite = "actions_update::"+dashboardController.actions.length+"::";

                    for(String[] eachAction : dashboardController.actions)
                    {
                        //FileInputStream fs = new FileInputStream("actions/icons/"+eachAction[3]);
                        //byte[] imageB = fs.readAllBytes();
                        //fs.close();
                        //String base64Image = Base64.getEncoder().encodeToString(imageB);
                        towrite+=eachAction[0]+"__"+eachAction[1]+"__"+eachAction[2]+"__"+eachAction[3]+"__"+eachAction[4]+"__"+eachAction[5]+"__"+eachAction[6]+"__"+eachAction[7]+"::";
                    }

                    System.out.println(towrite);
                    Main.dc.writeToOS(towrite);



                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Main.dc.hideProgress();
                            Main.dc.hideNewActionConfigDialog();
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();

    }

    boolean isImageFileOK = false;
    File selectedIconFile;
    Image previewIcon;

    @FXML
    public void browseButtonClicked()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG","*.png"), new FileChooser.ExtensionFilter("GIF","*.gif"));
        selectedIconFile = fileChooser.showOpenDialog(Main.ps);

        if(selectedIconFile == null)
        {
            isImageFileOK = false;
            iconPathField.setText("");
            iconPreviewImg.setImage(previewImageDefault);
        }

        try
        {
            previewIcon = new Image(selectedIconFile.toURI().toString());
            iconPreviewImg.setImage(previewIcon);
            iconPathField.setText(selectedIconFile.getAbsolutePath());
            isImageFileOK = true;
        }
        catch (Exception e)
        {
            isImageFileOK = false;
            e.printStackTrace();
            Main.dc.showErrorAlert("Uh Oh!","It seems that the Icon you selected is invalid!");
            iconPreviewImg.setImage(previewImageDefault);
            iconPathField.setText("");
        }
    }

    public String generateRandomID() {
        Random r = new Random();
        return "action_"+r.nextInt((15000 - 1) + 1) + 1;
    }

    @FXML
    public void openElgatoStreamDeckKeyCreator()
    {
        getHostServices().showDocument("https://www.elgato.com/en/gaming/keycreator");
    }
}
