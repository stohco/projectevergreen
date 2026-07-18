package dev.ergenverse.origin;
import net.minecraftforge.eventbus.api.Event;
public class AptitudeTestEvent extends Event {
    public enum Result { PASS, FAIL, SPECIAL }
    private Result result = Result.FAIL;
    public Result getTestResult() { return result; }
    public void setTestResult(Result r) { this.result = r; }
}
