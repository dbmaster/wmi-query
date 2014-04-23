import io.dbmaster.tools.wmi.WMIQuery
import io.dbmaster.tools.wmi.WMIQuery.WMIQueryResult
import com.branegy.service.core.QueryRequest
import com.branegy.dbmaster.connection.ConnectionProvider
import com.branegy.dbmaster.connection.Connector
import com.branegy.dbmaster.connection.JdbcConnector
import com.branegy.dbmaster.connection.JDBCDialect
import com.branegy.service.connection.api.ConnectionService
import com.branegy.service.connection.model.DatabaseConnection

import java.sql.Statement;
import java.sql.ResultSet

def runQuery = { host, namespace, query ->
    logger.info("Connecting to \\\\${host}\\${p_namespace}")
    WMIQueryResult result = WMIQuery.execute(host, namespace, query)
    logger.info("Query completed")
    println """<h3>Results for ${host}</h3><table cellspacing="0" class="simple-table" border="1">
               <tr style="background-color:#EEE">"""

    result.headers.each { h -> println "<th>${h}</th>" }
    println "</tr>"

    result.values.each { it ->
        println "<tr>"
        for (int i=0; i<it.length; i++) {
            println "<td>${it[i]}</td>"
        }
        println "</tr>"
    }
    println "</table>"
}

if (p_machineName==null || p_machineName.toLowerCase().startsWith("query:")) { 
    ConnectionService connectionSrv = dbm.getService(ConnectionService.class)
    logger.info("Using query ${p_machineName==null ? null : p_machineName.substring(6)}")
    def request = new QueryRequest(p_machineName==null ? null : p_machineName.substring(6))
    def connections = connectionSrv.getConnectionSlice(request, null)
    def processedHosts = []
    connections.each { connection ->
        try {
            def connector = ConnectionProvider.getConnector(connection)
            def dialect = connector.connect()
      
            if (!(dialect instanceof JDBCDialect) || !((JDBCDialect)dialect).getDialectName().contains("sqlserver")) {
                logger.info("Skipping checks for connection ${connection.getName()} as it is not a database one")
                return
            } else {
                logger.info("Connecting to ${connection.getName()}")
            }
            def jdbcConnection = connector.getJdbcConnection(null)
            dbm.closeResourceOnExit(jdbcConnection)
        
            Statement statement = jdbcConnection.createStatement();
            ResultSet rs = statement.executeQuery("select SERVERPROPERTY('MachineName')")

            if (rs.next()) {
                String machineName = rs.getString(1)
                if (processedHosts.contains(machineName)) {
                    logger.warn("Skipping wmi query for ${connection.getName()}: host ${machineName} already checked")
                } else {
                    logger.info("Running query for host ${machineName}")
                    processedHosts.add(machineName)
                    runQuery(machineName, p_namespace, p_query)
                }
            } else {
                logger.warn("Cannot get machine name for connection ${connection.getName()}")
            }
            jdbcConnection.close()            
        } catch (Exception e) {
            logger.error("Cannot run query for ${connection.getName()}",e)
        }
    }
     
} else {
    runQuery(p_machineName, p_namespace, p_query)
}
    
