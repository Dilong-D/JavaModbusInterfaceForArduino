import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;

class Master {

    private ModbusMaster master;

    private void initMasterComunication(String host, int port){
        IpParameters ipParameters = new IpParameters();
        ipParameters.setHost(host);
        ipParameters.setPort(port);
        master = new ModbusFactory().createTcpMaster(ipParameters, false);
        try {
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        } finally {
            master.destroy();
        }
    }

    void sendDataToController(int t_cob, int f_cob){
        initMasterComunication(Config.CONTROLLER_IP,Config.PORT);
        writeRegister(master,Config.CONTROLLER_SLAVE_ID,Config.T_COB_REGISTER,t_cob);
        writeRegister(master,Config.CONTROLLER_SLAVE_ID,Config.F_COB_REGISTER,f_cob);

    }

    void sendDataToLogger(int t_cob, int f_cob, int ub2, int tr){
        initMasterComunication(Config.LOGGER_IP,Config.PORT);
        writeRegister(master,Config.LOGGER_SLAVE_ID,Config.T_COB_REGISTER,t_cob);
        writeRegister(master,Config.LOGGER_SLAVE_ID,Config.F_COB_REGISTER,f_cob);
        writeRegister(master,Config.LOGGER_SLAVE_ID,Config.UB_REGISTER,ub2);
        writeRegister(master,Config.LOGGER_SLAVE_ID,Config.T_R_REGISTER,tr);
    }

    private void writeRegister(ModbusMaster master, int slaveId, int offset, int value) {
        try {
            WriteRegisterRequest request = new WriteRegisterRequest(slaveId, offset, value);
            WriteRegisterResponse response = (WriteRegisterResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println("Success");
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

}