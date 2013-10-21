package db;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.*;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import model.ExchangeRate;
import play.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Astyanax client for Cassandra
 */
public class CassandraClient {

    static final ColumnFamily<String, String> CF_EXCHANGE_RATE =
            new ColumnFamily<String, String>(
                    "exchangerate",              // Column Family Name
                    StringSerializer.get(),   // Key Serializer
                    StringSerializer.get());  // Column Serializer

    /**
     * Context initialization for read and write
     * @return Initialized context
     */
    private AstyanaxContext<Keyspace> getContext() {
        AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                .forCluster("Test Cluster")
                .forKeyspace("exchangerate")
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool")
                        .setPort(9160)
                        .setMaxConnsPerHost(1)
                        .setSeeds("127.0.0.1:9160")
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());

        return context;
    }

    /**
     * Read the DB for currency rows
     * @param currency Currency filter
     * @return ExchangeRate list of elements in DB
     */
    public List<ExchangeRate> read(String currency) {
        List<ExchangeRate> rates = new ArrayList<ExchangeRate>();

        AstyanaxContext<Keyspace> context = getContext();

        context.start();

        Keyspace keyspace = context.getEntity();

        try {
            OperationResult<Rows<String, String>> result = keyspace.prepareQuery(CF_EXCHANGE_RATE)
                    .searchWithIndex()
                    .addExpression()
                    .whereColumn("currency").equals().value(currency)
                    .execute();
            for (Row<String, String> row : result.getResult()) {
                ColumnList<String> cols = row.getColumns();

                if (!cols.isEmpty()) {
                    ExchangeRate rate = new ExchangeRate();
                    rate.setDate(cols.getStringValue("date", ""));
                    rate.setCurrency(currency);
                    rate.setRate(cols.getDoubleValue("rate", 0.0));

                    rates.add(rate);
                }
            }

        } catch (ConnectionException e) {
            Logger.error("Error ready rates from Cassandra", e);
        }

        Collections.sort(rates);
        return rates;
    }

    /**
     * Write to the DB rates (coming from feed)
     * @param currency Currency filter (also in each rate, must match)
     * @param rates List of ExchangeRate ites
     * @return true if successful, false if not
     */
    public boolean write(String currency, List<ExchangeRate> rates) {
        boolean success = true;
        AstyanaxContext<Keyspace> context = getContext();

        context.start();

        Keyspace keyspace = context.getEntity();

        try {
            // We use a custom generated ID like USD1

            // So, first we have to find out how many items we have in the DB
            OperationResult<CqlResult<String, String>> resultCount = keyspace.prepareQuery(CF_EXCHANGE_RATE)
                    .withCql("SELECT count(*) FROM exchangerate where currency='" + currency + "';")
                    .execute();

            long count = resultCount.getResult().getRows().getRowByIndex(0).getColumns().getColumnByName("count").getLongValue();

            // With the count, we delete those values (Cassandra won't allow a delete with a secondary index)
            MutationBatch m = keyspace.prepareMutationBatch();
            for (int i = 1; i <= count; i++) {
                m.withRow(CF_EXCHANGE_RATE, currency + Integer.toString(i)).delete();
            }
            OperationResult<Void> result = m.execute();

            // Write the new values
            m = keyspace.prepareMutationBatch();
            int i = 1;
            for (ExchangeRate rate : rates)  {
                m.withRow(CF_EXCHANGE_RATE, rate.getCurrency() + Integer.toString(i))
                        .putColumn("date", rate.getDate(), null)
                        .putColumn("currency", rate.getCurrency(), null)
                        .putColumn("rate", rate.getRate(), null);

                i++;
            }

            result = m.execute();
        } catch (ConnectionException e) {
            Logger.error("Error writing rates to Cassandra", e);
            success = false;
        }

        context.shutdown();

        return success;
    }

}
