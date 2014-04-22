package io.dbmaster.tools;

import java.util.ArrayList;
import java.util.List;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;

public class WMIQuery{
    
    public static class WMIQueryResult{
        List<String> headers = new ArrayList<String>();
        List<Object[]> values = new ArrayList<Object[]>();
    }
    
    private WMIQuery(){
    }
    
    public static WMIQueryResult execute(String machineName, String namespace, String query){
        WMIQueryResult result = new WMIQueryResult();
        try{
            ComThread.InitMTA();
            ActiveXComponent objLocator = new ActiveXComponent("WbemScripting.SWbemLocator");
            Variant service = objLocator.invoke("ConnectServer", new Variant(machineName), new Variant(namespace));

            ActiveXComponent axWMI = new ActiveXComponent(service.toDispatch());
            Variant vCollection = axWMI.invoke("ExecQuery", new Variant(query));
            
            EnumVariant enumVariant = new EnumVariant(vCollection.toDispatch());
            Dispatch item = null;
            boolean firstLine = true;
            while (enumVariant.hasMoreElements()) {
                item = enumVariant.nextElement().toDispatch();
                
                if (firstLine){
                    firstLine = false;
                    Dispatch dProps = Dispatch.call(item, "Properties_").getDispatch();
                    EnumVariant enProp = new EnumVariant(dProps);
                    while (enProp.hasMoreElements()) {
                        Dispatch dProp = enProp.nextElement().getDispatch();
                        String sProp = Dispatch.get(dProp, "name").getString();
                        result.headers.add(sProp);
                        dProp.safeRelease();
                    }
                    enProp.safeRelease();
                    dProps.safeRelease();
                }
                
                Object[] row = new Object[result.headers.size()];
                for (int i = 0; i< result.headers.size(); ++i){
                    row[i]= Dispatch.call(item, result.headers.get(i)).toJavaObject();
                }
                result.values.add(row);
                
                item.safeRelease();
            }
            enumVariant.safeRelease();
            vCollection.safeRelease();
            axWMI.safeRelease();
            service.safeRelease();
            objLocator.safeRelease();
        } finally {
            ComThread.Release();
        }
        return result;
    }
    
}