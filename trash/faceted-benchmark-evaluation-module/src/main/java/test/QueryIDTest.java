package test;

/**
 * Created by hpetzka on 14.12.2016.
 */
public class QueryIDTest {



    private Byte scenario;
    private Byte queryNumber;

    public QueryIDTest(byte scenario, byte queryNumber) {
        super();
        this.scenario = scenario;
        this.queryNumber = queryNumber;
    }

    public QueryIDTest(int scenario, int queryNumber) {
        super();
        this.scenario = (byte) scenario;
        this.queryNumber = (byte) queryNumber;
    }

    public int hashCode() {
        int hashScenario = scenario != null ? scenario.hashCode() : 0;
        int hashQueryNumber = queryNumber != null ? queryNumber.hashCode() : 0;

        return (hashScenario + hashQueryNumber) * hashQueryNumber + hashScenario;
    }

    public boolean equals(Object other) {
        if (other instanceof QueryIDTest) {
            QueryIDTest otherPair = (QueryIDTest) other;
            return
                    (
                            ( this.scenario == otherPair.scenario ||
                                    ( this.scenario != null && otherPair.scenario != null && this.scenario.equals(otherPair.scenario)))
                                    &&
                            ( this.queryNumber == otherPair.queryNumber ||
                                    ( this.queryNumber != null && otherPair.queryNumber != null && this.queryNumber.equals(otherPair.queryNumber)))
                    );
        }
        return false;
    }



    public String toString()
    {
        return "(Scenario, Query number) = ("+  scenario + ", " + queryNumber + ")";
    }

    public Byte getScenario() {
        return scenario;
    }

    public void setScenario(Byte scenario) {
        this.scenario = scenario;
    }

    public Byte getQueryNumber() {
        return queryNumber;
    }

    public void setQueryNumber(Byte queryNumber) {
        this.queryNumber = queryNumber;
    }
}
