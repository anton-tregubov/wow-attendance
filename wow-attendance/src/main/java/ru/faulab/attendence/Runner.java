package ru.faulab.attendence;

import com.google.inject.Guice;
import com.google.inject.Injector;
import ru.faulab.attendence.module.MainModule;
import ru.faulab.attendence.ui.MainFrame;

public class Runner {

    /*
    * 1. Статистика
    * */
    public static void main(String[] args) throws Exception {

        String userHome = System.getProperty("user.home");
        System.setProperty("derby.system.home", userHome);

        Injector injector = Guice.createInjector(new MainModule());

        MainFrame mainFrame = injector.getInstance(MainFrame.class);
        mainFrame.init();
    }


}
