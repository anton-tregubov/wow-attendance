package ru.faulab.attendence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Runner {

    /*
    * 1. Статистика
    * 2. Игроки ушедшии из гильдии не должны попадать в табилуц, а должны помечаться как неактивные с сохранениеи истории
    * */
    public static void main(String[] args) throws Exception {

        String userHome = System.getProperty("user.home");
        System.setProperty("derby.system.home", userHome);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("WoWAttendance");

        CharacterService characterService = new CharacterService(emf, new GuildDataLoader());
        MainFrame mainFrame = new MainFrame(new AttendanceLogParser(), new AttendanceService(emf, characterService), characterService);
        mainFrame.init();
    }


}
