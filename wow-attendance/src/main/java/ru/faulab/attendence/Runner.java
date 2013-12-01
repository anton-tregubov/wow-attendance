package ru.faulab.attendence;

import com.google.inject.Guice;
import com.google.inject.Injector;
import ru.faulab.attendence.module.MainModule;
import ru.faulab.attendence.ui.MainFrame;

import javax.swing.*;

public class Runner {

    /*
    * 1. Статистика
    * 2. AsyncFunction  - удалить так как профита никакого.
    * */
    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        String userHome = System.getProperty("user.home");
        System.setProperty("derby.system.home", userHome);

        Injector injector = Guice.createInjector(new MainModule());

        MainFrame mainFrame = injector.getInstance(MainFrame.class);
        mainFrame.init();
    }


}
