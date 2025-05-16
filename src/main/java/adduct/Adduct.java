package adduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        if (mz == null || adduct == null || adduct.isBlank()) return null;

        Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.getOrDefault(
                adduct, AdductList.MAPMZNEGATIVEADDUCTS.get(adduct)); // Busca la masa del aducto en los mapas de aductos positivos o negativos.
        if (adductMass == null) return null;

        int charge = 1;
        int multimer = 1;
        //nicializa carga (z) y el número de moléculas en el aducto (multímero) con valores por defecto.

        Matcher chargeMatcher = Pattern.compile("(\\d+)?([+-])\\]").matcher(adduct); // expresión que detecta la carga positiva o negativa al final del aducto
        if (chargeMatcher.find()) {
            String chargeStr = chargeMatcher.group(1); // si no se especifica una carga, asume que es 1
            charge = (chargeStr == null || chargeStr.isEmpty()) ? 1 : Integer.parseInt(chargeStr);
        }

        Matcher multimerMatcher = Pattern.compile("\\[(\\d*)M").matcher(adduct); // expresión que detecta el numero de moléculas (multímero)
        if (multimerMatcher.find()) {
            String multi = multimerMatcher.group(1);// si no se especifica una carga, asume que es 1
            multimer = (multi == null || multi.isEmpty()) ? 1 : Integer.parseInt(multi);
        }

        double numerator = (mz * charge) + adductMass; //Masa Neutra= (m/z * carga) + masa del aducto
        return numerator / multimer; //devuelve la masa neutra ajustada para el número de moléculas
    }

    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {
        if (monoisotopicMass == null || adduct == null || adduct.isBlank()) return null;

        Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.getOrDefault(
                adduct, AdductList.MAPMZNEGATIVEADDUCTS.get(adduct));
        if (adductMass == null) return null;

        int charge = 1;
        int multimer = 1;

        Matcher chargeMatcher = Pattern.compile("(\\d+)?([+-])\\]").matcher(adduct);
        if (chargeMatcher.find()) {
            String chargeStr = chargeMatcher.group(1);
            charge = (chargeStr == null || chargeStr.isEmpty()) ? 1 : Integer.parseInt(chargeStr);
        }

        Matcher multimerMatcher = Pattern.compile("\\[(\\d*)M").matcher(adduct);
        if (multimerMatcher.find()) {
            String multi = multimerMatcher.group(1);
            multimer = (multi == null || multi.isEmpty()) ? 1 : Integer.parseInt(multi);
        }

        double numerator = (monoisotopicMass * multimer) - adductMass; //m/z = ((masa * multímero) - masa del aducto) / carga
        return numerator / charge;
    }

    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) { //devuelve el error entre la masa experimental y la teórica (partes por millón)
        return (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1_000_000 / theoreticalMass));
    }

    public static double calculateDeltaPPM(Double experimentalMass, int ppm) { //calcula cuánta diferencia de masa es aceptable para un cierto margen en PPM
        return Math.abs((experimentalMass * ppm) / 1_000_000);
    }
}
