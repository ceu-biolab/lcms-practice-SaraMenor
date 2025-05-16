package lipid;

import adduct.Adduct;
import java.util.Map;

public class MassTransformation {

    public static Double calculateNeutralMass(double mz, String adduct) {
        return Adduct.getMonoisotopicMassFromMZ(mz, adduct);
    }

    public static Double calculateMz(double neutralMass, String adduct) {
        return Adduct.getMZFromMonoisotopicMass(neutralMass, adduct);
    }
}
