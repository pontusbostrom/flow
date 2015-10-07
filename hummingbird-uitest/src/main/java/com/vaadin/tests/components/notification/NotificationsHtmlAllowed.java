package com.vaadin.tests.components.notification;

import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Page;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class NotificationsHtmlAllowed extends TestBase
        implements ClickListener {

    private TextArea messageField;
    private CheckBox htmlAllowedBox;
    private TextField captionField;

    @Override
    protected void setup() {
        captionField = new TextField("Caption", "Hello <u>world</u>");
        add(captionField);
        captionField.focus();

        messageField = new TextArea("Message",
                "Hello <i>world</i>\nWith a newline <br/>And a html line break");
        messageField.setRows(10);
        add(messageField);

        htmlAllowedBox = new CheckBox("Html content allowed", true);
        add(htmlAllowedBox);

        Button showNotification = new Button("Show notification", this);
        add(showNotification);
    }

    @Override
    protected String getTestDescription() {
        return "Test case for htmlAllowed in notifications";
    }

    @Override
    protected Integer getTicketNumber() {
        return 6097;
    }

    @Override
    public void buttonClick(ClickEvent event) {
        Notification n = makeNotification();
        n.show(Page.getCurrent());
    }

    private Notification makeNotification() {
        Notification n = new Notification(captionField.getValue(),
                messageField.getValue(), Type.HUMANIZED_MESSAGE,
                htmlAllowedBox.getValue());
        return n;
    }
}