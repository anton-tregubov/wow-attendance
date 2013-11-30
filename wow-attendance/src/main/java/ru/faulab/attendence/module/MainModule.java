package ru.faulab.attendence.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import ru.faulab.attendence.service.*;
import ru.faulab.attendence.ui.AddAttendanceDialog;
import ru.faulab.attendence.ui.AddAttendanceDialogFactory;
import ru.faulab.attendence.ui.AddAttendanceDialogImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EntityManagerFactory.class).toProvider(new Provider<EntityManagerFactory>() {
            @Override
            public EntityManagerFactory get() {
                return Persistence.createEntityManagerFactory("WoWAttendance");
            }
        }).in(Scopes.SINGLETON);
        bind(EntityManager.class).toProvider(EntityManagerProvider.class);
        bind(AttendanceLogParser.class).to(AttendanceLogParserImpl.class);
        bind(AttendanceService.class).to(AttendanceServiceImpl.class);
        bind(CharacterService.class).to(CharacterServiceImpl.class);
        bind(GuildDataLoader.class).to(GuildDataLoaderImpl.class);

        install(new FactoryModuleBuilder()
                .implement(AddAttendanceDialog.class, AddAttendanceDialogImpl.class)
                .build(AddAttendanceDialogFactory.class));
    }

    private static class EntityManagerProvider implements Provider<EntityManager> {

        private final EntityManagerFactory entityManagerFactory;

        @Inject
        public EntityManagerProvider(EntityManagerFactory entityManagerFactory) {
            this.entityManagerFactory = entityManagerFactory;
        }

        @Override
        public EntityManager get() {
            return entityManagerFactory.createEntityManager();
        }
    }
}
