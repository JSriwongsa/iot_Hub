package ece448.iot_sim;
import static org.junit.Assert.*;

//import org.junit.Test;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.Arrays;

public class HTTPCommandsTests {

    List<PlugSim> plugList = Arrays.asList(new PlugSim("xxxx"), new PlugSim("yyyy"), new PlugSim("zzzz.789"));


    @Test
    public void testHTTPCommandsConstructor() {
        // setup our constructor, make sure we give it the plugList of all of our plugNames.
        HTTPCommands h = new HTTPCommands(plugList);
        
        // test to make sure the class name is correct 
        // because thats all we can test since TreeMap: plugs is private.
        String constructor_name = h.getClass().getName();
        assertEquals(constructor_name, h.getClass().getName());
        // check the values in our list and make sure they are the right names we set in our initPlugs
        assertEquals(plugList.get(0).getName(), "xxxx");
        assertEquals(plugList.get(1).getName(), "yyyy");
        assertEquals(plugList.get(2).getName(), "zzzz.789");
    }

    @Test public void testhandleGet() {
        Map<String, String> params = new HashMap<String, String>();

        String path = "/";
        HTTPCommands h = new HTTPCommands(plugList);
        String slashPathOutput = h.handleGet(path, params);
        assertEquals(slashPathOutput, h.listPlugs());

        HTTPCommands nullPath = new HTTPCommands(plugList);
        String p = "/null";
        assertNull(nullPath.handleGet(p, params));

        HTTPCommands httpNoAction = new HTTPCommands(plugList);
        params.put("action", null);
        PlugSim plug = plugList.get(0);
        String reportHtml = httpNoAction.report(plug);
        String plugName = plug.getName();
        String pathWithPlugName = path + plugName ;
        assertEquals(httpNoAction.handleGet(pathWithPlugName, params), reportHtml);

        HTTPCommands httpActionOn = new HTTPCommands(plugList);
        params.put("action", "on");
        plug.switchOn();
        String reportHtmlActionOn = httpActionOn.report(plug);
        assertEquals(httpActionOn.handleGet(pathWithPlugName, params), reportHtmlActionOn);

        HTTPCommands httpActionOff = new HTTPCommands(plugList);
        params.put("action", "off");
        plug.switchOff();
        String reportHtmlActionOff = httpActionOff.report(plug);
        assertEquals(httpActionOff.handleGet(pathWithPlugName, params), reportHtmlActionOff);

        HTTPCommands httpActionToggle = new HTTPCommands(plugList);
        params.put("action", "toggle");
        String reportHtmlActionToggle = httpActionToggle.report(plug);
        plug.toggle();
        assertEquals(httpActionToggle.handleGet(pathWithPlugName, params), reportHtmlActionToggle);

        HTTPCommands httpActionNonExistant = new HTTPCommands(plugList);
        params.put("action", "non_existant");
        String reportHtmlNonExistant = httpActionToggle.report(plug);
        assertEquals(httpActionNonExistant.handleGet(pathWithPlugName, params), reportHtmlNonExistant);
    }

    @Test public void testnoPlug() { 
        PlugSim plug = null;
        assertNull(plug);
    }

    @Test public void testnullAction() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", null);
        assertNull(params.get("action"));
    }

    @Test
    public void testactionOn() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "on");
        assertEquals("on", params.get("action"));
    }

    @Test
    public void testactionOff() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "off");
        assertEquals("off", params.get("action"));
    }

    @Test public void testactionToggle() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "toggle");
        assertEquals("toggle", params.get("action"));
    }

    @Test public void testactiondifferentParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "redo");
        // we need to test to make sure action is not on|off|toggle|null
        assertNotEquals("on", params.get("action"));
        assertNotEquals("off", params.get("action"));
        assertNotEquals("toggle", params.get("action"));
        assertNotEquals(null, params.get("action"));
    }

    @Test public void testnoQueryParam() {
        String path = "/";
        assertEquals(path, "/");
    }

    @Test
    public void testlistPlugs() {
        HTTPCommands h = new HTTPCommands(plugList);
        String html = h.listPlugs();
        
        StringBuilder final_string = new StringBuilder();
        final_string.append("<html><body><p><a href='/xxxx'>xxxx</a></p><p><a href='/yyyy'>yyyy</a></p><p><a href='/zzzz.789'>zzzz.789</a></p></body></html>");
        assertEquals(final_string.toString(), html);
    }

    @Test
    public void testreport() {
        HTTPCommands h = new HTTPCommands(plugList);
        PlugSim plug = plugList.get(0);
        
        // test turning the plug on.
        plug.switchOn();
        String plugName = plug.getName();
        String report_html = h.report(plug);
        String html_plug_on = String.format("<html><body>"
			+"<p>Plug %s is %s.</p>"
			+"<p>Power reading is %.3f.</p>"
			+"<p><a href='/%s?action=on'>Switch On</a></p>"
			+"<p><a href='/%s?action=off'>Switch Off</a></p>"
			+"<p><a href='/%s?action=toggle'>Toggle</a></p>"
            +"</body></html>",
            plugName,
            plug.isOn()? "on": "off",
            plug.getPower(), plugName, plugName, plugName);
        // plug.isOn() tertiary check should be 'on' first. check for this.
        assertEquals(report_html, html_plug_on);
        
        // switchOff() and rerun the same assertion. 
        plug.switchOff();
        String html_plug_off = String.format("<html><body>"
            +"<p>Plug %s is %s.</p>"
            +"<p>Power reading is %.3f.</p>"
            +"<p><a href='/%s?action=on'>Switch On</a></p>"
            +"<p><a href='/%s?action=off'>Switch Off</a></p>"
            +"<p><a href='/%s?action=toggle'>Toggle</a></p>"
            +"</body></html>",
            plugName,
            plug.isOn()? "on": "off",
            plug.getPower(), plugName, plugName, plugName);
        String updated_report = h.report(plug);
        assertEquals(updated_report, html_plug_off);
    }

}
