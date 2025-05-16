package lipid;

import adduct.Adduct;
import adduct.AdductList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private String adduct; // !!TODO The adduct will be detected based on the groupedSignals
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime) {
        this(lipid, mz, intensity, retentionTime, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        // !!TODO This set should be sorted according to help the program to deisotope the signals plus detect the adduct
        this.groupedSignals = new TreeSet<>(groupedSignals); //ya está ordenado
        this.score = 0;
        this.totalScoresApplied = 0;
        this.adduct=detectAdduct(this.groupedSignals);
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String detectAdduct(Set<Peak>groupedSignals) { // detectar un aducto a apartir de un conjunto de peaks

        if (groupedSignals == null || groupedSignals.isEmpty()) return "unknown"; //si no hay señales, devuelve unknown
        List<Peak> peaks = new ArrayList<>(this.groupedSignals); //crea una lista de señales
        double tolerance = 0.01; // se establece una tolerancia de 0.01
        double bestPPM = Double.MAX_VALUE; //el menor error de PPM, ya que hay aductos con valores iguales o parecidos. Se inicializa al maximo para guardar el primer valor
        String finalAdduct = null; //almacena el aducto elegido

        Map<String, Double> allAdducts = new LinkedHashMap<>(); //alamcena los adutos positivos y negativos
        allAdducts.putAll(AdductList.MAPMZPOSITIVEADDUCTS);
        allAdducts.putAll(AdductList.MAPMZNEGATIVEADDUCTS);

        for (String adduct : allAdducts.keySet()) { //recorre los aductor
            for (Peak peak : groupedSignals) { //recorre los picos asociados
                double mzPeak = peak.getMz(); // se obtiene el M/Z


                Double monoMass = Adduct.getMonoisotopicMassFromMZ(mzPeak, adduct); // Se estima la masa monoisotópica que generaría el pico con ese aducto
                if (monoMass == null) continue;


                Double expectedMz = Adduct.getMZFromMonoisotopicMass(monoMass, adduct); // lo mismo con m/z
                if (expectedMz == null) continue;


                double ppmError = Adduct.calculatePPMIncrement(this.mz, expectedMz); // se calcula el error entre el M/Z teórico y el M/Z generado

                if (ppmError < tolerance && ppmError < bestPPM) { //si error<tolerancia y el mas acertado hasta ahora => se guarda el aducto como final
                    bestPPM = ppmError;
                    finalAdduct = adduct;
                }
            }
        }

        return finalAdduct;
    }


    // !TODO Take into account that the score should be normalized between 0 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    public double getNormalizedScore() {
        return (double) this.score / this.totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    // !!TODO Detect the adduct with an algorithm or with drools, up to the user.
}


