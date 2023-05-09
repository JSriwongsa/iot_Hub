
package ece448.iot_hub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PlugControllerTests {

    @Mock
    private HubMqttController mqttControllerMock;

    private PlugController plugController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        plugController = new PlugController();
        plugController.mqttController = mqttControllerMock;
    }

    @Test
    public void testGetPlug() {
        when(mqttControllerMock.getState(anyString())).thenReturn("on");
        when(mqttControllerMock.getPower(anyString())).thenReturn("10");
        ResponseEntity<Plug> response = plugController.getPlug("a", null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Plug plug = response.getBody();
        assertEquals("a", plug.getName());
        assertEquals("on", mqttControllerMock.getState(plug.getName()));
        assertEquals("10", mqttControllerMock.getPower(plug.getName()));
    }

    @Test
    public void testGetPlugWithActionOn() {
        when(mqttControllerMock.getState(anyString())).thenReturn("on");
        when(mqttControllerMock.getPower(anyString())).thenReturn("100.000");
        ResponseEntity<Plug> response = plugController.getPlug("a", "on");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Plug plug = response.getBody();
        assertEquals("a", plug.getName());
        assertEquals("on", mqttControllerMock.getState(plug.getName()));
        assertEquals("100.000", mqttControllerMock.getPower(plug.getName()));
    }

    @Test
    public void testGetPlugWithActionOff() {
        when(mqttControllerMock.getState(anyString())).thenReturn("off");
        when(mqttControllerMock.getPower(anyString())).thenReturn("0.000");
        ResponseEntity<Plug> response = plugController.getPlug("a", "off");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Plug plug = response.getBody();
        assertEquals("a", plug.getName());
        assertEquals("off", mqttControllerMock.getState(plug.getName()));
        assertEquals("0.000", mqttControllerMock.getPower(plug.getName()));
    }

    @Test
    public void testGetPlugWithActionToggle() {
        when(mqttControllerMock.getState(anyString())).thenReturn("on");
        when(mqttControllerMock.getPower(anyString())).thenReturn("10.000");
        ResponseEntity<Plug> response = plugController.getPlug("a", "toggle");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Plug plug = response.getBody();
        assertEquals("a", plug.getName());
        assertEquals("on", mqttControllerMock.getState(plug.getName()));
        assertEquals("10.000", mqttControllerMock.getPower(plug.getName()));

        when(mqttControllerMock.getState(anyString())).thenReturn("off");
        when(mqttControllerMock.getPower(anyString())).thenReturn("0.000");
        ResponseEntity<Plug> secondResponse = plugController.getPlug("a", "toggle");
        Plug secondResultPlug = secondResponse.getBody();
        assertEquals("a", secondResultPlug.getName());
        assertEquals("off", mqttControllerMock.getState(secondResultPlug.getName()));
        assertEquals("0.000", mqttControllerMock.getPower(secondResultPlug.getName()));

    }

    @Test
    public void testGetAllPlugs() {
        when(mqttControllerMock.getState(anyString())).thenReturn("on");
        when(mqttControllerMock.getPower(anyString())).thenReturn("10");
        ArrayList<String> plugNames = new ArrayList<>();
        plugNames.add("a");
        plugNames.add("b");
        plugNames.add("c");
        List<Plug> plugs = new ArrayList<>();
        for (int i = 0; i < plugNames.size(); i++){
            String name = plugNames.get(i);
            String state = mqttControllerMock.getState(name);
            String power = mqttControllerMock.getPower(name);
            
            Plug plug = new Plug(name, state, power);
            plugs.add(plug);
            
        }
        ResponseEntity<List<Plug>> response = plugController.getAllPlugs();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        for (int i = 0; i < plugNames.size(); i++) {
            Plug plug = plugs.get(i);
            String name = plug.getName();
            assertEquals(name, plug.getName());
            assertEquals("on", mqttControllerMock.getState(plug.getName()));
            assertEquals("10", mqttControllerMock.getPower(plug.getName()));
        }
    }

    
}
