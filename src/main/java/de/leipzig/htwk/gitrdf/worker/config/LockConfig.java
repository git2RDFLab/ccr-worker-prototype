package de.leipzig.htwk.gitrdf.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

import javax.sql.DataSource;

@Configuration
public class LockConfig {

    private static final int ONE_HOUR = 1000 * 60 * 60;

    @Bean
    public DefaultLockRepository defaultLockRepository(DataSource dataSource) {

        DefaultLockRepository defaultLockRepository = new DefaultLockRepository(dataSource);
        defaultLockRepository.setTimeToLive(ONE_HOUR);

        return defaultLockRepository;
    }

    @Bean
    public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository);
    }

}
