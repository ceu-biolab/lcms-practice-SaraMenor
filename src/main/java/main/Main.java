package main;

import adduct.Adduct;
import lipid.*;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String adduct = "[M+2H]2+";
        double mz = 350.754;

        Double mass = Adduct.getMonoisotopicMassFromMZ(mz, adduct);
        Double recalculatedMz = Adduct.getMZFromMonoisotopicMass(mass, adduct);

        System.out.printf("Neutral mass: %.6f%n", mass);
        System.out.printf("Recalculated m/z: %.6f%n", recalculatedMz);

    }
}