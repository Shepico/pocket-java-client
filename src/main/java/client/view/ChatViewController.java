package client.view;

import client.Main;
import client.controller.ClientController;
import client.utils.Common;
import client.utils.CustomTextArea;
import client.utils.Sound;
import client.view.customFX.*;
import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import database.dao.DataBaseService;
import database.entity.Message;
import database.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ChatViewController implements Initializable {

    private static ChatViewController instance;


    @FXML
    private BorderPane borderPaneMain;
    @FXML
    private AnchorPane messagePanel;

    @FXML
    private WebView messageWebView;

    @FXML
    private JFXListView<CFXListElement> contactListView;

    @FXML
    private CustomTextArea messageField;

    @FXML
    private Tab chats;

    @FXML
    private Tab contacts;

    @FXML
    private AnchorPane userSearchPane;

    @FXML
    private JFXButton bAddContact;

    @FXML
    private AnchorPane contactsViewPane;

    @FXML
    private AnchorPane groupSearchPane;
    @FXML
    private AnchorPane groupListPane;

    @FXML
    private AnchorPane groupNewPane;

    @FXML
    private AnchorPane contactSearchPane;

    @FXML
    private ScrollPane profileScrollPane;

    @FXML
    private JFXListView<CFXListElement> listViewAddToGroup;

    @FXML
    private Menu menuLeff;

    @FXML
    private JFXHamburger hamburger;

    @FXML
    private JFXTextField groupName;

    @FXML
    private JFXTextField creategroupName;

    @FXML
    private CFXMyProfile myProfile;

    @FXML
    private JFXTextField tfSearchInput;

    @FXML
    private JFXTextField userSearchText;

    @FXML
    private JFXTabPane tabPane;
    //
    private WebEngine webEngine;

    private ObservableList<CFXListElement> contactsObservList;

    private ClientController clientController;

    private String backgroundImage;

    private Document DOMdocument;

    private String tsOld;

    private int idDivMsg;

    @FXML
    private  JFXButton btnContactSearchCancel;

    @FXML
    private JFXButton btnContactSearchInvite;

    @FXML
    private JFXListView<CFXListElement> searchList;
    private ObservableList<CFXListElement> searchObsList;

    @FXML
    private CFXMenuLeft cfxMenuLeft;

    @FXML
    private CFXMenuRightGroup cfxMenuRightGroup;

    private void initListenersToButtons(){
        btnContactSearchCancel.setOnAction(event -> contactSearchButtonCancelClicked());
        btnContactSearchInvite.setOnAction(event -> contactSearchButtonInviteClicked());

    }

    public static ChatViewController getInstance() {
        return instance;
    }

    private SingleSelectionModel<Tab> selectionModel;
    //ссылка на desktop
    private Desktop desktop;
    ////////////////////////
    HamburgerBasicCloseTransition transition;
    HamburgerBackArrowBasicTransition transitionBack;

    public ChatViewController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DOMdocument = null;
        tsOld = null; //чистка даты
        idDivMsg = 0; //присваивание ID

        webEngine = messageWebView.getEngine(); //инициализация WebEngine
        initBackgroundWebView();
        initWebView();

        clientController = ClientController.getInstance();
        clientController.setChatViewController(this);
        contactsObservList = FXCollections.observableList(ClientController.getInstance().getContactListOfCards());
        contactListView.setExpanded(true);
        searchObsList = FXCollections.observableList(new ArrayList<CFXListElement>());
        searchList.setExpanded(true);
        fillContactListView();

        desktop = Desktop.getDesktop();

        messageField.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().equals(KeyCode.ENTER)) {
                String text = messageField.getText().trim();
                if (!text.isEmpty()) {
//                    messageField.appendText(System.lineSeparator());
                    clientController.sendMessage(messageField.getText());
                    messageField.clear();
                    messageField.requestFocus();
                }
                event.consume();
            }
        });
        transition = new HamburgerBasicCloseTransition(hamburger);
        transitionBack = new HamburgerBackArrowBasicTransition(hamburger);
        PaneProvider.setTransitionBack(transitionBack);
         selectionModel=tabPane.getSelectionModel();
         initListenersToButtons();
         instance=this;
         CFXMenuLeft.setParentController(instance);
         PaneProvider.setBorderPaneMain(borderPaneMain);

    }


    //  инициализация картинки backgrounda
    private void initBackgroundWebView() {
        String path = "client/images/chat-bg.jpg"; //картинка фона
        ClassLoader cl = this.getClass().getClassLoader();
        backgroundImage = "";
        try {
            backgroundImage = cl.getResource(path).toURI().toString();
        }catch (Exception e) {
            //todo перенести в логирование
            e.printStackTrace();
        }
    }

    // инициализация только HTML в WebView.
    private void initWebView() {
        webEngine.loadContent(
                "<!DOCTYPE html> \n"+
                "<html lang=\"en\"> \n"+
                  "<head> \n"+
                    "<meta charset=UTF-8> \n"+
                    "<style> \n"+
                        "body { \n" +
                            "margin: 0; \n"+
                            "padding: 10px; \n"+
                            "background-image: url(" + backgroundImage + "); \n"+
                            "background-attachment: fixed; \n"+
                        "} \n"+
                        //общие стили
                        //time day
                        ".timeStampDay { \n" +
                            "display: inline-block; \n"+
                            "text-align: center; \n"+
                            //"width: 80px; \n"+
                            "margin: 0 38%;  \n"+
                            "margin-top: 10px;  \n"+
                            "color: #55635A; \n"+
                            "background: #BCDCC9; \n"+
                            "border-radius: 10px; \n"+
                            "padding: 5px 10px; \n"+
                        "} \n"+
                        //
                        ".message { \n"+
                            "display: flex; \n"+
                            "width: 0px; \n"+
                            "align-items: center; \n"+
                            "margin-left: 10px; \n"+
                            "margin-right: 10px; \n"+
                            "margin-top: 10px; \n"+
                            "margin-bottom: 30px; \n"+
                        "} \n"+
                        //div Logo
                        ".msgLogo { \n"+
                            "flex: none; \n"+
                            "align-self: start; \n"+
                            "width: 33px; \n"+
                            "height: 33px; \n"+
                            "background: lightgrey; \n"+
                            "border-radius: 50%; \n"+
                        "} \n"+
                        //div text, 1->2
                        ".msgTxt { \n"+
                            "display: flex \n"+
                            "flex-direction: column; \n"+
                            "flex: auto; \n"+
                            "max-width: 400px; \n"+
                            "min-width: 200px; \n"+
                            "width: 300px; \n"+
                            "border-radius: 20px; \n"+
                            "margin-left: 10px; \n"+
                            "margin-right: 10px; \n"+
                            "padding: 16px; \n"+
                            "box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.15); \n"+
                        "} \n"+
                        //div time
                        ".msgTime { \n"+
                            "flex: auto; \n"+
                        "} \n"+

                        //div msgTxt --> sender
                        ".myUserClass { \n"+
                            "background: #C6FCFF; \n"+
                        "} \n"+"" +
                        ".senderUserClass { \n"+
                            "background: #FFFFFF; \n"+
                        "} \n"+

                        //div text --> div sender
                        ".myUserClassS{ \n"+
                            "display: none; \n"+ //Отправителя себя не отображаем
                        "} \n"+

                        ".senderUserClassS{ \n"+
                            "word-wrap: break-word; \n"+    //<!--Перенос слов-->
                            "color: #1EA362; \n"+
                        "} \n"+

                        //div text --> div msg
                        ".msg { \n"+
                            "width: auto; \n"+
                            "word-wrap: break-word; \n"+    //<!--Перенос слов-->
                        "} \n"+

                        //div time -->sender
                        ".myUserClassT { \n"+
                            "color: #757575; \n"+
                        "} \n"+
                        ".senderUserClassT { \n"+
                            "color: #4285F4; \n"+
                        "} \n"+
                    "</style> \n"+
                  "</head> \n"+
                  "<body></body> \n"+
                "</html> \n");
    }

    public void fillContactListView() {
        contactListView.setItems(contactsObservList);
        //contactsObservList.addAll(clientController.getContactListOfCards());
        for (CFXListElement element:contactsObservList){
            /*User myUser = clientController.getMyUser();
            User otherUser = element.getUser();
            DataBaseService dbService = new DataBaseService(myUser);
            Message message = dbService.getLastMessage(otherUser, myUser);*/
            element.setUnreadMessages("0");
            element.setBody("Входящие сообщения");
            //element.setBody(message.getText());

        }
    }

    //  инициализация картинки аватара
    //if sex = true, is a woman
    //   sex = false, is a man
    private String initAvatar(boolean sex) {
        String path = "";
        if (sex) {
            path = "client/images/defaultAvatar/girl.png"; //картинка фона
        }else {
            path = "client/images/defaultAvatar/man.png"; //картинка фона
        }
        ClassLoader cl = this.getClass().getClassLoader();
        String avatar = "";
        try {
            avatar = cl.getResource(path).toURI().toString();
        }catch (Exception e) {
            //todo перенести в логирование
            e.printStackTrace();
        }
        return avatar;
    }

    /**
     *
     * @param pattern
     * @return
     * Устанавливаем формат даты
     */
    private SimpleDateFormat initDateFormat(String pattern){
        return new SimpleDateFormat(pattern);
    }

    /**
     *
     * @param message
     * @param senderName
     * @param timestamp     *
     * @param attrClass
     * ****
     * /* Create module DIV for messenger
     * <div class="timeStampDay"></div>
         * <div class="message">
             * <div class="msgLogo"></div>
             * <div class="attrClass msgTxt">
     *          <div class="'attrClass+S' sender"></div>
     *          <div class="'attrClass+M' msg">
     *              <Если ссылка то
     *              <a href=ссылка></a>
     *          </div>
     *        </div>
         * </div>
         * <div class="'attrClass+T' msgTime"></div>
     * </div>
     * Style create in initWebView
     *
     */
    private void createMessageDiv(String message, String senderName, Timestamp timestamp, String attrClass){
        //ID требуется для скрипта вставки тегов
        idDivMsg+=1;
        String idMsg = "msg"+idDivMsg;
        //получаем аватар
        //тут по идеи подбор по полу. Оставляю чтобы было понятно куда вставляется и настроить стили
        String avatar = initAvatar(false); //man
        String styleStr = "background-image: url(" + avatar + "); background-size: cover";
        //

        SimpleDateFormat dateFormatDay = initDateFormat("d MMMM");
        SimpleDateFormat dateFormat = initDateFormat("HH:mm");

        //Заменяем Enter на перенос строки, для отображения
        message = message.replaceAll("\n", "<br/>");
        //Парсим ссылки, получаем строку вида <a href="message">message</a>
        message = Common.urlToHyperlink(message);

        boolean visibleDateDay=false;
        if (tsOld == null) {
            tsOld = dateFormatDay.format(timestamp);
            visibleDateDay = true;
        }else if (!tsOld.equals(dateFormatDay.format(timestamp))) {
            tsOld = dateFormatDay.format(timestamp);
            visibleDateDay = true;
        }

        Node body = DOMdocument.getElementsByTagName("body").item(0);

        if (visibleDateDay) {
            Element divTimeDay = DOMdocument.createElement("div");
            divTimeDay.setAttribute("class", "timeStampDay");
            divTimeDay.setTextContent(dateFormatDay.format(timestamp));
            body.appendChild(divTimeDay);
        }
        Element div = DOMdocument.createElement("div");
        Element divLogo = DOMdocument.createElement("div");
        Element divTxt = DOMdocument.createElement("div");
        Element divTxtSender = DOMdocument.createElement("div");
        Element divTxtMsg = DOMdocument.createElement("div");
        Element divTime = DOMdocument.createElement("div");
        div.setAttribute("class", "message");
        divLogo.setAttribute("class", "msgLogo");
        divLogo.setAttribute("style", styleStr);
        divTxt.setAttribute("class", attrClass+" msgTxt");
        divTxtSender.setAttribute("class", attrClass+"S sender");
        divTxtMsg.setAttribute("class", attrClass+"M msg");
        divTxtMsg.setAttribute("id", idMsg); //id
        divTime.setAttribute("class", attrClass+"T msgTime");
        divTxtSender.setTextContent(senderName);
        divTxtMsg.setTextContent(message);
        divTime.setTextContent(dateFormat.format(timestamp));
        div.appendChild(divLogo);
        divTxt.appendChild(divTxtSender);
        divTxt.appendChild(divTxtMsg);
        div.appendChild(divTxt);
        div.appendChild(divTime);
        body.appendChild(div);
        //Scripts
        //вставляем текст с тегами
        webEngine.executeScript("document.getElementById(\"" + idMsg + "\").innerHTML = '" + message+"'");
        //Сдвигаем страницу на последний элемент
        webEngine.executeScript("document.body.scrollTop = document.body.scrollHeight");
        //Подписка на событие по открытию ссылки
        addListenerLinkExternalBrowser(divTxtMsg);
    }

    public void showMessage(String senderName, String message, Timestamp timestamp, boolean isNew) {
        /*if (isNew) {
            Sound.playSoundNewMessage().join();
        }*/

        String attrClass;
        if (clientController.getSenderName().equals(senderName)) {
            attrClass = "myUserClass";
        } else {
            attrClass = "senderUserClass";
        }

        //todo по хорошему надо переместить подписку на событие в другое место
        //Подписка на событие загрузки документа HTML in WebView
        if (DOMdocument == null) {
            //если пользователь только запустил клиента и локально нет ни одного сообщения
            if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                DOMdocument = webEngine.getDocument();
                createMessageDiv(message, senderName, timestamp, attrClass);
                updateLastMessageInCardsBody(message, senderName);
            }else {
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        DOMdocument = webEngine.getDocument(); // Должен быть здесь т.к. загрузка WebEngine только произошла
                        createMessageDiv(message, senderName, timestamp, attrClass);
                        updateLastMessageInCardsBody(message, senderName);
                    }
                });
            }
        }else {
            createMessageDiv(message, senderName, timestamp, attrClass);
            updateLastMessageInCardsBody(message, senderName);
        }
    }

    private void updateLastMessageInCardsBody(String message, String senderName){
        CFXListElement targetChat = null;

        for (CFXListElement element : contactsObservList){
            if (element.getUser().getAccount_name().equals(senderName)) targetChat = element;
        }
        if (targetChat == null) return; //TODO определить вероятность и доделать (вывод ошибки пользователю, лог)
        targetChat.setBody(message);
    }

    public void addNewUserToContacts(CFXListElement newUser) {
        contactsObservList.add(newUser);
    }

    @FXML
    private void handleDisconnectButton() {
        Stage stage = (Stage) messagePanel.getScene().getWindow();
        stage.close();
        clientController.disconnect();
        Tray.currentStage = null;
        Main.initRootLayout();
        Main.showOverview();
    }

    @FXML
    private void handleExit() {
        clientController.disconnect();
        System.exit(0);
    }

    @FXML
    private void handleSendMessage() {
        if (!messageField.getText().isEmpty()) {
            clientController.sendMessage(messageField.getText());
            messageField.clear();
            messageField.requestFocus();
        }
    }

    @FXML
    private void handleClientChoice(MouseEvent event) {
        if (event.getClickCount() == 1) {
            String receiver = contactListView.getSelectionModel().getSelectedItem().getTopic();
            //showAlert("Сообщения будут отправляться контакту " + receiver, Alert.AlertType.INFORMATION);
            clientController.setReceiver(receiver);
        }

        messageField.requestFocus();
        messageField.selectEnd();
    }

    @FXML
    private void handleAddContactButton() {
        contactListView.setVisible(false);
        bAddContact.setVisible(false);
        userSearchPane.setVisible(true);
        userSearchPane.setFocusTraversable(true);

//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/fxml/AddContactView.fxml"));
//        Parent root = fxmlLoader.load();
//        Stage stage = new Stage();
//        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.setTitle("Add contact");
//        stage.setResizable(false);
//        stage.setScene(new Scene(root));
//        stage.show();

    }
    @FXML
    private void onUserSearchButtonClicked(){
        clientController.addContact(userSearchText.getText());
        userSearchText.clear();
        bAddContact.setVisible(true);
        contactListView.setVisible(true);
        userSearchPane.setVisible(false);
    }

    //подписка на обработку открытия ссылок
    //Element tagElement = <div class="msg">
    private void addListenerLinkExternalBrowser(Element tagElement){
        NodeList nodeList = tagElement.getElementsByTagName("a");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ((EventTarget) nodeList.item(i)).addEventListener("click", listenerLinkExternalBrowser(), false);

        }
    }

    //обработчик открытия ссылок во внешнем браузере
    private EventListener listenerLinkExternalBrowser(){
        EventListener listener = new EventListener() {

            @Override
            public void handleEvent(Event evt) {
                String domEventType = evt.getType();
                if ("click".equals(domEventType)) {
                    String href = ((Element) evt.getTarget()).getAttribute("href");
                    try {
                        // Open URL in Browser:
                        //ну удалил, т.к. не много не понятно пока зачем
                        //if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            desktop.browse(new URI(href.contains("://") ? href : "http://" + href + "/"));
                            //отменяем событие, чтобы ссылка не открывалась в самом webView
                            evt.preventDefault();
                        /*} else {
                            System.out.println("Could not load URL: " + href);
                        }*/
                    } catch (IOException | URISyntaxException e) {
                        //todo logger
                        e.printStackTrace();
                    }
                }
            }
        };
        return listener;
    }

    @FXML
    public void handleSendFile() {
        Stage stage = (Stage) messagePanel.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                this.desktop.open(file);//открывается файл на компьютере
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<File> files = Arrays.asList(file);
            if (files == null || files.isEmpty()) return;
            for(File f : files) {
                messageField .appendText(f.getAbsolutePath() + "\n");
            }
        }
    }
    //метод добавления смайликов
    public void handleSendSmile(MouseEvent mouseEvent) {
    }

    /**
     * Вызывается для чистки документа внутри WebEngine
     * при первом вызове чистки нет, т.к. DOMdocument == null
     * так де обнуляем дату для группировки (tsOld) и ID для DIV
     */
    public void clearMessageWebView() {
        if (DOMdocument != null) {
            //чистим все, что внутри тегов <body></body>
            Node body = DOMdocument.getElementsByTagName("body").item(0);
            Node fc = body.getFirstChild();
            while (fc != null) {
                body.removeChild(fc);
                fc = body.getFirstChild();
            }
        }

        tsOld = null; //чистка даты
        idDivMsg =0; //присваивание ID
    }

    //метод смены иконки
    public void handleOnChatSelected() {
        chats.setGraphic(buildImage("/client/images/chat/chatsActive.png"));
        if (contacts != null) {
            contacts.setGraphic(buildImage("/client/images/chat/contacts.png"));
            contacts.setStyle("-fx-border-width: 0 0 5 0; " +
                    "          -fx-border-color: #3498DB #3498DB transparent #3498DB;" +
                    "-fx-border-insets: 0;" +
                    "          -fx-border-style: solid;");
        }
        chats.setStyle("-fx-border-width: 0 0 5 0; " +
                        "-fx-border-color: transparent transparent #F8D57D transparent;" +
                "-fx-border-insets: 0;" +
                        "-fx-border-style: solid;");
    }
    public void handleOnContactSelected() {
        contacts.setGraphic(buildImage("/client/images/chat/contactsActive.png"));
        chats.setGraphic(buildImage("/client/images/chat/chats.png"));
        contacts.setStyle("-fx-border-width: 0 0 5 0; " +
                "-fx-border-color: transparent transparent #F8D57D transparent;" +
                "-fx-border-insets: 0;" +
                "-fx-border-style: solid;");
        chats.setStyle("-fx-border-width: 0 0 5 0; " +
                "       -fx-border-color: #3498DB #3498DB transparent #3498DB;" +
                "-fx-border-insets: 0;" +
                "       -fx-border-style: solid;");
    }

    private ImageView buildImage(String s) {
        Image i = new Image(s);
        ImageView imageView = new ImageView();
        imageView.setImage(i);
        return imageView;
    }

    public void onGroupSearchButtonClicked(ActionEvent actionEvent) {
        groupSearchPane.setVisible(false);
    }

    public void handleGroupSearchButton(MouseEvent mouseEvent) {
        groupListPane.setVisible(false);
        groupSearchPane.setVisible(true);
    }

    public void handleGroupNewButton(MouseEvent mouseEvent) {

        groupListPane.setVisible(false);
    }

    public void onGroupSearchCancelButtonPressed(ActionEvent actionEvent) {
        groupSearchPane.setVisible(false);
        groupListPane.setVisible(true);
    }

    public void onSearchGroupButtonClicked(ActionEvent actionEvent) {
        groupListPane.setVisible(false);
        groupSearchPane.setVisible(true);
    }

    public void onNewGroupClicked(ActionEvent actionEvent) {
        selectionModel.select(0);
        cfxMenuLeft.setVisible(false);
        menuLeff.hide();
        groupListPane.setVisible(false);
        listViewAddToGroup.setExpanded(true);
        groupNewPane.setVisible(true);
    }

    public void onGroupNewCancelButtonPressed(ActionEvent actionEvent) {
        groupNewPane.setVisible(false);
        groupListPane.setVisible(true);
    }

    public void onMyProfileOpen(ActionEvent actionEvent) {
        PaneProvider.setMyProfileScrollPane(profileScrollPane);
        cfxMenuLeft.setVisible(false);
        menuLeff.hide();
        myProfile.setUser(clientController.getMyUser());
        myProfile.setVisible(true);
        profileScrollPane.setVisible(true);

        PaneProvider.getTransitionBack().setRate(1);
        PaneProvider.getTransitionBack().play();
    }

    public void onHamburgerClicked(MouseEvent mouseEvent) {
        if (profileScrollPane.isVisible()) {
            profileScrollPane.setVisible(false);
            PaneProvider.getTransitionBack().setRate(-1);
            transitionBack.play();
        }
        else if (!menuLeff.isShowing()){
            transition.setRate(1);
            transition.play();
//            menuLeff.show();
            cfxMenuLeft.setVisible(true);
        } else {
            menuLeff.hide();
            cfxMenuLeft.setVisible(false);
        }

    }

    public void onHideMenuLeft(javafx.event.Event event) {
        transition.setRate(-1);

        transition.play();
    }

    @FXML
    public void handleAddButton(){
        clientController.joinGroup(groupName.getText());
    }

    @FXML
    public void handleCreateButton(){
        clientController.addGroup(creategroupName.getText());
    }

    public void findContact(KeyEvent keyEvent) {
        if (tfSearchInput.getText().length()>0) {
            contactsViewPane.setVisible(false);
            contactSearchPane.setVisible(true);
            if (ClientController.getInstance().getAllUserNames().contains(tfSearchInput.getText())){
              CFXListElement newSearchElement = new CFXListElement();
              newSearchElement.setTopic(tfSearchInput.getText());
                searchObsList.add(newSearchElement);
            }
            selectionModel.select(1);
        } else {
            contactsViewPane.setVisible(true);
            contactSearchPane.setVisible(false);
        }

    }

    private void contactSearchButtonInviteClicked() {
        ClientController.getInstance().addContact(tfSearchInput.getText());
        contactSearchButtonCancelClicked();
    }

    private void contactSearchButtonCancelClicked() {
        contactsViewPane.setVisible(true);
        tfSearchInput.setText("");
        contactSearchPane.setVisible(false);
    }


    public void onMouseExitMenu(MouseEvent mouseEvent) {
        cfxMenuLeft.setVisible(false);
        transition.setRate(-1);

        transition.play();
    }

    public void onRightMenuButtonClicked(ActionEvent actionEvent) {
    }


    public void onMouseExitMenuRight(MouseEvent mouseEvent) {
        cfxMenuRightGroup.setVisible(false);
    }

    public void btnRightMenuClicked(ActionEvent actionEvent) {
        if (cfxMenuRightGroup.isVisible()){
            cfxMenuRightGroup.setVisible(false);
        } else {

            cfxMenuRightGroup.setVisible(true);
        }
    }
    public void alarmGroupQuitGroupExecute(){
        new AlarmGroupQuitGroup();
    }

    public void alarmGroupDeleteGroupExecute(){
        new AlarmDeleteGroup();
    }
    public void alarmDeleteMessageHistoryExecute(){
        new AlarmDeleteMessageHistory();
    }
    public void alarmDeleteProfileExecute(){
        new AlarmDeleteProfile();
    }
    public void alarmExirProfileExecute(){
        new AlarmExitProfile();
    }

}
