import com.serotonin.modbus4j.BasicProcessImage;
import com.serotonin.modbus4j.exception.IllegalDataAddressException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.tcp.TcpSlave;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModbusInterface {

    private static BasicProcessImage basicProcImage;


    /*
        1. Zegar (Mieszko) wpisuje do mojego rejestru (#0) czas.
        2. Zegar (Mieszko) wpisuje do mojego rejestru flagi gotowości (#0).
        3. Zegar (Mieszko) czyta z rejestru flage gotowosci
        4. Wymiennik(Michaly) wpisuje do budynków temperature wody w systemie(#200 - Tzco - woda wchodzaca do budynku).
        5. Budynek(Ja) wpisuje regulatorowi(Mariusz) rejestry (#420 - Tcob - woda wychodzaca z budynku i #422 - Fcob - strumien wody wychodzacej).
        6. Budynek(Ja) wpisuje Loggerowi (Jędrzej) (#420 - Tcob - woda wychodzaca z budynku, #422 - Fcob - strumien wody wychodzacej i #424 - Ub2 - stopien otwarcia zaworów i #426- Tr-temp budynku)).
     */
    private static int time_0; //#0
    private static int time_1;//#1
    private static boolean time_flag;//#0
    private static int To;//#100
    private static int Tzco; //#200
    private static int Tcob; //#420
    private static int Fcob; //#422
    private static int Ub2; //#424
    private static int Tr;//#426
    private static String time;

    public static String toStringStatic() {
        return "time " + time + " time_flag: " + time_flag + " To: " + To+ " Tzco: " + Tzco + " Tcob: " + Tcob + " Fcob: " + Fcob + " Ub2: " + Ub2 + " Tr: " + Tr ;
    }

    public static void main(String[] args) {

        TcpSlave slave = initSlave();

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        slave.start();
                    } catch (ModbusInitException ex) {
                        Logger.getLogger(ModbusInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                Master master = new Master();
                while (true) {
                    try {
                        time_flag = basicProcImage.getCoil(0);
                        if (time_flag == true) {
                            getRegistersValue();
                            Tcob= ThreadLocalRandom.current().nextInt(27315, 37315);
                            Fcob= ThreadLocalRandom.current().nextInt(278, 11111);
                            Ub2=ThreadLocalRandom.current().nextInt(0,100);
                            Tr= ThreadLocalRandom.current().nextInt(27315,30315);
                            master.sendDataToController(Tcob, Fcob);
                            master.sendDataToLogger(Tcob, Fcob, Ub2, Tr);
                            time_flag=false;
                            System.out.println(toStringStatic());
                        }

                    } catch (IllegalDataAddressException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    private static void getRegistersValue() throws IllegalDataAddressException {
        time_flag = basicProcImage.getCoil(Config.TIMER_FLAG_REGISTER);
        time_0 = basicProcImage.getHoldingRegister(Config.TIMER_0_REGISTER);
        time_1 = basicProcImage.getHoldingRegister(Config.TIMER_1_REGISTER);
        Tzco = basicProcImage.getHoldingRegister(Config.T_ZCO_REGISTER);
        Tcob = basicProcImage.getHoldingRegister(Config.T_COB_REGISTER);
        Fcob = basicProcImage.getHoldingRegister(Config.F_COB_REGISTER);
        Ub2 = basicProcImage.getHoldingRegister(Config.UB_REGISTER);
        Tr = basicProcImage.getHoldingRegister(Config.T_R_REGISTER);
        To = basicProcImage.getHoldingRegister(Config.T_O_REGISTER);
        int timeStamp = time_1 + time_0 * 65536;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        time = sdf.format(new Date((long) timeStamp * 1000));
    }

    private static TcpSlave initSlave(){
        basicProcImage = new BasicProcessImage(Config.OWN_SLAVE_ID);
        basicProcImage.setCoil(Config.TIMER_FLAG_REGISTER,false);
        basicProcImage.setHoldingRegister(Config.TIMER_0_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.TIMER_1_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.T_ZCO_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.T_COB_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.F_COB_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.UB_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.T_R_REGISTER, (short) 0);
        basicProcImage.setHoldingRegister(Config.T_O_REGISTER, (short) 0);
        TcpSlave slave = new TcpSlave(Config.PORT, false);
        slave.addProcessImage(basicProcImage);
        return slave;
    }
}