CREATE KEYSPACE exchangerate with strategy_class =  SimpleStrategy and strategy_options:replication_factor=1;

USE exchangerate;

CREATE TABLE exchangerate (hash varchar PRIMARY KEY, date varchar, currency varchar, rate double);

CREATE INDEX ON exchangerate (currency);
