package matrix.election.seed;

import java.util.ArrayList;
import java.util.List;

public class GenerateSeeds {
    static String factoryStr = "Electron\n" +
            "Memory chip\n" +
            "CPU/GPU\n" +
            "Analog chip\n" +
            "Automotive MCU\n" +
            "Wireless communication chip\n" +
            "Power semiconductor\n" +
            "Semiconductor equipment\n" +
            "Semiconductor material\n" +
            "Semiconductor manufacturing\n" +
            "Computer\n" +
            "CPU\n" +
            "Operating System\n" +
            "Database\n" +
            "Basic Office Software\n" +
            "Middleware\n" +
            "GIS\n" +
            "EDA\n" +
            "CDA\n" +
            "MES\n" +
            "Communication Main Equipment\n" +
            "Communication Main Equipment\n" +
            "FPGA Chip\n" +
            "Cell Communication Chip\n" +
            "Optical Module\n" +
            "Optical Chip\n" +
            "Electronic Testing Equipment\n" +
            "ADAS\n" +
            "Mechanical\n" +
            "Five-axis Machine Tool\n" +
            "Numerical Control Systems\n" +
            "Tool\n" +
            "Oscilloscope\n" +
            "PLC\n" +
            "Servo Motor\n" +
            "Military\n" +
            "Military Digital Chip\n" +
            "Military Analog Chip\n" +
            "GBT for Military Use\n" +
            "High Temperature Alloy\n" +
            "Carbon Fiber\n" +
            "Medicine\n" +
            "Culture Medium\n" +
            "Scientific Instruments\n" +
            "Biological Reagent\n" +
            "Chromatographic Packing Material\n" +
            "Medium borosilicate drug\n" +
            "Chemiluminescence\n" +
            "Biochemical Detection\n" +
            "CT Scanner\n" +
            "MR Scanner\n" +
            "Orthokeratology lenses\n" +
            "Continuous Dynamic Blood Analyzer\n" +
            "Oral Repair Film\n" +
            "Electrophysiology\n" +
            "Soft Endoscope\n" +
            "Automotive\n" +
            "Suspension\n" +
            "Braking System\n" +
            "Seat\n" +
            "Passive Safety\n" +
            "micromotor\n" +
            "Controller\n" +
            "Perception algorithm\n" +
            "Tire\n" +
            "bearing\n" +
            "Aluminum plastic film\n" +
            "Proton exchange membrane\n" +
            "Aramid material\n" +
            "Metal\n" +
            "Special aluminum alloy\n" +
            "Superalloy\n" +
            "Chemical engineering\n" +
            "Optical film\n" +
            "China six tail gas catalysis\n" +
            "Aramid fiber\n" +
            "Conductive carbon black\n" +
            "EVA particle\n" +
            "tyre\n" +
            "Aluminum-plastic film\n" +
            "photoresist\n" +
            "POE\n" +
            "Special engineering plastics\n" +
            "Carbon fiber\n" +
            "Polyimide\n" +
            "Ultra high molecular weight polyethylene\n" +
            "Original car paint\n" +
            "3C coating\n" +
            "OLED terminal material\n";

    static String peopleStr = "Kamala Harris\n" +
            "Dick Cheney\n" +
            "Liz Cheney\n" +
            "Morgan Finkelstein\n" +
            "Ron DeSantis\n" +
            "Robert F Kennedy Jr\n" +
            "Tim Walz\n" +
            "Donald Trump\n" +
            "Mark Cuban\n" +
            "Nikki Haley\n" +
            "Mitt Romney\n" +
            "J D Vance\n" +
            "Stephen Miller\n" +
            "Ron DeSantis\n";

    public static List<String> getAllSeeds() {
        ArrayList<String> list = new ArrayList<>();
        String[] factory = factoryStr.split("\n");
        String[] person = peopleStr.split("\n");
        for(String p : person) {
            for(String f : factory) {
                list.add(p+" "+f);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        List<String> list = getAllSeeds();
        long e = System.currentTimeMillis();
        System.out.println(list);
        System.out.println(list.size());
        System.out.println(e-s);
    }
}
