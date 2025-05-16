package lipid;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ElutionOrderTest {


    static final Logger LOG = LoggerFactory.getLogger(ElutionOrderTest.class);

    // !!TODO For the adduct detection both regular algorithms or drools can be used as far the tests are passed.


    @Before
    public void setup() {
        // !! TODO Empty by now,you can create common objects for all tests.
    }


    @Test
    public void score1BasedOnRT() {
        // Assume lipids already annotated
        LOG.info("Creating RuleUnit");
        LipidScoreUnit lipidScoreUnit = new LipidScoreUnit();

        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(lipidScoreUnit);


        // TODO CHECK THE Monoisotopic MASSES OF THE COMPOUNDS IN https://chemcalc.org/
        Lipid lipid1 = new Lipid(1, "TG 54:3", "C57H104O6", "TG", 54, 3); // MZ of [M+H]+ = 885.79057
        Lipid lipid2 = new Lipid(2, "TG 52:3", "C55H100O6", "TG", 52, 3); // MZ of [M+H]+ = 857.75927
        Lipid lipid3 = new Lipid(3, "TG 56:3", "C59H108O6", "TG", 56, 3); // MZ of [M+H]+ = 913.82187
        Annotation annotation1 = new Annotation(lipid1, 885.79056, 10E6, 10d);
        Annotation annotation2 = new Annotation(lipid2, 857.7593, 10E7, 9d);
        Annotation annotation3 = new Annotation(lipid3, 913.822, 10E5, 11d);


        LOG.info("Insert data");

        try {
            lipidScoreUnit.getAnnotations().add(annotation1);
            lipidScoreUnit.getAnnotations().add(annotation2);
            lipidScoreUnit.getAnnotations().add(annotation3);

            LOG.info("Run query. Rules are also fired");
            instance.fire();

            // Here the logic that we expect. In this case we expect the full 3 annotations to have a positive score of 1

            assertEquals(1.0, annotation1.getNormalizedScore(), 0.01);
            assertEquals(1.0, annotation2.getNormalizedScore(), 0.01);
            assertEquals(1.0, annotation3.getNormalizedScore(), 0.01);

        }
        finally {
            instance.close();
        }
    }

    @Test
    public void penalizeWrongCarbonElutionOrder() {
        Lipid l1 = new Lipid(1, "TG 54:3", "C57H104O6", "TG", 54, 3); // más carbonos
        Lipid l2 = new Lipid(2, "TG 52:3", "C55H100O6", "TG", 52, 3); // menos carbonos

        //RT mal: lípido con más carbonos eluye antes
        Annotation a1 = new Annotation(l1, 885.79, 1e6, 8.0);
        Annotation a2 = new Annotation(l2, 857.75, 1e6, 10.0);

        LipidScoreUnit unit = new LipidScoreUnit();
        unit.getAnnotations().add(a1);
        unit.getAnnotations().add(a2);

        try (RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            instance.fire();
        }

        assertTrue("A1 debería tener score negativo", a1.getScore() < 0);
        assertTrue("A2 debería tener score negativo", a2.getScore() < 0);
    }

    @Test
    public void penalizeWrongDoubleBondElutionOrder() {
        Lipid l1 = new Lipid(1, "TG 54:1", "C57H106O6", "TG", 54, 1); // menos insaturación
        Lipid l2 = new Lipid(2, "TG 54:3", "C57H104O6", "TG", 54, 3); // más insaturación

        //RT mal: lípido con más insaturación eluye después
        Annotation a1 = new Annotation(l1, 885.79, 1e6, 9.0);
        Annotation a2 = new Annotation(l2, 885.78, 1e6, 11.0);

        LipidScoreUnit unit = new LipidScoreUnit();
        unit.getAnnotations().add(a1);
        unit.getAnnotations().add(a2);

        try (RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            instance.fire();
        }

        assertTrue("Score debería ser negativo por orden incorrecto de insaturación", a1.getScore() < 0 || a2.getScore() < 0);
    }

    @Test
    public void penalizeWrongLipidClassElutionOrder() {
        Lipid l1 = new Lipid(1, "PC 34:1", "C42H82NO8P", "PC", 34, 1); // debería eluir más tarde
        Lipid l2 = new Lipid(2, "PE 34:1", "C42H80NO8P", "PE", 34, 1); // debería eluir antes

        //RT mal: PC eluye antes que PE
        Annotation a1 = new Annotation(l1, 760.60, 1e6, 6.0); // PC
        Annotation a2 = new Annotation(l2, 744.58, 1e6, 8.0); // PE

        LipidScoreUnit unit = new LipidScoreUnit();
        unit.getAnnotations().add(a1);
        unit.getAnnotations().add(a2);

        try (RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit)) {
            instance.fire();
        }

        assertTrue("Score debería ser negativo por orden de clase incorrecto", a1.getScore() < 0 || a2.getScore() < 0);
    }




}

