import io.dbmaster.testng.BaseToolTestNGCase;
import io.dbmaster.testng.OverridePropertyNames;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Optional;
import org.testng.annotations.Test
import org.testng.annotations.Parameters;

import com.branegy.tools.api.ExportType;

public class WmiQueryIT extends BaseToolTestNGCase {
    @Test
    @Parameters(["db-wmi-query.p_machineName","db-wmi-query.p_namespace","db-wmi-query.p_query"])
    public void testModelExport(@Optional("127.0.0.1") String p_machineName,
        String p_namespace,
        @Optional("Select * From Win32_Process") String p_query) {
        def parameters = [ "p_machineName"  :  p_machineName,
                           "p_namespace" : p_namespace,
                           "p_query" : p_query
                         ]
        String result = tools.toolExecutor("db-wmi-query", parameters).execute()
        assertTrue(result.contains("Results for"), "Unexpected search results ${result}");
    }
}

