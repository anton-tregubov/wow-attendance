package ru.faulab.attendence.module;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import ru.faulab.attendence.service.*;
import ru.faulab.attendence.ui.AddAttendanceDialog;
import ru.faulab.attendence.ui.AddAttendanceDialogFactory;
import ru.faulab.attendence.ui.AddAttendanceDialogImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(AttendanceLogParser.class).to(AttendanceLogParserImpl.class);
        bind(AttendanceService.class).to(AttendanceServiceImpl.class);
        bind(CharacterService.class).to(CharacterServiceImpl.class);
        bind(GuildDataLoader.class).to(GuildDataLoaderImpl.class);
        bind(AsyncEntityManagerProvider.class).to(AsyncEntityManagerProviderImpl.class);

        bind(ListeningExecutorService.class).toProvider(new Provider<ListeningExecutorService>() {
            @Override
            public ListeningExecutorService get() {
                final ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));
                MoreExecutors.addDelayedShutdownHook(listeningExecutorService, 5, TimeUnit.SECONDS);
                return listeningExecutorService;
            }
        }).asEagerSingleton();

        install(new FactoryModuleBuilder().implement(AddAttendanceDialog.class, AddAttendanceDialogImpl.class).build(AddAttendanceDialogFactory.class));
    }
}
