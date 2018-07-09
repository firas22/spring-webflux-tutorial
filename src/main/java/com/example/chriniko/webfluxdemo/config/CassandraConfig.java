package com.example.chriniko.webfluxdemo.config;

import com.datastax.driver.core.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

@Configuration
@EnableReactiveCassandraRepositories(basePackages = "com.example.chriniko.webfluxdemo.repository")
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    @Override
    protected String getContactPoints() {
        return "127.0.0.1";
    }

    /*
        Note:

        create keyspace if not exists dbg_keyspace with replication = {'class': 'SimpleStrategy', 'replication_factor':1} AND DURABLE_WRITES = true;

     */
    @Override
    protected String getKeyspaceName() {
        return "dbg_keyspace";
    }

    @Override
    protected int getPort() {
        return 9042;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.RECREATE;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{
                "com.example.chriniko.webfluxdemo.domain"
        };
    }

    @Bean
    public CassandraTemplate cassandraTemplate(Session session) {
        return new CassandraTemplate(session);
    }

}
