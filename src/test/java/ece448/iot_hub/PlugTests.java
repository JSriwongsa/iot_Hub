package ece448.iot_hub;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class PlugTests {

    @Test
    public void testplugConstructor() {
    }

    // testing getters
    @Test
    public void testgetName() {
        Plug plug = new Plug("a", "on", "50");
        assertEquals("a", plug.getName());
    }

    @Test
    public void testgetPower() {
        Plug plug = new Plug("a", "on", "50");
        assertEquals("50", plug.getPower());
    }

    @Test
    public void testgetState() {
        Plug plug = new Plug("a", "on", "50");
        assertEquals("on", plug.getState());
    }

    // testing setters
    @Test
    public void testsetName() {
        Plug plug = new Plug("a", "on", "50");
        assertEquals("a", plug.getName());
        plug.setName("b");
        assertTrue(plug.getName() == "b");
    }

    @Test
    public void testsetPower() {
        Plug plug = new Plug("a", "on", "50");
        assertEquals("50", plug.getPower());
        plug.setPower("100");
        assertTrue(plug.getPower() == "100");
    }

    @Test
    public void testsetState() {
        Plug plug = new Plug("a", "on", "50");
        assertEquals("on", plug.getState());
        plug.setState("off");
        assertTrue(plug.getState() == "off");
    }
}
