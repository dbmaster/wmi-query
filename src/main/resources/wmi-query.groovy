/*
 *  File Version:  $Id: db-data-files.groovy 145 2013-05-22 18:10:44Z schristin $
 */

import io.dbmaster.tools.WMIQuery
import io.dbmaster.tools.WMIQuery.WMIQueryResult

logger.info("Connection to \\${p_machineName}\${p_namespace}")
WMIQueryResult result = WMIQuery.execute(p_machineName, p_namespace, p_query);
logger.info("Query completed")

println "<table border='1'>";
println "<tr>";
result.headers.each { h ->
    println "<th>${h}</th>";
}
println "</tr>";

result.values.each{it ->
    println "<tr>";
    for (int i=0; i<it.length; i++){
        println "<td>${it[i]}</td>";
    }
    println "</tr>";
}
println "</table>";
println "<br/>";
  