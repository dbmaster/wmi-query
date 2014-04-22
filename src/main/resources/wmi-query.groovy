import io.dbmaster.tools.wmi.WMIQuery
import io.dbmaster.tools.wmi.WMIQuery.WMIQueryResult                                 

logger.info("Connection to \\${p_machineName}\${p_namespace}")
WMIQueryResult result = WMIQuery.execute(p_machineName, p_namespace, p_query)
logger.info("Query completed")

println """<table cellspacing="0" class="simple-table" border="1">
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
  