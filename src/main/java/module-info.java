module org.nmlinh2799 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires java.persistence;
    requires java.sql;
    requires net.bytebuddy;
    requires com.fasterxml.classmate;
    requires java.xml.bind;
    requires jbcrypt;
    requires javatuples;

    opens controllers to javafx.fxml;
    opens controllers.admin to javafx.fxml;
    opens controllers.user to javafx.fxml;
    opens entities to org.hibernate.orm.core, javafx.base;
    exports org.nmlinh2799;
}